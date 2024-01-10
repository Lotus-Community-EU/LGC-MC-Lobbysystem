package eu.lotusgc.mc.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import eu.lotusgc.mc.command.BuildCMD;
import eu.lotusgc.mc.ext.LotusController;
import eu.lotusgc.mc.misc.HotbarItem;

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
	
	public static String extras_title = "§9Extras";
	public static String extras_pets = "";
	public static String extras_boots = "";
	public static String extras_jboost = "";
	public static String extras_sboost = "";
	
	public static String rewards_title = "";
	public static String rewards_crates = "";
	public static String rewards_dailyRewards = "";
	
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
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_friends)) {
						event.setCancelled(true);
						lc.sendMessageReady(player, "event.hotbar.open.friends");
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_hider_all)) {
						event.setCancelled(true);
						player.getInventory().setItem(6, lc.defItem(Material.MAGENTA_DYE, HotbarItem.hb_hider_staff, 1));
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_hider_staff)) {
						event.setCancelled(true);
						player.getInventory().setItem(6, lc.defItem(Material.GRAY_DYE, HotbarItem.hb_hider_none, 1));
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_hider_none)) {
						event.setCancelled(true);
						player.getInventory().setItem(6, lc.defItem(Material.LIME_DYE, HotbarItem.hb_hider_all, 1));
					}else if(itemName.equalsIgnoreCase(HotbarItem.hb_language)) {
						event.setCancelled(true);
						lc.sendMessageReady(player, "event.hotbar.open.language");
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

}
