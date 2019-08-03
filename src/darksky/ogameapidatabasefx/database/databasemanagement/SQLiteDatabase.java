package darksky.ogameapidatabasefx.database.databasemanagement;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author darksky
 * 
 *         A simple wrapper class for a sqlite database using JDBC
 *
 */

class SQLiteDatabase implements IDatabase {

	private static Logger logger = Logger.getLogger(SQLiteDatabase.class.getName());

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.err.println("Error loading JDBC driver");
			e.printStackTrace();
			throw new RuntimeException("Error. Cannot load database driver");
		}
	}

	private Connection m_connection = null;
	private String m_databaseFolderPath = null;
	private String m_databaseFileName = null;

	SQLiteDatabase() {
	}

	@Override
	public void deleteDatabase() throws Exception {
		Path dbfilepath = Paths.get(m_databaseFolderPath, m_databaseFileName);
		Files.deleteIfExists(dbfilepath);
	}

	@Override
	public Connection getDatabaseConnection() throws SQLException {
		if (null == m_connection) {
			try {
				this.checkFile();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "database file does not exist and cannot be created", e);
				throw new SQLException("Database file does not exist and cannot be created");
			}
			m_connection = DriverManager
					.getConnection("jdbc:sqlite:" + Paths.get(m_databaseFolderPath, m_databaseFileName).toString());

		}
		return m_connection;
	}

	@Override
	public void closeDatabaseConnection(Connection connection) {
		if (null != m_connection && m_connection.equals(connection)) {
			try {
				m_connection.close();
				m_connection = null;
			} catch (SQLException e) {
				System.err.println("Could not close database connection.");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setDatabaseLocation(String location) {
		m_databaseFolderPath = Paths.get(location).toAbsolutePath().normalize().toString();
	}

	@Override
	public String getDatabaseLocation() {
		return m_databaseFolderPath;
	}

	@Override
	public void setLoginName(String loginName) {
	}

	@Override
	public String getLoginName() {
		return "";
	}

	@Override
	public void setLoginPassword(String password) {
	}

	@Override
	public String getLoginPassword() {
		return "";
	}

	@Override
	public void setDatabaseName(String name) {
		m_databaseFileName = Objects.requireNonNull(name);
	}

	@Override
	public String getDatabaseName() {
		return m_databaseFileName;
	}

	private void checkFile() throws IOException {
		try {
			Files.createDirectories(Paths.get(m_databaseFolderPath));

			Files.createFile(Paths.get(m_databaseFolderPath, m_databaseFileName));

		} catch (FileAlreadyExistsException e) {
		}
	}

}
