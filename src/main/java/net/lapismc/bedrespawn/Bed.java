package net.lapismc.bedrespawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

class Bed {

    private BedRespawn plugin;
    private UUID ownerUUID;
    private Location bedLocation;

    Bed(BedRespawn plugin, UUID ownerUUID, Location bedLocation) {
        this.plugin = plugin;
        this.ownerUUID = ownerUUID;
        this.bedLocation = bedLocation;
        if (!plugin.isBed(bedLocation.getBlock())) {
            plugin.getLogger().info("No bed was found at " + bedLocation.toString() + ", As such " + Bukkit.getOfflinePlayer(ownerUUID).getName() + " will not have a bed spawn");
            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.beds.remove(this), 5);
        }
    }

    Location getBedLocation() {
        return bedLocation;
    }

    UUID getOwnerUUID() {
        return ownerUUID;
    }

    void removeBed() {
        if (Bukkit.getOfflinePlayer(ownerUUID).isOnline()) {
            Bukkit.getPlayer(ownerUUID).sendMessage(plugin.getMessage("onBreak"));
        }
        plugin.beds.remove(this);
        plugin.saveBeds();
    }
}
