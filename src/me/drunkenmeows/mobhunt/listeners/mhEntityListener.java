package me.drunkenmeows.mobhunt.listeners;

import me.drunkenmeows.mobhunt.MobHunt;
import me.drunkenmeows.mobhunt.mhGame;
import me.drunkenmeows.mobhunt.mhPlayer;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class mhEntityListener implements Listener {
	
	public MobHunt plugin = null;
	
	public mhEntityListener(MobHunt p) {
		this.plugin = p;
	}
	
	//FIXME need to allow TNT at some stage!
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		for(Block b : e.blockList()) {
			if(plugin.insideGameArea(b.getWorld().getName(), b.getLocation().getX(), b.getLocation().getZ())){
				plugin.getServer().broadcastMessage("Explosion Inside game area block protected!");
				e.blockList().clear();
				return;
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e)
	{
		//FIXME change to 2 functions tookDamage() and gaveDamage()? 
		//if interaction is between fGamePlayers
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player p = (Player) e.getEntity();
			Player d =  (Player)e.getDamager();
			mhPlayer mhp = plugin.fPlayerList.get(p.getName());
			mhPlayer mhd = plugin.fPlayerList.get(d.getName());
			
			if(mhp != null) {
				if(mhp.getGame() != null) {
					mhp.getGame().onEntityDamageByEntity(e, true);
				}
			} else if (mhd != null) {
				if(mhd.getGame() != null) {
					mhd.getGame().onEntityDamageByEntity(e, false);
				}
			}
			
		}
	}
	
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent deathevent) {
		
		//get killed mob and player killer
		LivingEntity lm = deathevent.getEntity();
		
		//if mob died of natural causes or dumbassedness.
		if( !(lm.getKiller() instanceof Player) )
		{
			return;
		}

		Player p = lm.getKiller();		
		mhPlayer mhp = plugin.fPlayerList.get(p.getName());
		
		if(mhp != null) {
			mhGame game = mhp.getGame();
			if(game != null)
				game.onEntityDeath(deathevent, p, lm);
		}

	}
}
