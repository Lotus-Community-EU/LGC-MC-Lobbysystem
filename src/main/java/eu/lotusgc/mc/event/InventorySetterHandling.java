package eu.lotusgc.mc.event;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import eu.lotusgc.mc.command.BuildCMD;
import eu.lotusgc.mc.command.SpawnSystem;
import eu.lotusgc.mc.ext.LotusController;
import eu.lotusgc.mc.main.Main;
import eu.lotusgc.mc.misc.HotbarItem;
import eu.lotusgc.mc.misc.InputType;
import eu.lotusgc.mc.misc.MySQL;
import eu.lotusgc.mc.misc.Playerdata;
import eu.lotusgc.mc.misc.Prefix;
import eu.lotusgc.mc.misc.Serverdata;

public class InventorySetterHandling implements Listener{
	
	public static String navi_title = "§aNavigator";
	public static String navi_spawn = "§6Spawn";
	public static String navi_rewards = "§bRewards";
	public static String navi_creative = "§eCreative";
	public static String navi_creativehx = "§eCreative §6HX";
	public static String navi_survival = "§cSurvival";
	public static String navi_survivalhx = "§cSurvival §6HX";
	public static String navi_skyblock = "§7§fSky§2Block";
	public static String navi_gameslobby = "§dGameslobby";
	public static String navi_farmserver = "§9Farmserver";
	
	public static String language_title = "§6Languages";
	
	public static String extras_title = "§9Extras";
	public static String extras_pets = "";
	public static String extras_boots = "";
	public static String extras_jboost = "";
	public static String extras_sboost = "";
	
	public static String rewards_title = "§bRewards";
	public static String rewards_crates = "§eCrates §4(CLOSED)";
	public static String rewards_dailyRewards = "§aDaily Rewards (§4CLOSED)";
	
	public static String sboost_title = "§7Speedboost";
	public static String sboost_stage1 = "";
	public static String sboost_stage2 = "";
	public static String sboost_stage3 = "";
	public static String sboost_stage4 = "";
	public static String sboost_stage5 = "";
	
	public static String jboost_title = "§7Jumpboost";
	public static String jboost_stage1 = "";
	public static String jboost_stage2 = "";
	public static String jboost_stage3 = "";
	public static String jboost_stage4 = "";
	public static String jboost_stage5 = "";
	
	public static String close = "§cclose";
	public static String back = "§cback";
	
	public static void setNavigatorInventory(Player player) {
		boolean tempBool = true;
		if(tempBool) {
			//concurrent version
			Inventory mainInventory = Bukkit.createInventory(null, 2*9, navi_title);
			LotusController lc = new LotusController();
			for(int i = 0; i < 18; i++) {
				mainInventory.setItem(i, lc.defItem(Material.LIME_STAINED_GLASS_PANE, "§0", 1));
			}
			mainInventory.setItem(3, lc.defItem(Material.EMERALD, navi_spawn, 1));
			mainInventory.setItem(5, lc.defItem(Material.ECHO_SHARD, navi_rewards, 1));
			mainInventory.setItem(9, lc.naviServerItem(Material.DIAMOND_PICKAXE, "Creative"));
			mainInventory.setItem(11, lc.naviServerItem(Material.GRASS_BLOCK, "SkyBlock"));
			mainInventory.setItem(13, lc.naviServerItem(Material.NETHERITE_AXE, "Survival"));
			mainInventory.setItem(15, lc.naviServerItem(Material.RED_BED, "Gameslobby"));
			mainInventory.setItem(17, lc.naviServerItem(Material.GOLDEN_HOE, "Farmserver"));
			player.openInventory(mainInventory);
		}else {
			//version 1.12.2 (HX Servers)
			Inventory mainInventory = Bukkit.createInventory(null, 9*1, navi_title);
			LotusController lc = new LotusController();
			for(int i = 0; i < 8; i++) {
				mainInventory.setItem(i, lc.defItem(Material.ORANGE_STAINED_GLASS_PANE, "§0", 1));
			}
			mainInventory.setItem(1, lc.naviServerItem(Material.DIAMOND_PICKAXE, "Creative HX"));
			mainInventory.setItem(3, lc.defItem(Material.EMERALD, navi_spawn, 1));
			mainInventory.setItem(5, lc.defItem(Material.ECHO_SHARD, navi_rewards, 1));
			mainInventory.setItem(7, lc.naviServerItem(Material.GOLDEN_SWORD, "Survival HX"));
			player.openInventory(mainInventory);
		}
	}
	
	public static void setRewardsInventory(Player player) {
		Inventory mainInventory = Bukkit.createInventory(null, 9*1, rewards_title);
		LotusController lc = new LotusController();
		for(int i = 0; i < 9; i++) {
			mainInventory.setItem(i, lc.defItem(Material.MAGENTA_STAINED_GLASS_PANE, "§0", 1));
		}
		mainInventory.setItem(3, lc.defItem(Material.AMETHYST_SHARD, rewards_crates, 1));
		mainInventory.setItem(5, lc.defItem(Material.DIAMOND, rewards_dailyRewards, 1));
		player.openInventory(mainInventory);
	}
	
	public static void setLanguageInventory(Player player) {
		Inventory mainInventory = Bukkit.createInventory(null, 9*3, language_title);
		LotusController lc = new LotusController();
		int slot = 0;
		String playerLang = lc.getPlayerData(player, Playerdata.Language);
		for(String string : lc.getAvailableLanguages()) {
			String fancyName = langs.getOrDefault(string, "Error!");
			if(string.equalsIgnoreCase(playerLang)) {
				mainInventory.setItem(slot, lc.loreItem(Material.NETHER_STAR, 1, "§6" + fancyName, "§7Language is:", "§7» §a" + string));
			}else {
				mainInventory.setItem(slot, lc.defItemRandom(matList(), "§a" + fancyName, 1, "§7Language is:", "§7» §a" + string));
			}
			slot++;
		}
		player.openInventory(mainInventory);
	}
	
	public static void setExtrasInventory(Player player) {
		Inventory mainInventory = Bukkit.createInventory(null, 9*3, rewards_title);
		LotusController lc = new LotusController();
		for(int i = 0; i < 27; i++) {
			mainInventory.setItem(i, lc.defItem(Material.BLUE_STAINED_GLASS_PANE, "§0", 1));
		}
		mainInventory.setItem(10, lc.loreItem(Material.PIG_SPAWN_EGG, 1, extras_pets, "§cThis Feature is", "§cnot enabled yet."));
		mainInventory.setItem(12, lc.loreItem(Material.LEATHER_BOOTS, 1, extras_boots, "§cThis Feature is", "§cnot enabled yet."));
		mainInventory.setItem(14, lc.loreItem(Material.POTION, 1, extras_sboost, "§cThis Feature is", "§cnot enabled yet."));
		mainInventory.setItem(16, lc.loreItem(Material.POTION, 1, extras_jboost, "§cThis Feature is", "§cnot enabled yet."));
		player.openInventory(mainInventory);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if(event.getCurrentItem() == null) return;
		if(event.getView().getTitle().equalsIgnoreCase(navi_title)) {
			LotusController lc = new LotusController();
			event.setCancelled(true);
			if(event.getCurrentItem() == null && event.getCurrentItem().getItemMeta() == null) return;
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
			HashMap<String, String> fancyNames = getServerFancynames();
			if(fancyNames.containsKey(itemName)) {
				String bungeeName = fancyNames.get(itemName);
				if(lc.translateBoolean(lc.getServerData(bungeeName, Serverdata.OnlineStatus, InputType.BungeeKey))){
					if(lc.translateBoolean(lc.getServerData(bungeeName, Serverdata.LockedStatus, InputType.BungeeKey))) {
						if(player.hasPermission("lgc.bypassServerlock")) {
							sendPlayerToServer(player, itemName, bungeeName, lc);
						}else {
							Main.logger.info(player.getName() + " tried to join a locked server.");
						}
					}else {
						sendPlayerToServer(player, itemName, bungeeName, lc);
					}
				}else {
					//server dead - how poor lol
				}
			}else {
				if(itemName.equalsIgnoreCase(navi_rewards)) {
					player.closeInventory();
					setRewardsInventory(player);
				}else if(itemName.equalsIgnoreCase(navi_spawn)) {
					Location spawn = SpawnSystem.getSpawn("mainSpawn");
					player.closeInventory();
					player.teleport(spawn);
				}
			}
		}else if(event.getView().getTitle().equalsIgnoreCase(extras_title)) {
			event.setCancelled(true);
			LotusController lc = new LotusController();
			if(event.getCurrentItem() == null && event.getCurrentItem().getItemMeta() == null) return;
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
			if(itemName.equalsIgnoreCase(extras_boots)) {
				
			}else if(itemName.equalsIgnoreCase(extras_jboost)) {
				
			}else if(itemName.equalsIgnoreCase(extras_sboost)) {
				
			}else if(itemName.equalsIgnoreCase(extras_pets)) {
				lc.sendMessageReady(player, "event.pets.closed");
			}else if(itemName.equalsIgnoreCase(close)) {
				player.closeInventory();
			}
		}else if(event.getView().getTitle().equalsIgnoreCase(rewards_title)) {
			event.setCancelled(true);
			LotusController lc = new LotusController();
			if(event.getCurrentItem() == null && event.getCurrentItem().getItemMeta() == null) return;
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
			if(itemName.equalsIgnoreCase(rewards_dailyRewards)) {
				Location loc = SpawnSystem.getSpawn("dailyRewards");
				player.teleport(loc);
			}else if(itemName.equalsIgnoreCase(rewards_crates)) {
				Location loc = SpawnSystem.getSpawn("crates");
				player.teleport(loc);
			}
		}else if(event.getView().getTitle().equalsIgnoreCase(sboost_title)) {
			event.setCancelled(true);
			LotusController lc = new LotusController();
			if(event.getCurrentItem() == null && event.getCurrentItem().getItemMeta() == null) return;
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
		}else if(event.getView().getTitle().equalsIgnoreCase(jboost_title)) {
			event.setCancelled(true);
			LotusController lc = new LotusController();
			if(event.getCurrentItem() == null && event.getCurrentItem().getItemMeta() == null) return;
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
		}else if(event.getView().getTitle().equalsIgnoreCase(language_title)) {
			event.setCancelled(true);
			LotusController lc = new LotusController();
			if(event.getCurrentItem() == null && event.getCurrentItem().getItemMeta() == null) return;
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
			itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getLore().get(1)).substring(2);
			if(findAndUpdatePlayerLanguage(player, itemName)) {
				//Updated language to %language% successfully!
				player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.languageInventory.success").replace("%language%", itemName));
			}else {
				//Error whilst updating to language %language%
				player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.languageInventory.error").replace("%language%", itemName));
			}
		}else {
			LotusController lc = new LotusController();
			String item = event.getCurrentItem().getItemMeta().getDisplayName();
			String noMoveMsg = lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.playerInventory.cancel").replace("%item%", item);
			if(item.equalsIgnoreCase(HotbarItem.hb_extras) || item.equalsIgnoreCase(HotbarItem.hb_friends) || 
					item.equalsIgnoreCase(HotbarItem.hb_hider_all) || item.equalsIgnoreCase(HotbarItem.hb_hider_none) || 
					item.equalsIgnoreCase(HotbarItem.hb_hider_staff) || item.equalsIgnoreCase(HotbarItem.hb_language) || item.equalsIgnoreCase(HotbarItem.hb_navigator)) {
				player.sendMessage(noMoveMsg);
				event.setCancelled(true);
			}else {
				event.setCancelled(false);
			}
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		String item = event.getItemDrop().getItemStack().getItemMeta().getDisplayName();
		LotusController lc = new LotusController();
		// You can't drop %item%!
		String noDropMsg = lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.drop.cancel").replace("%item%", item);
		if(item.equalsIgnoreCase(HotbarItem.hb_extras) || item.equalsIgnoreCase(HotbarItem.hb_friends) || 
				item.equalsIgnoreCase(HotbarItem.hb_hider_all) || item.equalsIgnoreCase(HotbarItem.hb_hider_none) || 
				item.equalsIgnoreCase(HotbarItem.hb_hider_staff) || item.equalsIgnoreCase(HotbarItem.hb_language) || item.equalsIgnoreCase(HotbarItem.hb_navigator)) {
			event.setCancelled(true);
			player.sendMessage(noDropMsg);
		}else {
			event.setCancelled(false);
		}
	}
	
	
	//Playerhider is not yet testable with one account! Will happen once we've got a somewhat stable infrastructure.
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		if(BuildCMD.hasPlayer(player)) {
			event.setCancelled(false);
		}else {
			if(event.getItem() != null && event.getItem().getItemMeta() != null) {
				String itemName = event.getItem().getItemMeta().getDisplayName();
				if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
					LotusController lc = new LotusController();
					if(itemName.equalsIgnoreCase(HotbarItem.hb_extras)) {
						event.setCancelled(true);
						lc.sendMessageReady(player, "event.hotbar.open.extras");
						setExtrasInventory(player);
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_friends)) {
						event.setCancelled(true);
						lc.sendMessageReady(player, "event.hotbar.open.friends");
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_hider_all)) {
						event.setCancelled(true);
						player.getInventory().setItem(6, lc.defItem(Material.MAGENTA_DYE, HotbarItem.hb_hider_staff, 1));
						for(Player all : Bukkit.getOnlinePlayers()) {
							if(!all.hasPermission("lgc.isStaff")) {
								player.hidePlayer(Main.main, all);
							}
						}
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_hider_staff)) {
						event.setCancelled(true);
						player.getInventory().setItem(6, lc.defItem(Material.GRAY_DYE, HotbarItem.hb_hider_none, 1));
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_hider_none)) {
						event.setCancelled(true);
						player.getInventory().setItem(6, lc.defItem(Material.LIME_DYE, HotbarItem.hb_hider_all, 1));
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_language)) {
						event.setCancelled(true);
						lc.sendMessageReady(player, "event.hotbar.open.language");
						setLanguageInventory(player);
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_navigator)) {
						event.setCancelled(true);
						setNavigatorInventory(player);
						lc.sendMessageReady(player, "event.hotbar.open.navigator");
					}else {
						event.setCancelled(false);
					}
				}
			}
		}
	}
	
	private void sendPlayerToServer(Player player, String fancyName, String destinationServer, LotusController lc) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeUTF("Connect");
			dos.writeUTF(destinationServer);
			player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.navigator.sendPlayer.success"));
			Main.logger.info(player.getName() + " has been sent to " + destinationServer + " successfully.");
		} catch (IOException e) {
			Main.logger.severe(player.getName() + " attempted to be sent to " + destinationServer + " but failed!");
			e.printStackTrace();
		}
		player.sendPluginMessage(Main.main, "BungeeCord", baos.toByteArray());
	}
	
	private static HashMap<String, String> servers = new HashMap<>();
	private static HashMap<String, String> langs = new HashMap<>();
	
	public static void loadServer() {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT displayname,bungeeKey FROM mc_serverstats");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				servers.put(rs.getString("displayname"), rs.getString("bungeeKey"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			LotusController lc = new LotusController();
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM core_translations WHERE path = ?");
			ps.setString(1, "mcinternal.language");
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				for(String string : lc.getAvailableLanguages()) {
					langs.put(string, rs.getString(string));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private boolean findAndUpdatePlayerLanguage(Player player, String newLanguage) {
		boolean success = false;
		if(langs.containsKey(newLanguage)) {
			LotusController.playerLanguages.put(player.getUniqueId().toString(), newLanguage);
			try {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE mc_users SET language = ? WHERE mcuuid = ?");
				ps.setString(1, newLanguage);
				ps.setString(2, player.getUniqueId().toString());
				ps.executeUpdate();
				success = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else {
			success = false;
		}
		return success;
	}
	
	private HashMap<String, String> getServerFancynames(){
		return servers;
	}
	
	private static List<Material> matList(){
		List<Material> matList = new ArrayList<Material>();
		matList.add(Material.WHITE_CONCRETE_POWDER);
		matList.add(Material.LIGHT_GRAY_CONCRETE_POWDER);
		matList.add(Material.GRAY_CONCRETE_POWDER);
		matList.add(Material.BLACK_CONCRETE_POWDER);
		matList.add(Material.BROWN_CONCRETE_POWDER);
		matList.add(Material.RED_CONCRETE_POWDER);
		matList.add(Material.ORANGE_CONCRETE_POWDER);
		matList.add(Material.YELLOW_CONCRETE_POWDER);
		matList.add(Material.LIME_CONCRETE_POWDER);
		matList.add(Material.GREEN_CONCRETE_POWDER);
		matList.add(Material.CYAN_CONCRETE_POWDER);
		matList.add(Material.LIGHT_BLUE_CONCRETE_POWDER);
		matList.add(Material.BLUE_CONCRETE_POWDER);
		matList.add(Material.PURPLE_CONCRETE_POWDER);
		matList.add(Material.PINK_CONCRETE_POWDER);
		matList.add(Material.MAGENTA_CONCRETE_POWDER);
		return matList;
	}

}