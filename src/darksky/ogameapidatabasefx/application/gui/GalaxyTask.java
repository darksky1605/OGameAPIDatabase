package darksky.ogameapidatabasefx.application.gui;

import java.time.LocalDate;

import darksky.ogameapidatabasefx.database.entities.EntityReader;
import darksky.ogameapidatabasefx.database.entities.SolarSystem;
import darksky.ogameapidatabasefx.database.entities.SystemPosition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class GalaxyTask extends Task<ObservableList<SystemPosition>> {

	private final EntityReader m_reader;
	private final LocalDate m_date;
	private final int m_galaxy;
	private final int m_system;

	public GalaxyTask(EntityReader reader, LocalDate date, int galaxy,
			int system) {
		super();
		m_reader = reader;
		m_date = date;
		m_galaxy = galaxy;
		m_system = system;
	}

	@Override
	protected ObservableList<SystemPosition> call() throws Exception {
		ObservableList<SystemPosition> list = FXCollections
				.observableArrayList();
		this.updateProgress(1, 10);
		SolarSystem s = m_reader.getSolarSystem(m_galaxy, m_system,0
				/*m_date*/);
		if (s != null) {
			list.addAll(s.getPositions());
		} else {
			throw new Exception("Invalid system " + m_galaxy + " " + m_system
					+ ". solarsystem = null");
		}

		if (list.size() != 15)
			throw new Exception("Invalid system " + m_galaxy + " " + m_system
					+ ". list size < 15");
		this.updateProgress(10, 10);
		return list;
	}

}
