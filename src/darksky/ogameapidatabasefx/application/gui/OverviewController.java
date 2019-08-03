package darksky.ogameapidatabasefx.application.gui;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.stream.Collectors;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.Timestamps;
import darksky.ogameapidatabasefx.database.entities.Highscore;
import darksky.ogameapidatabasefx.database.entities.ServerData;
import darksky.ogameapidatabasefx.database.statistics.ServerActivityState;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class OverviewController implements BasicController {

	private static class TimestampDateEntry {
		String name;
		LocalDateTime value;
		int updateIntervalHours;

		TimestampDateEntry(String name, LocalDateTime value,
				int updateIntervalHours) {
			super();
			this.name = name;
			this.value = value;
			this.updateIntervalHours = updateIntervalHours;
		}
	}

	private ExecutorService m_executor = null;

	private Tooltip tooltip;
	private ObjectProperty<ServerActivityState> m_serverActivityStateProperty = new SimpleObjectProperty<ServerActivityState>(
			null);
	private ObjectProperty<ServerData> m_serverDataProperty = new SimpleObjectProperty<ServerData>(
			null);
	private ObservableList<Entry<String, String>> m_serverSettingsTableData = FXCollections
			.observableArrayList();

	private ObservableList<PieChart.Data> m_pieChartData = FXCollections
			.observableArrayList();

	private OGameAPIDatabase m_databaseController = null;

	private final ObjectProperty<Timestamps> m_databaseInformationProperty = new SimpleObjectProperty<Timestamps>(
			null);
	private final ObservableList<TimestampDateEntry> m_informationTableData = FXCollections
			.observableArrayList();

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TableView<Entry<String, String>> serverSettingsTable;

	@FXML
	private TableColumn<Entry<String, String>, String> keyColumn;

	@FXML
	private TableColumn<Entry<String, String>, String> valueColumn;

	@FXML
	private PieChart chart;

	@FXML
	private TextArea activityTextArea;

	@FXML
	private TableView<TimestampDateEntry> databaseInformationTable;

	@FXML
	private TableColumn<TimestampDateEntry, String> propertyColumn;

	@FXML
	private TableColumn<TimestampDateEntry, String> propertyValueColumn;

	@FXML
	void initialize() {
		assert serverSettingsTable != null : "fx:id=\"serverSettingsTable\" was not injected: check your FXML file 'Overview.fxml'.";
		assert keyColumn != null : "fx:id=\"keyColumn\" was not injected: check your FXML file 'Overview.fxml'.";
		assert valueColumn != null : "fx:id=\"valueColumn\" was not injected: check your FXML file 'Overview.fxml'.";
		assert chart != null : "fx:id=\"chart\" was not injected: check your FXML file 'Overview.fxml'.";
		assert activityTextArea != null : "fx:id=\"activityTextArea\" was not injected: check your FXML file 'Overview.fxml'.";
		assert databaseInformationTable != null : "fx:id=\"databaseInformationTable\" was not injected: check your FXML file 'Overview.fxml'.";
		assert propertyColumn != null : "fx:id=\"propertyColumn\" was not injected: check your FXML file 'Overview.fxml'.";
		assert propertyValueColumn != null : "fx:id=\"propertyValueColumn\" was not injected: check your FXML file 'Overview.fxml'.";

		Util.getLogger().config("OverviewController init");

		tooltip = new Tooltip("");

		serverSettingsTable.placeholderProperty().set(new Text(""));
		serverSettingsTable.setItems(m_serverSettingsTableData);
		serverSettingsTable.setFixedCellSize(25);
		serverSettingsTable.prefHeightProperty().bind(
				Bindings.size(serverSettingsTable.getItems())
						.multiply(serverSettingsTable.getFixedCellSize())
						.add(29));

		keyColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell
				.getValue().getKey()));
		valueColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell
				.getValue().getValue()));

		databaseInformationTable.setItems(m_informationTableData);
		databaseInformationTable.setPlaceholder(new Label(""));

		// callback to get a table cell. the cell's background color is orange
		// if an update is necessary
		Callback<TableColumn<TimestampDateEntry, String>, TableCell<TimestampDateEntry, String>> cellFactory = (
				TableColumn<TimestampDateEntry, String> param) -> {

			return new TableCell<TimestampDateEntry, String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					TimestampDateEntry entry = (TimestampDateEntry) getTableRow()
							.getItem();

					if (item == null || empty || entry == null) {
						setText(null);
						setBackground(null);
					} else {
						setText(getItem().toString());

						if (entry != null) {
							LocalDateTime now = LocalDateTime.now();
							LocalDateTime before = now
									.minusHours(entry.updateIntervalHours);
							LocalDateTime propvalue = entry.value;
							if (before.isAfter(propvalue))
								setBackground(new Background(
										new BackgroundFill(Color.ORANGE, null,
												null)));
						}

					}

					setGraphic(null);
				}

			};

		};

		propertyColumn.setCellValueFactory(cell -> new SimpleStringProperty(
				cell.getValue().name));

		propertyValueColumn
				.setCellValueFactory(cell -> new SimpleStringProperty(String
						.format("%s %s", cell.getValue().value.toLocalDate(),
								cell.getValue().value.toLocalTime())));

		propertyColumn.setCellFactory(cellFactory);
		propertyValueColumn.setCellFactory(cellFactory);

		// update table model
		m_databaseInformationProperty
				.addListener((property, oldvalue, newvalue) -> {

					List<TimestampDateEntry> entries = new ArrayList<TimestampDateEntry>();

					LocalDateTime datetime;
					TimestampDateEntry entry;

					if (newvalue != null) {						
						datetime = LocalDateTime.ofInstant(newvalue
										.getServerDataTimestamp(), ZoneId.systemDefault());
						entry = new TimestampDateEntry(
								resources.getString("serversettings"),
								datetime,
								OGameAPIDatabase.UPDATEINTERVAL_SERVERDATA_DAYS * 24);
						entries.add(entry);

						datetime = LocalDateTime.ofInstant(newvalue
								.getPlayersTimestamp(), ZoneId.systemDefault());
						entry = new TimestampDateEntry(
								resources.getString("players"),
								datetime,
								OGameAPIDatabase.UPDATEINTERVAL_PLAYERS_DAYS * 24);
						entries.add(entry);

						datetime = LocalDateTime.ofInstant(newvalue
								.getAlliancesTimestamp(), ZoneId.systemDefault());
						entry = new TimestampDateEntry(
								resources.getString("alliances"),
								datetime,
								OGameAPIDatabase.UPDATEINTERVAL_ALLIANCES_DAYS * 24);
						entries.add(entry);

						datetime = LocalDateTime.ofInstant(newvalue
								.getUniverseTimestamp(), ZoneId.systemDefault());
						entry = new TimestampDateEntry(
								resources.getString("universe"),
								datetime,
								OGameAPIDatabase.UPDATEINTERVAL_UNIVERSE_DAYS * 24);
						entries.add(entry);

						for (int category = 1; category <= 2; ++category) {
							for (int type = 0; type < 8; ++type) {
								datetime = LocalDateTime.ofInstant(newvalue
										.getHighscoreTimestamp(category, type), ZoneId.systemDefault());
								entry = new TimestampDateEntry(
										(category == 1 ? resources
												.getString("player") + " "
												: resources
														.getString("alliance")
														+ " ")
												+ Highscore.HighscoreType
														.values()[type],
										datetime,
										OGameAPIDatabase.UPDATEINTERVAL_HIGHSCORE_HOURS);
								entries.add(entry);

							}
						}
					}

					Platform.runLater(() -> {
						m_informationTableData.setAll(entries);
						databaseInformationTable.setVisible(true);
					});
				});

		chart.dataProperty().set(m_pieChartData);

		m_serverDataProperty.addListener((p, o, n) -> {
			if (n != null) {
				m_serverSettingsTableData.setAll(n.getDataMap().entrySet()
						.stream()
						.sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
						.collect(Collectors.toList()));
			}
		});

		m_serverActivityStateProperty
				.addListener((p, o, n) -> {
					if (n != null) {
						NumberFormat nf = NumberFormat
								.getNumberInstance(Locale.GERMANY);
						activityTextArea.clear();

						activityTextArea.appendText(resources
								.getString("players")
								+ " : "
								+ nf.format(n.getNumberOfPlayers()) + "\n");
						activityTextArea.appendText(resources
								.getString("vacationmode")
								+ " : "
								+ nf.format(n.getNumberOfVModePlayers()) + "\n");
						activityTextArea.appendText(resources
								.getString("inactive")
								+ " : "
								+ nf.format(n.getNumberOfInactivePlayers())
								+ "\n");
						activityTextArea.appendText("> 28 "
								+ resources.getString("inactive")
								+ " : "
								+ nf.format(n
										.getNumberOfLongtimeInactivePlayers())
								+ "\n");
						activityTextArea.appendText("> 35 "
								+ resources.getString("inactive") + " : "
								+ nf.format(n.getNumberOfNotDeletablePlayers())
								+ "\n");

						activityTextArea.appendText("\n");

						// add data to pie chart
						m_pieChartData.clear();
						for (Entry<String, Integer> entry : n
								.getPlayerActivityMap().entrySet()) {
							PieChart.Data data = new PieChart.Data(entry
									.getKey(), entry.getValue());
							m_pieChartData.add(data);
							activityTextArea.appendText(String.format(
									"%-7s:%6s%n", entry.getKey(),
									nf.format(entry.getValue())));
						}

						// add tooltip to pie chart
						for (PieChart.Data data : chart.getData()) {
							Node node = data.getNode();
							Tooltip.install(node, tooltip);
							node.setOnMouseEntered((e) -> {
								String text = String.format(
										"%.1f%%",
										100 * data.getPieValue()
												/ n.getNumberOfPlayers());
								tooltip.setText(text);

							});
						}
					}
				});

	}

	@Override
	public void setParentController(BasicController parentController) {
	}

	@Override
	public void setCurrentDatabaseController(OGameAPIDatabase databaseController) {

		m_databaseController = Objects.requireNonNull(databaseController);

		Task<ServerActivityState> stateTask = new Task<ServerActivityState>() {

			@Override
			protected ServerActivityState call() throws Exception {
				return m_databaseController.getStatisticsReader()
						.getNewestServerActivityState();
			}

		};

		m_serverActivityStateProperty.bind(stateTask.valueProperty());
		stateTask.setOnFailed(event -> {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(),
					stateTask.getException());
			activityTextArea.setText(resources
					.getString("error_loadserveractivity"));
		});

		m_executor.submit(stateTask);

		Task<ServerData> dataTask = new Task<ServerData>() {

			@Override
			protected ServerData call() throws Exception {
				return m_databaseController.getEntityReader().getServerData();
			}

		};

		m_serverDataProperty.bind(dataTask.valueProperty());
		dataTask.setOnFailed(event -> {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(),
					dataTask.getException());
			Util.displayErrorMessage(resources
					.getString("error_loadserversettings"));
		});

		m_executor.submit(dataTask);
		
		
		Task<Timestamps> timestamptask = new Task<Timestamps>() {

			@Override
			protected Timestamps call() throws Exception {
				return m_databaseController.getEntityReader()
						.getNewestTimestamps();
			}

		};

		m_databaseInformationProperty.bind(timestamptask.valueProperty());
		dataTask.setOnFailed(event -> {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(),
					dataTask.getException());
			Util.displayErrorMessage(resources
					.getString("error_loadserversettings"));
		});

		m_executor.submit(timestamptask);
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		m_executor = Objects.requireNonNull(executorService);
	}
}
