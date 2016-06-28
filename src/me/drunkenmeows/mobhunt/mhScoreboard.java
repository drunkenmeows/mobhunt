package me.drunkenmeows.mobhunt;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class mhScoreboard {
	
	public MobHunt plugin;
	public String name;
	public Scoreboard board = null;
	public Team team; 
	public Objective objective;

	
	public mhScoreboard(MobHunt plugin, String name, String prefix, String suffix, String displayname) {
		this.plugin = plugin;
		this.name = name;
		this.board = plugin.fScoreboadManager.getNewScoreboard();
		
		//scoreboards
		this.team = board.registerNewTeam(this.name);
		this.objective = board.registerNewObjective(this.name, "dummy");
				
		this.team.setPrefix(prefix);
		this.team.setSuffix(suffix);
		this.objective.setDisplayName(displayname);
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.team.setAllowFriendlyFire(true);
	}
	
	public void removePlayer(Player p) {
		p.setScoreboard(plugin.fScoreboadManager.getNewScoreboard());
		this.board.resetScores(p);
		this.team.removePlayer(p);	
	}
	
	public void updateScore(Player p, int score) {
		this.objective.getScore(p).setScore(score);
	}
	
	public void addPlayer(Player p) {
		
		this.team.addPlayer(p);
		p.setScoreboard(board);
		this.objective.getScore(p).setScore(0);
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	public int getScore(Player p) {
		return this.objective.getScore(p).getScore();
	}
	
	public void resetPlayers() {
		for(OfflinePlayer sp : this.board.getPlayers()){
				this.removePlayer((Player)sp);
		}
	}
}
