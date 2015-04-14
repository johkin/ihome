package org.ihome.binding.zwave;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.ihome.springactor.SpringExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 */
public class ManagerActorMain {



    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("org.ihome");

        ActorSystem actorSystem = context.getBean(ActorSystem.class);


        SpringExtension springExtension = context.getBean(SpringExtension.class);

        final ActorRef managerActor = actorSystem.actorOf(springExtension.props("ManagerActor"));

        //managerActor.tell(new ManagerActor.InitManager("/dev/cu.usbmodem1411", "/Users/johan/Downloads/openzwave-1.3.1000/config"), null);
        managerActor.tell(new ManagerActor.InitManager("/dev/cu.usbmodem1411", "./build/zwave-config"), null);

        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            do {
                line = br.readLine();
                if (line == null) {
                    continue;
                }

                switch (line) {
                    case "on":
                        managerActor.tell(new ManagerActor.AllOn(), null);
                        break;
                    case "off":
                        managerActor.tell(new ManagerActor.AllOff(), null);
                        break;
                }
            } while (line != null && !line.equals("q"));

            managerActor.tell(new ManagerActor.StopManager(), null);

            Thread.sleep(3000L);

            actorSystem.shutdown();

            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

}