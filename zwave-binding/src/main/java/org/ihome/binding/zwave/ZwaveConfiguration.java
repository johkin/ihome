package org.ihome.binding.zwave;

import org.springframework.context.annotation.Configuration;
import org.zwave4j.NativeLibraryLoader;
import org.zwave4j.ZWave4j;

/**
 *
 */
@Configuration
public class ZwaveConfiguration {

    static {
        NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);
    }


}
