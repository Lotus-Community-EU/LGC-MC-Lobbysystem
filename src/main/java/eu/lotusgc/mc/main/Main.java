package eu.lotusgc.mc.main;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import eu.lotusgc.mc.misc.MySQL;
import eu.lotusgc.mc.misc.SyncServerdata;
import net.luckperms.api.LuckPerms;

public class Main extends JavaPlugin{
	
	public static Main main;
	public static String consoleSend = "§cPlease execute this command in-Game!";
	public static Logger logger;
	public static LuckPerms luckPerms;
	
	public void onEnable() {
		main = this;
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.setLevel(Level.ALL);
		LotusManager mgr = new LotusManager();
		mgr.preInit();
		mgr.mainInit();
		mgr.postInit();
		SyncServerdata.setOnlineStatus(true);
	}
	
	public void onDisable() {
		main = null;
		SyncServerdata.setOnlineStatus(false);
		MySQL.disconnect();
	}

}
