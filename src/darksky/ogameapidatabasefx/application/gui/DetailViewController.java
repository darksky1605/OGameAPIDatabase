package darksky.ogameapidatabasefx.application.gui;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.Alliance;
import darksky.ogameapidatabasefx.database.entities.Player;
import darksky.ogameapidatabasefx.application.main.GUIApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class DetailViewController implements BasicController {

	private MainWindowController m_parentController = null;

	private ExecutorService m_executor = null;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TabPane informationTabPane;

	@FXML
	private void initialize() {
		assert informationTabPane != null : "fx:id=\"informationTabPane\" was not injected: check your FXML file 'DetailView.fxml'.";

		Util.getLogger().config("DetailViewController init");
	}

	public void displayPlayer(Player player, String serverPrefix) {
		Objects.requireNonNull(player);
		Objects.requireNonNull(serverPrefix);

		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setResources(resources);

		Parent playerDetailViewNode = null;
		try {
			playerDetailViewNode = fxmlLoader.load(getClass().getResource(
					GUIApplication.fxmlResourcePath + "PlayerDetailView.fxml")
					.openStream());
		} catch (IOException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
			Util.displayErrorMessage(resources.getString("error_playertab"));
			return;
		}

		PlayerDetailViewController controller = fxmlLoader.getController();
		controller.setParentController(this);
		controller.setExecutorService(m_executor);
		controller.setData(player,
				m_parentController.getDatabaseController(serverPrefix));
		Tab newTab = new Tab("P " + serverPrefix + " - " + player.getName(),
				playerDetailViewNode);
		newTab.setUserData(controller);

		informationTabPane.getTabs().add(newTab);
	}

	public void displayAlliance(Alliance alliance, String serverPrefix) {
		Objects.requireNonNull(alliance);
		Objects.requireNonNull(serverPrefix);

		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setResources(resources);

		Parent allianceDetailViewNode = null;
		try {
			allianceDetailViewNode = fxmlLoader.load(getClass()
					.getResource(
							GUIApplication.fxmlResourcePath
									+ "AllianceDetailView.fxml").openStream());
		} catch (IOException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
			Util.displayErrorMessage(resources.getString("error_alliancetab"));
			return;
		}

		AllianceDetailViewController controller = fxmlLoader.getController();
		controller.setParentController(this);
		controller.setExecutorService(m_executor);
		controller.setData(alliance,
				m_parentController.getDatabaseController(serverPrefix));
		Tab newTab = new Tab("A " + serverPrefix + " - " + alliance.getName(),
				allianceDetailViewNode);
		newTab.setUserData(controller);

		informationTabPane.getTabs().add(newTab);
	}

	@Override
	public void setParentController(BasicController parentController) {
		m_parentController = Objects
				.requireNonNull((MainWindowController) parentController);

	}

	@Override
	public void setCurrentDatabaseController(OGameAPIDatabase databaseController) {
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		m_executor = Objects.requireNonNull(executorService);
		informationTabPane.getTabs().forEach(
				tab -> ((BasicController) tab.getUserData())
						.setExecutorService(m_executor));
	}
}
