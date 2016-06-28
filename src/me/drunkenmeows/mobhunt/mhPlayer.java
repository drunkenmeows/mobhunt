package me.drunkenmeows.mobhunt;

import org.bukkit.Location;

public class mhPlayer {
	private mhGame theGame = null;
	
	//stats
	private int thePlayed = 0;
	private int theKills = 0;
	private int theDeaths = 0;
	private int theScore = 0;
	private int theTotalPoints = 0;
	private int theHighScore = 0;
	private int theWins = 0;
	private int theLosses = 0;
	private int theFirst = 0;
	private int theSecond = 0;
	private int theThird = 0;
	private int theRunnerUp = 0;
	
	//info
	private Location thePreviousLocation = null;
	private int theReward = 5;
	private float theScoreMultiplier;
	
	public mhPlayer() {	}

	public mhGame getGame()
	{
		return theGame;
	}

	public void setGame( mhGame pGame )
	{
		theGame = pGame;
	}

	public int getPlayed()
	{
		return thePlayed;
	}

	public void setPlayed( int pPlayed )
	{
		thePlayed = pPlayed;
	}

	public int getKills()
	{
		return theKills;
	}

	public void setKills( int pKills )
	{
		theKills = pKills;
	}

	public int getDeaths()
	{
		return theDeaths;
	}

	public void setDeaths( int pDeaths )
	{
		theDeaths = pDeaths;
	}

	public int getScore()
	{
		return theScore;
	}

	public void setScore( int pScore )
	{
		theScore = pScore;
	}

	public int getTotalPoints()
	{
		return theTotalPoints;
	}

	public void setTotalPoints( int pTotalPoints )
	{
		theTotalPoints = pTotalPoints;
	}

	public int getHighScore()
	{
		return theHighScore;
	}

	public void setHighScore( int pHighScore )
	{
		theHighScore = pHighScore;
	}

	public int getWins()
	{
		return theWins;
	}

	public void setWins( int pWins )
	{
		theWins = pWins;
	}

	public int getLosses()
	{
		return theLosses;
	}

	public void setLosses( int pLosses )
	{
		theLosses = pLosses;
	}

	public int getFirst()
	{
		return theFirst;
	}

	public void setFirst( int pFirst )
	{
		theFirst = pFirst;
	}

	public int getSecond()
	{
		return theSecond;
	}

	public void setSecond( int pSecond )
	{
		theSecond = pSecond;
	}

	public int getThird()
	{
		return theThird;
	}

	public void setThird( int pThird )
	{
		theThird = pThird;
	}

	public int getRunnerUp()
	{
		return theRunnerUp;
	}

	public void setRunnerUp( int pRunnerUp )
	{
		theRunnerUp = pRunnerUp;
	}

	public Location getPreviousLocation()
	{
		return thePreviousLocation;
	}

	public void setPreviousLocation( Location pPreviousLocation )
	{
		thePreviousLocation = pPreviousLocation;
	}

	public int getReward()
	{
		return theReward;
	}

	public void setReward( int pReward )
	{
		theReward = pReward;
	}

	public float getScoreMultiplier()
	{
		return theScoreMultiplier;
	}

	public void setScoreMultiplier( float pScoreMultiplier )
	{
		theScoreMultiplier = pScoreMultiplier;
	}

	public void incrementKills()
	{
		this.theKills++;
	}

	public void incrementDeaths()
	{
		this.theDeaths++;
	}

	public void updateScore( int pScore )
	{
		this.theScore += pScore;
	}
}
