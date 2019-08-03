package darksky.ogameapidatabasefx.database.databasemanagement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

class DatabaseCreator {

	private static final Logger logger = Logger.getLogger(DatabaseCreator.class.getName());

	/**
	 * The version of OGame database
	 */
	static final int DATABASE_VERSION = 1;

	static final int DEFAULT_ALLIANCE_ID = 999999;
	private static final String DEFAULT_ALLIANCE_NAME = "";
	private static final String DEFAULT_ALLIANCE_TAG = "";

	private OGameAPIDatabase ogdb = null;

	DatabaseCreator(OGameAPIDatabase ogdb) {
		this.ogdb = Objects.requireNonNull(ogdb);
	}

	/**
	 * Creates the necessary tables in the database if they do not already exist
	 * 
	 * @throws SQLException
	 */
	void createDatabaseTables(DatabaseSettings databaseSettings) throws SQLException {
		logger.fine(ogdb.getServerPrefix() + " createDatabaseTables");
		logger.info(ogdb.getServerPrefix() + " - creating database");

		boolean exists = false;
		try (Statement stmt = ogdb.getDatabaseConnection().createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM sqlite_master LIMIT 1;")) {
			exists = rs.next();
		}

		Connection con = ogdb.getDatabaseConnection();
		SQLException ex = null;

		final boolean oldAutoCommit = con.getAutoCommit();
		con.setAutoCommit(false);

		try (Statement stmt = ogdb.getDatabaseConnection().createStatement()) {
			// settings
			stmt.execute("CREATE TABLE IF NOT EXISTS databaseSettings (" + "databaseVersion INTEGER NOT NULL DEFAULT "
					+ DATABASE_VERSION
					+ ", saveActivityStates INTEGER NOT NULL DEFAULT 0, savePlanetDistribution INTEGER NOT NULL DEFAULT 0, "
					+ "saveHighscoreDistribution INTEGER NOT NULL DEFAULT 0, maxHighscoreEntriesPerEntity INTEGER NOT NULL DEFAULT 1);");

			// timestamps
			stmt.execute("CREATE TABLE IF NOT EXISTS timestamps ("
					+ "updateTimestamp INTEGER PRIMARY KEY, serverDataTimestamp INTEGER NOT NULL DEFAULT 0, playersTimestamp INTEGER NOT NULL DEFAULT 0, "
					+ "universeTimestamp INTEGER NOT NULL DEFAULT 0, alliancesTimestamp INTEGER DEFAULT 0, "
					+ "highscore10Timestamp INTEGER NOT NULL DEFAULT 0, highscore11Timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "highscore12Timestamp INTEGER NOT NULL DEFAULT 0, highscore13Timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "highscore14Timestamp INTEGER NOT NULL DEFAULT 0, highscore15Timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "highscore16Timestamp INTEGER NOT NULL DEFAULT 0, highscore17Timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "highscore20Timestamp INTEGER NOT NULL DEFAULT 0, highscore21Timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "highscore22Timestamp INTEGER NOT NULL DEFAULT 0, highscore23Timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "highscore24Timestamp INTEGER NOT NULL DEFAULT 0, highscore25Timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "highscore26Timestamp INTEGER NOT NULL DEFAULT 0, highscore27Timestamp INTEGER NOT NULL DEFAULT 0);");

			// server settings
			stmt.execute("CREATE TABLE IF NOT EXISTS serverData ("
					+ "domain PRIMARY KEY, name NOT NULL DEFAULT '', number INTEGER NOT NULL DEFAULT 0, language NOT NULL DEFAULT '', "
					+ "timezone NOT NULL DEFAULT '', timezoneOffset NOT NULL DEFAULT '+00:00', version NOT NULL DEFAULT '', speed INTEGER NOT NULL DEFAULT 1, "
					+ "speedFleet INTEGER NOT NULL DEFAULT 1, galaxies INTEGER NOT NULL DEFAULT 9, systems INTEGER NOT NULL DEFAULT 499, "
					+ "acs INTEGER NOT NULL DEFAULT 1, rapidFire INTEGER NOT NULL DEFAULT 1, defToTF INTEGER NOT NULL DEFAULT 0, debrisFactor REAL NOT NULL DEFAULT 0.3, "
					+ "repairFactor REAL NOT NULL DEFAULT 0.7, newbieProtectionLimit INTEGER NOT NULL DEFAULT 50000, newbieProtectionHigh INTEGER NOT NULL DEFAULT 10000, "
					+ "topScore INTEGER NOT NULL DEFAULT 0, bonusFields INTEGER NOT NULL DEFAULT 0, donutGalaxy INTEGER NOT NULL DEFAULT 1, donutSystem INTEGER NOT NULL DEFAULT 1,"
					+ "debrisFactorDef REAL NOT NULL DEFAULT 0.3, wfEnabled INTEGER NOT NULL DEFAULT 1, wfMinimumRessLost INTEGER NOT NULL DEFAULT 150000, "
					+ "wfMinimumLossPercentage INTEGER NOT NULL DEFAULT 5, wfBasicPercentageRepairable INTEGER NOT NULL DEFAULT 45, "
					+ "globalDeuteriumSaveFactor REAL NOT NULL DEFAULT 1.0, "
					+ "bashlimit INTEGER NOT NULL DEFAULT 0, "
					+ "probeCargo INTEGER NOT NULL DEFAULT 0, "
					+ "researchDurationDivisor INTEGER NOT NULL DEFAULT 1, "
					+ "darkMatterNewAcount INTEGER NOT NULL DEFAULT 0, "
					+ "cargoHyperspaceTechMultiplier INTEGER NOT NULL DEFAULT 1"
					+ ");");

			// players
			stmt.execute("CREATE TABLE IF NOT EXISTS players ("
					+ "playerId INTEGER PRIMARY KEY, playerName NOT NULL DEFAULT '', playerStatus NOT NULL DEFAULT 'unknown', allianceId INTEGER NOT NULL DEFAULT "
					+ DEFAULT_ALLIANCE_ID
					+ ", insertedOn INTEGER NOT NULL DEFAULT 0, lastUpdate INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(allianceId) REFERENCES alliances(allianceId));");

			// player highscore
			stmt.execute("CREATE TABLE IF NOT EXISTS playerHighscores ("
					+ "playerId INTEGER NOT NULL DEFAULT 0, timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "totalPoints INTEGER NOT NULL DEFAULT 0, totalRank INTEGER NOT NULL DEFAULT 0, "
					+ "economyPoints INTEGER NOT NULL DEFAULT 0, economyRank INTEGER NOT NULL DEFAULT 0, "
					+ "researchPoints INTEGER NOT NULL DEFAULT 0, researchRank INTEGER NOT NULL DEFAULT 0, "
					+ "militaryPoints INTEGER NOT NULL DEFAULT 0, militaryRank INTEGER NOT NULL DEFAULT 0, ships INTEGER NOT NULL DEFAULT 0, "
					+ "militaryBuiltPoints INTEGER NOT NULL DEFAULT 0, militaryBuiltRank INTEGER NOT NULL DEFAULT 0, "
					+ "militaryDestroyedPoints INTEGER NOT NULL DEFAULT 0, militaryDestroyedRank INTEGER NOT NULL DEFAULT 0, "
					+ "militaryLostPoints INTEGER NOT NULL DEFAULT 0, militaryLostRank INTEGER NOT NULL DEFAULT 0, "
					+ "honorPoints INTEGER NOT NULL DEFAULT 0, honorRank INTEGER NOT NULL DEFAULT 0, "
					+ "PRIMARY KEY (playerId, timestamp), "
					+ "FOREIGN KEY(playerId) REFERENCES players(playerId) ON DELETE CASCADE);");

			// alliances
			stmt.execute("CREATE TABLE IF NOT EXISTS alliances ("
					+ "allianceId INTEGER PRIMARY KEY, allianceName NOT NULL DEFAULT '" + DEFAULT_ALLIANCE_NAME
					+ "', allianceTag NOT NULL DEFAULT '" + DEFAULT_ALLIANCE_TAG + "', "
					+ "homepage NOT NULL DEFAULT '', logo NOT NULL DEFAULT '', open INTEGER NOT NULL DEFAULT 1, "
					+ "insertedOn INTEGER NOT NULL DEFAULT 0, lastUpdate INTEGER NOT NULL DEFAULT 0);");

			// alliance highscore
			stmt.execute("CREATE TABLE IF NOT EXISTS allianceHighscores ("
					+ "allianceId INTEGER NOT NULL DEFAULT 0, timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "totalPoints INTEGER NOT NULL DEFAULT 0, totalRank INTEGER NOT NULL DEFAULT 0, "
					+ "economyPoints INTEGER NOT NULL DEFAULT 0, economyRank INTEGER NOT NULL DEFAULT 0, "
					+ "researchPoints INTEGER NOT NULL DEFAULT 0, researchRank INTEGER NOT NULL DEFAULT 0, "
					+ "militaryPoints INTEGER NOT NULL DEFAULT 0, militaryRank INTEGER NOT NULL DEFAULT 0, "
					+ "militaryBuiltPoints INTEGER NOT NULL DEFAULT 0, militaryBuiltRank INTEGER NOT NULL DEFAULT 0, "
					+ "militaryDestroyedPoints INTEGER NOT NULL DEFAULT 0, militaryDestroyedRank INTEGER NOT NULL DEFAULT 0, "
					+ "militaryLostPoints INTEGER NOT NULL DEFAULT 0, militaryLostRank INTEGER NOT NULL DEFAULT 0, "
					+ "honorPoints INTEGER NOT NULL DEFAULT 0, honorRank INTEGER NOT NULL DEFAULT 0, "
					+ "PRIMARY KEY (allianceId, timestamp), "
					+ "FOREIGN KEY(allianceId) REFERENCES alliances(allianceId) ON DELETE CASCADE);");

			// planets
			stmt.execute("CREATE TABLE IF NOT EXISTS planets ("
					+ "planetId INTEGER PRIMARY KEY, planetName NOT NULL DEFAULT '', "
					+ "galaxy INTEGER NOT NULL DEFAULT 1, system INTEGER NOT NULL DEFAULT 1, position INTEGER NOT NULL DEFAULT 1, "
					+ "playerId INTEGER NOT NULL, insertedOn INTEGER NOT NULL DEFAULT 0, lastUpdate INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(playerId) REFERENCES players(playerId) ON DELETE CASCADE);");

			// moons
			stmt.execute("CREATE TABLE IF NOT EXISTS moons ("
					+ "moonId INTEGER PRIMARY KEY, moonName NOT NULL DEFAULT '', moonSize INTEGER NOT NULL DEFAULT 0, "
					+ "playerId INTEGER NOT NULL, planetId INTEGER NOT NULL, insertedOn INTEGER NOT NULL DEFAULT 0, lastUpdate INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(playerId) REFERENCES players(playerId) ON DELETE CASCADE, FOREIGN KEY(planetId) REFERENCES planets(planetId) ON DELETE CASCADE);");

			// log tables
			stmt.execute("CREATE TABLE IF NOT EXISTS playerNameChanges ("
					+ "playerId INTEGER, oldName NOT NULL DEFAULT '', newName NOT NULL DEFAULT '', timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(playerId) REFERENCES players(playerId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS playerStatusChanges ("
					+ "playerId INTEGER, oldStatus NOT NULL DEFAULT '', newStatus NOT NULL DEFAULT '', timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(playerId) REFERENCES players(playerId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS allianceNameChanges ("
					+ "allianceId INTEGER, oldName NOT NULL DEFAULT '', newName NOT NULL DEFAULT '', timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(allianceId) REFERENCES alliances(allianceId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS allianceTagChanges ("
					+ "allianceId INTEGER, oldTag NOT NULL DEFAULT '', newTag NOT NULL DEFAULT '', timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(allianceId) REFERENCES alliances(allianceId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS allianceHomepageChanges ("
					+ "allianceId INTEGER, oldHomepage NOT NULL DEFAULT '', newHomepage NOT NULL DEFAULT '', timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(allianceId) REFERENCES alliances(allianceId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS allianceLogoChanges ("
					+ "allianceId INTEGER, oldLogo NOT NULL DEFAULT '', newLogo NOT NULL DEFAULT '', timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(allianceId) REFERENCES alliances(allianceId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS allianceOpenChanges ("
					+ "allianceId INTEGER, oldOpen NOT NULL DEFAULT '', newOpen NOT NULL DEFAULT '', timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(allianceId) REFERENCES alliances(allianceId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS allianceMemberChanges ("
					+ "playerId INTEGER, oldAllianceId INTEGER NOT NULL DEFAULT " + DEFAULT_ALLIANCE_ID
					+ ", newAllianceId INTEGER NOT NULL DEFAULT " + DEFAULT_ALLIANCE_ID + ", "
					+ "timestamp INTEGER NOT NULL DEFAULT 0, " + "FOREIGN KEY(playerId) REFERENCES players(playerId), "
					+ "FOREIGN KEY(oldAllianceId) REFERENCES alliances(allianceId), "
					+ "FOREIGN KEY(newAllianceId) REFERENCES alliances(allianceId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS planetNameChanges ("
					+ "planetId INTEGER, oldName NOT NULL DEFAULT '', newName NOT NULL DEFAULT '', timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(planetId) REFERENCES planets(planetId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS moonNameChanges ("
					+ "moonId INTEGER, oldName NOT NULL DEFAULT '', newName NOT NULL DEFAULT '', timestamp INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(moonId) REFERENCES moons(moonId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS relocations (" + "playerId INTEGER, planetId INTEGER, "
					+ "oldGalaxy INTEGER NOT NULL DEFAULT 1, newGalaxy INTEGER NOT NULL DEFAULT 1, "
					+ "oldSystem INTEGER NOT NULL DEFAULT 1, newSystem INTEGER NOT NULL DEFAULT 1, "
					+ "oldPosition INTEGER NOT NULL DEFAULT 1, newPosition INTEGER NOT NULL DEFAULT 1, "
					+ "timestamp INTEGER NOT NULL DEFAULT 0, " + "FOREIGN KEY(playerId) REFERENCES players(playerId), "
					+ "FOREIGN KEY(planetId) REFERENCES planets(planetId));");

			stmt.execute("CREATE TABLE IF NOT EXISTS serverActivityStates ("
					+ "timestamp INTEGER PRIMARY KEY, notDeletablePlayers INTEGER NOT NULL DEFAULT 0, statusString NOT NULL DEFAULT '');");

			stmt.execute("CREATE TABLE IF NOT EXISTS planetDistributions ("
					+ "timestamp INTEGER PRIMARY KEY, galaxies INTEGER NOT NULL DEFAULT 9, systems INTEGER NOT NULL DEFAULT 499, "
					+ "planetDistributionString NOT NULL DEFAULT '');");

			stmt.execute("CREATE TABLE IF NOT EXISTS moonDistributions ("
					+ "timestamp INTEGER PRIMARY KEY, galaxies INTEGER NOT NULL DEFAULT 9, systems INTEGER NOT NULL DEFAULT 499, "
					+ "moonDistributionString NOT NULL DEFAULT '');");

			stmt.execute("CREATE TABLE IF NOT EXISTS highscoreDistributions("
					+ "timestamp INTEGER PRIMARY KEY, bins INTEGER NOT NULL DEFAULT 0, highscoreDistributionString NOT NULL DEFAULT '');");

			// trigger

			stmt.execute("CREATE TRIGGER IF NOT EXISTS playerNameChange AFTER UPDATE ON players " + "FOR EACH ROW "
					+ "WHEN old.playerName <> '' AND old.playerName <> new.playerName BEGIN "
					+ "INSERT INTO playerNameChanges VALUES (old.playerId, old.playerName, new.playerName, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS playerStatusChange AFTER UPDATE ON players " + "FOR EACH ROW "
					+ "WHEN old.playerStatus <> 'unknown' AND old.playerStatus <> new.playerStatus BEGIN "
					+ "INSERT INTO playerStatusChanges VALUES (old.playerId, old.playerStatus, new.playerStatus, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS allianceMemberChange AFTER UPDATE ON players " + "FOR EACH ROW "
					+ "WHEN old.allianceId <> new.allianceId BEGIN "
					+ "INSERT INTO allianceMemberChanges VALUES (old.playerId, old.allianceId, new.allianceId, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS allianceNameChange AFTER UPDATE ON alliances " + "FOR EACH ROW "
					+ "WHEN old.allianceName <> '' AND old.allianceName <> '" + DEFAULT_ALLIANCE_NAME
					+ "' AND old.allianceName <> new.allianceName BEGIN "
					+ "INSERT INTO allianceNameChanges VALUES (old.allianceId, old.allianceName, new.allianceName, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS allianceTagChange AFTER UPDATE ON alliances " + "FOR EACH ROW "
					+ "WHEN old.allianceTag <> '' AND old.allianceTag <> '" + DEFAULT_ALLIANCE_TAG
					+ "' AND old.allianceTag <> new.allianceTag BEGIN "
					+ "INSERT INTO allianceTagChanges VALUES (old.allianceId, old.allianceTag, new.allianceTag, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS allianceHomepageChange AFTER UPDATE ON alliances "
					+ "FOR EACH ROW " + "WHEN old.homepage <> 'DEFAULT' AND old.homepage <> new.homepage BEGIN "
					+ "INSERT INTO allianceHomepageChanges VALUES (old.allianceId, old.homepage, new.homepage, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS allianceLogoChange AFTER UPDATE ON alliances " + "FOR EACH ROW "
					+ "WHEN old.logo <> 'DEFAULT' AND old.logo <> new.logo BEGIN "
					+ "INSERT INTO allianceLogoChanges VALUES (old.allianceId, old.logo, new.logo, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS allianceOpenChange AFTER UPDATE ON alliances " + "FOR EACH ROW "
					+ "WHEN old.open <> new.open BEGIN "
					+ "INSERT INTO allianceOpenChanges VALUES (old.allianceId, old.open, new.open, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS planetNameChange AFTER UPDATE ON planets " + "FOR EACH ROW "
					+ "WHEN old.planetName <> new.planetName BEGIN "
					+ "INSERT INTO planetNameChanges VALUES (old.planetId, old.planetName, new.planetName, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS moonNameChange AFTER UPDATE ON moons " + "FOR EACH ROW "
					+ "WHEN old.moonName <> new.moonName BEGIN "
					+ "INSERT INTO moonNameChanges VALUES (old.moonId, old.moonName, new.moonName, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS relocation AFTER UPDATE ON planets " + "FOR EACH ROW "
					+ "WHEN old.galaxy <> new.galaxy OR old.system <> new.system OR old.position <> new.position BEGIN "
					+ "INSERT INTO relocations VALUES (old.playerId, old.planetId, old.galaxy, new.galaxy, old.system, new.system, old.position, new.position, new.lastUpdate); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS addHighscoreForPlayer AFTER INSERT ON players " + "FOR EACH ROW "
					+ "BEGIN "
					+ "INSERT INTO playerHighscores (playerId, timestamp) VALUES (new.playerId, new.insertedOn); "
					+ "END;");

			stmt.execute("CREATE TRIGGER IF NOT EXISTS addHighscoreAlliancePlayer AFTER INSERT ON alliances "
					+ "FOR EACH ROW " + "BEGIN "
					+ "INSERT INTO allianceHighscores (allianceId, timestamp) VALUES (new.allianceId, new.insertedOn); "
					+ "END;");

			// store player status distribution of non-deleted players after
			// players
			// have been updated

			stmt.execute(
					"CREATE TRIGGER IF NOT EXISTS updateServerActivityStates AFTER UPDATE OF playersTimestamp ON timestamps "
							+ "WHEN (SELECT saveActivityStates FROM databaseSettings) = 1  AND old.playersTimestamp < new.playersTimestamp BEGIN "
							// update current state, if exists
							+ "UPDATE serverActivityStates SET statusString = " + "   (SELECT group_concat(stat)  "
							+ "   FROM (" + "      SELECT (playerstatus || '=' || count(*)) AS stat, 1 AS one "
							+ "      FROM players " + "      WHERE lastupdate = (SELECT MAX(lastUpdate) FROM players) "
							+ "      GROUP BY playerstatus ORDER BY playerstatus ASC " + "   )GROUP BY one) "
							+ "WHERE timestamp = new.playersTimestamp " + "; "
							// insert new state, if not exists
							+ "INSERT OR IGNORE INTO serverActivityStates (timestamp, statusString) "
							+ "   SELECT new.playersTimestamp, group_concat(stat)  " + "   FROM ("
							+ "      SELECT (playerstatus || '=' || count(*)) AS stat, 1 AS one "
							+ "      FROM players " + "      WHERE lastupdate = (SELECT MAX(lastUpdate) FROM players) "
							+ "      GROUP BY playerstatus ORDER BY playerstatus ASC " + "   )GROUP BY one " + "; "
							// update number of not deletable players
							+ "UPDATE serverActivityStates " + "SET notDeletablePlayers = "
							+ "    (SELECT (MAX(0, A.Iplayers - B.newIplayers)) AS notDeletablePlayers FROM "
							+ "        (SELECT count(*) AS Iplayers FROM players "
							+ "        WHERE charindex('I' , playerStatus) > 0 AND lastupdate = (SELECT MAX(lastUpdate) FROM players) "
							+ "        ) AS A, "
							+ "        (SELECT count(*) AS newIplayers FROM playerStatusChanges psc JOIN players ON psc.playerId = players.playerId "
							+ "         WHERE charindex('I' ,psc.newStatus) > 0 AND playerstatus = psc.newstatus "
							+ "         AND (julianday('now') - julianday(psc.timestamp/1000, 'unixepoch')) <= 7 "
							+ "         AND lastupdate = (SELECT MAX(lastUpdate) FROM players) " + "        ) AS B "
							+ "    ) " + "WHERE timestamp = new.playersTimestamp " + "; " + "END;");

			// store planet and moon distribution
			stmt.execute(
					"CREATE TRIGGER IF NOT EXISTS universeTimestampUpdate AFTER UPDATE OF universeTimestamp ON timestamps "
							+ "WHEN old.universeTimestamp < new.universeTimestamp AND (SELECT savePlanetDistribution FROM databaseSettings) = 1 BEGIN "
							+ "INSERT OR IGNORE INTO planetDistributions (timestamp, galaxies, systems, planetDistributionString) "
							+ "   SELECT new.universeTimestamp, "
							+ "          (select galaxies from serverData), (select systems from serverData),"
							+ "          group_concat(galaxy || ' ' || system || ' ' || count) " + "   FROM "
							+ "      (SELECT galaxy, system, count(*) AS count " + "       FROM planets "
							+ "       WHERE planets.lastUpdate = (SELECT MAX(lastUpdate) FROM planets) "
							+ "       GROUP BY galaxy, system) " + "; " + ""
							+ "INSERT OR IGNORE INTO moonDistributions (timestamp, galaxies, systems, moonDistributionString)"
							+ "   SELECT new.universeTimestamp, "
							+ "          (select galaxies from serverData), (select systems from serverData),"
							+ "          group_concat(galaxy || ' ' || system || ' ' || count) " + "   FROM "
							+ "      (SELECT galaxy, system, count(*) AS count "
							+ "       FROM planets JOIN moons ON planets.planetId = moons.planetId "
							+ "       WHERE planets.lastUpdate = (SELECT MAX(lastUpdate) FROM planets) "
							+ "         AND moons.lastUpdate = (SELECT MAX(lastUpdate) FROM moons) "
							+ "       GROUP BY galaxy, system) " + "; " + "END;");

			// insert some rows

			if (!exists) {				
				stmt.execute("INSERT INTO databaseSettings (databaseVersion) VALUES(" + DATABASE_VERSION + ");");
				stmt.execute("INSERT INTO timestamps (updateTimestamp) VALUES (strftime('%s', 'now')*1000) ;");
				stmt.execute("INSERT INTO serverData (domain) VALUES ('" + ogdb.getFullServerName() + "');");
				stmt.execute("INSERT INTO alliances (allianceId) VALUES (" + DEFAULT_ALLIANCE_ID + ");");
			}
			
            if(databaseSettings != null){
                stmt.execute("UPDATE databaseSettings SET databaseVersion = " + databaseSettings.getDatabaseVersion()
                        + ", saveActivityStates = " + (databaseSettings.isSaveActivityStates() ? 1 : 0)
                        + ", savePlanetDistribution = " + (databaseSettings.isSavePlanetDistribution() ? 1 : 0)
                        + ", saveHighscoreDistribution = " + (databaseSettings.isSaveHighscoreDistribution() ? 1 : 0)
                        + ", maxHighscoreEntriesPerEntity = " + databaseSettings.getMaxHighscoreEntriesPerEntity());					
            }			

		} catch (SQLException e) {
			con.rollback();
			logger.info(ogdb.getServerPrefix() + " - creating database - rollback");
			logger.log(Level.INFO, e.getMessage(), e);
			ex = e;
		} finally {
			con.commit();
			con.setAutoCommit(oldAutoCommit);
		}

		if (ex != null)
			throw ex;
		logger.info(ogdb.getServerPrefix() + " - created database");

	}

}
