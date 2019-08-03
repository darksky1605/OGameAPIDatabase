package darksky.ogameapidatabasefx.application.gui;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
//TODO delete
public class SettingsViewController implements BasicController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button resetSettingsButton;

	@FXML
	private TableView<Map.Entry<String, String>> settingsTable;

	@FXML
	private TableColumn<Map.Entry<String, String>, String> optionColumn;

	@FXML
	private TableColumn<Map.Entry<String, String>, String> valueColumn;

	@FXML
	void resetSettings(ActionEvent event) {
	}

	@FXML
	void editCommit(TableColumn.CellEditEvent<Map.Entry<String, String>, String> event) {
	}

	@FXML
	void initialize() {
		assert resetSettingsButton != null : "fx:id=\"resetSettingsButton\" was not injected: check your FXML file 'SettingsView.fxml'.";
		assert settingsTable != null : "fx:id=\"settingsTable\" was not injected: check your FXML file 'SettingsView.fxml'.";
		assert optionColumn != null : "fx:id=\"optionColumn\" was not injected: check your FXML file 'SettingsView.fxml'.";
		assert valueColumn != null : "fx:id=\"valueColumn\" was not injected: check your FXML file 'SettingsView.fxml'.";

	}

	@Override
	public void setParentController(BasicController parentController) {
	}

	@Override
	public void setCurrentDatabaseController(OGameAPIDatabase databaseController) {
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
	}
}
