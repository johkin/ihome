package org.ihome.binding.zwave;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.ihome.springactor.Actor;

/**
 *
 */
@Actor("NodeActor")
public abstract class NodeActor extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    short nodeId;


}
