package eu.lotusgc.mc.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import eu.lotusgc.mc.command.BuildCMD;
import eu.lotusgc.mc.ext.LotusController;
import eu.lotusgc.mc.main.Main;
import eu.lotusgc.mc.misc.CountType;
import eu.lotusgc.mc.misc.MySQL;
import eu.lotusgc.mc.misc.Prefix;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

public class ScoreboardHandler implements Listener{
	
	private static HashMap<String, String> tabHM = new HashMap<>(); //HashMap for Tab
	private static HashMap<String, String> chatHM = new HashMap<>(); //HashMap for Chat
	private static HashMap<String, String> roleHM = new HashMap<>(); //HashMap for Team Priority (Sorted)
	private static HashMap<String, String> sbHM = new HashMap<>(); //HashMap for Sideboard (Like Chat, just with no additional chars)
	
	private static int sbSwitch = 0;
	
	public static void setScoreboard(Player player) {
		Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective o = sb.registerNewObjective("aaa", Criteria.DUMMY, "LGCINFOBOARD");
		LotusController lc = new LotusController();
		int currentUsersNetwork = lc.getPlayers("BungeeCord", CountType.CURRENT_PLAYERS);
		int currentUsersLocal = Bukkit.getOnlinePlayers().size();
		int maxUsers = lc.getPlayers("BungeeCord", CountType.MAX_ALL);
		String sbPrefix = lc.getPrefix(Prefix.SCOREBOARD);
		
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		sbSwitch++;
		if(BuildCMD.hasPlayer(player)) {
			
		}else {
			o.setDisplayName(sbPrefix);
			if(sbSwitch >= 0 && sbSwitch <= 2) {
				//money
				o.getScore("moni").setScore(2);
				o.getScore("cash").setScore(1);
				o.getScore("bank").setScore(0);
			}else if(sbSwitch >= 3 && sbSwitch <= 5) {
				//role
				o.getScore("role").setScore(1);
				o.getScore(retGroup(player)).setScore(0);
			}else if(sbSwitch >= 6 && sbSwitch <= 8) {
				//playerinfo
				o.getScore("id").setScore(3);
				o.getScore("0000").setScore(2);
				o.getScore("Team").setScore(1);
				o.getScore("mothersuckers").setScore(0);
			}else if(sbSwitch >= 9 && sbSwitch <= 11) {
				//serverinfo
				if(sbSwitch == 11) sbSwitch = 0; //resetting the Switcher to 0 so the views are going back again :)
				try {
					PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT displayname,currentPlayers FROM mc_serverstats ORDER BY serverid DESC");
					ResultSet rs = ps.executeQuery();
					int count = 0;
					while(rs.next()) {
						if(!rs.getBoolean("isHiddenGame")) {
							o.getScore(rs.getString("displayname") + "&7: §f" + rs.getInt("currentPlayers")).setScore(count);
						}
					}
					o.getScore("Servers").setScore(count + 1);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		player.setScoreboard(sb);
		
		//Teams will be done later, functionality is now more important (hence no real getters for the sb yet)
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		setScoreboard(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onChat(AsyncPlayerChatEvent event) {
		String message = event.getMessage().replace("&", "&&");
		event.setFormat(event.getPlayer() + "§7: " + message);
	}
	
	public Team getTeam(Scoreboard scoreboard, String role, ChatColor chatcolor) {
		Team team = scoreboard.registerNewTeam(returnPrefix(role, RankType.TEAM));
		team.setPrefix(returnPrefix(role, RankType.TAB));
		team.setColor(chatcolor);
		team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER); //TBD for removal if issues arise.
		return null;
	}
	
	private static String retGroup(Player player) {
		String group = "";
		UserManager um = Main.luckPerms.getUserManager();
		User user = um.getUser(player.getName());
		switch(user.getPrimaryGroup()) {
		default: group = user.getPrimaryGroup(); break;
		}
		return group;
	}
	
	public void initRoles() {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM core_ranks");
			ResultSet rs = ps.executeQuery();
			tabHM.clear();
			chatHM.clear();
			roleHM.clear();
			sbHM.clear();
			int count = 0;
			while(rs.next()) {
				count++;
				tabHM.put(rs.getString("ingame-id"), rs.getString("colour") + rs.getString("short"));
				chatHM.put(rs.getString("ingame-id"), rs.getString("colour") + rs.getString("name"));
				roleHM.put(rs.getString("ingame-id"), rs.getString("priority"));
				sbHM.put(rs.getString("ingame-id"), rs.getString("name"));
			}
			Main.logger.info("Downloaded " + count + " roles for the Prefix System. | Source: ScoreboardHandler#initRoles();");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String returnPrefix(String role, RankType type) {
		String toReturn = "";
		if(type == RankType.TAB) {
			toReturn = tabHM.get(role);
		}else if(type == RankType.CHAT) {
			toReturn = chatHM.get(role);
		}else if(type == RankType.SIDEBOARD) {
			toReturn = sbHM.get(role);
		}else if(type == RankType.TEAM) {
			toReturn = roleHM.get(role);
		}else {
			toReturn = null;
		}
		toReturn = ChatColor.translateAlternateColorCodes('&', toReturn); //transforms & -> §
		toReturn = LotusController.translateHEX(toReturn); //translates HEX Color Codes into Minecraft (Custom Color Codes ability)
		return toReturn;
	}
	
	public enum RankType {
		TAB,
		SIDEBOARD,
		CHAT,
		TEAM
	}
	
	public static void startScheduler(int delay, int sideboardRefresh, int tabRefresh) {
		//SYNC TASK - ONLY FOR THE SIDEBOARD
		new BukkitRunnable() {
			@Override
			public void run() {
				for(Player all : Bukkit.getOnlinePlayers()) {
					setScoreboard(all);
				}
			}
		}.runTaskTimer(Main.main, delay, sideboardRefresh);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				for(Player all : Bukkit.getOnlinePlayers()) {
					all.setPlayerListHeaderFooter("HEADER", "FOOTER");
				}
			}
		}.runTaskTimerAsynchronously(Main.main, delay, tabRefresh);
	}

}
