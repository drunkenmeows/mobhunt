package me.drunkenmeows.mobhunt;

import java.util.*;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class mhHuntGame extends mhGame {
	
	public long lastTime = 0;
	public long lastTimeStatus = 0;
	public long wtime;
	
	public ArrayList<String> theHuntMobs = new ArrayList<String>();
	public ArrayList<Integer> theHuntThresholds = new ArrayList<Integer>();
	
	public int skipDays;
	public int announcerDelay;	
	private int startTime;
	private int endTime;
	
	public float enchantmentModifier;
	public float bowModifier;
	
	public boolean allowLate;
	public boolean rewardRunnersup;
	public boolean randomHunts;
	public boolean useArea;
	public boolean autoTeleport;
	private boolean payHunt;
	private boolean allowPVP;
	
	public Location areaSpawn;

	protected mhHuntGame(MobHunt pPlugin, String pGameName, String pWorldName) {
		super(pPlugin, pGameName, pWorldName);

		this.fConfig = this.loadConfig(pWorldName + "-" +pGameName+ "-"  +getGameType().getSimpleName()+ ".yml");
		if( fConfig != null)
		{
			if( fConfig.contains( "Hunt" ) )
			{
				this.loadSettings();
			}
			else
			{
				fPluginInstance.fLogger.warning( "World:" + pWorldName + " Hunt:" + pGameName + " does not have any Hunt settings defined." );
			}
		}

		this.fScoreboard = new mhScoreboard(pPlugin,pWorldName,"[H]",":","Leader Board");
		setupGame(fPluginInstance.fEconomy);
		announcetask();

		this.maintask();
	}
	
	@Override
	public void loadSettings() {
		this.fRequiredPlayers = fConfig.getInt( "Hunt.MinimumHunters",3);
		this.skipDays = fConfig.getInt( "Hunt.SkipDays",0);
		this.fDeathPenalty = fConfig.getInt( "Hunt.DeathPenalty", 25);

		List<Integer> lAreaSpawn = fConfig.getIntegerList( "Hunt.AreaSpawn");
		if(!lAreaSpawn.isEmpty() && lAreaSpawn.size() > 2)
		{
			this.areaSpawn = new Location( fPluginInstance.getServer().getWorld( this.fWorld),
					lAreaSpawn.get( 0 ),
					lAreaSpawn.get( 1 ),
					lAreaSpawn.get( 2 ) );
		}
		
		this.fCost = fConfig.getDouble( "Hunt.Cost", 10.0);
	
		this.startTime = fConfig.getInt( "Hunt.StartTime", 14000)-200;
		this.endTime = fConfig.getInt( "Hunt.EndTime",24000)-200;
		this.announcerDelay = fConfig.getInt( "Hunt.AnnounceDelay",240)*20;
		
		this.allowLate = fConfig.getBoolean( "Hunt.AllowLateHunters", true);
		this.randomHunts = fConfig.getBoolean( "Hunt.RandomHunts", true);
		this.rewardRunnersup = fConfig.getBoolean("Hunt.RewardRunnersUp", true);
		this.autoTeleport = fConfig.getBoolean( "Hunt.AutoTeleport", true);
		
		this.fDenyBlockBreak = fConfig.getBoolean( "Hunt.DenyBlockBreak");
		this.fDenyBlockPlace = fConfig.getBoolean( "Hunt.DenyBlockPlace");
		
		this.fBlockPlaceWhitelist = fConfig.getStringList( "Hunt.blockWhitelist.place");
		this.fBlockBreakWhiteList = fConfig.getStringList( "Hunt.blockWhitelist.break");

		this.fGameCountDown = 5;
	}

	@Override
	public void maintask() {
		this.fGameTask = fPluginInstance.getServer().getScheduler().runTaskTimer(fPluginInstance, new Runnable(){
			public void run(){
				mainloop();
			}		
		}, 0, 20L);
	}
	
	public void announcetask() {
		this.fTempTask = fPluginInstance.getServer().getScheduler().runTaskTimer(fPluginInstance, new Runnable(){
			public void run(){
				announceloop();
			}		
		}, 10*20, fConfig.getLong( "Hunt.AnnounceDelay", 240)*20);
	}
		
	@Override
	public void status(Player p) {
		int req =  fRequiredPlayers - fGamePlayers.size();
		if(req < 0)
			req = 0;
		p.sendMessage(mhMessages.get().get("huntStatus", this.fWorld));
		
		switch(this.fGameState)	{
			case DISABLED:
				p.sendMessage(mhMessages.get().get("huntDisabled"));
				p.sendMessage(mhMessages.get().get("playersWaiting", this.fGamePlayers.size()));
				p.sendMessage(mhMessages.get().get("playersRequired", req));
				break;
			case WAITING:
				p.sendMessage(mhMessages.get().get("huntWaiting"));
				p.sendMessage(mhMessages.get().get("playersWaiting", this.fGamePlayers.size()));
				p.sendMessage(mhMessages.get().get("playersRequired", req));
				break;
			case ACTIVE:
				p.sendMessage(mhMessages.get().get("huntActive"));
				p.sendMessage(mhMessages.get().get("playersWaiting", this.fGamePlayers.size()));
				p.sendMessage(mhMessages.get().get("playersRequired",req));
				break;
			case ENDING:
				break;
			case REGENERATING:
				break;
			case RESETTING:
				break;
			case STARTING:
				break;
			case WARMUP:
				break;
			default:
				break;	
		}
		
		if(this.skipDays > 0) {
			p.sendMessage(mhMessages.get().get("skipdayMsg", this.skipDays));
		}
		
	}

	@Override
	public GameType getGameType()
	{
		return GameType.HUNT;
	}

	public void onEntityDeath(EntityDeathEvent deathevent, Player p, LivingEntity lm) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHuntGame.onMobKill Fired!");
			
		//if mob is a spawner is killed
		if(fPluginInstance.fExcludedMobs.contains(deathevent.getEntity().getEntityId()))
		{	
			//remove it from the hot list.
			if(!this.useArea) {
				fPluginInstance.fExcludedMobs.remove((Integer)deathevent.getEntity().getEntityId());
				if(this.fConfig.getBoolean( "Hunt.DenySpawnerMobs",true) && (this.isActive()))
					p.sendMessage(mhMessages.get().get("denySpawnerMobs"));
				if(this.fConfig.getBoolean( "Hunt.OutsideMobsOnly",true) && (this.isActive()))
					p.sendMessage(mhMessages.get().get("outsideMobsOnly"));
				return;
			}
		}
		
		if( this.isActive() )
		{
			//theScore modifier for bows and enchants.
			float scoremodifier = 1;
			//check if player is inside hunter area IF hunt area is used.
			if(this.useArea && this.isOutsideArena(p.getWorld().getName(), p.getLocation().getX(), p.getLocation().getZ()))	{
				p.sendMessage(mhMessages.get().get("outsideHunt"));
				p.sendMessage(mhMessages.get().get("huntSpawn"));
				//player not in hunt area, do nothing
				return;
			}
			
			//that the worlds are the same.
			if(/*!this.fGamePlayers.containsKey(p.getName()) ||*/ (!(p.getWorld().getName().equals(this.fWorld))))	{
				p.sendMessage("Wrong world! The hunt is Active in "+this.fWorld +" world");
				return;
			}
			
			//check if enchantments are allowed
			if(p.getItemInHand().getEnchantments().size() > 0 ) {
				scoremodifier = this.enchantmentModifier;
			}

			//Check if bows are allowed
			if(lm.getLastDamageCause() instanceof EntityDamageByEntityEvent)
			{
				EntityDamageByEntityEvent dmgEvent = (EntityDamageByEntityEvent)lm.getLastDamageCause();
				if(dmgEvent.getDamager() instanceof Arrow) {
					scoremodifier = this.bowModifier;
				}
			} 
						
			//FIXME move to another place? if player dies give penalty points!
			if((deathevent.getEntity() instanceof Player) && (this.fDeathPenalty > 0))
			{
				Player lmp = (Player)deathevent.getEntity();
				String victim = lmp.getName();
				//if the player is on the list.
				if(this.fGamePlayers.containsKey(victim))	{
					int lScore = this.fGamePlayers.get(victim).getScore() - this.fDeathPenalty;
					this.fGamePlayers.get(victim).setScore( lScore );
					this.fScoreboard.updateScore(lmp, lScore);
					lmp.sendMessage(mhMessages.get().get("deathMsg",this.fDeathPenalty));
				}	
			}
			
			//if killer is not in survival mode don't count
			if(!p.getGameMode().toString().equalsIgnoreCase("survival")){
				p.sendMessage(mhMessages.get().get("gamemodeMsg"));
				return;
			}
			
			int points = 0;	
			
			if(lm instanceof Blaze) {
				points = getPoints("Blaze", scoremodifier, p);
			} else if(lm instanceof CaveSpider) {
				points = getPoints("CaveSpider", scoremodifier, p);
			} else if(lm instanceof Creeper) {
				points = getPoints("Creeper", scoremodifier, p);
			} else if(lm instanceof Enderman) {
				points = getPoints("Enderman", scoremodifier, p);
			} else if(lm instanceof EnderDragon) {
				points = getPoints("EnderDragon", scoremodifier, p);
			} else if(lm instanceof Ghast) {
				points = getPoints("Ghast", scoremodifier, p);
			} else if(lm instanceof Giant) {
				points = getPoints("Giant", scoremodifier, p);
			} else if(lm instanceof MagmaCube) {
				points = getPoints("MagmaCube", scoremodifier, p);
			} else if(lm instanceof PigZombie) {
				points = getPoints("PigZombie", scoremodifier, p);
			} else if(lm instanceof Silverfish) {
				points = getPoints("Silverfish", scoremodifier, p);
			} else if(lm instanceof Skeleton) {
				if(((Skeleton)lm).getSkeletonType().toString().equalsIgnoreCase("normal")) {
					points = getPoints("Skeleton", scoremodifier, p);
				} else {
					points = getPoints("WitherSkeleton", scoremodifier, p);
				}
			} else if(lm instanceof Slime) {
				points = getPoints("Slime", scoremodifier, p);
			} else if(lm instanceof Spider) {
				points = getPoints("Spider", scoremodifier, p);
			} else if(lm instanceof Witch) {
				points = getPoints("Witch", scoremodifier, p);
			} else if(lm instanceof Wither) {
				points = getPoints("Wither", scoremodifier, p);
			} else if(lm instanceof Zombie) {
				points = getPoints("Zombie", scoremodifier, p);
			} else if(lm instanceof Player) {
				points = getPoints("Player", scoremodifier, p);
			} else {
				points = 0;
			}

			if(points == 0)	{
				return;
			}
			//update points based on modifier
			//points = Math.round(points * scoremodifier);
			//increase player theScore
			int acumPoints = this.fGamePlayers.get(p.getName()).getScore() + points;
			this.fGamePlayers.get(p.getName()).setScore( acumPoints  );
			//updater scoreboars
			this.fScoreboard.updateScore(p, acumPoints);

			//update kill count
			if(this.fGamePlayers.containsKey(p.getName())) {
				mhPlayer lMhPlayer = fGamePlayers.get( p.getName() );
				lMhPlayer.incrementKills();

			}
		}
	}

	private int getPoints(String mob, float scoremodifier, Player p) {
		if(this.theHuntMobs.contains(mob.toLowerCase()) || this.theHuntMobs.contains("all")) {
			p.sendMessage(mhMessages.get().get("huntKill", mob, Math.round(this.fConfig.getInt( "Hunt.Points."+mob)* scoremodifier)));
			return Math.round(this.fConfig.getInt( "Hunt.Points."+mob) * scoremodifier);
		} else {
			p.sendMessage(mhMessages.get().get("notinHunt", mob));
			p.sendMessage(mhMessages.get().get("mobs", this.formatHuntString( theHuntMobs.toString())));
			return 0;
		}
	}
	
	//setup random hunt
	public void setupGame(Economy econ) {
		int hunt = 0;
		//reset player scoreboards
		fScoreboard.resetPlayers();
		//if fGamePlayers are wait in a pay hunt give their money back
		//this.refundPlayers();
		//will this hunt skip a day?
		setupSkipDays();		
		//get random hunt
		if(this.randomHunts)
		{
			int huntsize = fConfig.getMapList( "Hunt.Hunts").size();
			fPluginInstance.fLogger.info( "huntsize: " + huntsize );
			hunt = new Random().nextInt( huntsize );
		}	
		//setup hunt thresholds
		this.setupHuntSettings(hunt);
		//setup hunt mobs
//		this.setupHuntMobs(hunt);
		//get enchantment theScore modifier
		this.setEnchantmentModifier();
		//get bow theScore modifier
		this.setBowModifier();
		
		//remove fGamePlayers from game
		Set<String> hunters = new HashSet<String>();
		hunters.addAll(this.fGamePlayers.keySet());
		for(String hunter: hunters) {
			Player p = fPluginInstance.getServer().getPlayer(hunter);
			this.removePlayer(p);
		}
		
		//setup pay hunt
		this.setupPayHunt(econ);
		//setup area
		this.setupUseArea();
		//setup pvp
		this.setupAllowPVP();		
		//clear hunters list
		this.fGamePlayers.clear();
	}
	//is PVP allowed?
	private void setupAllowPVP() {
		this.allowPVP = (new Random().nextInt(99)+1) <= fConfig.getInt( "Hunt.PvpChance", 10);
		
		if(!this.allowPVP) {
			this.allowPVP = (theHuntMobs.contains("pvp") || theHuntMobs.contains("player"));
		}
	}
	//should this hunt use Area?
	private void setupUseArea() {
		
		this.fUseArea = false;
		
		List<String> hunts = fConfig.getStringList( "Hunt.Hunts");
		for(String hunt:hunts){
			if(hunt.toLowerCase().contains("area")) {
				this.fUseArea = true;
			}
		}
		
		this.Xmax = fConfig.getInt( "Hunt.Area.Xmax",10);
		this.Zmax = fConfig.getInt( "Hunt.Area.Zmax",10);
		this.Xmin = fConfig.getInt( "Hunt.Area.Xmin",-10);
		this.Zmin = fConfig.getInt( "Hunt.Area.Zmin",-10);
		
		//fPluginInstance.fLogger.info("X:"+this.Xmax+" x:"+this.Xmin+" Z:"+this.Zmax+" z:"+this.Zmin);
		
		this.useArea = fConfig.getBoolean( "Hunt.useArea", false);
		
		if(!this.useArea)
			this.useArea = theHuntMobs.contains("area");
		
		if(this.useArea)
			fPluginInstance.fLogger.info(this.fGameName + " will use area!");
	}
	//is this hunt a pay hunt?
	private void setupPayHunt(Economy econ) {
		
		this.fPot = 0.0;
		
		if(fPluginInstance.fEconomy != null) {
			if((new Random().nextInt(99)+1) <= fConfig.getInt( "Hunt.PayChance", 25))
				this.payHunt = true;
			else
				this.payHunt = false;
		}
		
		if(!this.payHunt && econ != null)
			this.payHunt = theHuntMobs.contains("pay");
	}

	public void setupHuntSettings( int pHuntChoice)
	{
		Map<?,?> lHunt = fConfig.getMapList( "Hunt.Hunts").get(pHuntChoice);
		fPluginInstance.fLogger.info( "Random Hunt [" + ( pHuntChoice + 1 ) + "] loaded" + lHunt.toString() );

		List< Map<?,?> > lSettings = ( List< Map<?,?> > ) lHunt.get(pHuntChoice+1);

		setupHuntMobs( ( List< String > ) lSettings.get( MOBS ).get( "mobs" ) );
		setupHuntThresholds( ( List< Integer > ) lSettings.get( THRESHOLDS ).get("thresholds") );
	}
	
	public void setupHuntThresholds(List<Integer> pThresholds)
	{
		fPluginInstance.fLogger.info( "Loading thresholds " + pThresholds + " for hunt" );
		this.theHuntThresholds.clear();
		for( Integer lThreshold : pThresholds )
		{
			theHuntThresholds.add( lThreshold );
		}
	}

	public void setupHuntMobs(List<String> pMobs)
	{
		fPluginInstance.fLogger.info( "Loading mobs " + pMobs + " for hunt" );
		this.theHuntMobs.clear();
		for(String lMob : pMobs)
		{
			theHuntMobs.add( lMob.toLowerCase() );
		}
	}
	
	public void setEnchantmentModifier() {
		String [] enchantrange = fConfig.getString( "Hunt.EnchantmentModifierRange","30-70").split("-");
		int emin = Integer.valueOf(enchantrange[0]);
		int emax = Integer.valueOf(enchantrange[1]);
		int enchantchance = new Random().nextInt((emax)-(emin))+emin;
		enchantmentModifier = (float)enchantchance / 100;
	}

	public void setBowModifier() {
		String [] bowrange = fConfig.getString( "Hunt.BowModifierRange","40-80").split("-");
		int bmin = Integer.valueOf(bowrange[0]);
		int bmax = Integer.valueOf(bowrange[1]);
		int bowchance = new Random().nextInt((bmax)-(bmin))+bmin;
		bowModifier = (float)bowchance / 100;
	}

	@Override
	protected void mainloop() {
		World world = fPluginInstance.getServer().getWorld(this.fWorld);
		
		if(world == null) {
			fGameTask.cancel();
			fPluginInstance.fLogger.warning("[MobHunt] World:"+this.fWorld +" has ceased to exist, stopping Hunt.");
			return;
		}
		
		wtime = world.getTime();
		long wcurr = world.getFullTime();
	
		long wpassed = wcurr-lastTime;
		
		//process hunts if not a skip day
		if(skipDays == 0) {
	
			//start hunt
			this.startGame();
							
			//end hunt
			if(this.isActive()) {
				if((wtime > this.endTime) || (this.fGamePlayers.size() < this.fRequiredPlayers)) {
					if(this.fGamePlayers.size() < this.fRequiredPlayers)
						joinedBroadcast(mhMessages.get().get("notEnoughPlayers"));
					if(fGameCountDown != 0) {
						worldBroadcast(mhMessages.get().get("endTimerMsg", fGameCountDown));
						fGameCountDown--;
					} else {
						//Broadcaster server message
						worldBroadcast(mhMessages.get().get("endMsg"));
						//stop the hunt.
						endGame();
						//reset fGameCountDown
						fGameCountDown = 5;
					}
				}
			}
			
			//reset hunt fGameState from a disabled hunt.
			if((wtime < this.startTime) && (this.isDisabled()))	{
				fGameState = mhGameState.WAITING;
				fGameCountDown = 5;
			}
			
		} else {
			if((wpassed > this.announcerDelay*2)) {
				worldBroadcast(mhMessages.get().get("skipdayMsg", this.skipDays));
				lastTime = wcurr;
			}
		}
		
		//decrement skip days
		if(this.skipDays > 0) {
			if((wtime < 100) && (wtime > 0)) {
				this.skipDays--;
				worldBroadcast(mhMessages.get().get("skipdayMsg",this.skipDays));
				lastTimeStatus = wcurr;
			}	
		}
		
	}
	
	@Override
	public void startGame() {
		if(this.isWaiting()) {
			if((wtime > this.startTime) && (wtime < this.endTime)) {
				if((this.fGamePlayers.size() < this.fRequiredPlayers))	{
					worldBroadcast(mhMessages.get().get("tooFewPlayers"));
					fGameState = mhGameState.DISABLED;
				} else {
					if(fGameCountDown != 0) {
						worldBroadcast(mhMessages.get().get("startTimerMsg", fGameCountDown));
						fGameCountDown--;
					} else {
						worldBroadcast(mhMessages.get().get("startMsg"));
						
						if(this.allowPVP) {
							this.fScoreboard.team.setAllowFriendlyFire(true);
							if(fPluginInstance.fDebugging)
								fPluginInstance.getServer().broadcastMessage("mhHuntGame.AllowFriendlyFire!");
						} else {
							this.fScoreboard.team.setAllowFriendlyFire(false);
							if(fPluginInstance.fDebugging)
								fPluginInstance.getServer().broadcastMessage("mhHuntGame.DenyFriendlyFire!");
						}		
						
						this.fGameState = mhGameState.ACTIVE;
						if(this.useArea) {
							if(this.autoTeleport)
								this.teleportPlayersIn();
							else
								worldBroadcast(mhMessages.get().get("huntSpawn"));
						}
						fGameCountDown = 5;
					}
				}
			}
		}
	}
	
	//FIXME le problem? et quoi?
	public void announceloop() {
		int worldpopulation = fPluginInstance.getServer().getWorld(this.fWorld).getPlayers().size();
		if(this.isWaiting() && worldpopulation >= this.fRequiredPlayers) {
			if(this.skipDays == 0) {
				long timeTilNext = 0;
				//get time til next game
				if(wtime < this.startTime)
					timeTilNext = this.startTime-wtime;
				else
					timeTilNext = (24000 - wtime) + this.startTime;
				
				long minutes = (long)((timeTilNext/20)/60);
				long seconds = (long)(timeTilNext/20) % 60;
				unjoinedBroadcast(mhMessages.get().get("nextHunt",minutes,seconds));
			} else {
				unjoinedBroadcast(mhMessages.get().get("skipdayMsg",this.skipDays));
			}
			unjoinedBroadcast(mhMessages.get().get("joinMsg"));
		}
	}

	public void setupSkipDays() {
		if((this.skipDays == 0) && (new Random().nextInt(99)+1) <= fConfig.getInt( "Hunt.SkipDayChance", 0)) {
			this.skipDays = 1;
			worldBroadcast(mhMessages.get().get("skipdayMsg", this.skipDays));
		}
	}
	
	public void refundPlayers() {
		if(this.payHunt && (fPluginInstance.fEconomy != null) && (this.isActive()))
		{
			for(String name: fGamePlayers.keySet()) {
				Player p = fPluginInstance.getServer().getPlayer(name);
				fPluginInstance.fEconomy.depositPlayer(name, Math.abs(this.fCost));
				p.sendMessage(mhMessages.get().get("leavePaid", (int)this.fCost));
			}
		}
	}
	
	@Override
	public void endGame() {
		//hunt has finished.
		fGameState = mhGameState.WAITING;
		
		//create theScore sorted list.
		HashMap<String, Integer> scores = new HashMap<String, Integer>();
		
		for(String player: fGamePlayers.keySet()) {
			scores.put(player, fGamePlayers.get(player).getScore());
		}
		
		ValueComparator vc =  new ValueComparator(scores);
		TreeMap<String,Integer> sortedHunters = new TreeMap<String,Integer>(vc);
		List<String> hunterlist = new ArrayList<String>();
		sortedHunters.putAll(scores);
		hunterlist.addAll(sortedHunters.keySet());
		
		int prevscore = -1;
		int place = 1;
		
		for(String hunter: hunterlist) {
			Player p = fPluginInstance.getServer().getPlayer(hunter);
			int score = scores.get(hunter);
			//FIXME no draws for payhunts? or top winners take fPot
			if(prevscore == scores.get(hunter))
				place--;
			
			prevscore = scores.get(hunter);
			
			this.rewardPlayer(p, place, score);
			
			place++;
					
			this.removePlayer(p);
			
		}
		this.setupGame(fPluginInstance.fEconomy);
	}
	
	@Override
	public void onMobSpawn(CreatureSpawnEvent spawnevent) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHuntGame.OnMobSpawn Fired!");
			
		if( fConfig.getBoolean( "Hunt.DenySpawnerMobs",true))	{
			if ((spawnevent.getSpawnReason().toString() == "SPAWNER") || (spawnevent.getSpawnReason().toString() == "SPAWNER_EGG"))	{
				fPluginInstance.fExcludedMobs.add(spawnevent.getEntity().getEntityId());
				return;
			}
		}
		
		if( fConfig.getBoolean( "Hunt.OutsideMobsOnly",true)) {
			//check if spawned in mob grinder
			Block block = spawnevent.getLocation().getBlock();
			LivingEntity lm = spawnevent.getEntity();
			
			if(lm instanceof Silverfish)
				return;
			
			if(lm instanceof Slime)
				return;
			
			int vertdist = 255 - block.getY();			
			for(int i = 1; i < vertdist; i++) {
				block = block.getRelative(BlockFace.UP);
				//if spawned under block that are not safe.
				if(!block.isEmpty() && (block.getType() != Material.LEAVES) && (block.getType() != Material.LOG))	{
					fPluginInstance.fExcludedMobs.add(spawnevent.getEntity().getEntityId());
					return;	
				}
			}
		}
		return;
	}

	@Override
	public void onPlayerDeath(PlayerDeathEvent e) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHuntGame.onPlayerDeath Fired!");
		
		Player victim = e.getEntity(); 
		Player killer = victim.getKiller();
		
		
		//tally victim theDeaths
		if(this.fGamePlayers.containsKey(victim.getName())) {
			this.fGamePlayers.get(victim.getName()).incrementDeaths();
		}
		//tally killers theKills
		if(killer != null) {		
			if(this.fGamePlayers.containsKey(killer.getName())) {
				this.fGamePlayers.get(killer.getName()).incrementKills();
			}
		}
	}
	
	@Override
	public void removePlayer(Player p) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHuntGame.removePlayer Fired!");
		
		if(this.fGamePlayers.containsKey(p.getName())) {
			
			if(this.isActive()) {
				p.sendMessage(mhMessages.get().get("leaveHunt"));
				worldBroadcast(mhMessages.get().get("playerLeftMsg",p.getName()));
			}
			//teleport player back to last location.
			if(this.useArea)
				p.teleport(this.fGamePlayers.get(p.getName()).getPreviousLocation());
			
			//pay back player
			if(!this.isActive() && this.payHunt && (fPluginInstance.fEconomy != null)) {
				EconomyResponse d = fPluginInstance.fEconomy.depositPlayer(p.getName(), Math.abs(this.fCost));
				this.fPot = this.fPot - this.fCost;
				if(d.transactionSuccess()) //FIXME get(str, double);
					p.sendMessage(mhMessages.get().get("leavePaid", (int) this.fCost));
				
				this.fGamePlayers.remove(p.getName());
				fPluginInstance.fPlayerList.remove(p.getName());
				this.fScoreboard.removePlayer(p);
			//else don't	
			} else {
				this.fGamePlayers.remove(p.getName());
				fPluginInstance.fPlayerList.remove(p.getName());
				this.fScoreboard.removePlayer(p);
			}
		//not in a hunt
		} else {
			p.sendMessage(mhMessages.get().get("leaveHuntFail"));
		}
	}
	
	//FIXME same as mhGame remove?
	@Override
	public void removeOutsidePlayer(Player p) {
		// if hunt is active and is an area hunt and is outside the area > remove!
		if(this.isActive() && this.useArea && this.isOutsideArena(p.getWorld().getName(), p.getLocation().getX(), p.getLocation().getZ()))
		{
			this.removePlayer(p);
			//TODO add to config.
			p.sendMessage("You've have been removed from the game! You left the arena!");
			
		}	
	}
	
	@Override
	public void addPlayer(Player p, mhPlayer pMhPlayer) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHuntGame.addPlayer Fired!");
		//if player in a game fail to join
		if(this.fGamePlayers.containsKey(p.getName()) || fPluginInstance.fPlayerList.containsKey(p.getName())) {
			p.sendMessage(mhMessages.get().get("playerJoinFail"));
		} else {
			//if the game payhunt and the server has an economy
			if(this.payHunt && fPluginInstance.fEconomy != null) {
				//withdraw money from player
				EconomyResponse d = fPluginInstance.fEconomy.withdrawPlayer(p.getName(), Math.abs(this.fCost));
				if(d.transactionSuccess()) {
					//set player game to be this
					pMhPlayer.setGame( this );
					//add player to global player list
					fPluginInstance.fPlayerList.put(p.getName(), pMhPlayer );
					//add player to hunt player list
					this.fGamePlayers.put(p.getName(), pMhPlayer );
					//add player to hunt fScoreboard
					this.fScoreboard.addPlayer(p);
					//update fPot value
					this.fPot = this.fPot + this.fCost;
					//messages
					p.sendMessage(mhMessages.get().get("joinPaid", (int) this.fCost));
					this.worldBroadcast(mhMessages.get().get("playerJoinMsg", p.getName()));
					this.worldBroadcast(mhMessages.get().get("playersRequired", Math.max((this.fRequiredPlayers - this.fGamePlayers.size()),0)));
				} else {
					//player didn't have enough money!
					p.sendMessage(mhMessages.get().get("noFunds"));
				}
			//not a pay hunt
			} else {
				//set player game to be this
				pMhPlayer.setGame( this );
				//add player to global player list
				fPluginInstance.fPlayerList.put(p.getName(), pMhPlayer );
				//add player to hunt player list
				this.fGamePlayers.put(p.getName(), pMhPlayer );
				//add player to hunt fScoreboard
				this.fScoreboard.addPlayer(p);
				//messages
				this.worldBroadcast(mhMessages.get().get("playerJoinMsg", p.getName()));
				if(this.fGamePlayers.size() < this.fRequiredPlayers) {
					this.worldBroadcast(mhMessages.get().get("playersRequired", Math.max((this.fRequiredPlayers - this.fGamePlayers.size()), 0)));
				}
				
			}
				
		}	
	}
	
	public Location getSpawn()
	{
		Block locblock;
		
		if( fConfig.getBoolean( "Hunt.RandomSpawn", true)) {
			//find a safe position
			do {
				//get random location within area extents
				int x = new Random().nextInt((this.Xmax-1)-(this.Xmin+1))+this.Xmin+1;
				int z = new Random().nextInt((this.Zmax-1)-(this.Zmin+1))+this.Zmin+1;
				
				//get highest block position	
				locblock = fPluginInstance.getServer().getWorld(this.fWorld).getHighestBlockAt(x, z);
				
			} while ((locblock.getType() == Material.LAVA) || (locblock.getType() == Material.STATIONARY_LAVA) || (locblock.getRelative(BlockFace.DOWN).getType() == Material.LAVA) || (locblock.getRelative(BlockFace.DOWN).getType() == Material.STATIONARY_LAVA));
		} else {
			locblock = fPluginInstance.getServer().getWorld(this.fWorld).getBlockAt(this.areaSpawn);
		}
		//return block position centred
		Location tploc = locblock.getLocation().add(0.5, 0, 0.5);
		
		//FIXME is it needed? load chunk if it's not
		if(!locblock.getWorld().isChunkLoaded(tploc.getChunk().getX(), tploc.getChunk().getZ()))
			locblock.getWorld().loadChunk(tploc.getChunk().getX(), tploc.getChunk().getZ());
		
		return tploc;
	}
	
	@Override
	public void getTeleportArea(Player p)	{
		if(this.isActive() && this.isOutsideArena(p.getWorld().getName(), p.getLocation().getX(), p.getLocation().getZ()) && this.useArea) {
			Location telespawn;
			//get teleport position
			telespawn = getSpawn();
			//move player with correct pitch and yaw
			telespawn.setPitch(p.getLocation().getPitch());
			telespawn.setYaw(p.getLocation().getYaw());
			//allow for world changes
			telespawn.setWorld(fPluginInstance.getServer().getWorld(this.fWorld));
			p.teleport(telespawn);
		} else {
			p.sendMessage(mhMessages.get().get("spawnFail"));
		}
	
	}

	@Override
	public void teleportPlayersIn() {
		for(String name: fGamePlayers.keySet())  {
			Player p = fPluginInstance.getServer().getPlayer(name);
			this.fGamePlayers.get(name).setPreviousLocation( p.getLocation().clone() );
			getTeleportArea(p);
		}
	}
	
	public void rewardPlayer(Player p, int place, int score) {
		switch(place) {
		case 1: {
			fGamePlayers.get(p.getName()).setFirst( 1 );
			fGamePlayers.get(p.getName()).setWins( 1 );
			
			int reward = mhReward.get(fPluginInstance).getRewardPlace(score, theHuntThresholds, place);
			mhReward.get(fPluginInstance).rewardPlayer(p, reward, "Hunt", this.fConfig);
			
			worldBroadcast(mhMessages.get().get("firstMsg",p.getName(), score));
			fireworkEffect(this.useArea, p,Color.GREEN,FireworkEffect.Type.STAR);
			if(reward != 1)
				worldBroadcast(mhMessages.get().get("firstFailMsg", p.getName(), theHuntThresholds.get(0)));
			if(reward == 5)
				p.sendMessage(mhMessages.get().get("noRewards",score, theHuntThresholds.get(3)));
			if(this.payHunt && (fPluginInstance.fEconomy != null) && this.fPot > 0){
				EconomyResponse d = fPluginInstance.fEconomy.depositPlayer(p.getName(), Math.abs(mhReward.get(fPluginInstance).givePotShare(p, this.fGamePlayers.size(), this.fPot, reward)));
				this.fPot = Math.max((this.fPot - d.amount),0);
			} 
			break;
		}
		case 2: {
			fGamePlayers.get(p.getName()).setSecond( 1 );
			fGamePlayers.get(p.getName()).setLosses( 1 );
			
			int reward = mhReward.get(fPluginInstance).getRewardPlace(score, theHuntThresholds, place);
			mhReward.get(fPluginInstance).rewardPlayer(p, reward, "Hunt", this.fConfig);
			
			worldBroadcast(mhMessages.get().get("secondMsg",p.getName(), score));
			fireworkEffect(this.useArea, p,Color.YELLOW,FireworkEffect.Type.BURST);
			if(reward != 2)
				worldBroadcast(mhMessages.get().get("secondFailMsg", p.getName(), theHuntThresholds.get(1)));
			if(reward == 5)
				p.sendMessage(mhMessages.get().get("noRewards", score, theHuntThresholds.get(3)));
			
			if(this.payHunt && (fPluginInstance.fEconomy != null) && this.fPot > 0){
				EconomyResponse d = fPluginInstance.fEconomy.depositPlayer(p.getName(), Math.abs(mhReward.get(fPluginInstance).givePotShare(p, this.fGamePlayers.size(), this.fPot, reward)));
				this.fPot = Math.max((this.fPot - d.amount),0);
			} 
			break;
		}
		case 3: {
			fGamePlayers.get(p.getName()).setThird( 1 );
			fGamePlayers.get(p.getName()).setLosses( 1 );
			
			int reward = mhReward.get(fPluginInstance).getRewardPlace(score, theHuntThresholds, place);
			mhReward.get(fPluginInstance).rewardPlayer(p, reward, "Hunt", this.fConfig);
			
			worldBroadcast(mhMessages.get().get("thirdMsg", p.getName(), score));
			fireworkEffect(this.useArea, p,Color.RED,FireworkEffect.Type.BALL);
			if(reward != 3)
				worldBroadcast(mhMessages.get().get("thirdFailMsg", p.getName(), theHuntThresholds.get(2)));
			if(reward == 5)
				p.sendMessage(mhMessages.get().get("noRewards", score, theHuntThresholds.get(3)));
			if(this.payHunt && (fPluginInstance.fEconomy != null) && this.fPot > 0) {
				EconomyResponse d = fPluginInstance.fEconomy.depositPlayer(p.getName(), Math.abs(mhReward.get(fPluginInstance).givePotShare(p, this.fGamePlayers.size(), this.fPot, reward)));
				this.fPot = Math.max((this.fPot - d.amount),0);
			}
			break;
		}
		default: {
			if( fConfig.getBoolean( "Hunt.RewardRunnersUp", true)) {
				fGamePlayers.get(p.getName()).setRunnerUp( 1 );
				fGamePlayers.get(p.getName()).setLosses( 1 );
				
				int reward = mhReward.get(fPluginInstance).getRewardPlace(score, theHuntThresholds, place);
				mhReward.get(fPluginInstance).rewardPlayer(p, reward, "Hunt", this.fConfig);
				
				p.sendMessage(mhMessages.get().get("fourthMsg", score));
				fireworkEffect(this.useArea, p,Color.WHITE,FireworkEffect.Type.CREEPER);
				if(reward != 4)
					p.sendMessage(mhMessages.get().get("noRewards", score, theHuntThresholds.get(3)));
				if(this.payHunt && (fPluginInstance.fEconomy != null) && this.fPot > 0) {
					EconomyResponse d = fPluginInstance.fEconomy.depositPlayer(p.getName(), Math.abs(mhReward.get(fPluginInstance).givePotShare(p, this.fGamePlayers.size(), this.fPot, reward)));
					this.fPot = Math.max((this.fPot - d.amount),0);
				}
			} else {
				joinedBroadcast(mhMessages.get().get("noRunnerupRewards"));
			}
			break;
		}
	}
		
	}
	
	public void gameInfo(Player p)
	{
		long minutes = (long)(((this.endTime-this.startTime)/20)/60);
		long seconds = (long)((this.endTime-this.startTime)/20) % 60;
		p.sendMessage(mhMessages.get().get("header"));
		p.sendMessage(mhMessages.get().get("mobs", this.formatHuntString( theHuntMobs.toString())));
		if(fPluginInstance.fEconomy != null)
			p.sendMessage(mhMessages.get().get("payHunt",""+this.payHunt,(int)this.fCost));
		p.sendMessage(mhMessages.get().get("thresholds",this.theHuntThresholds.get(0),this.theHuntThresholds.get(1),this.theHuntThresholds.get(2),this.theHuntThresholds.get(3)));
		p.sendMessage(mhMessages.get().get("pvp",""+this.allowPVP,this.fDeathPenalty));
		p.sendMessage(mhMessages.get().get("runnersupRewards",""+this.rewardRunnersup));
		p.sendMessage(mhMessages.get().get("bowChance",(int)(this.bowModifier*100)));
		p.sendMessage(mhMessages.get().get("enchantmentsChance",(int)(this.enchantmentModifier*100)));
		p.sendMessage(mhMessages.get().get("huntTimes",this.startTime,this.endTime));
		p.sendMessage(mhMessages.get().get("huntLength",minutes,seconds));
		p.sendMessage(mhMessages.get().get("footer"));
	}
	
	public void gameInfo()
	{
		//long current = fPluginInstance.getServer().getWorld(this.world).getTime();
		long minutes = (long)(((this.endTime-this.startTime)/20)/60);
		long seconds = (long)((this.endTime-this.startTime)/20) % 60;
		worldBroadcast(mhMessages.get().get("header"));
		worldBroadcast(mhMessages.get().get("mobs", this.formatHuntString( theHuntMobs.toString())));
		if(fPluginInstance.fEconomy != null)
			worldBroadcast(mhMessages.get().get("payHunt",""+this.payHunt,(int)this.fCost));
		worldBroadcast(mhMessages.get().get("thresholds",this.theHuntThresholds.get(0),this.theHuntThresholds.get(1),this.theHuntThresholds.get(2),this.theHuntThresholds.get(3)));
		worldBroadcast(mhMessages.get().get("pvp",""+this.allowPVP,this.fDeathPenalty));
		worldBroadcast(mhMessages.get().get("runnersupRewards",""+this.rewardRunnersup));
		worldBroadcast(mhMessages.get().get("bowChance",(int)(this.bowModifier*100)));
		worldBroadcast(mhMessages.get().get("enchantmentsChance",(int)(this.enchantmentModifier*100)));
		worldBroadcast(mhMessages.get().get("huntTimes",this.startTime,this.endTime));
		worldBroadcast(mhMessages.get().get("huntLength",minutes,seconds));
		worldBroadcast(mhMessages.get().get("footer"));
	}
	
	//broadcast to all fGamePlayers in world
	public void worldBroadcast(String message)	{
		World world = fPluginInstance.getServer().getWorld(this.fWorld);
		if(world != null) {
			List<Player> players = world.getPlayers();
			for(Player p : players)		{
				if(p.hasPermission("mobhunt.player"))
					p.sendMessage(mhMessages.get().colourise(message));
			}
		}
	}
	

	@Override
	public void onRespawn(PlayerRespawnEvent e) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHuntGame.onRespawn Fired!");
	
		if(this.isActive()) {
			Location area = getSpawn();
			if(this.useArea) {
				e.setRespawnLocation(area);
			} 
		}
		return;
	}
	
/*	public void updateStats() {
		for(String player : fGamePlayers.keySet())
		{
			try {
				Statement statement = null;
				
				if(fPluginInstance.con.isClosed()) {
					fPluginInstance.fLogger.severe("Connection closed");
					statement =	fPluginInstance.MySQL.openConnection().createStatement();
				}
				else {
					fPluginInstance.fLogger.severe("Connection open");
					statement = fPluginInstance.con.createStatement();
				}
				
				fPluginInstance.fLogger.severe("Player:"+player+" World:"+this.world);
				
				ResultSet res = statement.executeQuery("SELECT * FROM `hunt_stats` WHERE player='"+player+"' AND hunt='"+this.world+"';");
				
				if(res.next()) {
					fGamePlayers.get(player).thePlayed += res.getInt("thePlayed");
					fGamePlayers.get(player).theKills += res.getInt("theKills");
					fGamePlayers.get(player).theDeaths += res.getInt("theDeaths");
					//hunter_stats.get(player).theScore += res.getInt("theScore");
					fGamePlayers.get(player).theTotalPoints = fGamePlayers.get(player).theScore + res.getInt("theTotalPoints");
					if(fGamePlayers.get(player).theScore <= res.getInt("theHighScore"))
						fGamePlayers.get(player).theHighScore = res.getInt("theHighScore");
					else
						fGamePlayers.get(player).theHighScore = fGamePlayers.get(player).theScore;
					fGamePlayers.get(player).theWins += res.getInt("theWins");
					fGamePlayers.get(player).theLosses += res.getInt("theLosses");
					fGamePlayers.get(player).theFirst += res.getInt("theFirst");
					fGamePlayers.get(player).theSecond += res.getInt("theSecond");
					fGamePlayers.get(player).theThird += res.getInt("theThird");
					fGamePlayers.get(player).theRunnerUp += res.getInt("theRunnerUp");
					commitUpdateStats(player);
				} else {
					commitInsertStats(player);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void commitUpdateStats(String player) {
			try {
				Statement statement = fPluginInstance.con.createStatement();
				statement.executeUpdate("UPDATE `hunt_stats` SET " +
															  "thePlayed="+fGamePlayers.get(player).thePlayed+
															", theKills="+fGamePlayers.get(player).theKills+
															", theDeaths="+fGamePlayers.get(player).theDeaths+
															", theWins="+fGamePlayers.get(player).theWins+
															", theLosses="+fGamePlayers.get(player).theLosses+
															", theTotalPoints="+fGamePlayers.get(player).theTotalPoints+
															", theHighScore="+fGamePlayers.get(player).theHighScore+
															", theFirst="+fGamePlayers.get(player).theFirst+
															", theSecond="+fGamePlayers.get(player).theSecond+
															", theThird="+fGamePlayers.get(player).theThird+
															", theRunnerUp="+fGamePlayers.get(player).theRunnerUp+
															" WHERE player='"+player+"' AND hunt='"+this.world+"';");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void commitInsertStats(String player) {
			try {
				Statement statement = fPluginInstance.con.createStatement();
				statement.executeUpdate("INSERT INTO `hunt_stats` VALUES ('" + player + "','" 
																			 + this.world + "','" 
																			 + fGamePlayers.get(player).thePlayed + "','"
																			 + fGamePlayers.get(player).theKills + "','"
																			 + fGamePlayers.get(player).theDeaths + "','"
																			 + fGamePlayers.get(player).theWins + "','"
																			 + fGamePlayers.get(player).theLosses + "','"
																			 + fGamePlayers.get(player).theScore + "','"
																			 + fGamePlayers.get(player).theScore + "','"
																			 + fGamePlayers.get(player).theFirst + "','"
																			 + fGamePlayers.get(player).theSecond + "','"
																			 + fGamePlayers.get(player).theThird + "','"
																			 + fGamePlayers.get(player).theRunnerUp + "');");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public String formatHuntString(String hunts) {
		return hunts.replaceAll(",","").replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("(?i)pay", "").replaceAll("(?i)pvp","").replaceAll("(?i)area","").replaceAll("(?i)xp","").trim().replaceAll(" ", ", ");
	}*/

	@Override
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e, boolean victim) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHuntGame.onEntityDamageByEntity Fired!");
		
	}

	@Override
	public void onPlayerMove(Player p) {
		this.removeOutsidePlayer(p);
	}
}
