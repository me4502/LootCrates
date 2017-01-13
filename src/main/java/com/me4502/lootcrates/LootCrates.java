package com.me4502.lootcrates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.lootcrates.command.GiveKeyCommand;
import com.me4502.lootcrates.command.SetKeyCommand;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Plugin(
        id = "lootcrates",
        name = "LootCrates",
        description = "LootCrates for Sponge",
        authors = {
                "Me4502"
        }
)
public class LootCrates {

    public static LootCrates instance;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private List<LootCrate> crates = new ArrayList<>();

    private Comparator<ItemStack> itemStackComparator;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        instance = this;
        loadConfig();

        CommandSpec giveKeyCommand = CommandSpec.builder()
                .permission("lootcrates.give")
                .executor(new GiveKeyCommand())
                .arguments(
                        GenericArguments.choices(Text.of("crate"), () -> crates.stream().map(LootCrate::getName).collect(Collectors.toList()), s -> crates.stream().filter(crate -> crate.getName().equals(s)).findFirst().orElse(null)),
                        GenericArguments.optional(GenericArguments.player(Text.of("player"))),
                        GenericArguments.optional(GenericArguments.integer(Text.of("amount")))
                ).build();

        Sponge.getCommandManager().register(this, giveKeyCommand, "givekey");

        CommandSpec setKeyCommand = CommandSpec.builder()
                .permission("lootcrates.set")
                .executor(new SetKeyCommand())
                .arguments(
                        GenericArguments.choices(Text.of("crate"), () -> crates.stream().map(LootCrate::getName).collect(Collectors.toList()), s -> crates.stream().filter(crate -> crate.getName().equals(s)).findFirst().orElse(null))
                ).build();

        Sponge.getCommandManager().register(this, setKeyCommand, "setkey");

        itemStackComparator = Ordering.compound(ImmutableList.of(ItemStackComparators.TYPE, ItemStackComparators.ITEM_DATA));
    }

    public void loadConfig() {
        try {
            if (!Files.exists(defaultConfig, LinkOption.NOFOLLOW_LINKS)) {
                URL jarConfigFile = this.getClass().getResource("default.conf");
                ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setURL(jarConfigFile).build();
                configManager.save(loader.load());
            }

            ConfigurationNode node = configManager.load();
            node.getOptions().setShouldCopyDefaults(true);

            ConfigurationNode cratesNode = node.getNode("crates");

            for (ConfigurationNode crateNode : cratesNode.getChildrenMap().values()) {
                try {
                    LootCrate crate = new LootCrate(
                            String.valueOf(crateNode.getKey()),
                            crateNode.getNode("loot-commands").getList(TypeToken.of(String.class)),
                            getFromString(crateNode.getNode("location").getString("world:0:0:0")),
                            crateNode.getNode("key-item").getValue(TypeToken.of(ItemStack.class), ItemStack.builder().itemType(ItemTypes.BLAZE_ROD).add(Keys.DISPLAY_NAME, Text.of("Super Key")).build()));
                    crates.add(crate);
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
            }

            configManager.save(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            ConfigurationNode node = configManager.load();
            ConfigurationNode cratesNode = node.getNode("crates");

            for (LootCrate crate : crates) {
                try {
                    ConfigurationNode crateNode = cratesNode.getNode(crate.getName());
                    crateNode.getNode("key-item").setValue(TypeToken.of(ItemStack.class), crate.getKeyItem());
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
            }

            configManager.save(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onClick(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        event.getTargetBlock().getLocation().map(this::getCrateForLocation).ifPresent((crate -> player.getItemInHand(HandTypes.MAIN_HAND).ifPresent(itemStack -> {
            if (itemStackComparator.compare(crate.getKeyItem(), itemStack) == 0) {
                for (int i = 0; i < itemStack.getQuantity(); i++) {
                    String command = crate.getLootCommands().get(ThreadLocalRandom.current().nextInt(crate.getLootCommands().size()));
                    Sponge.getGame().getCommandManager().process(Sponge.getGame().getServer().getConsole(), command.replace("@p", player.getName()));
                }
                player.setItemInHand(HandTypes.MAIN_HAND, null);
                event.setCancelled(true);
            }
        })));
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
