package com.me4502.lootcrates.command;

import com.me4502.lootcrates.LootCrate;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class GiveKeyCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        LootCrate crate = args.<LootCrate>getOne("crate").orElse(null);
        Player player = args.<Player>getOne("player").orElse(src instanceof Player ? (Player) src : null);
        int amount = Math.max(1, Math.min(args.<Integer>getOne("amount").orElse(1), crate.getKeyItem().getMaxStackQuantity()));

        if (player == null) {
            src.sendMessage(Text.of("You must provide a player!"));
            return CommandResult.empty();
        }

        ItemStack crateKey = crate.getKeyItem().copy();
        crateKey.setQuantity(amount);

        InventoryTransactionResult result = player.getInventory().offer(crateKey);

        if (result.getRejectedItems().isEmpty()) {
            player.sendMessage(Text.of(TextColors.YELLOW, "You got " + amount + " " + crate.getName() + " key(s)!"));
        } else {
            player.sendMessage(Text.of(TextColors.RED, "Failed to give you key!"));
        }

        return CommandResult.success();
    }
}
