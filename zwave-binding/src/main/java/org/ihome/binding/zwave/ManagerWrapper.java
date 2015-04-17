package org.ihome.binding.zwave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zwave4j.Manager;
import org.zwave4j.Options;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;

/**
 *
 */
@Component
public class ManagerWrapper {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @PostConstruct
    public void init() throws Exception {
        File file = new File("build/ozw-config");
        if (!file.exists()) {
            file.mkdirs();
        }
        Options options = Options.create(file.getAbsolutePath(), "", "");
        options.addOptionBool("ConsoleOutput", false);
        options.lock();

        Manager.create();

    }

    @PreDestroy
    public void destroy() throws Exception {
        logger.debug("Manager destroy");
        Manager.destroy();
        logger.debug("Options destroy");
        Options.destroy();
        logger.debug("Manager stopped");
    }
}
