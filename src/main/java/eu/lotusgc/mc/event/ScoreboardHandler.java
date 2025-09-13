package eu.lotusgc.mc.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import eu.lotusgc.mc.command.BuildCMD;
import eu.lotusgc.mc.main.Main;
import eu.lotusgc.mc.misc.InputType;
import eu.lotusgc.mc.misc.LotusController;
import eu.lotusgc.mc.misc.LotusPlayer;
import eu.lotusgc.mc.misc.MySQL;
import eu.lotusgc.mc.misc.Playerdata;
import eu.lotusgc.mc.misc.Prefix;
import eu.lotusgc.mc.misc.ServerRestarter;
import eu.lotusgc.mc.misc.Serverdata;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

public class ScoreboardHandler implements Listener {

	private static HashMap<String, String> tabHM = new HashMap<>(); // HashMap for Tab
	private static HashMap<String, String> chatHM = new HashMap<>(); // HashMap for Chat
	private static HashMap<String, String> roleHM = new HashMap<>(); // HashMap for Team Priority (Sorted)
	private static HashMap<String, String> sbHM = new HashMap<>(); // HashMap for Sideboard (Like Chat, just with no
																	// additional chars)
	public static HashMap<Player, Long> buildTime = new HashMap<>();

	private static int sbSwitch = 0;

	public void setScoreboard(Player player) {
		Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective o = sb.registerNewObjective("aaa", Criteria.DUMMY, "LGCINFOBOARD");
		LotusController lc = new LotusController();
		String sbPrefix = lc.getPrefix(Prefix.SCOREBOARD);
		String currentUsersNetwork = lc.getServerData("BungeeCord", Serverdata.CurrentPlayers, InputType.Servername);
		int currentUsersLocal = Bukkit.getOnlinePlayers().size();
		String maxUsers = lc.getServerData("BungeeCord", Serverdata.MaxPlayers, InputType.Servername);

		o.setDisplaySlot(DisplaySlot.SIDEBAR);

		sbSwitch++;
		if (sbSwitch == 15)
			sbSwitch = 0;

		if (BuildCMD.hasPlayer(player)) {
			ItemStack mainHandItem = player.getInventory().getItemInMainHand();
			ItemStack offHandItem = player.getInventory().getItemInOffHand();
			o.setDisplayName("§bBuild Statistics");
			o.getScore(lc.sendMessageToFormat(player, "event.scoreboard.build.usedTime")).setScore(6);
			o.getScore("§7» §a" + getBuildTime(player)).setScore(5);
			o.getScore("§0").setScore(4);
			if (!mainHandItem.getType().toString().equalsIgnoreCase("air")) {
				o.getScore(lc.sendMessageToFormat(player, "event.scoreboard.build.block") + "§6").setScore(3);
				o.getScore("§7» §a" + mainHandItem.getType().toString()).setScore(2);
			}
			if (!offHandItem.getType().toString().equalsIgnoreCase("air")) {
				o.getScore(lc.sendMessageToFormat(player, "event.scoreboard.build.block") + "§7").setScore(1);
				o.getScore("§7» §a" + offHandItem.getType().toString()).setScore(0);
			}
		} else {
			o.setDisplayName(sbPrefix);
			if (sbSwitch >= 0 && sbSwitch <= 3) {
				// money
				o.getScore(lc.sendMessageToFormat(player, "event.scoreboard.money")).setScore(2);
				o.getScore("§7» Pocket: §a" + lc.getPlayerData(player, Playerdata.MoneyPocket) + " §6Loti").setScore(1);
				o.getScore("§7» Bank: §e" + lc.getPlayerData(player, Playerdata.MoneyBank) + " §6Loti").setScore(0);
			} else if (sbSwitch >= 4 && sbSwitch <= 7) {
				// role
				o.getScore(lc.sendMessageToFormat(player, "event.scoreboard.role")).setScore(1);
				o.getScore(retGroup(player)).setScore(0);
			} else if (sbSwitch >= 8 && sbSwitch <= 11) {
				// playerinfo
				o.getScore(lc.sendMessageToFormat(player, "event.scoreboard.userid")).setScore(3);
				o.getScore("§7» §a" + lc.getPlayerData(player, Playerdata.LotusChangeID)).setScore(2);
				o.getScore("§7Clan:").setScore(1);
				o.getScore("§7» §a" + lc.getPlayerData(player, Playerdata.Clan)).setScore(0);
			} else if (sbSwitch >= 12 && sbSwitch <= 15) {
				// serverinfo
				// Will be reworked, as Lobby will have ViaVersion (to support 1.12.2 and latest
				// (current 1.20.4)) - as the Hybrid Servers will be running mcv 1.12.2 - all HX
				// servers will be listed on their own to just see servers where I can actually
				// play on. (Website + Discord Bot will also be a possibility to lookup)
				try {
					PreparedStatement ps = MySQL.getConnection().prepareStatement(
							"SELECT displayname,currentPlayers,isHiddenGame,isOnline,isMinigame FROM mc_serverstats ORDER BY serverid DESC");
					ResultSet rs = ps.executeQuery();
					int count = 0;
					while (rs.next()) {
						if (!rs.getBoolean("isHiddenGame") && rs.getBoolean("isOnline")
								&& !rs.getBoolean("isMinigame")) {
							count++;
							o.getScore("§7» " + rs.getString("displayname") + "§7: §f" + rs.getInt("currentPlayers"))
									.setScore(count);
						}
					}
					count++;
					o.getScore("  §6" + currentUsersLocal + "§7, §a" + currentUsersNetwork + " §7/§c " + maxUsers)
							.setScore(count);
					o.getScore("§a§b").setScore(count + 1);
					o.getScore("§7Servers").setScore(count + 2);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		player.setScoreboard(sb);

		Team projlead = getTeam(sb, "projectleader", ChatColor.DARK_GRAY);
		Team viceProjLead = getTeam(sb, "viceprojleader", ChatColor.DARK_GRAY);
		Team humanresources = getTeam(sb, "humanresources", ChatColor.DARK_GRAY);
		Team staffmanager = getTeam(sb, "staffmanager", ChatColor.DARK_GRAY);
		Team devmgr = getTeam(sb, "devmgr", ChatColor.DARK_GRAY);
		Team qamanager = getTeam(sb, "qamanager", ChatColor.DARK_GRAY);
		Team lfmmanager = getTeam(sb, "lfmmanager", ChatColor.DARK_GRAY);
		Team aprsman = getTeam(sb, "aprsman", ChatColor.DARK_GRAY);
		Team sdanalyst = getTeam(sb, "sdanalyst", ChatColor.DARK_GRAY);
		Team staffsupervisor = getTeam(sb, "staffsupervisor", ChatColor.DARK_GRAY);
		Team developer = getTeam(sb, "developer", ChatColor.GRAY);
		Team addon = getTeam(sb, "addon", ChatColor.GRAY);
		Team admin = getTeam(sb, "admin", ChatColor.GRAY);
		Team moderator = getTeam(sb, "moderator", ChatColor.GRAY);
		Team lfmlpresenter = getTeam(sb, "lfmlpresenter", ChatColor.GRAY);
		Team lfmdj = getTeam(sb, "lfmdj", ChatColor.GRAY);
		Team lfmplcurator = getTeam(sb, "lfmplcurator", ChatColor.GRAY);
		Team lfmredactor = getTeam(sb, "lfmredactor", ChatColor.GRAY);
		Team socialmedia = getTeam(sb, "socialmedia", ChatColor.GRAY);
		Team support = getTeam(sb, "support", ChatColor.GRAY);
		Team translator = getTeam(sb, "translator", ChatColor.GRAY);
		Team designer = getTeam(sb, "designer", ChatColor.GRAY);
		Team builder = getTeam(sb, "builder", ChatColor.GRAY);
		Team event = getTeam(sb, "event", ChatColor.GRAY);
		Team retired = getTeam(sb, "retired", ChatColor.WHITE);
		Team beta = getTeam(sb, "beta", ChatColor.WHITE);
		Team userg = getTeam(sb, "default", ChatColor.WHITE);

		for (Player all : Bukkit.getOnlinePlayers()) {
			// Lotus Internal
			LotusPlayer lp = new LotusPlayer(all);
			String nick = lp.getNick();
			String clan = lp.getClan();
			int id = lp.getLGCId();
			boolean logStatus = lp.isLoggedIn();
			if (nick.equalsIgnoreCase("none")) {
				all.setCustomName(all.getName());
			} else {
				all.setCustomName(nick);
			}
			if (clan.equalsIgnoreCase("none")) {
				clan = "";
			}

			// LuckPerms
			UserManager um = Main.luckPerms.getUserManager();
			User user = um.getUser(all.getName());

			if (logStatus) {
				if (user.getPrimaryGroup().equalsIgnoreCase("projectleader")) {
					projlead.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("viceprojectleader")) {
					viceProjLead.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("humanresources")) {
					humanresources.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("staffmanager")) {
					staffmanager.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("devmgr")) {
					devmgr.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("qamanager")) {
					qamanager.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("lfmmanager")) {
					lfmmanager.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("aprsmanager")) {
					aprsman.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("sdanalyst")) {
					sdanalyst.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				}else if (user.getPrimaryGroup().equalsIgnoreCase("staffsupervisor")) {
					staffsupervisor.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("developer")) {
					developer.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("addon")) {
					addon.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				}else if (user.getPrimaryGroup().equalsIgnoreCase("admin")) {
					admin.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("moderator")) {
					moderator.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("lfmlpresenter")) {
					lfmlpresenter.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("lfmdj")) {
					lfmdj.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("lfmplcurator")) {
					lfmplcurator.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("lfmredactor")) {
					lfmredactor.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("socialmedia")) {
					socialmedia.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("support")) {
					support.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("translator")) {
					translator.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("designer")) {
					designer.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("builder")) {
					builder.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("event")) {
					event.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("retired")) {
					retired.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else if (user.getPrimaryGroup().equalsIgnoreCase("beta")) {
					beta.addEntry(all.getName());
					all.setDisplayName(returnPrefix(user.getPrimaryGroup(), RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix(user.getPrimaryGroup(), RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				} else {
					userg.addEntry(all.getName());
					all.setDisplayName(returnPrefix("default", RankType.CHAT) + all.getCustomName());
					all.setPlayerListName(returnPrefix("default", RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
				}
			}else {
				userg.addEntry(all.getName());
				all.setDisplayName(returnPrefix("default", RankType.CHAT) + all.getCustomName());
				all.setPlayerListName(returnPrefix("default", RankType.TAB) + all.getCustomName() + " §7(§a" + id + "§7) §f" + clan);
			}

		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		setScoreboard(event.getPlayer());
		event.setJoinMessage("§8[§a+§8] §7" + event.getPlayer().getDisplayName());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent event) {
		LotusController lc = new LotusController();
		String message = ChatColor.translateAlternateColorCodes('&', event.getMessage().replace("%", "%%"));
		event.setFormat(event.getPlayer().getDisplayName() + " §7("
				+ lc.getPlayerData(event.getPlayer(), Playerdata.LotusChangeID) + "): " + message);
	}

	private static String getBuildTime(Player player) {
		if (buildTime.containsKey(player)) {
			long seconds = (System.currentTimeMillis() / 1000) - (buildTime.get(player));
			long hours = (seconds % (24 * 3600)) / 3600;
			long minutes = (seconds % 3600) / 60;
			long remainingSeconds = seconds % 60;
			return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
		} else {
			return "§bError!";
		}
	}

	public Team getTeam(Scoreboard scoreboard, String role, ChatColor chatcolor) {
		Team team = scoreboard.registerNewTeam(returnPrefix(role, RankType.TEAM));
		team.setPrefix(returnPrefix(role, RankType.TAB));
		team.setColor(chatcolor);
		team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER); // TBD for removal if issues arise.
		return team;
	}

	private String retGroup(Player player) {
		String group = "";
		UserManager um = Main.luckPerms.getUserManager();
		User user = um.getUser(player.getName());
		group = "§a" + returnPrefix(user.getPrimaryGroup(), RankType.SIDEBOARD);
		return group;
	}

	public static void initRoles() {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM core_ranks");
			ResultSet rs = ps.executeQuery();
			tabHM.clear();
			chatHM.clear();
			roleHM.clear();
			sbHM.clear();
			int count = 0;
			while (rs.next()) {
				count++;
				tabHM.put(rs.getString("ingame_id"), rs.getString("colour") + rs.getString("short"));
				chatHM.put(rs.getString("ingame_id"), rs.getString("colour") + rs.getString("name"));
				roleHM.put(rs.getString("ingame_id"), rs.getString("priority"));
				sbHM.put(rs.getString("ingame_id"), rs.getString("name"));
			}
			Main.logger.info(
					"Downloaded " + count + " roles for the Prefix System. | Source: ScoreboardHandler#initRoles();");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String returnPrefix(String role, RankType type) {
		String toReturn = "";
		if (type == RankType.TAB) {
			if (tabHM.containsKey(role)) {
				toReturn = tabHM.get(role) + " §7» ";
			} else {
				toReturn = "&cDEF";
			}
		} else if (type == RankType.CHAT) {
			if (chatHM.containsKey(role)) {
				toReturn = chatHM.get(role) + " §7» ";
			} else {
				toReturn = "&cDEF";
			}
		} else if (type == RankType.SIDEBOARD) {
			if (sbHM.containsKey(role)) {
				toReturn = sbHM.get(role);
			} else {
				toReturn = "DEF";
			}
		} else if (type == RankType.TEAM) {
			if (roleHM.containsKey(role)) {
				toReturn = roleHM.get(role);
			} else {
				Random r = new Random();
				toReturn = "0" + r.nextInt(0, 250) + "0";
			}
		}
		toReturn = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', toReturn); // transforms & -> §
		toReturn = LotusController.translateHEX(toReturn); // translates HEX Color Codes into Minecraft (Custom Color
															// Codes ability)
		return toReturn;
	}

	public enum RankType {
		TAB,
		SIDEBOARD,
		CHAT,
		TEAM
	}

	public void startScheduler(int delay, int sideboardRefresh, int tabRefresh) {
		// SYNC TASK - ONLY FOR THE SIDEBOARD
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player all : Bukkit.getOnlinePlayers()) {
					setScoreboard(all);
				}
			}
		}.runTaskTimer(Main.main, delay, sideboardRefresh);

		new BukkitRunnable() {
			@Override
			public void run() {
				new ServerRestarter().triggerRestart();
			}
		}.runTaskTimer(Main.main, delay, tabRefresh);

		new BukkitRunnable() {
			@Override
			public void run() {
				LotusController lc = new LotusController();
				for (Player all : Bukkit.getOnlinePlayers()) {
					String timeZone = lc.getPlayerData(all, Playerdata.TimeZone);
					ZoneId zoneId = ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone));
					SimpleDateFormat sdf = new SimpleDateFormat(lc.getPlayerData(all, Playerdata.CustomTimeFormat));
					sdf.setTimeZone(TimeZone.getTimeZone(Objects.requireNonNullElse(zoneId.getId(), "UTC")));
					all.setPlayerListHeaderFooter("§cLotus §aGaming §fCommunity", "§7Server: §a" + lc.getServerName()
							+ "\n§7Time: §a" + sdf.format(new Date()) + "\n§7Ping: §a" + all.getPing());
				}
			}
		}.runTaskTimerAsynchronously(Main.main, delay, tabRefresh);
	}
}