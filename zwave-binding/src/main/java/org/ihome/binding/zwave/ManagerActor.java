package org.ihome.binding.zwave;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.ihome.springactor.Actor;
import org.ihome.springactor.SpringExtension;
import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.NotificationWatcher;
import org.zwave4j.ValueId;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
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
    private SpringExtension springExtension;

    private long homeId;
    private boolean ready;

    private NotificationWatcher watcher;

    private String controllerPort;

    private Map<Short, ActorRef> nodeRegistry = new HashMap<>();

    private Manager manager;

    @Override
    public void preStart() throws Exception {
        manager = Manager.get();

        watcher = new NotificationWatcher() {
            @Override
            public void onNotification(Notification notification, Object context) {
                self().tell(notification, null);
            }
        };
        manager.addWatcher(watcher, null);
    }

    @Override
    public void postStop() throws Exception {
        if (watcher != null) {
        //    manager.removeWatcher(watcher, null);
            watcher = null;
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof InitManager) {

            InitManager initMessage = (InitManager) message;

            controllerPort = initMessage.controllerPort;

            manager.addDriver(controllerPort);

        } else if (message instanceof StopManager) {
/*
            if (watcher != null) {
                manager.removeWatcher(watcher, null);
                watcher = null;
            }
*/
            manager.removeDriver(controllerPort);

        } else if (message instanceof AllOn) {
            log.debug("Switch all on");
            manager.switchAllOn(homeId);
            log.debug("Switched on");
        } else if (message instanceof AllOff) {
            log.debug("Switch all off");
            manager.switchAllOff(homeId);
            log.debug("Switched off");
        } else if (message instanceof Notification) {
            getWatcher().onNotification((Notification) message, null);
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
                        final String nodeType = manager.getNodeType(notification.getHomeId(), notification.getNodeId());
                        log.debug(String.format("Node protocol info\n" +
                                        "\tnode id: %d\n" +
                                        "\ttype: %s",
                                notification.getNodeId(),
                                nodeType));
                        ActorRef nodeActor = null;
                        switch (nodeType) {
                            case "Static PC Controller":
                                nodeActor = getContext().actorOf(springExtension.props("ControllerNodeActor"));
                                break;
                            case "Binary Power Switch":
                                nodeActor = getContext().actorOf(springExtension.props("BinarySwitchNodeActor"));
                                break;
                        }
                        nodeRegistry.put(notification.getNodeId(), nodeActor);
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
                        nodeRegistry.get(notification.getNodeId()).tell(new NodeActor.ValueAdded(notification.getValueId()), self());
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
                        nodeRegistry.get(notification.getNodeId()).tell(new NodeActor.ValueChanged(notification.getValueId()), self());
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
                        nodeRegistry.get(notification.getNodeId()).tell(new NodeActor.ValueChanged(notification.getValueId()), self());
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
                        nodeRegistry.get(notification.getNodeId()).tell(new NodeActor.ValueRefreshed(notification.getValueId()), self());
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
