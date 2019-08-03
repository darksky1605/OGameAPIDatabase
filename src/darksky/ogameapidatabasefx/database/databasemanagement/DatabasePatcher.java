package darksky.ogameapidatabasefx.database.databasemanagement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import java.util.logging.Logger;

class DatabasePatcher {

    private final Logger logger = Logger.getLogger(DatabasePatcher.class.getName());

	private OGameAPIDatabase m_ogdb = null;
	private Connection m_dbcon = null;

	/**
	 * Constructor
	 * 
	 * @param ogdb
	 *            the OGameAPIDatabase that should be patched
	 */
	DatabasePatcher(OGameAPIDatabase ogdb) {
		m_ogdb = Objects.requireNonNull(ogdb);
	}

	/**
	 * Patches the database to the latest version
	 * 
	 * @throws SQLException
	 *             if a database error occurs
	 * @throws DatabasePatchException
	 *             if the current database version could not be determined
	 */
	void patchDatabase() throws SQLException, DatabasePatchException {
		/*
		DatabaseInformation databaseInformation = m_ogdb.getEntityReader().getDatabaseInformation();
		if (databaseInformation == null) {
			throw new DatabasePatchException("databaseInformation == null");
		}

		int databaseVersion = databaseInformation.getDatabaseVersion();

		if (databaseVersion == DatabaseCreator.DATABASE_VERSION) {
			return;
		}

		switch (databaseVersion) {
		case 1:
			patchV2();
		case 2: // update to v 3
			// and so on ...
		default:
			break;
		}*/
		
		m_dbcon = m_ogdb.getDatabaseConnection();
		
		DatabaseSettings dbsettings = m_ogdb.getEntityReader().getDatabaseSettings();
		if (dbsettings == null) {
			throw new DatabasePatchException("dbsettings == null");
		}		
        int databaseVersion = dbsettings.getDatabaseVersion();
        
        switch (databaseVersion) {
		case 1:
			//patchV2();
		case 2: // update to v3
			// and so on ...
		default:
			break;
		}
	}
	
	private void patchV2() throws SQLException{
/*        logger.info("patch database " + m_ogdb.getServerPrefix() + " to v2");
        
        DatabaseSettings dbsettings = m_ogdb.getEntityReader().getDatabaseSettings();
        
        DatabaseSettings newdbsettings = m_ogdb.getEntityReader().getDatabaseSettings();
        
        newdbsettings.setDatabaseVersion(2);
        
		final boolean oldAutoCommit = m_dbcon.getAutoCommit();
		m_dbcon.setAutoCommit(false);

		Statement stmt = m_dbcon.createStatement();

		SQLException ex = null;

		try {
		
            // ..... patch here
            
		} catch (SQLException e) {
			ex = e;
			m_dbcon.rollback();
			logger.warning("patch database " + m_ogdb.getDatabaseName() + " to v2 - rollback");
		} finally {
			m_dbcon.commit();
			m_dbcon.setAutoCommit(oldAutoCommit);
		}

		if (ex != null){
            logger.severe(ex.getMessage());
			throw ex;
        }
		logger.info("patch database " + m_ogdb.getServerPrefix() + " to v2 - done");  */ 
	}
	
}
