package de.blu.coordinator.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.command.data.Command;
import de.blu.common.command.data.CommandExecutor;
import de.blu.common.config.FileRootConfig;
import de.blu.coordinator.repository.UserRepository;
import lombok.Getter;

@Singleton
@Getter
@Command(name = "createuser")
public final class CreateUserCommand extends CommandExecutor {

    @Inject
    private FileRootConfig fileRootConfig;

    @Inject
    private UserRepository userRepository;

    @Override
    public void execute(String label, String[] args) {
        if (args.length < 2) {
            System.out.println("&eUsage: createuser <Username> <password>");
            return;
        }

        String username = args[0];
        String password = args[1];

        if (this.getUserRepository().userExist(username)) {
            System.out.println("&cUser already exist.");
            return;
        }

        this.getUserRepository().createUser(username, password);
        System.out.println("&bUser " + username + " created.");
    }
}
