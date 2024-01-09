package eu.lotusgc.mc.misc;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import eu.lotusgc.mc.ext.LotusController;

public class HotbarItem {
	
	public void setHotbarItems(Player player){
		LotusController lc = new LotusController();
		player.getInventory().clear();
		player.getInventory().setItem(0, lc.defItem(Material.ECHO_SHARD, "", 1)); //extras
		player.getInventory().setItem(2, lc.defItem(Material.BOOK, "", 1)); //language
		player.getInventory().setItem(4, lc.defItem(Material.COMPASS, "", 1)); //navigator
		player.getInventory().setItem(6, lc.defItem(Material.LIME_DYE, "", 1)); //loop thru see player-staff-none
		player.getInventory().setItem(8, lc.defItem(Material.PLAYER_HEAD, "", 1));
	}

}
