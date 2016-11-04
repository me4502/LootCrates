package com.me4502.lootcrates;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
        id = "lootcrates",
        name = "LootCrates",
        description = "LootCrates for Sponge",
        authors = {
                "Me4502"
        }
)
public class LootCrates {

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
    }
}
