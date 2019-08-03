package darksky.ogameapidatabasefx.application.main;

import java.nio.file.Paths;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.List;

import org.ini4j.Ini;

import darksky.ogameapidatabasefx.application.gui.MainWindowController;
import darksky.ogameapidatabasefx.application.gui.Settings;
import darksky.ogameapidatabasefx.application.gui.Util;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIApplication extends Application {

	public static final String langBundleBasePath = "darksky.ogameapidatabasefx.application.resources.localization";

	public static final String fxmlResourcePath = "/darksky/ogameapidatabasefx/application/resources/fxml/";

	public static final String cssResourcePath = "/darksky/ogameapidatabasefx/application/resources/css/";

	@Override
	public void start(Stage primaryStage) {

		final Parameters params = getParameters();

		try {
			Ini ini = Settings.getIni();

			final double iniheight = ini.get("global", "height", double.class);
			final double iniwidth = ini.get("global", "width", double.class);

			final double height = Math.max(1, iniheight);
			final double width = Math.max(1, iniwidth);

			final FXMLLoader fxmlLoader = new FXMLLoader();
			final String language = ini.get("global", "language");
			final ResourceBundle defaultBundle = ResourceBundle.getBundle(langBundleBasePath + ".lang",
					new Locale("en", Locale.getDefault().getCountry()));
			try {
				final ResourceBundle bundle = ResourceBundle.getBundle(langBundleBasePath + ".lang",
						new Locale(language, Locale.getDefault().getCountry()));
				if (bundle != null)
					fxmlLoader.setResources(bundle);
				else
					fxmlLoader.setResources(defaultBundle);
			} catch (MissingResourceException e) {
				Util.getLogger().info("bundle " + language + " could not be found");
				fxmlLoader.setResources(defaultBundle);
			}

			Parent root = fxmlLoader.load(getClass().getResource(fxmlResourcePath + "MainWindow.fxml").openStream());

			Scene scene = new Scene(root, width, height);
			scene.getStylesheets().add(getClass().getResource(cssResourcePath + "application.css").toExternalForm());
			scene.widthProperty().addListener((observable, oldvalue, newvalue) -> {
				ini.put("global", "width", (double) newvalue);
				try {
					ini.store();
				} catch (Exception e) {
					Util.getLogger().log(Level.WARNING, "new width could not be stored in ini", e);
				}
			});
			scene.heightProperty().addListener((observable, oldvalue, newvalue) -> {
				ini.put("global", "height", (double) newvalue);
				try {
					ini.store();
				} catch (Exception e) {
					Util.getLogger().log(Level.WARNING, "new height could not be stored in ini", e);
				}
			});

			primaryStage.setScene(scene);
			primaryStage.setTitle("OGame API Database (Dark Sky)");
			primaryStage.setMinHeight(440);
			primaryStage.setMinWidth(600);

			ExecutorService executorService = Executors.newCachedThreadPool();

			MainWindowController controller = fxmlLoader.getController();
			controller.setPathToDatabaseFiles(Paths.get(params.getNamed().getOrDefault("location", Main.defaultDatabaseLocation)));
			
			List<String> rawparams = params.getRaw();
			for(int i = 0; i < rawparams.size(); i++){
                if (rawparams.get(i).equals("--location") && i < rawparams.size() - 1) {
                    controller.setPathToDatabaseFiles(Paths.get(rawparams.get(i+1)));
                    break;
                }
			}
			
					
			controller.setExecutorService(executorService);

			primaryStage.setOnCloseRequest((windowEvent) -> {
				executorService.shutdownNow();
			});

			primaryStage.show();

		} catch (Exception e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
			throw new RuntimeException("Could not load main window");
		}
	}
}
