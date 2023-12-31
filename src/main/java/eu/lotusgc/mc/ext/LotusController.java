package eu.lotusgc.mc.ext;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import eu.lotusgc.mc.main.Main;
import eu.lotusgc.mc.misc.MySQL;
import eu.lotusgc.mc.misc.Playerdata;
import eu.lotusgc.mc.misc.Prefix;
import eu.lotusgc.mc.misc.Serverdata;
import net.md_5.bungee.api.ChatColor;

public class LotusController {
	
	// < - - - INSTANCES FOR ALL SECTIONS GROUPED IN ORDER - - - >
	
	//Language System
	private static HashMap<String, HashMap<String, String>> langMap = new HashMap<>();
	public static HashMap<String, String> playerLanguages = new HashMap<>();
	private static List<String> availableLanguages = new ArrayList<>();
	
	//Prefix System
	private static HashMap<String, String> prefix = new HashMap<>();
	private static boolean useSeasonalPrefix = false;
	
	//Servername and ServerID
	private static String servername = "Server";
	private static String serverid = "0";
	
	//misc
	
	public static String navigatorTitle = "";
	public static String extrasTitle = "";
	public static String languageTitle = "";
	public static String dailyRewardsTitle = "";
	public static String cratesTitle = "";
	
	// < - - - END OF INSTANCES - - - >
	
	/* Server reads out how many columns there are for the language system. 
	 * For each entry (except the key value and optionings) a HashMap<String, String> will be created within a HashMap<String, HashMap<String, String>>
	 * Also it will "download" all keys and their respective value to have less methods for the init.
	 * IF the map keeps empty due to an unknown reason, then the path will be given out.
	 */
	public boolean initLanguageSystem() {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM core_translations");
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd =  rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			
			while(rs.next()) {
				HashMap<String, String> map;
				for(int i = 6; i <= columnCount; i++) {
					String name = rsmd.getColumnName(i);
					availableLanguages.add(name);
					PreparedStatement ps1 = MySQL.getConnection().prepareStatement("SELECT path," + name + ",isGame FROM core_translations");
					ResultSet rs1 = ps1.executeQuery();
					map = new HashMap<>();
					while(rs1.next()) {
						if(rs1.getBoolean("isGame")) {
							//Only get Strings, which are for the game (what would we do with website/bot string, right?)
							map.put(rs1.getString("path"), rs1.getString(name));
						}
					}
					langMap.put(name, map);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return langMap.isEmpty();
	}
	
	public List<String> getAvailableLanguages() {
		return availableLanguages;
	}
	
	public boolean initPlayerLanguages() {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT mcuuid,language FROM mc_users");
			ResultSet rs = ps.executeQuery();
			int count = 0;
			while(rs.next()) {
				count++;
				playerLanguages.put(rs.getString("mcuuid"), rs.getString("language"));
			}
			rs.close();
			ps.close();
			Main.logger.info("Initialised " + count + " users for the language system. | Source: LotusController#initPlayerLanguages();");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return playerLanguages.isEmpty();
	}
	
	//only used, when a never-joined player joins the network.
	public void addPlayerLanguageWhenRegistered(Player player) {
		playerLanguages.put(player.getUniqueId().toString(), "English");
		Main.logger.info("Added " + player.getName() + " to the languageMap with default. | Source: LotusController#addPlayerLanguageWhenRegistered(PLAYER);");
	}
	
	//This method is used if no spaceholders needs to be translated additionally.
	public void sendMessageReady(Player player, String path) {
		player.sendMessage(getPrefix(Prefix.MAIN) + sendMessageToFormat(player, path));
	}
	
	//This method is used if spaceholders needs to be translated before sending (or if the target is NOT a player).
	public String sendMessageToFormat(Player player, String path) {
		String toReturn = returnString(returnLanguage(player), path);
		if(toReturn.equalsIgnoreCase("none")) {
			return returnString("English", path);
		}else {
			return toReturn;
		}
	}
	
	//This method is returns the player's selected language.
	public String returnLanguage(Player player) {
		String defaultLanguage = "English";
		if(playerLanguages.containsKey(player.getUniqueId().toString())) {
			defaultLanguage = playerLanguages.get(player.getUniqueId().toString());
		}
		return defaultLanguage;
	}
	
	//This method is just for one string, the NoPerm one
	public void noPerm(Player player, String lackingPermissionNode) {
		player.sendMessage(getPrefix(Prefix.System) + sendMessageToFormat(player, "global.noPermission").replace("%permissionNode%", lackingPermissionNode));
	}
	
	//This method returns the String from the language selected.
	private String returnString(String language, String path) {
		if(langMap.containsKey(language)) {
			HashMap<String, String> localMap = langMap.get(language);
			if(localMap.containsKey(path)) {
				return ChatColor.translateAlternateColorCodes('&', localMap.get(path));
			}else {
				return "The path '" + path + "' does not exist!";
			}
		}else {
			return "The language '" + language + "' does not exist!";
		}
	}
	
	// < - - - END OF LANGUAGE SYSTEM - - - >
	// < - - - BEGIN OF THE PREFIX SYSTEM - - - >
	
	//initialise the Prefix System (also used to re-load it after a command reload)
	public void initPrefixSystem() {
		if(!prefix.isEmpty()) prefix.clear();
		
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM mc_prefix");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				if(rs.getString("type").equalsIgnoreCase("UseSeason")) {
					useSeasonalPrefix = translateToBool(rs.getString("prefix"));
					Main.logger.info("Using Seasonal Prefix | Source: LotusController#initPrefixSystem()");
					Bukkit.getConsoleSender().sendMessage("Using Seasonal Prefix!");
				}
				prefix.put(rs.getString("type"), rs.getString("prefix").replace('&', '§'));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private boolean translateToBool(String input) {
		switch(input) {
		case "TRUE": return true;
		case "FALSE": return false;
		default: return false;
		}
	}
	
	//get Prefix with the Enum class "eu.lotusgc.mc.misc.Prefix"
	public String getPrefix(Prefix prefixType) {
		String toReturn = "";
		switch(prefixType) {
		case MAIN: if(useSeasonalPrefix) { toReturn = prefix.get("SEASONAL_MAIN"); } else { toReturn = prefix.get("MAIN"); }
			break;
		case PMSYS: toReturn = prefix.get("PMSYS");
			break;
		case SCOREBOARD: if(useSeasonalPrefix) { toReturn = prefix.get("SEASONAL_SB"); } else { toReturn = prefix.get("SCOREBOARD"); }
			break;
		case System: toReturn = prefix.get("SYSTEM");
			break;
		default: toReturn = prefix.get("MAIN");
			break;
		}
		return toReturn;
	}
	
	// < - - - END OF PREFIX SYSTEM - - - >
	// < - - - BEGIN OF THE ITEMSTACKS - - - >
	
	public ItemStack defItem(Material material, String displayName, int amount) {
		ItemStack is = new ItemStack(material, amount);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(displayName);
		is.setItemMeta(im);
		return is;
	}
	
	public ItemStack potionItem(int amount, PotionType potionType, String displayName, Color potionColor) {
		ItemStack is = new ItemStack(Material.POTION, amount);
		PotionMeta pm = (PotionMeta) is.getItemMeta();
		pm.setBasePotionType(potionType);
		pm.setColor(potionColor);
		pm.setDisplayName(displayName);
		is.setItemMeta(pm);
		return is;
	}
	
	public ItemStack enchantedItem(Material material, int amount, String displayName, Enchantment enchantment) {
		ItemStack is = new ItemStack(material, amount);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(displayName);
		im.addEnchant(enchantment, 1, true);
		is.setItemMeta(im);
		return is;
	}
	
	public ItemStack loreItem(Material material, int amount, String displayname, String... lore) {
		List<String> loreList = new ArrayList<String>();
		for(String string : lore) {
			loreList.add(string);
		}
		ItemStack is = new ItemStack(material, amount);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(displayname);
		im.setLore(loreList);
		is.setItemMeta(im);
		return is;
	}
	
	// < - - - EN OF THE ITEMSTACKS - - - >
	// < - - - BEGIN OF THE MISC UTILS - - - >
	
	//load server id and name into cache
	public void loadServerIDName() {
		File file = new File("server.properties");
		Properties p = new Properties();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))){
			p.load(bis);
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		servername = p.getProperty("server-name");
		serverid = p.getProperty("server-id");
	}
	
	//get the server name
	public String getServerName() {
		return servername;
	}
	
	//get the server id
	public String getServerId() {
		return serverid;
	}
	
	//Original by Grubsic (LGC Vice Project Leader) | Thank you for your contributions! <3
	private static final Pattern HEX_PATTERN = Pattern.compile("#[0-9a-fA-F]{6}");
	public static String translateHEX(String text) {
		Matcher matcher = HEX_PATTERN.matcher(text);
		while(matcher.find()) { text = text.replace(matcher.group(), ChatColor.of(matcher.group()).toString()); }
		return text;
	}
	
	//Get the players of a chosen server - returns 0 if server is nonexistent | Type is current, staff or max
	public String getServerData(String server, Serverdata data) {
		String toReturn = "";
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT " + data.getColumnName() + " FROM mc_serverstats WHERE servername = ?");
			ps.setString(1, server);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				toReturn = rs.getString(data.getColumnName());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return toReturn;
	}
	
	public String getPlayerData(Player player, Playerdata data) {
		String toReturn = "";
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT " + data.getColumnName() + " FROM mc_users WHERE mcuuid = ?");
			ps.setString(1, player.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				toReturn = rs.getString(data.getColumnName());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return toReturn;
	}
}