package io.github.coreyshupe.mcdiskutil.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import io.github.coreyshupe.mcdiskutil.DiskUtilMessenger;

@SuppressWarnings("ClassCanBeRecord")
public class DiskUtilVelocityCommand implements SimpleCommand {
    private final DiskUtilMessenger<CommandSource> diskUtilMessenger;

    public DiskUtilVelocityCommand(DiskUtilMessenger<CommandSource> diskUtilMessenger) {
        this.diskUtilMessenger = diskUtilMessenger;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            diskUtilMessenger.display(source, "/");
        } else {
            diskUtilMessenger.display(source, String.join(" ", args));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("disk.util");
    }
}
