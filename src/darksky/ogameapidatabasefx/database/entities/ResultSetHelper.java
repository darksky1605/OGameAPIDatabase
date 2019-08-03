package darksky.ogameapidatabasefx.database.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

import darksky.ogameapidatabasefx.database.databasemanagement.DatabaseSettings;
import darksky.ogameapidatabasefx.database.entities.Highscore.HighscoreType;

public class ResultSetHelper {

	public static DatabaseSettings getDatabaseSettingsFromResultSet(ResultSet rs, String suffix) throws SQLException {
		Objects.requireNonNull(rs, "resultset is null");
		Objects.requireNonNull(suffix, "suffix is null");

		DatabaseSettings settings = new DatabaseSettings(rs.getInt("databaseVersion"),
				rs.getBoolean("saveActivityStates" + suffix), rs.getBoolean("savePlanetDistribution" + suffix),
				rs.getBoolean("saveHighscoreDistribution" + suffix), rs.getInt("maxHighscoreEntriesPerEntity" + suffix));

		return settings;
	}

	/**
	 * Creates a Timestamps from the current row of the result set. The columns
	 * must be named the same as the actual column names in the
	 * databaseInformtion table + an additional suffix
	 * 
	 * @param rs
	 *            the open result set, not null
	 * @param suffix
	 *            the suffix, not null
	 * @return the Timestamps
	 * @throws SQLException
	 *             if a column in rs could not be accessed
	 */
	public static Timestamps getTimestampsFromResultSet(ResultSet rs, String suffix) throws SQLException {
		Objects.requireNonNull(rs, "resultset is null");
		Objects.requireNonNull(suffix, "suffix is null");

		Timestamps t = new Timestamps(rs.getTimestamp("updateTimestamp").toInstant(),
				rs.getTimestamp("serverDataTimestamp" + suffix).toInstant(),
				rs.getTimestamp("playersTimestamp" + suffix).toInstant(),
				rs.getTimestamp("universeTimestamp" + suffix).toInstant(),
				rs.getTimestamp("alliancesTimestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore10Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore11Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore12Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore13Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore14Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore15Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore16Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore17Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore20Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore21Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore22Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore23Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore24Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore25Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore26Timestamp" + suffix).toInstant(),
				rs.getTimestamp("highscore27Timestamp" + suffix).toInstant());
		return t;
	}

	/**
	 * Creates a ServerData from the current row of the result set. The columns
	 * must be named the same as the actual column names in the serverData table
	 * + an additional suffix
	 * 
	 * @param rs
	 *            the open result set, not null
	 * @param suffix
	 *            the suffix, not null
	 * @return the ServerData
	 * @throws SQLException
	 *             if a column in rs could not be accessed
	 */
	public static ServerData getServerDataFromResultSet(ResultSet rs, String suffix) throws SQLException {
		Objects.requireNonNull(rs, "resultset is null");
		Objects.requireNonNull(suffix, "suffix is null");

		ServerData serverData = new ServerData(rs.getString("domain" + suffix), rs.getString("name" + suffix),
				rs.getInt("number" + suffix), rs.getString("language" + suffix), rs.getString("timezone" + suffix),
				rs.getString("timezoneOffset" + suffix), rs.getString("version" + suffix), rs.getInt("speed" + suffix),
				rs.getInt("speedFleet" + suffix), rs.getInt("galaxies" + suffix), rs.getInt("systems" + suffix),
				rs.getBoolean("acs" + suffix), rs.getBoolean("rapidFire" + suffix), rs.getBoolean("defToTF" + suffix),
				rs.getFloat("debrisFactor" + suffix), rs.getFloat("repairFactor" + suffix),
				rs.getInt("newbieProtectionLimit" + suffix), rs.getInt("newbieProtectionHigh" + suffix),
				rs.getInt("topScore" + suffix), rs.getInt("bonusFields" + suffix),
				rs.getBoolean("donutGalaxy" + suffix), rs.getBoolean("donutSystem" + suffix), rs.getFloat("debrisFactorDef"+suffix),
				rs.getBoolean("wfEnabled"+suffix), rs.getInt("wfMinimumRessLost"+suffix), rs.getInt("wfMinimumLossPercentage"+suffix), 
				rs.getInt("wfBasicPercentageRepairable"+suffix), rs.getFloat("globalDeuteriumSaveFactor"+suffix),
				rs.getInt("bashlimit"+suffix), rs.getInt("probeCargo"+suffix),
				rs.getInt("researchDurationDivisor"+suffix), rs.getInt("darkMatterNewAcount"+suffix),
				rs.getInt("cargoHyperspaceTechMultiplier"+suffix)); 
		return serverData;
	}

	/**
	 * Creates a Highscore from the current row of the result set. The columns
	 * must be named the same as the actual column names in the (players /
	 * alliances) table + an additional suffix
	 * 
	 * @param rs
	 *            the open result set, not null
	 * @param suffix
	 *            the suffix, not null
	 * @return the Highscore
	 * @throws SQLException
	 *             if a column in rs could not be accessed
	 */
	private static Highscore getBasicHighscoreFromResultSet(ResultSet rs, String suffix) throws SQLException {
		Objects.requireNonNull(rs, "resultset is null");
		Objects.requireNonNull(suffix, "suffix is null");

		Instant lastUpdate = rs.getTimestamp("timestamp" + suffix).toInstant();
		Highscore highscore = new Highscore(lastUpdate);
		
		long ships = 0;
		try{
			ships = rs.getLong("ships" + suffix);
		}catch(SQLException e){}
		
		highscore.setEntry(HighscoreType.Total, rs.getLong("totalPoints" + suffix), rs.getInt("totalRank" + suffix));
		highscore.setEntry(HighscoreType.Economy, rs.getLong("economyPoints" + suffix),
				rs.getInt("economyRank" + suffix));
		highscore.setEntry(HighscoreType.Research, rs.getLong("researchPoints" + suffix),
				rs.getInt("researchRank" + suffix));
		highscore.setEntry(HighscoreType.Military, rs.getLong("militaryPoints" + suffix),
				rs.getInt("militaryRank" + suffix));
		highscore.setEntry(HighscoreType.MilitaryBuilt, rs.getLong("militaryBuiltPoints" + suffix),
				rs.getInt("militaryBuiltRank" + suffix));
		highscore.setEntry(HighscoreType.MilitaryDestroyed, rs.getLong("militaryDestroyedPoints" + suffix),
				rs.getInt("militaryDestroyedRank" + suffix));
		highscore.setEntry(HighscoreType.MilitaryLost, rs.getLong("militaryLostPoints" + suffix),
				rs.getInt("militaryLostRank" + suffix));
		highscore.setEntry(HighscoreType.Honor, rs.getLong("honorPoints" + suffix), rs.getInt("honorRank" + suffix));
		
		highscore.setEntry(HighscoreType.Ships, ships, 0);

		return highscore;
	}
	
	public static Highscore getPlayerHighscoreFromResultSet(ResultSet rs, String suffix) throws SQLException {
		Highscore h = getBasicHighscoreFromResultSet(rs, suffix);
		return h;
	}
	
	public static Highscore getAllianceHighscoreFromResultSet(ResultSet rs, String suffix) throws SQLException {
		Highscore h = getBasicHighscoreFromResultSet(rs, suffix);
		return h;
	}

	/**
	 * Creates a Player from the current row of the result set. The columns must
	 * be named the same as the actual column names in the players table + an
	 * additional suffix
	 * 
	 * @param rs
	 *            the open result set, not null
	 * @param suffix
	 *            the suffix, not null
	 * @return the Player, or null
	 * @throws SQLException
	 *             if a column in rs could not be accessed
	 */
	public static Player getPlayerFromResultSet(ResultSet rs, String suffix) throws SQLException {

		Objects.requireNonNull(rs, "resultset is null");
		Objects.requireNonNull(suffix, "playerSuffix is null");

		Timestamp inT = rs.getTimestamp("insertedOn" + suffix);
		Timestamp lT = rs.getTimestamp("lastUpdate" + suffix);
		
		if(inT == null || lT == null) return null;
		
		Instant insertedOn = inT.toInstant();
		Instant lastUpdate = lT.toInstant();

		Player player = new Player(rs.getInt("playerId" + suffix), rs.getString("playerName" + suffix), insertedOn,
				lastUpdate, rs.getBoolean("isDeleted" + suffix), rs.getString("playerStatus" + suffix),
				rs.getInt("allianceId" + suffix));

		return player;
	}

	/**
	 * Creates a Alliance from the current row of the result set. The columns
	 * must be named the same as the actual column names in the alliances table
	 * + an additional suffix
	 * 
	 * @param rs
	 *            the open result set, not null
	 * @param suffix
	 *            the suffix, not null
	 * @return the Alliance, or null
	 * @throws SQLException
	 *             if a column in rs could not be accessed
	 */
	public static Alliance getAllianceFromResultSet(ResultSet rs, String suffix) throws SQLException {

		Objects.requireNonNull(rs, "resultset is null");
		Objects.requireNonNull(suffix, "allianceSuffix is null");

		Timestamp inT = rs.getTimestamp("insertedOn" + suffix);
		Timestamp lT = rs.getTimestamp("lastUpdate" + suffix);
		
		if(inT == null || lT == null) return null;
		
		Instant insertedOn = inT.toInstant();
		Instant lastUpdate = lT.toInstant();

		Alliance alliance = new Alliance(rs.getInt("allianceId" + suffix), rs.getString("allianceName" + suffix),
				insertedOn, lastUpdate, rs.getBoolean("isDeleted" + suffix),
				rs.getString("allianceTag" + suffix), rs.getString("homepage" + suffix), rs.getString("logo" + suffix),
				rs.getBoolean("open" + suffix));

		return alliance;
	}

	/**
	 * Creates a Planet from the current row of the result set. The columns must
	 * be named the same as the actual column names in the planets table + an
	 * additional suffix
	 * 
	 * @param rs
	 *            the open result set, not null
	 * @param suffix
	 *            the suffix, not null
	 * @return the Planet
	 * @throws SQLException
	 *             if a column in rs could not be accessed
	 */
	public static Planet getPlanetFromResultSet(ResultSet rs, String suffix) throws SQLException {

		Objects.requireNonNull(rs, "resultset is null");
		Objects.requireNonNull(suffix, "suffix is null");
		
		Timestamp inT = rs.getTimestamp("insertedOn" + suffix);
		Timestamp lT = rs.getTimestamp("lastUpdate" + suffix);
		
		if(inT == null || lT == null) return null;

		Instant insertedOn = inT.toInstant();
		Instant lastUpdate = lT.toInstant();

		Planet planet = new Planet(rs.getInt("planetId" + suffix), rs.getString("planetname" + suffix), insertedOn,
				lastUpdate, rs.getBoolean("isDeleted" + suffix), rs.getInt("galaxy" + suffix),
				rs.getInt("system" + suffix), rs.getInt("position" + suffix), rs.getInt("playerId" + suffix));

		return planet;
	}

	/**
	 * Creates a Moon from the current row of the result set. The columns must
	 * be named the same as the actual column names in the moons table + an
	 * additional suffix
	 * 
	 * @param rs
	 *            the open result set, not null
	 * @param suffix
	 *            the suffix, not null
	 * @return the Moon, or null
	 * @throws SQLException
	 *             if a column in rs could not be accessed
	 */
	public static Moon getMoonFromResultSet(ResultSet rs, String suffix) throws SQLException {

		Objects.requireNonNull(rs, "resultset is null");
		Objects.requireNonNull(suffix, "suffix is null");

		Timestamp inT = rs.getTimestamp("insertedOn" + suffix);
		Timestamp lT = rs.getTimestamp("lastUpdate" + suffix);
		
		if(inT == null || lT == null) return null;
		
		Instant insertedOn = inT.toInstant();
		Instant lastUpdate = lT.toInstant();

		Moon moon = new Moon(rs.getInt("moonId" + suffix), rs.getString("moonName" + suffix), insertedOn, lastUpdate,
				rs.getBoolean("isDeleted" + suffix), rs.getInt("moonSize" + suffix), rs.getInt("playerId" + suffix),
				rs.getInt("planetId" + suffix));

		return moon;
	}
}
