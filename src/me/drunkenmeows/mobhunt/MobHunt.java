package me.drunkenmeows.mobhunt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import me.drunkenmeows.mobhunt.listeners.mhBlockListener;
import me.drunkenmeows.mobhunt.listeners.mhEntityListener;
import me.drunkenmeows.mobhunt.listeners.mhInventoryListener;
import me.drunkenmeows.mobhunt.listeners.mhMobListener;
import me.drunkenmeows.mobhunt.listeners.mhPlayerListener;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;

//import com.sk89q.worldedit.bukkit.WorldEditPlugin;
//import com.sk89q.worldedit.bukkit.selections.Selection;

public class MobHunt extends JavaPlugin {
	
	//list of spawned mobs on the server
	ArrayList<Integer> fExcludedMobs = new ArrayList<Integer>();
	//list of arenas world arena list	
	public HashMap<String, mhGame> fGames = new HashMap<String,mhGame>();
	//player, hunt world/arena name
	public HashMap<String,mhPlayer> fPlayerList = new HashMap<String, mhPlayer>();
	final Logger fLogger = Logger.getLogger("Minecraft");
	//Economy
	Economy fEconomy = null;
	//instance of worldedit
//    public static WorldEditPlugin we = null;
	//fPluginInstance fScoreboard manager
	ScoreboardManager fScoreboadManager = null;
	//fDebugging flag
	boolean fDebugging = false;

	
	@Override
	public void onDisable() {
		//output to console
		PluginDescriptionFile pdfFile = this.getDescription();
				
		if(!fGames.isEmpty()) {
			 unloadHunts();
			 unloadArenas();
		}
		
		//reset any arenas and fGamePlayers if server or fPluginInstance reloads
		/*for(mhArena a : arenas.values()) {
			a.resetBlocks();
			a.clearDrops();
		}*/

		HandlerList.unregisterAll(this);
		this.fLogger.info("["+pdfFile.getName()+"] Closing MySQL connection.");
		//MySQL.closeConnection();
		
		fGames.clear();
		fPlayerList.clear();
		fExcludedMobs.clear();
	}	
	
	@Override
	public void onEnable()	{
		//output to console
		PluginDescriptionFile pdfFile = this.getDescription();
		
		//save setting.yml into fPluginInstance folder
		this.saveDefaultConfig();
		
		//dump example configs
		this.saveResource("world-global-hunt.yml", false);
		this.saveResource("world-forest-hunt.yml", false);
		//this.saveResource("world_nether-hunt.yml", false);
		//this.saveResource("world-arenaname.yml", false);
		//this.saveResource("hgsworld-hgs.yml", false);		
		
		//Check and update config file if required
		double version = getConfig().getDouble("Version");
		//if config file is incorrect version copy, delete it and create a default.
		if(version < 5.00 || (!getConfig().getKeys(false).contains("Version")))	{
			copyConfig(pdfFile);
			this.saveDefaultConfig();
			this.reloadConfig();
			return;
		}
		
		//instaniate message instance
		mhMessages.instance = new mhMessages(this);
		
		//set up scoreboardmanager
		this.fScoreboadManager = getServer().getScoreboardManager();
		
		//setup economy handler hook
		setupEconomy();
		//setup worldedit fPluginInstance hook
		setupWorldEdit();
		//loadup hunts
		loadHunts();
		//loadup arenas
		loadArenas();
		
		//if no hunts, thus no worlds or settings for worlds disable the fPluginInstance.
		if(fGames.size() < 1) {
			this.fLogger.severe("["+pdfFile.getName()+"] No worlds loaded, fPluginInstance disabled, check your config.yml");
			onDisable();
			getPluginLoader().disablePlugin(this);
			return;
		}
	
		//listen for events
		getServer().getPluginManager().registerEvents(new mhBlockListener(this), this);
		getServer().getPluginManager().registerEvents(new mhEntityListener(this), this);
		getServer().getPluginManager().registerEvents(new mhInventoryListener(this), this);
		getServer().getPluginManager().registerEvents(new mhMobListener(this), this);
		getServer().getPluginManager().registerEvents(new mhPlayerListener(this), this);
		
		//running hunts
		this.fLogger.info("["+pdfFile.getName()+"] Games running:"+ fGames.keySet());
	}
	
	private void loadArenas() {
		PluginDescriptionFile pdfFile = this.getDescription();
		List<String> worldlist = getConfig().getStringList("Worlds");

		for(String world : worldlist)
		{
			List<String> arenalist = getConfig().getStringList("Arenas."+world);
			
			if(getServer().getWorld(world) != null)	{			
				for(String arenaname: arenalist) {
					YamlConfiguration config = loadArenaConfig(arenaname, world);
					if(config.getString("Arena.GameType").equalsIgnoreCase("hgs"))
						fGames.put(arenaname, new mhHgsGame(this, arenaname, world));
					//else if(config.getString("Arena.Gametype").equalsIgnoreCase("hgs"))
					//	arenas.put(arenaname, new mhFfaArena(this));
					this.fLogger.info("["+pdfFile.getName()+"] Arena:"+arenaname+" loaded for "+world+" world");
				}				
			} else {
				this.fLogger.warning("["+pdfFile.getName()+"] World:"+world+" does not exist, check your config.yml");
			}
		}
		
	}

	private void loadHunts() {
		this.getDescription();
		List<String> lWorlds = getConfig().getStringList( "Worlds" );
		fLogger.info( "lWorlds " + lWorlds.toString() );

		for( String lWorld : lWorlds)
		{
			List< String > lHuntList = getConfig().getStringList( "Hunts."+lWorld );
			fLogger.info( "lHuntList " + lHuntList.toString() );
			for(String lHunt : lHuntList )
			{
				mhHuntGame lHuntGame = new mhHuntGame( this, lHunt, lWorld );
				fGames.put( lWorld, lHuntGame );
			}
		}
	}

	private void unloadArenas() {
		PluginDescriptionFile lPluginDescriptionFile = this.getDescription();
		List<String> lWorlds = getConfig().getStringList("Worlds");

		for(String lWorld : lWorlds)
		{
			List<String> lArenas = getConfig().getStringList("Arenas."+lWorld);
			for(String lAreaName : lArenas) {
				this.fLogger.info("["+lPluginDescriptionFile.getName()+"] unloading arena "+lAreaName+" in "+lWorld);
				mhGame lGame = fGames.get(lAreaName);
				if( lGame != null ) {
					lGame.unload();
				}
			}
		}
	}
	
	private void unloadHunts() {
		PluginDescriptionFile lPluginDescriptionFile = this.getDescription();
		List<String> lHuntWorlds = getConfig().getStringList("Hunts");
		
		//remove mobs that have been spawner spawned.
		for(String lWorld : lHuntWorlds)
		{
			this.fLogger.info("["+lPluginDescriptionFile.getName()+"] unloading hunt for "+ lWorld);

			//FIXME: invert and excute unload
			if(getServer().getWorld(lWorld) == null || fGames.get(lWorld) == null) {
				break;
			}
			
			mhHuntGame lHuntGame = (mhHuntGame) fGames.get(lWorld);
			//reset fScoreboard
			//get list of entities for each world and remove them
			List<LivingEntity> lLivingEntities = getServer().getWorld(lWorld).getLivingEntities();
			
			//clear worlds of 'hot' mobs
			if( lHuntGame.fConfig.getBoolean("Hunt.RemoveSpawnerMobs", true) )	{
				for(LivingEntity lLivingEntity : lLivingEntities)
				{
					if( fExcludedMobs.contains( lLivingEntity.getEntityId() ) )
					{
						lLivingEntity.remove();
					}
				}
				this.fLogger.info("["+lPluginDescriptionFile.getName()+"] RemoveSpawnerMobs:("+lHuntGame.fConfig.getBoolean("Hunt.RemoveSpawnerMobs",true)+") - Removed spawner mobs from: "+ lWorld);
			}
			//unload hunt
			lHuntGame.unload();
		}
	}
	
	public void copyConfig(PluginDescriptionFile pdfFile)
    {			
    	InputStream lIS;
    	OutputStream lOS;
    	//copy old file 
    	try{
    		File pluginsFolder = this.getDataFolder();
    		
    	    File lConfigYML = new File(pluginsFolder.toString()+"/config.yml");
    	    File lConfigOLD = new File(pluginsFolder.toString()+"/config.old");
 
    	    lIS = new FileInputStream(lConfigYML);
    	    lOS = new FileOutputStream(lConfigOLD);
 
    	    byte[] lFileBuffer = new byte[1024];
 
    	    int lReadLength;
    	    //copy the file content in bytes 
    	    while ((lReadLength = lIS.read(lFileBuffer)) > 0)
			{
    	    	lOS.write(lFileBuffer, 0, lReadLength);
    	    }
    	    
     	    lIS.close();
    	    lOS.close();
    	    //delete old file
    	    if( lConfigYML.delete() ) {
    	    	this.fLogger.warning("["+pdfFile.getName()+"] Config incorrect version, creating default config.");
    		} else {
    			this.fLogger.warning("["+pdfFile.getName()+"] Config incorrect version, failed to create default fConfig...");
    		}
    	    
    	    this.fLogger.info("["+pdfFile.getName()+"] Config incorrect version, backup created...");
 
    	}catch( IOException lException ) {
    		lException.printStackTrace();
    	}
    }
	
	public boolean onCommand(CommandSender pSender, Command pCommand, String pLabel, String[] pArgs)	{
		if(pCommand.getLabel().equals("mobhunt")) {
			if(pSender.hasPermission("mobhunt.player")) {
				if(pArgs.length > 0) {
													
					if(pArgs[0].equalsIgnoreCase("join")) {
						if(pSender instanceof Player) {
							Player p = (Player)pSender;
							mhGame game = null;
							
							if(pArgs.length > 1) {
								game = fGames.get(pArgs[1]);
							} else {
								game = fGames.get(p.getWorld().getName());
							}
							
							if(game != null) {
								game.addPlayer(p, new mhPlayer());
							} else {
								p.sendMessage(mhMessages.get().get("noGame"));
								return false;
							}
							
						} else {
							pSender.sendMessage(mhMessages.get().get("msgPrefix")+"Denied - player use only.");
						}
					}
					
					if(pArgs[0].equalsIgnoreCase("leave")) {
						if(pSender instanceof Player) {
							Player p = (Player)pSender;
							mhPlayer mhp = this.fPlayerList.get(p.getName());
							
							if(mhp != null) {
								if(mhp.getGame() != null) {
									mhp.getGame().removePlayer(p);
								} else {
									//FIXME 
									p.sendMessage(mhMessages.get().get("noGame"));
									return false;
								}
							} else {
								//TODO player not in game.
							}

						} else {
							pSender.sendMessage(mhMessages.get().get("msgPrefix")+"Denied - player use only.");
						}
					}
					
					//FIXME move into Hunt Game
					if(pArgs[0].equalsIgnoreCase("spawn")) {
						if(pSender instanceof Player) {
							Player p = (Player)pSender;
							mhPlayer lMhPlayer = this.fPlayerList.get(p.getName());
							
							if(lMhPlayer != null) {
								if(lMhPlayer.getGame() != null) {
									lMhPlayer.getGame().getTeleportArea(p);
								} else {
									p.sendMessage(mhMessages.get().get("noGame"));
									return false;
								}
							} else {
								//TODO player not in game.
							}
				
						} else {
							pSender.sendMessage("Denied - player use only.");
						}
					}
					
					//FIXME allow player to execute without joining first.
					if(pArgs[0].equalsIgnoreCase("info")) {
						if(pSender instanceof Player) {
							Player p = (Player)pSender;
							mhGame game = null;
							
							if(pArgs.length > 1) {
								game = fGames.get(pArgs[1]);
							} else {
								game = fGames.get(p.getWorld().getName());
							}
							
							if(game != null) {
								game.gameInfo(p);
							}else {
								p.sendMessage(mhMessages.get().get("noGame"));
								return false;
							}
						} else {
							pSender.sendMessage("Denied - player use only.");
						}
					}
					
					if(pArgs[0].equalsIgnoreCase("status")) {
						if(pSender instanceof Player) {
							Player p = (Player)pSender;
							//mhPlayer mhp = this.fPlayerList.get(p.getName());
							mhGame game = null;
							
							if(pArgs.length > 1) {
								game = fGames.get(pArgs[1]);
							} else {
								game = fGames.get(p.getWorld().getName());
							}
						
							if(game != null) {
								game.status(p);
							} else {
								p.sendMessage(mhMessages.get().get("noGame"));
								return false;
							}
							
						} else {
							pSender.sendMessage("Denied - player use only.");
						}
					}
				}
				
				if(pArgs.length == 0) {
					cmdusage(pSender);
				}
			}
				
			if(pSender.hasPermission("mobhunt.admin")) {
				if(pArgs.length > 0)
				{
					if(pArgs[0].equalsIgnoreCase("setspawn")) {
						if(pSender instanceof Player) {
							Player p = (Player)pSender;
							mhGame game = null;
							
							if(pArgs.length > 1) {
								game = fGames.get(pArgs[1]);
							} else {
								game = fGames.get(p.getWorld().getName());
							}
							
							if(game != null) {
								game.fSpawnPoints.add(p.getLocation());
								game.saveSpawnPoints();
							} else {
								p.sendMessage("Game:"+ pArgs[1] +"doesn't exist");
							}
							
						} else {
							pSender.sendMessage(mhMessages.get().get("msgPrefix")+"Denied - player use only.");
						}
					}
					
//					if(args[0].equalsIgnoreCase("setarena") || args[0].equalsIgnoreCase("setarea") ) {
//						if(args.length > 1) {
//							if(sender instanceof Player) {
//								Player p = (Player)sender;
//								mhGame game = games.get(args[1]);
//
//								if(game != null)
//								{
//									//FIXME move into mhGame?
//									//make into fucntion
//									Selection s = we.getSelection(p);
//
//									Location max = s.getMaximumPoint();
//									Location min = s.getMinimumPoint();
//									//sort out the extents
//									fLogger.info("Max:"+max.toString());
//									fLogger.info("Min:"+min.toString());
//
//									if(max.getBlockX() > min.getBlockX()) {
//										game.Xmax = max.getBlockX();
//										game.Xmin = min.getBlockX();
//									}
//									else {
//										game.Xmax = min.getBlockX();
//										game.Xmin = max.getBlockX();
//									}
//
//									if(max.getBlockZ() > min.getBlockZ()) {
//										game.Zmax = max.getBlockZ();
//										game.Zmin = min.getBlockZ();
//									}
//									else {
//										game.Zmax = min.getBlockZ();
//										game.Zmin = max.getBlockZ();
//									}
//
//									//save to file
//									game.saveArea();
//								} else {
//									sender.sendMessage(mhMessages.get().get("noGame"));
//									return false;
//								}
//							} else {
//								sender.sendMessage("Denied - player use only.");
//							}
//						}
//					}
					
					if(pArgs[0].equalsIgnoreCase("pos1")) {
						if(pSender instanceof Player) {
							Player p = (Player)pSender;
							mhGame game = fGames.get(pArgs[1]);

							game.Xmax = p.getLocation().getBlockX();
							game.Zmax = p.getLocation().getBlockZ();

							game.saveArea();
						}

					}

					if(pArgs[0].equalsIgnoreCase("pos2")) {
						if(pSender instanceof Player) {
							Player p = (Player)pSender;
							mhGame game = fGames.get(pArgs[1]);

							game.Xmin = p.getLocation().getBlockX();
							game.Zmin = p.getLocation().getBlockZ();

							game.saveArea();
						}
					}
					
					if(pArgs[0].equalsIgnoreCase("reload")) {
						onDisable();
						this.reloadConfig();
						onEnable();
						pSender.sendMessage(mhMessages.get().get("reloadMsg"));
					}

					if(pArgs[0].equalsIgnoreCase("next")) {
						mhGame game = null;
						if(pSender instanceof Player) {
							Player p = (Player)pSender;
							
							if(pArgs.length > 1) {
								game = fGames.get(pArgs[1]);
							} else {
								game = fGames.get(p.getWorld().getName());
							}
						} else {
							if(pArgs.length > 1) {
								game = fGames.get(pArgs[1]);
							} else {
								pSender.sendMessage("World undefined.");
							}
							
						}
						
						if(game != null) {
							game.setupGame(fEconomy);
							game.gameInfo();
						} else {
							pSender.sendMessage(mhMessages.get().get("noGame"));
							return false;
						}
					} //end next command
				}
			}
		}
		return true;
	}
	
	private void cmdusage(CommandSender pCommandSender) {
		pCommandSender.sendMessage(mhMessages.get().get("cmdtitle", this.getDescription().getVersion()));
		pCommandSender.sendMessage("&e/mobhunt join"+mhMessages.get().get("cmdjoin"));
		pCommandSender.sendMessage("&cAlias: &e/joinhunt"+mhMessages.get().get("cmdjoin"));
		pCommandSender.sendMessage("&e/mobhunt leave"+mhMessages.get().get("cmdleave"));
		pCommandSender.sendMessage("&cAlias: &e/leavehunt"+mhMessages.get().get("cmdleave"));
		pCommandSender.sendMessage("&e/mobhunt spawn"+mhMessages.get().get("cmdspawn"));
		pCommandSender.sendMessage("&cAlias: &e/huntspawn"+mhMessages.get().get("cmdspawn"));
		pCommandSender.sendMessage("&e/mobhunt info"+mhMessages.get().get("cmdinfo"));
		pCommandSender.sendMessage("&cAlias: &e/huntinfo"+mhMessages.get().get("cmdinfo"));
		pCommandSender.sendMessage("&e/mobhunt status"+mhMessages.get().get("cmdstatus"));
		pCommandSender.sendMessage("&cAlias: &e/huntstatus"+mhMessages.get().get("cmdstatus"));

		if(pCommandSender.hasPermission("mobhunt.admin")) {
			pCommandSender.sendMessage("&e/mobhunt reload"+mhMessages.get().get("cmdreload"));
			pCommandSender.sendMessage("&e/mobhunt next"+mhMessages.get().get("cmdnext"));
		}
	}
	
	private void setupWorldEdit() {
		if(this.getServer().getPluginManager().getPlugin("WorldEdit") != null)
		{
//			we = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
			fLogger.warning("[MobHunt] Worldedit hooked!");
		} else {
			fLogger.warning("[MobHunt] Worldedit not found! Use /pos1 and /pos2 for area hunt and arena defining.");
		}
	}
	
	private void setupEconomy() {
		PluginDescriptionFile fPluginDescriptionFile = this.getDescription();
		
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
           return;
        } 
        
        RegisteredServiceProvider<Economy> lEconomyServiceProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (lEconomyServiceProvider == null) {
        	this.fLogger.warning("["+fPluginDescriptionFile.getName()+"] Valut not found - Economy features disabled.");
            return;
        } else {
        	fEconomy = lEconomyServiceProvider.getProvider();
        	this.fLogger.info("["+fPluginDescriptionFile.getName()+"] Valut found - Economy enabled.");
        }
	}

	public YamlConfiguration loadArenaConfig(String pArenaName, String pWorldName) {
		File lArenaConfigFile = new File(this.getDataFolder(), pWorldName+"-"+pArenaName+"-arena.yml");
	    if (!lArenaConfigFile.exists()) {
			try {
				lArenaConfigFile.createNewFile();
				return YamlConfiguration.loadConfiguration(lArenaConfigFile);
			} catch (Exception lException) {
			}
		}
	    return YamlConfiguration.loadConfiguration(lArenaConfigFile);
	}
	
	public boolean insideGameArea(String pWorldName, double lXPosition, double lZPosition) {
		for(Entry<String, mhGame> lAreanGameEntrySet : fGames.entrySet()) {
			if(lAreanGameEntrySet.getValue().fWorld.equalsIgnoreCase(pWorldName)
					&& !lAreanGameEntrySet.getValue().hasZeroArea() ) {
				if(!lAreanGameEntrySet.getValue().isOutsideArena(pWorldName, lXPosition, lZPosition)) {
					return true;
				}
			}
		}
		return false;
	}
}


