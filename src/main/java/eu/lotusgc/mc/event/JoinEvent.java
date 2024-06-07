package eu.lotusgc.mc.event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.lotusgc.mc.misc.HotbarItem;
import eu.lotusgc.mc.misc.LotusController;
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
		LotusController lc = new LotusController();
		
		//Handling the ID
		if(existPlayer(player.getUniqueId())) {
			updateOnlineStatus(player, true);
		}else {
			Set<Integer> existingIDs = getExistingIDs();
			addPlayerToDB(player, existingIDs);
			updateOnlineStatus(player, true);
			lc.addPlayerLanguageWhenRegistered(player);
		}
		
		//Setting Items
		new HotbarItem().setHotbarItems(player);
		
		player.setGameMode(GameMode.SURVIVAL);
		player.setHealth(20.0);
		player.setFoodLevel(20);
		player.setWalkSpeed((float) 0.2);
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
	private void updateOnlineStatus(Player player, boolean status) {
		try {
			JsonObject jo = getAPIData(player.getAddress().getHostName());
			LotusController lc = new LotusController();
			PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE mc_users SET isOnline = ?, name = ?, timeZone = ?, countryCode = ?, currentLastServer = ? WHERE mcuuid = ?");
			ps.setBoolean(1, status);
			ps.setString(2, player.getName());
			ps.setString(3, jo.get("timeZone").getAsString().replace("\"", ""));
			ps.setString(4, jo.get("countryCode").getAsString().replace("\"", ""));
			ps.setString(5, lc.getServerName());
			ps.setString(6, player.getUniqueId().toString());
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
			PreparedStatement ps = MySQL.getConnection().prepareStatement("INSERT INTO mc_users(mcuuid, lgcid, name, firstJoin, lastJoin, currentLastServer, isOnline) VALUES (?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, player.getUniqueId().toString());
			ps.setInt(2, newID);
			ps.setString(3, player.getName());
			ps.setLong(4, System.currentTimeMillis());
			ps.setLong(5, System.currentTimeMillis());
			ps.setString(6, "Lobby");
			ps.setBoolean(7, true);
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
	
	@SuppressWarnings("deprecation")
	private JsonObject getAPIData(String ip) {
		StringBuilder sb = new StringBuilder();
		try {
			URL uri = new URL("http://api.ipinfodb.com/v3/ip-city/?key=d7859a91e5346872d0378a2674821fbd60bc07ed63684c3286c083198f024138&ip=" + ip + "&format=json");
			URLConnection uc = uri.openConnection();
			BufferedReader bR = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line;
			while((line = bR.readLine()) != null) {
				sb.append(line + "\n");
			}
			bR.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String lortu = sb.toString();
		JsonParser parser = new JsonParser();
		JsonObject jo = (JsonObject) parser.parse(lortu);
		return jo;
	}
	
	/*private JSONObject getAPIData(String ip) {
		String uri = "http://api.ipinfodb.com/v3/ip-city/?key=d7859a91e5346872d0378a2674821fbd60bc07ed63684c3286c083198f024138&ip=" + ip + "&format=json";
		URL url = null;
		try {
			url = new URL(uri);
		}catch (MalformedURLException e) {
		}
		URLConnection uc = null;
		try {
			uc = url.openConnection();
		} catch (IOException e) {
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
		}catch (Exception e) {
		}
		
		String inputLine;
		StringBuilder sb = new StringBuilder();
		try {
			while((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
			}
			in.close();
		}catch (Exception e) {
		}
		return (JSONObject) JSONValue.parse(in);
	}*/
}