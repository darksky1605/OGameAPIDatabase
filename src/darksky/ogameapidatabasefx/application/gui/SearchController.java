package darksky.ogameapidatabasefx.application.gui;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.Alliance;
import darksky.ogameapidatabasefx.database.entities.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.util.Pair;

public class SearchController implements BasicController {

	private ExecutorService m_executor = null;

	private MainWindowController m_parentController = null;

	private DetailViewController m_detailViewController = null;

	private OGameAPIDatabase m_currentDatabaseController = null;

	private ObservableList<String> m_existingServersList = FXCollections
			.observableArrayList();

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ListView<Pair<String, Player>> playerList;

	@FXML
	private TextField searchInputField;

	@FXML
	private ListView<Pair<String, Alliance>> allianceList;

	@FXML
	private Button searchSubmitButton;

	@FXML
	private ProgressBar searchProgressBar;

	@FXML
	private CheckBox everyDatabaseCheckbox;

	@FXML
	void searchSubmitAction(ActionEvent e) {
		List<OGameAPIDatabase> controllers;
		if (everyDatabaseCheckbox.isSelected()) {
			controllers = m_existingServersList.stream()
					.map(s -> m_parentController.getDatabaseController(s))
					.collect(Collectors.toList());

		} else {
			controllers = Collections
					.singletonList(m_currentDatabaseController);
		}

		search(controllers);
	}

	@FXML
	private void initialize() {
		assert playerList != null : "fx:id=\"playerList\" was not injected: check your FXML file 'Search.fxml'.";
		assert searchInputField != null : "fx:id=\"searchInputField\" was not injected: check your FXML file 'Search.fxml'.";
		assert allianceList != null : "fx:id=\"allianceList\" was not injected: check your FXML file 'Search.fxml'.";
		assert searchSubmitButton != null : "fx:id=\"searchSubmitButton\" was not injected: check your FXML file 'Search.fxml'.";
		assert searchProgressBar != null : "fx:id=\"searchProgressBar\" was not injected: check your FXML file 'Search.fxml'.";
		assert everyDatabaseCheckbox != null : "fx:id=\"everyDatabaseCheckbox\" was not injected: check your FXML file 'Search.fxml'.";

		Util.getLogger().config("SearchController init");

		// set cell factories
		playerList
				.setCellFactory((ListView<Pair<String, Player>> param) -> {
					ListCell<Pair<String, Player>> cell = new ListCell<Pair<String, Player>>() {

						@Override
						protected void updateItem(Pair<String, Player> item,
								boolean bln) {
							super.updateItem(item, bln);
							if (item != null && item.getKey() != null
									&& item.getValue() != null) {
								String serverPrefix = item.getKey();
								Player player = item.getValue();
								setText(serverPrefix
										+ " - "
										+ player.getName()
										+ " ("
										+ player.getPlayerStatus()
										+ ")"
										+ (player.isDeleted() ? " "
												+ resources
														.getString("deleted")
												: ""));
							} else {
								setText(null);
								setGraphic(null);
							}
						}

					};

					return cell;
				});

		allianceList
				.setCellFactory((ListView<Pair<String, Alliance>> param) -> {
					ListCell<Pair<String, Alliance>> cell = new ListCell<Pair<String, Alliance>>() {

						@Override
						protected void updateItem(Pair<String, Alliance> item,
								boolean bln) {
							super.updateItem(item, bln);
							if (item != null && item.getKey() != null
									&& item.getValue() != null) {
								String serverPrefix = item.getKey();
								Alliance alliance = item.getValue();
								setText(serverPrefix
										+ " - "
										+ alliance.getName()
										+ " ("
										+ alliance.getAllianceTag()
										+ ")"
										+ (alliance.isDeleted() ? " "
												+ resources
														.getString("deleted")
												: ""));
							} else {
								setText(null);
								setGraphic(null);
							}
						}

					};

					return cell;
				});

		// set mouselisteners
		playerList.setOnMouseClicked((event) -> {

			if (event.getButton().equals(MouseButton.PRIMARY)
					&& event.getClickCount() == 2) {

				Pair<String, Player> pair = playerList.getSelectionModel()
						.getSelectedItem();

				if (pair != null)
					m_detailViewController.displayPlayer(pair.getValue(),
							pair.getKey());
			}
		});

		allianceList.setOnMouseClicked((event) -> {

			if (event.getButton().equals(MouseButton.PRIMARY)
					&& event.getClickCount() == 2) {

				Pair<String, Alliance> pair = allianceList.getSelectionModel()
						.getSelectedItem();
				if (pair != null)
					m_detailViewController.displayAlliance(pair.getValue(),
							pair.getKey());
			}
		});
	}

	private void search(List<OGameAPIDatabase> controllers) {
		Objects.requireNonNull(controllers);

		String searchParameter = searchInputField
				.getText();

		PlayerSearchTask ptask = new PlayerSearchTask(controllers,
				searchParameter);
		AllianceSearchTask atask = new AllianceSearchTask(controllers,
				searchParameter);

		EventHandler<WorkerStateEvent> shandler = (event) -> {
			if (event.getSource().getException() != null)
				Util.displayInformation(resources
						.getString("error_incompleteresults"));
		};

		ptask.setOnSucceeded(shandler);
		atask.setOnSucceeded(shandler);

		playerList.itemsProperty().bind(ptask.valueProperty());
		allianceList.itemsProperty().bind(atask.valueProperty());

		searchSubmitButton.disableProperty().bind(
				ptask.runningProperty().or(atask.runningProperty()));
		searchProgressBar.visibleProperty().bind(
				ptask.runningProperty().or(atask.runningProperty()));
		searchProgressBar.progressProperty().bind(
				(ptask.progressProperty().add(atask.progressProperty())
						.divide(2.0)));

		m_executor.submit(ptask);
		m_executor.submit(atask);
	}

	public void setDetailViewController(
			DetailViewController detailViewController) {
		m_detailViewController = Objects.requireNonNull(detailViewController);
	}

	public void setExistingServersList(List<String> existingServerList) {
		Objects.requireNonNull(existingServerList);
		m_existingServersList.setAll(existingServerList);
	}

	@Override
	public void setCurrentDatabaseController(
			OGameAPIDatabase currentDatabaseController) {
		m_currentDatabaseController = Objects
				.requireNonNull(currentDatabaseController);
	}

	@Override
	public void setParentController(BasicController parentController) {
		m_parentController = Objects
				.requireNonNull((MainWindowController) parentController);
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		m_executor = Objects.requireNonNull(executorService);
	}
}
