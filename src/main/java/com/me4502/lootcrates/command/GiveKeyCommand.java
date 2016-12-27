package com.me4502.lootcrates.command;

import com.me4502.lootcrates.LootCrate;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class GiveKeyCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        LootCrate crate = args.<LootCrate>getOne("crate").orElse(null);
        Player player = args.<Player>getOne("player").orElse(src instanceof Player ? (Player) src : null);

        if (player == null) {
            src.sendMessage(Text.of("You must provide a player!"));
            return CommandResult.empty();
        }

        InventoryTransactionResult result = player.getInventory().offer(crate.getKeyItem().copy());

        if (result.getRejectedItems().isEmpty()) {
            player.sendMessage(Text.of(TextColors.YELLOW, "You got a " + crate.getName() + " key!"));
        } else {
            player.sendMessage(Text.of(TextColors.RED, "Failed to give you key!"));
        }

        return CommandResult.success();
    }
}
