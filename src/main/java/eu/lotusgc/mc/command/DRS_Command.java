//Created by Chris Wille at 28.02.2024
package eu.lotusgc.mc.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import eu.lotusgc.mc.ext.LotusController;
import eu.lotusgc.mc.main.Main;
import eu.lotusgc.mc.misc.Prefix;
import eu.lotusgc.mc.misc.RewardsUtils;

public class DRS_Command implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			LotusController lc = new LotusController();
			if(args.length == 1) {
				String mode = args[0];
				if(mode.equalsIgnoreCase("daily")) {
					if(player.hasPermission("lgc.rewards.daily")) {
						Villager villager = (Villager)player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
						villager.setAgeLock(true);
						villager.setAdult();
						villager.setCustomNameVisible(true);
						villager.setCustomName(RewardsUtils.r_daily);
						villager.setProfession(Profession.NONE);
					}else {
						lc.noPerm(player, "lgc.rewards.daily");
					}
				}else if(mode.equalsIgnoreCase("advents")) {
					if(player.hasPermission("lgc.rewards.advents")) {
						Snowman sman = (Snowman)player.getWorld().spawnEntity(player.getLocation(), EntityType.SNOWMAN);
						sman.setCustomNameVisible(true);
						sman.setCustomName(RewardsUtils.r_advents);
						sman.setDerp(true);
						sman.setGliding(true);
					}
				}
			}else {
				player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "global.args") + "§7/rewards <daily|advents>");
			}
		}else {
			Bukkit.getConsoleSender().sendMessage(Main.consoleSend);
		}
		return true;
	}
}