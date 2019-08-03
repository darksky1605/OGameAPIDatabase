package darksky.ogameapidatabasefx.database.statistics;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.Planet;
import darksky.ogameapidatabasefx.database.entities.Player;
import darksky.ogameapidatabasefx.database.logs.ChangeLogEntry;
import darksky.ogameapidatabasefx.database.logs.LogReader;

public class StatisticsReader {
	
	private static final Logger logger = Logger.getLogger(StatisticsReader.class.getName());

	private final OGameAPIDatabase m_ogdb;

	public StatisticsReader(OGameAPIDatabase ogdb) {
		m_ogdb = Objects.requireNonNull(ogdb);
	}

	/**
	 * Get all planet distributions between dateFrom and dateTo
	 * 
	 * @param dateFrom
	 *            the start date, inclusive, not null
	 * @param dateTo
	 *            the end date, exclusive, not null
	 * @return List of planet distributions
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<XYDistribution> getPlanetDistributions(LocalDate dateFrom,
			LocalDate dateTo) throws SQLException {
		if (dateFrom == null || dateTo == null)
			return Collections.emptyList();

		try (PreparedStatement ps = m_ogdb
				.getDatabaseConnection()
				.prepareStatement(
						"SELECT timestamp, galaxies, systems, planetDistributionString FROM planetDistributions "
								+ "WHERE timestamp >= ? AND timestamp <= ?")) {
			ps.setDate(1, Date.valueOf(dateFrom),
					OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(dateTo), OGameAPIDatabase.serverCalendar);
			try (ResultSet rs = ps.executeQuery()) {
				List<XYDistribution> list = new ArrayList<XYDistribution>();
				while (rs.next()) {
					LocalDate date = rs.getDate("timestamp",
							OGameAPIDatabase.serverCalendar).toLocalDate();
					String distributionString = rs
							.getString("planetDistributionString");
					int galaxies = rs.getInt("galaxies");
					int systems = rs.getInt("systems");
					XYDistribution pd = new XYDistribution(date, galaxies,
							systems);
					pd.setFromString(distributionString);
					list.add(pd);
				}
				return list;
			}
		}
	}

	/**
	 * Get the newest planet distribution from database
	 * 
	 * @return the newest planet distribution or null if no planet distribution
	 *         exists
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public XYDistribution getNewestPlanetDistribution() throws SQLException {
		try (PreparedStatement ps = m_ogdb
				.getDatabaseConnection()
				.prepareStatement(
						"SELECT timestamp, galaxies, systems, planetDistributionString FROM planetDistributions "
								+ "WHERE timestamp = (SELECT max(timestamp) FROM planetDistributions);")) {
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					LocalDate date = rs.getDate("timestamp",
							OGameAPIDatabase.serverCalendar).toLocalDate();
					String distributionString = rs
							.getString("planetDistributionString");
					int galaxies = rs.getInt("galaxies");
					int systems = rs.getInt("systems");
					XYDistribution pd = new XYDistribution(date, galaxies,
							systems);
					pd.setFromString(distributionString);
					return pd;
				}
				return null;
			}
		}
	}

	/**
	 * Get the newest moon distribution from database
	 * 
	 * @return the newest moon distribution or null if no moon distribution
	 *         exists
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public XYDistribution getNewestMoonDistribution() throws SQLException {
		try (PreparedStatement ps = m_ogdb
				.getDatabaseConnection()
				.prepareStatement(
						"SELECT timestamp, galaxies, systems, moonDistributionString FROM moonDistributions "
								+ "WHERE timestamp = (SELECT max(timestamp) FROM moonDistributions);")) {
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					LocalDate date = rs.getDate("timestamp",
							OGameAPIDatabase.serverCalendar).toLocalDate();
					String distributionString = rs
							.getString("moonDistributionString");
					int galaxies = rs.getInt("galaxies");
					int systems = rs.getInt("systems");
					XYDistribution md = new XYDistribution(date, galaxies,
							systems);
					md.setFromString(distributionString);
					return md;
				}
				return null;
			}
		}
	}

	/**
	 * Get all moon distributions between dateFrom and dateTo
	 * 
	 * @param dateFrom
	 *            the start date, inclusive, not null
	 * @param dateTo
	 *            the end date, exclusive, not null
	 * @return List of moon distributions
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<XYDistribution> getMoonDistributions(LocalDate dateFrom,
			LocalDate dateTo) throws SQLException {
		if (dateFrom == null || dateTo == null)
			return Collections.emptyList();

		try (PreparedStatement ps = m_ogdb
				.getDatabaseConnection()
				.prepareStatement(
						"SELECT timestamp, galaxies, systems, moonDistributionString FROM moonDistributions "
								+ "WHERE timestamp >= ? AND timestamp <= ?")) {
			ps.setDate(1, Date.valueOf(dateFrom),
					OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(dateTo), OGameAPIDatabase.serverCalendar);
			try (ResultSet rs = ps.executeQuery()) {
				List<XYDistribution> list = new ArrayList<XYDistribution>();
				while (rs.next()) {
					LocalDate date = rs.getDate("timestamp",
							OGameAPIDatabase.serverCalendar).toLocalDate();
					String distributionString = rs
							.getString("moonDistributionString");
					int galaxies = rs.getInt("galaxies");
					int systems = rs.getInt("systems");
					XYDistribution pd = new XYDistribution(date, galaxies,
							systems);
					pd.setFromString(distributionString);
					list.add(pd);
				}
				return list;
			}
		}
	}

	/**
	 * Generates a ServerHistory of this server for the provided time span
	 * 
	 * @param dateFrom
	 *            begin of report, inclusive, not null
	 * @param dateTo
	 *            end of report, exclusive, not null
	 * @return the server history, or null if no server activity states in the
	 *         given range exist.
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public ServerHistory generateServerHistory(LocalDate dateFrom,
			LocalDate dateTo) throws SQLException {
		if (dateFrom == null || dateTo == null)
			return null;

		ServerHistory serverHistory = null;

		List<ServerActivityState> alist = getServerActivityStates(
				dateFrom, dateTo);
		if (alist.size() == 0)
			return null;
		serverHistory = new ServerHistory(alist.get(0),
				alist.get(alist.size() - 1));
		
		
		
		final String autodeletedplayerssql = "SELECT count(*) from players "
				+ "WHERE lastUpdate >= ? AND lastUpdate <= ? AND lastUpdate <> (SELECT MAX(lastUpdate) FROM players) "
				+ "AND charIndex('I', playerStatus) > 0 ;";

		int autodeletedplayers = -666;
		try (PreparedStatement ps = m_ogdb.getDatabaseConnection()
				.prepareStatement(autodeletedplayerssql)) {
			ps.setDate(1, Date.valueOf(dateFrom
					.minusDays(OGameAPIDatabase.UPDATEINTERVAL_PLAYERS_DAYS)));
			ps.setDate(2, Date.valueOf(dateTo
					.minusDays(OGameAPIDatabase.UPDATEINTERVAL_PLAYERS_DAYS)));
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					autodeletedplayers = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING,
					"could not get autodeletedplayers", e);
		}
		serverHistory.setNumberOfAutoDeletedPlayers(autodeletedplayers);
		
		int numberOfCreatedPlayers = getNumberOfPlayersCreatedBetween(
				dateFrom, dateTo);
		int numberOfCreatedPlanets = getNumberOfPlanetsCreatedBetween(
				dateFrom, dateTo);
		int numberOfCreatedMoons = getNumberOfMoonsCreatedBetween(
				dateFrom, dateTo);
		int numberOfCreatedAlliances = getNumberOfAlliancesCreatedBetween(
				dateFrom, dateTo);
		int numberOfDeletedPlayers = getNumberOfPlayersDeletedBetween(
				dateFrom, dateTo);
		int numberOfDeletedPlanets = getNumberOfPlanetsDeletedBetween(
				dateFrom, dateTo);
		int numberOfDeletedMoons = getNumberOfMoonsDeletedBetween(
				dateFrom, dateTo);
		int numberOfDeletedAlliances = getNumberOfAlliancesDeletedBetween(
				dateFrom, dateTo);
		
		serverHistory.setNumberOfNewPlayers(numberOfCreatedPlayers);
		serverHistory.setNumberOfNewPlanets(numberOfCreatedPlanets);
		serverHistory.setNumberOfNewMoons(numberOfCreatedMoons);
		serverHistory.setNumberOfNewAlliances(numberOfCreatedAlliances);
		serverHistory.setNumberOfDeletedPlayers(numberOfDeletedPlayers);		
		serverHistory.setNumberOfDeletedPlanets(numberOfDeletedPlanets);
		serverHistory.setNumberOfDeletedMoons(numberOfDeletedMoons);
		serverHistory.setNumberOfDeletedAlliances(numberOfDeletedAlliances);
		serverHistory
				.setNumberOfPlayerNameChanges(getNumberOfLogsInTableBetween(
						dateFrom, dateTo, "playerNameChanges"));
		serverHistory
				.setNumberOfPlayerStatusChanges(getNumberOfLogsInTableBetween(
						dateFrom, dateTo, "playerStatusChanges"));
		serverHistory
				.setNumberOfAllianceNameChanges(getNumberOfLogsInTableBetween(
						dateFrom, dateTo, "allianceNameChanges"));
		serverHistory
				.setNumberOfAllianceTagChanges(getNumberOfLogsInTableBetween(
						dateFrom, dateTo, "allianceTagChanges"));
		serverHistory
				.setNumberOfAllianceHomepageChanges(getNumberOfLogsInTableBetween(
						dateFrom, dateTo, "allianceHomepageChanges"));
		serverHistory
				.setNumberOfAllianceLogoChanges(getNumberOfLogsInTableBetween(
						dateFrom, dateTo, "allianceLogoChanges"));
		serverHistory
				.setNumberOfAllianceOpenChanges(getNumberOfLogsInTableBetween(
						dateFrom, dateTo, "allianceOpenChanges"));
		serverHistory
				.setNumberOfAllianceMemberChanges(getNumberOfLogsInTableBetween(
						dateFrom, dateTo, "allianceMemberChanges"));
		serverHistory
				.setNumberOfPlanetNameChanges(getNumberOfLogsInTableBetween(
						dateFrom, dateTo, "planetNameChanges"));
		serverHistory.setNumberOfMoonNameChanges(getNumberOfLogsInTableBetween(
				dateFrom, dateTo, "moonNameChanges"));

		LogReader logReader = m_ogdb.getLogReader();
		List<ChangeLogEntry<Player, Planet>> relocationList = logReader
				.getRelocations(dateFrom, dateTo, null);
		serverHistory.setNumberOfRelocations(relocationList.size());

		int relocatedplayers = (int)relocationList.stream()
				.map(logentry -> logentry.getOwner()).distinct().count();
		serverHistory.setNumberOfRelocatedPlayers(relocatedplayers);

		return serverHistory;
	}

	/**
	 * Returns the newest stored ServerActivityState
	 * 
	 * @return the activity state, or null if there is no activity state
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public ServerActivityState getNewestServerActivityState()
			throws SQLException {

		try (Statement stmt = m_ogdb.getDatabaseConnection().createStatement();
				ResultSet rs = stmt
						.executeQuery("SELECT timestamp, notDeletablePlayers, statusString FROM serverActivityStates "
								+ "ORDER BY timestamp DESC LIMIT 1;");) {
			if (rs.next()) {
				ServerActivityState state = new ServerActivityState(
						m_ogdb.getServerPrefix());
				state.setPlayerActivityMapFromString(rs
						.getString("statusString"));
				state.setNumberOfNotDeletablePlayers(rs
						.getInt("notDeletablePlayers"));
				state.setDateTime(rs.getTimestamp("timestamp",
						OGameAPIDatabase.serverCalendar).toLocalDateTime());
				return state;
			} else {
				return null;
			}
		}
	}

	/**
	 * Returns the oldest stored ServerActivityState
	 * 
	 * @return the activity state, or null if there is no activity state
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public ServerActivityState getOldestServerActivityState()
			throws SQLException {

		try (Statement stmt = m_ogdb.getDatabaseConnection().createStatement();
				ResultSet rs = stmt
						.executeQuery("SELECT timestamp, notDeletablePlayers, statusString FROM serverActivityStates "
								+ "ORDER BY timestamp ASC LIMIT 1;");) {
			if (rs.next()) {
				ServerActivityState state = new ServerActivityState(
						m_ogdb.getServerPrefix());
				state.setPlayerActivityMapFromString(rs
						.getString("statusString"));
				state.setNumberOfNotDeletablePlayers(rs
						.getInt("notDeletablePlayers"));
				state.setDateTime(rs.getTimestamp("timestamp",
						OGameAPIDatabase.serverCalendar).toLocalDateTime());
				return state;
			} else {
				return null;
			}
		}
	}

	/**
	 * Get the activity state of the specific date
	 * 
	 * @param date
	 *            , not null
	 * @return the activity state, or null if no such activity state exists.
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public ServerActivityState getServerActivityState(LocalDate date)
			throws SQLException {
		if (date == null)
			return null;

		String sql = "SELECT timestamp, notDeletablePlayers, statusString FROM serverActivityStates "
				+ "WHERE timestamp = ?;";

		try (PreparedStatement ps = m_ogdb.getDatabaseConnection()
				.prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(date), OGameAPIDatabase.serverCalendar);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					ServerActivityState state = new ServerActivityState(
							m_ogdb.getServerPrefix());
					state.setPlayerActivityMapFromString(rs
							.getString("statusString"));
					state.setNumberOfNotDeletablePlayers(rs
							.getInt("notDeletablePlayers"));
					state.setDateTime(rs.getTimestamp("timestamp",
							OGameAPIDatabase.serverCalendar).toLocalDateTime());
					return state;
				} else {
					return null;
				}
			}
		}
	}

	/**
	 * Returns a list of all server activity states for the provided time span
	 * 
	 * @param dateFrom
	 *            , inclusive, not null
	 * @param dateTo
	 *            , exclusive, not null
	 * @return List of server activity states
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public List<ServerActivityState> getServerActivityStates(
			LocalDate dateFrom, LocalDate dateTo) throws SQLException {
		if (dateFrom == null || dateTo == null)
			return Collections.emptyList();

		String sql = "SELECT timestamp, notDeletablePlayers, statusString FROM serverActivityStates "
				+ "WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp ASC;";

		try (PreparedStatement ps = m_ogdb.getDatabaseConnection()
				.prepareStatement(sql);) {
			ps.setDate(1, Date.valueOf(dateFrom),
					OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(dateTo), OGameAPIDatabase.serverCalendar);

			try (ResultSet rs = ps.executeQuery();) {
				final List<ServerActivityState> returnList = new ArrayList<ServerActivityState>();
				while (rs.next()) {
					ServerActivityState state = new ServerActivityState(
							m_ogdb.getServerPrefix());
					state.setPlayerActivityMapFromString(rs
							.getString("statusString"));
					state.setNumberOfNotDeletablePlayers(rs
							.getInt("notDeletablePlayers"));
					state.setDateTime(rs.getTimestamp("timestamp",
							OGameAPIDatabase.serverCalendar).toLocalDateTime());
					returnList.add(state);
				}
				return returnList;
			}
		}

	}

	/**
	 * 
	 * @return number of players in database
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfPlayersInDatabase() throws SQLException {
		int c = getCreatedBetween(LocalDate.of(2000, 1, 1),
				LocalDate.of(3000, 1, 1), "players");
		return c;
	}

	/**
	 * 
	 * @return number of deleted players in database
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfDeletedPlayersInDatabase() throws SQLException {
		int c = getDeletedBetween(LocalDate.of(2000, 1, 1),
				LocalDate.of(3000, 1, 1), "players");
		return c;
	}

	/**
	 * 
	 * @param from
	 *            , inclusive, not null
	 * @param to
	 *            , exclusive, not null
	 * @return number of players created in range
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfPlayersCreatedBetween(LocalDate from, LocalDate to)
			throws SQLException {
		return getCreatedBetween(Objects.requireNonNull(from),
				Objects.requireNonNull(to), "players");
	}

	/**
	 * 
	 * @param from
	 *            , inclusive, not null
	 * @param to
	 *            , exclusive, not null
	 * @return number of players deleted in range
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfPlayersDeletedBetween(LocalDate from, LocalDate to)
			throws SQLException {
		LocalDate from2 = Objects.requireNonNull(from).minusDays(
				OGameAPIDatabase.UPDATEINTERVAL_PLAYERS_DAYS);
		LocalDate to2 = Objects.requireNonNull(to).minusDays(
				OGameAPIDatabase.UPDATEINTERVAL_PLAYERS_DAYS);
		return getDeletedBetween(from2, to2, "players");
	}

	/**
	 * 
	 * @return number of alliances in database
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfAlliancesInDatabase() throws SQLException {
		return getCreatedBetween(LocalDate.of(2000, 1, 1),
				LocalDate.of(3000, 1, 1), "alliances");
	}

	/**
	 * 
	 * @return number of deleted alliances in database
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfDeletedAlliancesInDatabase() throws SQLException {
		return getDeletedBetween(LocalDate.of(2000, 1, 1),
				LocalDate.of(3000, 1, 1), "alliances");
	}

	/**
	 * 
	 * @param from
	 *            , inclusive, not null
	 * @param to
	 *            , exclusive, not null
	 * @return number of alliances created in range
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfAlliancesCreatedBetween(LocalDate from, LocalDate to)
			throws SQLException {
		return getCreatedBetween(Objects.requireNonNull(from),
				Objects.requireNonNull(to), "alliances");
	}

	/**
	 * 
	 * @param from
	 *            , inclusive, not null
	 * @param to
	 *            , exclusive, not null
	 * @return number of alliances deleted in range
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfAlliancesDeletedBetween(LocalDate from, LocalDate to)
			throws SQLException {
		LocalDate from2 = Objects.requireNonNull(from).minusDays(
				OGameAPIDatabase.UPDATEINTERVAL_ALLIANCES_DAYS);
		LocalDate to2 = Objects.requireNonNull(to).minusDays(
				OGameAPIDatabase.UPDATEINTERVAL_ALLIANCES_DAYS);
		return getDeletedBetween(from2, to2, "alliances");
	}

	/**
	 * 
	 * @return number of planets in database
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfPlanetsInDatabase() throws SQLException {
		return getCreatedBetween(LocalDate.of(2000, 1, 1),
				LocalDate.of(3000, 1, 1), "planets");
	}

	/**
	 * 
	 * @return number of deleted planets in database
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfDeletedPlanetsInDatabase() throws SQLException {
		return getDeletedBetween(LocalDate.of(2000, 1, 1),
				LocalDate.of(3000, 1, 1), "planets");
	}

	/**
	 * 
	 * @param from
	 *            , inclusive, not null
	 * @param to
	 *            , exclusive, not null
	 * @return number of planets created in range
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfPlanetsCreatedBetween(LocalDate from, LocalDate to)
			throws SQLException {
		return getCreatedBetween(Objects.requireNonNull(from),
				Objects.requireNonNull(to), "planets");
	}

	/**
	 * 
	 * @param from
	 *            , inclusive, not null
	 * @param to
	 *            , exclusive, not null
	 * @return number of planets deleted in range
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfPlanetsDeletedBetween(LocalDate from, LocalDate to)
			throws SQLException {
		LocalDate from2 = Objects.requireNonNull(from).minusDays(
				OGameAPIDatabase.UPDATEINTERVAL_UNIVERSE_DAYS);
		LocalDate to2 = Objects.requireNonNull(to).minusDays(
				OGameAPIDatabase.UPDATEINTERVAL_UNIVERSE_DAYS);
		return getDeletedBetween(from2, to2, "planets");
	}

	/**
	 * 
	 * @return number of moons in database
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfMoonsInDatabase() throws SQLException {
		return getCreatedBetween(LocalDate.of(2000, 1, 1),
				LocalDate.of(3000, 1, 1), "moons");
	}

	/**
	 * 
	 * @return number of deleted moons in database
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfDeletedMoonsInDatabase() throws SQLException {
		return getDeletedBetween(LocalDate.of(2000, 1, 1),
				LocalDate.of(3000, 1, 1), "moons");
	}

	/**
	 * 
	 * @param from
	 *            , inclusive, not null
	 * @param to
	 *            , exclusive, not null
	 * @return number of moons created in range
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfMoonsCreatedBetween(LocalDate from, LocalDate to)
			throws SQLException {
		return getCreatedBetween(Objects.requireNonNull(from),
				Objects.requireNonNull(to), "moons");
	}

	/**
	 * 
	 * @param from
	 *            , inclusive, not null
	 * @param to
	 *            , exclusive, not null
	 * @return number of moons deleted in range
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getNumberOfMoonsDeletedBetween(LocalDate from, LocalDate to)
			throws SQLException {
		LocalDate from2 = Objects.requireNonNull(from).minusDays(
				OGameAPIDatabase.UPDATEINTERVAL_UNIVERSE_DAYS);
		LocalDate to2 = Objects.requireNonNull(to).minusDays(
				OGameAPIDatabase.UPDATEINTERVAL_UNIVERSE_DAYS);
		return getDeletedBetween(from2, to2, "moons");
	}

	private int getCreatedBetween(LocalDate from, LocalDate to, String table)
			throws SQLException {
		String sql = "Select count(*) AS count FROM " + table
				+ " WHERE insertedOn >= ? AND insertedOn <= ?;";
		int count = -1;
		try (PreparedStatement ps = m_ogdb.getDatabaseConnection()
				.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					count = rs.getInt(1);
				}
			}
		}

		return count;
	}

	private int getDeletedBetween(LocalDate from, LocalDate to, String table)
			throws SQLException {
		String sql = "Select count(*) AS count FROM " + table
				+ " WHERE lastUpdate <> (SELECT max(lastUpdate) FROM " + table
				+ ") " + "AND lastUpdate >= ? AND lastUpdate <= ?;";
		int count = -1;
		try (PreparedStatement ps = m_ogdb.getDatabaseConnection()
				.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					count = rs.getInt(1);
				}
			}
		}

		return count;
	}

	private int getNumberOfLogsInTableBetween(LocalDate from, LocalDate to,
			String logTableName) throws SQLException {
		final String sql = "SELECT count(*) from " + logTableName
				+ " WHERE timestamp >= ? AND timestamp <= ? ; ";

		int count = -1;
		try (PreparedStatement ps = m_ogdb.getDatabaseConnection()
				.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(from), OGameAPIDatabase.serverCalendar);
			ps.setDate(2, Date.valueOf(to), OGameAPIDatabase.serverCalendar);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					count = rs.getInt(1);
				}
			}
		}

		return count;
	}

}
