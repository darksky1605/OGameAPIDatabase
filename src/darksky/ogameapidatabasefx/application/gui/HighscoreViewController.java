package darksky.ogameapidatabasefx.application.gui;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.entities.Alliance;
import darksky.ogameapidatabasefx.database.entities.Entity;
import darksky.ogameapidatabasefx.database.entities.Highscore;
import darksky.ogameapidatabasefx.database.entities.IHighscoreEntity;
import darksky.ogameapidatabasefx.database.entities.Player;
import darksky.ogameapidatabasefx.database.entities.Highscore.HighscoreType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

public class HighscoreViewController implements BasicController {

	private int m_highscoreEntriesPerPage = 50;

	private ExecutorService m_executor = null;

	private SimpleObjectProperty<OGameAPIDatabase> m_currentDatabaseControllerProperty = new SimpleObjectProperty<OGameAPIDatabase>(
			null);

	private DetailViewController m_detailViewController = null;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TableView<IHighscoreEntity> highscoreTable;

	@FXML
	private TableColumn<IHighscoreEntity, String> rankColumn;

	@FXML
	private TableColumn<IHighscoreEntity, String> nameColumn;

	@FXML
	private TableColumn<IHighscoreEntity, String> totalColumn;

	@FXML
	private TableColumn<IHighscoreEntity, String> economyColumn;

	@FXML
	private TableColumn<IHighscoreEntity, String> researchColumn;

	@FXML
	private TableColumn<IHighscoreEntity, String> militaryColumn;

	@FXML
	private TableColumn<IHighscoreEntity, String> militaryBuiltColumn;

	@FXML
	private TableColumn<IHighscoreEntity, String> militaryDestroyedColumn;

	@FXML
	private TableColumn<IHighscoreEntity, String> militaryLostColumn;

	@FXML
	private TableColumn<IHighscoreEntity, String> honorColumn;
	
	@FXML
	private TableColumn<IHighscoreEntity, String> shipsColumn;

	@FXML
	private RadioButton playerRadioButton;

	@FXML
	private ToggleGroup group;

	@FXML
	private RadioButton allianceRadioButton;

	@FXML
	private Label sortLabel;

	@FXML
	private ComboBox<HighscoreType> sortCombobox;

	@FXML
	private Button prevPageButton;

	@FXML
	private Button nextPageButton;

	@FXML
	private Button submitButton;

	@FXML
	private TextField pageTextField;

	@FXML
	private Label pageLabel;

	@FXML
	void nextPageButtonAction(ActionEvent e) {
		int newPage = 1;
		if (pageTextField.getText().matches("[0-9]+")) {
			newPage = Integer.parseInt(pageTextField.getText()) + 1;
		}
		pageTextField.setText(String.valueOf(newPage));
		submitButtonAction(null);
	}

	@FXML
	void prevPageButtonAction(ActionEvent e) {
		int newPage = 1;
		if (pageTextField.getText().matches("[0-9]+")) {
			newPage = Integer.parseInt(pageTextField.getText()) - 1;
		}
		pageTextField.setText(String.valueOf(newPage));
		submitButtonAction(null);
	}

	@FXML
	void submitButtonAction(ActionEvent e) {

		String input = pageTextField.getText();
		if (input.matches("\\d+")) {
			int page = Integer.parseInt(pageTextField.getText());
			int highscoreTypeIndex = sortCombobox.getSelectionModel().getSelectedIndex();

			HighscoreType highscoreType = HighscoreType.values()[highscoreTypeIndex];

			GetHighscoreActorsTask task = new GetHighscoreActorsTask(m_currentDatabaseControllerProperty.get(), page,
					m_highscoreEntriesPerPage, highscoreType, playerRadioButton.isSelected(), pageTextField);

			task.setOnFailed(eh -> {
				Util.displayErrorMessage(resources.getString("error_loadhighscore"));
				Util.getLogger().log(Level.SEVERE, this.getClass().getName(), task.getException());
			});

			highscoreTable.itemsProperty().bind(task.valueProperty());

			m_executor.submit(task);
		}
	}

	@FXML
	private void initialize() {
		assert playerRadioButton != null : "fx:id=\"playerRadioButton\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert group != null : "fx:id=\"group\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert allianceRadioButton != null : "fx:id=\"allianceRadioButton\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert sortLabel != null : "fx:id=\"sortLabel\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert sortCombobox != null : "fx:id=\"sortCombobox\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert prevPageButton != null : "fx:id=\"prevPageButton\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert nextPageButton != null : "fx:id=\"nextPageButton\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert pageTextField != null : "fx:id=\"pageTextField\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert pageLabel != null : "fx:id=\"pageLabel\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert submitButton != null : "fx:id=\"submitButton\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert highscoreTable != null : "fx:id=\"highscoreTable\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert rankColumn != null : "fx:id=\"rankColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert nameColumn != null : "fx:id=\"nameColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert totalColumn != null : "fx:id=\"totalColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert economyColumn != null : "fx:id=\"economyColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert researchColumn != null : "fx:id=\"researchColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert militaryColumn != null : "fx:id=\"militaryColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert militaryBuiltColumn != null : "fx:id=\"militaryBuiltColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert militaryDestroyedColumn != null : "fx:id=\"militaryDestroyedColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert militaryLostColumn != null : "fx:id=\"militaryLostColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert honorColumn != null : "fx:id=\"honorColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";
		assert shipsColumn != null : "fx:id=\"shipsColumn\" was not injected: check your FXML file 'HighscoreView.fxml'.";

		Util.getLogger().config("HighscoreViewController init");

		int inientries = Settings.getIni().get("highscore", "entriesPerPage", int.class);
		inientries = Math.max(1, inientries);
		setNumberOfHighscoreEntriesPerPage(inientries);

		m_currentDatabaseControllerProperty.addListener((p, oldv, newv) -> {
			if (oldv != newv) {
				sortCombobox.getSelectionModel().select(0);
				highscoreTable.getItems().clear();
			}
		});

		highscoreTable.setPlaceholder(new Label(""));
		highscoreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	//	highscoreTable.scroll

		sortCombobox.setItems(FXCollections.observableArrayList(Arrays.asList(HighscoreType.values())));
		sortCombobox.getSelectionModel().select(0);
		sortCombobox.setCellFactory(param -> {
			return new ListCell<HighscoreType>() {
				@Override
				public void updateItem(HighscoreType item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
					} else {
						setText(resources.getString(getItem().toString().toLowerCase()));
					}

				}
			};

		});

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);

		rankColumn.setCellValueFactory(p -> {
			int rank = 0;
			Highscore newestHighscore = p.getValue().getHighscores().get(p.getValue().getHighscores().size()-1);
			rank = newestHighscore.getRank(sortCombobox.getSelectionModel().getSelectedItem());

			return new ReadOnlyObjectWrapper<String>(nf.format(rank));
		});

		nameColumn.setCellValueFactory(p -> {
			String text = p.getValue().getEntity().getName();
			if (playerRadioButton.isSelected()) {
				text += " [" + ((Player) p.getValue().getEntity()).getPlayerStatus() + "]";
			}
			return new ReadOnlyObjectWrapper<String>(text);
		});

		totalColumn.setCellValueFactory(p -> {
			Highscore newestHighscore = p.getValue().getHighscores().get(p.getValue().getHighscores().size()-1);
			long points = newestHighscore.getPoints(HighscoreType.Total);
			return new ReadOnlyObjectWrapper<String>(nf.format(points));
		});

		economyColumn.setCellValueFactory(p -> {
			Highscore newestHighscore = p.getValue().getHighscores().get(p.getValue().getHighscores().size()-1);
			long points = newestHighscore.getPoints(HighscoreType.Economy);
			return new ReadOnlyObjectWrapper<String>(nf.format(points));
		});

		researchColumn.setCellValueFactory(p -> {
			Highscore newestHighscore = p.getValue().getHighscores().get(p.getValue().getHighscores().size()-1);
			long points = newestHighscore.getPoints(HighscoreType.Research);
			return new ReadOnlyObjectWrapper<String>(nf.format(points));
		});

		militaryColumn.setCellValueFactory(p -> {
			Highscore newestHighscore = p.getValue().getHighscores().get(p.getValue().getHighscores().size()-1);
			long points = newestHighscore.getPoints(HighscoreType.Military);
			return new ReadOnlyObjectWrapper<String>(nf.format(points));
		});

		militaryBuiltColumn.setCellValueFactory(p -> {
			Highscore newestHighscore = p.getValue().getHighscores().get(p.getValue().getHighscores().size()-1);
			long points = newestHighscore.getPoints(HighscoreType.MilitaryBuilt);
			return new ReadOnlyObjectWrapper<String>(nf.format(points));
		});

		militaryDestroyedColumn.setCellValueFactory(p -> {
			Highscore newestHighscore = p.getValue().getHighscores().get(p.getValue().getHighscores().size()-1);
			long points = newestHighscore.getPoints(HighscoreType.MilitaryDestroyed);
			return new ReadOnlyObjectWrapper<String>(nf.format(points));
		});

		militaryLostColumn.setCellValueFactory(p -> {
			Highscore newestHighscore = p.getValue().getHighscores().get(p.getValue().getHighscores().size()-1);
			long points = newestHighscore.getPoints(HighscoreType.MilitaryLost);
			return new ReadOnlyObjectWrapper<String>(nf.format(points));
		});

		honorColumn.setCellValueFactory(p -> {
			Highscore newestHighscore = p.getValue().getHighscores().get(p.getValue().getHighscores().size()-1);
			long points = newestHighscore.getPoints(HighscoreType.Honor);
			return new ReadOnlyObjectWrapper<String>(nf.format(points));
		});
		
		shipsColumn.setCellValueFactory(p -> {
			Highscore newestHighscore = p.getValue().getHighscores().get(p.getValue().getHighscores().size()-1);
			long points = newestHighscore.getPoints(HighscoreType.Ships);
			return new ReadOnlyObjectWrapper<String>(nf.format(points));
		});

		highscoreTable.setRowFactory(tv -> {
			TableRow<IHighscoreEntity> row = new TableRow<IHighscoreEntity>(){
				@Override
                protected void updateItem(IHighscoreEntity hentity, boolean empty){
                    super.updateItem(hentity, empty);
                    if (!empty && hentity != null) {
                    	Entity entity = hentity.getEntity();
                        if (entity instanceof Player) {
                        	Player p = (Player)entity;
                        	String status = p.getPlayerStatus();
                        	if(!status.equals("active")){
                        		if(status.equals("v")){
    								setStyle("-fx-background-color: slateblue");
                        		}
                        		if(status.toLowerCase().indexOf("i") > -1){
                        			setStyle("-fx-background-color: grey");
                        		}
                        		if(status.toLowerCase().indexOf("i") > -1 && status.indexOf("v") > -1){
                        			setStyle("-fx-background-color: lightblue");
                        		}
                        	} else{
                        		setStyle("");
                        	}
                        }else{
                        	// no highlight for alliance
                        	setStyle("");
                        }
                    } else {
                    	setStyle("");
                    }
                }
			};
			row.setOnMouseClicked(event -> {
				if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && (!row.isEmpty())) {
					IHighscoreEntity highscoreEntity = row.getItem();
					Entity actor = highscoreEntity.getEntity();
					if (actor instanceof Player) {
						m_detailViewController.displayPlayer((Player) actor,
								m_currentDatabaseControllerProperty.get().getServerPrefix());
					}
					if (actor instanceof Alliance) {
						m_detailViewController.displayAlliance((Alliance) actor,
								m_currentDatabaseControllerProperty.get().getServerPrefix());
					}
				}
			});
			return row;
		});

	}

	public void setNumberOfHighscoreEntriesPerPage(int entries) {
		if (entries > 0)
			m_highscoreEntriesPerPage = entries;
	}

	public void setCurrentDatabaseController(OGameAPIDatabase currentDatabaseController) {
		m_currentDatabaseControllerProperty.set(Objects.requireNonNull(currentDatabaseController));
	}

	public void setDetailViewController(DetailViewController detailViewController) {
		m_detailViewController = Objects.requireNonNull(detailViewController);
	}

	@Override
	public void setParentController(BasicController parentController) {
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		m_executor = Objects.requireNonNull(executorService);
	}
}
