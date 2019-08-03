package darksky.ogameapidatabasefx.application.gui;

import java.util.concurrent.ExecutorService;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;

public interface BasicController {

	public void setParentController(BasicController parentController);

	public void setCurrentDatabaseController(OGameAPIDatabase databaseController);

	public void setExecutorService(ExecutorService executorService);
}
