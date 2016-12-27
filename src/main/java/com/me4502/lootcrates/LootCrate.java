package com.me4502.lootcrates;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class LootCrate {

    private String name;

    private Location<World> crateLocation;
    private List<String> lootCommands;

    private ItemStack keyItem;

    public LootCrate(String name, List<String> lootCommands, Location<World> crateLocation, ItemStack keyItem) {
        this.name = name;
        this.lootCommands = lootCommands;
        this.crateLocation = crateLocation;
        this.keyItem = keyItem;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getLootCommands() {
        return this.lootCommands;
    }

    public Location<World> getCrateLocation() {
        return this.crateLocation;
    }

    public ItemStack getKeyItem() {
        return this.keyItem;
    }

    public void setKeyItem(ItemStack keyItem) {
        this.keyItem = keyItem;
    }
}
