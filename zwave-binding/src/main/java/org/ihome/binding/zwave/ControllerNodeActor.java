package org.ihome.binding.zwave;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.ihome.springactor.Actor;

/**
 *
 */
@Actor("ControllerNodeActor")
public class ControllerNodeActor extends NodeActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
        log.debug(message.toString());


    }
}
