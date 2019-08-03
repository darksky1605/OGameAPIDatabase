package darksky.ogameapidatabasefx.application.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

public class DatabaseUpdateMasterTask extends Task<List<OGameAPIDatabase>> {

	private final int m_threads;
	private final int m_minimumDatabasesPerThread;
	private final ExecutorService m_executor;
	private final List<OGameAPIDatabase> m_updateList;
	private final ResourceBundle m_resources;

	public DatabaseUpdateMasterTask(int threads, int minimumDatabasesPerThread, ExecutorService executor,
			List<OGameAPIDatabase> updateList, ResourceBundle resources) {
		m_threads = Math.max(1, threads);
		m_minimumDatabasesPerThread = Math.max(1, minimumDatabasesPerThread);
		m_executor = Objects.requireNonNull(executor);
		m_updateList = Objects.requireNonNull(updateList);
		m_resources = Objects.requireNonNull(resources);
	}

	@Override
	protected List<OGameAPIDatabase> call() throws Exception {
		if (m_updateList.size() == 0)
			return Collections.emptyList();

		this.updateProgress(0.0, 1.0);

		List<OGameAPIDatabase> resultList = new ArrayList<>();

		int chunksize = (m_updateList.size() + m_threads - 1) / m_threads;
		chunksize = Math.max(chunksize, m_minimumDatabasesPerThread);

		List<List<OGameAPIDatabase>> chunks = new ArrayList<List<OGameAPIDatabase>>();
		int index = 0;
		while (index < m_updateList.size()) {
			int count = m_updateList.size() - index > chunksize ? chunksize : m_updateList.size() - index;
			chunks.add(m_updateList.subList(index, index + count));
			index += chunksize;
		}

		NumberBinding progressBinding = new SimpleDoubleProperty(0.0).add(0.0);
		StringExpression progressTextExpression = new SimpleStringProperty("").concat("");
		SimpleStringProperty space = new SimpleStringProperty(" ");

		List<DatabaseUpdateSlaveTask> updateTasks = new ArrayList<>();
		for (List<OGameAPIDatabase> chunk : chunks) {
			DatabaseUpdateSlaveTask task = new DatabaseUpdateSlaveTask(chunk, m_resources);

			progressBinding = progressBinding.add(task.progressProperty());
			progressTextExpression = progressTextExpression.concat(task.messageProperty().concat(space));

			updateTasks.add(task);
		}

		progressBinding = progressBinding.divide(chunks.size());
		progressBinding.addListener((observable, oldValue, newValue) -> {
			updateProgress((double) newValue, 1.0);
		});
		progressTextExpression.addListener((observable, oldValue, newValue) -> {
			updateMessage(newValue);
		});

		if (!isCancelled()) {
			for (DatabaseUpdateSlaveTask task : updateTasks) {
				m_executor.execute(task);
			}
		}

		for (DatabaseUpdateSlaveTask task : updateTasks) {
			try {
				List<OGameAPIDatabase> result = task.get();
				resultList.addAll(result);
			} catch (CancellationException ce) {
				List<OGameAPIDatabase> result = task.getValue();
				resultList.addAll(result);
			} catch (InterruptedException ie) {
				for (DatabaseUpdateSlaveTask task2 : updateTasks) {
					task2.cancel();
				}
				break;
			} catch (ExecutionException ee) {
				setException(ee);
			}
		}

		if (isCancelled()) {
			resultList.clear();
			for (DatabaseUpdateSlaveTask task : updateTasks) {
				List<OGameAPIDatabase> result = task.get();
				resultList.addAll(result);
			}
		}

		updateProgress(1.0, 1.0);
		this.updateMessage(isCancelled() ? m_resources.getString("canceled") : m_resources.getString("done"));

		return resultList;
	}

}
