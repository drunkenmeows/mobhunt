package me.drunkenmeows.mobhunt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public abstract class mhGame {
	
	//settings
	int fGameCountDown;

	final static int MOBS = 0;
	final static int THRESHOLDS = 1;

	public HashMap<String, mhPlayer> fGamePlayers = new HashMap<String, mhPlayer>();
	
	HashMap<String, ItemStack[][]> fInventoryHolder = new HashMap<String, ItemStack[][]>();
	
	private ArrayList<Block> fPlacedBlocks = new ArrayList<Block>();
	private ArrayList<Block> fBrokenBlocks = new ArrayList<Block>();
	private ArrayList<Material> fPreviousBlockMaterial = new ArrayList<Material>();
	
	ArrayList<Location> fFilledChests = new ArrayList<Location>();
		
	private List<int[]> fChunkIds = new ArrayList<int[]>();
	
	public MobHunt fPluginInstance;
	
	mhGameState fGameState = mhGameState.WAITING;
	
	BukkitTask fGameTask = null;
	BukkitTask fTempTask = null;
	YamlConfiguration fConfig = null;
	
	mhScoreboard fScoreboard = null;
	
	//Game config
	String fGameType;
	protected double fCost = 100.0;
	
	String fWorld;
	String fGameName;
	boolean fHideNameTags = true;
	boolean fAllowRandomChests;
	boolean fDenyBlockPlace;
	boolean fDenyBlockBreak;
	boolean fDisableCommands;
	boolean fTallyPot;
	boolean fUseChests;
	protected boolean queueInsideArena;
	//always true unless another game says otherwise.
	boolean fUseArea = true;
	
	int fPlayerEndLimit;
	int fWarmupTime;
	int fStartupTime;
	int fTimelimit;
	int fFragLimit;
	int fVoteLimit;
	double fPot;
	int fKillReward;
	int fDeathPenalty;
	int Xmax, Zmax, Xmin, Zmin;
	int fPlayerLimit;
	int fRequiredPlayers;
	
	ArrayList<Location> fSpawnPoints = new ArrayList<Location>();
	
	List<String> fCommandWhitelist;
	List<String> fBlockPlaceWhitelist;
	List<String> fBlockBreakWhiteList;
	
	protected mhGame( MobHunt pMobHunt, String pGameName, String pWorldName ) {
		this.fPluginInstance = pMobHunt;
		this.fGameName = pGameName;
		this.fWorld = pWorldName;
		this.loadSettings();
		this.getChunkIds();
	}
	
	public abstract void maintask();
	protected abstract void mainloop();
	public abstract void loadSettings();
	public abstract void startGame();
	public abstract void endGame();
		
	public abstract void teleportPlayersIn();

	public abstract void onMobSpawn(CreatureSpawnEvent event);
	public abstract void onEntityDeath(EntityDeathEvent deathevent, Player p, LivingEntity lm);
	public void onInventoryOpen(Player p, Chest chest) {	}
	public abstract void onEntityDamageByEntity(EntityDamageByEntityEvent e, boolean victim);
	
	public abstract void onPlayerMove(Player p);
	public abstract void onPlayerDeath(PlayerDeathEvent e);
	public abstract void onRespawn(PlayerRespawnEvent e);
	
	public abstract void gameInfo();
	public abstract void gameInfo(Player p);
	public abstract void setupGame(Economy econ);
	public abstract void status(Player p);
	public abstract GameType getGameType();
	
	public void addPlayer(Player p, mhPlayer mhp)
	{
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhGame.addPlayer Fired!");
		
		if(this.isActive() || this.fGamePlayers.containsKey(p.getName()) || fPluginInstance.fPlayerList.containsKey(p.getName())) {
			p.sendMessage(mhMessages.get().get("playerJoinFail"));
		} else {
			//set player game to be this
			mhp.setGame( this );
			//add player to global player list
			fPluginInstance.fPlayerList.put(p.getName(), mhp );
			//add player to hunt player list
			this.fGamePlayers.put(p.getName(), mhp );
			//add player to hunt fScoreboard
			//this.fScoreboard.addPlayer(p);
		}
	}

	YamlConfiguration loadConfig(String pFilename)
	{
		File lConfigFile = new File( fPluginInstance.getDataFolder(), pFilename );
		fPluginInstance.fLogger.info( "loading config for " + pFilename );

		if( !lConfigFile.exists() )
		{
			try
			{
				if(lConfigFile.createNewFile())
				{
					fPluginInstance.fLogger.info("New configuration file Created!");
				}
				return YamlConfiguration.loadConfiguration( lConfigFile );
			}
			catch( Exception pException )
			{
				pException.printStackTrace();
			}
		}

		return YamlConfiguration.loadConfiguration(lConfigFile);
	}

	public boolean onBlockBreak(Block block, Player p) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhGame.onBlockBreak Fired!");
		
		if(!this.hasZeroArea()) {
			if(this.fWorld.equalsIgnoreCase(p.getWorld().getName()) && !(this.isOutsideArena(block.getWorld().getName(), block.getLocation().getX(), block.getLocation().getZ()))) {
				if(this.isActive()) {
					if(this.fDenyBlockBreak)
						return false;
					if(!this.fBlockBreakWhiteList.isEmpty() && !this.fBlockBreakWhiteList.contains(block.getType().toString()))
						return false;
					
					if(!fBrokenBlocks.contains(block) && !fPlacedBlocks.contains(block)) {
						this.fBrokenBlocks.add(block);
						this.fPreviousBlockMaterial.add(block.getType());
						return true;
					}
					
				} else {
					return false;
				}
			} 
		}
		
		return true;
	}
	
	public boolean onBlockPlace(Block block, Player p) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhGame.onBlockPlace Fired!");
		
		if(!this.hasZeroArea()) {			
			if(this.fWorld.equalsIgnoreCase(p.getWorld().getName()) && !(this.isOutsideArena(block.getWorld().getName(), block.getLocation().getX(), block.getLocation().getZ()))) {
				if(this.isActive()) {
					if(this.fDenyBlockPlace)
						return false;
					
					if(!this.fBlockPlaceWhitelist.isEmpty() && !this.fBlockPlaceWhitelist.contains(block.getType().toString()))
						return false;
					
					this.fPlacedBlocks.add(block);
				} else {
					return false;
				}
			}
		}
		return true;
	}
	
	void clearDrops() {
		for(int[] id: fChunkIds){
			Chunk c = fPluginInstance.getServer().getWorld(fWorld).getChunkAt(id[0], id[1]);
			//fPluginInstance.fLogger.info("Chunk:"+id[0]+","+id[1]+" clearing "+ c.getEntities().length+" entities.");
			for(Entity e: c.getEntities())	{
				if(!(e instanceof Player))
					e.remove();
			}
			fPluginInstance.getServer().getWorld(fWorld).refreshChunk(id[0], id[1]);
		}
	}
	
	public void checkArea() {
		int x;
		int z;
		PluginDescriptionFile pdfFile = fPluginInstance.getDescription();
		if(Xmin > Xmax)	{
			fPluginInstance.fLogger.warning("["+pdfFile.getName()+"] Area: Xmin > Xmax... Swapping values");
			x = Xmax;
			Xmax = Xmin;
			Xmin = x;	
		}
		
		if(Zmin > Zmax ) {
			fPluginInstance.fLogger.warning("["+pdfFile.getName()+"] Area: Zmin > Zmax... Swapping values");
			z = Zmax;
			Zmax = Zmin;
			Zmin = z;
		}
	}
	
	public void cancelTasks() {
		this.fGameTask.cancel();
		this.fTempTask.cancel();
	}
	
	void fireworkEffect(boolean useArea, Player player, Color color, FireworkEffect.Type FET) {
		
		Location loc = (useArea) ? this.fGamePlayers.get(player.getName()).getPreviousLocation() : player.getLocation();
		
	    loc.setY(loc.getY() - 1.0D);
	    loc.setPitch(-90.0F);
	
	    Firework firework = (Firework)player.getWorld().spawnEntity(loc, EntityType.FIREWORK);
	    FireworkMeta fireworkMeta = firework.getFireworkMeta();
	    FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(color).with(FET).trail(true).build();
	    fireworkMeta.addEffect(effect);
	    fireworkMeta.setPower(1);
		firework.setFireworkMeta(fireworkMeta);
	}
	
	boolean hasZeroArea() {
		return (this.Xmax == this.Xmin) || (this.Zmin == this.Zmax);
	}
	
	public void teleportOutPlayer(Player p) {
		fPluginInstance.fLogger.info("Player: "+p.getName()+" prev: "+this.fGamePlayers.get(p.getName()).getPreviousLocation().toString());
		
		//set Pitch on previous position
		this.fGamePlayers.get(p.getName()).getPreviousLocation().setPitch(p.getLocation().getPitch());
		//set Yaw on previous position
		this.fGamePlayers.get(p.getName()).getPreviousLocation().setYaw(p.getLocation().getYaw());
		p.setVelocity(new Vector(0,0,0));
		//teleport player into previous position
		p.teleport(this.fGamePlayers.get(p.getName()).getPreviousLocation());
	}	
	
	public void restoreInventory(Player p) {
		ItemStack[][] inventory = this.fInventoryHolder.get(p.getName());
		try {
			p.getInventory().setArmorContents(inventory[0]);
			//set Inventory content
			p.getInventory().setContents(inventory[1]);
			this.fInventoryHolder.remove(p.getName());
		}
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public void resetArenaBlocks() {
		fTempTask = fPluginInstance.getServer().getScheduler().runTaskTimer(fPluginInstance, new Runnable(){
			public void run(){
				resetBlocksQueued();
			}	
		}, 40L, 20L);
	}
	
	public void resetBlocks() {
		//in a repeating task?
		for(Block b: this.fPlacedBlocks)
			b.setType(Material.AIR);
		
		int i = 0;
		for(Block b: this.fBrokenBlocks)	{
			b.setType(this.fPreviousBlockMaterial.get(i));
			i++;
		}
	}
	
	public boolean rollDice(int chance) {
		return ((new Random().nextInt(99)+1) <= chance);
	}

	protected void resetBlocksQueued() {
		int lQueueSize = 50;
		for(int i = 0; i < lQueueSize; i++) {
			if(!fPlacedBlocks.isEmpty()) {
				this.fPlacedBlocks.get(0).setType(Material.AIR);
				this.fPlacedBlocks.remove(0);
			}
			if(!fBrokenBlocks.isEmpty()) {
				this.fBrokenBlocks.get(0).setType(this.fPreviousBlockMaterial.get(0));
				this.fBrokenBlocks.remove(0);
				this.fPreviousBlockMaterial.remove(0);
			}
		}
		
		if(fBrokenBlocks.isEmpty() && fPlacedBlocks.isEmpty()) {
			fTempTask.cancel();
			this.fGameState = mhGameState.WAITING;
			fPluginInstance.fLogger.info("["+this.fGameName + "] regeneration COMPLETE!");
		}
			
	}	
	
	//FIXME basic code
	public void removePlayer(Player p) {
		if(fPluginInstance.fDebugging)
			fPluginInstance.getServer().broadcastMessage("mhHgsGame.removePlayer Fired!");
		
		mhPlayer mhp = this.fGamePlayers.remove(p.getName());
		mhp.setGame( null );
		fPluginInstance.fPlayerList.remove(p.getName());
		this.fScoreboard.removePlayer(p);
	}
	
	//FIXME basic code
	public void removeOutsidePlayer(Player p) {
		if(this.isActive() && this.isOutsideArena(p.getWorld().getName(), p.getLocation().getX(), p.getLocation().getZ())){
			this.removePlayer(p);
			
			p.sendMessage("You've have been removed from the game! You left the arena!");
		}
		
	}
	
	public void rewardPlayer(String gametype, Player p) 
	{
		List<String> items = parseItemString(gametype, fGamePlayers.size());
		
		for(String i : items) {
			String[] item = i.split(";");
			
			if(i.contains("$") && fPluginInstance.fEconomy != null) {
				//process money reward
				this.giveMoney(item, p);
			}
			
			if(item.length == 4) {
				//process normal item reward
				this.giveItemStack(item, p);
			}
			
			if(item.length > 5) {
				//process enchanted item reward
				this.giveEnchantedItemStack(item, p);
			}
		}
	}
		
	public void saveArea() {
		this.checkArea();
		
		File file = null;
		
		if(this instanceof mhHgsGame)
		{
			fConfig.set( "Arena.Area.Xmax", Xmax );
			fConfig.set( "Arena.Area.Xmin", Xmin );
			fConfig.set( "Arena.Area.Zmax", Zmax );
			fConfig.set( "Arena.Area.Zmin", Zmin );
			file = new File( fPluginInstance.getDataFolder(), fWorld + "-" + fGameName + "-arena.yml" );
		}
		if(this instanceof mhHuntGame)
		{
			fConfig.set( "Hunt.Area.Xmax", Xmax );
			fConfig.set( "Hunt.Area.Xmin", Xmin );
			fConfig.set( "Hunt.Area.Zmax", Zmax );
			fConfig.set( "Hunt.Area.Zmin", Zmin );
			file = new File( fPluginInstance.getDataFolder(), fWorld + "-hunt.yml" );
		}

		try {
			this.fConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveSpawnPoints() {
		List<String> spawns = new ArrayList<String>();
		for(Location l: this.fSpawnPoints)
			spawns.add(String.valueOf(l.getBlockX())+","+String.valueOf(l.getBlockY())+","+String.valueOf(l.getBlockZ()));
		
		fConfig.set("Arena.Spawns", spawns);
		File file = new File(fPluginInstance.getDataFolder(), fWorld +"-"+ fGameName +"-arena.yml");
		try {
			this.fConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void storeInventory(Player p) {
		ItemStack[][] inventory = new ItemStack[2][1];
		//get Inventory and Armour
		inventory[0] = p.getInventory().getArmorContents(); 
		inventory[1] = p.getInventory().getContents();
		for(ItemStack is: inventory[1]) {
			if(is !=null)
				fPluginInstance.fLogger.info("p:"+p.getName()+": "+is.toString());
			else 
				fPluginInstance.fLogger.info("is: null");
		}
		//store Inventory
		this.fInventoryHolder.put(p.getName(), inventory);
		//clear
		p.getInventory().clear();
	}

	public boolean isOutsideArena(String world, double px, double pz)
	{
		if(!this.fUseArea) return true;
		if(!world.equalsIgnoreCase(this.fWorld)) return true;
		
		if(px>Xmax) return true;
		if(px<Xmin) return true;
		if(pz>Zmax) return true;
		if(pz<Zmin) return true;
		
		return false;
	}
	
	public boolean isActive() {
		return this.fGameState == mhGameState.ACTIVE;
	}
	
	public boolean isStarting() {
		return this.fGameState == mhGameState.STARTING;
	}
	
	public boolean isWaiting() {
		return this.fGameState == mhGameState.WAITING;
	}
	
	public boolean isDisabled() {
		return this.fGameState == mhGameState.DISABLED;
	}
	
	public boolean isWarmup() {
		return this.fGameState == mhGameState.WARMUP;
	}
	
	public boolean isRegenerating() {
		return this.fGameState == mhGameState.REGENERATING;
	}
	
	public float getRangeValue(float min, float max) {
		return Math.round(new Random().nextFloat() * (max - min) + min);
	}
	
	protected void getChunkIds() {
		for(int x = Xmin; x <= Xmax; x = x+16 )
		{
			for(int z = Zmin; z <= Zmax; z = z+16) {
				int[] id = {(x/16),(z/16)};
				
				fChunkIds.add(id);
				fPluginInstance.fLogger.info("Chunk:"+(x/16)+","+(z/16));
			}
		}
	}
	
	public void giveItemStack(String[] item, Player p) {
		if(this.rollDice(Integer.parseInt(item[3]))) {
			HashMap<Integer, ItemStack> leftitems = new HashMap<Integer, ItemStack>();
			//add item to inventory
			leftitems = p.getInventory().addItem(new ItemStack(Material.getMaterial(item[0]), Integer.parseInt(item[2]), Short.parseShort(item[1])));
			if(!leftitems.isEmpty()) {
				//drop items
				for(ItemStack j : leftitems.values()){
					p.getWorld().dropItemNaturally(p.getLocation(), j);
				}
			}
		}
	}
	
	public void giveEnchantedItemStack(String[] item, Player p) {
		if(this.rollDice(Integer.parseInt(item[5]))) {
			HashMap<Integer, ItemStack> leftitems = new HashMap<Integer, ItemStack>();
			ItemStack itemstack = new ItemStack(Material.getMaterial(item[0]), Integer.parseInt(item[2]), Short.parseShort(item[1]));
			//ItemStack itemstack = new ItemStack(Integer.parseInt(item[0]),Integer.parseInt(item[2]),Short.parseShort(item[1]));
			//itemstack.addEnchantment(Enchantment.getByName(item[3]), Integer.parseInt(item[4]));
			//FIXME
			//itemstack = applyEnchantment(item[3], Integer.parseInt(item[4]), itemstack);
			
			leftitems = p.getInventory().addItem(itemstack);
			if(!leftitems.isEmpty()) {
				//drop items
				for(ItemStack j : leftitems.values()){
					p.getWorld().dropItemNaturally(p.getLocation(), j);
				}
			}
		}
	}

	public void getTeleportArea(Player p) {
		//TODO
	}

	public void getRandomChestItems(Chest chest) {
	
		List<String> items = fConfig.getStringList("Arena.Chest.Items");
		for(String i: items) {
			String[] item = i.split(";");
			
			if(mhReward.get(fPluginInstance).rollthedice(Integer.parseInt(item[item.length-1]))) {
				ItemStack itemstack = new ItemStack(Material.getMaterial(item[0]), Integer.parseInt(item[2]), Short.parseShort(item[1]));
				
				if(item.length == 5)
					itemstack = mhReward.get(fPluginInstance).applyEnchantment(item[3], Integer.parseInt(item[4]), itemstack);
				
				chest.getInventory().addItem(itemstack);
			}
			
		}
	}
	//broadcast to player not signed up for hunt
	public void unjoinedBroadcast(String message)	{
		World world = fPluginInstance.getServer().getWorld(this.fWorld);
		if(world != null) {
			List<Player> players = world.getPlayers();
			for(Player p : players)		{
				if(p.hasPermission("mobhunt.player") && !(this.fGamePlayers.containsKey(p.getName())))
					p.sendMessage(mhMessages.get().colourise(message));
			}
		}
	}
	
	//broadcast to hunters only
	public void joinedBroadcast(String message)	{
		for(String name : fGamePlayers.keySet())	{
			Player p = fPluginInstance.getServer().getPlayer(name);
			
			if((p != null) && p.hasPermission("mobhunt.player"))
				p.sendMessage(mhMessages.get().colourise(message));
		}
	}
	
	public void giveMoney(String[] item, Player p) {
		if(this.rollDice(Integer.parseInt(item[2]))) {
			float money = 0;
			if(item[1].contains("-")) {
				money = this.getRangeValue(Float.parseFloat(item[1].split("-")[0]),  Float.parseFloat(item[1].split("-")[1]));
			} else {
				money = Float.parseFloat(item[1]);
			}
			//give money to player
			fPluginInstance.fEconomy.depositPlayer(p.getName(), Math.abs(money));
			p.sendMessage(mhMessages.get().get("moneyRewards",money));
		}
	}
	
	private List<String> parseItemString(String gametype, int position) {
		
		switch(position) {
			case 1: {
				return fConfig.getStringList(gametype+".Rewards.First.Items");
			}
			case 2: {
				return fConfig.getStringList(gametype+".Rewards.Second.Items");
			}
			case 3: {
				return fConfig.getStringList(gametype+".Rewards.Third.Items");
			}
			default: {
				return fConfig.getStringList(gametype+".Rewards.RunnersUp.Items");
			}
		}
	}
	
	public String formatHuntString(String hunts) {
		return hunts.replaceAll(",","").replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("(?i)pay", "").replaceAll("(?i)pvp","").replaceAll("(?i)area","").replaceAll("(?i)xp","").trim().replaceAll(" ", ", ");
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

	void unload() {
		fScoreboard.resetPlayers();
		fGameTask.cancel();
		fTempTask.cancel();
		fGamePlayers.clear();
	}
}
