package me.drunkenmeows.mobhunt.listeners;

import me.drunkenmeows.mobhunt.MobHunt;
import me.drunkenmeows.mobhunt.mhGame;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class mhMobListener implements Listener {

	private MobHunt plugin = null;
	
	public mhMobListener(MobHunt p) {
		this.plugin = p;
	}
	
	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent event) {
		World w = event.getEntity().getWorld();
		//mhHunt hunt = fPluginInstance.hunts.get(w.getName());
		mhGame game = plugin.fGames.get(w.getName());
		
		if(game == null)
			return;
		
		game.onMobSpawn(event);

	}
}
