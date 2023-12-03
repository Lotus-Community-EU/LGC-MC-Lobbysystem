package eu.lotusgc.mc.ext;

import org.bukkit.entity.Player;

public class LotusPlayer{
	
	private Player spigotPlayer;
	
	public LotusPlayer(Player spigotPlayer) {
		this.spigotPlayer = spigotPlayer;
	}
	
	public int getLotusID() {
		@SuppressWarnings("unused")
		String uuid = spigotPlayer.getUniqueId().toString();
		return 0;
	}

}
