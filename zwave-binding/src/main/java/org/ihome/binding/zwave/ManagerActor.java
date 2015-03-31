package org.ihome.binding.zwave;

import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import org.ihome.springactor.Actor;
import org.ihome.springactor.SpringExtension;
import org.zwave4j.*;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
@Actor("ManagerActor")
public class ManagerActor extends UntypedActor {

    @Inject
    private ActorSystem actorSystem;


    private Manager manager;
    @Inject
    private SpringExtension springExtension;

    private long homeId;
    private boolean ready;

    private NotificationWatcher watcher;

    private String controllerPort;

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof InitManager) {

            InitManager initMessage = (InitManager) message;
            final Options options = Options.create(initMessage.configPath, "", "");
            options.addOptionBool("ConsoleOutput", false);
            options.lock();

            manager = Manager.create();

            boolean result = manager.addDriver(initMessage.controllerPort);
            if (result) {
                System.out.println("Driver added!");
            }

            watcher = new NotificationWatcher() {
                @Override
                public void onNotification(Notification notification, Object context) {

                    switch (notification.getType()) {
                        case DRIVER_READY:
                            System.out.println(String.format("Driver ready\n" +
                                            "\thome id: %d",
                                    notification.getHomeId()
                            ));
                            homeId = notification.getHomeId();
                            break;
                        case DRIVER_FAILED:
                            System.out.println("Driver failed");
                            break;
                        case DRIVER_RESET:
                            System.out.println("Driver reset");
                            break;
                        case AWAKE_NODES_QUERIED:
                            System.out.println("Awake nodes queried");
                            break;
                        case ALL_NODES_QUERIED:
                            System.out.println("All nodes queried");
                            manager.writeConfig(homeId);
                            ready = true;
                            break;
                        case ALL_NODES_QUERIED_SOME_DEAD:
                            System.out.println("All nodes queried some dead");
                            manager.writeConfig(homeId);
                            ready = true;
                            break;
                        case POLLING_ENABLED:
                            System.out.println("Polling enabled");
                            break;
                        case POLLING_DISABLED:
                            System.out.println("Polling disabled");
                            break;
                        case NODE_NEW:
                            System.out.println(String.format("Node new\n" +
                                            "\tnode id: %d",
                                    notification.getNodeId()
                            ));
                            break;
                        case NODE_ADDED:
                            System.out.println(String.format("Node added\n" +
                                            "\tnode id: %d",
                                    notification.getNodeId()
                            ));
                            break;
                        case NODE_REMOVED:
                            System.out.println(String.format("Node removed\n" +
                                            "\tnode id: %d",
                                    notification.getNodeId()
                            ));
                            break;
                        case ESSENTIAL_NODE_QUERIES_COMPLETE:
                            System.out.println(String.format("Node essential queries complete\n" +
                                            "\tnode id: %d",
                                    notification.getNodeId()
                            ));
                            break;
                        case NODE_QUERIES_COMPLETE:
                            System.out.println(String.format("Node queries complete\n" +
                                            "\tnode id: %d",
                                    notification.getNodeId()
                            ));
                            break;
                        case NODE_EVENT:
                            System.out.println(String.format("Node event\n" +
                                            "\tnode id: %d\n" +
                                            "\tevent id: %d",
                                    notification.getNodeId(),
                                    notification.getEvent()
                            ));
                            break;
                        case NODE_NAMING:
                            System.out.println(String.format("Node naming\n" +
                                            "\tnode id: %d",
                                    notification.getNodeId()
                            ));
                            break;
                        case NODE_PROTOCOL_INFO:
                            System.out.println(String.format("Node protocol info\n" +
                                            "\tnode id: %d\n" +
                                            "\ttype: %s",
                                    notification.getNodeId(),
                                    manager.getNodeType(notification.getHomeId(), notification.getNodeId())
                            ));
                            break;
                        case VALUE_ADDED:
                            System.out.println(String.format("Value added\n" +
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
                            System.out.println(String.format("Value removed\n" +
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
                            System.out.println(String.format("Value changed\n" +
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
                            System.out.println(String.format("Value refreshed\n" +
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
                            System.out.println(String.format("Group\n" +
                                            "\tnode id: %d\n" +
                                            "\tgroup id: %d",
                                    notification.getNodeId(),
                                    notification.getGroupIdx()
                            ));
                            break;

                        case SCENE_EVENT:
                            System.out.println(String.format("Scene event\n" +
                                            "\tscene id: %d",
                                    notification.getSceneId()
                            ));
                            break;
                        case CREATE_BUTTON:
                            System.out.println(String.format("Button create\n" +
                                            "\tbutton id: %d",
                                    notification.getButtonId()
                            ));
                            break;
                        case DELETE_BUTTON:
                            System.out.println(String.format("Button delete\n" +
                                            "\tbutton id: %d",
                                    notification.getButtonId()
                            ));
                            break;
                        case BUTTON_ON:
                            System.out.println(String.format("Button on\n" +
                                            "\tbutton id: %d",
                                    notification.getButtonId()
                            ));
                            break;
                        case BUTTON_OFF:
                            System.out.println(String.format("Button off\n" +
                                            "\tbutton id: %d",
                                    notification.getButtonId()
                            ));
                            break;
                        case NOTIFICATION:
                            System.out.println("Notification");
                            break;
                        default:
                            System.out.println(notification.getType().name());
                            break;
                    }
                }
            };

            manager.addWatcher(watcher, null);

        } else if (message instanceof StopManager) {
            manager.removeWatcher(watcher, null);
            manager.removeDriver(controllerPort);
            Manager.destroy();
            Options.destroy();
        }
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
}
