package io.github.coreyshupe.mcdiskutil.paper;

import io.github.coreyshupe.mcdiskutil.DiskUtilMessenger;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@SuppressWarnings("unused")
public class PaperPlugin extends JavaPlugin {
    private final static DiskUtilMessenger<CommandSender> PAPER_DISK_UTIL_MESSENGER = new DiskUtilMessenger<>(
            (player, components) -> player.hasPermission("disk.util.*") || player.hasPermission("disk.util." + String.join(".", components))
    );

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("diskutil")).setExecutor((commandSender, command, label, args) -> {
            if (args.length == 0) {
                PAPER_DISK_UTIL_MESSENGER.display(commandSender, "/");
            } else {
                PAPER_DISK_UTIL_MESSENGER.display(commandSender, String.join(" ", args));
            }
            return true;
        });
    }
}
