package de.minimichecker.common.logging;

import jline.console.ConsoleReader;
import lombok.Getter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.fusesource.jansi.Ansi;

/**
 * Colour-coded console appender for Log4J.
 */
public final class ANSIConsoleAppender extends ConsoleAppender {
    private static final String PREFIX = "\u001b[";
    private static final String SUFFIX = "m";
    private static final char SEPARATOR = ';';
    private static final String END_COLOR = PREFIX + SUFFIX;

    @Getter
    private ConsoleReader consoleReader;

    public ANSIConsoleAppender(Layout layout, ConsoleReader consoleReader) {
        super(layout);

        this.consoleReader = consoleReader;
    }

    /**
     * Wraps the ANSI control characters around the
     * output from the super-class Appender.
     */
    protected void subAppend(LoggingEvent event) {
        String message = this.replaceColorCodes(event.getRenderedMessage());

        try {
            this.getConsoleReader().print(Ansi.ansi().eraseLine(Ansi.Erase.ALL).toString() + ConsoleReader.RESET_LINE);
            this.getConsoleReader().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LoggingEvent modifiedEvent = new LoggingEvent(event.getFQNOfLoggerClass(), event.getLogger(), event.getTimeStamp(), event.getLevel(), message,
                event.getThreadName(), event.getThrowableInformation(), event.getNDC(), event.getLocationInformation(),
                event.getProperties());
        /*
        super.subAppend(modifiedEvent);
         */
        String fullMessage = this.getLayout().format(modifiedEvent);


        try {
            System.out.write((fullMessage /* + System.lineSeparator() */).getBytes());
            System.out.flush();

            try {
                this.getConsoleReader().drawLine();
            } catch (Throwable ex) {
                this.getConsoleReader().getCursorBuffer().clear();
            }

            this.getConsoleReader().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String replaceColorCodes(String message) {
        for (Color color : Color.values()) {
            message = message.replaceAll("&" + color.getColorCode(), PREFIX + color.getLoggingColorId() + SUFFIX);
        }

        // TODO: better implementation of formatting (check if the previous is the timestamp
        //message = message.replaceAll("INFO", TEST_COLOR + "INFO" + END_COLOR).replaceAll("&4", TEST_COLOR);

        return message + END_COLOR;
    }

    private String removeColorCodes(String message) {
        for (Color color : Color.values()) {
            message = message.replaceAll("&" + color.getColorCode(), "");
        }

        return message;
    }
}
