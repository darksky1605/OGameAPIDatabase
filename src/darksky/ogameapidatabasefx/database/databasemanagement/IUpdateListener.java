package darksky.ogameapidatabasefx.database.databasemanagement;

public interface IUpdateListener {
	
	public static int SERVERDATA = 0;
	public static int PLAYERS = 1;
	public static int ALLIANCES = 2;
	public static int UNIVERSE = 3;
	public static int HIGHSCORE = 4;
	
	/**
	 * Called when a partial update begins
	 * @param whatIsStarting
	 */
	public void updateOf(int updateOf);

}
