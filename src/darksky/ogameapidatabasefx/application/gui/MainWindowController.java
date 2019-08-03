package darksky.ogameapidatabasefx.application.gui;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.stream.Collectors;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.application.main.GUIApplication;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.util.Pair;

public class MainWindowController implements BasicController {

	public enum WindowType {
		Overview, Search, DetailView, DataManagement, LogView, HighscoreView, GalaxyView, SettingsView, StatisticsView, SQLView
	}

	private ExecutorService m_executor = null;

	private Map<WindowType, Pair<BasicController, Node>> m_nodemap = new EnumMap<WindowType, Pair<BasicController, Node>>(
			WindowType.class);

	private Map<String, OGameAPIDatabase> m_databaseControllers = new HashMap<String, OGameAPIDatabase>();

	private Path m_pathToDatabaseFiles = Paths.get("./databases");

	private ObservableList<String> m_existingDatabasesList = FXCollections.observableArrayList();

	private SimpleObjectProperty<WindowType> m_currentWindowTypeProperty = new SimpleObjectProperty<WindowType>(null);

	private OGameAPIDatabase m_currentDatabaseController = null;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private GridPane mainGridPane;

	@FXML
	private Button overviewButton;

	@FXML
	private Button searchButton;

	@FXML
	private Button detailsButton;

	@FXML
	private Button logButton;

	@FXML
	private Button highscoreButton;

	@FXML
	private Button galaxyButton;

	@FXML
	private Button sqlButton;

	@FXML
	private Button statisticsButton;

	@FXML
	private Button settingsButton;

	@FXML
	private Button dataManagementButton;

	@FXML
	private ListView<String> existingDatabasesListView;

	@FXML
	void navigateButtonAction(ActionEvent event) {
		WindowType type = (WindowType) ((Button) event.getSource()).getUserData();
		m_currentWindowTypeProperty.set(type);
	}

	@FXML
	private void initialize() {
		assert mainGridPane != null : "fx:id=\"mainGridPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert overviewButton != null : "fx:id=\"overviewButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert searchButton != null : "fx:id=\"searchButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert detailsButton != null : "fx:id=\"detailsButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert logButton != null : "fx:id=\"logButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert highscoreButton != null : "fx:id=\"highscoreButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert galaxyButton != null : "fx:id=\"galaxyButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert sqlButton != null : "fx:id=\"sqlButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert statisticsButton != null : "fx:id=\"statisticsButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert settingsButton != null : "fx:id=\"settingsButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert dataManagementButton != null : "fx:id=\"dataManagementButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
		assert existingDatabasesListView != null : "fx:id=\"existingDatabasesListView\" was not injected: check your FXML file 'MainWindow.fxml'.";

		Util.getLogger().config("mainwindowcontroller init");

		overviewButton.setUserData(WindowType.Overview);
		dataManagementButton.setUserData(WindowType.DataManagement);
		searchButton.setUserData(WindowType.Search);
		detailsButton.setUserData(WindowType.DetailView);
		logButton.setUserData(WindowType.LogView);
		highscoreButton.setUserData(WindowType.HighscoreView);
		galaxyButton.setUserData(WindowType.GalaxyView);
		settingsButton.setUserData(WindowType.SettingsView);
		statisticsButton.setUserData(WindowType.StatisticsView);
		sqlButton.setUserData(WindowType.SQLView);

		enableButtons(false);
		settingsButton.setDisable(false);
		dataManagementButton.setDisable(false);

		initControllers();

		existingDatabasesListView.setItems(m_existingDatabasesList);
		existingDatabasesListView.getSelectionModel().selectedItemProperty()
				.addListener((property, oldvalue, newvalue) -> {
					if (newvalue != null) {
						OGameAPIDatabase newController = getDatabaseController(newvalue);
						if (newController != null) {
							m_currentDatabaseController = getDatabaseController(newvalue);
							WindowType t = m_currentWindowTypeProperty.get();
							if (t != null)
								m_nodemap.get(t).getKey().setCurrentDatabaseController(m_currentDatabaseController);
							enableButtons(true);
						}
					}
				});

		m_currentWindowTypeProperty.addListener((property, oldvalue, newvalue) -> {
			if (oldvalue != null) {
				mainGridPane.getChildren().remove(m_nodemap.get(oldvalue).getValue());
			}
			if (newvalue != null) {
				Object controllerObject = changeContentWindow(newvalue);
				switch (newvalue) {
				case DataManagement: {
					DatabaseManagementController controller = (DatabaseManagementController) controllerObject;
					controller.setExecutorService(m_executor);
					controller.setExistingServerListData(m_existingDatabasesList);
					controller.setCurrentDatabaseController(m_currentDatabaseController);
				}
					break;
				case DetailView: {
					DetailViewController controller = (DetailViewController) controllerObject;
					controller.setExecutorService(m_executor);
				}
					break;
				case GalaxyView: {
					GalaxyViewController controller = (GalaxyViewController) controllerObject;
					controller.setExecutorService(m_executor);
					controller.setCurrentDatabaseController(m_currentDatabaseController);
					controller.setDetailViewController(getDetailViewController());
				}
					break;
				case HighscoreView: {
					HighscoreViewController controller = (HighscoreViewController) controllerObject;
					controller.setExecutorService(m_executor);
					controller.setCurrentDatabaseController(m_currentDatabaseController);
					controller.setDetailViewController(getDetailViewController());
				}
					break;
				case LogView: {
					LogViewController controller = (LogViewController) controllerObject;
					controller.setExecutorService(m_executor);
					controller.setCurrentDatabaseController(m_currentDatabaseController);
					controller.setDetailViewController(getDetailViewController());
				}
					break;
				case Overview: {
					OverviewController controller = (OverviewController) controllerObject;
					controller.setExecutorService(m_executor);
					controller.setCurrentDatabaseController(m_currentDatabaseController);
				}
					break;
				case SQLView: {
					SQLViewController controller = (SQLViewController) controllerObject;
					controller.setExecutorService(m_executor);
					controller.setCurrentDatabaseController(m_currentDatabaseController);
					controller.setExistingDatabasesList(m_existingDatabasesList);
				}
					break;
				case Search: {
					SearchController controller = (SearchController) controllerObject;
					controller.setExecutorService(m_executor);

					controller.setExistingServersList(m_existingDatabasesList);

					controller.setCurrentDatabaseController(m_currentDatabaseController);
					controller.setDetailViewController(getDetailViewController());
				}
					break;
				case SettingsView: {
					SettingsViewController controller = (SettingsViewController) controllerObject;
					controller.setExecutorService(m_executor);
				}
					break;
				case StatisticsView: {
					StatisticsController controller = (StatisticsController) controllerObject;
					controller.setExecutorService(m_executor);
					controller.setCurrentDatabaseController(m_currentDatabaseController);
					controller.setExistingServerListData(m_existingDatabasesList);
				}
					break;
				default:
					break;

				}
			}
		});

	}

	public DetailViewController getDetailViewController() {
		return (DetailViewController) m_nodemap.get(WindowType.DetailView).getKey();
	}

	public DatabaseManagementController getDataManagementController() {
		return (DatabaseManagementController) m_nodemap.get(WindowType.DataManagement).getKey();
	}

	public OverviewController getOverviewController() {
		return (OverviewController) m_nodemap.get(WindowType.Overview).getKey();
	}

	public SearchController getSearchController() {
		return (SearchController) m_nodemap.get(WindowType.Search).getKey();
	}

	public LogViewController getLogViewController() {
		return (LogViewController) m_nodemap.get(WindowType.LogView).getKey();
	}

	public HighscoreViewController getHighscoreViewController() {
		return (HighscoreViewController) m_nodemap.get(WindowType.HighscoreView).getKey();
	}

	public HighscoreViewController getGalaxyViewController() {
		return (HighscoreViewController) m_nodemap.get(WindowType.GalaxyView).getKey();
	}

	public SettingsViewController getSettingsViewController() {
		return (SettingsViewController) m_nodemap.get(WindowType.SettingsView).getKey();
	}

	public StatisticsController getOverviewHistoryController() {
		return (StatisticsController) m_nodemap.get(WindowType.StatisticsView).getKey();
	}

	public SQLViewController getSQLViewController() {
		return (SQLViewController) m_nodemap.get(WindowType.SQLView).getKey();
	}

	private void initControllers() {
		Pair<BasicController, Node> pair = null;
		for (WindowType type : WindowType.values()) {
			pair = loadContentWindow(type);
			BasicController controller = pair.getKey();
			controller.setParentController(this);
			m_nodemap.put(type, pair);
		}
	}

	private BasicController changeContentWindow(WindowType type) {

		Pair<BasicController, Node> pair = null;
		if (m_nodemap.containsKey(type)) {
			pair = m_nodemap.get(type);
		} else {
			pair = loadContentWindow(type);
		}

		Node newContentNode = pair.getValue();
		mainGridPane.add(newContentNode, 1, 0, 1, 1);
		GridPane.setHgrow(newContentNode, Priority.ALWAYS);
		GridPane.setVgrow(newContentNode, Priority.ALWAYS);

		m_nodemap.put(type, pair);
		return pair.getKey();
	}

	private Pair<BasicController, Node> loadContentWindow(WindowType type) {
		FXMLLoader fxmlLoader = new FXMLLoader();

		fxmlLoader.setResources(resources);

		Parent node = null;
		try {
			switch (type) {
			case Overview:
				node = fxmlLoader
						.load(getClass().getResource(GUIApplication.fxmlResourcePath + "Overview.fxml").openStream());
				node.getStylesheets()
						.add(getClass().getResource(GUIApplication.cssResourcePath + "Overview.css").toExternalForm());
				break;
			case DetailView:
				node = fxmlLoader
						.load(getClass().getResource(GUIApplication.fxmlResourcePath + "DetailView.fxml").openStream());
				node.getStylesheets().add(
						getClass().getResource(GUIApplication.cssResourcePath + "DetailView.css").toExternalForm());
				break;
			case Search:
				node = fxmlLoader
						.load(getClass().getResource(GUIApplication.fxmlResourcePath + "Search.fxml").openStream());
				node.getStylesheets()
						.add(getClass().getResource(GUIApplication.cssResourcePath + "Search.css").toExternalForm());
				break;
			case DataManagement:
				node = fxmlLoader.load(getClass()
						.getResource(GUIApplication.fxmlResourcePath + "DatabaseManagement.fxml").openStream());
				node.getStylesheets().add(getClass()
						.getResource(GUIApplication.cssResourcePath + "DatabaseManagement.css").toExternalForm());
				break;
			case LogView:
				node = fxmlLoader
						.load(getClass().getResource(GUIApplication.fxmlResourcePath + "LogView.fxml").openStream());
				node.getStylesheets()
						.add(getClass().getResource(GUIApplication.cssResourcePath + "LogView.css").toExternalForm());
				break;
			case HighscoreView:
				node = fxmlLoader.load(
						getClass().getResource(GUIApplication.fxmlResourcePath + "HighscoreView.fxml").openStream());
				node.getStylesheets().add(
						getClass().getResource(GUIApplication.cssResourcePath + "HighscoreView.css").toExternalForm());
				break;
			case GalaxyView:
				node = fxmlLoader
						.load(getClass().getResource(GUIApplication.fxmlResourcePath + "GalaxyView.fxml").openStream());
				node.getStylesheets().add(
						getClass().getResource(GUIApplication.cssResourcePath + "GalaxyView.css").toExternalForm());
				break;
			case SettingsView:
				node = fxmlLoader.load(
						getClass().getResource(GUIApplication.fxmlResourcePath + "SettingsView.fxml").openStream());
				node.getStylesheets().add(
						getClass().getResource(GUIApplication.cssResourcePath + "SettingsView.css").toExternalForm());
				break;
			case StatisticsView:
				node = fxmlLoader.load(
						getClass().getResource(GUIApplication.fxmlResourcePath + "StatisticsView.fxml").openStream());
				node.getStylesheets().add(
						getClass().getResource(GUIApplication.cssResourcePath + "StatisticsView.css").toExternalForm());
				break;

			case SQLView:
				node = fxmlLoader
						.load(getClass().getResource(GUIApplication.fxmlResourcePath + "SQLView.fxml").openStream());
				node.getStylesheets()
						.add(getClass().getResource(GUIApplication.cssResourcePath + "SQLView.css").toExternalForm());
				break;

			default:
				Pane pane = new Pane();
				pane.getChildren().add(new Text("Error:\n Could not load content"));
				node = pane;
				break;
			}
		} catch (NullPointerException | IOException e1) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e1);
			Pane pane = new Pane();
			pane.getChildren().add(new Text("Error:\n Could not load content"));
			node = pane;
		}

		BasicController c = fxmlLoader.getController();
		return new Pair<BasicController, Node>(c, node);

	}

	private List<Path> getExistingDatabaseFiles(Path pathToDatabaseFiles) throws IOException {

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(pathToDatabaseFiles)) {
			List<Path> list = new ArrayList<Path>();
			stream.forEach(p -> {
				String s = p.getFileName().toString();
				if (s.matches("^s[0-9]+-[a-zA-Z]+")) {
					list.add(p);
				}
			});
			return list;
		} catch (NoSuchFileException e) {
			Util.getLogger().log(Level.WARNING, "invalid path", e);
			return Collections.emptyList();
		}
	}

	public OGameAPIDatabase getDatabaseController(String serverPrefix) {
		Objects.requireNonNull(serverPrefix);

		try {
			OGameAPIDatabase controller = m_databaseControllers.get(serverPrefix);
			if (controller == null) {
				controller = new OGameAPIDatabase(serverPrefix, m_pathToDatabaseFiles.toAbsolutePath().toString(), "",
						"");
				m_databaseControllers.put(serverPrefix, controller);
			}

			return controller;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public Path getPathToDatabaseFiles() {
		return m_pathToDatabaseFiles;
	}

	public void setPathToDatabaseFiles(Path pathToDatabaseFiles) {
		Util.getLogger().config("dbpath = " + pathToDatabaseFiles);
		m_pathToDatabaseFiles = Objects.requireNonNull(pathToDatabaseFiles);

		try {
			if (!Files.exists(m_pathToDatabaseFiles)) {
				Files.createDirectories(m_pathToDatabaseFiles);
			}
			List<String> blalist = getExistingDatabaseFiles(m_pathToDatabaseFiles).stream()
					.map(p -> p.getFileName().toString().split("\\.")[0]).sorted((a, b) -> {
						String[] tmpa = a.substring(1).split("-");
						String[] tmpb = b.substring(1).split("-");
						assert tmpa.length == 2;
						assert tmpb.length == 2;
						if (tmpa[1].equals(tmpb[1])) {
							return Integer.compare(Integer.parseInt(tmpa[0]), Integer.parseInt(tmpb[0]));
						} else {
							return tmpa[1].compareTo(tmpb[1]);
						}
					}).collect(Collectors.toList());
			m_existingDatabasesList.addAll(blalist);

			final String defaultSelection = Settings.getIni().get("global", "defaultserver");
			if (existingDatabasesListView.getItems().contains(defaultSelection)) {
				existingDatabasesListView.getSelectionModel().select(defaultSelection);
				existingDatabasesListView.scrollTo(defaultSelection);
			}
		} catch (IOException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
		}
	}

	private void enableButtons(boolean enabled) {
		overviewButton.setDisable(!enabled);
		statisticsButton.setDisable(!enabled);
		dataManagementButton.setDisable(!enabled);
		searchButton.setDisable(!enabled);
		detailsButton.setDisable(!enabled);
		logButton.setDisable(!enabled);
		highscoreButton.setDisable(!enabled);
		galaxyButton.setDisable(!enabled);
		sqlButton.setDisable(!enabled);
		settingsButton.setDisable(!enabled);
	}

	@Override
	public void setParentController(BasicController parentController) {
	}

	@Override
	public void setCurrentDatabaseController(OGameAPIDatabase databaseController) {
		m_currentDatabaseController = Objects.requireNonNull(databaseController);
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		m_executor = Objects.requireNonNull(executorService);
		m_nodemap.values().stream().filter(e -> e != null).map(e -> e.getKey())
				.forEach(controller -> controller.setExecutorService(m_executor));
	}
}
