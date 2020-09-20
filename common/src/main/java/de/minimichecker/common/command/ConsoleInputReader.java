package de.minimichecker.common.command;

import java.util.function.Consumer;

public interface ConsoleInputReader {
    /**
     * Initialize the Handler to accept all
     * incoming Messages to the System InputStream
     */
    void init();

    /**
     * Waiting for the Input of a User. Blocking! Please
     * only execute this Method on another Thread (like Command-Thread)
     *
     * @return the line which was read
     */
    String readLine();

    /**
     * ReadLine Async
     *
     * @param lineCallback with the line which was read
     */
    void readLine(Consumer<String> lineCallback);

    /**
     * Simulate a Commandline Input
     *
     * @param line the line to simulate
     */
    void dispatch(String line);
}
