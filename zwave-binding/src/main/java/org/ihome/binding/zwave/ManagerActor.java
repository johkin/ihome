package org.ihome.binding.zwave;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.ihome.springactor.Actor;
import org.ihome.springactor.SpringExtension;
import org.zwave4j.*;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
@Actor("ManagerActor")
public class ManagerActor extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Inject
    private ActorSystem actorSystem;

    @Inject
    private ExecutorService executorService;

    private Manager manager;
    @Inject
    private SpringExtension springExtension;

    private long homeId;
    private boolean ready;

    private NotificationWatcher watcher;

    private String controllerPort;

    private Map<Short, ActorRef> nodeRegistry = new HashMap<>();

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof InitManager) {

            InitManager initMessage = (InitManager) message;

            watcher = getWatcher();

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    final Thread thread = Thread.currentThread();
                    System.out.println(thread.getName());

                    File file = new File(initMessage.configPath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    final Options options = Options.create(file.getAbsolutePath(), "", "");
                    options.addOptionBool("ConsoleOutput", false);
                    options.lock();

                    manager = Manager.create();

                    boolean result = manager.addDriver(initMessage.controllerPort);
                    if (result) {
                        log.debug("Driver added!");
                    }
                    manager.addWatcher(watcher, null);

                }
            });

        } else if (message instanceof StopManager) {

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    final Thread thread = Thread.currentThread();
                    System.out.println(thread.getName());

                    log.debug("Stopping manager");
                    // manager.removeWatcher(watcher, null);
                    log.debug("Removed watcher");
                    // manager.removeDriver(controllerPort);
                    log.debug("Manager destroy");
                    Manager.destroy();
                    log.debug("Options destroy");
                    Options.destroy();
                    log.debug("Manager stopped");
                }
            });

        } else if (message instanceof AllOn) {
            log.debug("Switch all on");
            manager.switchAllOn(homeId);
            log.debug("Switched on");
        } else if (message instanceof AllOff) {
            log.debug("Switch all off");
            manager.switchAllOff(homeId);
            log.debug("Switched off");
        }

    }

    private NotificationWatcher getWatcher() {
        return new NotificationWatcher() {
            @Override
            public void onNotification(Notification notification, Object context) {

                switch (notification.getType()) {
                    case DRIVER_READY:
                        log.debug(String.format("Driver ready\n" +
                                        "\thome id: %d",
                                notification.getHomeId()
                        ));
                        homeId = notification.getHomeId();
                        break;
                    case DRIVER_FAILED:
                        log.debug("Driver failed");
                        break;
                    case DRIVER_RESET:
                        log.debug("Driver reset");
                        break;
                    case AWAKE_NODES_QUERIED:
                        log.debug("Awake nodes queried");
                        break;
                    case ALL_NODES_QUERIED:
                        log.debug("All nodes queried");
                        manager.writeConfig(homeId);
                        ready = true;
                        break;
                    case ALL_NODES_QUERIED_SOME_DEAD:
                        log.debug("All nodes queried some dead");
                        manager.writeConfig(homeId);
                        ready = true;
                        break;
                    case POLLING_ENABLED:
                        log.debug("Polling enabled");
                        break;
                    case POLLING_DISABLED:
                        log.debug("Polling disabled");
                        break;
                    case NODE_NEW:
                        log.debug(String.format("Node new\n" +
                                        "\tnode id: %d",
                                notification.getNodeId()
                        ));
                        break;
                    case NODE_ADDED:
                        log.debug(String.format("Node added\n" +
                                        "\tnode id: %d",
                                notification.getNodeId()
                        ));
                        break;
                    case NODE_REMOVED:
                        log.debug(String.format("Node removed\n" +
                                        "\tnode id: %d",
                                notification.getNodeId()
                        ));
                        break;
                    case ESSENTIAL_NODE_QUERIES_COMPLETE:
                        log.debug(String.format("Node essential queries complete\n" +
                                        "\tnode id: %d",
                                notification.getNodeId()
                        ));
                        break;
                    case NODE_QUERIES_COMPLETE:
                        log.debug(String.format("Node queries complete\n" +
                                        "\tnode id: %d",
                                notification.getNodeId()
                        ));
                        break;
                    case NODE_EVENT:
                        log.debug(String.format("Node event\n" +
                                        "\tnode id: %d\n" +
                                        "\tevent id: %d",
                                notification.getNodeId(),
                                notification.getEvent()
                        ));
                        break;
                    case NODE_NAMING:
                        log.debug(String.format("Node naming\n" +
                                        "\tnode id: %d",
                                notification.getNodeId()
                        ));
                        break;
                    case NODE_PROTOCOL_INFO:
                        log.debug(String.format("Node protocol info\n" +
                                        "\tnode id: %d\n" +
                                        "\ttype: %s",
                                notification.getNodeId(),
                                manager.getNodeType(notification.getHomeId(), notification.getNodeId())
                        ));
                        break;
                    case VALUE_ADDED:
                        log.debug(String.format("Value added\n" +
                                        "\tnode id: %d\n" +
                                        "\tcommand class: %d\n" +
                                        "\tinstance: %d\n" +
                                        "\tindex: %d\n" +
                                        "\tgenre: %s\n" +
                                        "\ttype: %s\n" +
                                        "\tlabel: %s\n" +
                                        "\tvalue: %s",
                                notification.getNodeId(),
                                notification.getValueId().getCommandClassId(),
                                notification.getValueId().getInstance(),
                                notification.getValueId().getIndex(),
                                notification.getValueId().getGenre().name(),
                                notification.getValueId().getType().name(),
                                manager.getValueLabel(notification.getValueId()),
                                getValue(notification.getValueId())
                        ));
                        break;
                    case VALUE_REMOVED:
                        log.debug(String.format("Value removed\n" +
                                        "\tnode id: %d\n" +
                                        "\tcommand class: %d\n" +
                                        "\tinstance: %d\n" +
                                        "\tindex: %d",
                                notification.getNodeId(),
                                notification.getValueId().getCommandClassId(),
                                notification.getValueId().getInstance(),
                                notification.getValueId().getIndex()
                        ));
                        break;
                    case VALUE_CHANGED:
                        log.debug(String.format("Value changed\n" +
                                        "\tnode id: %d\n" +
                                        "\tcommand class: %d\n" +
                                        "\tinstance: %d\n" +
                                        "\tindex: %d\n" +
                                        "\tvalue: %s",
                                notification.getNodeId(),
                                notification.getValueId().getCommandClassId(),
                                notification.getValueId().getInstance(),
                                notification.getValueId().getIndex(),
                                getValue(notification.getValueId())
                        ));
                        break;
                    case VALUE_REFRESHED:
                        log.debug(String.format("Value refreshed\n" +
                                        "\tnode id: %d\n" +
                                        "\tcommand class: %d\n" +
                                        "\tinstance: %d\n" +
                                        "\tindex: %d" +
                                        "\tvalue: %s",
                                notification.getNodeId(),
                                notification.getValueId().getCommandClassId(),
                                notification.getValueId().getInstance(),
                                notification.getValueId().getIndex(),
                                getValue(notification.getValueId())
                        ));
                        break;
                    case GROUP:
                        log.debug(String.format("Group\n" +
                                        "\tnode id: %d\n" +
                                        "\tgroup id: %d",
                                notification.getNodeId(),
                                notification.getGroupIdx()
                        ));
                        break;

                    case SCENE_EVENT:
                        log.debug(String.format("Scene event\n" +
                                        "\tscene id: %d",
                                notification.getSceneId()
                        ));
                        break;
                    case CREATE_BUTTON:
                        log.debug(String.format("Button create\n" +
                                        "\tbutton id: %d",
                                notification.getButtonId()
                        ));
                        break;
                    case DELETE_BUTTON:
                        log.debug(String.format("Button delete\n" +
                                        "\tbutton id: %d",
                                notification.getButtonId()
                        ));
                        break;
                    case BUTTON_ON:
                        log.debug(String.format("Button on\n" +
                                        "\tbutton id: %d",
                                notification.getButtonId()
                        ));
                        break;
                    case BUTTON_OFF:
                        log.debug(String.format("Button off\n" +
                                        "\tbutton id: %d",
                                notification.getButtonId()
                        ));
                        break;
                    case NOTIFICATION:
                        log.debug("Notification");
                        break;
                    default:
                        log.debug(notification.getType().name());
                        break;
                }
            }
        };
    }

    private Object getValue(ValueId valueId) {
        switch (valueId.getType()) {
            case BOOL:
                AtomicReference<Boolean> b = new AtomicReference<>();
                manager.getValueAsBool(valueId, b);
                return b.get();
            case BYTE:
                AtomicReference<Short> bb = new AtomicReference<>();
                manager.getValueAsByte(valueId, bb);
                return bb.get();
            case DECIMAL:
                AtomicReference<Float> f = new AtomicReference<>();
                manager.getValueAsFloat(valueId, f);
                return f.get();
            case INT:
                AtomicReference<Integer> i = new AtomicReference<>();
                manager.getValueAsInt(valueId, i);
                return i.get();
            case LIST:
                return null;
            case SCHEDULE:
                return null;
            case SHORT:
                AtomicReference<Short> s = new AtomicReference<>();
                manager.getValueAsShort(valueId, s);
                return s.get();
            case STRING:
                AtomicReference<String> ss = new AtomicReference<>();
                manager.getValueAsString(valueId, ss);
                return ss.get();
            case BUTTON:
                return null;
            case RAW:
                AtomicReference<short[]> sss = new AtomicReference<>();
                manager.getValueAsRaw(valueId, sss);
                return sss.get();
            default:
                return null;
        }
    }

    static class InitManager {
        private final String controllerPort;
        private final String configPath;

        public InitManager(String controllerPort, String configPath) {
            this.controllerPort = controllerPort;
            this.configPath = configPath;
        }

        public String getControllerPort() {
            return controllerPort;
        }

        public String getConfigPath() {
            return configPath;
        }
    }

    static class StopManager {

    }

    static class AllOn {
    }

    static class AllOff {
    }
}
