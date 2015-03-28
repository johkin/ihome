package org.ihome.binding.zwave;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zwave4j.Manager;

/**
 *
 */
@Configuration
public class ZwaveConfiguration {

    @Bean
    public Manager createManager() {
        return Manager.create();
    }

}
