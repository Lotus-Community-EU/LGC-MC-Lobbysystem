package eu.lotusgc.mc.main;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import eu.lotusgc.mc.misc.MySQL;

public class LotusManager {
	
	
	//will be loaded as first upon plugin loading!
	public void preInit() {
		long current = System.currentTimeMillis();
		
		//Configs
		File mainFolder = new File("plugins/LotusGaming");
		File mainConfig = new File("plugins/LotusGaming/config.yml");
		File propsConfig = new File("plugins/LotusGaming/propertiesBackup.yml");
		
		if(!mainFolder.exists()) mainFolder.mkdirs();
		if(!mainConfig.exists()) try { mainConfig.createNewFile(); } catch (Exception ex) { };
		if(!propsConfig.exists()) try { propsConfig.createNewFile(); } catch (Exception ex) { };
		
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(mainConfig);
		cfg.addDefault("MySQL.Host", "127.0.0.1");
		cfg.addDefault("MySQL.Port", "3306");
		cfg.addDefault("MySQL.Database", "TheDataBaseTM");
		cfg.addDefault("MySQL.Username", "user");
		cfg.addDefault("MySQL.Password", "pass");
		cfg.options().copyDefaults(true);
		
		try { cfg.save(mainConfig); } catch (Exception ex) { }
		
		if(!cfg.getString("MySQL.Password").equalsIgnoreCase("pass")) {
			MySQL.connect(cfg.getString("MySQL.Host"), cfg.getString("MySQL.Port"), cfg.getString("MySQL.Database"), cfg.getString("MySQL.Username"), cfg.getString("MySQL.Password"));
		}
		
		Bukkit.getConsoleSender().sendMessage("§aPre-Initialisation took §6" + (System.currentTimeMillis() - current) + "§ams");
	}
	
	//Commands and Event registers will be thrown in here!
	public void mainInit() {
		long current = System.currentTimeMillis();
		
		
		Bukkit.getConsoleSender().sendMessage("§aMain-Initialisation took §6" + (System.currentTimeMillis() - current) + "§ams");
	}
	
	//will loaded as last - for schedulers and misc stuff
	public void postInit() {
		long current = System.currentTimeMillis();
		
		
		Bukkit.getConsoleSender().sendMessage("§aPost-Initialisation took §6" + (System.currentTimeMillis() - current) + "§ams");
	}

}