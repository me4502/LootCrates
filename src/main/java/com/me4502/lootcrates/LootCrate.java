package com.me4502.lootcrates;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class LootCrate {

    private String name;

    private Location<World> crateLocation;
    private List<String> lootCommands;

    private String keyCommand;
    private String keyName;

    public LootCrate(String name, List<String> lootCommands, Location<World> crateLocation, String keyCommand, String keyName) {
        this.name = name;
        this.lootCommands = lootCommands;
        this.crateLocation = crateLocation;
        this.keyCommand = keyCommand;
        this.keyName = keyName;
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

    public String getKeyCommand() {
        return this.keyCommand;
    }

    public String getKeyName() {
        return this.keyName;
    }
}
