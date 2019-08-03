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
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.stream.Collectors;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.AccountData;
import darksky.ogameapidatabasefx.database.entities.Alliance;
import darksky.ogameapidatabasefx.database.entities.EntityReader;
import darksky.ogameapidatabasefx.database.entities.Highscore;
import darksky.ogameapidatabasefx.database.entities.Moon;
import darksky.ogameapidatabasefx.database.entities.Planet;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

public class PlayerDetailViewController implements BasicController {

	private List<Pair<LocalDate, String>> m_logTextList = new ArrayList<Pair<LocalDate, String>>();

	private List<ChangeLogEntry<Player, Optional<Alliance>>> m_allianceChanges = null;
	private List<ChangeLogEntry<Player, Planet>> m_moonNameChanges = null;
	private List<ChangeLogEntry<Player, Planet>> m_planetNameChanges = null;
	private List<ChangeLogEntry<Player, Player>> m_playerNameChanges = null;
	private List<ChangeLogEntry<Player, Player>> m_playerStatusChanges = null;
	private List<ChangeLogEntry<Player, Planet>> m_relocations = null;
	private List<ChangeLogEntry<Player, Planet>> m_newPlanets = null;
	private List<ChangeLogEntry<Player, Planet>> m_deletedPlanets = null;
	private List<ChangeLogEntry<Player, Planet>> m_newMoons = null;
	private List<ChangeLogEntry<Player, Planet>> m_deletedMoons = null;

	private DetailViewController m_detailViewController = null;

	private Player m_player;
	private AccountData m_accountData;
	private String m_serverPrefix;
	private EntityReader m_entityReader;

	private LogReader m_logReader;

	private ExecutorService m_executor = null;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TextArea playerInformationTextArea;

	@FXML
	private TextArea allianceInformationTextArea;

	@FXML
	private TextArea planetListTextArea;

	@FXML
	private TextArea highscoreTextArea;

	@FXML
	private TextArea changelogTextArea;

	@FXML
	private StackPane progressPane;

	@FXML
	private ProgressIndicator progressIndicator;

	@FXML
	private Label progressIndicatorLabel;

	@FXML
	void initialize() {
		assert playerInformationTextArea != null : "fx:id=\"playerInformationTextArea\" was not injected: check your FXML file 'PlayerDetailView.fxml'.";
		assert allianceInformationTextArea != null : "fx:id=\"allianceInformationTextArea\" was not injected: check your FXML file 'PlayerDetailView.fxml'.";
		assert planetListTextArea != null : "fx:id=\"planetListTextArea\" was not injected: check your FXML file 'PlayerDetailView.fxml'.";
		assert highscoreTextArea != null : "fx:id=\"highscoreTextArea\" was not injected: check your FXML file 'PlayerDetailView.fxml'.";
		assert changelogTextArea != null : "fx:id=\"changelogTextArea\" was not injected: check your FXML file 'PlayerDetailView.fxml'.";
		assert progressPane != null : "fx:id=\"progressPane\" was not injected: check your FXML file 'PlayerDetailView.fxml'.";
		assert progressIndicator != null : "fx:id=\"progressIndicator\" was not injected: check your FXML file 'PlayerDetailView.fxml'.";
		assert progressIndicatorLabel != null : "fx:id=\"progressIndicatorLabel\" was not injected: check your FXML file 'PlayerDetailView.fxml'.";

		allianceInformationTextArea.setOnMouseClicked((event) -> {

			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				m_accountData.getAlliance().ifPresent(a -> m_detailViewController.displayAlliance(a, m_serverPrefix));
			}
		});

		highscoreTextArea.setOnMouseClicked((event) -> {			

			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				
				HighscoreType[] chartTypes = { HighscoreType.Total, HighscoreType.Economy, HighscoreType.Research,
						HighscoreType.Military };
				
				List<Highscore> highscoreList = m_accountData.getHighscores();
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
				highscoreChart.setTitle(resources.getString("highscore") + " " + m_accountData.getPlayer().getName());

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
				stage.setTitle(m_accountData.getPlayer().getName());
				stage.setScene(new Scene(vBox, 800, 400));
				stage.show();
			}
		});

	}

	public void setData(Player player, OGameAPIDatabase dbcontroller) {
		Objects.requireNonNull(player);
		Objects.requireNonNull(dbcontroller);

		m_entityReader = dbcontroller.getEntityReader();
		m_logReader = dbcontroller.getLogReader();
		m_serverPrefix = dbcontroller.getServerPrefix();
		m_player = player;

		m_executor.submit(() -> {
			showProgressIndicator();
			getPlayerInformationFromDatabase();
			displayPlayerInformation();
			displayAllianceInformation();
			displayPlanetList();
			displayHighscore();
			displayChangeLog();
			hideProgressIndicator();
		});
	}

	private void displayPlayerInformation() {
		if (m_player != null) {
			Platform.runLater(() -> {
				playerInformationTextArea.appendText(String.format("%s [%s] (Id %d)%n%n", m_player.getName(),
						m_player.getPlayerStatus(), m_player.getId()));
				playerInformationTextArea.appendText(String.format("%s %s%n%n", resources.getString("indatabasesince"),
						LocalDateTime.ofInstant(m_player.getInsertedOn(), ZoneId.systemDefault()).toLocalDate()));
				playerInformationTextArea.appendText(String.format("%s %s%n%n", resources.getString("lastupdatedat"),
						LocalDateTime.ofInstant(m_player.getLastUpdate(), ZoneId.systemDefault()).toLocalDate()));
				playerInformationTextArea.appendText(m_player.isDeleted() ? resources.getString("deleted") + " !" : "");
			});
		}
	}

	private void displayAllianceInformation() {
		if (m_accountData.getAlliance().isPresent()) {
			Alliance alliance = m_accountData.getAlliance().get();
			Platform.runLater(() -> {
				allianceInformationTextArea.appendText(String.format("%s [%s] (Id %d)%n%n", alliance.getName(),
						alliance.getAllianceTag(), alliance.getId()));
				allianceInformationTextArea.appendText(String.format("%s %s%n%n",
						resources.getString("indatabasesince"),
						LocalDateTime.ofInstant(alliance.getInsertedOn(), ZoneId.systemDefault()).toLocalDate()));
				allianceInformationTextArea.appendText(String.format("%s %s%n%n", resources.getString("lastupdatedat"),
						LocalDateTime.ofInstant(alliance.getLastUpdate(), ZoneId.systemDefault()).toLocalDate()));
				allianceInformationTextArea
						.appendText(alliance.isDeleted() ? resources.getString("deleted") + " !" : "");
			});
		} else {
			Platform.runLater(() -> {
				allianceInformationTextArea.setText(resources.getString("notmemberofalliance"));
			});
		}
	}

	private void displayPlanetList() {
		if (m_accountData != null) {
			StringBuilder text = new StringBuilder();
			String deletedString = resources.getString("deleted");
			m_accountData.getPlanetMap().entrySet().forEach(entry -> {
				Planet p = entry.getKey();
				List<Moon> moonList = entry.getValue();

				String planetText = String.format("[%d:%3d:%2d] %-20s %s", p.getGalaxy(), p.getSystem(),
						p.getPosition(), p.getName(), (p.isDeleted() ? "(" + deletedString + ")" : ""));

				if (moonList.size() == 0) {
					text.append(planetText);
					text.append("\n");
				} else {

					for (int i = 0; i < moonList.size(); ++i) {
						Moon m = moonList.get(i);
						if (m != null) {
							String moontext = String.format(" - [%d km] %s %s", m.getMoonSize(), m.getName(),
									(m.isDeleted() ? "(" + deletedString + ")" : ""));
							text.append(String.format("%-1s %-1s%n", (i == 0 ? planetText : ""), moontext));
						}

					}
				}
			});
			Platform.runLater(() -> planetListTextArea.setText(text.toString()));
		}
	}

	private void displayHighscore() {
		if (m_accountData != null) {
			Highscore playerHighscore = m_accountData.getHighscores().get(m_accountData.getHighscores().size() - 1);
			StringBuilder text = new StringBuilder();
			NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);

			for (HighscoreType t : HighscoreType.values()) {
				text.append(String.format("%-20s : %15s - %6s", resources.getString(t.toString().toLowerCase()),
						nf.format(playerHighscore.getPoints(t)), nf.format(playerHighscore.getRank(t))));
				text.append("\n");
			}
			Platform.runLater(() -> {
				highscoreTextArea.setText(text.toString());
			});
		}
	}

	private void displayChangeLog() {

		if (m_allianceChanges != null) {
			m_allianceChanges.stream().map(logentry -> {
				LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
				// boolean a =
				// !logentry.getOldValue().orElse(null).equals(m_accountData.getAlliance().get());
				boolean a = !logentry.getOldValue().isPresent();
				boolean b = !logentry.getNewValue().isPresent();
				assert (a && b) == false;
				String text = "";
				if (a) {
					text = String.format("%s %s (Id %d)", resources.getString("enteredalliance"),
							logentry.getNewValue().get().getName(), logentry.getNewValue().get().getId());
				} else {
					if (b) {
						text = String.format("%s %s (Id %d)", resources.getString("leftalliance"),
								logentry.getOldValue().get().getName(), logentry.getOldValue().get().getId());
					} else {
						text = String.format("%s - %s (Id %d) -> %s (Id %d)", resources.getString("changedalliance"),
								logentry.getOldValue().get().getName(), logentry.getOldValue().get().getId(),
								logentry.getNewValue().get().getName(), logentry.getNewValue().get().getId());
					}
				}
				return new Pair<LocalDate, String>(date, text);
			}).forEach(m_logTextList::add);
		}
		if (m_moonNameChanges != null) {
			m_moonNameChanges.stream().map(logentry -> {
				LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
				String text = String.format("%s - (Id %d) %s -> %s", resources.getString("renamedmoon"),
						logentry.getOwner().getId(), logentry.getOldValue().getMoon().get().getName(),
						logentry.getNewValue().getMoon().get().getName());
				return new Pair<LocalDate, String>(date, text);
			}).forEach(m_logTextList::add);
		}
		if (m_planetNameChanges != null) {
			m_planetNameChanges.stream().map(logentry -> {
				LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
				String text = String.format("%s - (Id %d) %s -> %s", resources.getString("renamedplanet"),
						logentry.getOwner().getId(), logentry.getOldValue().getName(),
						logentry.getNewValue().getName());
				return new Pair<LocalDate, String>(date, text);
			}).forEach(m_logTextList::add);
		}
		if (m_playerNameChanges != null) {
			m_playerNameChanges.stream().map(logentry -> {
				LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
				String text = String.format("%s - %s -> %s", resources.getString("renamedplayer"),
						logentry.getOldValue().getName(), logentry.getNewValue().getName());
				return new Pair<LocalDate, String>(date, text);
			}).forEach(m_logTextList::add);
		}
		if (m_playerStatusChanges != null) {
			m_playerStatusChanges.stream().map(logentry -> {
				LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
				String text = String.format("%s - %s -> %s", resources.getString("statuschange"),
						logentry.getOldValue().getPlayerStatus(), logentry.getNewValue().getPlayerStatus());
				return new Pair<LocalDate, String>(date, text);
			}).forEach(m_logTextList::add);
		}
		if (m_relocations != null) {
			m_relocations.stream().map(logentry -> {
				LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
				Planet op = logentry.getOldValue();
				Planet np = logentry.getNewValue();
				String text = String.format("%s - [%d:%3d:%2d] -> [%d:%3d:%2d]", resources.getString("relocation"),
						op.getGalaxy(), op.getSystem(), op.getPosition(), np.getGalaxy(), np.getSystem(),
						np.getPosition());
				return new Pair<LocalDate, String>(date, text);
			}).forEach(m_logTextList::add);
		}
		if (m_newPlanets != null) {
			m_newPlanets.stream().map(logentry -> {
				LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
				Planet planet = logentry.getNewValue();
				String text = String.format("%s - %s [%d:%3d:%2d] (Id %d)", resources.getString("newplanet"),
						planet.getName(), planet.getGalaxy(), planet.getSystem(), planet.getPosition(), planet.getId());
				return new Pair<LocalDate, String>(date, text);
			}).forEach(m_logTextList::add);
		}
		if (m_deletedPlanets != null) {
			m_deletedPlanets.stream().map(logentry -> {
				LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
				Planet planet = logentry.getOldValue();
				String text = String.format("%s - %s [%d:%3d:%2d] (Id %d)", resources.getString("deletedplanet"),
						planet.getName(), planet.getGalaxy(), planet.getSystem(), planet.getPosition(), planet.getId());
				return new Pair<LocalDate, String>(date, text);
			}).forEach(m_logTextList::add);
		}
		if (m_newMoons != null) {
			m_newMoons.stream().map(logentry -> {
				LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
				Planet planet = logentry.getNewValue();
				Moon moon = planet.getMoon().get();
				String text = String.format("%s - %s (Id %d)%s", resources.getString("newmoon"), moon.getName(),
						moon.getId(), planet != null ? String.format(" [%d:%3d:%2d] (Id %d)", planet.getGalaxy(),
								planet.getSystem(), planet.getPosition(), planet.getId()) : "");
				return new Pair<LocalDate, String>(date, text);
			}).forEach(m_logTextList::add);
		}
		if (m_deletedMoons != null) {
			m_deletedMoons.stream().map(logentry -> {
				LocalDate date = LocalDateTime.ofInstant(logentry.getInstant(), ZoneId.systemDefault()).toLocalDate();
				Planet planet = logentry.getOldValue();
				Moon moon = planet.getMoon().get();
				String text = String.format("%s - %s (Id %d)%s", resources.getString("deletedmoon"), moon.getName(),
						moon.getId(), planet != null ? String.format(" [%d:%3d:%2d] (Id %d)", planet.getGalaxy(),
								planet.getSystem(), planet.getPosition(), planet.getId()) : "");
				return new Pair<LocalDate, String>(date, text);
			}).forEach(m_logTextList::add);
		}

		// entries are sorted by date descending

		StringBuilder displayedLogText = new StringBuilder();
		if (m_logTextList.size() > 0) {
			displayedLogText.append(m_logTextList.stream().sorted((c1, c2) -> c2.getKey().compareTo(c1.getKey()))
					.map(c -> c.getKey() + " : " + c.getValue()).collect(Collectors.joining("\n")));
		}

		Platform.runLater(() -> changelogTextArea.setText(displayedLogText.toString()));
	}

	private void getPlayerInformationFromDatabase() {
		try {
			m_accountData = m_entityReader.getAccountData(m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "Could not get account data of player " + m_player.getId(), e);
		}

		LocalDate from = LocalDate.of(2000, 1, 1);
		LocalDate to = LocalDate.now().plusDays(1);

		try {
			m_allianceChanges = m_logReader.getAllianceMemberChanges(from, to, m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "could not get alliance member changes", e);
		}
		try {
			m_moonNameChanges = m_logReader.getMoonNameChanges(from, to, m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "could not get moon name changes", e);
		}
		try {
			m_planetNameChanges = m_logReader.getPlanetNameChanges(from, to, m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "could not get planet name changes", e);
		}
		try {
			m_playerNameChanges = m_logReader.getPlayerNameChanges(from, to, m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "could not get player name changes", e);
		}
		try {
			m_playerStatusChanges = m_logReader.getPlayerStatusChanges(from, to, m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "could not get player status changes", e);
		}
		try {
			m_relocations = m_logReader.getRelocations(from, to, m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "could not get relocations", e);
		}

		try {
			m_newPlanets = m_logReader.getNewPlanets(from, to, m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "could not get new planets", e);
		}
		try {
			m_deletedPlanets = m_logReader.getDeletedPlanets(from, to, m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "could not get deleted planets", e);
		}
		try {
			m_newMoons = m_logReader.getNewMoons(from, to, m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "could not get new moons", e);
		}
		try {
			m_deletedMoons = m_logReader.getDeletedMoons(from, to, m_player);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, "could not get deleted moons", e);
		}
	}

	@Override
	public void setParentController(BasicController parentController) {
		m_detailViewController = Objects.requireNonNull((DetailViewController) parentController);
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
