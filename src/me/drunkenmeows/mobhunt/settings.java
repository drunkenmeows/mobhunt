package me.drunkenmeows.mobhunt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

public class settings {

	public YamlConfiguration config = null;

	//hunt settings
	//mobs
	public boolean all;
	public boolean creeper;
	public boolean blaze;
	public boolean cavespider;
	public boolean enderdragon;
	public boolean witherskeleton;
	public boolean wither;
	public boolean witch;
	public boolean ghast;
	public boolean magmacube;
	public boolean pigzombie;
	public boolean giant;
	public boolean zombie;
	public boolean skeleton;
	public boolean spider;
	public boolean enderman;
	public boolean slime;
	public boolean silverfish;

	public boolean payHunt;
	public boolean allowPvp;

	public float bowModifier;
	public float enchantmentModifier;
	public float huntPot;

	public int firstThresh;
	public int secondThresh;
	public int thirdThresh;
	public int fourthThresh;

	//config settings
	public int startTime;
	public int endTime;
	public int announceDelay; //3mins
	public int huntSize;
	public int deathPenalty;
	public int skipdays;
	public int skipDayChance;
	public int pvpChance;
	//public float xpModifier;

	public String worldName;
	public String hunts;
	public List<Integer> enchantRange;
	public List<Integer> bowRange;
	//public String xpRange;

	public boolean removeSpawnerMobs;
	public boolean allowLateHunters;
	public boolean denySpawnerMobs;
	public boolean outsideMobsOnly;
	public boolean rewardRunnersUp;
	public boolean randomhunts;
	public boolean randomspawn;
	//public boolean xpforpoint;
	public float cost;
	public int payChance;


	//area
	public boolean useArea;
	public boolean autoTeleport;
	public int spawnx;
	public int spawny;
	public int spawnz;
	public int Xmin;
	public int Xmax;
	public int Zmin;
	public int Zmax;

	public settings(MobHunt p, String world)
	{
		//this.resetSettings();
		this.config = loadConfig(p,world);
		this.loadSettings(p, world);
	}

	public YamlConfiguration loadConfig(MobHunt p, String world) {
		File f = new File(p.getDataFolder(), world+"-hunt.yml");
	    if (!f.exists())
	      try {
	        f.createNewFile();
	        return YamlConfiguration.loadConfiguration(f);
	      } catch (Exception localException) {
	      }
	    return YamlConfiguration.loadConfiguration(f);
	}

	public void updateTheshholds(int first, int second, int third, int fourth) {
		this.firstThresh = first;
		this.secondThresh = second;
		this.thirdThresh = third;
		this.fourthThresh = fourth;
	}

	public void update(String hunt, Economy econ) {
		this.hunts = hunt;
		this.all = hunts.toLowerCase().contains("all");
		this.creeper = hunts.toLowerCase().contains("creeper");
		this.zombie = hunts.toLowerCase().contains("zombie");
		this.skeleton = hunts.toLowerCase().contains("skeleton");
		this.spider = hunts.toLowerCase().contains("spider");
		this.enderman = hunts.toLowerCase().contains("enderman");
		this.slime = hunts.toLowerCase().contains("slime");
		this.silverfish = hunts.toLowerCase().contains("silverfish");
		this.blaze  = hunts.toLowerCase().contains("blaze");
		this.cavespider  = hunts.toLowerCase().contains("vesspid");
		this.enderdragon  = hunts.toLowerCase().contains("dragon");
		this.witherskeleton  = hunts.toLowerCase().contains("itherske");
		this.wither = hunts.toLowerCase().contains("boss");
		this.witch = hunts.toLowerCase().contains("witch");
		this.ghast = hunts.toLowerCase().contains("ghast");
		this.magmacube = hunts.toLowerCase().contains("magmacube");
		this.pigzombie = hunts.toLowerCase().contains("igzomb");
		this.giant = hunts.toLowerCase().contains("giant");
		//config.xpforpoint = config.hunts.toLowerCase().contains("xp");
		if(!this.payHunt && (econ != null))
			this.payHunt = this.hunts.toLowerCase().contains("pay");
		if(!this.useArea)
			this.useArea = this.hunts.toLowerCase().contains("area");
		if(!this.allowPvp)
			this.allowPvp = this.hunts.toLowerCase().contains("pvp");
	}

	public void resetSettings() {
		this.firstThresh = 1000;
		this.secondThresh = 750;
		this.thirdThresh = 500;
		this.fourthThresh = 200;

		//this.worldName = "";

		this.hunts = "ALL";
		this.all = false;
		this.creeper = false;
		this.zombie = false;
		this.skeleton = false;
		this.spider = false;
		this.enderman = false;
		this.slime = false;
		this.silverfish = false;

		this.blaze  = false;
		this.cavespider  = false;
		this.enderdragon  = false;
		this.witherskeleton  = false;
		this.wither = false;
		this.witch = false;
		this.ghast = false;
		this.magmacube = false;
		this.pigzombie = false;
		this.giant = false;

		this.allowPvp = true;

		this.payHunt = false;
		this.huntPot = 0;

		//reset AreaHunt
		this.useArea = this.config.getBoolean("Hunt.useArea", false);
		//reset Skipdays.
		this.skipdays = this.config.getInt("Hunt.SkipDays", 0);
	}

	public void loadSettings(MobHunt p, String world)
	{
		this.worldName = world;

		this.startTime = config.getInt("Hunt.StartTime", 14000);
		this.endTime = config.getInt("Hunt.EndTime", 23800);
			if(this.endTime >= 24000)
				this.endTime = 23800;
			if(this.endTime < this.startTime)
			   {this.startTime = 14000; this.endTime = 23800;}
		this.announceDelay = config.getInt("Hunt.AnnounceDelay", 240)*20;
		this.huntSize = config.getInt("Hunt.MinimumHunters", 3);

		this.deathPenalty = config.getInt("Hunt.DeathPenalty", 10);
		this.skipdays = config.getInt("Hunt.SkipDays", 0);
		this.skipDayChance = config.getInt("Hunt.SkipDayChance", 0);
		//this.bowchance = config.getInt("Hunt.BowChance",50);
		//this.enchantmentChance = config.getInt("Hunt.EnchantmentChance",10);

		this.bowRange = config.getIntegerList("Hunt.BowModifierRange");
		this.enchantRange = config.getIntegerList("Hunt.EnchantmentModifierRange");
		//this.xpRange = config.getString("Hunt.XpPointsMoidifier","10-50");

		this.removeSpawnerMobs = config.getBoolean("Hunt.RemoveSpawnerMobs", true);
		this.allowLateHunters = config.getBoolean("Hunt.AllowLateHunters", true);
		this.denySpawnerMobs = config.getBoolean("Hunt.DenySpawnerMobs", true);
		this.outsideMobsOnly = config.getBoolean("Hunt.OutsideMobsOnly", true);
		this.rewardRunnersUp = config.getBoolean("Hunt.RewardRunnersUp", true);
		this.randomhunts = config.getBoolean("Hunt.RandomHunts", true);
		this.randomspawn = config.getBoolean("Hunt.RandomSpawn", true);

		this.cost = config.getInt("Hunt.Cost", 10);
		this.payChance = config.getInt("Hunt.PayChance", 25);

		this.pvpChance = config.getInt("Hunt.PvpChance", 10);

		this.useArea = config.getBoolean("Hunt.useArea", false);
		this.autoTeleport = config.getBoolean("Hunt.AutoTeleport", true);
		//get Area Spawn
		List<Integer> coords = config.getIntegerList("Hunt.AreaSpawn");
		//p.fLogger.info(this.worldName);
		//p.fLogger.info(coords.toString());
		this.spawnx = coords.get(0);
		this.spawny = coords.get(1);
		this.spawnz = coords.get(2);

		//this.Xmax = config.getInt("Hunt.Area.xmax",10);
		//this.Zmax = config.getInt("Hunt.Area.zmax",10);
		//this.Xmin = config.getInt("Hunt.Area.xmin",-10);
		//this.Zmin = config.getInt("Hunt.Area.zmin",-10);

		checkArea(p);
	}

	public void checkArea(MobHunt p){
		int x;
		int z;
		PluginDescriptionFile pdfFile = p.getDescription();
		if(Xmin > Xmax)	{
			p.fLogger.warning("["+pdfFile.getName()+"] Area: Xmin > Xmax... Swapping values");
			x = Xmax;
			Xmax = Xmin;
			Xmin = x;
		}

		if(Zmin > Zmax ) {
			p.fLogger.warning("["+pdfFile.getName()+"] Area: Zmin > Zmax... Swapping values");
			z = Zmax;
			Zmax = Zmin;
			Zmin = z;
		}
	}

	public void saveArea(MobHunt p) {
		this.checkArea(p);
		config.set("Hunt.Area.xmax", Xmax);
		config.set("Hunt.Area.xmin", Xmin);
		config.set("Hunt.Area.zmax", Zmax);
		config.set("Hunt.Area.zmin", Zmin);
		File file = new File(p.getDataFolder(), "Hunt-hunt.yml");
		try {
			this.config.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setEnchantmentModifier() {
		int emin = enchantRange.get(0);
		int emax = enchantRange.get(1);
		int enchantchance = new Random().nextInt((emax)-(emin))+emin;
		enchantmentModifier = (float)enchantchance / 100;
	}

	public void setBowModifier() {
		int bmin = bowRange.get(0);
		int bmax = bowRange.get(1);
		int bowchance = new Random().nextInt((bmax)-(bmin))+bmin;
		bowModifier = (float)bowchance / 100;
	}

	public void getRandomHunt(Economy econ) {
		List<String> hunts = config.getStringList("Hunt.Hunts");
		int i = new Random().nextInt(hunts.size());
		this.resetSettings();
		this.setThresholds(i);
		//get the next hunt
		update(hunts.get(i), econ);
		//fPluginInstance.getServer().broadcastMessage("XP:"+setting.xpforpoint);
		//fPluginInstance.getServer().broadcastMessage("modifier:"+setting.xpModifier);
	}

	public void getSingleHunt() {
		this.all = true;
		this.hunts = "All";
		//get thresholds this (i) hunt
		this.setThresholds(0);

	}

	private void setThresholds(int hunt) {
		List<String> thresholds = new ArrayList<String>();
		thresholds = config.getStringList("Hunt.Thresholds");
		if(this.randomhunts) {
			if(thresholds.size() > 0) {
				String thresh = thresholds.get(hunt);
				thresh = thresh.replaceAll(" ","");
				String[] threshs = thresh.split(",");
				if(threshs.length > 0) {
					this.updateTheshholds(Integer.parseInt(threshs[0]),
							Integer.parseInt(threshs[1]),
							Integer.parseInt(threshs[2]),
							Integer.parseInt(threshs[3]));
				}
			}
		} else {
			if(thresholds.size() > 0) {
				String thresh = thresholds.get(hunt);
				thresh = thresh.replaceAll(" ","");
				String[] threshs = thresh.split(",");
								this.updateTheshholds(Integer.parseInt(threshs[0]),
						Integer.parseInt(threshs[1]),
						Integer.parseInt(threshs[2]),
						Integer.parseInt(threshs[3]));
			}
		}
	}

	public void formatHuntString() {
		this.hunts = this.hunts.replaceAll(",","");
		this.hunts = this.hunts.replaceAll("(?i)pay", "").replaceAll("(?i)pvp","").replaceAll("(?i)area","").replaceAll("(?i)xp","").trim();
		this.hunts = this.hunts.replaceAll(" ", ",");
	}
}
