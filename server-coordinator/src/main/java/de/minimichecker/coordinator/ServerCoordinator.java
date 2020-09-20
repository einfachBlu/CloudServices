package de.minimichecker.coordinator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.minimichecker.common.util.LibraryUtils;
import de.minimichecker.coordinator.module.ModuleSettings;

import java.io.File;
import java.net.URISyntaxException;

public final class ServerCoordinator {
    public static void main(String[] args) {
        try {
            // Init Libraries
            LibraryUtils.createLibraryFolder(ServerCoordinator.getRootDirectory());
            LibraryUtils.loadLibraries();

            // Create Injector
            Injector injector = Guice.createInjector(new ModuleSettings());

            // Calling Injected Constructor of CloudNode and start the Node
            injector.getInstance(ServerCoordinator.class);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                System.out.println("Stopping in 3 Seconds...");
                Thread.sleep(3000);
                System.exit(0);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static File getRootDirectory() {
        File directory = null;

        String debugPath = "G:/JavaProjects/mini-network/server-coordinator/build/debugging/";
        boolean isDebug = java.lang.management.ManagementFactory.
                getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");

        if (isDebug) {
            directory = new File(debugPath);
        } else {
            try {
                directory = new File(ServerCoordinator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (!directory.isDirectory()) {
            if (!directory.mkdir()) {
                throw new NullPointerException("Couldn't create root directory!");
            }
        }

        return directory;
    }

    public ServerCoordinator() {
        System.out.println("Started!");
        /*
        HelloWorldPacket helloWorldPacket = new HelloWorldPacket();
        helloWorldPacket.setMessage("HelloWorld2");

        PacketSender packetSender = new PacketSender();
        PacketListenerRepository packetListenerRepository = new PacketListenerRepository();

        packetListenerRepository.registerListener((packet, hadCallback) -> {
            Packet receivedPacket = packet;

            if (hadCallback) {
                // Prevent Executing because already handled by callback
                return;
            }
        }, "testChannel");


        packetSender.sendPacket(helloWorldPacket, "testChannel");

        packetSender.sendPacket(helloWorldPacket, aVoid -> {
        }, "testChannel");

        packetSender.sendRequestPacket(helloWorldPacket, resultPacket -> {
            HelloWorldPacket packet = resultPacket;
        }, "testChannel");

        packetSender.sendRequestPacket(helloWorldPacket, resultPacket -> {
            HelloWorldPacket packet = resultPacket;
        }, "testChannel");
         */
    }
}
