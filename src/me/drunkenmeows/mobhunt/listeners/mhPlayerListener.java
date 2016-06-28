package me.drunkenmeows.mobhunt.listeners;

import me.drunkenmeows.mobhunt.MobHunt;
import me.drunkenmeows.mobhunt.mhGame;
import me.drunkenmeows.mobhunt.mhPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class mhPlayerListener implements Listener {

MobHunt plugin = null;
	
	public mhPlayerListener(MobHunt p) {
		this.plugin = p;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		mhPlayer mhp = plugin.fPlayerList.get(p.getName());
		
		if(mhp != null) {
			mhGame game = mhp.getGame();
			if(game != null) {
				game.onPlayerMove(p);
			}	
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		mhPlayer mhp = plugin.fPlayerList.get(p.getName());
		
		if(mhp != null) {
			mhGame game = mhp.getGame();
			if(game != null) {
				game.onRespawn(e);
			}
		}
	}
	
	//FIXME
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		mhPlayer mhp = plugin.fPlayerList.get(p.getName());
		
		if(mhp != null) {
			mhGame game = mhp.getGame();
			if(game != null) {
				game.onPlayerDeath(e);
			}
		}
		return;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		Player p = e.getPlayer();
		mhPlayer mhp = plugin.fPlayerList.get(p.getName());
		
		if(mhp != null) {
			mhGame game = mhp.getGame();
			if(game != null) {
				game.removePlayer(p);
			}
		}
		
		//hunt 
		/*
		if(fPluginInstance.inGame.containsKey(p.getName())) {
			mhHunt h = fPluginInstance.hunts.get(fPluginInstance.inGame.get(p.getName()));
			if(h != null) {
				h.fGamePlayers.remove(p.getName());
				h.worldBroadcast(fPluginInstance.msg.get("playerLeftMsg",p.getName()));
			
				if(h.setting.payHunt && (fPluginInstance.fEconomy != null) && (h.huntstate != huntStates.ACTIVE)) {
					fPluginInstance.fEconomy.depositPlayer(p.getName(), Math.abs(h.setting.fCost));
					h.setting.huntPot = h.setting.huntPot - h.setting.fCost;
				}
			} else { 
				return;
			}
			
		}*/
		/*
		if(fPluginInstance.inGame.containsKey(p.getName())) {
			mhGame a = fPluginInstance.arenas.get(fPluginInstance.inGame.get(p.getName()));
			if(a != null) {
				a.teleportOutPlayer(p);
				a.restoreInventory(p);
				a.fGamePlayers.remove(p.getName());
			} else {
				return;
			}
		}*/
		
		//for all hunts remove player if they leave.
		/*for(mhHunt h : fPluginInstance.hunts.values())
		{
			if(h.fGamePlayers.containsKey(quitEvent.getPlayer().getName()))
			{
				h.fGamePlayers.remove(quitEvent.getPlayer().getName());
				h.worldBroadcast(h.colourise(fPluginInstance.msg.msgPrefix+fPluginInstance.msg.parse(quitEvent.getPlayer().getName(), fPluginInstance.msg.playerLeftMsg)));
				
				//pay player money back for leaving
				if(h.setting.payHunt && (fPluginInstance.fEconomy != null) && (h.huntstate != huntStates.ACTIVE)) {
					fPluginInstance.fEconomy.depositPlayer(quitEvent.getPlayer().getName(), Math.abs(h.setting.fCost));
					h.setting.huntPot = h.setting.huntPot - h.setting.fCost;
				}
			}
		}*/
	}
	
	

}
