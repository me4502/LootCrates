package com.me4502.lootcrates;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Plugin(
        id = "lootcrates",
        name = "LootCrates",
        description = "LootCrates for Sponge",
        authors = {
                "Me4502"
        }
)
public class LootCrates {

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private List<LootCrate> crates = new ArrayList<>();

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        try {
            if (!Files.exists(defaultConfig, LinkOption.NOFOLLOW_LINKS)) {
                URL jarConfigFile = this.getClass().getResource("default.conf");
                ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setURL(jarConfigFile).build();
                configManager.save(loader.load());
            }

            ConfigurationNode node = configManager.load();

            ConfigurationNode cratesNode = node.getNode("crates");

            for (ConfigurationNode crateNode : cratesNode.getChildrenMap().values()) {
                try {
                    LootCrate crate = new LootCrate(
                            crateNode.getString("Crate Name"),
                            crateNode.getNode("loot-commands").getList(TypeToken.of(String.class)),
                            getFromString(crateNode.getNode("location").getString("world:0:0:0")),
                            crateNode.getNode("key-command").getString("/give @p minecraft:blaze_rod 1 0 {display:{Name:\"Super Key\"}}"),
                            crateNode.getNode("key-name").getString("Super Key"));

                    crates.add(crate);
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onClick(InteractBlockEvent.Secondary event, @First Player player) {
        event.getTargetBlock().getLocation().map(this::getCrateForLocation).ifPresent((crate -> player.getItemInHand().ifPresent((itemStack -> itemStack.get(Keys.DISPLAY_NAME).ifPresent((name -> {
            if (name.toPlain().contains(crate.getKeyName())) {
                String command = crate.getLootCommands().get(ThreadLocalRandom.current().nextInt(crate.getLootCommands().size()));
                Sponge.getGame().getCommandManager().process(Sponge.getGame().getServer().getConsole(), command.replace("@p", player.getName()));
                if (itemStack.getQuantity() > 1) {
                    itemStack.setQuantity(itemStack.getQuantity() - 1);
                } else {
                    player.setItemInHand(null);
                }
                event.setCancelled(true);
            }
        }))))));
    }

    public LootCrate getCrateForLocation(Location<World> location) {
        for (LootCrate crate : crates) {
            if (crate.getCrateLocation().getBlockPosition().equals(location.getBlockPosition())) {
                return crate;
            }
        }

        return null;
    }

    public Location<World> getFromString(String string) {
        String[] bits = string.split(":");
        return new Location<>(Sponge.getGame().getServer().getWorld(bits[0]).get(), Integer.parseInt(bits[1]), Integer.parseInt(bits[2]), Integer.parseInt(bits[3]));
    }
}
