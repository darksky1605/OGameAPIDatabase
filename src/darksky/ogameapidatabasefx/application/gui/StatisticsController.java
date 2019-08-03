package darksky.ogameapidatabasefx.application.gui;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.stream.Collectors;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.statistics.ServerActivityState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StatisticsController implements BasicController {

	private ExecutorService m_executor = null;

	private final ObservableList<Series<String, Integer>> m_lineChartModel = FXCollections
			.observableArrayList();
	private final List<String> m_lineChartDataRowNames = new ArrayList<>();

	private OGameAPIDatabase m_currentDatabaseController = null;
	private ObservableList<String> m_existingServerPrefixesList = FXCollections
			.observableArrayList();
	private MainWindowController m_parentController = null;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private LineChart<String, Integer> lineChart;

	@FXML
	private DatePicker fromDatePicker;

	@FXML
	private DatePicker toDatePicker;

	@FXML
	private Button showHistoryButton;

	@FXML
	private Button showPMDistributionButton;

	@FXML
	private ListView<String> existingServersList;

	@FXML
	private Button savePMDistributionImagesButton;

	@FXML
	private ProgressBar savePMDistributionImagesProgressBar;

	@FXML
	private Button saveStatisticsReportsButton;

	@FXML
	private ProgressBar saveStatisticsReportsProgressBar;

	@FXML
	void savePMDistributionImages(ActionEvent event) {
		final LocalDate dateFrom = fromDatePicker.getValue();
		final LocalDate dateTo = toDatePicker.getValue().plusDays(1);
		File saveFile = Util.getDirectoryFromUser("Select save directory",
				savePMDistributionImagesButton.getScene().getWindow());
		if (saveFile != null) {
			final List<String> selectedServersList = existingServersList
					.getSelectionModel().getSelectedItems();
			List<OGameAPIDatabase> clist = selectedServersList.stream()
					.map(s -> m_parentController.getDatabaseController(s))
					.collect(Collectors.toList());
			PMDistributionTask task = new PMDistributionTask(clist, dateFrom,
					dateTo, saveFile);
			task.setOnFailed(eh -> {
				Util.getLogger().log(Level.SEVERE, this.getClass().getName(),
						task.getException());
				Util.displayErrorMessage(resources
						.getString("error_savedistributions"));
			});

			savePMDistributionImagesProgressBar.progressProperty().bind(
					task.progressProperty());
			savePMDistributionImagesProgressBar.visibleProperty().bind(
					task.runningProperty());
			savePMDistributionImagesButton.disableProperty().bind(
					task.runningProperty());

			m_executor.submit(task);
		}
	}

	@FXML
	void saveStatisticsReportsAction(ActionEvent event) {
		final LocalDate dateFrom = fromDatePicker.getValue();
		final LocalDate dateTo = toDatePicker.getValue().plusDays(1);
		if (dateFrom.isAfter(dateTo)) {
			return;
		}

		File saveFile = Util.getDirectoryFromUser("Select report directory",
				saveStatisticsReportsProgressBar.getScene().getWindow());

		if (saveFile != null) {

			final Path savePath = Paths.get(saveFile.getAbsolutePath()
					+ File.separatorChar);
			final ObservableList<String> selectedServerPrefixes = existingServersList
					.getSelectionModel().getSelectedItems();

			final List<OGameAPIDatabase> controllers = selectedServerPrefixes
					.stream().map(s -> {
						return m_parentController.getDatabaseController(s);
					}).filter(c -> c != null).collect(Collectors.toList());

			Task<List<String>> reportTask = new StatisticsReportCreationTask(
					controllers, dateFrom, dateTo, savePath, resources);

			reportTask.setOnFailed(eh -> {
				Util.getLogger().log(Level.SEVERE, this.getClass().getName(),
						reportTask.getException());
				Util.displayErrorMessage(resources
						.getString("error_savereports"));
			});

			saveStatisticsReportsButton.disableProperty().bind(
					reportTask.runningProperty());
			saveStatisticsReportsProgressBar.visibleProperty().bind(
					reportTask.runningProperty());

			saveStatisticsReportsProgressBar.progressProperty().bind(
					reportTask.progressProperty());

			m_executor.submit(reportTask);
		}
	}

	@FXML
	void showHistoryButtonAction(ActionEvent event) {
		LocalDate to = toDatePicker.getValue();
		LocalDate from = fromDatePicker.getValue();

		// check if start date is before end date
		if (from.isAfter(to)) {
			return;
		}

		LocalDateTime dtTo = to.plusDays(1).atStartOfDay();
		LocalDateTime dtFrom = from.atStartOfDay();

		try {
			this.updateLineChartModel(dtFrom, dtTo);
		} catch (SQLException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
			Util.displayErrorMessage(resources.getString("error_loadhistory"));
		}
	}

	@FXML
	void showPMDistributionWindow(ActionEvent event) {

		OGameAPIDatabase c = m_currentDatabaseController;

		PMDistributionTask task = new PMDistributionTask(
				Collections.singletonList(c), null, LocalDate.MAX, null);
		task.setOnFailed(eh -> {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(),
					task.getException());
			Util.displayErrorMessage("Error.");
		});

		task.setOnSucceeded(eh -> {
			List<Image> list = task.getValue().getOrDefault(c, null);
			if (list != null && list.size() > 0) {
				Image image = list.get(0);

				Canvas distributionCanvas = new Canvas(image.getWidth(), image
						.getHeight());
				distributionCanvas.getGraphicsContext2D()
						.drawImage(image, 0, 0);

				VBox vBox = new VBox();
				vBox.getChildren().add(distributionCanvas);

				Stage stage = new Stage();
				stage.setTitle(resources.getString("currentdistributiontitle")
						+ " " + m_currentDatabaseController.getServerPrefix());
				stage.setScene(new Scene(vBox, distributionCanvas.getWidth(),
						distributionCanvas.getHeight()));
				stage.show();

			}
		});

		showPMDistributionButton.disableProperty().bind(task.runningProperty());

		m_executor.submit(task);
	}

	@FXML
	void initialize() {
		assert lineChart != null : "fx:id=\"lineChart\" was not injected: check your FXML file 'StatisticsView.fxml'.";
		assert fromDatePicker != null : "fx:id=\"fromDatePicker\" was not injected: check your FXML file 'StatisticsView.fxml'.";
		assert toDatePicker != null : "fx:id=\"toDatePicker\" was not injected: check your FXML file 'StatisticsView.fxml'.";
		assert showHistoryButton != null : "fx:id=\"showHistoryButton\" was not injected: check your FXML file 'StatisticsView.fxml'.";
		assert showPMDistributionButton != null : "fx:id=\"showPMDistributionButton\" was not injected: check your FXML file 'StatisticsView.fxml'.";
		assert existingServersList != null : "fx:id=\"existingServersList\" was not injected: check your FXML file 'StatisticsView.fxml'.";
		assert savePMDistributionImagesButton != null : "fx:id=\"savePMDistributionImagesButton\" was not injected: check your FXML file 'StatisticsView.fxml'.";
		assert savePMDistributionImagesProgressBar != null : "fx:id=\"savePMDistributionImagesProgressBar\" was not injected: check your FXML file 'StatisticsView.fxml'.";
		assert saveStatisticsReportsButton != null : "fx:id=\"saveStatisticsReportsButton\" was not injected: check your FXML file 'StatisticsView.fxml'.";
		assert saveStatisticsReportsProgressBar != null : "fx:id=\"saveStatisticsReportsProgressBar\" was not injected: check your FXML file 'StatisticsView.fxml'.";

		Util.getLogger().config("OverviewHistoryController init");

		m_lineChartDataRowNames.add(resources.getString("active"));
		m_lineChartDataRowNames.add(resources.getString("inactive"));
		m_lineChartDataRowNames.add(">28 " + resources.getString("inactive"));
		m_lineChartDataRowNames.add(">35 " + resources.getString("inactive"));
		m_lineChartDataRowNames.add(resources.getString("vacationmode"));
		m_lineChartDataRowNames.add(resources.getString("total"));

		LocalDate dateNow = LocalDate.now();
		toDatePicker.setValue(dateNow);
		fromDatePicker.setValue(dateNow.minusDays(10));

		lineChart.setData(m_lineChartModel);
		lineChart.setAnimated(false);

		existingServersList.setItems(m_existingServerPrefixesList);
		existingServersList.getSelectionModel().setSelectionMode(
				SelectionMode.MULTIPLE);

	}

	private void updateLineChartModel(LocalDateTime datetimeFrom,
			LocalDateTime datetimeTo) throws SQLException {
		Objects.requireNonNull(datetimeTo);
		Objects.requireNonNull(datetimeFrom);

		m_lineChartModel.clear();
		for (int i = 0; i < m_lineChartDataRowNames.size(); i++) {
			Series<String, Integer> series = new Series<String, Integer>();
			series.setName(m_lineChartDataRowNames.get(i));
			m_lineChartModel.add(series);
		}

		List<ServerActivityState> stateList = m_currentDatabaseController
				.getStatisticsReader().getServerActivityStates(
						datetimeFrom.toLocalDate(), datetimeTo.toLocalDate());

		for (ServerActivityState sas : stateList) {
			String currentdatetime = sas.getDateTime().toLocalDate().toString();
			m_lineChartModel
					.get(0)
					.getData()
					.add(new XYChart.Data<String, Integer>(currentdatetime, sas
							.getNumberOfActivePlayers()));
			m_lineChartModel
					.get(1)
					.getData()
					.add(new XYChart.Data<String, Integer>(currentdatetime, sas
							.getNumberOfInactivePlayers()));
			m_lineChartModel
					.get(2)
					.getData()
					.add(new XYChart.Data<String, Integer>(currentdatetime, sas
							.getNumberOfLongtimeInactivePlayers()));
			m_lineChartModel
					.get(3)
					.getData()
					.add(new XYChart.Data<String, Integer>(currentdatetime, sas
							.getNumberOfNotDeletablePlayers()));
			m_lineChartModel
					.get(4)
					.getData()
					.add(new XYChart.Data<String, Integer>(currentdatetime, sas
							.getNumberOfVModePlayers()));
			m_lineChartModel
					.get(5)
					.getData()
					.add(new XYChart.Data<String, Integer>(currentdatetime, sas
							.getNumberOfPlayers()));

		}

		for (Series<String, Integer> series : lineChart.getData()) {
			for (XYChart.Data<String, Integer> item : series.getData()) {
				Tooltip.install(item.getNode(),
						new Tooltip("" + item.getYValue()));
				item.getNode().setOnMouseEntered(event -> {
					item.getNode().getStyleClass().add("onHover");
				});

				item.getNode().setOnMouseExited(event -> {
					item.getNode().getStyleClass().remove("onHover");
				});
			}
		}

	}

	public void setDomainServerPrefixesList(
			List<String> domainServerPrefixesList) {
		// m_domainServerPrefixesList = Objects
		// .requireNonNull(domainServerPrefixesList);
	}

	public void setExistingServerListData(
			List<String> existingServerPrefixesList) {
		m_existingServerPrefixesList.setAll(Objects
				.requireNonNull(existingServerPrefixesList));
	}

	public void setCurrentDatabaseController(
			OGameAPIDatabase currentDatabaseController) {
		Objects.requireNonNull(currentDatabaseController);
		m_currentDatabaseController = currentDatabaseController;
		m_lineChartModel.clear();
	}

	@Override
	public void setParentController(BasicController parentController) {
		m_parentController = Objects
				.requireNonNull((MainWindowController) parentController);
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		m_executor = Objects.requireNonNull(executorService);
	}
}
