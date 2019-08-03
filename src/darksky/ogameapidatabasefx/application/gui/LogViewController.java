package darksky.ogameapidatabasefx.application.gui;

import java.net.URL;
import java.time.LocalDate;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.Alliance;
import darksky.ogameapidatabasefx.database.entities.Entity;
import darksky.ogameapidatabasefx.database.entities.Player;
import darksky.ogameapidatabasefx.database.logs.LogReader;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.util.Pair;

public class LogViewController implements BasicController {

	private ExecutorService m_executor = null;

	private DetailViewController m_detailViewController = null;

	private SimpleObjectProperty<OGameAPIDatabase> m_currentDatabaseControllerProperty = new SimpleObjectProperty<OGameAPIDatabase>(
			null);

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private RadioButton inactivesChooser;

	@FXML
	private ToggleGroup logToggleGroup;

	@FXML
	private RadioButton statusChangeChooser;
	
	@FXML
	private RadioButton pilloryChooser;

	@FXML
	private RadioButton memberChangesChooser;

	@FXML
	private RadioButton relocationsChooser;

	@FXML
	private RadioButton playerNameChooser;

	@FXML
	private RadioButton allianceNameChooser;

	@FXML
	private RadioButton planetNameChooser;

	@FXML
	private RadioButton moonNameChooser;

	@FXML
	private RadioButton newPlayersChooser;

	@FXML
	private RadioButton newAlliancesChooser;

	@FXML
	private RadioButton newPlanetsChooser;

	@FXML
	private RadioButton newMoonsChooser;

	@FXML
	private RadioButton deletedPlayersChooser;

	@FXML
	private RadioButton deletedAlliancesChooser;

	@FXML
	private RadioButton deletedPlanetsChooser;

	@FXML
	private RadioButton deletedMoonsChooser;

	@FXML
	private RadioButton allianceTagChangesChooser;

	@FXML
	private RadioButton allianceHomepageChangesChooser;

	@FXML
	private RadioButton allianceLogoChangesChooser;

	@FXML
	private RadioButton allianceApplicationChangesChooser;

	@FXML
	private DatePicker fromDateInput;

	@FXML
	private DatePicker toDateInput;

	@FXML
	private Button submitButton;

	@FXML
	private ProgressBar logProgressBar;

	@FXML
	private ListView<Pair<Entity, String>> logList;

	@FXML
	private void initialize() {
		assert inactivesChooser != null : "fx:id=\"inactivesChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert logToggleGroup != null : "fx:id=\"logToggleGroup\" was not injected: check your FXML file 'LogView.fxml'.";
		assert statusChangeChooser != null : "fx:id=\"statusChangeChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert pilloryChooser != null : "fx:id=\"pilloryChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert memberChangesChooser != null : "fx:id=\"memberChangesChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert relocationsChooser != null : "fx:id=\"relocationsChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert playerNameChooser != null : "fx:id=\"playerNameChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert allianceNameChooser != null : "fx:id=\"allianceNameChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert planetNameChooser != null : "fx:id=\"planetNameChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert moonNameChooser != null : "fx:id=\"moonNameChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert newPlayersChooser != null : "fx:id=\"newPlayersChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert newAlliancesChooser != null : "fx:id=\"newAlliancesChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert newPlanetsChooser != null : "fx:id=\"newPlanetsChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert newMoonsChooser != null : "fx:id=\"newMoonsChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert deletedPlayersChooser != null : "fx:id=\"deletedPlayersChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert deletedAlliancesChooser != null : "fx:id=\"deletedAlliancesChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert deletedPlanetsChooser != null : "fx:id=\"deletedPlanetsChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert deletedMoonsChooser != null : "fx:id=\"deletedMoonsChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert allianceTagChangesChooser != null : "fx:id=\"allianceTagChangesChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert allianceHomepageChangesChooser != null : "fx:id=\"allianceHomepageChangesChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert allianceLogoChangesChooser != null : "fx:id=\"allianceLogoChangesChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert allianceApplicationChangesChooser != null : "fx:id=\"allianceApplicationChangesChooser\" was not injected: check your FXML file 'LogView.fxml'.";
		assert fromDateInput != null : "fx:id=\"fromDateInput\" was not injected: check your FXML file 'LogView.fxml'.";
		assert toDateInput != null : "fx:id=\"toDateInput\" was not injected: check your FXML file 'LogView.fxml'.";
		assert submitButton != null : "fx:id=\"submitButton\" was not injected: check your FXML file 'LogView.fxml'.";
		assert logList != null : "fx:id=\"logList\" was not injected: check your FXML file 'LogView.fxml'.";

		Util.getLogger().config("LogViewController init");

		m_currentDatabaseControllerProperty.addListener((p, oldc, newc) -> {
			if (oldc != newc) {
				logList.getItems().clear();
			}
		});

		int minusDays = Settings.getIni().get("logs", "daysInPast", int.class);
		minusDays = Math.abs(minusDays);
		setLogBeginDate(minusDays);
		
		toDateInput.setValue(LocalDate.now());

		logList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		// mouse listener to display log owner in detail view
		logList.setOnMouseClicked((event) -> {

			if (event.getButton().equals(MouseButton.PRIMARY)
					&& event.getClickCount() == 2) {
				Entity actor = logList.getSelectionModel().getSelectedItem()
						.getKey();
				if (actor instanceof Player) {
					m_detailViewController.displayPlayer((Player) actor,
							m_currentDatabaseControllerProperty.get()
									.getServerPrefix());
				}
				if (actor instanceof Alliance) {
					m_detailViewController.displayAlliance((Alliance) actor,
							m_currentDatabaseControllerProperty.get()
									.getServerPrefix());
				}

			}
		});

		// format cells
		logList.setCellFactory((ListView<Pair<Entity, String>> param) -> {
			ListCell<Pair<Entity, String>> cell = new ListCell<Pair<Entity, String>>() {
				@Override
				protected void updateItem(Pair<Entity, String> entry,
						boolean empty) {
					super.updateItem(entry, empty);
					if (entry != null && !empty) {
						setText(entry.getValue());
					} else {
						setText(null);
						setGraphic(null);
					}
				}
			};
			return cell;
		});

		setChooserUserData();

		KeyCodeCombination copyCombination = new KeyCodeCombination(KeyCode.C,
				KeyCombination.CONTROL_ANY);
		logList.setOnKeyPressed(keyEvent -> {
			if (copyCombination.match(keyEvent)) {

				if (keyEvent.getSource() instanceof ListView) {

					StringBuilder clipboardString = new StringBuilder();

					ObservableList<Pair<Entity, String>> positionList = logList
							.getSelectionModel().getSelectedItems();

					positionList.forEach(p -> clipboardString.append(p
							.getValue() + "\n"));

					final ClipboardContent clipboardContent = new ClipboardContent();
					clipboardContent.putString(clipboardString.toString());

					Clipboard.getSystemClipboard().setContent(clipboardContent);

					keyEvent.consume();
				}

			}
		});
	}

	@FXML
	void submitButtonAction() {

		LogReader logReader = m_currentDatabaseControllerProperty.get()
				.getLogReader();

		LocalDate from = fromDateInput.getValue();
		LocalDate to = toDateInput.getValue().plusDays(1);

		int logType = (int) logToggleGroup.getSelectedToggle().getUserData();

		LogAccessTask task = new LogAccessTask(logReader, logType, from, to,
				resources);
		task.setOnFailed(e -> {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(),
					task.getException());
			Util.displayErrorMessage(resources.getString("error_loadlogs"));
		});

		submitButton.disableProperty().bind(task.runningProperty());
		logList.itemsProperty().bind(task.valueProperty());

		m_executor.submit(task);

	}

	private void setChooserUserData() {

		// set user data. this determines which log to get
		int i = -1;
		inactivesChooser.setUserData(++i);
		statusChangeChooser.setUserData(++i);
		memberChangesChooser.setUserData(++i);
		relocationsChooser.setUserData(++i);
		playerNameChooser.setUserData(++i);
		allianceNameChooser.setUserData(++i);
		planetNameChooser.setUserData(++i);
		moonNameChooser.setUserData(++i);
		newPlayersChooser.setUserData(++i);
		newAlliancesChooser.setUserData(++i);
		newPlanetsChooser.setUserData(++i);
		newMoonsChooser.setUserData(++i);
		deletedPlayersChooser.setUserData(++i);
		deletedAlliancesChooser.setUserData(++i);
		deletedPlanetsChooser.setUserData(++i);
		deletedMoonsChooser.setUserData(++i);
		allianceTagChangesChooser.setUserData(++i);
		allianceHomepageChangesChooser.setUserData(++i);
		allianceLogoChangesChooser.setUserData(++i);
		allianceApplicationChangesChooser.setUserData(++i);
		pilloryChooser.setUserData(++i);

		inactivesChooser.requestFocus();
	}

	public void setLogBeginDate(int minusDays) {
		fromDateInput.setValue(LocalDate.now().minusDays(minusDays));
	}

	public void setDetailViewController(
			DetailViewController detailViewController) {
		m_detailViewController = detailViewController;
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
