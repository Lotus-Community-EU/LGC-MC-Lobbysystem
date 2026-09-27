package eu.lotusgc.mc.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import eu.lotusgc.mc.main.LotusManager;
import eu.lotusgc.mc.main.Main;
import eu.lotusgc.mc.misc.LotusController;

public class ChestPointer implements CommandExecutor, Listener {

    static List<Player> settingCrateChest = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player!");
            return true;
        }else {
            LotusController lc = new LotusController();
            if(player.hasPermission("lgc.setCratesChest")){
                settingCrateChest.add(player);
                lc.sendMessageReady(player, "cmd.setcratechest.start");
            }else {
                lc.noPerm(player, "lgc.setCratesChest");
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerPoint(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(settingCrateChest.contains(player)) {
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Block clickedBlock = event.getClickedBlock();
                LotusController lc = new LotusController();
                if(clickedBlock != null && clickedBlock.getType() == Material.CHEST) {
                    Location chestLocation = clickedBlock.getLocation();
                    lc.sendMessageReady(player, "cmd.setcratechest.success");
                    setCrates(chestLocation, player);
                    settingCrateChest.remove(player);
                    event.setCancelled(true);
                } else {
                    lc.sendMessageReady(player, "cmd.setcratechest.notchest");
                    event.setCancelled(true);
                }
            }
        }
    }

    void setCrates(Location location, Player player) {
		File config = LotusManager.mainConfig;
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(config);
		
		if(location != null) {
			cfg.set("Crates.World", location.getWorld().getName());
			cfg.set("Crates.X", location.getX());
			cfg.set("Crates.Y", location.getY());
			cfg.set("Crates.Z", location.getZ());
			cfg.set("Crates.Timestamp.Set", System.currentTimeMillis());
		}
		if(player != null) {
			cfg.set("Crates.Setter", player.getUniqueId().toString());
		}
		try {
			cfg.save(config);
			Main.logger.info("Crates have been updated by " + player.getName());
		} catch (IOException e) {
			Main.logger.severe("Attempting to save crates, but errored: " + e.getMessage());
		}
	}
}