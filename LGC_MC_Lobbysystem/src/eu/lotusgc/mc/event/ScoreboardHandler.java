package eu.lotusgc.mc.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import eu.lotusgc.mc.ext.LotusController;
import eu.lotusgc.mc.misc.MySQL;

public class ScoreboardHandler {
	
	private static HashMap<String, String> tabHM = new HashMap<>(); //HashMap for Tab
	private static HashMap<String, String> chatHM = new HashMap<>(); //HashMap for Chat
	private static HashMap<String, String> roleHM = new HashMap<>(); //HashMap for Team Priority (Sorted)
	private static HashMap<String, String> sbHM = new HashMap<>(); //HashMap for Sideboard (Like Chat, just with no additional chars)
	
	
	public static void setScoreboard(Player player) {
		Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective o = sb.registerNewObjective("aaa", Criteria.DUMMY, "LGCINFOBOARD");
	}
	
	public Team getTeam(Scoreboard scoreboard, String role, ChatColor chatcolor) {
		Team team = scoreboard.registerNewTeam(returnPrefix(role, RankType.TEAM));
		team.setPrefix(returnPrefix(role, RankType.TAB));
		team.setColor(chatcolor);
		team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER); //TBD for removal if issues arise.
		return null;
	}
	
	public void initRoles() {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM core_ranks");
			ResultSet rs = ps.executeQuery();
			tabHM.clear();
			chatHM.clear();
			roleHM.clear();
			sbHM.clear();
			while(rs.next()) {
				tabHM.put(rs.getString("ingame-id"), rs.getString("colour") + rs.getString("short"));
				chatHM.put(rs.getString("ingame-id"), rs.getString("colour") + rs.getString("name"));
				roleHM.put(rs.getString("ingame-id"), rs.getString("priority"));
				sbHM.put(rs.getString("ingame-id"), rs.getString("name"));
			}
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

}
