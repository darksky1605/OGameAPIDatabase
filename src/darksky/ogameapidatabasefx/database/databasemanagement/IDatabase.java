package darksky.ogameapidatabasefx.database.databasemanagement;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Dark Sky
 *
 */
interface IDatabase {

	Connection getDatabaseConnection() throws SQLException;

	void closeDatabaseConnection(Connection connection);

	void deleteDatabase() throws Exception;

	void setDatabaseLocation(String location);

	void setDatabaseName(String name);

	void setLoginName(String loginName);

	void setLoginPassword(String password);

	String getDatabaseLocation();

	String getDatabaseName();

	String getLoginName();

	String getLoginPassword();
}
