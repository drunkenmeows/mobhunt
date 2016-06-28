package me.drunkenmeows.mobhunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class mhReward {
	public static mhReward instance;
	private MobHunt plugin = null;
	
	public HashMap<String, String> enchantnames = new HashMap<String, String>();

	
	public mhReward(MobHunt p) {
		this.plugin = p;
		initEnchantmentLists();
		//instance = this;
	}
	
	public static mhReward get(MobHunt p)
	{
	    if (instance == null) {
	    	instance = new mhReward(p);
	    }
	    
	    return instance;
	}
	
	public void initEnchantmentLists() {
			//setup enchantment common name, values			
			enchantnames.put("sharpness","DAMAGE_ALL");		
			enchantnames.put("baneofarthropods","DAMAGE_ARTHROPODS");
			enchantnames.put("smite","DAMAGE_UNDEAD");
			enchantnames.put("efficiency","DIG_SPEED");
			enchantnames.put("unbreaking","DURABILITY");
			enchantnames.put("fireaspect","FIRE_ASPECT");
			enchantnames.put("knockback","KNOCKBACK");
			enchantnames.put("fortune","LOOT_BONUS_BLOCKS");
			enchantnames.put("looting","LOOT_BONUS_MOBS");
			enchantnames.put("respiration","OXYGEN");
			enchantnames.put("protection","PROTECTION_ENVIRONMENTAL");
			enchantnames.put("blastprotection","PROTECTION_EXPLOSIONS");
			enchantnames.put("projectileprotection","PROTECTION_FALL");
			enchantnames.put("fireprotection","PROTECTION_FIRE");
			enchantnames.put("sharpness","PROTECTION_PROJECTILE");
			enchantnames.put("silktouch","SILK_TOUCH");
			enchantnames.put("aquainfinity","WATER_WORKER");
			enchantnames.put("bowflame","ARROW_FIRE");
			enchantnames.put("bowpower","ARROW_DAMAGE");
			enchantnames.put("bowpunch","ARROW_KNOCKBACK");
			enchantnames.put("bowinfinity","ARROW_INFINITE");
		}
	
	//FIXME 100% roll for testing
	public boolean rollthedice(int chance) 
	{
		int roll = (new Random().nextInt(99)+1);
		
		return (roll <= chance);
		//return true;
	}
	
	public float getRangeValue(float min, float max) {
		return Math.round(new Random().nextFloat() * (max - min) + min);
	}
	
	private List<String> parseItemString(Player p, int rewards, String type, final YamlConfiguration config) {
		
		switch(rewards) {
			case 1: {
				p.sendMessage(mhMessages.get().get("firstRewards"));
				return config.getStringList(type+".Rewards.First.Items");
			}
			case 2: {
				p.sendMessage(mhMessages.get().get("secondRewards"));
				return config.getStringList(type+".Rewards.Second.Items");
			}
			case 3: {
				p.sendMessage(mhMessages.get().get("thirdRewards"));
				return config.getStringList(type+".Rewards.Third.Items");
			}
			case 4: {
				p.sendMessage(mhMessages.get().get("fourthRewards"));
				return config.getStringList(type+".Rewards.RunnersUp.Items");
			} 
			default: {
				return new ArrayList<String>();
			}
				
		}
	}
	
	public int getRewardPlace(int score, ArrayList<Integer> huntThresholds, int place)
	{
		if(score >= huntThresholds.get(0) && place == 1) {
			return 1;
		} else if (score >= huntThresholds.get(1) && place < 3){
			return 2;
		} else if (score >= huntThresholds.get(2) && place < 4 ){
			return 3;
		} else if (score >= huntThresholds.get(3) && place <= 4)	{
			return 4;
		} else
			return 5;
	}
		
	public void rewardPlayer(Player p, int reward, String type, final YamlConfiguration  config) {
			
		List<String> items = parseItemString(p, reward, type, config);
		
		for(String i : items) {
			String[] item = i.split(";");
			
			if(i.contains("$") && plugin.fEconomy != null) {
				//process money reward
				this.giveMoney(item, p);
			}
			
			if(item.length >= 4) {
				//process normal item reward
				this.giveItemStack(item, p);
			}
		}
	}
	
	public void giveItemStack(String[] item, Player p) {
		if(this.rollthedice(Integer.parseInt(item[item.length-1]))) {
			HashMap<Integer, ItemStack> leftitems = new HashMap<Integer, ItemStack>();
			ItemStack itemstack = new ItemStack(Material.getMaterial(item[0]), Integer.parseInt(item[2]), Short.parseShort(item[1]));
			
			if(item.length == 5)
				itemstack = applyEnchantment(item[3], Integer.parseInt(item[4]), itemstack);
			
			leftitems = p.getInventory().addItem(itemstack);
			if(!leftitems.isEmpty()) {
				//drop items
				for(ItemStack i : leftitems.values()){
					p.getWorld().dropItemNaturally(p.getLocation(), i);
				}
			}
		}
	}
	
	public double givePotShare(Player p, int playercount, double pot, int reward) {
		
		double firstpot = pot/2;
		double secondpot = firstpot/2;
		double thirdpot = secondpot/2;
		double fourthpot = thirdpot;
		
		if(playercount > 3)
			fourthpot = thirdpot/(playercount-3);
		//FIXME messages
		switch(reward){
			case 1:
				//p.sendMessage(colourise(fPluginInstance.msg.msgPrefix+fPluginInstance.msg.get("",firstpot,fPluginInstance.msg.)));
				return firstpot;
			case 2:
				//p.sendMessage(colourise(fPluginInstance.msg.msgPrefix+fPluginInstance.msg.parse(secondpot,fPluginInstance.msg.secondPot)));
				return secondpot;
			case 3:
				//p.sendMessage(colourise(fPluginInstance.msg.msgPrefix+fPluginInstance.msg.parse(thirdpot,fPluginInstance.msg.thirdPot)));
				return thirdpot;
			case 4:
				//p.sendMessage(colourise(fPluginInstance.msg.msgPrefix+fPluginInstance.msg.parse(fourthpot,fPluginInstance.msg.fourthPot)));
				return fourthpot;
			default:
				return 0;
			}
	}
	
	public void giveMoney(String[] item, Player p) {
		if(this.rollthedice(Integer.parseInt(item[2]))) {
			float money = 0;
			if(item[1].contains("-")) {
				money = this.getRangeValue(Float.parseFloat(item[1].split("-")[0]),  Float.parseFloat(item[1].split("-")[1]));
			} else {
				money = Float.parseFloat(item[1]);
			}
			//give money to player
			plugin.fEconomy.depositPlayer(p.getName(), Math.abs(money));
			p.sendMessage(mhMessages.get().get("moneyRewards", money));
		}
	}
	
	public ItemStack applyEnchantment(String ench, int level, ItemStack item)
	{
		Enchantment e = Enchantment.getByName(this.enchantnames.get(ench.toLowerCase()));
		if(e != null)
			if(e.canEnchantItem(item) && level <= e.getMaxLevel())
			{
				item.addEnchantment(e, level);
				return item;
			}
		return item;
	}
}
