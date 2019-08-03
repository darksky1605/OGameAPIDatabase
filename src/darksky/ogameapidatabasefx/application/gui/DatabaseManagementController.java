package darksky.ogameapidatabasefx.application.gui;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.stream.Collectors;

import darksky.ogameapidatabasefx.database.databasemanagement.DatabaseSettings;
import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class DatabaseManagementController implements BasicController {

	private ExecutorService m_executor = null;

	private ObservableList<String> m_domainServerListData = FXCollections.observableArrayList();

	private ObservableList<String> m_existingServerListData = null;

	private MainWindowController m_parentController = null;

	private DatabaseUpdateMasterTask m_updateTask = null;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ListView<String> domainServerList;

	@FXML
	private ListView<String> existingServerList;

	@FXML
	private Button showDomainServerListButton;

	@FXML
	private Button createSelectedDatabasesButton;

	@FXML
	private Button updateSelectedServersButton;

	@FXML
	private Button cancelUpdateButton;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private Label progressMessageLabel;

	@FXML
	private Button deleteSelectedDatabasesButton;
	
    @FXML
    private CheckBox saveActivityCheckbox;

    @FXML
    private CheckBox savePlanetDistributionCheckbox;

    @FXML
    private CheckBox saveHighscoreDistributionCheckbox;

    @FXML
    private TextField highscoreEntriesTextfield;

    @FXML
    private Button applyDatabaseSettingsButton;

	@FXML
	void cancelUpdateButtonAction(ActionEvent event) {
		m_updateTask.cancel(true);
	}	

	@FXML
	void createSelectedDatabasesButtonAction(ActionEvent event) {
		createSelectedDatabasesButton.setDisable(true);
		ObservableList<String> selectedServerPrefixes = domainServerList.getSelectionModel().getSelectedItems();

		m_executor.submit(() -> {
			List<String> successPrefixes = new ArrayList<String>();
			List<String> failedPrefixes = new ArrayList<String>();

			boolean saveActivityStates = "true".equals(Settings.getIni().get("databasecreationdefaultsettings", "saveActivity"));
			boolean savePlanetDistribution = "true".equals(Settings.getIni().get("databasecreationdefaultsettings", "savePlanetDistribution"));
			boolean saveHighscoreDistribution = "true".equals(Settings.getIni().get("databasecreationdefaultsettings", "saveHighscoreDistribution"));
			int maxHighscoreEntriesPerEntity = 1;
			try{
				maxHighscoreEntriesPerEntity = Integer.parseInt(Settings.getIni().get("databasecreationdefaultsettings", "maxHighscoreEntriesPerId"));
			}catch(Exception e){}
			
			DatabaseSettings settings = new DatabaseSettings(saveActivityStates, savePlanetDistribution, saveHighscoreDistribution, maxHighscoreEntriesPerEntity);
			selectedServerPrefixes.forEach(prefix -> {
				OGameAPIDatabase controller = m_parentController.getDatabaseController(prefix);
				try {
					//if (!m_existingServerListData.contains(prefix)) {
						controller.createDatabase(settings);
						successPrefixes.add(prefix);
					//}
				} catch (SQLException e) {
					Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
					failedPrefixes.add(prefix);
				}
			});

			Platform.runLater(() -> {
				successPrefixes.forEach(prefix -> m_existingServerListData.add(prefix));
				if (failedPrefixes.size() > 0) {
					failedPrefixes.forEach(prefix -> domainServerList.getSelectionModel().select(prefix));
					Util.displayErrorMessage(resources.getString("error_createdatabase"));
				}
				createSelectedDatabasesButton.setDisable(false);
			});
		});
	}

	@FXML
	void deletedSelectedDatabases(ActionEvent event) {
		List<String> selectedDatabases = existingServerList.getSelectionModel().getSelectedItems();
		List<String> successList = new ArrayList<>();

		Optional<ButtonType> result = Util.displayConfirmationDialog(resources.getString("deleteselecteddatabases"),
				resources.getString("deleteselecteddatabases") + " ?",
				selectedDatabases.stream().collect(Collectors.joining(", ")));
		if (result.get() == ButtonType.OK) {
			for (String s : selectedDatabases) {
				OGameAPIDatabase c = m_parentController.getDatabaseController(s);
				if (c != null) {
					try {
						c.closeDatabaseConnection();
						c.deleteDatabase();
						successList.add(s);
					} catch (Exception e) {
						Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
						Util.displayErrorMessage(resources.getString("error_deletedatabase") + " " + s);
					}
				}
			}
			for (String s : successList) {
				m_existingServerListData.remove(s);
			}
			existingServerList.getSelectionModel().clearSelection();
		}
	}

	@FXML
	void showDomainServerListButtonAction(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog(Settings.getIni().get("global", "defaultserver"));
		dialog.setTitle("");
		dialog.setHeaderText("");
		dialog.setContentText(resources.getString("enterexistingserver") + ":");
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(serverprefix -> {
			try {
				List<String> domainList = OGameAPIDatabase.getServerPrefixList(serverprefix);
				m_domainServerListData.setAll(domainList);
			} catch (Exception e) {
				Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
				Util.displayErrorMessage(resources.getString("error_loadserverlist"));
			}
		});
	}

	@FXML
	void updateSelectedServersButton(ActionEvent event) {
		
		if(m_updateTask != null && !m_updateTask.isDone())
			return;

		List<OGameAPIDatabase> selectedDatabases = existingServerList.getSelectionModel().getSelectedItems().stream()
				.map(s -> m_parentController.getDatabaseController(s)).collect(Collectors.toList());

		if (selectedDatabases.size() == 0) {
			return;
		}

		final int inithreads = Settings.getIni().get("databaseupdate", "workerthreads", int.class);
		final int iniminimumWorkPerThread = Settings.getIni().get("databaseupdate", "minimumwork", int.class);

		m_updateTask = new DatabaseUpdateMasterTask(inithreads, iniminimumWorkPerThread, m_executor,
				selectedDatabases, resources);
		progressBar.progressProperty().bind(m_updateTask.progressProperty());
		progressMessageLabel.textProperty().bind(m_updateTask.messageProperty());
		
		updateSelectedServersButton.disableProperty().bind(m_updateTask.runningProperty());
		cancelUpdateButton.disableProperty().bind(m_updateTask.runningProperty().not());
		
		
		m_updateTask.setOnSucceeded(event1 -> {
			existingServerList.getSelectionModel().clearSelection();

			List<OGameAPIDatabase> returnList = m_updateTask.getValue();
			if (returnList.size() > 0) {
				returnList.forEach(c -> existingServerList.getSelectionModel().select(c.getServerPrefix()));
				Util.displayInformation(resources.getString("error_updatedatabase"));
			}

		});

		m_updateTask.setOnFailed((event2) -> {
			Throwable th = m_updateTask.getException();
			if (th != null) {
				Util.getLogger().log(Level.SEVERE, "update task failed", th);
				Util.displayErrorMessage(th.getMessage());
			}
		});
		
		m_executor.submit(m_updateTask);

	}
	
	@FXML
	void applyDatabaseSettingsButtonAction(ActionEvent event){
		List<OGameAPIDatabase> selectedDatabases = existingServerList.getSelectionModel().getSelectedItems().stream()
				.map(s -> m_parentController.getDatabaseController(s)).collect(Collectors.toList());
		
		boolean saveActivityStates = saveActivityCheckbox.isSelected();
		boolean savePlanetDistribution = savePlanetDistributionCheckbox.isSelected();
		boolean saveHighscoreDistribution = saveHighscoreDistributionCheckbox.isSelected();
		int maxHighscoreEntriesPerEntity = 100;
		try{
			maxHighscoreEntriesPerEntity = Integer.parseInt(highscoreEntriesTextfield.getText());
		}catch(Exception e){}
		
		DatabaseSettings settings = new DatabaseSettings(saveActivityStates, savePlanetDistribution, saveHighscoreDistribution, maxHighscoreEntriesPerEntity);
		
		List<String> failedPrefixes = new ArrayList<String>();
		for(OGameAPIDatabase odb : selectedDatabases){
			try {
				odb.applyDatabaseSettings(settings);
			} catch (SQLException e) {
				failedPrefixes.add(odb.getServerPrefix());
				Util.getLogger().log(Level.SEVERE, "could not apply database settings to " + odb.getServerPrefix(), e);
			}
		}
		if(!failedPrefixes.isEmpty()){
			Util.displayErrorMessage("Could not apply database settings for " + failedPrefixes.stream().collect(Collectors.joining(", ")));
		}
	}

	@FXML
	private void initialize() {
		assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'DataManagement.fxml'.";
		assert progressMessageLabel != null : "fx:id=\"progressMessageLabel\" was not injected: check your FXML file 'DataManagement.fxml'.";
		assert domainServerList != null : "fx:id=\"domainServerList\" was not injected: check your FXML file 'DataManagement.fxml'.";
		assert existingServerList != null : "fx:id=\"existingServerList\" was not injected: check your FXML file 'DataManagement.fxml'.";
		assert showDomainServerListButton != null : "fx:id=\"showDomainServerListButton\" was not injected: check your FXML file 'DataManagement.fxml'.";
		assert createSelectedDatabasesButton != null : "fx:id=\"createSelectedDatabasesButton\" was not injected: check your FXML file 'DataManagement.fxml'.";
		assert updateSelectedServersButton != null : "fx:id=\"updateSelectedServersButton\" was not injected: check your FXML file 'DataManagement.fxml'.";
		assert cancelUpdateButton != null : "fx:id=\"cancelUpdateButton\" was not injected: check your FXML file 'DataManagement.fxml'.";

		Util.getLogger().config("DataManagementController init");

		domainServerList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		domainServerList.setItems(m_domainServerListData);

		existingServerList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	public void setDomainServerListData(List<String> domainServerListData) {
		Objects.requireNonNull(domainServerListData);
		m_domainServerListData.setAll(domainServerListData);
	}

	public void setExistingServerListData(ObservableList<String> existingServerListData) {
		Objects.requireNonNull(existingServerListData);

		m_existingServerListData = existingServerListData;
		existingServerList.setItems(existingServerListData);
	}

	@Override
	public void setCurrentDatabaseController(OGameAPIDatabase currentDatabaseController) {
	}

	@Override
	public void setParentController(BasicController controller) {
		m_parentController = Objects.requireNonNull((MainWindowController) controller);
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		m_executor = Objects.requireNonNull(executorService);
	}
}
