package darksky.ogameapidatabasefx.application.gui;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Text;

public class SQLViewController implements BasicController {

	private static Logger logger = Util.getLogger();

	private ExecutorService m_executor = null;

	private final ObservableList<ObservableList<String>> m_tableData = FXCollections
			.observableArrayList();

	private OGameAPIDatabase m_currentDatabaseController;

	private List<String> m_existingDatabasesList = null;

	private MainWindowController m_parentController = null;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private SplitPane splitPane;

	@FXML
	private Button submitButton;

	@FXML
	private TextArea queryInputArea;

	@FXML
	private CheckBox includeEveryExistingDatabaseCheckbox;

	@FXML
	private TableView<ObservableList<String>> dataTable;

	@SuppressWarnings("rawtypes")
	@FXML
	private void initialize() {
		assert splitPane != null : "fx:id=\"splitPane\" was not injected: check your FXML file 'SQLView.fxml'.";
		assert submitButton != null : "fx:id=\"submitButton\" was not injected: check your FXML file 'SQLView.fxml'.";
		assert queryInputArea != null : "fx:id=\"queryInputArea\" was not injected: check your FXML file 'SQLView.fxml'.";
		assert includeEveryExistingDatabaseCheckbox != null : "fx:id=\"includeEveryExistingDatabaseCheckbox\" was not injected: check your FXML file 'SQLView.fxml'.";
		assert dataTable != null : "fx:id=\"dataTable\" was not injected: check your FXML file 'SQLView.fxml'.";

		logger.config("SQLViewController init");

		dataTable.setItems(m_tableData);
		dataTable.getSelectionModel().setCellSelectionEnabled(true);
		dataTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		dataTable.setPlaceholder(new Text(""));

		queryInputArea.heightProperty().add(500);

		KeyCodeCombination copyCombination = new KeyCodeCombination(KeyCode.C,
				KeyCombination.CONTROL_ANY);
		// copy selected cells to clip board when pressing ctrl + c
		dataTable
				.setOnKeyPressed(keyEvent -> {
					if (copyCombination.match(keyEvent)) {

						if (keyEvent.getSource() instanceof TableView) {

							StringBuilder clipboardString = new StringBuilder();

							ObservableList<TablePosition> positionList = dataTable
									.getSelectionModel().getSelectedCells();

							int prevRow = -1;

							for (TablePosition position : positionList) {

								int row = position.getRow();
								int col = position.getColumn();

								Object cell = (Object) dataTable.getColumns()
										.get(col).getCellData(row);

								if (cell == null) {
									cell = "";
								}
								if (prevRow == row) {

									clipboardString.append('\t');

								} else if (prevRow != -1) {

									clipboardString.append('\n');

								}

								String text = cell.toString();

								clipboardString.append(text);

								prevRow = row;
							}

							final ClipboardContent clipboardContent = new ClipboardContent();
							clipboardContent.putString(clipboardString
									.toString());

							Clipboard.getSystemClipboard().setContent(
									clipboardContent);

							keyEvent.consume();
						}

					}
				});

	}

	@FXML
	void submitButtonAction(ActionEvent event) {
		m_executor.submit(() -> {
			try {
				submitButton.setDisable(true);
				executeQuery();
			} finally {
				submitButton.setDisable(false);
			}
		});
	}

	private void executeQuery() {
		String sqlstring = queryInputArea.getText();
		if (sqlstring == null)
			return;

		ObservableList<ObservableList<String>> tableData = FXCollections
				.observableArrayList();
		ArrayList<TableColumn<ObservableList<String>, String>> columns = new ArrayList<TableColumn<ObservableList<String>, String>>();

		List<OGameAPIDatabase> controllers = null;
		if (includeEveryExistingDatabaseCheckbox.isSelected()) {
			controllers = m_existingDatabasesList
					.stream()
					.map(prefix -> m_parentController
							.getDatabaseController(prefix))
					.collect(Collectors.toList());
		} else {
			controllers = Collections
					.singletonList(m_currentDatabaseController);
		}

		boolean firstDatabase = true;

		List<String> failed = new ArrayList<>();
		for (OGameAPIDatabase controller : controllers) {

			try (Statement stmt = controller.getDatabaseConnection()
					.createStatement();) {
				boolean result = stmt.execute(sqlstring);
				Util.getLogger().fine("qry executed");
				if (result) {
					try (ResultSet rs = stmt.getResultSet();) {
						if (rs != null) {
							if (firstDatabase) {
								if (controllers.size() > 1) {
									TableColumn<ObservableList<String>, String> servercol = new TableColumn<ObservableList<String>, String>(
											"Server");
									servercol.setCellValueFactory(param -> {
										return new SimpleStringProperty(param
												.getValue().get(0));
									});
									columns.add(servercol);
									firstDatabase = false;
								}
								for (int i = 1; i <= rs.getMetaData()
										.getColumnCount(); i++) {

									final int j = i;
									TableColumn<ObservableList<String>, String> col = new TableColumn<ObservableList<String>, String>(
											rs.getMetaData().getColumnName(i));
									col.setCellValueFactory(param -> {
										return new SimpleStringProperty(param
												.getValue().get(j));
									});
									columns.add(col);
								}
							}

							final String prefix = controller.getServerPrefix();
							while (rs.next()) {
								ObservableList<String> row = FXCollections
										.observableArrayList();
								row.add(prefix);
								for (int i = 1; i <= rs.getMetaData()
										.getColumnCount(); i++) {
									row.add(rs.getString(i));
								}
								tableData.add(row);
							}
						}
					}
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, this.getClass().getName(), e);
				failed.add(controller.getServerPrefix() + " : "
						+ e.getMessage());
			}
		}

		Platform.runLater(() -> {
			dataTable.getColumns().clear();
			dataTable.getColumns().addAll(columns);
			m_tableData.setAll(tableData);
			if (failed.size() > 0) {
				Util.displayErrorMessage(failed.stream().limit(10)
						.collect(Collectors.joining("\n"))
						+ (failed.size() > 10 ? "\n +" + (failed.size() - 10)
								+ " errors" : ""));

			}
		});
	}

	@Override
	public void setCurrentDatabaseController(
			OGameAPIDatabase currentDatabaseController) {
		m_currentDatabaseController = Objects
				.requireNonNull(currentDatabaseController);
	}

	public void setExistingDatabasesList(
			ObservableList<String> existingDatabasesList) {
		m_existingDatabasesList = existingDatabasesList;
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
