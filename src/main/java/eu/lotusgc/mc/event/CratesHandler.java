package eu.lotusgc.mc.event;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import eu.lotusgc.mc.main.LotusManager;
import eu.lotusgc.mc.misc.LotusController;
import eu.lotusgc.mc.misc.MySQL;

public class CratesHandler implements Listener {

    public static String mainGUITitle = "§d";
    public static String main_crates = "§eCrates";
    public static String main_spinner = "§bSpinner";

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST) {
            Location clickedLocation = event.getClickedBlock().getLocation();
            if(isCratesChest(clickedLocation)) {
                //Open crates GUI
                createMainGUI(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        if(event.getView().getTitle().equalsIgnoreCase(mainGUITitle)){
            event.setCancelled(true);
            if(itemName.equalsIgnoreCase(main_crates)) {
                event.setCancelled(true);
                int keys = getAvailableCrateKeys(player.getUniqueId());
                if(keys >= 1){
                    //Open crates GUI
                }else {
                    player.sendMessage("§cYou don't have any crate keys!");
                    return;
                }
            }else if(itemName.equalsIgnoreCase(main_spinner)) {
                event.setCancelled(true);
                int keys = getAvailableSpinnerKeys(player.getUniqueId());
                if(keys >= 1){
                    //Open spinner GUI
                }else {
                    player.sendMessage("§cYou don't have any spinner keys!");
                    return;
                }
            }
        }
    }

    public static void createMainGUI(Player player){
        Inventory inv = Bukkit.createInventory(null, 3*9, mainGUITitle);
        LotusController lc = new LotusController();
        for(int i = 0; i < 27; i++) {
            if((i % 2) == 0){
                inv.setItem(i, lc.defItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "§0", 1));
            }else {
                inv.setItem(i, lc.defItem(Material.BLUE_STAINED_GLASS_PANE, "§0", 1));
            }
            
        }

        inv.setItem(11, lc.loreItem(Material.AMETHYST_SHARD, 1, main_crates, "§7Available Keys", "§6" + getAvailableCrateKeys(player.getUniqueId())));
        inv.setItem(15, lc.loreItem(Material.LAPIS_LAZULI, 1, main_spinner, "§7Available Keys" , "§6" + getAvailableSpinnerKeys(player.getUniqueId())));

        player.openInventory(inv);
    }

    boolean isCratesChest(Location location) {
        File config = LotusManager.mainConfig;
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(config);
		
		double s_y, s_x, s_z, c_y, c_x, c_z;
        String s_world, c_world;

        s_world = cfg.getString("Crates.World");
        s_x = cfg.getDouble("Crates.X");
        s_y = cfg.getDouble("Crates.Y");
        s_z = cfg.getDouble("Crates.Z");

        c_world = location.getWorld().getName();
        c_x = location.getX();
        c_y = location.getY();
        c_z = location.getZ();
        if(s_world.equalsIgnoreCase(c_world)) {
            if(s_x == c_x && s_y == c_y && s_z == c_z) {
                return true;
            }
        }
        return false;
    }

    static int getAvailableSpinnerKeys(UUID uuid){
		int keys = 0;
		try (PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT type FROM mc_crateskeys WHERE uuid = ? AND used = ?")){
			ps.setString(1, uuid.toString());
			ps.setBoolean(2, false);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				if(rs.getString("type").equalsIgnoreCase("spinner")) {
					keys++;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keys;
	}

	static int getAvailableCrateKeys(UUID uuid) {
		int keys = 0;
		try (PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT type FROM mc_crateskeys WHERE uuid = ? AND used = ?")){
			ps.setString(1, uuid.toString());
			ps.setBoolean(2, false);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				if(rs.getString("type").equalsIgnoreCase("crates")) {
					keys++;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keys;
	}
}