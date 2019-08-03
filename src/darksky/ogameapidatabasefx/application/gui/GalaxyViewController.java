package darksky.ogameapidatabasefx.application.gui;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.Alliance;
import darksky.ogameapidatabasefx.database.entities.EntityReader;
import darksky.ogameapidatabasefx.database.entities.Moon;
import darksky.ogameapidatabasefx.database.entities.Planet;
import darksky.ogameapidatabasefx.database.entities.Player;
import darksky.ogameapidatabasefx.database.entities.ServerData;
import darksky.ogameapidatabasefx.database.entities.SystemPosition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class GalaxyViewController implements BasicController {

	private ExecutorService m_executor = null;

	private final ObservableList<SystemPosition> m_solarSystemData = FXCollections
			.observableArrayList();

	private DetailViewController m_detailViewController;
	private SimpleObjectProperty<OGameAPIDatabase> m_currentDatabaseControllerProperty = new SimpleObjectProperty<OGameAPIDatabase>(
			null);

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button submitButton;

	@FXML
	private DatePicker galaxyDatePicker;

	@FXML
	private TableView<SystemPosition> galaxyTable;

	@FXML
	private TableColumn<SystemPosition, Integer> positionColumn;

	@FXML
	private TableColumn<SystemPosition, String> planetColumn;

	@FXML
	private TableColumn<SystemPosition, String> moonColumn;

	@FXML
	private TableColumn<SystemPosition, String> playerColumn;

	@FXML
	private TableColumn<SystemPosition, String> statusColumn;

	@FXML
	private TableColumn<SystemPosition, String> allianceColumn;

	@FXML
	private ProgressBar galaxyProgressBar;

	@FXML
	private Button previousGalaxyButton;

	@FXML
	private Button nextGalaxyButton;

	@FXML
	private TextField galaxyInput;

	@FXML
	private Button previousSystemButton;

	@FXML
	private Button nextSystemButton;

	@FXML
	private TextField systemInput;

	@FXML
	private Label galaxyLabel;

	@FXML
	private Label systemLabel;

	private class TCell<S, T> extends TableCell<S, T> {
		@Override
		public void updateItem(T item, boolean empty) {
			super.updateItem(item, empty);
			setText(empty ? null : getString());
			setGraphic(null);
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	};

	@FXML
	void nextGalaxyButtonAction(ActionEvent event) {
		int newGalaxy = 1;
		if (galaxyInput.getText().matches("[0-9]+")) {
			newGalaxy = Integer.parseInt(galaxyInput.getText()) + 1;
		}
		galaxyInput.setText(String.valueOf(newGalaxy));
		submitButtonAction(null);
	}

	@FXML
	void nextSystemButtonAction(ActionEvent event) {
		int newSystem = 1;
		if (systemInput.getText().matches("[0-9]+")) {
			newSystem = Integer.parseInt(systemInput.getText()) + 1;
		}
		systemInput.setText(String.valueOf(newSystem));
		submitButtonAction(null);
	}

	@FXML
	void previousGalaxyButtonAction(ActionEvent event) {
		int newGalaxy = 1;
		if (galaxyInput.getText().matches("[0-9]+")) {
			newGalaxy = Integer.parseInt(galaxyInput.getText()) - 1;
		}
		galaxyInput.setText(String.valueOf(newGalaxy));
		submitButtonAction(null);
	}

	@FXML
	void previousSystemButtonAction(ActionEvent event) {
		int newSystem = 1;
		if (systemInput.getText().matches("[0-9]+")) {
			newSystem = Integer.parseInt(systemInput.getText()) - 1;
		}
		systemInput.setText(String.valueOf(newSystem));
		submitButtonAction(null);
	}

	@FXML
	void submitButtonAction(ActionEvent event) {
		int galaxy = 1;
		int system = 1;
		try {
			galaxy = Integer.parseInt(galaxyInput.getText());
		} catch (NumberFormatException e) {
		}
		try {
			system = Integer.parseInt(systemInput.getText());
		} catch (NumberFormatException e) {
		}

		EntityReader entityReader = m_currentDatabaseControllerProperty.get()
				.getEntityReader();

		try {
			ServerData serverData = entityReader.getServerData();
			if (serverData != null) {

				int maxGalaxy = serverData.getGalaxies();
				int maxSystem = serverData.getSystems();
				galaxy = Math.min(Math.max(1, galaxy), maxGalaxy);
				system = Math.min(Math.max(1, system), maxSystem);
				systemInput.setText("" + system);
				galaxyInput.setText("" + galaxy);

				GalaxyTask task = new GalaxyTask(entityReader, galaxyDatePicker
						.getValue().minusDays(0), galaxy, system);
				task.setOnFailed(e -> {
					Util.getLogger().log(Level.SEVERE,
							this.getClass().getName(), task.getException());
					Util.displayErrorMessage(resources
							.getString("error_loadsystem"));
				});
				galaxyTable.disableProperty().bind(task.runningProperty());
				galaxyTable.itemsProperty().bind(task.valueProperty());
				galaxyProgressBar.progressProperty().bind(
						task.progressProperty());
				galaxyProgressBar.visibleProperty()
						.bind(task.runningProperty());

				m_executor.submit(task);

			} else {
				Util.getLogger().severe(
						"galaxyviewcontroller "
								+ m_currentDatabaseControllerProperty.get()
										.getServerPrefix()
								+ " serverdata == null");
				Util.displayErrorMessage(resources
						.getString("error_loadserversettings"));
			}

		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
			Util.displayErrorMessage(resources
					.getString("error_loadserversettings"));
		}
	}

	@SuppressWarnings("unchecked")
	@FXML
	private void initialize() {
		assert submitButton != null : "fx:id=\"submitButton\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert galaxyDatePicker != null : "fx:id=\"galaxyDatePicker\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert galaxyTable != null : "fx:id=\"galaxyTable\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert positionColumn != null : "fx:id=\"positionColumn\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert planetColumn != null : "fx:id=\"planetColumn\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert moonColumn != null : "fx:id=\"moonColumn\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert playerColumn != null : "fx:id=\"playerColumn\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert statusColumn != null : "fx:id=\"statusColumn\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert allianceColumn != null : "fx:id=\"allianceColumn\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert galaxyProgressBar != null : "fx:id=\"galaxyProgressBar\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert galaxyInput != null : "fx:id=\"galaxyInput\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert previousGalaxyButton != null : "fx:id=\"previousGalaxyButton\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert galaxyLabel != null : "fx:id=\"galaxyLabel\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert nextGalaxyButton != null : "fx:id=\"nextGalaxyButton\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert systemLabel != null : "fx:id=\"systemLabel\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert systemInput != null : "fx:id=\"systemInput\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert previousSystemButton != null : "fx:id=\"previousSystemButton\" was not injected: check your FXML file 'GalaxyView.fxml'.";
		assert nextSystemButton != null : "fx:id=\"nextSystemButton\" was not injected: check your FXML file 'GalaxyView.fxml'.";

		Util.getLogger().config("GalaxyViewController init");

		galaxyDatePicker.setValue(LocalDate.now());

		m_currentDatabaseControllerProperty.addListener((p, oldv, newv) -> {
			if (oldv != newv) {
				m_solarSystemData.clear();
				galaxyInput.setText("1");
				systemInput.setText("1");
			}
		});

		galaxyTable.setItems(m_solarSystemData);
		galaxyTable.setPlaceholder(new Label(""));
		galaxyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		positionColumn.setCellValueFactory((
				CellDataFeatures<SystemPosition, Integer> p) -> {
			int position = p.getValue().getPosition();
			return new ReadOnlyObjectWrapper<Integer>(position);
		});

		planetColumn.setCellValueFactory((
				CellDataFeatures<SystemPosition, String> p) -> {
			Planet planet = p.getValue().getPlanet().orElse(null);
			return new ReadOnlyObjectWrapper<String>((planet == null ? ""
					: planet.getName()));
		});

		moonColumn.setCellValueFactory((
				CellDataFeatures<SystemPosition, String> p) -> {
			Moon moon = p.getValue().getMoon().orElse(null);
			return new ReadOnlyObjectWrapper<String>((moon == null ? "" : moon
					.getName()));
		});

		playerColumn.setCellValueFactory((
				CellDataFeatures<SystemPosition, String> p) -> {
			Player player = p.getValue().getPlayer().orElse(null);
			return new ReadOnlyObjectWrapper<String>((player == null ? ""
					: player.getName()));
		});

		statusColumn.setCellValueFactory((
				CellDataFeatures<SystemPosition, String> p) -> {
			Player player = p.getValue().getPlayer().orElse(null);
			return new ReadOnlyObjectWrapper<String>((player == null ? ""
					: player.getPlayerStatus()));
		});

		allianceColumn.setCellValueFactory((
				CellDataFeatures<SystemPosition, String> p) -> {
			Alliance alliance = p.getValue().getAlliance().orElse(null);
			return new ReadOnlyObjectWrapper<String>((alliance == null ? ""
					: alliance.getAllianceTag()));
		});

		// mouse listeners for cells
		playerColumn
				.setCellFactory((TableColumn<SystemPosition, String> p) -> {
					TableCell<SystemPosition, String> cell = new TCell<SystemPosition, String>();

					cell.addEventFilter(
							MouseEvent.MOUSE_CLICKED,
							(event) -> {
								if (event.getButton().equals(
										MouseButton.PRIMARY)
										&& event.getClickCount() > 1) {
									TableCell<SystemPosition, String> c = (TableCell<SystemPosition, String>) event
											.getSource();
									Optional<Player> player = ((SystemPosition) c
											.getTableRow().getItem())
											.getPlayer();
									player.ifPresent(pl->
										m_detailViewController.displayPlayer(
												pl,
												m_currentDatabaseControllerProperty
														.get()
														.getServerPrefix()));
									
								}
							});
					return cell;
				});

		allianceColumn
				.setCellFactory((TableColumn<SystemPosition, String> p) -> {
					TableCell<SystemPosition, String> cell = new TCell<SystemPosition, String>();

					cell.addEventFilter(
							MouseEvent.MOUSE_CLICKED,
							(event) -> {
								if (event.getButton().equals(
										MouseButton.PRIMARY)
										&& event.getClickCount() > 1) {
									TableCell<SystemPosition, String> c = (TableCell<SystemPosition, String>) event
											.getSource();
									Optional<Alliance> alliance = ((SystemPosition) c
											.getTableRow().getItem())
											.getAlliance();
									alliance.ifPresent(al->
										m_detailViewController.displayAlliance(
												al,
												m_currentDatabaseControllerProperty
														.get()
														.getServerPrefix()));
									
								}
							});
					return cell;
				});

	}

	public void setDetailViewController(
			DetailViewController detailViewController) {
		m_detailViewController = Objects.requireNonNull(detailViewController);
	}

	@Override
	public void setCurrentDatabaseController(
			OGameAPIDatabase currentDatabaseController) {
		m_currentDatabaseControllerProperty.set(Objects
				.requireNonNull(currentDatabaseController));
	}

	@Override
	public void setParentController(BasicController parentController) {
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		m_executor = Objects.requireNonNull(executorService);
	}
}
