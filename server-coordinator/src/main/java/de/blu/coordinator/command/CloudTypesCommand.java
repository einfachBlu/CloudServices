package de.blu.coordinator.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.command.data.Command;
import de.blu.common.command.data.CommandExecutor;
import de.blu.common.data.CloudType;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.coordinator.printer.CloudTypePrinter;
import lombok.Getter;

import java.util.Collections;

@Singleton
@Command(name = "cloudtypes", aliases = "ct")
@Getter
public final class CloudTypesCommand extends CommandExecutor {

    @Inject
    private CloudTypePrinter cloudTypePrinter;

    @Override
    public void execute(String label, String[] args) {
        this.getCloudTypePrinter().printAll();
    }
}
