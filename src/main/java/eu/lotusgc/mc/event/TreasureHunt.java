//Created by Chris Wille at 28.02.2024
package eu.lotusgc.mc.event;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;

import eu.lotusgc.mc.command.BuildCMD;
import eu.lotusgc.mc.ext.LotusController;
import eu.lotusgc.mc.main.LotusManager;
import eu.lotusgc.mc.misc.Money;
import eu.lotusgc.mc.misc.MySQL;
import eu.lotusgc.mc.misc.Prefix;
import net.md_5.bungee.api.ChatColor;

public class TreasureHunt implements Listener {
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		if(event.getLine(0).equals("[th]")) {
			if(player.hasPermission("lgc.addTreasureHuntSigns")) {
				String id = event.getLine(1);
				String special = event.getLine(2);
				boolean speciale = false;
				if(special.isBlank() || special.equalsIgnoreCase("false")) {
					speciale = false;
				}else if(special.equalsIgnoreCase("true")) {
					speciale = true;
				}
				if(speciale) {
					event.setLine(0, "§7°                    °");
					event.setLine(1, "§6Treasure Hunt");
					event.setLine(2, "§5§l" + id);
					Sign sign = (Sign) event.getBlock().getState();
					sign.getTargetSide(player).setGlowingText(true);
					sign.update();
				}else {
					event.setLine(0, "§7°                    °");
					event.setLine(1, "§aTreasure Hunt");
					event.setLine(2, "§6§l" + id);
				}
				addTreasureSign(event.getBlock().getLocation(), id, speciale);
			}
		}
	}
	
	@EventHandler
	public void onSignOpen(PlayerSignOpenEvent event) {
		Player player = event.getPlayer();
		if(isTreasureSign(event.getSign().getLocation())) {
			event.setCancelled(true);
		}else {
			if(BuildCMD.hasPlayer(player)) {
				event.setCancelled(false);
			}else {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(isSign(event.getBlock().getType())) {
			if(BuildCMD.hasPlayer(event.getPlayer())) {
				if(isTreasureSign(event.getBlock().getLocation())) {
					deleteTreasureSign(event.getBlock().getLocation());
				}
			}
		}
	}
	
	@EventHandler
	public void onSignInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if(event.getClickedBlock() == null) return;
		if(isSign(event.getClickedBlock().getType())) {
			Player player = event.getPlayer();
			Sign sign = (Sign) event.getClickedBlock().getState();
			LotusController lc = new LotusController();
			if(action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
				String line1 = ChatColor.stripColor(sign.getTargetSide(player).getLine(1));
				if(line1.equalsIgnoreCase("Treasure Hunt")) {
					String line2 = ChatColor.stripColor(sign.getTargetSide(player).getLine(2));
					if(hasClaimedTH(line2, player)) {
						lc.sendMessageReady(player, "event.treasurehunt.alreadyClaimed");
					}else {
						setClaimed(line2, player);
						int claims = getClaims(line2);
						updateClaims(line2, (claims + 1));
						if(isSpecialSign(event.getClickedBlock().getLocation())) {
							double random = ThreadLocalRandom.current().nextDouble(25.0, 250.0);
							DecimalFormat df = new DecimalFormat("#.##");
							lc.addMoney(player, Double.valueOf(df.format(random)), Money.POCKET);
							player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.treasurehunt.claimed").replace("%gift%", "§a" + df.format(random) + " §7Coins"));
						}else {
							double random = ThreadLocalRandom.current().nextDouble(0.5, 15.0);
							DecimalFormat df = new DecimalFormat("#.##");
							lc.addMoney(player, Double.valueOf(df.format(random)), Money.POCKET);
							player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "event.treasurehunt.claimed").replace("%gift%", "§a" + df.format(random) + " §7Coins"));
						}
					}
				}
			}
		}
	}
	
	boolean hasClaimedTH(String key, Player player) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(LotusManager.treasureHuntConfig);
		boolean hasClaimed = false;
		if(cfg.contains(player.getUniqueId().toString() + ".TH" + key)) {
			hasClaimed = cfg.getBoolean(player.getUniqueId().toString() + ".TH" + key);
		}
		return hasClaimed;
	}
	
	void setClaimed(String key, Player player) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(LotusManager.treasureHuntConfig);
		cfg.set(player.getUniqueId().toString() + ".TH" + key, true);
		try {
			cfg.save(LotusManager.treasureHuntConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	boolean isTreasureSign(Location location) {
		boolean treasureSign = false;
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM mc_treasurehunt WHERE x = ? AND y = ? AND z = ? AND world = ?");
			ps.setInt(1, location.getBlockX());
			ps.setInt(2, location.getBlockY());
			ps.setInt(3, location.getBlockZ());
			ps.setString(4, location.getWorld().getName());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				treasureSign = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return treasureSign;
	}
	
	boolean isSpecialSign(Location location) {
		boolean treasureSign = false;
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT special FROM mc_treasurehunt WHERE x = ? AND y = ? AND z = ? AND world = ?");
			ps.setInt(1, location.getBlockX());
			ps.setInt(2, location.getBlockY());
			ps.setInt(3, location.getBlockZ());
			ps.setString(4, location.getWorld().getName());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				treasureSign = rs.getBoolean("special");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return treasureSign;
	}
	
	void addTreasureSign(Location location, String key, boolean special) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("INSERT INTO mc_treasurehunt(treasureKey,world,x,y,z,special) VALUES (?,?,?,?,?,?)");
			ps.setString(1, key);
			ps.setString(2, location.getWorld().getName());
			ps.setInt(3, location.getBlockX());
			ps.setInt(4, location.getBlockY());
			ps.setInt(5, location.getBlockZ());
			ps.setBoolean(6, special);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void deleteTreasureSign(Location location) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("DELETE FROM mc_treasurehunt WHERE world = ? AND x = ? AND y = ? AND z = ?");
			ps.setString(1, location.getWorld().getName());
			ps.setInt(2, location.getBlockX());
			ps.setInt(3, location.getBlockY());
			ps.setInt(4, location.getBlockZ());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	int getClaims(String key) {
		int claims = 0;
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT claims FROM mc_treasurehunt WHERE treasureKey = ?");
			ps.setString(1, key);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				claims = rs.getInt("claims");
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return claims;
	}
	
	void updateClaims(String key, int claims) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE mc_treasurehunt SET claims = ? WHERE treasureKey = ?");
			ps.setInt(1, claims);
			ps.setString(2, key);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//if input material is contained in list, returns true, otherwise false
	boolean isSign(Material input) {
		List<Material> signs = new ArrayList<>();
		signs.add(Material.OAK_WALL_SIGN);
		signs.add(Material.OAK_WALL_HANGING_SIGN);
		signs.add(Material.OAK_SIGN);
		signs.add(Material.SPRUCE_WALL_SIGN);
		signs.add(Material.SPRUCE_WALL_HANGING_SIGN);
		signs.add(Material.SPRUCE_SIGN);
		signs.add(Material.BIRCH_WALL_SIGN);
		signs.add(Material.BIRCH_WALL_HANGING_SIGN);
		signs.add(Material.BIRCH_SIGN);
		signs.add(Material.JUNGLE_WALL_SIGN);
		signs.add(Material.JUNGLE_WALL_HANGING_SIGN);
		signs.add(Material.JUNGLE_SIGN);
		signs.add(Material.ACACIA_WALL_SIGN);
		signs.add(Material.ACACIA_WALL_HANGING_SIGN);
		signs.add(Material.ACACIA_SIGN);
		signs.add(Material.DARK_OAK_WALL_SIGN);
		signs.add(Material.DARK_OAK_WALL_HANGING_SIGN);
		signs.add(Material.DARK_OAK_SIGN);
		signs.add(Material.MANGROVE_WALL_SIGN);
		signs.add(Material.MANGROVE_WALL_HANGING_SIGN);
		signs.add(Material.MANGROVE_SIGN);
		signs.add(Material.CHERRY_WALL_SIGN);
		signs.add(Material.CHERRY_WALL_HANGING_SIGN);
		signs.add(Material.CHERRY_SIGN);
		signs.add(Material.BAMBOO_WALL_SIGN);
		signs.add(Material.BAMBOO_WALL_HANGING_SIGN);
		signs.add(Material.BAMBOO_SIGN);
		signs.add(Material.CRIMSON_WALL_SIGN);
		signs.add(Material.CRIMSON_WALL_HANGING_SIGN);
		signs.add(Material.CRIMSON_SIGN);
		signs.add(Material.WARPED_WALL_SIGN);
		signs.add(Material.WARPED_WALL_HANGING_SIGN);
		signs.add(Material.WARPED_SIGN);
		return signs.contains(input);
	}
}