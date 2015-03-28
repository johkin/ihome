package org.ihome.springactor;

import akka.actor.Extension;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * An Akka Extension to provide access to Spring managed Actor Beans.
 */
@Component
public class SpringExtension implements Extension {

  private ApplicationContext applicationContext;

  public void initialize(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public Props props(String actorBeanName) {
    return Props.create(SpringActorProducer.class,
            applicationContext, actorBeanName);
  }
}
