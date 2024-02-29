//Created by Chris Wille at 28.02.2024
package eu.lotusgc.mc.misc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import eu.lotusgc.mc.main.LotusManager;

public class RewardsUtils {
	
	public static String r_daily = "§cDaily §aRewards";
	public static String r_advents = "§cA§fd§cv§fe§cn§ft §cC§fa§cl§fe§cn§fd§ca§fr";
	
	
	public String getRemainingTime(Player player, String node) {
		long current = System.currentTimeMillis();
		long remaining = dr_getTime(player, node);
		long difference = remaining - current;
		long seconds = difference / 1000;
		long hours = (seconds % (24* 3600)) / 3600; 
		long minutes = (seconds % 3600) / 60;
		long remainingSeconds = seconds % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
	}
	
	public long dr_getTime(Player player, String node) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(LotusManager.dailyRewardsConfig);
		return cfg.getLong(player.getUniqueId().toString() + "." + node);
	}
	
	public boolean dr_canBeRewarded(Player player, String node) {
		long current = System.currentTimeMillis();
		long saved = dr_getTime(player, node);
		return (current >= saved);
	}
	
	public void dr_setReward(Player player, String node) {
		long nowPlus1Day = System.currentTimeMillis() + 86400000;
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(LotusManager.dailyRewardsConfig);
		cfg.set(player.getUniqueId().toString() + "." + node, nowPlus1Day);
		try {
			cfg.save(LotusManager.dailyRewardsConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean ac_hasRewardUsed(Player player, int day) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(LotusManager.adventsRewardsConfig);
		return cfg.contains(player.getUniqueId().toString() + "." + day);
	}
	
	public void ac_setReward(Player player, int day) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(LotusManager.adventsRewardsConfig);
		cfg.set(player.getUniqueId().toString() + "." + day, true);
		try {
			cfg.save(LotusManager.adventsRewardsConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isAllowedDate(int day) {
		Date date = new Date(1734009897000l);
		if(new SimpleDateFormat("MM").format(date).equals("12")) {
			int currentDay = Integer.parseInt(new SimpleDateFormat("dd").format(date));
			if(currentDay >= day) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}
	
	public Set<Integer> randomSlots(int low, int max, int amount) {
		Set<Integer> set = new Random().ints(low, max)
				.distinct()
				.limit(amount)
				.boxed()
				.collect(Collectors.toSet());
		return set;
	}

}