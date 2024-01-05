package eu.lotusgc.mc.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import eu.lotusgc.mc.ext.LotusController;
import eu.lotusgc.mc.main.Main;
import eu.lotusgc.mc.misc.MySQL;
import eu.lotusgc.mc.misc.Prefix;
import eu.lotusgc.mc.misc.TextCryptor;

public class PasswordSystem implements Listener, CommandExecutor{

	/*
	 * This class is going to be command and event in one.
	 * Users can enable/disable passwords, staffs are required to have a password.
	 * Prevents hacker to do damages, as no bans, mutes or any other bad stuff can be done until actually logged in.
	 */
	
	//if boolean is false, then no administrative stuff can be done (security reasons)
	//non staffs can use a password, but are not forced to - see it as 2FA.
	private static HashMap<UUID, Boolean> passList = new HashMap<>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			LotusController lc = new LotusController();
			// Command Usage: /unlock <Password>
			if(cmd.getName().equalsIgnoreCase("unlock")) {
				if(args.length == 1) {
					String givenPassword = args[0];
					if(hasPassword(player.getUniqueId())) {
						String passSalt = getPasswordOption(player.getUniqueId(), PasswordOption.SALT);
						String savedPasswordEncrypted = getPasswordOption(player.getUniqueId(), PasswordOption.PASSWORD);
						String savedPasswordDecrypted = TextCryptor.decrypt(savedPasswordEncrypted, passSalt.toCharArray());
						if(givenPassword.equals(savedPasswordDecrypted)) {
							if(passList.containsKey(player.getUniqueId())) {
								passList.remove(player.getUniqueId());
								passList.put(player.getUniqueId(), true);
								lc.sendMessageReady(player, "command.unlock.success");
							}else {
								//this should never happen, however to ensure functionality, I add it nontheless.
								Bukkit.getConsoleSender().sendMessage("DEBUG | Password System detected user which logged in but were not in the Hashmap!");
								passList.put(player.getUniqueId(), true);
							}
						}else {
							lc.sendMessageReady(player, "command.unlock.failed");
						}
					}else {
						lc.sendMessageReady(player, "command.unlock.noPasswordSet");
					}
				}else {
					player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "global.args") + "§7/unlock <Password>");
				}
			//Command Usage: /pwmanager <set|remove> [Password]
			//If set, a password must be entered everytime you join the network
			}else if(cmd.getName().equalsIgnoreCase("pwmanager")) {
				if(args.length == 1) {
					//remove
					if(args[0].equalsIgnoreCase("remove")) {
						
					}else {
						
					}
				}else if(args.length == 2) {
					//set & remove
					if(args[0].equalsIgnoreCase("remove")) {
						
					}else if(args[0].equalsIgnoreCase("set")){
						
					}
				}else {
					//wrong argumentation info
				}
			}
			
		}else {
			sender.sendMessage(Main.consoleSend);
		}
		return false;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		boolean isStaff = isStaff(player.getUniqueId());
		LotusController lc = new LotusController();
		if(hasPassword(player.getUniqueId())) {
			lc.sendMessageReady(player, "event.join.passwordinfo.user");
		}
		if(isStaff) {
			lc.sendMessageReady(player, "event.join.passwordinfo.staff");
		}
		passList.put(player.getUniqueId(), false);
	}
	
	//determines whether User is staff or not - if true, that account has staff permissions.
	private boolean isStaff(UUID uuid) {
		boolean isStaff = false;
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT isStaff FROM mc_users WHERE mcuuid = ?");
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				isStaff = rs.getBoolean("isStaff");
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isStaff;
	}
	
	//Get the SALT or encrypted Password from DB
	private String getPasswordOption(UUID uuid, PasswordOption option) {
		String output = "";
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT passPin,passSalt FROM mc_users WHERE mcuuid = ?");
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				if(option == PasswordOption.PASSWORD) {
					output = rs.getString("passPin");
				}else if (option == PasswordOption.SALT) {
					output = rs.getString("passSalt");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	//Return false, if the account has not a password but returns true if otherwise
	private boolean hasPassword(UUID uuid) {
		boolean password = false;
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT passPin FROM mc_users WHERE mcuuid = ?");
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				if(!rs.getString("passPin").equalsIgnoreCase("none")) {
					password = true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return password;
	}
	
	
	//
	private void updatePassword(UUID uuid, String password, String salt) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE mc_users SET passPin = ?, passSalt = ? WHERE mcuuid = ?");
			ps.setString(1, password);
			ps.setString(2, salt);
			ps.setString(3, uuid.toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//This method must be used in order to fulfill the system.
	//IF the UUID exists, it's status will be returned, if else it will be true (in case no password is set as example)
	public static boolean isLoggedIn(UUID uuid) {
		if(passList.containsKey(uuid)) {
			return passList.get(uuid);
		}else {
			return true;
		}
	}
	
	private enum PasswordOption {
		PASSWORD,
		SALT;
	}
	
}