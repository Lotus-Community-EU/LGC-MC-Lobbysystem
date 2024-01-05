package eu.lotusgc.mc.command;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import eu.lotusgc.mc.ext.LotusController;
import eu.lotusgc.mc.main.LotusManager;
import eu.lotusgc.mc.main.Main;
import eu.lotusgc.mc.misc.Prefix;

public class SpawnSystem implements CommandExecutor, Listener{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player player) {
			if(command.getName().equalsIgnoreCase("spawn-admin")) {
				LotusController lc = new LotusController();
				// SYNTAX /spawn-admin <set|remove>
				if(args.length == 1) {
					String mode = args[0];
					if(mode.equalsIgnoreCase("set")) {
						if(player.hasPermission("lgc.spawn.admin")) {
							setSpawn(player.getLocation(), player, true);
							player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "cmd.spawnadmin.update").replace("%attribute%", "true"));
						}else {
							lc.noPerm(player, "lgc.spawn.admin");
						}
					}else if(mode.equalsIgnoreCase("remove")) {
						if(player.hasPermission("lgc.spawn.admin")) {
							setSpawn(null, player, false);
							player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "cmd.spawnadmin.update").replace("%attribute%", "false"));
						}else {
							lc.noPerm(player, "lgc.spawn.admin");
						}
					}else {
						player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "global.args") + " §7/spawn-admin <set|remove>");
					}
				}else {
					player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "global.args") + " §7/spawn-admin <set|remove>");
				}
			}else if(command.getName().equalsIgnoreCase("spawn")) {
				// SYNTAX /spawn [no args]
				LotusController lc = new LotusController();
				if(shouldSpawnBeUsed()) {
					player.teleport(getSpawn());
					lc.sendMessageReady(player, "cmd.spawn.success");
				}else {
					lc.sendMessageReady(player, "cmd.spawn.notInUse");
				}
			}
		}else {
			Bukkit.getConsoleSender().sendMessage(Main.consoleSend);
		}
		return true;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(shouldSpawnBeUsed()) {
			if(event.getPlayer().hasPlayedBefore()) {
				event.getPlayer().teleport(getSpawn());
			}else {
				new BukkitRunnable() {
					
					@Override
					public void run() {
						event.getPlayer().teleport(getSpawn());
					}
				}.runTaskLater(Main.main, 10);
			}
		}else {
			Main.logger.severe(event.getPlayer().getName() + " joined but the spawn is currently not used.");
		}
		
	}
	
	void setSpawn(Location location, Player player, boolean shouldBeUsed) {
		File config = LotusManager.mainConfig;
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(config);
		
		if(location != null) {
			cfg.set("Spawn.World", location.getWorld().getName());
			cfg.set("Spawn.X", location.getX());
			cfg.set("Spawn.Y", location.getY());
			cfg.set("Spawn.Z", location.getZ());
			cfg.set("Spawn.YAW", location.getYaw());
			cfg.set("Spawn.PITCH", location.getPitch());
			cfg.set("Spawn.Timestamp.Set", System.currentTimeMillis());
		}
		if(player != null) {
			cfg.set("Spawn.Setter", player.getUniqueId().toString());
		}
		if(!shouldBeUsed) {
			cfg.set("Spawn.Timestamp.Remove", System.currentTimeMillis());
			cfg.set("Spawn.isInUse", shouldBeUsed);
		}else {
			cfg.set("Spawn.isInUse", shouldBeUsed);
		}
		try {
			cfg.save(config);
			Main.logger.info("Spawn has been updated by " + player.getName() + " with the attribute: shouldBeUsed=" + shouldBeUsed);
		} catch (IOException e) {
			Main.logger.severe("Attempting to save spawn, but errored: " + e.getMessage());
		}
	}
	
	boolean shouldSpawnBeUsed() {
		File config = LotusManager.mainConfig;
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(config);
		return cfg.getBoolean("Spawn.isInUse");
	}
	
	Location getSpawn() {
		File config = LotusManager.mainConfig;
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(config);
		
		Location location = new Location(Bukkit.getWorld(cfg.getString("Spawn.World")), cfg.getDouble("Spawn.X"), cfg.getDouble("Spawn.Y"), cfg.getDouble("Spawn.Z"), (float)cfg.getDouble("Spawn.YAW"), (float)cfg.getDouble("Spawn.PITCH"));
		return location;
	}

}
