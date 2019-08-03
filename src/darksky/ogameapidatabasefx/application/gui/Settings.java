package darksky.ogameapidatabasefx.application.gui;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.ini4j.Ini;

public class Settings {

	private static final String iniFileName = "settings.ini";

	private static Ini ini;

	private static final String[] availableLanguages = { "en", "de" };

	static {

		File iniFile = new File(iniFileName);

		if (iniFile.exists() && iniFile.isFile()) {
			try {
				ini = new Ini(iniFile);
			} catch (IOException e) {
				Util.getLogger().log(Level.WARNING, "settings.ini could not be loaded. using temporary default settings", e);
				ini = createDefaultIni();
			}
		} else {
			ini = createDefaultIni();
			try {
				iniFile.createNewFile();				
				ini.setFile(iniFile);
				ini.store();
			} catch (IOException e1) {
				Util.getLogger().log(Level.WARNING, "settings.ini could not be created", e1);
			}
		}
	}
	
	private static Ini createDefaultIni(){
		Ini ini = new Ini();
		ini.put("global", "language", "en");
		ini.put("global", "defaultserver", "s1-de");
		ini.put("global", "width", "1000");
		ini.put("global", "height", "700");
		ini.put("logs", "daysInPast", "5");
		ini.put("highscore", "entriesPerPage", "50");
		ini.put("databaseupdate", "workerthreads", "2");
		ini.put("databaseupdate", "minimumwork", "8");
		ini.put("databasecreationdefaultsettings", "saveActivity", "true");
		ini.put("databasecreationdefaultsettings", "savePlanetDistribution", "true");
		ini.put("databasecreationdefaultsettings", "saveHighscoreDistribution", "true");
		ini.put("databasecreationdefaultsettings", "maxHighscoreEntriesPerId", "100");
		return ini;
	}

	public static Ini getIni() {
		return ini;
	}

	public static String[] getAvailableLanguages() {
		return availableLanguages;
	}

}
