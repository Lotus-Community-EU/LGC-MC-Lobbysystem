package eu.lotusgc.mc.misc;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import eu.lotusgc.mc.ext.LotusController;

public class HotbarItem {
	
	public static String hb_extras = "§9Extras";
	public static String hb_language = "§6Language";
	public static String hb_navigator = "§aNavigator";
	public static String hb_hider_all = "§7Visibility: §aAll";
	public static String hb_hider_staff = "§7Visibility: §6Staff";
	public static String hb_hider_none = "§7Visibility: §cNone";
	public static String hb_friends = "§3Friends";
	
	public void setHotbarItems(Player player){
		LotusController lc = new LotusController();
		player.getInventory().clear();
		player.getInventory().setItem(0, lc.defItem(Material.ECHO_SHARD, hb_extras, 1)); //extras
		player.getInventory().setItem(2, lc.defItem(Material.BOOK, hb_language, 1)); //language
		player.getInventory().setItem(4, lc.defItem(Material.COMPASS, hb_navigator, 1)); //navigator
		player.getInventory().setItem(6, lc.defItem(Material.LIME_DYE, hb_hider_all, 1)); //loop thru see player-staff-none
		player.getInventory().setItem(8, lc.skullItem(1, hb_friends, player.getName()));
	}

}
