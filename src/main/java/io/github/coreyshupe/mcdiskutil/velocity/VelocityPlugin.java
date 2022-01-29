package io.github.coreyshupe.mcdiskutil.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.coreyshupe.mcdiskutil.DiskUtilMessenger;

@Plugin(
        id = "mc-disk-util",
        authors = {"FiXed (Corey)"},
        description = "Simple disk utility plugin for Minehut servers.",
        name = "MCDiskUtil",
        version = "1.0.0"
)
public class VelocityPlugin {
    private final static DiskUtilMessenger<CommandSource> VELOCITY_DISK_UTIL_MESSENGER = new DiskUtilMessenger<>(
            (source, components) -> source.hasPermission("disk.util.*") || source.hasPermission("disk.util." + String.join(".", components))
    );
    @Inject
    private ProxyServer proxyServer;

    @Subscribe
    public void onInit(ProxyInitializeEvent ignored) {
        proxyServer.getCommandManager().register(
                proxyServer.getCommandManager().metaBuilder("diskutil").aliases("du").build(),
                new DiskUtilVelocityCommand(VELOCITY_DISK_UTIL_MESSENGER)
        );
    }
}
