package darksky.ogameapidatabasefx.database.databasemanagement;

public class DatabaseSettings {

	private int databaseVersion = DatabaseCreator.DATABASE_VERSION;
	private boolean saveActivityStates = false;
	private boolean savePlanetDistribution = false;
	private boolean saveHighscoreDistribution = false;
	private int maxHighscoreEntriesPerEntity = 1;

	public DatabaseSettings(int databaseVersion, boolean saveActivityStates, boolean savePlanetDistribution,
			boolean saveHighscoreDistribution, int maxHighscoreDays) {
		super();
		if (maxHighscoreDays < 1) {
			throw new IllegalArgumentException("maxHighscoreDays < 1");
		}
		if (databaseVersion < 1) {
			throw new IllegalArgumentException("databaseVersion < 1");
		}
		this.databaseVersion = databaseVersion;
		this.saveActivityStates = saveActivityStates;
		this.savePlanetDistribution = savePlanetDistribution;
		this.saveHighscoreDistribution = saveHighscoreDistribution;
		this.maxHighscoreEntriesPerEntity = maxHighscoreDays;
	}

	public DatabaseSettings(boolean saveActivityStates, boolean savePlanetDistribution,
			boolean saveHighscoreDistribution, int maxHighscoreEntriesPerEntity) {
		if (maxHighscoreEntriesPerEntity < 1) {
			throw new IllegalArgumentException("maxHighscoreEntriesPerEntity < 1");
		}
		this.saveActivityStates = saveActivityStates;
		this.savePlanetDistribution = savePlanetDistribution;
		this.saveHighscoreDistribution = saveHighscoreDistribution;
		this.maxHighscoreEntriesPerEntity = maxHighscoreEntriesPerEntity;
	}

	public int getDatabaseVersion() {
		return databaseVersion;
	}

	void setDatabaseVersion(int databaseVersion) {
		if (databaseVersion > 0)
			this.databaseVersion = databaseVersion;
	}

	public boolean isSaveActivityStates() {
		return saveActivityStates;
	}

	public void setSaveActivityStates(boolean saveActivityStates) {
		this.saveActivityStates = saveActivityStates;
	}

	public boolean isSavePlanetDistribution() {
		return savePlanetDistribution;
	}

	public void setSavePlanetDistribution(boolean savePlanetDistribution) {
		this.savePlanetDistribution = savePlanetDistribution;
	}

	public boolean isSaveHighscoreDistribution() {
		return saveHighscoreDistribution;
	}

	public void setSaveHighscoreDistribution(boolean saveHighscoreDistribution) {
		this.saveHighscoreDistribution = saveHighscoreDistribution;
	}

	public int getMaxHighscoreEntriesPerEntity() {
		return maxHighscoreEntriesPerEntity;
	}

	public void setMaxHighscoreEntriesPerEntity(int maxHighscoreDays) {
		if (maxHighscoreDays > 0)
			this.maxHighscoreEntriesPerEntity = maxHighscoreDays;
	}

}
