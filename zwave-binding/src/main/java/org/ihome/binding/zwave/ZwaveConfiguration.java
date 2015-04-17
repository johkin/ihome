package org.ihome.binding.zwave;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zwave4j.NativeLibraryLoader;
import org.zwave4j.ZWave4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
@Configuration
@ComponentScan("org.ihome.binding.zwave")
public class ZwaveConfiguration {

    static {
        NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);
    }

    @Bean
    public ExecutorService getExecutorService() {
        return Executors.newSingleThreadExecutor();
    }

}
