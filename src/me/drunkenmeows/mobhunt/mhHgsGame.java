package me.drunkenmeows.mobhunt;

//import java.io.File;
//import java.util.HashMap;
//import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Location;
//import org.bukkit.Material;
import org.bukkit.block.Chest;
//import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
//import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class mhHgsGame extends mhGame {

	private BukkitTask showplayertask;

	protected mhHgsGame(MobHunt p, String pGameName, String world) {
		super(p, pGameName, world);

		this.fConfig = loadConfig( world+"-"+pGameName+"-"+ fGameType +".yml" );
		if( fConfig != null)
		{
			this.loadSettings();
		}

		this.maintask();
	}

	public void maintask()
	{
		fGameTask = fPluginInstance.getServer().getScheduler().runTaskTimer(fPluginInstance, new Runnable(){
			public void run(){
				mainloop();
			}
		}, 0, 20L);
	}

	protected void mainloop() {
		this.startGame();
		this.endGame();
	}

	public void showPlayerPositionTask()
	{
		showplayertask = fPluginInstance.getServer().getScheduler().runTaskTimer(fPluginInstance, new Runnable(){
			public void run(){
				showPlayerPosition();
			}
		}, 0, 20L*30L);
	}

	private void showPlayerPosition() {
		if(fGamePlayers.size() <= 5 && fGamePlayers.size() >= 2) {
			for(String s : fGamePlayers.keySet()) {
				Player p = fPluginInstance.getServer().getPlayer(s);
				p.getWorld().strikeLightningEffect(p.getLocation());
			}
		}
		//cancel when game isn't running
		if(this.fGameState != mhGameState.ACTIVE) {
			this.showplayertask.cancel();
		}
	}

	public void loadSettings() {
		this.fRequiredPlayers = fConfig.getInt("Arena.fRequiredPlayers", 1);
		this.fPlayerEndLimit = fConfig.getInt("Arena.endLimit", 0);
		this.fStartupTime = this.fConfig.getInt("Arena.StartupTime", 20);
		this.fWarmupTime = this.fConfig.getInt("Arena.WarmupTime", 20);
		this.fTimelimit = this.fConfig.getInt("Arena.TimeLimit", 10)*60;
		this.fFragLimit = this.fConfig.getInt("Arena.KillLimit", 50);
		this.fVoteLimit = this.fConfig.getInt("Arena.VoteLimit", 60);
		this.fDisableCommands = this.fConfig.getBoolean("Arena.DiableCommands", true);
		this.fCommandWhitelist = this.fConfig.getStringList("Arena.CommandWhiteList");
		this.fCost = this.fConfig.getDouble("Arena.Cost", 0);
		this.fTallyPot = this.fConfig.getBoolean("Arena.TallyPot", true);
		this.fPot = this.fConfig.getInt("Arena.Pot",100);
		this.fKillReward = this.fConfig.getInt("Arena.Killer", 10);
		this.fDeathPenalty = fConfig.getInt("Arena.DeathPenalty", 10);
		this.fHideNameTags = fConfig.getBoolean("Arena.HidePlayerNames", true);

		this.fGameType = fConfig.getString("Arena.GameType", "ffa");
		this.fDenyBlockBreak = fConfig.getBoolean("Arena.DenyBlockBreak");
		this.fDenyBlockPlace = fConfig.getBoolean("Arena.DenyBlockPlace");
		this.fUseChests = fConfig.getBoolean("Arena.fUseChests", true);
		this.fAllowRandomChests = fConfig.getBoolean("Arena.fAllowRandomChests", true);

		this.fBlockPlaceWhitelist = fConfig.getStringList("Arena.blockWhitelist.place");
		this.fBlockBreakWhiteList = fConfig.getStringList("Arena.blockWhitelist.break");

		//get area
		this.Xmax = fConfig.getInt("Arena.Area.xmax");
		this.Xmin = fConfig.getInt("Arena.Area.xmin");
		this.Zmax = fConfig.getInt("Arena.Area.zmax");
		this.Zmin = fConfig.getInt("Arena.Area.zmin");
		//check extents
		this.checkArea();

		//get fSpawnPoints
		for(String loc: fConfig.getStringList("Arena.Spawns")) {
			String[] s = loc.split(",");
			Location l = new Location(fPluginInstance.getServer().getWorld(this.fWorld), Double.valueOf(s[0]),Double.valueOf(s[1]),Double.valueOf(s[2]));
			this.fSpawnPoints.add(l);
		}

		this.fPlayerLimit = this.fSpawnPoints.size();

	}

	public void startGame() {
		if(fGamePlayers.size() >= fRequiredPlayers && this.isWaiting()) {
			this.fGameState = mhGameState.STARTING;
		}

		if(this.isStarting()) {
			if(fGameCountDown != 0) {
				//startup time message
				fPluginInstance.getServer().broadcastMessage("["+ fGameName +"] warmup starting in..."+ fGameCountDown);
				fGameCountDown--;
			} else {
				this.fGameState = mhGameState.WARMUP;
				fGameCountDown = fWarmupTime;
				fPluginInstance.getServer().broadcastMessage("["+ fGameName +"] teleporting fGamePlayers...");
				//teleport -> save and cler inventory for ALL arena fGamePlayers
				this.teleportPlayersIn();
				fPluginInstance.getServer().broadcastMessage("["+ fGameName +"] warmup STARTED!");
			}
		}

		if(this.isWarmup()) {
			if(fGameCountDown != 0) {
				//warmup time message
				fPluginInstance.getServer().broadcastMessage("["+ fGameName +"] game starting in..."+ fGameCountDown);
				fGameCountDown--;
			} else {
				fPluginInstance.getServer().broadcastMessage("["+ fGameName +"] game STARTED!");
				this.fGameState = mhGameState.ACTIVE;
				//showPlayerPositionTask();
				fGameCountDown = fStartupTime;
			}
		}
	}

	public void endGame() {
		if(this.isActive()) {
			//FIXME should be 1
			fPluginInstance.getServer().broadcastMessage("fGamePlayers in game: "+ fGamePlayers.size());
			if(this.fGamePlayers.size() <= 0) {
				this.fGameState = mhGameState.ENDING;
				for(String name : fGamePlayers.keySet()) {
					Player p = fPluginInstance.getServer().getPlayer(name);
					this.teleportOutPlayer(p);
					this.restoreInventory(p);
					this.rewardPlayer("Arena", p);
					this.removePlayer(p);
				}

				this.fGameState = mhGameState.REGENERATING;
				this.resetArenaBlocks();
				this.fGamePlayers.clear();
				//this.deadplayers.clear();
				//this.resetTimers();
				this.clearDrops();
				this.fFilledChests.clear();
				this.fInventoryHolder.clear();
				/*for(Location l: fFilledChests) {
					Chest chest = (Chest) fPluginInstance.getServer().getWorld(this.world).getBlockAt(l;
					chest.getInventory().clear();
				}*/
				fPluginInstance.getServer().broadcastMessage("["+this.fGameName +"] game ENDED!");
				//resetArena();
				return;
			}
		}
	}

	@Override
	public void onPlayerDeath(PlayerDeathEvent e)
	{
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHgsGame.onPlayerDeath Fired!");

		if(this.isActive()) {
			Player victim = e.getEntity();
			Player killer = null;

			if(victim.getKiller() != null) {

				killer = victim.getKiller();
				fGamePlayers.get(killer.getName()).updateScore( 10 );
			}

			fGamePlayers.get(victim.getName()).updateScore( -10  );

			victim.setHealth(20.0D);
			victim.setFoodLevel(20);
			//keep EXP
			e.setNewExp(e.getDroppedExp());
			e.setKeepLevel(true);
			//clear dropped EXP
			e.setDroppedExp(0);

			this.teleportOutPlayer(victim);
			this.restoreInventory(victim);

			this.removePlayer(victim);
		}
	}

	public void onInventoryOpen (Player p, Chest chest) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHgsGame.onInventoryOpen Fired!");

		if((this.isWaiting() || this.isActive()) && !this.isOutsideArena(p.getWorld().getName(), p.getLocation().getX(), p.getLocation().getY())) {
			if(!this.fFilledChests.contains(chest.getLocation())) {
				if(fPluginInstance.fDebugging)
					fPluginInstance.getServer().broadcastMessage("mhHgaGame.onInventoryOpen Filling chest!");
				this.fFilledChests.add(chest.getLocation());
				chest.getInventory().clear();
				this.getRandomChestItems(chest);
			}
		}

	}

	public void onPlayerDeath(Player victim, Player killer) {
		//set theScore values
		//TODO theScore values.


		//arena.rewardPlayer(p);
		//remove player from fGamePlayers and add to dead plays
		//this.fGamePlayers.remove(victim.getName());
	}

	public void onRespawn(PlayerRespawnEvent e) {
		/*Player victim = e.getPlayer();

		this.teleportOutPlayer(victim);
		this.restoreInventory(victim);*/
		//return this.fGamePlayers.get(p.getName()).thePreviousLocation;
	}

	public void teleportPlayersIn()  {
		int i = 0;
		for(String name: fGamePlayers.keySet())  {
			Player p = fPluginInstance.getServer().getPlayer(name);

			Location tp = this.fSpawnPoints.get(i);
			tp.setPitch(p.getLocation().getPitch());
			tp.setYaw(p.getLocation().getBlockX());
			tp.setWorld(fPluginInstance.getServer().getWorld(this.fWorld));
			tp.add(0.5, 0.5, 0.5);

			this.fGamePlayers.get(p.getName()).setPreviousLocation( new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), p.getLocation().getYaw(), p.getLocation().getPitch()) );
			fPluginInstance.getServer().broadcastMessage("W:"+p.getWorld().getName()+" ["+p.getLocation().getX()+","+p.getLocation().getY()+","+p.getLocation().getZ()+"] Y:"+p.getLocation().getYaw()+" P:"+p.getLocation().getPitch());

			p.teleport(tp);
			//store and clear inventory
			this.storeInventory(p);
			//fGamePlayers aren't damaged for 5 secs
			p.setNoDamageTicks(this.fWarmupTime *20);
			//increment spawnpoints index
			i++;
		}
	}


	@Override
	public void onMobSpawn(CreatureSpawnEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEntityDeath(EntityDeathEvent deathevent, Player p, LivingEntity lm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameInfo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameInfo(Player p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setupGame(Economy econ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void status(Player p) {
		// TODO Auto-generated method stub

	}

	@Override
	public GameType getGameType()
	{
		return GameType.HUNGER;
	}

	@Override
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e, boolean victim) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHgsArena.onEntityDamageByEntity Fired!");

		if(this.isActive()) {
			//are both entities fGamePlayers?
			if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
				Player p = (Player) e.getEntity();
				Player d = (Player) e.getDamager();
				//is attacker is in this game?
				if(this.fGamePlayers.containsKey(d.getName())) {
					if(e.getDamage() >= p.getHealth()) {
						e.setCancelled(true);
						p.setHealth(20.0D);
						p.setFoodLevel(20);
						this.teleportOutPlayer(p);
						this.restoreInventory(p);
						this.removePlayer(p);
					}
				}
			}
		}
	}

	/*public void removeOutsidePlayer(Player p) {
		if(this.isActive() && this.isOutsideArena(p.getWorld().getName(), p.getLocation().getX(), p.getLocation().getZ())){
			this.removePlayer(p);
		}

	}*/

	@Override
	public void onPlayerMove(Player p) {
		this.removeOutsidePlayer(p);

	}



}
