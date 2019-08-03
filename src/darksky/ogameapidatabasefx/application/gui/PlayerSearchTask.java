package darksky.ogameapidatabasefx.application.gui;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.util.Pair;
import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.EntityReader;
import darksky.ogameapidatabasefx.database.entities.Player;

public class PlayerSearchTask extends
		Task<ObservableList<Pair<String, Player>>> {

	private List<OGameAPIDatabase> m_searchList = null;
	private String m_searchParameter = null;

	PlayerSearchTask(List<OGameAPIDatabase> searchList, String searchParameter) {
		super();
		m_searchList = searchList;
		m_searchParameter = searchParameter;
	}

	@Override
	protected ObservableList<Pair<String, Player>> call() throws Exception {

		ObservableList<Pair<String, Player>> returnList = FXCollections
				.observableArrayList();

		int maxProgress = m_searchList.size();
		int progress = 0;

		for (OGameAPIDatabase controller : m_searchList) {
			String prefix = controller.getServerPrefix();
			EntityReader reader = controller.getEntityReader();
			try {
				List<Player> playerResult = reader.getPlayersByNameLike(
						m_searchParameter);
				if (m_searchParameter.matches("\\d+")) {
					int id = Integer.parseInt(m_searchParameter);
					Optional<Player> playerWithId = reader.getPlayerById(id);
					playerWithId.ifPresent(p->playerResult.add(p));
				}
				playerResult.forEach(p -> returnList
						.add(new Pair<String, Player>(prefix, p)));

			} catch (SQLException e) {
				Util.getLogger()
						.log(Level.SEVERE, this.getClass().getName(), e);
				setException(e);
			}
			updateProgress(++progress, maxProgress);
		}

		Util.getLogger().fine("player search complete");

		return returnList;
	}

}
