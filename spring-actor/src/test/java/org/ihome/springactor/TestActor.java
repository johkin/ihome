package org.ihome.springactor;

import akka.actor.UntypedActor;

/**
 *
 */
@Actor("TestActor")
public class TestActor extends UntypedActor {

    public static class Count {}
    public static class Get {}


    private int count = 0;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Count) {
            count++;
        } else if (message instanceof Get) {
            getSender().tell(count, getSelf());
        } else {
            unhandled(message);
        }


    }
}
