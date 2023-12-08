package eu.lotusgc.mc.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import eu.lotusgc.mc.misc.MySQL;

public class JoinEvent implements Listener{
	
	/*
	 * DOCUMENTATION PURPOSES
	 * The "lgcid" is our internal ID system, like it was on RediCraft, just improvised and way more stable.
	 * It will be used to lookup, ban, warn, etc. as well as other internal stuff
	 * The ID can be changed again, but must be unique, too.
	 */
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		if(existPlayer(player.getUniqueId())) {
			updateOnlineStatus(player.getUniqueId(), true);
		}else {
			Set<Integer> existingIDs = getExistingIDs();
			addPlayerToDB(player, existingIDs);
			updateOnlineStatus(player.getUniqueId(), true);
		}
		
		event.setJoinMessage(null);
	}
	
	//checks wether the player already exists or not
	private boolean existPlayer(UUID uuid) {
		boolean exists = false;
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT mcuuid from mc_users WHERE mcuuid = ?");
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				exists = true;
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return exists;
	}
	
	//update the online status (true for online, false for offline)
	private void updateOnlineStatus(UUID uuid, boolean status) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE mc_users SET isOnline = ? WHERE mcuuid = ?");
			ps.setBoolean(1, status);
			ps.setString(2, uuid.toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//retrieve all IDs which are currently given out to players
	private Set<Integer> getExistingIDs(){
		Set<Integer> existingIDs = new HashSet<>();
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT lgcid FROM mc_users");
			ResultSet rs = ps.executeQuery();
			int count = 0;
			while(rs.next()) {
				count++;
				existingIDs.add(rs.getInt("lgcid"));
			}
			Bukkit.getConsoleSender().sendMessage("§aRetrieved §6" + count + " §aIDs, ready to assign.");
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return existingIDs;
	}
	
	//creates a new row in the player table and assigns an ID to them
	private void addPlayerToDB(Player player, Set<Integer> knownIDs) {
		int newID;
		do {
			newID = randomInt(0, 9999);
		}while (knownIDs.contains(newID));
		
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("INSERT INTO mc_users SET mcuuid, lgcid, name, firstJoin, currentLastServer, isOnline VALUES ?, ?, ?, ?, ?, ?");
			ps.setString(1, player.getUniqueId().toString());
			ps.setInt(2, newID);
			ps.setString(3, player.getName());
			ps.setLong(4, System.currentTimeMillis());
			ps.setString(5, "Lobby");
			ps.setBoolean(6, true);
			ps.executeUpdate();
			Bukkit.getConsoleSender().sendMessage("§aPlayer §6" + player.getName() + " §ahas been assigned the ID §6" + newID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	//generates an random int with min-max value
	private int randomInt(int low, int max) {
		Random r = new Random();
		int number = r.nextInt(max);
		while (number < low) {
			number = r.nextInt(max);
		}
		return number;
	}

}