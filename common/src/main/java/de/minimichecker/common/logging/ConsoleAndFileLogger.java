package de.minimichecker.common.logging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jline.console.ConsoleReader;
import lombok.Getter;
import org.apache.log4j.*;
import org.apache.log4j.varia.NullAppender;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

@Singleton
public final class ConsoleAndFileLogger implements Logger {

    @Getter
    private static Set<Logger> loggers = new HashSet<>();

    @Inject
    @Getter
    private ConsoleReader reader;

    @Getter
    private org.apache.log4j.Logger fileLogger;

    @Getter
    private org.apache.log4j.Logger apacheLogger;

    @Getter
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd") {{
        this.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
    }};

    @Override
    public void init(File logsDirectory) {
        Date date = new Date(System.currentTimeMillis());
        File file = new File(logsDirectory.getAbsolutePath() + "/unknown.log");
        for (int id = 1; id < Integer.MAX_VALUE; id++) {
            file = new File(logsDirectory, this.getSimpleDateFormat().format(date) + "-" + id + ".log");
            if (file.exists()) {
                continue;
            }
            break;
        }

        if (!logsDirectory.exists()) {
            logsDirectory.mkdir();
        }

        this.apacheLogger = org.apache.log4j.Logger.getLogger("CloudSystemLogger");
        this.fileLogger = org.apache.log4j.Logger.getLogger("CloudSystemFileLogger");

        BasicConfigurator.configure(new NullAppender());

        PatternLayout layout = new PatternLayout("[%d{HH:mm:ss}] %m%n");
        ConsoleAppender consoleAppender = new ANSIConsoleAppender(layout, this.getReader());

        this.getApacheLogger().addAppender(consoleAppender);

        try {
            FileAppender fileAppender = new FileAppender(layout, file.getAbsolutePath(), false);
            FileAppender fileAppender2 = new FileAppender(layout, logsDirectory.getAbsolutePath() + "/latest.log", false);
            this.getFileLogger().addAppender(fileAppender);
            this.getFileLogger().addAppender(fileAppender2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.getApacheLogger().setLevel(Level.INFO);
        this.getFileLogger().setLevel(Level.INFO);

        // Add to Loggerlist
        Logger.getLoggers().add(this);
    }

    public void info(String message) {
        message = "&3INFO&r: " + message + "";

        this.getFileLogger().info(this.removeColorCodes(message));
        this.getApacheLogger().info(message);
    }

    public void error(String message) {
        message = "&cERROR&r: &c" + message + "";

        this.getFileLogger().info(this.removeColorCodes(message));
        this.getApacheLogger().info(message);
    }

    public void error(String message, Throwable throwable) {
        message = "&cERROR&r: &c" + message + "";

        this.getFileLogger().info(this.removeColorCodes(message), throwable);
        this.getApacheLogger().info(message, throwable);
        throwable.printStackTrace();
    }

    public void debug(String message) {
        message = "&bDEBUG&r: &b" + message + "";

        this.getFileLogger().info(this.removeColorCodes(message));
        this.getApacheLogger().info(message);
    }

    public void warning(String message) {
        message = "&eWARNING&r: &e" + message + "";

        this.getFileLogger().info(this.removeColorCodes(message));
        this.getApacheLogger().info(message);
    }

    private String removeColorCodes(String message) {
        String[] list = new String[]{
                "a", "b", "c", "d", "e", "f",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "r", "l", "n"
        };
        for (String colorcode : list) {
            message = message.replaceAll("§" + colorcode, "");
            message = message.replaceAll("&" + colorcode, "");
        }

        return message;
    }
}
