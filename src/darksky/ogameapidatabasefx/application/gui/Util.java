package darksky.ogameapidatabasefx.application.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

public class Util {

	private static Logger logger = null;

	public static void displayErrorMessage(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Fehler");
		alert.setHeaderText("Fehler");
		alert.setContentText(message);

		alert.showAndWait();
	}

	public static void displayInformation(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Hinweis");
		alert.setHeaderText("Hinweis");
		alert.setContentText(message);

		alert.showAndWait();
	}

	public static Optional<ButtonType> displayConfirmationDialog(String title,
			String header, String content) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		Optional<ButtonType> result = alert.showAndWait();
		return result;
	}

	public static File getDirectoryFromUser(String title, Window w) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle(title);
		dirChooser.setInitialDirectory(new File("."));
		File saveFile = dirChooser.showDialog(w);
		return saveFile;
	}

	public static Logger getLogger() {
		if (logger == null) {
			logger = Logger.getLogger("darksky.ogamedatabase.application");
			logger.setLevel(Level.ALL);
			logger.setUseParentHandlers(false);

			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.FINEST);
			logger.addHandler(consoleHandler);
			
			try {
				final String errorFolder = "errorlog";
				Files.createDirectories(Paths.get(errorFolder));
				Handler fileHandler = new FileHandler(errorFolder+File.separatorChar+"errorlog.log", 50000, 2,
						true);
				fileHandler.setFormatter(new SimpleFormatter());
				fileHandler.setLevel(Level.SEVERE);
				logger.addHandler(fileHandler);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return logger;
	}
}
