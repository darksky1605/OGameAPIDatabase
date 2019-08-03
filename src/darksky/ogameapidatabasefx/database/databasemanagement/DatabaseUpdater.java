package darksky.ogameapidatabasefx.database.databasemanagement;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import darksky.ogameapidatabasefx.database.entities.Timestamps;
import darksky.ogameapidatabasefx.database.databasemanagement.DatabasePatcher;

public class DatabaseUpdater {

	private final Logger logger = Logger.getLogger(DatabaseUpdater.class.getName());

	static final String PATH_PLAYERS = "/api/players.xml";
	static final String PATH_UNIVERSE = "/api/universe.xml";
	static final String PATH_ALLIANCES = "/api/alliances.xml";
	static final String PATH_PLAYERDATA = "/api/playerData.xml";
	static final String PATH_SERVERDATA = "/api/serverData.xml";
	static final String PATH_HIGHSCORE = "/api/highscore.xml";
	static final String PATH_UNIVERSES = "/api/universes.xml";
	
	private static final String PROTOCOL = "https";

	private static final String[] POINTS = { "totalPoints", "economyPoints", "researchPoints", "militaryPoints",
			"militaryLostPoints", "militaryBuiltPoints", "militaryDestroyedPoints", "honorPoints" };
	private static final String[] RANKS = { "totalRank", "economyRank", "researchRank", "militaryRank",
			"militaryLostRank", "militaryBuiltRank", "militaryDestroyedRank", "honorRank" };

	private static final String[] HIGHSCORE_TABLE_NAMES = { "", "playerHighscores", "allianceHighscores" };
	private static final String[] HIGHSCORE_KEYS = { "", "playerId", "allianceId" };

	private static final String[] SERVERDATA_COLUMNNAMES = { "domain", "name", "number", "language", "timezone",
			"timezoneOffset", "version", "speed", "speedFleet", "galaxies", "systems", "acs", "rapidFire", "defToTF",
			"debrisFactor", "repairFactor", "newbieProtectionLimit", "newbieProtectionHigh", "topScore", "bonusFields",
			"donutGalaxy", "donutSystem", "debrisFactorDef", "wfEnabled", "wfMinimumRessLost", "wfMinimumLossPercentage", 
			"wfBasicPercentageRepairable", "globalDeuteriumSaveFactor", "bashlimit", "probeCargo", "researchDurationDivisor", "darkMatterNewAcount", "cargoHyperspaceTechMultiplier" };

	static {
		// sort for binary search later on
		Arrays.sort(SERVERDATA_COLUMNNAMES);
	}

	private OGameAPIDatabase ogdb = null;
	private List<IUpdateListener> updateListeners = null;

	DatabaseUpdater(OGameAPIDatabase ogdb) {
		this.ogdb = Objects.requireNonNull(ogdb);
		updateListeners = new ArrayList<>();
	}

	/**
	 * Check if database has the database version needed for the update process
	 *
	 * @return True if it's the required database version. False if it is not
	 *         the required database version, or the database version could not
	 *         be determined.
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public boolean isRequiredDatabaseVersion() throws SQLException {
		DatabaseSettings settings = ogdb.getEntityReader().getDatabaseSettings();
		if (settings == null)
			return false;
		boolean result = isRequiredDatabaseVersion(settings);
		return result;
	}

	/**
	 * Adds an update listener. The listeners are called during updateDatabase()
	 * before updating a new database section
	 * 
	 * @param listener
	 *            the update listener
	 */
	public void addUpdateListener(IUpdateListener listener) {
		Objects.requireNonNull(listener);
		updateListeners.add(listener);
	}

	/**
	 * Removes every update listener
	 */
	public void removeUpdateListeners() {
		updateListeners.clear();
	}

	/**
	 * Updates the complete database.
	 * 
	 * @throws UnsupportedOgameDatabaseVersionException
	 *             if the used database version does not match the version
	 *             required for updating and could not be patched to this version.
	 * @throws IOException
	 *             if there is an error getting the OGame API files from the
	 *             server
	 * @throws SQLException
	 *             if a database error occurs
	 * @throws SAXException
	 *             if an OGame API file could not be parsed
	 * @throws ParserConfigurationException
	 *             java internal parser error
	 */
	public void updateDatabase() throws UnsupportedOgameDatabaseVersionException, IOException, SQLException,
			SAXException, ParserConfigurationException {

		logger.info(ogdb.getServerPrefix() + " updating database");

		//isRequiredDatabaseVersionOrEx();
		
		if(!isRequiredDatabaseVersion()){
            try{
                DatabasePatcher patcher = new DatabasePatcher(ogdb);
                patcher.patchDatabase();
            }catch(DatabasePatchException e){
                throw new UnsupportedOgameDatabaseVersionException(ogdb.getEntityReader().getDatabaseSettings().getDatabaseVersion(),
					DatabaseCreator.DATABASE_VERSION);
            }
		}

		Timestamps timestamps = ogdb.getEntityReader().getNewestTimestamps();
		final Instant updateTimestamp = Instant.now();

		try (PreparedStatement stmt = ogdb.getDatabaseConnection()
				.prepareStatement("INSERT INTO timestamps (updateTimestamp) VALUES (?);")) {
			stmt.setTimestamp(1, Timestamp.from(updateTimestamp));
			stmt.execute();
		}

		IOException updateIOExeption = null;
		SAXException updateSAXException = null;

		updateListeners.forEach(l -> l.updateOf(IUpdateListener.SERVERDATA));
		try {
			updateServerData(timestamps, updateTimestamp);
		} catch (IOException e) {
			updateIOExeption = e;
		} catch (SAXException e) {
			updateSAXException = e;
		}
		updateListeners.forEach(l -> l.updateOf(IUpdateListener.PLAYERS));
		try {
			updatePlayers(timestamps, updateTimestamp);
		} catch (IOException e) {
			updateIOExeption = e;
		} catch (SAXException e) {
			updateSAXException = e;
		}
		updateListeners.forEach(l -> l.updateOf(IUpdateListener.ALLIANCES));
		try {
			updateAlliances(timestamps, updateTimestamp);
		} catch (IOException e) {
			updateIOExeption = e;
		} catch (SAXException e) {
			updateSAXException = e;
		}
		updateListeners.forEach(l -> l.updateOf(IUpdateListener.UNIVERSE));
		try {
			updateUniverse(timestamps, updateTimestamp);
		} catch (IOException e) {
			updateIOExeption = e;
		} catch (SAXException e) {
			updateSAXException = e;
		}
		updateListeners.forEach(l -> l.updateOf(IUpdateListener.HIGHSCORE));
		// highscore updates need current players timestamp. could have
		// changed when updating players
		// timestamps = ogdb.getEntityReader().getNewestTimestamps();
		try {
			updateHighscore(timestamps, updateTimestamp);
		} catch (IOException e) {
			e.printStackTrace();
			updateIOExeption = e;
		} catch (SAXException e) {
			updateSAXException = e;
			e.printStackTrace();
		}

		if (updateIOExeption != null)
			throw updateIOExeption;
		if (updateSAXException != null)
			throw updateSAXException;

	}

	private void updateServerData(Timestamps timestamps, Instant updateBegin)
			throws SAXException, ParserConfigurationException, IOException, SQLException {

		logger.info(ogdb.getServerPrefix() + " updating serverdata");

		final String timestampColumnName = "serverDataTimestamp";

		final Instant nowTimestamp = Instant.now();
		final Instant currentServerDataTimestamp = timestamps.getServerDataTimestamp();
		final long updateIntervalSeconds = OGameAPIDatabase.UPDATEINTERVAL_SERVERDATA_DAYS * 24 * 60 * 60;
		final Instant noUpdateNeededBefore = currentServerDataTimestamp.plusSeconds(updateIntervalSeconds);
		
		//System.out.println(nowTimestamp + " " + currentServerDataTimestamp);

		if (nowTimestamp.isBefore(noUpdateNeededBefore)) {
			updateTimestampsTable(updateBegin, timestampColumnName, currentServerDataTimestamp);
			return; // no new data available
		}

		final String xmlPath = PROTOCOL + "://" + ogdb.getFullServerName() + PATH_SERVERDATA;

		final Document document = OGameAPIDatabase.getDocumentFromXMLurl(xmlPath);
		if (document == null) {
			return; // file could not be parsed. cannot update
		}

		final Node head = document.getFirstChild();

		final Instant fileTimestamp = Instant
				.ofEpochSecond(Long.parseLong(head.getAttributes().getNamedItem("timestamp").getNodeValue()));

		// no new data available
		if (!currentServerDataTimestamp.isBefore(fileTimestamp)) {
			updateTimestampsTable(updateBegin, timestampColumnName, currentServerDataTimestamp);
			return;
		}

		Node n;
		if (head.getFirstChild() != null) {
			n = head.getFirstChild();
		} else {
			logger.info(ogdb.getServerPrefix() + " serverdata xml contains no data");
			return;
		}

		String nodeName;
		String nodeText;
		int arrayIndex;
		
		Connection connection = ogdb.getDatabaseConnection();

		do {
			nodeName = n.getNodeName();
			nodeText = n.getTextContent();
			arrayIndex = Arrays.binarySearch(SERVERDATA_COLUMNNAMES, nodeName);

			if (arrayIndex < 0) {
				logger.warning("unsupported node name : " + nodeName + " , node text : " + nodeText);
			} else {

				try (PreparedStatement ps = connection
						.prepareStatement("UPDATE serverData SET " + nodeName + " = ?;");) {
					ps.setString(1, nodeText);
					ps.execute();
				}
			}

			n = n.getNextSibling();
		} while (n != null);

		updateTimestampsTable(updateBegin, timestampColumnName, fileTimestamp);
		timestamps.setServerDataTimestamp(fileTimestamp);
	}

	private void updatePlayers(Timestamps timestamps, Instant updateBegin)
			throws ParserConfigurationException, SAXException, IOException, SQLException {

		logger.info(ogdb.getServerPrefix() + " updating players");

		final String timestampColumnName = "playersTimestamp";

		final Instant nowTimestamp = Instant.now();
		final Instant currentPlayersTimestamp = timestamps.getPlayersTimestamp();
		final long updateIntervalSeconds = OGameAPIDatabase.UPDATEINTERVAL_PLAYERS_DAYS * 24 * 60 * 60;
		final Instant noUpdateNeededBefore = currentPlayersTimestamp.plusSeconds(updateIntervalSeconds);

		if (nowTimestamp.isBefore(noUpdateNeededBefore)) {
			updateTimestampsTable(updateBegin, timestampColumnName, currentPlayersTimestamp);
			return; // no new data available
		}

		final String xmlPath = PROTOCOL + "://" + ogdb.getFullServerName() + PATH_PLAYERS;

		final Document document = OGameAPIDatabase.getDocumentFromXMLurl(xmlPath);
		if (document == null) {
			return; // file could not be parsed. cannot update
		}

		final Node head = document.getFirstChild();

		final Instant fileTimestamp = Instant
				.ofEpochSecond(Long.parseLong(head.getAttributes().getNamedItem("timestamp").getNodeValue()));

		if (!currentPlayersTimestamp.isBefore(fileTimestamp)) {
			updateTimestampsTable(updateBegin, timestampColumnName, currentPlayersTimestamp);
			return; // no new data available
		}

		Node currentNode;
		if (head.getFirstChild() != null) {
			currentNode = head.getFirstChild();
		} else {
			logger.info(ogdb.getServerPrefix() + " players xml contains no data");
			return;
		}
		int playerID = 0;
		int allyID = 0;
		String statusString = null;
		String username = null;
		Node aIdNode = null;
		Node statusNode = null;
		int counter1 = 0;

		final int batchsize = 1000;
		
		Connection connection = ogdb.getDatabaseConnection();

		try (PreparedStatement updatePlayer = connection.prepareStatement(
				"UPDATE players SET playerName = ?, playerStatus = ?, allianceId = ?, lastUpdate = ? WHERE playerId = ?;");
				PreparedStatement insertPlayer = connection.prepareStatement(
						"INSERT OR IGNORE INTO players (playerId, playerName, playerStatus, allianceId, insertedOn, lastUpdate) "
								+ "VALUES (?, ?, ?, ?, ?, ?);");) {

			final Timestamp lastUpdatesqltimestamp = Timestamp.from(fileTimestamp);

			do {
				playerID = Integer.parseInt(currentNode.getAttributes().getNamedItem("id").getNodeValue());

				username = currentNode.getAttributes().getNamedItem("name").getNodeValue();

				aIdNode = currentNode.getAttributes().getNamedItem("alliance");
				if (aIdNode != null) {
					allyID = Integer.parseInt(aIdNode.getNodeValue());
				} else {
					allyID = DatabaseCreator.DEFAULT_ALLIANCE_ID;
				}

				statusNode = currentNode.getAttributes().getNamedItem("status");
				if (statusNode != null) {
					statusString = statusNode.getNodeValue();
				} else {
					statusString = "active";
				}

				updatePlayer.setString(1, username);
				updatePlayer.setString(2, statusString);
				updatePlayer.setInt(3, allyID);
				updatePlayer.setTimestamp(4, lastUpdatesqltimestamp);
				updatePlayer.setInt(5, playerID);
				updatePlayer.addBatch();

				insertPlayer.setInt(1, playerID);
				insertPlayer.setString(2, username);
				insertPlayer.setString(3, statusString);
				insertPlayer.setInt(4, allyID);
				insertPlayer.setTimestamp(5, lastUpdatesqltimestamp);
				insertPlayer.setTimestamp(6, lastUpdatesqltimestamp);
				insertPlayer.addBatch();

				if (++counter1 % batchsize == 0) {

					connection.setAutoCommit(false);

					updatePlayer.executeBatch();
					insertPlayer.executeBatch();

					connection.commit();

					connection.setAutoCommit(true);

				}
				currentNode = currentNode.getNextSibling();
			} while (currentNode != null);
			connection.setAutoCommit(false);

			updatePlayer.executeBatch();
			insertPlayer.executeBatch();

			connection.commit();

			connection.setAutoCommit(true);

		}
		updateTimestampsTable(updateBegin, timestampColumnName, fileTimestamp);
		timestamps.setPlayersTimestamp(fileTimestamp);
	}

	private void updateUniverse(Timestamps timestamps, Instant updateBegin)
			throws ParserConfigurationException, SAXException, IOException, SQLException {

		logger.info(ogdb.getServerPrefix() + " updating universe");

		final String timestampColumnName = "universeTimestamp";

		final Instant nowTimestamp = Instant.now();
		final Instant currentUniverseTimestamp = timestamps.getUniverseTimestamp();
		final long updateIntervalSeconds = OGameAPIDatabase.UPDATEINTERVAL_UNIVERSE_DAYS * 24 * 60 * 60;
		final Instant noUpdateNeededBefore = currentUniverseTimestamp.plusSeconds(updateIntervalSeconds);

		if (nowTimestamp.isBefore(noUpdateNeededBefore)) {
			updateTimestampsTable(updateBegin, timestampColumnName, currentUniverseTimestamp);
			return; // no new data available
		}

		final String xmlPath = PROTOCOL + "://" + ogdb.getFullServerName() + PATH_UNIVERSE;

		final Document document = OGameAPIDatabase.getDocumentFromXMLurl(xmlPath);
		if (document == null) {
			return; // file could not be parsed. cannot update
		}

		final Node head = document.getFirstChild();

		final Instant fileTimestamp = Instant
				.ofEpochSecond(Long.parseLong(head.getAttributes().getNamedItem("timestamp").getNodeValue()));

		if (!currentUniverseTimestamp.isBefore(fileTimestamp)) {
			updateTimestampsTable(updateBegin, timestampColumnName, currentUniverseTimestamp);
			return; // no new data available
		}

		Node currentNode;
		if (head.getFirstChild() != null) {
			currentNode = head.getFirstChild();
		} else {
			logger.info(ogdb.getServerPrefix() + " universe xml contains no data");
			return;
		}
		
		Connection connection = ogdb.getDatabaseConnection();

		try (PreparedStatement updatePlanets = connection.prepareStatement(
				"UPDATE planets SET planetName = ?, galaxy = ?, system = ?, position = ?, lastUpdate = ? WHERE planetId = ?;");
				PreparedStatement insertPlanets = connection
						.prepareStatement("INSERT OR IGNORE INTO planets VALUES (?,?,?,?,?,?,?,?);");
				PreparedStatement updateMoons = connection.prepareStatement(
						"UPDATE moons SET moonName = ?, moonSize = ?, lastUpdate = ? WHERE moonId = ?;");
				PreparedStatement insertMoons = connection
						.prepareStatement("INSERT OR IGNORE INTO moons VALUES (?,?,?,?,?,?,?);");) {

			int planetID = 0;
			int galaxy = 0;
			int system = 0;
			int position = 0;
			int moonID = 0;
			int playerID = 0;
			int counter1 = 0;
			int size = 0;

			final int batchsize = 1000;

			final Timestamp lastUpdatesqltimestamp = Timestamp.from(fileTimestamp);

			String planetname;
			String moonname;
			String coords;
			String[] splittedCoords;

			Node moonNode;

			do {
				// insert planet
				planetID = Integer.parseInt(currentNode.getAttributes().getNamedItem("id").getNodeValue());
				playerID = Integer.parseInt(currentNode.getAttributes().getNamedItem("player").getNodeValue());
				planetname = currentNode.getAttributes().getNamedItem("name").getNodeValue();
				coords = currentNode.getAttributes().getNamedItem("coords").getNodeValue();
				splittedCoords = coords.split(":");
				galaxy = Integer.parseInt(splittedCoords[0]);
				system = Integer.parseInt(splittedCoords[1]);
				position = Integer.parseInt(splittedCoords[2]);

				updatePlanets.setString(1, planetname);
				updatePlanets.setInt(2, galaxy);
				updatePlanets.setInt(3, system);
				updatePlanets.setInt(4, position);
				updatePlanets.setTimestamp(5, lastUpdatesqltimestamp, OGameAPIDatabase.serverCalendar);
				updatePlanets.setInt(6, planetID);
				updatePlanets.addBatch();

				insertPlanets.setInt(1, planetID);
				insertPlanets.setString(2, planetname);
				insertPlanets.setInt(3, galaxy);
				insertPlanets.setInt(4, system);
				insertPlanets.setInt(5, position);
				insertPlanets.setInt(6, playerID);
				insertPlanets.setTimestamp(7, lastUpdatesqltimestamp, OGameAPIDatabase.serverCalendar);
				insertPlanets.setTimestamp(8, lastUpdatesqltimestamp, OGameAPIDatabase.serverCalendar);
				insertPlanets.addBatch();

				// insert moon, if exists
				moonNode = currentNode.getFirstChild();
				if (moonNode != null) {
					moonID = Integer.parseInt(moonNode.getAttributes().getNamedItem("id").getNodeValue());
					moonname = moonNode.getAttributes().getNamedItem("name").getNodeValue();
					size = Integer.parseInt(moonNode.getAttributes().getNamedItem("size").getNodeValue());

					updateMoons.setString(1, moonname);
					updateMoons.setInt(2, size);
					updateMoons.setTimestamp(3, lastUpdatesqltimestamp, OGameAPIDatabase.serverCalendar);
					updateMoons.setInt(4, moonID);
					updateMoons.addBatch();

					insertMoons.setInt(1, moonID);
					insertMoons.setString(2, moonname);
					insertMoons.setInt(3, size);
					insertMoons.setInt(4, playerID);
					insertMoons.setInt(5, planetID);
					insertMoons.setTimestamp(6, lastUpdatesqltimestamp, OGameAPIDatabase.serverCalendar);
					insertMoons.setTimestamp(7, lastUpdatesqltimestamp, OGameAPIDatabase.serverCalendar);
					insertMoons.addBatch();

				}

				if (++counter1 % batchsize == 0) {
					connection.setAutoCommit(false);
					updatePlanets.executeBatch();
					insertPlanets.executeBatch();
					updateMoons.executeBatch();
					insertMoons.executeBatch();
					connection.commit();

					connection.setAutoCommit(true);
				}
				currentNode = currentNode.getNextSibling();

			} while (currentNode != null);

			connection.setAutoCommit(false);
			updatePlanets.executeBatch();
			insertPlanets.executeBatch();
			updateMoons.executeBatch();
			insertMoons.executeBatch();
			connection.commit();

			connection.setAutoCommit(true);

		}
		updateTimestampsTable(updateBegin, timestampColumnName, fileTimestamp);
		timestamps.setUniverseTimestamp(fileTimestamp);
	}

	private void updateAlliances(Timestamps timestamps, Instant updateBegin)
			throws SAXException, ParserConfigurationException, IOException, SQLException {

		logger.info(ogdb.getServerPrefix() + " updating alliances");

		final String timestampColumnName = "alliancesTimestamp";

		final Instant nowTimestamp = Instant.now();
		final Instant currentAlliancesTimestamp = timestamps.getAlliancesTimestamp();
		final long updateIntervalSeconds = OGameAPIDatabase.UPDATEINTERVAL_ALLIANCES_DAYS * 24 * 60 * 60;
		final Instant noUpdateNeededBefore = currentAlliancesTimestamp.plusSeconds(updateIntervalSeconds);

		if (nowTimestamp.isBefore(noUpdateNeededBefore)) {
			updateTimestampsTable(updateBegin, timestampColumnName, currentAlliancesTimestamp);
			return; // no new data available
		}

		final String xmlPath = PROTOCOL + "://" + ogdb.getFullServerName() + PATH_ALLIANCES;

		final Document document = OGameAPIDatabase.getDocumentFromXMLurl(xmlPath);
		if (document == null) {
			return; // file could not be parsed. cannot update
		}

		final Node head = document.getFirstChild();

		final Instant fileTimestamp = Instant
				.ofEpochSecond(Long.parseLong(head.getAttributes().getNamedItem("timestamp").getNodeValue()));

		if (!currentAlliancesTimestamp.isBefore(fileTimestamp)) {
			updateTimestampsTable(updateBegin, timestampColumnName, currentAlliancesTimestamp);
			return; // no new data available
		}

		Node currentNode;
		if (head.getFirstChild() != null) {
			currentNode = head.getFirstChild();
		} else {
			logger.info(ogdb.getServerPrefix() + " alliances xml contains no data");
			return;
		}
		
		Connection connection = ogdb.getDatabaseConnection();

		try (PreparedStatement updateAlliances = connection
				.prepareStatement("UPDATE alliances SET allianceName = ?, allianceTag = ?, homepage = ?, "
						+ "logo = ?, open = ?, lastUpdate = ? WHERE allianceId = ?;");
				PreparedStatement insertAlliances = connection
						.prepareStatement("INSERT OR IGNORE INTO alliances (allianceId, allianceName, allianceTag, "
								+ "homepage, logo, open, insertedOn, lastUpdate) VALUES (?,?,?,?,?,?,?,?);");) {
			int allyID;
			String allyName;
			String allyTag;
			int counter1 = 0;

			final int batchsize = 1000;
			final Timestamp lastUpdatesqltimestamp = Timestamp.from(fileTimestamp);

			do {
				allyID = Integer.parseInt(currentNode.getAttributes().getNamedItem("id").getNodeValue());
				allyName = currentNode.getAttributes().getNamedItem("name").getNodeValue();
				allyTag = currentNode.getAttributes().getNamedItem("tag").getNodeValue();

				// optional data
				String homepage = "";
				String logo = "";
				boolean open = false;
				Node homepageNode = currentNode.getAttributes().getNamedItem("homepage");
				Node logoNode = currentNode.getAttributes().getNamedItem("logo");
				Node openNode = currentNode.getAttributes().getNamedItem("open");
				if (homepageNode != null)
					homepage = homepageNode.getNodeValue();
				if (logoNode != null)
					logo = logoNode.getNodeValue();
				if (openNode != null)
					open = (Integer.parseInt(openNode.getNodeValue()) > 0);

				updateAlliances.setString(1, allyName);
				updateAlliances.setString(2, allyTag);
				updateAlliances.setString(3, homepage);
				updateAlliances.setString(4, logo);
				updateAlliances.setBoolean(5, open);
				updateAlliances.setTimestamp(6, lastUpdatesqltimestamp, OGameAPIDatabase.serverCalendar);
				updateAlliances.setInt(7, allyID);
				updateAlliances.addBatch();

				insertAlliances.setInt(1, allyID);
				insertAlliances.setString(2, allyName);
				insertAlliances.setString(3, allyTag);
				insertAlliances.setString(4, homepage);
				insertAlliances.setString(5, logo);
				insertAlliances.setBoolean(6, open);
				insertAlliances.setTimestamp(7, lastUpdatesqltimestamp, OGameAPIDatabase.serverCalendar);
				insertAlliances.setTimestamp(8, lastUpdatesqltimestamp, OGameAPIDatabase.serverCalendar);
				insertAlliances.addBatch();

				if (++counter1 % batchsize == 0) {
					connection.setAutoCommit(false);

					updateAlliances.executeBatch();
					insertAlliances.executeBatch();

					connection.commit();
					connection.setAutoCommit(true);
				}
				currentNode = currentNode.getNextSibling();

			} while (currentNode != null);

			connection.setAutoCommit(false);

			updateAlliances.executeBatch();
			insertAlliances.executeBatch();

			connection.commit();
			connection.setAutoCommit(true);

		}
		updateTimestampsTable(updateBegin, timestampColumnName, fileTimestamp);
		timestamps.setAlliancesTimestamp(fileTimestamp);
	}

	private void updateHighscore(Timestamps timestamps, Instant updateBegin)
			throws SAXException, ParserConfigurationException, IOException, SQLException {
		for (int category = 1; category < 3; category++) {
			for (int type = 0; type < 8; type++) {
				updateHighscore(category, type, timestamps, updateBegin);
			}
		}
		deleteOutdatedHighscores();
	}

	private void updateHighscore(int category, int type, Timestamps timestamps, Instant updateBegin)
			throws SAXException, ParserConfigurationException, IOException, SQLException {

		logger.info(ogdb.getServerPrefix() + " updating highscore category " + category + ", type = " + type);

		if (category < 1 || category > 2 || type < 0 || type > 7) {
			throw new IllegalArgumentException("Error in updateHighscore. category and or type not in range");
		}

		final String timestampColumnName = "highscore" + category + "" + type + "Timestamp";
		final String xmlPath = PROTOCOL + "://" + ogdb.getFullServerName() + PATH_HIGHSCORE + "?category=" + category
				+ "&type=" + type;

		final Document document = OGameAPIDatabase.getDocumentFromXMLurl(xmlPath);
		if (document == null) {
			return; // file could not be parsed. cannot update
		}

		final Node head = document.getFirstChild();

		final Instant fileTimestamp = Instant
				.ofEpochSecond(Long.parseLong(head.getAttributes().getNamedItem("timestamp").getNodeValue()));

		Node currentNode;
		if (head.getFirstChild() != null) {
			currentNode = head.getFirstChild();
		} else {
			logger.info(ogdb.getServerPrefix() + " highscore xml contains no data");
			return;
		}

		final String highscoreTableName = HIGHSCORE_TABLE_NAMES[category];
		final String entityTableName = new String[] { "", "players", "alliances" }[category];
		final String pointName = POINTS[type];
		final String rankName = RANKS[type];
		final String idName = HIGHSCORE_KEYS[category];

		final int batchsize = 2000;
		
		Connection connection = ogdb.getDatabaseConnection();

		try (PreparedStatement insertOrIgnoreNewEntity = connection
				.prepareStatement("INSERT OR IGNORE INTO " + entityTableName + "(" + idName
						+ ", insertedOn, lastUpdate) VALUES (?,?,?);");
				PreparedStatement insertNewHighscoreEntry = connection.prepareStatement(
						"INSERT INTO " + highscoreTableName + "(" + idName + ",timestamp) VALUES (?,?);");
				PreparedStatement updateHighscore = connection
						.prepareStatement("UPDATE " + highscoreTableName + " SET " + pointName + " = ?, " + rankName
								+ " = ? WHERE " + idName + " = ? AND timestamp = ?;");
				PreparedStatement updateHighscoreShips = (category == 1 && type == 3)
						? connection.prepareStatement("UPDATE " + highscoreTableName
								+ " SET ships = ? WHERE " + idName + " = ? AND timestamp = ?;")
						: null;

		) {

			int entityId = 0;
			long score = 0;
			int rank = 0;
			int counter1 = 0;
			long ships = 0;
			Node shipsNode = null;

			final Timestamp entityTimestamp = category == 1 ? Timestamp.from(timestamps.getPlayersTimestamp())
					: Timestamp.from(timestamps.getAlliancesTimestamp());

			final Timestamp updateBeginSQLTimestamp = Timestamp.from(updateBegin);

			do {
				entityId = Integer.parseInt(currentNode.getAttributes().getNamedItem("id").getNodeValue());
				score = Long.parseLong(currentNode.getAttributes().getNamedItem("score").getNodeValue());
				rank = Integer.parseInt(currentNode.getAttributes().getNamedItem("position").getNodeValue());

				insertOrIgnoreNewEntity.setInt(1, entityId);
				insertOrIgnoreNewEntity.setTimestamp(2, entityTimestamp);
				insertOrIgnoreNewEntity.setTimestamp(3, entityTimestamp);
				insertOrIgnoreNewEntity.addBatch();

				if (type == 0) {
					insertNewHighscoreEntry.setInt(1, entityId);
					insertNewHighscoreEntry.setTimestamp(2, updateBeginSQLTimestamp);
					insertNewHighscoreEntry.addBatch();
				}

				updateHighscore.setLong(1, score);
				updateHighscore.setInt(2, rank);
				updateHighscore.setInt(3, entityId);
				updateHighscore.setTimestamp(4, updateBeginSQLTimestamp);
				updateHighscore.addBatch();

				if (category == 1 & type == 3) {
					shipsNode = currentNode.getAttributes().getNamedItem("ships");
					if (shipsNode != null) {
						ships = Long.parseLong(currentNode.getAttributes().getNamedItem("ships").getNodeValue());
						updateHighscoreShips.setLong(1, ships);
						updateHighscoreShips.setInt(2, entityId);
						updateHighscoreShips.setTimestamp(3, updateBeginSQLTimestamp);
						updateHighscoreShips.addBatch();
					}
				}

				// set topscore in serverdata to keep it up-to-date
				// instead of waiting for the serverdata.xml to be updated
				if (rank == 1 && category == 1 && type == 0) {
					try (PreparedStatement ps = connection
							.prepareStatement("UPDATE serverData SET topScore = ? ;");) {
						ps.setLong(1, score);
						ps.execute();
					}
				}

				if (++counter1 % batchsize == 0) {
					long totalExecute = 0;
					long newplayer = 0;
					long newhighscore = 0;
					long updatehighscore = 0;
					logger.finer("batch start");
					totalExecute = System.currentTimeMillis();
					connection.setAutoCommit(false);

					newplayer = System.currentTimeMillis();
					insertOrIgnoreNewEntity.executeBatch();
					logger.finer("new player took " + (System.currentTimeMillis() - newplayer) + " ms");

					newhighscore = System.currentTimeMillis();
					insertNewHighscoreEntry.executeBatch();
					logger.finer("new highscore took " + (System.currentTimeMillis() - newhighscore) + " ms");

					updatehighscore = System.currentTimeMillis();
					updateHighscore.executeBatch();
					logger.finer("update highscore took " + (System.currentTimeMillis() - updatehighscore) + " ms");

					if (category == 1 & type == 3) {
						updateHighscoreShips.executeBatch();
					}

					connection.commit();
					connection.setAutoCommit(true);
					logger.finer("batch end. took " + (System.currentTimeMillis() - totalExecute) + " ms");
				}
				currentNode = currentNode.getNextSibling();

			} while (currentNode != null);
			logger.finer("batch start");
			Long ms = System.currentTimeMillis();
			connection.setAutoCommit(false);

			insertOrIgnoreNewEntity.executeBatch();
			insertNewHighscoreEntry.executeBatch();
			updateHighscore.executeBatch();
			if (category == 1 & type == 3) {
				updateHighscoreShips.executeBatch();
			}

			connection.commit();
			connection.setAutoCommit(true);
			logger.finer("batch end. took " + (System.currentTimeMillis() - ms) + " ms");
		}

		updateTimestampsTable(updateBegin, timestampColumnName, fileTimestamp);
		timestamps.setHighscoreTimestamp(category, type, fileTimestamp);
	}

	private void updateTimestampsTable(Instant updateTime, String column, Instant timestamp) throws SQLException {
		try (PreparedStatement ps = ogdb.getDatabaseConnection()
				.prepareStatement("UPDATE timestamps SET " + column + " = ? WHERE updateTimestamp = ?;");) {
			ps.setTimestamp(1, Timestamp.from(timestamp));
			ps.setTimestamp(2, Timestamp.from(updateTime));
			ps.execute();
		}
	}

	private void deleteOutdatedHighscores() throws SQLException {
		int highscorelimit = ogdb.getEntityReader().getDatabaseSettings().getMaxHighscoreEntriesPerEntity();

		try (Statement stmt = ogdb.getDatabaseConnection().createStatement()) {
			stmt.execute("DELETE FROM playerHighscores " + "WHERE timestamp NOT IN ( " + "		SELECT timestamp "
					+ "		FROM playerHighscores phs " + "		WHERE playerHighscores.playerId = phs.playerId "
					+ "		ORDER BY timestamp DESC " + "		LIMIT " + highscorelimit + ");");

			stmt.execute("DELETE FROM allianceHighscores " + "WHERE timestamp NOT IN ( " + "		SELECT timestamp "
					+ "		FROM allianceHighscores ahs "
					+ "		WHERE allianceHighscores.allianceId = ahs.allianceId " + "		ORDER BY timestamp DESC "
					+ "		LIMIT " + highscorelimit + ");");
		}
	}

	private boolean isRequiredDatabaseVersion(DatabaseSettings settings) {
		if (settings == null)
			return false;
		int currentDatabaseVersion = settings.getDatabaseVersion();
		int requiredDatabaseVersion = DatabaseCreator.DATABASE_VERSION;
		return (currentDatabaseVersion == requiredDatabaseVersion);
	}

	private void isRequiredDatabaseVersionOrEx() throws UnsupportedOgameDatabaseVersionException, SQLException {
		DatabaseSettings settings = ogdb.getEntityReader().getDatabaseSettings();
		if (!isRequiredDatabaseVersion(settings)) {
			throw new UnsupportedOgameDatabaseVersionException(settings != null ? settings.getDatabaseVersion() : -1,
					DatabaseCreator.DATABASE_VERSION);
		}
	}

}
