package darksky.ogameapidatabasefx.application.gui;

import java.sql.ResultSet;
import java.sql.Statement;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.EntityReader;
import darksky.ogameapidatabasefx.database.entities.IHighscoreEntity;
import darksky.ogameapidatabasefx.database.entities.Highscore.HighscoreType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TextField;

public class GetHighscoreActorsTask extends
		Task<ObservableList<IHighscoreEntity>> {

	final OGameAPIDatabase m_ogdb;
	final int m_page; // first page is page = 1
	final int m_entriesPerPage;
	final HighscoreType m_type;
	final boolean m_isPlayerHighscore;

	final TextField m_pageField;

	public GetHighscoreActorsTask(OGameAPIDatabase ogdb, int page,
			int entriesPerPage, HighscoreType type, boolean isPlayerHighscore,
			TextField pageField) {
		super();
		m_ogdb = ogdb;
		m_page = page;
		m_entriesPerPage = entriesPerPage;
		m_type = type;
		m_isPlayerHighscore = isPlayerHighscore;
		m_pageField = pageField;
	}

	@Override
	protected ObservableList<IHighscoreEntity> call() throws Exception {

		int availableEntries = 0;
		if (m_isPlayerHighscore) {
			try (Statement stmt = m_ogdb.getDatabaseConnection()
					.createStatement();
					ResultSet rs = stmt
							.executeQuery("SELECT count(*) AS notdeletednotbanned "
									+ "FROM players JOIN playerHighscores ph ON players.playerid = ph.playerid "
									+ "WHERE charIndex('b', playerStatus) = 0 AND "
									+ m_type
									+ "Rank <> 0 AND lastUpdate = (SELECT MAX(lastUpdate) FROM players);");) {
				if (rs.next()) {
					availableEntries = rs.getInt("notdeletednotbanned");
				}
			}
		} else {
			try (Statement stmt = m_ogdb.getDatabaseConnection()
					.createStatement();
					ResultSet rs = stmt
							.executeQuery("SELECT count(*) AS notdeleted FROM alliances join allianceHighscores on alliances.allianceid = allianceHighscores.allianceid "
									+ "WHERE "
									+ m_type
									+ "Rank <> 0 AND lastUpdate = (SELECT MAX(lastUpdate) FROM alliances);");) {
				if (rs.next()) {
					availableEntries = rs.getInt("notdeleted");
				}
			}
		}

		final int maxPage = (availableEntries + m_entriesPerPage - 1)
				/ m_entriesPerPage;
		final int page = Math.min(Math.max(m_page, 1), maxPage);

		Platform.runLater(() -> m_pageField.setText("" + page));

		ObservableList<IHighscoreEntity> list = FXCollections
				.observableArrayList();

		EntityReader reader = m_ogdb.getEntityReader();
		if (m_isPlayerHighscore)
			list.addAll(reader.getPlayersByHighscoreRank(m_type, (page - 1)
					* m_entriesPerPage, page * m_entriesPerPage, false, false));
		else {
			list.addAll(reader.getAlliancesByHighscoreRank(m_type, (page - 1)
					* m_entriesPerPage, page * m_entriesPerPage, false));
		}

		return list;
	}

}
