package darksky.ogameapidatabasefx.database.entities;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;

import darksky.ogameapidatabasefx.database.databasemanagement.DatabaseSettings;
import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.Highscore.HighscoreType;

/**
 * @author darksky
 * 
 *         Reads data from database e.g Player, Alliance, Moon, Planet,
 *         AccountData, AllianceData, SolarSystem, ServerData,
 *         DatabaseInformation
 *
 */
public class EntityReader {

	private static final Logger logger = Logger.getLogger(EntityReader.class.getName());

	private final OGameAPIDatabase ogdb;

	private static final String PLAYER_COLUMNS = "players.playerid AS playeridP, players.playername AS playernameP, players.playerstatus AS playerstatusP, "
			+ "players.allianceid AS allianceidP, players.insertedOn AS insertedonP, players.lastupdate AS lastupdateP, "
			+ "(players.lastUpdate <> (SELECT max(lastUpdate) FROM players WHERE playerName <> '')) AS isDeletedP";

	private static final String PLAYERHIGHSCORE_COLUMNS = "playerhighscores.playerId AS playerIdPH, "
			+ "playerhighscores.totalPoints AS totalPointsPH, playerhighscores.totalRank AS totalRankPH, "
			+ "playerhighscores.economyPoints AS economyPointsPH, playerhighscores.economyRank AS economyRankPH, "
			+ "playerhighscores.researchPoints AS researchPointsPH, playerhighscores.researchRank AS researchRankPH, "
			+ "playerhighscores.militaryPoints AS militaryPointsPH, playerhighscores.militaryRank AS militaryRankPH, "
			+ "playerhighscores.militaryBuiltPoints AS militaryBuiltPointsPH, playerhighscores.militaryBuiltRank AS militaryBuiltRankPH, "
			+ "playerhighscores.militaryDestroyedPoints AS militaryDestroyedPointsPH, playerhighscores.militaryDestroyedRank AS militaryDestroyedRankPH, "
			+ "playerhighscores.militaryLostPoints AS militaryLostPointsPH, playerhighscores.militaryLostRank AS militaryLostRankPH, "
			+ "playerhighscores.honorPoints AS honorPointsPH, playerhighscores.honorRank AS honorRankPH, playerhighscores.ships AS shipsPH, "
			+ "playerhighscores.timestamp AS timestampPH";

	private static final String ALLIANCE_COLUMNS = "alliances.allianceId AS allianceIdA, alliances.allianceName AS allianceNameA, alliances.allianceTag AS allianceTagA, "
			+ "alliances.homepage AS homepageA, alliances.logo AS logoA, alliances.open AS openA, alliances.insertedOn AS insertedOnA, alliances.lastUpdate AS lastUpdateA, "
			+ "(alliances.lastUpdate <> (SELECT max(lastUpdate) FROM alliances WHERE alliancename <> '')) AS isDeletedA";

	private static final String ALLIANCEHIGHSCORE_COLUMNS = "alliancehighscores.allianceId AS allianceIdAH, "
			+ "alliancehighscores.totalPoints AS totalPointsAH, alliancehighscores.totalRank AS totalRankAH, "
			+ "alliancehighscores.economyPoints AS economyPointsAH, alliancehighscores.economyRank AS economyRankAH, "
			+ "alliancehighscores.researchPoints AS researchPointsAH, alliancehighscores.researchRank AS researchRankAH, "
			+ "alliancehighscores.militaryPoints AS militaryPointsAH, alliancehighscores.militaryRank AS militaryRankAH, "
			+ "alliancehighscores.militaryBuiltPoints AS militaryBuiltPointsAH, alliancehighscores.militaryBuiltRank AS militaryBuiltRankAH, "
			+ "alliancehighscores.militaryDestroyedPoints AS militaryDestroyedPointsAH, alliancehighscores.militaryDestroyedRank AS militaryDestroyedRankAH, "
			+ "alliancehighscores.militaryLostPoints AS militaryLostPointsAH, alliancehighscores.militaryLostRank AS militaryLostRankAH, "
			+ "alliancehighscores.honorPoints AS honorPointsAH, alliancehighscores.honorRank AS honorRankAH, "
			+ "alliancehighscores.timestamp AS timestampAH";

	private static final String PLANET_COLUMNS = "planets.planetId AS planetIdPL, planets.planetName AS planetNamePL, "
			+ "planets.galaxy AS galaxyPL, planets.system AS systemPL, planets.position AS positionPL, "
			+ "planets.playerId AS playerIdPL, planets.insertedOn AS insertedOnPL, planets.lastUpdate AS lastUpdatePL, "
			+ "(planets.lastUpdate <> (SELECT max(lastUpdate) FROM planets)) AS isDeletedPL";

	private static final String MOON_COLUMNS = "moons.moonId AS moonIdM, moons.moonName AS moonNameM, moons.moonSize AS moonSizeM, moons.playerId AS playerIdM, "
			+ "moons.planetId AS planetIdM, moons.insertedOn AS insertedOnM, moons.lastUpdate AS lastUpdateM, "
			+ "(moons.lastUpdate <> (SELECT max(lastUpdate) FROM moons)) AS isDeletedM";

	private final String getDatabaseSettingsQry = "SELECT databaseVersion, saveActivityStates, savePlanetDistribution, saveHighscoreDistribution, maxHighscoreEntriesPerEntity "
			+ "FROM databaseSettings;";

	private final String getTimestampsQry = "SELECT updateTimestamp, serverDataTimestamp, "
			+ "playersTimestamp, universeTimestamp, alliancesTimestamp, highscore10Timestamp, highscore11Timestamp, highscore12Timestamp, "
			+ "highscore13Timestamp, highscore14Timestamp, highscore15Timestamp, highscore16Timestamp, highscore17Timestamp, "
			+ "highscore20Timestamp, highscore21Timestamp, highscore22Timestamp, highscore23Timestamp, highscore24Timestamp, highscore25Timestamp, "
			+ "highscore26Timestamp, highscore27Timestamp FROM timestamps";

	private final String getServerDataQry = "SELECT domain, name, number, language, timezone, timezoneOffset, version, speed, speedFleet, galaxies, systems, "
			+ "acs, rapidFire, defToTF, debrisFactor, repairFactor, newbieProtectionLimit, newbieProtectionHigh, "
			+ "topScore, bonusFields, donutGalaxy, donutSystem, debrisFactorDef, wfEnabled, wfMinimumRessLost, wfMinimumLossPercentage, "
			+ "wfBasicPercentageRepairable, globalDeuteriumSaveFactor, bashlimit, probeCargo, "
			+ "researchDurationDivisor, darkMatterNewAcount, cargoHyperspaceTechMultiplier FROM serverData;";

	private final String getPlayerQry = "SELECT " + PLAYER_COLUMNS + " FROM players";

	private final String getPlayerByIdQry = getPlayerQry + " WHERE playerIdP = ?;";

	private final String getPlayerByNameQry = getPlayerQry + " WHERE playerNameP = ?;";

	private final String getPlayersByNameLikeQry = getPlayerQry + " WHERE playerNameP like ?;";

	private final String getPlayersByAllianceIdQry = getPlayerQry + " WHERE allianceIdP = ?;";

	private final String getAllianceQry = "SELECT " + ALLIANCE_COLUMNS + " FROM alliances";

	private final String getAllianceByIdQry = getAllianceQry + " WHERE allianceIdA = ?;";

	private final String getAllianceByNameQry = getAllianceQry + " WHERE allianceNameA = ?;";

	private final String getAlliancesByNameLikeQry = getAllianceQry + " WHERE allianceNameA like ?;";

	private final String getAlliancesByOpenStatusQry = getAllianceQry + " WHERE openA = ?;";

	private final String getPlanetQry = "SELECT planetId AS planetId, planetName, galaxy, system, position, playerId, insertedOn, lastUpdate, "
			+ "(lastUpdate <> (SELECT max(lastUpdate) FROM planets)) AS isDeleted " + "FROM planets";

	private final String getPlanetByIdQry = getPlanetQry + " WHERE planetId = ?;";

	private final String getPlanetsByPlayerQry = getPlanetQry + " WHERE playerId = ?";

	private final String getMoonQry = "SELECT " + MOON_COLUMNS + " FROM moons";

	private final String getMoonByIdQry = getMoonQry + " WHERE moonId = ?";

	private final String getMoonsByPlanetQry = getMoonQry + " WHERE planetId = ?";

	private final String getMoonsByPlayerQry = getMoonQry + " WHERE playerId = ?";

	public EntityReader(OGameAPIDatabase databaseController) {
		ogdb = Objects.requireNonNull(databaseController);
	}

	public DatabaseSettings getDatabaseSettings() throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getDatabaseSettings");

		try (Statement stmt = ogdb.getDatabaseConnection().createStatement();
				ResultSet rs = stmt.executeQuery(getDatabaseSettingsQry);) {
			if (rs.next()) {
				return ResultSetHelper.getDatabaseSettingsFromResultSet(rs, "");
			} else {
				logger.warning(ogdb.getServerPrefix() + "no database settings");
				return null;
			}
		}
	}

	/**
	 * Reads the Timestamps from database.
	 * 
	 * updateTimestamp, serverDataTimestamp, playersTimestamp,
	 * universeTimestamp, alliancesTimestamp, highscore10Timestamp,
	 * highscore11Timestamp, highscore12Timestamp, highscore13Timestamp,
	 * highscore14Timestamp, highscore15Timestamp, highscore16Timestamp,
	 * highscore17Timestamp, highscore20Timestamp, highscore21Timestamp,
	 * highscore22Timestamp, highscore23Timestamp, highscore24Timestamp,
	 * highscore25Timestamp, highscore26Timestamp, highscore27Timestamp
	 * 
	 * @return A List of Timestamps
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<Timestamps> getTimestamps() throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getTimestamps");

		try (Statement stmt = ogdb.getDatabaseConnection().createStatement();
				ResultSet rs = stmt.executeQuery(getTimestampsQry + " ORDER BY updateTimestamp ASC;");) {
			List<Timestamps> list = new ArrayList<>();
			while (rs.next()) {
				list.add(ResultSetHelper.getTimestampsFromResultSet(rs, ""));
			}
			return list;
		}
	}

	/**
	 * Reads the Timestamps from database.
	 * 
	 * updateTimestamp, serverDataTimestamp, playersTimestamp,
	 * universeTimestamp, alliancesTimestamp, highscore10Timestamp,
	 * highscore11Timestamp, highscore12Timestamp, highscore13Timestamp,
	 * highscore14Timestamp, highscore15Timestamp, highscore16Timestamp,
	 * highscore17Timestamp, highscore20Timestamp, highscore21Timestamp,
	 * highscore22Timestamp, highscore23Timestamp, highscore24Timestamp,
	 * highscore25Timestamp, highscore26Timestamp, highscore27Timestamp
	 * 
	 * @return The newest timestamps, or null if no timestamps in database
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public Timestamps getNewestTimestamps() throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getNewestTimestamps");

		try (Statement stmt = ogdb.getDatabaseConnection().createStatement();
				ResultSet rs = stmt.executeQuery(getTimestampsQry + " ORDER BY updateTimestamp DESC;");) {
			if (rs.next()) {
				return ResultSetHelper.getTimestampsFromResultSet(rs, "");
			} else {
				logger.warning(ogdb.getServerPrefix() + "no timestamps");
				return null;
			}
		}
	}

	/**
	 * Reads the server data from database
	 * 
	 * @return the server data, or null if there is no server data
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public ServerData getServerData() throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getServerData");

		try (Statement stmt = ogdb.getDatabaseConnection().createStatement();
				ResultSet rs = stmt.executeQuery(getServerDataQry)) {
			rs.next();
			return ResultSetHelper.getServerDataFromResultSet(rs, "");
		}
	}

	/**
	 * Gets the player with the specified id. Includes deleted players.
	 * 
	 * @param playerId
	 *            the player id > 0
	 * @param includeHighscore
	 *            if the player should contain the highscore
	 * @return An Optional. The Optional is empty if no player with this id
	 *         exists
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public Optional<Player> getPlayerById(int playerId) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getPlayerById(" + playerId + ")");

		if (playerId < 1)
			throw new IllegalArgumentException("playerId < 1");

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getPlayerByIdQry);) {
			ps.setInt(1, playerId);
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					Player p = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					return Optional.of(p);
				} else
					return Optional.empty();
			}
		}
	}

	/**
	 * Gets the player with the specified name. Includes deleted players.
	 * 
	 * @param playerName
	 *            the player name
	 * @return An Optional. The Optional is empty if no player with this id
	 *         exists
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public Optional<Player> getPlayerByName(String playerName) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getPlayerByName(" + playerName + ")");

		Objects.requireNonNull(playerName, "playerName is null");

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getPlayerByNameQry);) {
			ps.setString(1, playerName);
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					Player p = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					return Optional.of(p);
				} else
					return Optional.empty();
			}
		}
	}

	/**
	 * Get players with a current highscore rank between the passed values
	 * 
	 * @param highscoreType
	 *            the highscore type
	 * @param smallestRank
	 *            (inclusive)
	 * @param highestRank
	 *            (inclusive)
	 * @param includeDeletedPlayers
	 *            if deleted players should be included. this can result in two
	 *            or more players having the same rank in the returned list
	 * @return List of players, never null
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<AccountData> getPlayersByHighscoreRank(HighscoreType highscoreType, int smallestRank, int highestRank,
			boolean includeDeletedPlayers, boolean includeBannedPlayers) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getPlayersByHighscoreRank(" + highscoreType + ", " + smallestRank + ", "
				+ highestRank + ", " + includeDeletedPlayers + ", " + includeBannedPlayers + ")");

		if (highscoreType == null || smallestRank < 0 || highestRank < 0 || smallestRank > highestRank)
			return Collections.emptyList();

		String rank = highscoreType + "RankPH";

		String sql = "SELECT " + PLAYERHIGHSCORE_COLUMNS + ", " + PLAYER_COLUMNS
				+ " FROM playerhighscores JOIN players ON playerhighscores.playerid = players.playerid INNER JOIN "
				+ "(SELECT playerid, MAX(timestamp) as maxtimestamp FROM playerhighscores GROUP BY playerid) t2 "
				+ "ON playerhighscores.playerid = t2.playerid AND playerhighscores.timestamp = t2.maxtimestamp "
				+ "WHERE " + rank + " > ? AND " + rank + " <= ? ORDER BY " + rank + " ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setInt(1, smallestRank);
			ps.setInt(2, highestRank);
			try (ResultSet rs = ps.executeQuery();) {
				List<AccountData> playerList = new ArrayList<>();
				while (rs.next()) {
					Player p = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					Highscore h = ResultSetHelper.getPlayerHighscoreFromResultSet(rs, "PH");
					boolean isDeleted = p.isDeleted();
					boolean isBanned = p.getPlayerStatus().indexOf('b') > 0;
					if ((!isDeleted && (includeBannedPlayers || !isBanned))
							|| (includeDeletedPlayers && (includeBannedPlayers || !isBanned))) {
						AccountData data = new AccountData(p);
						data.setHighscores(Collections.singletonList(h));
						playerList.add(data);
					}
				}

				return playerList;
			}
		}
	}

	/**
	 * Get alliances with a highscore rank between the passed values
	 * 
	 * @param highscoreType
	 *            the highscore type
	 * @param smallestRank
	 *            (inclusive)
	 * @param highestRank
	 *            (inclusive)
	 * @param includeDeletedAlliances
	 *            if deleted alliances should be included. this can result in
	 *            two or more alliances having the same rank in the returned
	 *            list
	 * @return List of alliances, never null
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<AllianceData> getAlliancesByHighscoreRank(HighscoreType highscoreType, int smallestRank,
			int highestRank, boolean includeDeletedAlliances) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAlliancesByHighscoreRank(" + highscoreType + ", " + smallestRank
				+ ", " + highestRank + ", " + includeDeletedAlliances + ")");

		if (highscoreType == null || smallestRank < 0 || highestRank < 0 || smallestRank > highestRank)
			throw new IllegalArgumentException(
					"highscoreType is null OR smallestRank < 0 OR highestRank < 0 OR smallestRank > highestRank");

		String rank = highscoreType + "RankAH";

		String sql = "SELECT " + ALLIANCEHIGHSCORE_COLUMNS + ", " + ALLIANCE_COLUMNS
				+ " FROM allianceHighscores JOIN alliances ON allianceHighscores.allianceid = alliances.allianceid INNER JOIN "
				+ "(SELECT allianceid, MAX(timestamp) as maxtimestamp FROM allianceHighscores GROUP BY allianceid) t2 "
				+ "ON allianceHighscores.allianceid = t2.allianceid AND allianceHighscores.timestamp = t2.maxtimestamp "
				+ "WHERE " + rank + " > ? AND " + rank + " <= ? ORDER BY " + rank + " ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setInt(1, smallestRank);
			ps.setInt(2, highestRank);
			try (ResultSet rs = ps.executeQuery();) {
				List<AllianceData> allianceList = new ArrayList<>();

				while (rs.next()) {
					Alliance a = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					Highscore h = ResultSetHelper.getAllianceHighscoreFromResultSet(rs, "AH");
					if (!a.isDeleted() || includeDeletedAlliances) {
						AllianceData data = new AllianceData(a);
						data.setHighscores(Collections.singletonList(h));
						allianceList.add(data);
					}
				}

				return allianceList;
			}
		}
	}

	/**
	 * Gets the planet with the specified id. Includes deleted planets
	 * 
	 * @param planetId
	 *            the planet id
	 * @return An Optional which is empty if no such planet exists
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public Optional<Planet> getPlanetByPlanetId(int planetId) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getPlanetByPlanetId(" + planetId + ")");

		if (planetId < 1)
			throw new IllegalArgumentException("planetId < 1");

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getPlanetByIdQry);) {
			ps.setInt(1, planetId);
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					Planet p = ResultSetHelper.getPlanetFromResultSet(rs, "");
					return Optional.of(p);
				} else
					return Optional.empty();
			}
		}
	}

	/**
	 * Get every planet of the specified player. Includes deleted planets.
	 * 
	 * @param player
	 *            the player
	 * @return A list of all planets of this player. planets are sorted by
	 *         galaxy, system, position
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<Planet> getPlanetsOfPlayer(Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getPlanetsOfPlayer(" + player + ")");

		Objects.requireNonNull(player, "player is null");

		try (PreparedStatement ps = ogdb.getDatabaseConnection()
				.prepareStatement(getPlanetsByPlayerQry + " ORDER BY galaxy ASC, system ASC, position ASC");) {
			ps.setInt(1, player.getId());
			try (ResultSet rs = ps.executeQuery();) {
				List<Planet> planetList = new ArrayList<Planet>();

				Planet planet = null;
				while (rs.next()) {
					planet = ResultSetHelper.getPlanetFromResultSet(rs, "");
					planetList.add(planet);
				}

				return planetList;
			}
		}
	}

	/**
	 * Get the moon with the specified moon id. Includes deleted moons.
	 * 
	 * @param moonId
	 *            the moon id
	 * @return An Optional which is empty if no such moon exists
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public Optional<Moon> getMoonByMoonId(int moonId) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getMoonByMoonId(" + moonId + ")");

		if (moonId < 1)
			throw new IllegalArgumentException("moonId < 1");

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getMoonByIdQry);) {
			ps.setInt(1, moonId);
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					Moon m = ResultSetHelper.getMoonFromResultSet(rs, "M");
					return Optional.of(m);
				} else
					return Optional.empty();
			}
		}
	}

	/**
	 * Get the current moon of the specified planet. If the planet is deleted,
	 * the method will return a deleted moon, if any.
	 * 
	 * @param planet
	 *            the planet, not null
	 * @return An Optional which is empty if no such moon exists
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public Optional<Moon> getCurrentMoonOfPlanet(Planet planet) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getCurrentMoonOfPlanet(" + planet + ")");
		Objects.requireNonNull(planet, "planet is null");

		List<Moon> possibleMoons = this.getMoonsOfPlanet(planet);

		Optional<Moon> moon = possibleMoons.stream().filter(m -> m.getLastUpdate() == planet.getLastUpdate())
				.findFirst();

		return moon;
	}

	/**
	 * Get every moon that ever belonged to the planet. Includes deleted moons
	 * 
	 * @param planet
	 *            the planet
	 * @return A list of every moon that has ever belonged to the planet
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<Moon> getMoonsOfPlanet(Planet planet) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getMoonsOfPlanet(" + planet + ")");
		Objects.requireNonNull(planet, "planet is null");

		try (PreparedStatement ps = ogdb.getDatabaseConnection()
				.prepareStatement(getMoonsByPlanetQry + " ORDER BY lastUpdate DESC");) {
			ps.setInt(1, planet.getId());
			try (ResultSet rs = ps.executeQuery();) {
				List<Moon> moonList = new ArrayList<Moon>();

				Moon moon = null;
				while (rs.next()) {
					moon = ResultSetHelper.getMoonFromResultSet(rs, "M");
					moonList.add(moon);
				}

				return moonList;
			}
		}
	}

	/**
	 * Get every moon of the specified player. Includes deleted moons.
	 * 
	 * @param player
	 *            the player
	 * @return A list of every moon of the player.
	 * @throws SQLException
	 */
	public List<Moon> getMoonsOfPlayer(Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getMoonsOfPlayer(" + player + ")");
		Objects.requireNonNull(player, "player is null");

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getMoonsByPlayerQry);) {
			ps.setInt(1, player.getId());
			try (ResultSet rs = ps.executeQuery();) {
				List<Moon> moonList = new ArrayList<Moon>();

				Moon moon = null;
				while (rs.next()) {
					moon = ResultSetHelper.getMoonFromResultSet(rs, "M");
					moonList.add(moon);
				}

				return moonList;
			}
		}
	}

	/**
	 * Get the alliance with the specified id. Includes deleted alliances.
	 * 
	 * @param allianceId
	 *            the id
	 * @return An Optional which is empty if no such alliance exists
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public Optional<Alliance> getAllianceById(int allianceId) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllianceById(" + allianceId + ")");

		if (allianceId < 1)
			throw new IllegalArgumentException("allianceId < 1");
		if (allianceId == 999999)
			return Optional.empty();
		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getAllianceByIdQry);) {
			ps.setInt(1, allianceId);
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					Alliance a = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					return Optional.of(a);
				} else
					return Optional.empty();
			}
		}
	}

	/**
	 * Get the alliance with the specified name. Includes deleted alliances.
	 * 
	 * @param allianceName
	 *            the name
	 * @return An Optional which is empty if no such alliance exists
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public Optional<Alliance> getAllianceByName(String allianceName) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllianceByName(" + allianceName + ")");
		Objects.requireNonNull(allianceName, "allianceName is null");

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getAllianceByNameQry);) {
			ps.setString(1, allianceName);
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					Alliance a = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					return Optional.of(a);
				} else
					return Optional.empty();
			}
		}
	}

	/**
	 * Get every alliance with the specified open status.
	 * 
	 * @param isOpen
	 * @param includeDeletedAlliances
	 *            If true, the method will include deleted alliances.
	 * @return List of alliances
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<Alliance> getAlliancesByOpenStatus(boolean isOpen, boolean includeDeletedAlliances)
			throws SQLException {
		logger.fine(
				ogdb.getServerPrefix() + " getAlliancesByOpenStatus(" + isOpen + ", " + includeDeletedAlliances + ")");

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getAlliancesByOpenStatusQry);) {
			ps.setBoolean(1, isOpen);
			try (ResultSet rs = ps.executeQuery();) {
				List<Alliance> allianceList = new ArrayList<Alliance>();

				Alliance alliance = null;
				while (rs.next()) {
					alliance = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					if (!alliance.isDeleted() || includeDeletedAlliances)
						allianceList.add(alliance);
				}

				return allianceList;
			}
		}
	}

	/**
	 * Get a list of every player of the specified alliance. Includes deleted
	 * players.
	 * 
	 * @param alliance
	 *            the alliance, not null
	 * @return A list of every alliance member
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<Player> getAllianceMembers(Alliance alliance) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllianceMembers(" + alliance + ")");
		Objects.requireNonNull(alliance, "alliance is null");

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getPlayersByAllianceIdQry);) {
			ps.setInt(1, alliance.getId());
			try (ResultSet rs = ps.executeQuery();) {
				List<Player> playerList = new ArrayList<Player>();

				Player player = null;
				while (rs.next()) {
					player = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					playerList.add(player);
				}

				return playerList;
			}
		}
	}

	/**
	 * Gets the current solar system of specified galaxy and system
	 * 
	 * @param galaxy
	 * @param system
	 * @return The solar system, or null if galaxy or system are out of range.
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public SolarSystem getCurrentSolarSystem(int galaxy, int system) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getCurrentSolarSystem(" + galaxy + ", " + system + ")");
		if (galaxy < 1 || system < 1)
			throw new IllegalArgumentException("galaxy < 1 OR system < 1");

		SolarSystem solarSystem = this.getSolarSystem(galaxy, system, 0);
		return solarSystem;
	}

	/**
	 * NOT IMPLEMENTED FOR weeksInPast != 0 ! returns the same SolarSystem as
	 * getCurrentSolarSystem
	 * 
	 * Gets the solar system of specified galaxy and system at the specific date
	 * 
	 * @param galaxy
	 * @param system
	 * @param weeksInPast
	 *            the SolarSystem of weeksInPast weeks ago is returned. 0 means
	 *            current SolarSystem. Must be >= 0.
	 * @return The solar system
	 * @throws SQLException
	 *             if a database error occurs
	 */
	// TODO
	public SolarSystem getSolarSystem(int galaxy, int system, int weeksInPast) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getSolarSystem(" + galaxy + ", " + system + ", " + weeksInPast + ")");

		if (galaxy < 1 || system < 1 || weeksInPast < 0)
			throw new IllegalArgumentException("galaxy < 1 OR system < 1 OR weeksInPast < 0");

		ServerData sd = getServerData();
		if (sd == null) {
			logger.info(ogdb.getServerPrefix() + "returning empty solar system");
			return new SolarSystem(0, 0);
		}

		if (galaxy > sd.getGalaxies() || system > sd.getSystems())
			throw new IllegalArgumentException("galaxy > " + sd.getGalaxies() + " OR system > " + sd.getSystems());

		try (PreparedStatement ps = ogdb.getDatabaseConnection()
				.prepareStatement("SELECT playerIdP, playerNameP, playerStatusP, allianceIdP, insertedOnP, lastUpdateP, isDeletedP "
				+ ", allianceIdA, allianceNameA, allianceTagA, homepageA, logoA, openA, insertedOnA, lastUpdateA, isDeletedA "
				+ ", planetIdPL, planetNamePL, galaxyPL, systemPL, positionPL, playerIdPL, insertedOnPL, lastUpdatePL, isDeletedPL "
				+ ", moonIdM, moonNameM, moonSizeM, playerIdM, planetIdM, insertedOnM, lastUpdateM, isDeletedM "
				+ " FROM "
				// get planets that existed at this date
				+ "(SELECT " + PLANET_COLUMNS + " FROM planets "
				+ "WHERE galaxyPL = ? AND systemPL = ? AND lastUpdatePL = (SELECT max(lastUpdate) FROM planets) ) AS t1 "
				+ "JOIN "
				// get players that existed at this date
				+ "(SELECT " + PLAYER_COLUMNS
				+ " FROM players WHERE lastUpdateP = (SELECT max(lastUpdate) FROM players WHERE playerName <> '')) AS t2 ON playerIdPL = playerIdP "
				+ "LEFT JOIN "
				// get moons that existed at this date
				+ "(SELECT " + MOON_COLUMNS
				+ " FROM moons WHERE lastUpdateM = (SELECT max(lastUpdate) FROM moons)) AS t3 ON planetIdPL = planetIdM "
				+ "LEFT JOIN "
				// get alliances
				+ "(SELECT " + ALLIANCE_COLUMNS
				+ " FROM alliances WHERE lastUpdateA = (SELECT max(lastUpdate) FROM alliances WHERE allianceName <> '')) AS t4 ON allianceIdA = allianceIdP "
				+ "ORDER BY galaxyPL ASC, systemPL ASC, positionPL ASC; ")) {

			ps.setInt(1, galaxy);
			ps.setInt(2, system);

			try (ResultSet rs = ps.executeQuery()) {
				SolarSystem solarSystem = new SolarSystem(galaxy, system);
				int position = 1;
				int currentPosition = 0;
				while (rs.next()) {
					currentPosition = rs.getInt("positionPL");
					// add empty positions
					for (; position < currentPosition; position++) {
						solarSystem.addSystemPosition(new SystemPosition(position, null, null, null, null));
					}
					// add position from database
					Player player = ResultSetHelper.getPlayerFromResultSet(rs, "P");

					Planet planet = ResultSetHelper.getPlanetFromResultSet(rs, "PL");

					Moon moon = ResultSetHelper.getMoonFromResultSet(rs, "M");

					Alliance alliance = ResultSetHelper.getAllianceFromResultSet(rs, "A");

					solarSystem.addSystemPosition(new SystemPosition(currentPosition, player, planet, moon, alliance));
					position++;
				}
				for (; position < 16; position++) {
					solarSystem.addSystemPosition(new SystemPosition(position, null, null, null, null));
				}
				return solarSystem;
			}

		}
	}

	/**
	 * Get every player whose player name includes the specified name. Includes
	 * deleted players.
	 * 
	 * @param name
	 *            not null
	 * @return List of players
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<Player> getPlayersByNameLike(String name) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getPlayersByNameLike(" + name + ")");
		Objects.requireNonNull(name, "name is null");

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getPlayersByNameLikeQry);) {
			ps.setString(1, "%" + name.replace("'", "\"") + "%");
			try (ResultSet rs = ps.executeQuery();) {
				List<Player> playerList = new ArrayList<Player>();

				while (rs.next()) {
					Player player = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					playerList.add(player);
				}

				return playerList;
			}
		}
	}

	/**
	 * Get every alliance whose alliance name includes the specified name.
	 * Includes deleted alliances.
	 * 
	 * @param name
	 * @return List of alliances
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<Alliance> getAlliancesByNameLike(String name) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAlliancesByNameLike(" + name + ")");
		Objects.requireNonNull(name, "name is null");

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getAlliancesByNameLikeQry);) {
			ps.setString(1, "%" + name.replace("'", "\"") + "%");
			try (ResultSet rs = ps.executeQuery();) {
				List<Alliance> allianceList = new ArrayList<Alliance>();

				Alliance alliance = null;
				while (rs.next()) {
					alliance = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					allianceList.add(alliance);
				}

				return allianceList;
			}
		}
	}

	/**
	 * Gets full account data of the player. This includes the current alliance,
	 * current planets and current moons.
	 * 
	 * @param player
	 * @return The account data
	 * @throws SQLException
	 */
	public AccountData getAccountData(Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAccountData(" + player + ")");
		Objects.requireNonNull(player, "player is null");

		Optional<Alliance> alliance = getAllianceById(player.getAllianceId());
		List<Highscore> highscores = getAllPlayerHighscoresOfPlayer(player);
		List<Planet> planets = getPlanetsOfPlayer(player);

		// comparator to sort planets by ascending coordinates
		Comparator<? super Planet> comp = (p1, p2) -> {
			int g = Integer.compare(p1.getGalaxy(), p2.getGalaxy());
			if (g != 0)
				return g;
			int s = Integer.compare(p1.getSystem(), p2.getSystem());
			if (s != 0)
				return s;
			int p = Integer.compare(p1.getPosition(), p2.getPosition());
			if (p != 0)
				return p;
			return p1.isDeleted() ? 1 : -1;
		};
		Map<Planet, List<Moon>> map = new TreeMap<Planet, List<Moon>>(comp);

		if (planets.size() > 0) {
			for (Planet planet : planets) {
				List<Moon> moons = getMoonsOfPlanet(planet);
				map.put(planet, moons);
			}
		}

		AccountData accountData = new AccountData(player, alliance, highscores, map);
		return accountData;
	}

	/**
	 * Gets full alliance data of alliance. This includes the current members.
	 * 
	 * @param allianceId
	 * @return The alliance data
	 * @throws SQLException
	 */
	public AllianceData getAllianceData(Alliance alliance) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllianceData(" + alliance + ")");
		Objects.requireNonNull(alliance, "alliance is null");

		List<Highscore> highscores = getAllAllianceHighscoresOfAlliance(alliance);
		List<Player> memberList = getAllianceMembers(alliance);
		
		Highscore newestHighscore = highscores.get(highscores.size()-1);
		long ships = 0;
		for(Player p : memberList){
			Highscore ph = getNewestHighscoreOfPlayer(p);
			ships += ph.getPoints(HighscoreType.Ships);
		}
		newestHighscore.setEntry(HighscoreType.Ships, ships, 0);

		AllianceData allianceData = new AllianceData(alliance, highscores, memberList);
		return allianceData;
	}

	public Highscore getNewestHighscoreOfPlayer(Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getNewestHighscoreOfPlayer(" + player + ")");
		Objects.requireNonNull(player, "player is null");

		String getNewestHighscoreOfPlayerQry = "SELECT " + PLAYERHIGHSCORE_COLUMNS
				+ " FROM playerHighscores WHERE playerIdPH = ? ORDER BY timestamp DESC LIMIT 1";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getNewestHighscoreOfPlayerQry);) {
			ps.setInt(1, player.getId());
			try (ResultSet rs = ps.executeQuery();) {
				Highscore h = null;
				if (rs.next()) {
					h = ResultSetHelper.getPlayerHighscoreFromResultSet(rs, "PH");
				}
				return h;
			}
		}
	}

	public Highscore getNewestHighscoreOfAlliance(Alliance alliance) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getNewestHighscoreOfAlliance(" + alliance + ")");
		Objects.requireNonNull(alliance, "alliance is null");

		String getNewestHighscoreOfAllianceQry = "SELECT " + ALLIANCEHIGHSCORE_COLUMNS
				+ " FROM allianceHighscores WHERE allianceIdAH = ? ORDER BY timestamp DESC LIMIT 1";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getNewestHighscoreOfAllianceQry);) {
			ps.setInt(1, alliance.getId());
			try (ResultSet rs = ps.executeQuery();) {
				Highscore h = null;
				if (rs.next()) {
					h = ResultSetHelper.getAllianceHighscoreFromResultSet(rs, "AH");
				}
				return h;
			}
		}
	}

	public List<Highscore> getAllPlayerHighscoresOfPlayer(Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllPlayerHighscoresOfPlayer(" + player + ")");
		Objects.requireNonNull(player, "player is null");

		String getAllPlayeHighscoresOfPlayerQry = "SELECT " + PLAYERHIGHSCORE_COLUMNS
				+ " FROM playerHighscores WHERE playerIdPH = ?";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getAllPlayeHighscoresOfPlayerQry);) {
			ps.setInt(1, player.getId());
			try (ResultSet rs = ps.executeQuery();) {
				List<Highscore> highscoreList = new ArrayList<Highscore>();

				Highscore h = null;
				while (rs.next()) {
					h = ResultSetHelper.getPlayerHighscoreFromResultSet(rs, "PH");
					highscoreList.add(h);
				}

				return highscoreList;
			}
		}
	}

	public List<Highscore> getAllAllianceHighscoresOfAlliance(Alliance alliance) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllAllianceHighscoresOfAlliance(" + alliance + ")");
		Objects.requireNonNull(alliance, "alliance is null");

		String getAllPlayeHighscoresOfPlayerQry = "SELECT " + ALLIANCEHIGHSCORE_COLUMNS
				+ " FROM allianceHighscores WHERE allianceIdAH = ?";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getAllPlayeHighscoresOfPlayerQry);) {
			ps.setInt(1, alliance.getId());
			try (ResultSet rs = ps.executeQuery();) {
				List<Highscore> highscoreList = new ArrayList<Highscore>();

				Highscore h = null;
				while (rs.next()) {
					h = ResultSetHelper.getAllianceHighscoreFromResultSet(rs, "AH");
					highscoreList.add(h);
				}

				return highscoreList;
			}
		}
	}

	public void writeDatabaseSettings(DatabaseSettings databaseSettings) throws SQLException {
		Objects.requireNonNull(databaseSettings, "databaseSettings is null");
		try (PreparedStatement ps = ogdb.getDatabaseConnection()
				.prepareStatement("UPDATE databaseSettings SET databaseVersion = ?, saveActivityStates = ?, "
						+ "savePlanetDistribution = ?, saveHighscoreDistribution = ?, maxHighscoreEntriesPerEntity = ?");) {
			ps.setInt(1, databaseSettings.getDatabaseVersion());
			ps.setBoolean(2, databaseSettings.isSaveActivityStates());
			ps.setBoolean(3, databaseSettings.isSavePlanetDistribution());
			ps.setBoolean(4, databaseSettings.isSaveHighscoreDistribution());
			ps.setInt(5, databaseSettings.getMaxHighscoreEntriesPerEntity());
			ps.execute();
		}
	}

}
