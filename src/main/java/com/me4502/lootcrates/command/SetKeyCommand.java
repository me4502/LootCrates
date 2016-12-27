package com.me4502.lootcrates.command;

import com.me4502.lootcrates.LootCrate;
import com.me4502.lootcrates.LootCrates;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class SetKeyCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        LootCrate crate = args.<LootCrate>getOne("crate").orElse(null);

        if (src instanceof Player) {
            crate.setKeyItem(((Player) src).getItemInHand(HandTypes.MAIN_HAND).orElse(null));
            LootCrates.instance.saveConfig();
            src.sendMessage(Text.of("Updated key item!"));
        } else {
            src.sendMessage(Text.of("This command can only be run as a player!"));
            return CommandResult.empty();
        }

        return CommandResult.success();
    }
}
