package darksky.ogameapidatabasefx.database.databasemanagement;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import darksky.ogameapidatabasefx.database.entities.EntityReader;
import darksky.ogameapidatabasefx.database.logs.LogReader;
import darksky.ogameapidatabasefx.database.statistics.StatisticsReader;

/**
 * Manages a database
 * 
 * @author darksky
 *
 */
public class OGameAPIDatabase {

	private static Logger logger = Logger.getLogger(OGameAPIDatabase.class.getName());

	public static final int UPDATEINTERVAL_SERVERDATA_DAYS = 1;
	public static final int UPDATEINTERVAL_PLAYERS_DAYS = 1;
	public static final int UPDATEINTERVAL_UNIVERSE_DAYS = 7;
	public static final int UPDATEINTERVAL_ALLIANCES_DAYS = 1;
	public static final int UPDATEINTERVAL_HIGHSCORE_HOURS = 1;

	public static final Calendar serverCalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
	
	private static final String PROTOCOL = "https";

	private DatabaseCreator creator = null;
	private DatabaseUpdater updater = null;
	//private DatabasePatcher patcher = null;
	private IDatabase database = null;

	private EntityReader entityReader = null;
	private LogReader logReader = null;
	private StatisticsReader statisticsReader = null;

	private static ConnectionSettings connectionSettings = new ConnectionSettings(20, 60, 5);

	/*
	 * the full server name, e.g. "s1-de.ogame.gameforge.com"
	 */
	private String fullServerName = null;

	/*
	 * the server prefix, e.g. "s1-de"
	 */
	private String serverPrefix = null;

	/**
	 * Creates a OGameAPIDatabase. To create the database tables, call
	 * createDatabaseTables()
	 * 
	 * @param serverPrefix
	 *            the server prefix of the database, e.g "s1-de"
	 * @param databaseLocation
	 *            the databaseLocation. the path of the folder containing the
	 *            database file
	 * @param loginName
	 *            the login name for the database
	 * @param loginPassword
	 *            the login password for the database
	 * @exception IllegalArgumentException
	 *                if serverPrefix does not match ^s[0-9]+-[a-zA-Z]+
	 */
	public OGameAPIDatabase(final String serverPrefix, final String databaseLocation, final String loginName,
			final String loginPassword) throws IllegalArgumentException {
		Objects.requireNonNull(serverPrefix);
		Objects.requireNonNull(databaseLocation);

		if (Pattern.matches("^s[0-9]+-[a-zA-Z]+", serverPrefix)) {
			this.serverPrefix = serverPrefix;
			fullServerName = serverPrefix + ".ogame.gameforge.com";

			database = new SQLiteDatabase();
			updater = new DatabaseUpdater(this);
			//patcher = new DatabasePatcher(this);
			creator = new DatabaseCreator(this);

			this.setDatabaseLocation(databaseLocation);
			this.setDatabaseName(serverPrefix);
			this.setLoginName(loginName);
			this.setLoginPassword(loginPassword);

		} else {
			throw new IllegalArgumentException("Illegal server prefix : " + serverPrefix);
		}
	}

	public void applyDatabaseSettings(DatabaseSettings databaseSettings) throws SQLException {
		logger.fine(getServerPrefix() + " applyDatabaseSettings");
		Objects.requireNonNull(databaseSettings, "databaseSettings is null");
		getEntityReader().writeDatabaseSettings(databaseSettings);
	}

	/**
	 * Deletes the database
	 * 
	 * @throws Exception
	 */
	public void deleteDatabase() throws Exception {
		logger.fine(getServerPrefix() + " deleteDatabase");
		database.deleteDatabase();
	}

	/**
	 * Creates the database tables
	 * 
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public void createDatabase(DatabaseSettings databaseSettings) throws SQLException {
		logger.fine(getServerPrefix() + " createDatabase");
		creator.createDatabaseTables(databaseSettings);
	}

	/**
	 * Get the full server name, e.g. "s1-de.ogame.gameforge.com"
	 * 
	 * @return the full server name
	 */
	public String getFullServerName() {
		return fullServerName;
	}

	/**
	 * Get the server prefix, e.g. "s1-de"
	 * 
	 * @return the server prefix
	 */
	public String getServerPrefix() {
		return serverPrefix;
	}

	/**
	 * Get a database connection
	 * 
	 * @return a direct connection to the underlying database
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public Connection getDatabaseConnection() throws SQLException {
		logger.fine(getServerPrefix() + " getDatabaseConnection");
		return database.getDatabaseConnection();
	}

	/**
	 * Closes the database connection
	 */
	public void closeDatabaseConnection() {
		logger.fine(getServerPrefix() + " closeDatabaseConnection");
		try {
			database.closeDatabaseConnection(database.getDatabaseConnection());
		} catch (SQLException e) {
			logger.log(Level.SEVERE, getServerPrefix() + " error getting database connection to close it", e);
		}
	}

	/**
	 * Generates a list of every server prefix available in the country of the
	 * current server.
	 * 
	 * @param serverPrefix
	 * @return A list of server prefixes
	 * @throws SAXException
	 *             if the OGame API file containing the server names could not
	 *             be parsed
	 * @throws IOException
	 *             if the OGame API file could not be read from the server
	 */
	public static List<String> getServerPrefixList(String serverPrefix) throws SAXException, IOException {
		logger.fine("getServerPrefixList("+serverPrefix+")");
		final String xmlPath = PROTOCOL+"://" + serverPrefix + ".ogame.gameforge.com" + DatabaseUpdater.PATH_UNIVERSES;

		// Parse XML file into Document
		Document document = getDocumentFromXMLurl(xmlPath);

		ArrayList<String> returnList = new ArrayList<String>();

		if (document != null) {
			Node head = document.getFirstChild();

			Node n;
			if (head.getFirstChild() != null) {
				n = head.getFirstChild();
			} else {
				logger.info("getServerPrefixList("+serverPrefix+") : no servers in universes list");
				return returnList;
			}

			// Generate list of server names
			String serverurl;
			do {
				serverurl = n.getAttributes().getNamedItem("href").getNodeValue();
				returnList.add(serverurl.substring(serverurl.lastIndexOf('/') + 1, serverurl.indexOf('.')));
				n = n.getNextSibling();
			} while (n != null);
		}

		return returnList;

	}

	/**
	 * Set the database location
	 * 
	 * @param location
	 */
	public void setDatabaseLocation(String location) {
		logger.fine(getServerPrefix() + " setDatabaseLocation("+location+")");
		Objects.requireNonNull(location);
		database.setDatabaseLocation(location);
	}

	/**
	 * Set the database name
	 * 
	 * @param name
	 */
	public void setDatabaseName(String name) {
		Objects.requireNonNull(name);
		database.setDatabaseName(name);
	}

	/**
	 * Set the login name
	 * 
	 * @param loginName
	 */
	public void setLoginName(String loginName) {
		Objects.requireNonNull(loginName);
		database.setLoginName(loginName);
	}

	/**
	 * Set the login password
	 * 
	 * @param loginPassword
	 */
	public void setLoginPassword(String loginPassword) {
		Objects.requireNonNull(loginPassword);
		database.setLoginPassword(loginPassword);
	}

	public String getDatabaseLocation() {
		return database.getDatabaseLocation();
	}

	public String getDatabaseName() {
		return database.getDatabaseName();
	}

	public String getLoginName() {
		return database.getLoginName();
	}

	public String getLoginPassword() {
		return database.getLoginPassword();
	}

	public DatabaseUpdater getDatabaseUpdater() {
		return updater;
	}

	/**
	 * Get the EntityReader of the current database
	 * 
	 * @return entitiy reader
	 */
	public EntityReader getEntityReader() {
		if (entityReader == null)
			entityReader = new EntityReader(this);
		return entityReader;
	}

	/**
	 * Get LogReader of the current database.
	 * 
	 * @return a log reader
	 */
	public LogReader getLogReader() {
		if (logReader == null)
			logReader = new LogReader(this);
		return logReader;
	}

	/**
	 * Get StatisticsReader of the current database.
	 * 
	 * @return a statistics reader
	 */
	public StatisticsReader getStatisticsReader() {
		if (statisticsReader == null)
			statisticsReader = new StatisticsReader(this);
		return statisticsReader;
	}

	public static void setConnectionSettings(ConnectionSettings settings) {
		logger.fine("setConnectionSettings("+settings+")");
		connectionSettings = Objects.requireNonNull(settings, "connectionSettings is null");
	}

	static Document getDocumentFromXMLurl(final String xmlURL) throws SAXException, IOException {
		logger.fine("getDocumentFromXMLurl(" + xmlURL +")");
		
		URL url;
		HttpURLConnection ucon;
		int count = 0;		

		final int retries = connectionSettings.getRetries();

		while (true) {
			if (count + 1 == retries) {
				logger.warning("get xml file " + xmlURL + " : try " + retries + " / " + retries);
			}
			try {
				url = new URL(xmlURL);
				ucon = (HttpURLConnection) url.openConnection();
				ucon.setConnectTimeout(connectionSettings.getConnectTimeoutSec() * 1000);
				ucon.setReadTimeout(connectionSettings.getReadTimeoutSec() * 1000);
				try (BufferedInputStream bis = new BufferedInputStream(ucon.getInputStream())) {
					Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bis);
					return document;
				}
			} catch (ParserConfigurationException ex) {
				if (++count == retries) {
					logger.severe("get xml file " + xmlURL + " : ParserConfigurationException - return null");
					return null;
				}
			} catch (IOException | SAXException ex) {
				if (++count == retries) {
					logger.log(Level.SEVERE, "get xml file " + xmlURL + " : Exception", ex);
					throw ex;
				}
			}
		}

	}

	static

	{

			Logger databaselogger = Logger.getLogger("darksky.ogameapidatabasefx.database");
			databaselogger.setLevel(Level.ALL);
			databaselogger.setUseParentHandlers(false);

			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.INFO);
			databaselogger.addHandler(consoleHandler);

			try {
				final String folder = "logfiles";
				Files.createDirectories(Paths.get(folder));

				Handler fileHandler = new FileHandler(folder + File.separatorChar + "database.log", 50000, 2, true);
				fileHandler.setFormatter(new Formatter() {

					@Override
					public String format(LogRecord record) {
						String output = "";
						LocalDateTime ldt = LocalDateTime.ofEpochSecond(record.getMillis() / 1000, 0, ZoneOffset.UTC);
						output += ldt.toString() + " - ";
						output += record.getLevel() + ": ";
						output += record.getMessage() + System.getProperty("line.separator");
						return output;
					}
				});
				fileHandler.setLevel(Level.INFO);
				databaselogger.addHandler(fileHandler);

				Handler fileHandler2 = new FileHandler(folder + File.separatorChar + "error.log", 50000, 2, true);
				fileHandler2.setFormatter(new SimpleFormatter());
				fileHandler2.setLevel(Level.WARNING);
				databaselogger.addHandler(fileHandler2);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


	}

}
