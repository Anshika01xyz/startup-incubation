package com.startup.platform.util;

import com.startup.platform.config.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;

public final class AppLog {
    private static final Logger LOGGER=Logger.getLogger("com.startup.platform");
    static {
        try {
            Files.createDirectories(Path.of("logs"));
            FileHandler handler=new FileHandler("logs/application-%g.log",1_000_000,3,true);
            handler.setFormatter(new SimpleFormatter());LOGGER.addHandler(handler);LOGGER.setUseParentHandlers(false);
        } catch(IOException e){LOGGER.log(Level.WARNING,"File logging could not be initialized",e);}
    }
    private AppLog() {}
    public static void info(String message){LOGGER.info(message);}
    public static void error(String message,Throwable error){LOGGER.log(Level.SEVERE,message,error);}
}
