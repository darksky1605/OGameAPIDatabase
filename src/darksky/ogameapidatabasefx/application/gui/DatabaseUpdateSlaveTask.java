package darksky.ogameapidatabasefx.application.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javafx.concurrent.Task;
import darksky.ogameapidatabasefx.database.databasemanagement.DatabaseUpdater;
import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;

public class DatabaseUpdateSlaveTask extends Task<List<OGameAPIDatabase>> {

	private List<OGameAPIDatabase> m_updateList = null;
	private ResourceBundle m_resources = null;

	private String[] m_updateStrings;

	public DatabaseUpdateSlaveTask(List<OGameAPIDatabase> updateList, ResourceBundle resources) {
		super();
		m_updateList = Objects.requireNonNull(updateList);
		m_resources = Objects.requireNonNull(resources);
		m_updateStrings = new String[] { "", m_resources.getString("players"), m_resources.getString("alliances"),
				m_resources.getString("galaxy"), m_resources.getString("highscore") };
	}

	@Override
	protected List<OGameAPIDatabase> call() throws Exception {
		final String threadName = Thread.currentThread().getName();
		Util.getLogger().info(threadName + " - update task started. databases : " + m_updateList.size());
		List<String> domainList = Collections.emptyList();
		List<OGameAPIDatabase> controllersToUpdate = new ArrayList<OGameAPIDatabase>();

		// check if update of server is possible
		String currentDomain = "";
		for (OGameAPIDatabase c : m_updateList) {
			String prefix = c.getServerPrefix();
			String dom = prefix.split("-")[1];
			if (!currentDomain.equals(dom)) {
				try {
					domainList = OGameAPIDatabase.getServerPrefixList(prefix);
					currentDomain = dom;
				} catch (Exception e) {
					Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
					continue;
				}
			}
			if (domainList.contains(prefix)) {
				controllersToUpdate.add(c);
			}
		}

		List<OGameAPIDatabase> returnList = new ArrayList<OGameAPIDatabase>();
		int[] progress = { 0 };
		// 6, but first progress is the same as last progress of previous server
		int maxProgressPerServer = 5;
		int maxProgress = maxProgressPerServer * controllersToUpdate.size();

		this.updateProgress(0, maxProgress);
		for (OGameAPIDatabase controller : controllersToUpdate) {
			if (!isCancelled() && controller != null) {

				String prefix = controller.getServerPrefix();
				this.updateMessage(prefix);

				try {
					DatabaseUpdater updater = controller.getDatabaseUpdater();

// 					if (!updater.isRequiredDatabaseVersion()) {
// 						Util.getLogger().log(Level.WARNING, "error updating " + prefix + ". wrong database version");
// 						returnList.add(controller);
// 						continue;
// 					}
					

						updater.addUpdateListener(i -> this.updateProgress(progress[0]++, maxProgress));
						try {
							updater.updateDatabase();
						} catch (Exception e) {
							Util.getLogger().log(Level.SEVERE, "", e);
							System.out.println("error updating " + controller.getServerPrefix());
						}

					

//					String whatIsBeingUpdated = "";
//					int i = 0;
//
//					whatIsBeingUpdated = m_updateStrings[i++];
//					this.updateProgress(progress[0]++, maxProgress);
//					Util.getLogger().fine(threadName + " " + prefix + " : " + whatIsBeingUpdated
//							+ " - progress : " + (progress[0] - 1) + " / " + maxProgress);
//
//					updater.updateServerData();
//
//					if (isCancelled())
//						continue;
//
//					whatIsBeingUpdated = m_updateStrings[i++];
//					this.updateProgress(progress[0]++, maxProgress);
//					Util.getLogger().fine(threadName + " " + prefix + " : " + whatIsBeingUpdated
//							+ " - progress : " + (progress[0] - 1) + " / " + maxProgress);
//
//					updater.updatePlayers();
//
//					if (isCancelled())
//						continue;
//
//					whatIsBeingUpdated = m_updateStrings[i++];
//					this.updateProgress(progress[0]++, maxProgress);
//					Util.getLogger().fine(threadName + " " + prefix + " : " + whatIsBeingUpdated
//							+ " - progress : " + (progress[0] - 1) + " / " + maxProgress);
//
//					updater.updateAlliances();
//
//					if (isCancelled())
//						continue;
//
//					whatIsBeingUpdated = m_updateStrings[i++];
//					this.updateProgress(progress[0]++, maxProgress);
//					Util.getLogger().fine(threadName + " " + prefix + " : " + whatIsBeingUpdated
//							+ " - progress : " + (progress[0] - 1) + " / " + maxProgress);
//
//					updater.updateUniverse();
//
//					if (isCancelled())
//						continue;
//
//					whatIsBeingUpdated = m_updateStrings[i++];
//					this.updateProgress(progress[0]++, maxProgress);
//					Util.getLogger().fine(threadName + " " + prefix + " : " + whatIsBeingUpdated
//							+ " - progress : " + (progress[0] - 1) + " / " + maxProgress);
//
//					for (int category = 1; category < 3; category++) {
//						for (int type = 0; type < 8 && !isCancelled(); type++) {
//							updater.updateHighscore(category, type);
//						}
//					}

				} catch (Exception e) {

					Util.getLogger().log(Level.SEVERE, "error updating " + prefix, e);
					returnList.add(controller);

				}

			} else {
				returnList.add(controller);
			}

		}
		this.updateProgress(maxProgress, maxProgress);

		this.updateMessage("");

		Util.getLogger().info(Thread.currentThread().getName() + " - database update task stopped.");

		return returnList;
	}

}
