package darksky.ogameapidatabasefx.database.logs;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.Alliance;
import darksky.ogameapidatabasefx.database.entities.Entity;
import darksky.ogameapidatabasefx.database.entities.Moon;
import darksky.ogameapidatabasefx.database.entities.Planet;
import darksky.ogameapidatabasefx.database.entities.Player;
import darksky.ogameapidatabasefx.database.entities.ResultSetHelper;

public class LogReader {
	
	private static final Logger logger = Logger.getLogger(LogReader.class.getName());
	
	private static final String PLAYER_COLUMNS = "players.playerid AS playeridP, players.playername AS playernameP, players.playerstatus AS playerstatusP, "
			+ "players.allianceid AS allianceidP, players.insertedOn AS insertedonP, players.lastupdate AS lastupdateP, "
			+ "(players.lastUpdate <> (SELECT max(lastUpdate) FROM players WHERE playerName <> '')) AS isDeletedP";
	
	private static final String PLAYERHIGHSCORE_COLUMNS = "playerhighscores.totalPoints AS totalPointsPH, playerhighscores.totalRank AS totalRankPH, "
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
	
	private static final String ALLIANCEHIGHSCORE_COLUMNS = "alliancehighscores.totalPoints AS totalPointsAH, alliancehighscores.totalRank AS totalRankAH, "
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
	
	private static final String getNewPlayersQry = "SELECT " + PLAYER_COLUMNS
			+ " FROM players "
			+ "WHERE insertedOnP >= ? AND insertedOnP <= ? "
			+ "ORDER BY insertedOnP DESC, playernameP ASC;";
	
	private static final String getDeletedPlayersQry = "SELECT " + PLAYER_COLUMNS
			+ " FROM players "
			+ "WHERE lastUpdateP >= ? AND lastUpdateP <= ? AND lastUpdateP <> (SELECT max(lastUpdate) FROM players WHERE playerName <> '') "
			+ "ORDER BY lastUpdateP DESC, playerNameP ASC;";
	
	private static final String getNewAlliancesQry = "SELECT " + ALLIANCE_COLUMNS
			+ " FROM alliances "
			+ "WHERE insertedOnA >= ? AND insertedOnA <= ? "
			+ "ORDER BY insertedOnA DESC, allianceNameA ASC;";
	
	private static final String getDeletedAlliancesQry = "SELECT " + ALLIANCE_COLUMNS
			+ " FROM alliances "
			+ "WHERE lastUpdateA >= ? AND lastUpdateA <= ? AND lastUpdateA <> (SELECT max(lastUpdate) FROM alliances WHERE allianceName <> '') "			
			+ "ORDER BY lastUpdateA DESC, allianceNameA ASC;";
	
	private static final String getNewPlanetsQry = "SELECT " + PLAYER_COLUMNS + ", " + PLANET_COLUMNS
			+ " FROM planets JOIN players ON planets.playerId = players.playerId "
			+ "WHERE insertedOnPL >= ? AND insertedOnPL <= ? ";
	
	private static final String getDeletedPlanetsQry = "SELECT " + PLAYER_COLUMNS + ", " + PLANET_COLUMNS
			+ " FROM planets JOIN players ON planets.playerId = players.playerId "
			+ "WHERE insertedOnPL >= ? AND insertedOnPL <= ? AND lastUpdatePL <> (SELECT max(lastUpdate) FROM planets) ";
	
	private static final String getNewMoonsQry = "SELECT " + MOON_COLUMNS + ", " + PLANET_COLUMNS + ", " + PLAYER_COLUMNS 
			+ " FROM moons JOIN planets ON moons.planetId = planets.planetId JOIN players ON moons.playerId = players.playerId "
			+ "WHERE insertedOnM >= ? AND insertedOnM <= ? ";

	private static final String getDeletedMoonsQry = "SELECT " + MOON_COLUMNS + ", " + PLANET_COLUMNS + ", " + PLAYER_COLUMNS
			+ " FROM moons JOIN planets ON moons.planetId = planets.planetId JOIN players ON moons.playerId = players.playerId "
			+ "WHERE lastUpdateM >= ? AND lastUpdateM <= ? AND lastUpdateM <> (SELECT max(lastUpdate) FROM moons) ";

	private static final String getPlayerNameChangesQry = "SELECT " + PLAYER_COLUMNS + ", oldName, newName, timestamp"
			+ " FROM playerNameChanges JOIN players ON playerNameChanges.playerId = players.playerId "
			+ "WHERE timestamp >= ? AND timestamp <= ? ";

	private static final String getPlayerStatusChangesQry = "SELECT " + PLAYER_COLUMNS + ", oldStatus, newStatus, timestamp"
			+ " FROM playerStatusChanges JOIN players ON playerStatusChanges.playerId = players.playerId "
			+ "WHERE timestamp >= ? AND timestamp <= ? ";

	private static final String getAllianceNameChangesQry = "SELECT " + ALLIANCE_COLUMNS + ", oldName, newName, timestamp"
			+ " FROM allianceNameChanges JOIN alliances ON allianceNameChanges.allianceId = alliances.allianceId "
			+ "WHERE timestamp >= ? AND timestamp <= ?";

	private static final String getAllianceTagChangesQry = "SELECT " + ALLIANCE_COLUMNS + ", oldTag, newTag, timestamp"
			+ " FROM allianceTagChanges JOIN alliances ON allianceTagChanges.allianceId = alliances.allianceId "
			+ "WHERE timestamp >= ? AND timestamp <= ?";

	private static final String getAllianceHomepageChangesQry = "SELECT " + ALLIANCE_COLUMNS + ", oldHomepage, newHomepage, timestamp"
			+ " FROM allianceHomepageChanges JOIN alliances ON allianceHomepageChanges.allianceId = alliances.allianceId "
			+ "WHERE timestamp >= ? AND timestamp <= ?";

	private static final String getAllianceLogoChangesQry = "SELECT " + ALLIANCE_COLUMNS + ", oldLogo, newLogo, timestamp"
			+ " FROM allianceLogoChanges JOIN alliances ON allianceLogoChanges.allianceId = alliances.allianceId "
			+ "WHERE timestamp >= ? AND timestamp <= ?";

	private static final String getAllianceOpenChangesQry = "SELECT " + ALLIANCE_COLUMNS + ", oldOpen, newOpen, timestamp"
			+ " FROM allianceOpenChanges JOIN alliances ON allianceOpenChanges.allianceId = alliances.allianceId "
			+ "WHERE timestamp >= ? AND timestamp <= ?";

	private static final String getAllianceMemberChangesQry = "SELECT " + PLAYER_COLUMNS
			+ ", A1.allianceId AS allianceIdOld, A1.allianceName AS allianceNameOld, A1.allianceTag AS allianceTagOld, A1.homepage AS homepageOld, "
			+ "A1.logo AS logoOld, A1.open AS openOld, A1.insertedOn AS insertedOnOld, A1.lastUpdate AS lastUpdateOld, "
			+ "(A1.lastUpdate <> (SELECT max(lastUpdate) FROM alliances WHERE allianceName <> '')) AS isDeletedOld, "
			+ "A2.allianceId AS allianceIdNew, A2.allianceName AS allianceNameNew, A2.allianceTag AS allianceTagNew, A2.homepage AS homepageNew, "
			+ "A2.logo AS logoNew, A2.open AS openNew, A2.insertedOn AS insertedOnNew, A2.lastUpdate AS lastUpdateNew, "
			+ "(A2.lastUpdate <> (SELECT max(lastUpdate) FROM alliances WHERE allianceName <> '')) AS isDeletedNew, "
			+ "timestamp "			
			+ "FROM allianceMemberChanges AMC JOIN alliances A1 ON AMC.oldAllianceId = A1.allianceId "
			+ "JOIN alliances A2 ON AMC.newAllianceId = A2.allianceId JOIN players ON AMC.playerId = players.playerId "
			+ "WHERE timestamp >= ? AND timestamp <= ?";

	private static final String getPlanetNameChangesQry = "SELECT " + PLAYER_COLUMNS + ", " + PLANET_COLUMNS + ", oldName, newName, timestamp"
			+ " FROM planetNameChanges JOIN planets ON planetNameChanges.planetId = planets.planetId JOIN players ON players.playerId = planets.playerId "
			+ "WHERE timestamp >= ? AND timestamp <= ?";

	private static final String getMoonNameChangesQry = "SELECT " + PLAYER_COLUMNS + ", " + PLANET_COLUMNS + ", " + MOON_COLUMNS + ", oldName, newName, timestamp"
			+ " FROM moonNameChanges JOIN moons ON moonNameChanges.moonId = moons.moonId JOIN planets ON planets.planetid = moons.planetid "
			+ "JOIN players ON moons.playerId = players.playerId "
			+ "WHERE timestamp >= ? AND timestamp <= ?";

	private static final String getRelocationsQry = "SELECT " + PLAYER_COLUMNS + ", " + PLANET_COLUMNS 
			+ ", oldGalaxy, oldSystem, oldPosition, newGalaxy, newSystem, newPosition, timestamp "
			+ "FROM relocations JOIN players ON relocations.playerId = players.playerId JOIN planets ON relocations.planetId = planets.planetId "
			+ "WHERE timestamp >= ? AND timestamp <= ?";

	private final OGameAPIDatabase ogdb;

	/**
	 * 
	 * @param ogdb
	 *            the OgameAPIDatabase that should be accessed, not null
	 */
	public LogReader(OGameAPIDatabase ogdb) {
		this.ogdb = Objects.requireNonNull(ogdb);
	}

	/**
	 * Get players that registered between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Player>> getNewPlayers(LocalDate from, LocalDate to) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getNewPlayers");
		
		if (from == null || to == null)
			return Collections.emptyList();
		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getNewPlayersQry);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Player>> newPlayers = new ArrayList<ChangeLogEntry<Player, Player>>();
				while (rs.next()) {
					Instant logDate = rs.getTimestamp("insertedOnP").toInstant();
					Player player = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					newPlayers.add(new ChangeLogEntry<Player, Player>(player, logDate, null, null));
				}				

				return newPlayers;
			}
		}
	}

	/**
	 * Get players that have been deleted between from and to. The instant of a
	 * ChangeLogEntry is equal to the last update date of the player.
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Player>> getDeletedPlayers(LocalDate from, LocalDate to) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getDeletedPlayers");
		
		if (from == null || to == null)
			return Collections.emptyList();

		LocalDate from2 = from.minusDays(OGameAPIDatabase.UPDATEINTERVAL_PLAYERS_DAYS);
		LocalDate to2 = to.minusDays(OGameAPIDatabase.UPDATEINTERVAL_PLAYERS_DAYS);

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getDeletedPlayersQry);) {
			ps.setDate(1, Date.valueOf(from2), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to2), OGameAPIDatabase.serverCalendar);

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Player>> deletedPlayers = new ArrayList<ChangeLogEntry<Player, Player>>();
				while (rs.next()) {
					Instant logDate = rs.getTimestamp("lastUpdateP").toInstant();
					Player player = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					deletedPlayers
							.add(new ChangeLogEntry<Player, Player>(player, logDate, null, null));
				}

				return deletedPlayers;
			}
		}
	}

	/**
	 * Get alliances created between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Alliance, Alliance>> getNewAlliances(LocalDate from, LocalDate to) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getNewAlliances");
		
		if (from == null || to == null)
			return Collections.emptyList();

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getNewAlliancesQry);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Alliance, Alliance>> newAlliances = new ArrayList<ChangeLogEntry<Alliance, Alliance>>();

				while (rs.next()) {
					Instant logDate = rs.getTimestamp("insertedOnA").toInstant();
					Alliance alliance = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					newAlliances.add(
							new ChangeLogEntry<Alliance, Alliance>(alliance, logDate, null, null));
				}

				return newAlliances;
			}
		}
	}

	/**
	 * Get alliances deleted between from and to. The instant of a ChangeLogEntry
	 * is equal to the last update date of the alliance.
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Alliance, Alliance>> getDeletedAlliances(LocalDate from, LocalDate to) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getDeletedAlliances");
		
		if (from == null || to == null)
			return Collections.emptyList();

		LocalDate from2 = from.minusDays(OGameAPIDatabase.UPDATEINTERVAL_ALLIANCES_DAYS);
		LocalDate to2 = to.minusDays(OGameAPIDatabase.UPDATEINTERVAL_ALLIANCES_DAYS);

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(getDeletedAlliancesQry);) {
			ps.setDate(1, Date.valueOf(from2), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to2), OGameAPIDatabase.serverCalendar);

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Alliance, Alliance>> deletedAlliances = new ArrayList<ChangeLogEntry<Alliance, Alliance>>();

				while (rs.next()) {
					Instant logDate = rs.getTimestamp("lastUpdateA").toInstant();
					Alliance alliance = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					deletedAlliances.add(
							new ChangeLogEntry<Alliance, Alliance>(alliance, logDate, null, null));
				}

				return deletedAlliances;
			}
		}
	}

	/**
	 * Get planets colonized between from and to.
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param player
	 *            get new planets of this player. if null every new planet is
	 *            included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Planet>> getNewPlanets(LocalDate from, LocalDate to, Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getNewPlanets");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (player == null ? "" : "AND playerIdP = ? ");

		String sql = getNewPlanetsQry + idCondition + "ORDER BY insertedOnPL DESC, planetNamePL ASC;";
		logger.fine(sql);
		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (player != null) {
				ps.setInt(3, player.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Planet>> newPlanets = new ArrayList<ChangeLogEntry<Player, Planet>>();
				while (rs.next()) {

					Planet planet = ResultSetHelper.getPlanetFromResultSet(rs, "PL");
					Player player2 = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					newPlanets.add(new ChangeLogEntry<Player, Planet>(player2, planet.getInsertedOn(),
							null, planet));
				}

				return newPlanets;
			}
		}
	}

	/**
	 * Get planets which have been deleted between from and to. The instant of a
	 * ChangeLogEntry is equal to the last update date of the planet.
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param player
	 *            get deleted planets of this player. if null every deleted
	 *            planet is included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Planet>> getDeletedPlanets(LocalDate from, LocalDate to, Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getDeletedPlanets");
		
		if (from == null || to == null)
			return Collections.emptyList();

		LocalDate from2 = from.minusDays(OGameAPIDatabase.UPDATEINTERVAL_UNIVERSE_DAYS);
		LocalDate to2 = to.minusDays(OGameAPIDatabase.UPDATEINTERVAL_UNIVERSE_DAYS);

		String idCondition = (player == null ? "" : "AND playerIdP = ? ");

		String sql = getDeletedPlanetsQry + idCondition + "ORDER BY insertedOnPL DESC, planetNamePL ASC;";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from2), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to2), OGameAPIDatabase.serverCalendar);
			if (player != null) {
				ps.setInt(3, player.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Planet>> deletedPlanets = new ArrayList<ChangeLogEntry<Player, Planet>>();
				while (rs.next()) {

					Planet planet = ResultSetHelper.getPlanetFromResultSet(rs, "PL");
					Player player2 = ResultSetHelper.getPlayerFromResultSet(rs, "P");

					deletedPlanets.add(new ChangeLogEntry<Player, Planet>(player2,
							planet.getLastUpdate(), planet, null));
				}

				return deletedPlanets;
			}
		}
	}

	/**
	 * Get moons created between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param player
	 *            get new moons of this player. if null every new moon is
	 *            included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Planet>> getNewMoons(LocalDate from, LocalDate to, Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getNewMoons");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (player == null ? "" : "AND playerIdP = ? ");

		String sql = getNewMoonsQry + idCondition + " ORDER BY insertedOnM DESC, moonNameM ASC;";
		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (player != null) {
				ps.setInt(3, player.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Planet>> newMoons = new ArrayList<ChangeLogEntry<Player, Planet>>();

				while (rs.next()) {

					Moon moon = ResultSetHelper.getMoonFromResultSet(rs, "M");
					Player player2 = ResultSetHelper.getPlayerFromResultSet(rs, "P");

					Planet planetOld = ResultSetHelper.getPlanetFromResultSet(rs, "PL");
					Planet planetNew = ResultSetHelper.getPlanetFromResultSet(rs, "PL");
					planetNew.setMoon(Optional.of(moon));
					newMoons.add(new ChangeLogEntry<Player, Planet>(player2, moon.getInsertedOn(), planetOld,
							planetNew));
				}

				return newMoons;
			}
		}
	}

	/**
	 * Get moons deleted between from and to. The instant of a ChangeLogEntry is
	 * equal to the last update date of the moon.
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param player
	 *            get deleted moons of this player. if null every deleted moon
	 *            is included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Planet>> getDeletedMoons(LocalDate from, LocalDate to, Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getDeletedMoons");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (player == null ? "" : "AND playerIdP = ? ");

		LocalDate from2 = from.minusDays(OGameAPIDatabase.UPDATEINTERVAL_UNIVERSE_DAYS);
		LocalDate to2 = to.minusDays(OGameAPIDatabase.UPDATEINTERVAL_UNIVERSE_DAYS);

		String sql = getDeletedMoonsQry + idCondition + "ORDER BY lastUpdateM DESC, moonNameM ASC;";
		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from2), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to2), OGameAPIDatabase.serverCalendar);
			if (player != null) {
				ps.setInt(3, player.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Planet>> deletedMoons = new ArrayList<ChangeLogEntry<Player, Planet>>();
				while (rs.next()) {

					Moon moon = ResultSetHelper.getMoonFromResultSet(rs, "M");
					Player player2 = ResultSetHelper.getPlayerFromResultSet(rs, "P");

					Planet planetOld = ResultSetHelper.getPlanetFromResultSet(rs, "PL");
					planetOld.setMoon(Optional.of(moon));
					Planet planetNew = ResultSetHelper.getPlanetFromResultSet(rs, "PL");
					deletedMoons.add(new ChangeLogEntry<Player, Planet>(player2, moon.getLastUpdate(),
							planetOld, planetNew));
				}

				return deletedMoons;
			}
		}
	}

	/**
	 * Get all name changes between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param player
	 *            get name changes of this player. if null every name change is
	 *            included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Player>> getPlayerNameChanges(LocalDate from, LocalDate to,
			Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getPlayerNameChanges");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (player == null ? "" : "AND playerIdP = ? ");

		String sql = getPlayerNameChangesQry + idCondition + "ORDER BY timestamp DESC, playerName ASC";
		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (player != null) {
				ps.setInt(3, player.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Player>> list = new ArrayList<ChangeLogEntry<Player, Player>>();
				while (rs.next()) {

					Instant logDate = rs.getTimestamp("timestamp").toInstant();

					Player player2;
					if (player == null) {
						player2 = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					} else {
						player2 = player;
					}
					
					Player playerOld = new Player(player2.getId(), rs.getString("oldName"), null, null, false, null, 0);
					
					Player playerNew = new Player(player2.getId(), rs.getString("newName"), null, null, false, null, 0);

					ChangeLogEntry<Player, Player> entry = new ChangeLogEntry<Player, Player>(player2,
							logDate, playerOld, playerNew);

					list.add(entry);

				}
				return list;
			}
		}
	}

	/**
	 * Get all status changes between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param player
	 *            get status changes of this player. if null every status change
	 *            is included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Player>> getPlayerStatusChanges(LocalDate from, LocalDate to,
			Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getPlayerStatusChanges");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (player == null ? "" : "AND playerIdP = ? ");

		String sql = getPlayerStatusChangesQry + idCondition + "ORDER BY timestamp DESC, playerName ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (player != null) {
				ps.setInt(3, player.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Player>> list = new ArrayList<ChangeLogEntry<Player, Player>>();
				while (rs.next()) {
					Instant logDate = rs.getTimestamp("timestamp").toInstant();

					Player player2;
					if (player == null) {
						player2 = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					} else {
						player2 = player;
					}
					Player playerOld = new Player(player2.getId(), null, null, null, false, rs.getString("oldStatus"), 0);
					
					Player playerNew = new Player(player2.getId(), null, null, null, false, rs.getString("newStatus"), 0);

					ChangeLogEntry<Player, Player> entry = new ChangeLogEntry<Player, Player>(player2,
							logDate, playerOld, playerNew);
					list.add(entry);

				}
				return list;
			}
		}
	}

	/**
	 * Get all alliance name changes between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param alliance
	 *            get name changes of this alliance. if null every name change
	 *            is included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Alliance, Alliance>> getAllianceNameChanges(LocalDate from, LocalDate to,
			Alliance alliance) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllianceNameChanges");
		
		if (from == null || to == null)
			return Collections.emptyList();

		Alliance owner = alliance;

		String idCondition = (owner == null ? "" : " AND allianceIdA = ? ");

		String sql = getAllianceNameChangesQry + idCondition + "ORDER BY timestamp DESC, allianceName ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (owner != null) {
				ps.setInt(3, owner.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Alliance, Alliance>> list = new ArrayList<ChangeLogEntry<Alliance, Alliance>>();

				while (rs.next()) {
					Instant logDate = rs.getTimestamp("timestamp").toInstant();					

					Alliance alliance2;
					if (owner == null) {
						alliance2 = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					} else {
						alliance2 = owner;
					}
					Alliance allianceOld = new Alliance(alliance2.getId(), rs.getString("oldName"), null, null, false, null, null, null, false);
					Alliance allianceNew = new Alliance(alliance2.getId(), rs.getString("newName"), null, null, false, null, null, null, false);

					ChangeLogEntry<Alliance, Alliance> entry = new ChangeLogEntry<Alliance, Alliance>(
							alliance2, logDate, allianceOld, allianceNew);
					list.add(entry);

				}
				return list;
			}
		}
	}

	/**
	 * Get all tag changes of alliance between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param alliance
	 *            get tag changes of this alliance. if null every tag change is
	 *            included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Alliance, Alliance>> getAllianceTagChanges(LocalDate from, LocalDate to,
			Alliance alliance) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllianceTagChanges");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (alliance == null ? "" : " AND allianceIdA = ? ");

		String sql = getAllianceTagChangesQry + idCondition + "ORDER BY timestamp DESC, allianceName ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (alliance != null) {
				ps.setInt(3, alliance.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Alliance, Alliance>> list = new ArrayList<ChangeLogEntry<Alliance, Alliance>>();

				while (rs.next()) {
					Instant logDate = rs.getTimestamp("timestamp").toInstant();

					Alliance alliance2;
					if (alliance == null) {
						alliance2 = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					} else {
						alliance2 = alliance;
					}
					Alliance allianceOld = new Alliance(alliance2.getId(), null, null, null, false, rs.getString("oldTag"), null, null, false);
					Alliance allianceNew = new Alliance(alliance2.getId(), null, null, null, false, rs.getString("newTag"), null, null, false);

					ChangeLogEntry<Alliance, Alliance> entry = new ChangeLogEntry<Alliance, Alliance>(
							alliance2, logDate, allianceOld, allianceNew);
					list.add(entry);

				}
				return list;
			}
		}
	}

	/**
	 * Get all homepage changes of alliance between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param alliance
	 *            get homepage changes of this alliance. if null every homepage
	 *            change is included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Alliance, Alliance>> getAllianceHomepageChanges(LocalDate from, LocalDate to,
			Alliance alliance) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllianceHomepageChanges");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (alliance == null ? "" : " AND allianceIdA = ? ");

		String sql = getAllianceHomepageChangesQry + idCondition + "ORDER BY timestamp DESC, allianceName ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (alliance != null) {
				ps.setInt(3, alliance.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Alliance, Alliance>> list = new ArrayList<ChangeLogEntry<Alliance, Alliance>>();

				while (rs.next()) {

					Instant logDate = rs.getTimestamp("timestamp").toInstant();

					Alliance alliance2;
					if (alliance == null) {
						alliance2 = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					} else {
						alliance2 = alliance;
					}
					Alliance allianceOld = new Alliance(alliance2.getId(), null, null, null, false, null, rs.getString("oldHomepage"), null, false);
					Alliance allianceNew = new Alliance(alliance2.getId(), null, null, null, false, null, rs.getString("newHomepage"), null, false);

					ChangeLogEntry<Alliance, Alliance> entry = new ChangeLogEntry<Alliance, Alliance>(
							alliance2, logDate, allianceOld, allianceNew);
					list.add(entry);

				}
				return list;
			}
		}
	}

	/**
	 * Get all logo changes of alliance between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param alliance
	 *            get logo changes of this alliance. if null every logo change
	 *            is included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Alliance, Alliance>> getAllianceLogoChanges(LocalDate from, LocalDate to,
			Alliance alliance) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllianceLogoChanges");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (alliance == null ? "" : " AND allianceIdA = ? ");

		String sql = getAllianceLogoChangesQry + idCondition + "ORDER BY timestamp DESC, allianceName ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (alliance != null) {
				ps.setInt(3, alliance.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Alliance, Alliance>> list = new ArrayList<ChangeLogEntry<Alliance, Alliance>>();

				while (rs.next()) {

					Instant logDate = rs.getTimestamp("timestamp").toInstant();

					Alliance alliance2;
					if (alliance == null) {
						alliance2 = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					} else {
						alliance2 = alliance;
					}
					Alliance allianceOld = new Alliance(alliance2.getId(), null, null, null, false, null, null, rs.getString("oldLogo"), false);
					Alliance allianceNew = new Alliance(alliance2.getId(), null, null, null, false, null, null, rs.getString("newLogo"), false);

					ChangeLogEntry<Alliance, Alliance> entry = new ChangeLogEntry<Alliance, Alliance>(
							alliance2, logDate, allianceOld, allianceNew);
					list.add(entry);

				}
				return list;
			}
		}
	}

	/**
	 * Get all open changes of alliance between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param alliance
	 *            get open changes of this alliance. if null every open change
	 *            is included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Alliance, Alliance>> getAllianceOpenChanges(LocalDate from, LocalDate to,
			Alliance alliance) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllianceOpenChanges");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (alliance == null ? "" : " AND allianceIdA = ? ");

		String sql = getAllianceOpenChangesQry + idCondition + "ORDER BY timestamp DESC, allianceName ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (alliance != null) {
				ps.setInt(3, alliance.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Alliance, Alliance>> list = new ArrayList<ChangeLogEntry<Alliance, Alliance>>();

				while (rs.next()) {

					Instant logDate = rs.getTimestamp("timestamp").toInstant();

					Alliance alliance2;
					if (alliance == null) {
						alliance2 = ResultSetHelper.getAllianceFromResultSet(rs, "A");
					} else {
						alliance2 = alliance;
					}
					Alliance allianceOld = new Alliance(alliance2.getId(), null, null, null, false, null, null, null, rs.getBoolean("oldOpen"));
					Alliance allianceNew = new Alliance(alliance2.getId(), null, null, null, false, null, null, null, rs.getBoolean("newOpen"));

					ChangeLogEntry<Alliance, Alliance> entry = new ChangeLogEntry<Alliance, Alliance>(
							alliance2, logDate, allianceOld, allianceNew);
					list.add(entry);

				}
				return list;
			}
		}
	}

	/**
	 * Get all alliance member changes between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param owner
	 *            get alliance member changes of owner. owner can be a player or
	 *            an alliance. if owner is null every alliance member change is
	 *            included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Optional<Alliance>>> getAllianceMemberChanges(LocalDate from, LocalDate to,
			Entity owner) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getAllianceMemberChanges");
		
		if (from == null || to == null)
			return Collections.emptyList();

		boolean ownerIsPlayer = (owner instanceof Player);
		boolean ownerIsAlliance = (owner instanceof Alliance);
		if(owner != null && !ownerIsPlayer && !ownerIsAlliance){
			throw new IllegalArgumentException("wrong owner");
		}

		String idCondition = "";
		if (ownerIsPlayer) {
			idCondition = " AND playerIdP = ? ";
		}
		if (ownerIsAlliance) {
			idCondition = " AND (A1.allianceId = ? OR A2.allianceId = ?) ";
		}

		String sql = getAllianceMemberChangesQry + idCondition + "ORDER BY timestamp DESC, playerName ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (ownerIsPlayer) {
				ps.setInt(3, owner.getId());
			}
			if (ownerIsAlliance) {
				ps.setInt(3, owner.getId());
				ps.setInt(4, owner.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Optional<Alliance>>> list = new ArrayList<>();
				while (rs.next()) {

					Instant logDate = rs.getTimestamp("timestamp").toInstant();

					Alliance allianceOld = ResultSetHelper.getAllianceFromResultSet(rs, "Old");

					Alliance allianceNew = ResultSetHelper.getAllianceFromResultSet(rs, "New");
					
					allianceOld = allianceOld.getId() == 999999 ? null : allianceOld;
					allianceNew = allianceNew.getId() == 999999 ? null : allianceNew;

					Player player = ResultSetHelper.getPlayerFromResultSet(rs, "P");

					ChangeLogEntry<Player, Optional<Alliance>> entry = new ChangeLogEntry<>(
							player, logDate, Optional.ofNullable(allianceOld), Optional.ofNullable(allianceNew));
					list.add(entry);

				}
				return list;
			}
		}
	}

	/**
	 * Get all planet name changes of player between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param player
	 *            get planet name changes of this player. if null every planet
	 *            name change is included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Planet>> getPlanetNameChanges(LocalDate from, LocalDate to,
			Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getPlanetNameChanges");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (player == null ? "" : "AND playerIdP = ? ");

		String sql = getPlanetNameChangesQry + idCondition + "ORDER BY timestamp DESC, playerName ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (player != null) {
				ps.setInt(3, player.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Planet>> list = new ArrayList<ChangeLogEntry<Player, Planet>>();
				while (rs.next()) {

					Instant logDate = rs.getTimestamp("timestamp").toInstant();
					
					Player player2;
					if (player == null) {
						player2 = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					} else {
						player2 = player;
					}
					Planet planetOld = ResultSetHelper.getPlanetFromResultSet(rs, "Pl");
					planetOld.setName(rs.getString("oldName"));
					Planet planetNew = ResultSetHelper.getPlanetFromResultSet(rs, "Pl");
					planetNew.setName(rs.getString("newName"));

					ChangeLogEntry<Player, Planet> entry = new ChangeLogEntry<Player, Planet>(player2,
							logDate, planetOld, planetNew);
					list.add(entry);

				}
				return list;
			}
		}
	}

	/**
	 * Get all moon name changes of player between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param player
	 *            get moon name changes of this player. if null every moon name
	 *            change is included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Planet>> getMoonNameChanges(LocalDate from, LocalDate to, Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getMoonNameChanges");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (player == null ? "" : "AND playerIdP = ? ");

		String sql = getMoonNameChangesQry + idCondition + "ORDER BY timestamp DESC, playerName ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (player != null) {
				ps.setInt(3, player.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Planet>> list = new ArrayList<ChangeLogEntry<Player, Planet>>();
				while (rs.next()) {

					Instant logDate = rs.getTimestamp("timestamp").toInstant();

					Player player2;
					if (player == null) {
						player2 = ResultSetHelper.getPlayerFromResultSet(rs, "P");

					} else {
						player2 = player;
					}
					Planet planet = ResultSetHelper.getPlanetFromResultSet(rs, "Pl");
					Planet planetOld = new Planet(planet.getId(), null, null, null, false, 1, 1, 1, 0);
					Planet planetNew = new Planet(planet.getId(), null, null, null, false, 1, 1, 1, 0);

					Moon moon = ResultSetHelper.getMoonFromResultSet(rs, "M");
					Moon moonOld = new Moon(moon.getId(), rs.getString("oldName"), null, null, false, 0, player2.getId(), planet.getId());
					Moon moonNew = new Moon(moon.getId(), rs.getString("newName"), null, null, false, 0, player2.getId(), planet.getId());
					
					planetOld.setMoon(Optional.of(moonOld));
					planetNew.setMoon(Optional.of(moonNew));

					ChangeLogEntry<Player, Planet> entry = new ChangeLogEntry<Player, Planet>(player2,
							logDate, planetOld, planetNew);
					list.add(entry);

				}
				return list;
			}
		}
	}

	/**
	 * Get all relocations of player between from and to
	 * 
	 * @param from
	 *            the begin, inclusive not null
	 * @param to
	 *            the end date, exclusive not null
	 * @param player
	 *            get relocations of this player. if null every relocation is
	 *            included
	 * @return list of results. if there are no results the list is empty
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ChangeLogEntry<Player, Planet>> getRelocations(LocalDate from, LocalDate to, Player player) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " getRelocations");
		
		if (from == null || to == null)
			return Collections.emptyList();

		String idCondition = (player == null ? "" : "AND playerIdP = ? ");

		String sql = getRelocationsQry + idCondition + "ORDER BY timestamp DESC, playerName ASC";

		try (PreparedStatement ps = ogdb.getDatabaseConnection().prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			if (player != null) {
				ps.setInt(3, player.getId());
			}

			try (ResultSet rs = ps.executeQuery();) {

				List<ChangeLogEntry<Player, Planet>> list = new ArrayList<>();
				while (rs.next()) {

					Instant logDate = rs.getTimestamp("timestamp").toInstant();

					Player player2;
					if (player == null) {
						player2 = ResultSetHelper.getPlayerFromResultSet(rs, "P");
					} else {
						player2 = player;
					}
					Planet currentPlanet = ResultSetHelper.getPlanetFromResultSet(rs, "Pl");

					Planet oldPlanet = new Planet(currentPlanet).setGalaxy(rs.getInt("oldGalaxy"))
							.setSystem(rs.getInt("oldSystem")).setPosition(rs.getInt("oldPosition"));

					Planet newPlanet = new Planet(currentPlanet).setGalaxy(rs.getInt("newGalaxy"))
							.setSystem(rs.getInt("newSystem")).setPosition(rs.getInt("newPosition"));

					ChangeLogEntry<Player, Planet> entry = new ChangeLogEntry<Player, Planet>(player2,
							logDate, oldPlanet, newPlanet);
					list.add(entry);

				}
				return list;
			}
		}
	}

}
