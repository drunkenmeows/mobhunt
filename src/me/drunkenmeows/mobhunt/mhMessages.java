package me.drunkenmeows.mobhunt;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public class mhMessages {
	
	public static mhMessages instance;
	public HashMap<String, String> messages = new HashMap<String, String>();
	
	public mhMessages(MobHunt p) {
		this.loadmsg(p);
	}
	
	public static mhMessages get() {
		/*if(instance == null) {
			instance = new mhMessages(p);
			return instance;
		} else {
			return instance;
		}*/
		return instance;
	}
	
	public void loadmsg(Plugin p)
	{

		messages.put("msgPrefix", p.getConfig().getString("Messages.Prefix", "&e[&cMobHunt&e] "));
		messages.put("outsideHunt",p.getConfig().getString("Messages.OutsideHunt", "&cYou are outside the huntarea!"));
		messages.put("huntSpawn",p.getConfig().getString("Messages.Huntspawn", "&aType /huntspawn to join the hunt area."));
		messages.put("enchantmentFail",p.getConfig().getString("Messages.EnchantmentFail", "&cNo points - Enchantment score chance &e[&f%i&e]"));
		messages.put("bowFail",p.getConfig().getString("Messages.BowFail", "&cNo points - Bow score chance &e[&f%i&e]"));
		messages.put("noGame",p.getConfig().getString("Message.NoGame", "&aThis world doesnt have a hunt!"));
		messages.put("denySpawnerMobs",p.getConfig().getString("Message.DenySpawnerMobs", "&cMob spawned from spawner!"));
		messages.put("outsideMobsOnly",p.getConfig().getString("Message.OutsideMobsOnly", "&cMob didn't spawn outside!"));
		messages.put("notinHunt",p.getConfig().getString("Message.NotInHunt", "&cMob &e[&f%s&e]&c not included in hunt"));
		
		messages.put("playerLeftMsg",p.getConfig().getString("Messages.PlayerLeftMsg","&aPlayer&f %s &aleft the hunt!"));
		messages.put("playerJoinMsg",p.getConfig().getString("Messages.PlayerJoinMsg","&aPlayer&f %s &ajoined the hunt!"));
		messages.put("playersRequired",p.getConfig().getString("Messages.PlayersRequired","&e[&f%i&e]&a hunters required for next hunt!"));
		messages.put("playerJoinFail",p.getConfig().getString("Messages.PlayerJoinFail","&cYou already joined the hunt!"));		
		messages.put("toLateToJoin",p.getConfig().getString("Messages.ToLateToJoin","&cToo Late, Hunt has already commenced!"));
		
		messages.put("leaveHunt",p.getConfig().getString("Messages.LeaveHunt","&cYou have forfeited the hunt!"));
		messages.put("leaveHuntFail",p.getConfig().getString("Messages.LeaveHuntFail","&cYou are not apart of the hunt!"));
		messages.put("deathMsg",p.getConfig().getString("Messages.DeathMsg","&cYou died! You lose &e[&f%i&e]&c points"));
		messages.put("gamemodeMsg",p.getConfig().getString("Messages.GamemodeMsg","&cYou cannot hunt in Creative mode!"));
		messages.put("huntPositions",p.getConfig().getString("Messages.HuntPositions","&f[&a%s&f][&e%s&f][&c%s&f]"));
		messages.put("huntPoints",p.getConfig().getString("Messages.HuntPoints","&f[&a%i&f][&e%i&f][&c%i&f]"));
		messages.put("huntKill",p.getConfig().getString("Messages.HuntKill","&aYou killed a &e[&f%s&e]&a for &e[&f%i&e]&a points."));
		messages.put("huntPlayerPos",p.getConfig().getString("Messages.HuntPlayerPos","&aYou are &e[&f%i&a/&c%i&e]&a with &e[&f%i&e]&a points."));
				
		messages.put("header",p.getConfig().getString("Messages.Huntinfo.Header","&f~~~~~~~~~~~&e[&fHunt Settings&e]&f~~~~~~~~~~~"));
		messages.put("payHunt",p.getConfig().getString("Messages.PayHunt.Mobs","&aPayHunt: &e[&f%s&e]&a Cost: &e[&f$%i&e]&a"));
		messages.put("mobs",p.getConfig().getString("Messages.Huntinfo.Mobs","&aMobs: &e[&f%s&e]&a"));
		messages.put("thresholds",p.getConfig().getString("Messages.Huntinfo.Thresholds:","&aThresholds &e[&f1st:%i&e][&f2nd:%i&e][&f3rd:%i&e][&fR:%i&e]"));
		messages.put("pvp",p.getConfig().getString("Messages.Huntinfo.PVP","&aPVP: &e[&f%s&e]&a - Death Penalty: &e[&f%i&e]"));
		messages.put("runnersupRewards",p.getConfig().getString("Messages.Huntinfo.RunnersupRewards","&aReward Runners-up: &e[&f%s&e]"));
		messages.put("bowChance",p.getConfig().getString("Messages.Huntinfo.BowModifier","&aBow: &e[&f%i% Score Modifier&e]"));
		messages.put("enchantmentsChance",p.getConfig().getString("Messages.Huntinfo.EnchantmentsModifier","&aEnchants: &e[&f%i% Score Modifier&e]"));
		
		messages.put("huntTimes",p.getConfig().getString("Messages.Huntinfo.HuntTimes","&aStart: &e[&f%i&e]&aticks - Finish: &e[&f%i&e]&aticks"));
		messages.put("huntLength",p.getConfig().getString("Messages.Huntinfo.HuntLength","&aHunt Length: &e[&f%i&e]&aminutes and &e[&f%i&e]&aseconds."));
		messages.put("footer",p.getConfig().getString("Messages.Huntinfo.Footer","&f~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"));
		
		messages.put("notEnoughPlayers",p.getConfig().getString("Messages.NotEnoughPlayers","&c****NOT ENOUGH PLAYERS****"));
		
		messages.put("nextHunt",p.getConfig().getString("Messages.NextHunt","&aNext hunt in &e[&f%i&e]&aminutes and &e[&f%i&e]&aseconds."));
		
		messages.put("huntActive",p.getConfig().getString("Messages.HuntActive","&aHunt is active!"));
		messages.put("huntWaiting",p.getConfig().getString("Messages.HuntWaiting","&aWaiting to commence the hunt!"));
		messages.put("huntDisabled",p.getConfig().getString("Messages.HuntDisabled","&cNo hunt tonight!"));
		messages.put("playersWaiting",p.getConfig().getString("Messages.PlayersWaiting","&e[&f%i&e]&a fGamePlayers are waiting."));
				
		messages.put("joinMsg",p.getConfig().getString("Messages.JoinMsg","&aType /joinhunt now to join."));
		messages.put("tooFewPlayers",p.getConfig().getString("Messages.TooFewPlayers","&cNo mob hunt tonight, too few fGamePlayers..."));
		messages.put("startMsg",p.getConfig().getString("Messages.StartMsg","&a*****START HUNTING*****&f"));
		messages.put("endMsg",p.getConfig().getString("Messages.EndMsg","&c*****THE HUNT IS OVER*****"));
		messages.put("startTimerMsg",p.getConfig().getString("Messages.StartTimerMsg","&aHUNT STARTING IN...&f %i"));
		messages.put("endTimerMsg",p.getConfig().getString("Messages.EndTimerMsg","&cHUNT ENDING IN...&f %i"));
		messages.put("skipdayMsg",p.getConfig().getString("Messages.SkipdayMsg","&aNext hunt in &e[&f%i&e]&adays"));
		
		messages.put("firstMsg",p.getConfig().getString("Messages.FirstMsg","&aFirst place is &f%s&a, with &e[&f%i&e] &apoints."));
		messages.put("secondMsg",p.getConfig().getString("Messages.SecondMsg","&aSecond place is &f%s&a, with &e[&f%i&e] &apoints."));
		messages.put("thirdMsg",p.getConfig().getString("Messages.ThirdMsg","&aThird place is &f%s&a, with &e[&f%i&e] &apoints."));
		messages.put("fourthMsg",p.getConfig().getString("Messages.FourthMsg","&aYou are a runner-up with &e[&f%i&e]&a points."));
				
		messages.put("firstFailMsg",p.getConfig().getString("Messages.FirstFailMsg","&f%s &adid not make 1st place threshold &e[&f%i&e]"));
		messages.put("secondFailMsg",p.getConfig().getString("Messages.SecondFailMsg","&f%s &adid not make 2nd place threshold &e[&f%i&e]"));
		messages.put("thirdFailMsg",p.getConfig().getString("Messages.ThirdFailMsg","&f%s &adid not make 3rd place threshold &e[&f%i&e]"));
		messages.put("noRewards",p.getConfig().getString("Messages.NoRewards","&cNo rewards! Score &e[&f%i&e]&c below threshold &e[&f%i&e]"));
		
		messages.put("firstRewards",p.getConfig().getString("Messages.FirstRewards","&aYou received &e[&fFirst&e] place rewards."));
		messages.put("secondRewards",p.getConfig().getString("Messages.SecondRewards","&aYou received &e[&fSecond&e] place rewards."));
		messages.put("thirdRewards", p.getConfig().getString("Messages.ThirdRewards","&aYou received &e[&fThird&e] place rewards."));
		messages.put("fourthRewards",p.getConfig().getString("Messages.FourthRewards","&aYou received &e[&fRunnerup&e] rewards."));
		
		messages.put("firstPot",p.getConfig().getString("Messages.FirstPot","&aYou received &e[&f$%i&e][&fFirst&e]&a place fPot."));
		messages.put("secondPot",p.getConfig().getString("Messages.SecondPot","&aYou received &e[&f$%i&e][&fSecond&e]&a place fPot."));
		messages.put("thirdPot", p.getConfig().getString("Messages.ThirdPot","&aYou received &e[&f$%i&e][&fThird&e]&a place fPot."));
		messages.put("fourthPot",p.getConfig().getString("Messages.FourthPot","&aYou received &e[&f$%i&e][&fRunnersUp&e]&a fPot"));
		
		messages.put("noRunnerupRewards",p.getConfig().getString("Messages.NoRunnerupRewards","&cNo runnerup rewards!"));
		messages.put("moneyRewards",p.getConfig().getString("Messages.MoneyRewards","&aYou have recieve a &e[&f$%i&e]&a money reward"));
				
		messages.put("reloadMsg",p.getConfig().getString("Messages.ReloadMsg","&aConfig reloaded!")); 
		messages.put("huntStatus",p.getConfig().getString("Messages.HuntStatus","&f~~~~~~~&e[&fHunt Status - %s&e]&f~~~~~~~"));
		messages.put("spawnFail",p.getConfig().getString("Messages.SpawnFail","&aYou are already inside the hunt area!"));
		
		messages.put("joinPaid",p.getConfig().getString("Messages.JoinPaid","&aYou paid &e[&f$%i&e]&a to join the next hunt"));
		messages.put("leavePaid",p.getConfig().getString("Messages.LeavePaid","&aYou recieved &e[&f$%i&e]&a back"));
		messages.put("noFunds",p.getConfig().getString("Messages.Nofunds","&cYou don't have enough money!"));
		
		messages.put("cmdtitle",p.getConfig().getString("Messages.Commands.Title","&e========[&cMobhunt %s&e]========"));
		messages.put("cmdjoin",p.getConfig().getString("Messages.Commands.Join","&a - Join a hunt."));
		messages.put("cmdleave",p.getConfig().getString("Messages.Commands.Leave","&a - leave a hunt."));
		messages.put("cmdinfo",p.getConfig().getString("Messages.Commands.Info","&a - Display current hunt information."));
		messages.put("cmdstatus",p.getConfig().getString("Messages.Commands.Status","&a - Display status of the hunt."));
		messages.put("cmdspawn",p.getConfig().getString("Messages.Commands.Spawn","&a - Teleport to the hunter area."));
		messages.put("cmdreload",p.getConfig().getString("Messages.Commands.Reload","&a - Reload mobhunt and it's config."));
		messages.put("cmdnext",p.getConfig().getString("Messages.Commands.Next","&a - Select a new hunt."));
	}
	
	public String get(String message) {
		return this.colourise(messages.get("msgPrefix")+messages.get(message));
	}
	
	public String get(String message, int i) {
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%i",""+i));
	}
	
	public String get(String message, String s)	{
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%s",s));
	}
	
	public String get(String message, long l1, long l2)	{
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%i",""+l1).replaceFirst("%i",""+l2));
	}
	
	public String get(String message, long l)	{
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%i",""+l));
	}
		
	public String get(String message, String s1, String s2, String s3)	{
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%s",s1).replaceFirst("%s",s2).replaceFirst("%s",s3));
	}
	
	public String get(String message, String s, float i) {
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%s",s).replaceFirst("%i",""+i));
	}
	
	public String get(String message, String s, int i) {
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%s",s).replaceFirst("%i",""+i));
	}
		
	public String get(String message, float i) {
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%i",""+i));
	}
	
	public String get(String message, int i1, int i2) {
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%i",""+i1).replaceFirst("%i",""+i2));
	}
	
	public String get(String message, int i1, int i2, int i3) {
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%i",""+i1).replaceFirst("%i",""+i2).replaceFirst("%i",""+i3));
	}
	
	public String get(String message, int i1, int i2, int i3, int i4) {	
		return this.colourise(messages.get("msgPrefix")+messages.get(message).replaceFirst("%i",""+i1).replaceFirst("%i",""+i2).replaceFirst("%i",""+i3).replaceFirst("%i",""+i4));
	}
	
	public String colourise(String msg)	{
	    return ChatColor.translateAlternateColorCodes('&',msg);
	}


}
