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
import darksky.ogameapidatabasefx.database.entities.Alliance;
import darksky.ogameapidatabasefx.database.entities.EntityReader;

public class AllianceSearchTask extends
		Task<ObservableList<Pair<String, Alliance>>> {

	private List<OGameAPIDatabase> m_searchList = null;
	private String m_searchParameter = null;

	AllianceSearchTask(List<OGameAPIDatabase> searchList, String searchParameter) {
		super();
		m_searchList = searchList;
		m_searchParameter = searchParameter;
	}

	@Override
	protected ObservableList<Pair<String, Alliance>> call() throws Exception {

		ObservableList<Pair<String, Alliance>> returnList = FXCollections
				.observableArrayList();

		int maxProgress = m_searchList.size();
		int progress = 0;

		for (OGameAPIDatabase controller : m_searchList) {
			String prefix = controller.getServerPrefix();
			EntityReader reader = controller.getEntityReader();
			try {
				List<Alliance> allianceResult = reader.getAlliancesByNameLike(
						m_searchParameter);
				if (m_searchParameter.matches("\\d+")) {
					int id = Integer.parseInt(m_searchParameter);
					Optional<Alliance> allianceWithId = reader.getAllianceById(id);
					allianceWithId.ifPresent(a->allianceResult.add(a));
				}
				allianceResult.forEach(a -> returnList
						.add(new Pair<String, Alliance>(prefix, a)));

			} catch (SQLException e) {
				Util.getLogger()
						.log(Level.SEVERE, this.getClass().getName(), e);
				setException(e);
			}
			updateProgress(++progress, maxProgress);
		}

		Util.getLogger().fine("alliance search complete");

		return returnList;
	}

}
