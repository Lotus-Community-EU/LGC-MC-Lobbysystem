package eu.lotusgc.mc.main;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	
	public static Main main;
	public static String consoleSend = "§cPlease execute this command in-Game!";
	
	public void onEnable() {
		main = this;
		LotusManager mgr = new LotusManager();
		mgr.preInit();
		mgr.mainInit();
		mgr.postInit();
	}
	
	public void onDisable() {
		main = null;
	}

}
