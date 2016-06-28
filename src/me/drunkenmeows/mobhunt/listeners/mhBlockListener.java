package me.drunkenmeows.mobhunt.listeners;

import me.drunkenmeows.mobhunt.MobHunt;
import me.drunkenmeows.mobhunt.mhGame;
import me.drunkenmeows.mobhunt.mhPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class mhBlockListener implements Listener {
	
	
	private MobHunt plugin;
	
	public mhBlockListener(MobHunt p) {
		this.plugin = p;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		mhPlayer mhp = plugin.fPlayerList.get(p.getName());
		if(mhp != null) {
			mhGame game = mhp.getGame();
			if(game != null) {
				//FIXME remove if()
				if(!game.onBlockBreak(e.getBlock(), p))
					e.setCancelled(true);
			}
		//FIXME for debugging
		} else if(!p.hasPermission("mobhunt.admin") && plugin.insideGameArea(e.getBlock().getWorld().getName(), e.getBlock().getLocation().getX(), e.getBlock().getLocation().getZ())){
			//p.sendMessage("Inside game area block protected!");
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		mhPlayer mhp = plugin.fPlayerList.get(p.getName());
		if(mhp != null) {
			mhGame game = mhp.getGame();
			if(game != null) {
				if(!game.onBlockPlace(e.getBlock(), p))
					//FIXME check this...
					e.setCancelled(true);
			}
			//FIXME for debugging
		} else if(!p.hasPermission("mobhunt.admin") && plugin.insideGameArea(e.getBlock().getWorld().getName(), e.getBlock().getLocation().getX(), e.getBlock().getLocation().getZ())){
			//p.sendMessage("Inside game area block protected!");
			e.setCancelled(true);
		}
	}
}
