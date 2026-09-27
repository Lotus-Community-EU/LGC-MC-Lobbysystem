package eu.lotusgc.mc.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import eu.lotusgc.mc.event.ScoreboardHandler;
import eu.lotusgc.mc.main.Main;
import eu.lotusgc.mc.misc.HotbarItem;
import eu.lotusgc.mc.misc.LotusController;
import eu.lotusgc.mc.misc.PlayerBuildData;

public class BuildCMD implements CommandExecutor, Listener{
	
	private static List<Player> allowedPlayers = new ArrayList<>();
	private static final Map<Player, PlayerBuildData> playerBuildData = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player player) {
			LotusController lc = new LotusController();
			if(args.length == 1) {
				Player player2 = Bukkit.getPlayerExact(args[0]);
				if(player2.isOnline()) {
					if(player.hasPermission("lgc.build.others")) {
						if(allowedPlayers.contains(player2)) {
							lc.sendMessageReady(player, "cmd.build.others.self.remove");
							lc.sendMessageReady(player2, "cmd.build.others.recipient.remove");
							allowedPlayers.remove(player2);
							if(ScoreboardHandler.buildTime.containsKey(player2)) {
								ScoreboardHandler.buildTime.remove(player2);
							}
							player2.setGameMode(GameMode.SURVIVAL);
							new HotbarItem().setHotbarItems(player2);
						}else {
							lc.sendMessageReady(player, "cmd.build.others.self.add");
							lc.sendMessageReady(player2, "cmd.build.others.recipient.add");
							allowedPlayers.add(player2);
							ScoreboardHandler.buildTime.put(player2, System.currentTimeMillis() / 1000);
							player2.setGameMode(GameMode.CREATIVE);
							player2.getInventory().clear();
						}
					}else {
						lc.noPerm(player, "lgc.build.others");
					}
				}else {
					lc.sendMessageReady(player, "global.playerOffline");
				}
			}else {
				if(player.hasPermission("lgc.build.self")) {
					if(allowedPlayers.contains(player)) {
						allowedPlayers.remove(player);
						lc.sendMessageReady(player, "cmd.build.self.remove");
						if(ScoreboardHandler.buildTime.containsKey(player)) {
							ScoreboardHandler.buildTime.remove(player);
						}
						player.setGameMode(GameMode.SURVIVAL);
						new HotbarItem().setHotbarItems(player);
						playerBuildData.remove(player);
					}else {
						allowedPlayers.add(player);
						playerBuildData.put(player, new PlayerBuildData());
						lc.sendMessageReady(player, "cmd.build.self.add");
						ScoreboardHandler.buildTime.put(player, System.currentTimeMillis() / 1000);
						player.setGameMode(GameMode.CREATIVE);
						player.getInventory().clear();
					}
				}else {
					lc.noPerm(player, "lgc.build.self");
				}
			}
		}else {
			sender.sendMessage(Main.consoleSend);
		}
		return false;
	}
	
	public static boolean hasPlayer(Player player) {
		return allowedPlayers.contains(player);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(allowedPlayers.contains(player)) {
			event.setCancelled(false);
			if(playerBuildData.containsKey(player)) {
				playerBuildData.get(player).incrementBrokenBlocks();
			}
		}else {
			event.setCancelled(true);
			new LotusController().sendMessageReady(player, "event.build.blockpb.cantDoThat");
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if(allowedPlayers.contains(player)) {
			event.setCancelled(false);
			if(playerBuildData.containsKey(player)) {
				playerBuildData.get(player).incrementPlacedBlocks();
			}
		}else {
			event.setCancelled(true);
			new LotusController().sendMessageReady(player, "event.build.blockpb.cantDoThat");
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		List<Material> exempted = new ArrayList<>();
		exempted.add(Material.BIG_DRIPLEAF);
		exempted.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
		exempted.add(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		exempted.add(Material.OAK_PRESSURE_PLATE);
		exempted.add(Material.DARK_OAK_PRESSURE_PLATE);
		exempted.add(Material.ACACIA_PRESSURE_PLATE);
		exempted.add(Material.BAMBOO_PRESSURE_PLATE);
		exempted.add(Material.STONE_PRESSURE_PLATE);
		if(event.getAction() == Action.PHYSICAL) {
			if(!exempted.contains(event.getClickedBlock().getType())) {
				event.setCancelled(true);
				new LotusController().sendMessageReady(player, "event.build.wheat.cantDoThat");
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerQuitEvent event) {
		if(allowedPlayers.contains(event.getPlayer())) {
			allowedPlayers.remove(event.getPlayer());
		}
		if(playerBuildData.containsKey(event.getPlayer())) {
			playerBuildData.remove(event.getPlayer());
		}
	}

    public static PlayerBuildData getPlayerBuildData(Player player) {
        if(playerBuildData.containsKey(player)) {
            return playerBuildData.get(player);
        }
        return null;
    }
}