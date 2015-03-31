package org.ihome.springactor;

import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.inject.Named;

/**
 * The application configuration.
 */
@Configuration
@ComponentScan("org.ihome.springactor")
class AppConfiguration {

    // the application context is needed to initialize the Akka Spring Extension
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpringExtension springExtension;

    /**
     * Actor system singleton for this application.
     */
    @Bean
    @Named("ActorSystem")
    public ActorSystem actorSystem() {
        ActorSystem system = ActorSystem.create("ihome-system");
        springExtension.initialize(applicationContext);
        return system;
    }
}
