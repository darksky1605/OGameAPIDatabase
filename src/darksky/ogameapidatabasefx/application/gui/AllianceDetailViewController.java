package darksky.ogameapidatabasefx.application.gui;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.stream.Collectors;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.Alliance;
import darksky.ogameapidatabasefx.database.entities.AllianceData;
import darksky.ogameapidatabasefx.database.entities.EntityReader;
import darksky.ogameapidatabasefx.database.entities.Highscore;
import darksky.ogameapidatabasefx.database.entities.Player;
import darksky.ogameapidatabasefx.database.entities.Highscore.HighscoreType;
import darksky.ogameapidatabasefx.database.logs.ChangeLogEntry;
import darksky.ogameapidatabasefx.database.logs.LogReader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

public class AllianceDetailViewController implements BasicController {

	private List<Pair<LocalDate, String>> m_logTextList = new ArrayList<Pair<LocalDate, String>>();

	private ObservableList<Player> m_memberListData = FXCollections
			.observableArrayList();

	private List<ChangeLogEntry<Player, Optional<Alliance>>> m_memberChanges = null;
	private List<ChangeLogEntry<Alliance, Alliance>> m_nameChanges = null;
	private List<ChangeLogEntry<Alliance, Alliance>> m_tagChanges = null;
	private List<ChangeLogEntry<Alliance, Alliance>> m_homepageChanges = null;
	private List<ChangeLogEntry<Alliance, Alliance>> m_logoChanges = null;
	private List<ChangeLogEntry<Alliance, Alliance>> m_openChanges = null;

	private Alliance m_alliance;
	private AllianceData m_allianceData;
	private String m_serverPrefix;

	private DetailViewController m_detailViewController;

	private EntityReader m_entityReader;

	private LogReader m_logReader;

	private ExecutorService m_executor;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TextArea allianceInformationTextArea;

	@FXML
	private TextArea highscoreTextArea;

	@FXML
	private TextArea changelogTextArea;

	@FXML
	private ListView<Player> memberListListView;

	@FXML
	private TextArea allianceMemberInformationArea;

	@FXML
	private StackPane progressPane;

	@FXML
	private ProgressIndicator progressIndicator;

	@FXML
	private Label progressIndicatorLabel;

	@FXML
	void initialize() {
		assert allianceInformationTextArea != null : "fx:id=\"allianceInformationTextArea\" was not injected: check your FXML file 'AllianceDetailView.fxml'.";
		assert highscoreTextArea != null : "fx:id=\"highscoreTextArea\" was not injected: check your FXML file 'AllianceDetailView.fxml'.";
		assert changelogTextArea != null : "fx:id=\"changelogTextArea\" was not injected: check your FXML file 'AllianceDetailView.fxml'.";
		assert memberListListView != null : "fx:id=\"memberListListView\" was not injected: check your FXML file 'AllianceDetailView.fxml'.";
		assert allianceMemberInformationArea != null : "fx:id=\"allianceMemberInformationArea\" was not injected: check your FXML file 'AllianceDetailView.fxml'.";
		assert progressPane != null : "fx:id=\"progressPane\" was not injected: check your FXML file 'AllianceDetailView.fxml'.";
		assert progressIndicator != null : "fx:id=\"progressIndicator\" was not injected: check your FXML file 'AllianceDetailView.fxml'.";
		assert progressIndicatorLabel != null : "fx:id=\"progressIndicatorLabel\" was not injected: check your FXML file 'AllianceDetailView.fxml'.";

		Util.getLogger().config("AllianceDetailViewController init");

		memberListListView.setItems(m_memberListData);

		memberListListView.setCellFactory((ListView<Player> param) -> {
			ListCell<Player> cell = new ListCell<Player>() {
				@Override
				protected void updateItem(Player entry, boolean empty) {
					super.updateItem(entry, empty);
					if (entry != null && !empty) {
						setText(entry.getName() + " ["
								+ entry.getPlayerStatus() + "] (Id "
								+ entry.getId() + ")");
					} else {
						setText(null);
						setGraphic(null);
					}
				}
			};
			return cell;
		});

		memberListListView.setOnMouseClicked(event -> {

			if (event.getButton().equals(MouseButton.PRIMARY)
					&& event.getClickCount() == 2) {
				Player player = memberListListView.getSelectionModel()
						.getSelectedItem();
				if (player != null) {
					m_detailViewController
							.displayPlayer(player, m_serverPrefix);
				}

			}
		});
		
		highscoreTextArea.setOnMouseClicked((event) -> {			

			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				
				HighscoreType[] chartTypes = { HighscoreType.Total, HighscoreType.Economy, HighscoreType.Research,
						HighscoreType.Military };
				
				List<Highscore> highscoreList = m_allianceData.getHighscores();
				Axis<String> xAxis = new javafx.scene.chart.CategoryAxis();
				Axis<Number> yAxis = new NumberAxis();
				ObservableList<Series<String, Number>> series = FXCollections.observableArrayList();

				for (int i = 0; i < chartTypes.length; i++) {
					Series<String, Number> s = new Series<>();
					s.setName(resources.getString(chartTypes[i].toString().toLowerCase()));
					series.add(s);
				}

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				for (Highscore h : highscoreList) {
					String ldt = formatter.format(LocalDateTime.ofInstant(h.getInstant(), ZoneId.systemDefault()));
					for (int i = 0; i < chartTypes.length; i++) {
						series.get(i).getData().add(new XYChart.Data<String, Number>(ldt, h.getPoints(chartTypes[i])));
					}
				}

				LineChart<String, Number> highscoreChart = new LineChart<String, Number>(xAxis, yAxis, series);
				highscoreChart.setAnimated(false);
				highscoreChart.setTitle(resources.getString("highscore") + " " + m_allianceData.getAlliance().getName());

				NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);

				for (Series<String, Number> s : series) {
					for (XYChart.Data<String, Number> item : s.getData()) {
						Tooltip.install(item.getNode(), new Tooltip(nf.format(item.getYValue())));
						item.getNode().setOnMouseEntered(e -> {
							item.getNode().setStyle("-fx-background-color: BLACK");
						});

						item.getNode().setOnMouseExited(e -> {
							item.getNode().setStyle("");
						});
					}
				}

				VBox vBox = new VBox();
				vBox.getChildren().add(highscoreChart);

				Stage stage = new Stage();
				stage.setTitle(m_allianceData.getAlliance().getName());
				stage.setScene(new Scene(vBox, 800, 400));
				stage.show();
			}
		});
	}

	public void setData(Alliance alliance, OGameAPIDatabase dbcontroller) {
		Objects.requireNonNull(alliance);
		Objects.requireNonNull(dbcontroller);

		m_entityReader = dbcontroller.getEntityReader();
		m_logReader = dbcontroller.getLogReader();
		m_serverPrefix = dbcontroller.getServerPrefix();
		m_alliance = alliance;

		m_executor.submit(() -> {
			showProgressIndicator();
			getAllianceInformationFromDatabase();
			displayAllianceInformation();
			displayMemberInformation();
			displayMemberList();
			displayHighscore();
			displayChangeLog();
			hideProgressIndicator();
		});
	}

	private void displayAllianceInformation() {
		if (m_alliance != null) {			
				Platform.runLater(() -> {
					allianceInformationTextArea.appendText(String.format(
							"%s [%s] (Id %d)%n%n", m_alliance.getName(),
							m_alliance.getAllianceTag(), m_alliance.getId()));
					allianceInformationTextArea.appendText(String.format(
							"%s %s%n%n",
							resources.getString("indatabasesince"), LocalDateTime.ofInstant(m_alliance.getInsertedOn(), ZoneId.systemDefault()).toLocalDate()));
					allianceInformationTextArea.appendText(String.format(
							"%s %s%n%n", resources.getString("lastupdatedat"),
							LocalDateTime.ofInstant(m_alliance.getLastUpdate(), ZoneId.systemDefault()).toLocalDate()));
					allianceInformationTextArea.appendText(m_alliance
							.isDeleted() ? resources.getString("deleted")
							+ " !" : "");
				});

		} else {
			Platform.runLater(() -> {
				allianceInformationTextArea.setText(resources
						.getString("nodataavailable"));
			});
		}

	}

	private void displayMemberInformation() {
		if (m_allianceData != null) {
			Map<String, Integer> allyactivity = m_allianceData
					.getMembers()
					.stream()
					.collect(
							Collectors.toMap(p -> p.getPlayerStatus(),
									p -> (Integer) 1, (a, b) -> a + b));
			Platform.runLater(() -> {
				allianceMemberInformationArea.appendText(String.format(
						"%s : %d%n", resources.getString("players"),
						m_allianceData.getMembers().size()));
				allyactivity.entrySet()
						.forEach(
								e -> allianceMemberInformationArea
										.appendText(String.format("%s : %d%n",
												e.getKey(), e.getValue())));
			});
		}
	}

	private void displayMemberList() {
		if (m_allianceData != null) {
			Platform.runLater(() -> {
				m_memberListData.setAll(m_allianceData.getMembers());
			});
		}
	}

	private void displayHighscore() {
		if (m_allianceData != null) {
			Highscore allianceHighscore = m_allianceData.getHighscores().get(m_allianceData.getHighscores().size()-1);
			StringBuilder text = new StringBuilder();
			NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);

			for (HighscoreType t : HighscoreType.values()) {
				text.append(String.format("%-20s : %15s - %6s",
						resources.getString(t.toString().toLowerCase()),
						nf.format(allianceHighscore.getPoints(t)),
						nf.format(allianceHighscore.getRank(t))));
				text.append("\n");
			}
			text.deleteCharAt(text.length() - 1);
			Platform.runLater(() -> {
				highscoreTextArea.setText(text.toString());
			});
		}
	}

	private void displayChangeLog() {

		if (m_memberChanges != null) {
			m_memberChanges
					.stream()
					.map(logentry -> {
						LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
						Player player = logentry.getOwner();
						boolean entered = logentry.getNewValue().isPresent();
						String text = String.format(
								"%s - %s (Id %d)",
								(entered ? resources
										.getString("enteredalliance")
										: resources.getString("leftalliance")),
								player.getName(), player.getId());
						return new Pair<LocalDate, String>(date, text);
					}).forEach(m_logTextList::add);
		}
		if (m_nameChanges != null) {
			m_nameChanges
					.stream()
					.map(logentry -> {
						LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
						String text = String.format("%s - %s -> %s",
								resources.getString("renamedalliance"),
								logentry.getOldValue().getName(), logentry.getNewValue().getName());
						return new Pair<LocalDate, String>(date, text);
					}).forEach(m_logTextList::add);
		}
		if (m_tagChanges != null) {
			m_tagChanges
					.stream()
					.map(logentry -> {
						LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
						String text = String.format("%s - %s -> %s",
								resources.getString("changedalliancetag"),
								logentry.getOldValue().getAllianceTag(), logentry.getNewValue().getAllianceTag());
						return new Pair<LocalDate, String>(date, text);
					}).forEach(m_logTextList::add);
		}
		if (m_homepageChanges != null) {
			m_homepageChanges
					.stream()
					.map(logentry -> {
						LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
						String text = String.format("%s - %s -> %s",
								resources.getString("changedalliancehomepage"),
								logentry.getOldValue().getHomepage(), logentry.getNewValue().getHomepage());
						return new Pair<LocalDate, String>(date, text);
					}).forEach(m_logTextList::add);
		}
		if (m_logoChanges != null) {
			m_logoChanges
					.stream()
					.map(logentry -> {
						LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
						String text = String.format("%s - %s -> %s",
								resources.getString("changedalliancelogo"),
								logentry.getOldValue().getLogo(), logentry.getNewValue().getLogo());
						return new Pair<LocalDate, String>(date, text);
					}).forEach(m_logTextList::add);
		}
		if (m_openChanges != null) {
			final String[] openstatus = {
					resources.getString("allianceapplicationpossibleno"),
					resources.getString("allianceapplicationpossibleyes") };
			m_openChanges
					.stream()
					.map(logentry -> {
						LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
						String text = String.format("%s -> %s",
								(logentry.getOldValue().isOpen() ? openstatus[1]
										: openstatus[0]), (logentry
										.getNewValue().isOpen() ? openstatus[1]
										: openstatus[0]));
						return new Pair<LocalDate, String>(date, text);
					}).forEach(m_logTextList::add);
		}

		// entries are sorted by date descending

		StringBuilder displayedLogText = new StringBuilder();
		if (m_logTextList.size() > 0) {
			displayedLogText.append(m_logTextList.stream()
					.sorted((c1, c2) -> c2.getKey().compareTo(c1.getKey()))
					.map(c -> c.getKey() + " : " + c.getValue())
					.collect(Collectors.joining("\n")));
		}

		Platform.runLater(() -> changelogTextArea.setText(displayedLogText
				.toString()));

	}

	private void getAllianceInformationFromDatabase() {
		try {
			m_allianceData = m_entityReader.getAllianceData(m_alliance);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
		}

		LocalDate from = LocalDate.of(2000, 1, 1);
		LocalDate to = LocalDate.now().plusDays(1);

		try {
			m_memberChanges = m_logReader.getAllianceMemberChanges(from, to,
					m_alliance);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
		}
		try {
			m_nameChanges = m_logReader.getAllianceNameChanges(from, to,
					m_alliance);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
		}
		try {
			m_tagChanges = m_logReader.getAllianceTagChanges(from, to,
					m_alliance);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
		}
		try {
			m_homepageChanges = m_logReader.getAllianceHomepageChanges(from,
					to, m_alliance);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
		}
		try {
			m_logoChanges = m_logReader.getAllianceLogoChanges(from, to,
					m_alliance);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
		}
		try {
			m_openChanges = m_logReader.getAllianceOpenChanges(from, to,
					m_alliance);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
		}
	}

	@Override
	public void setParentController(BasicController parentController) {
		m_detailViewController = Objects
				.requireNonNull((DetailViewController) parentController);
	}

	@Override
	public void setCurrentDatabaseController(OGameAPIDatabase databaseController) {
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		m_executor = Objects.requireNonNull(executorService);
	}

	public void showProgressIndicator() {
		Platform.runLater(() -> {
			progressPane.setVisible(true);
			progressPane.setDisable(false);
		});

	}

	public void hideProgressIndicator() {
		Platform.runLater(() -> {
			progressPane.setVisible(false);
			progressPane.setDisable(true);
		});
	}
}
