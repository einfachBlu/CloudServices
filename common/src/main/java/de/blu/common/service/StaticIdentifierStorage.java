package de.blu.common.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

@Singleton
@Getter
public final class StaticIdentifierStorage {

    private Properties properties = new Properties() {{
        this.setProperty("identifier", UUID.randomUUID().toString());
    }};

    @Inject
    private SelfServiceInformation selfServiceInformation;

    public void init(File identifierFile) {
        try {
            if (!identifierFile.exists()) {
                identifierFile.createNewFile();

                this.getProperties().store(new FileWriter(identifierFile), "Static Identifier for this Service");
                this.getSelfServiceInformation().setIdentifier(UUID.fromString(this.getProperties().getProperty("identifier")));
                return;
            }

            this.getProperties().load(new FileReader(identifierFile));
            this.getSelfServiceInformation().setIdentifier(UUID.fromString(this.getProperties().getProperty("identifier")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
