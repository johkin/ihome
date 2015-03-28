package org.ihome.binding.zwave;

import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import org.ihome.springactor.Actor;
import org.zwave4j.Manager;

import javax.inject.Inject;

/**
 *
 */
@Actor("ManagerActor")
public class ManagerActor extends UntypedActor {

    @Inject
    private ActorSystem actorSystem;

    @Inject
    private Manager manager;

    @Override
    public void onReceive(Object o) throws Exception {

    }
}
