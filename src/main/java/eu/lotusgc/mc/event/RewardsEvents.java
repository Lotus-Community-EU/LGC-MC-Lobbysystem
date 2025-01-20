//Created by Chris Wille at 28.02.2024
package eu.lotusgc.mc.event;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;

import eu.lotusgc.mc.misc.LotusController;
import eu.lotusgc.mc.misc.Money;
import eu.lotusgc.mc.misc.Prefix;
import eu.lotusgc.mc.misc.RewardsUtils;

public class RewardsEvents implements Listener{
	
	static void dr_inv(Player player) {
		Inventory inventory = Bukkit.createInventory(null, 3*9, RewardsUtils.r_daily);
		LotusController lc = new LotusController();
		RewardsUtils ru = new RewardsUtils();
		if(player.hasPermission("lgc.dailyrewards.vip")) {
			if(ru.dr_canBeRewarded(player, "vip")) {
				inventory.setItem(12, lc.defItem(Material.CHEST_MINECART, "§aReward §6VIP", 1));
			}else {
				inventory.setItem(12, lc.loreItem(Material.MINECART, 1, "§aReward §6VIP", "§7Remaining Time:", "§a" + ru.getRemainingTime(player, "vip")));
			}
		}
		if(player.hasPermission("lgc.dailyrewards.vip2")) {
			if(ru.dr_canBeRewarded(player, "vip2")) {
				inventory.setItem(14, lc.defItem(Material.CHEST_MINECART, "§aReward §5VIP", 1));
			}else {
				inventory.setItem(14, lc.loreItem(Material.MINECART, 1, "§aReward §5VIP", "§7Remaining Time:", "§a" + ru.getRemainingTime(player, "vip2")));
			}
		}
		if(player.hasPermission("lgc.dailyrewards.staff")) {
			if(ru.dr_canBeRewarded(player, "staff")) {
				inventory.setItem(16, lc.defItem(Material.CHEST_MINECART, "§aReward §cStaff", 1));
			}else {
				inventory.setItem(16, lc.loreItem(Material.MINECART, 1, "§aReward §cStaff", "§7Remaining Time:", "§a" + ru.getRemainingTime(player, "staff")));
			}
		}
		if(ru.dr_canBeRewarded(player, "default")) {
			inventory.setItem(10, lc.defItem(Material.CHEST_MINECART, "§aReward", 1));
		}else {
			inventory.setItem(10, lc.loreItem(Material.MINECART, 1, "§aReward", "§7Remaining Time:", "§a" + ru.getRemainingTime(player, "default")));
		}
		player.openInventory(inventory);
	}
	
	static void ac_inv(Player player) {
		Inventory inventory = Bukkit.createInventory(null, 6*9, RewardsUtils.r_advents);
		LotusController lc = new LotusController();
		RewardsUtils ru = new RewardsUtils();
		Set<Integer> set = ru.randomSlots(0, 54, 24);
		int dayInt = 1;
		for(int i : set) {
			if(ru.isAllowedDate(dayInt)) {
				if(ru.ac_hasRewardUsed(player, dayInt)) {
					inventory.setItem(i, lc.defItem(Material.MINECART, "§cDay " + dayInt + " §7- used", 1));
				}else {
					inventory.setItem(i, lc.defItem(Material.CHEST_MINECART, "§cDay " + dayInt, 1));
				}
			}else {
				if(ru.ac_hasRewardUsed(player, dayInt)) {
					inventory.setItem(i, lc.defItem(Material.MINECART, "§cDay " + dayInt + " §7- used", 1));
				}else {
					inventory.setItem(i, lc.defItem(Material.CHEST_MINECART, "§cA §fD§ca§fy", 1));
				}
			}
			dayInt++;
		}
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void inventoryClick(InventoryClickEvent event) {
		Player player = (Player)event.getWhoClicked();
		LotusController lc = new LotusController();
		RewardsUtils ru = new RewardsUtils();
		if(event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;
		if(event.getView().getTitle().equals(RewardsUtils.r_daily)) {
			event.setCancelled(true);
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
			if(itemName.equalsIgnoreCase("§aReward")) {
				if(ru.dr_canBeRewarded(player, "default")) {
					int random = ThreadLocalRandom.current().nextInt(1, 65);
					ru.dr_setReward(player, "default");
					lc.addMoney(player, random, Money.POCKET);
					player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.rewards.daily.claimed").replace("%type%", "Default Reward").replace("%money%", String.valueOf(random)));
				}else {
					player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.rewards.daily.cooldown").replace("%time%", ru.getRemainingTime(player, "default")));
				}
				dr_inv(player);
			}else if(itemName.equalsIgnoreCase("§aReward §6VIP")) {
				if(ru.dr_canBeRewarded(player, "vip")) {
					int random = ThreadLocalRandom.current().nextInt(66, 250);
					ru.dr_setReward(player, "vip");
					lc.addMoney(player, random, Money.POCKET);
					player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.rewards.daily.claimed").replace("%type%", "§6VIP§7 Reward").replace("%money%", String.valueOf(random)));
				}else {
					player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.rewards.daily.cooldown").replace("%time%", ru.getRemainingTime(player, "default")));
				}
				dr_inv(player);
			}else if(itemName.equalsIgnoreCase("§aReward §5VIP")) {
				if(ru.dr_canBeRewarded(player, "vip2")) {
					int random = ThreadLocalRandom.current().nextInt(66, 500);
					ru.dr_setReward(player, "vip2");
					lc.addMoney(player, random, Money.POCKET);
					player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.rewards.daily.claimed").replace("%type%", "§5VIP§7 Reward").replace("%money%", String.valueOf(random)));
				}else {
					player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.rewards.daily.cooldown").replace("%time%", ru.getRemainingTime(player, "default")));
				}
				dr_inv(player);
			}else if(itemName.equalsIgnoreCase("§aReward §cStaff")) {
				if(ru.dr_canBeRewarded(player, "staff")) {
					int random = ThreadLocalRandom.current().nextInt(1, 250);
					ru.dr_setReward(player, "staff");
					lc.addMoney(player, random, Money.POCKET);
					player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.rewards.daily.claimed").replace("%type%", "Staff Reward").replace("%money%", String.valueOf(random)));
				}else {
					player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.rewards.daily.cooldown").replace("%time%", ru.getRemainingTime(player, "default")));
				}
				dr_inv(player);
			}
		}else if(event.getView().getTitle().equalsIgnoreCase(RewardsUtils.r_advents)) {
			event.setCancelled(true);
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
			if(itemName.equalsIgnoreCase("§cA §fD§ca§fy")) {
				lc.sendMessageReady(player, "events.advents.incorrectday");
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		Entity target = event.getRightClicked();
		if(target.getType() == EntityType.VILLAGER) {
			if(target.getCustomName().equals(RewardsUtils.r_daily)) {
				event.setCancelled(true);
				dr_inv(player);
			}
		}else if(target.getType() == EntityType.SNOW_GOLEM) {
			if(target.getCustomName().equals(RewardsUtils.r_advents)) {
				event.setCancelled(true);
				ac_inv(player);
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		Entity hitter = event.getDamager();
		Entity target = event.getEntity();
		Player player = null;
		LotusController lc = new LotusController();
		if(hitter.getType() == EntityType.PLAYER) player = (Player)hitter;
		if(target.getType() == EntityType.VILLAGER) {
			event.setCancelled(true);
			player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.rewards.daily.hitmessage").replace("%entityName%", target.getCustomName()));
		}else if(target.getType() == EntityType.SNOW_GOLEM) {
			event.setCancelled(true);
			player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.rewards.daily.hitmessage").replace("%entityName%", target.getCustomName()));
		}
	}
}