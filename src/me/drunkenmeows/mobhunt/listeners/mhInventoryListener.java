package me.drunkenmeows.mobhunt.listeners;

import me.drunkenmeows.mobhunt.MobHunt;
import me.drunkenmeows.mobhunt.mhGame;
import me.drunkenmeows.mobhunt.mhPlayer;

import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class mhInventoryListener implements Listener {

	private MobHunt plugin = null;
	
	public mhInventoryListener(MobHunt p) {
		this.plugin = p;
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e){
		if(!(e.getInventory().getHolder() instanceof Chest))
			return;
		
		Player p = (Player) e.getPlayer();
		mhPlayer mhp = plugin.fPlayerList.get(p.getName());
		
		if(mhp != null) {
			mhGame game = mhp.getGame();
			if(game != null) {
				game.onInventoryOpen(p, (Chest)e.getInventory().getHolder());
			}
		}
	}
}


