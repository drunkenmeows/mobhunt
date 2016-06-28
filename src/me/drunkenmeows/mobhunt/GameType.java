package me.drunkenmeows.mobhunt;

public enum GameType
{
	HUNT(1, "Hunt"),
	HUNGER(2, "Hunger"),
	ARENA(3, "Arena");

	private int theId;
	private String theName;

	GameType(int pId, String pName)
	{
		this.theId = pId;
		this.theName = pName;
	}

	public int getId() {
		return theId;
	}

	public String getName() {
		return theName;
	}

	public String getSimpleName() {
		return theName.toLowerCase();
	}
}
