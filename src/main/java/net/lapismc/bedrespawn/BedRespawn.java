package net.lapismc.bedrespawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class BedRespawn extends JavaPlugin implements Listener {

    ArrayList<Bed> beds = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        File f = new File(getDataFolder(), "beds.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration bedsYml = YamlConfiguration.loadConfiguration(f);
        List<String> bedStringList = bedsYml.getStringList("Beds");
        for (String bedString : bedStringList) {
            String[] bedStringArray = bedString.split(":");
            UUID ownerUUID = UUID.fromString(bedStringArray[0]);
            Location bedLocation = parseString(bedStringArray[1]);
            beds.add(new Bed(this, ownerUUID, bedLocation));
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info(getDescription().getName() + " v." + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        saveBeds();
        getLogger().info(getDescription().getName() + " has been disabled");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (isBed(e.getBlock())) {
            if (!doesPlayerHaveBed(e.getPlayer().getUniqueId())) {
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (isBed(e.getBlock().getLocation().getBlock())) {
                        beds.add(new Bed(this, e.getPlayer().getUniqueId(), e.getBlock().getLocation()));
                        saveBeds();
                        e.getPlayer().sendMessage(getMessage("onPlace.BedSpawnCreated"));
                    }
                }, 20);
            } else {
                e.getPlayer().sendMessage(getMessage("onPlace.OtherSpawnExists"));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (isBed(e.getBlock())) {
            if (isPlayerBed(e.getBlock())) {
                Objects.requireNonNull(getPlayersBed(e.getBlock())).removeBed();
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (doesPlayerHaveBed(e.getPlayer().getUniqueId())) {
            Location bedLocation = Objects.requireNonNull(getPlayersBed(e.getPlayer().getUniqueId())).getBedLocation();
            Location respawnLocation = new Location(bedLocation.getWorld(), bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
            e.setRespawnLocation(respawnLocation);
            e.getPlayer().sendMessage(getMessage("onRespawn.toBed"));
        } else {
            e.getPlayer().sendMessage(getMessage("onRespawn.noBed"));
        }
    }

    private boolean isPlayerBed(Block b) {
        return getPlayersBed(b) != null;
    }

    private boolean doesPlayerHaveBed(UUID uuid) {
        return getPlayersBed(uuid) != null;
    }

    private Bed getPlayersBed(UUID uuid) {
        for (Bed bed : beds) {
            if (bed.getOwnerUUID().equals(uuid)) {
                return bed;
            }
        }
        return null;
    }

    private Bed getPlayersBed(Block b) {
        for (Bed bed : beds) {
            if (bed.checkBedLocation(b.getLocation())) {
                return bed;
            }
        }
        return null;
    }

    void saveBeds() {
        File f = new File(getDataFolder(), "beds.yml");
        YamlConfiguration bedsYml = YamlConfiguration.loadConfiguration(f);
        bedsYml.set("Beds", null);
        List<String> bedStringList = new ArrayList<>();
        for (Bed bed : beds) {
            bedStringList.add(bed.getOwnerUUID().toString() + ":" + parseLocation(bed.getBedLocation()));
        }
        bedsYml.set("Beds", bedStringList);
        try {
            bedsYml.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean isBed(Block block) {
        return block.getBlockData() instanceof org.bukkit.block.data.type.Bed;
    }

    String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages." + key));
    }

    private Location parseString(String s) {
        String[] array = s.split(",");
        World world = Bukkit.getWorld(array[0]);
        double x = Double.parseDouble(array[1]);
        double y = Double.parseDouble(array[2]);
        double z = Double.parseDouble(array[3]);
        return new Location(world, x, y, z);
    }

    private String parseLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

}
