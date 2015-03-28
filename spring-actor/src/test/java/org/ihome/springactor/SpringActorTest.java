package org.ihome.springactor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfiguration.class)
public class SpringActorTest {

    @Inject
    private ActorSystem actorSystem;

    @Inject
    private SpringExtension springExtension;

    @Test
    public void testSpring() throws Exception {
        ActorRef testActor = actorSystem.actorOf(springExtension.props("TestActor"), "testActor");


        // tell it to count three times
        testActor.tell(new TestActor.Count(), null);
        testActor.tell(new TestActor.Count(), null);
        testActor.tell(new TestActor.Count(), null);

        // check that it has counted correctly
        FiniteDuration duration = FiniteDuration.create(3, TimeUnit.SECONDS);
        Future<Object> result = ask(testActor, new TestActor.Get(),
                Timeout.durationToTimeout(duration));
        assertEquals(3, Await.result(result, duration));
    }
}
