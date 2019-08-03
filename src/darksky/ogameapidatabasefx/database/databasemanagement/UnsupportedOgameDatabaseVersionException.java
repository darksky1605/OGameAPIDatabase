package darksky.ogameapidatabasefx.database.databasemanagement;

/**
 * An exception indicating a wrong ogame database version
 * 
 * @author darksky
 *
 */
@SuppressWarnings("serial")
public class UnsupportedOgameDatabaseVersionException extends Exception {

	private int m_currentVersion = 0;
	private int m_requiredVersion = 0;

	public UnsupportedOgameDatabaseVersionException() {
		super();
	}

	public UnsupportedOgameDatabaseVersionException(String message) {
		super(message);
	}

	public UnsupportedOgameDatabaseVersionException(int currentVersion,
			int requiredVersion) {
		super();
		this.setCurrentVersion(currentVersion);
		this.setRequiredVersion(requiredVersion);
	}

	public int getCurrentVersion() {
		return m_currentVersion;
	}

	public void setCurrentVersion(int currentVersion) {
		m_currentVersion = currentVersion;
	}

	public int getRequiredVersion() {
		return m_requiredVersion;
	}

	public void setRequiredVersion(int requiredVersion) {
		m_requiredVersion = requiredVersion;
	}

}
