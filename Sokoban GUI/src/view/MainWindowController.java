package view;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Observable;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import commons.Level;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainWindowController extends Observable implements Initializable, IView {

	@FXML
	private SokobanDisplayer sokobanDisplayer;

	// Steps
	@FXML
	private Text countSteps;

	// Timer
	@FXML
	private Text timerText;
	private Timer timer;
	private int secCount, minCount;
	private StringProperty timerCount;
	private boolean loadFromGui;

	// Keyboard settings;
	private ViewSettings viewSettings;

	// Stage
	private Stage primaryStage;

	// Sound
	@FXML
	private Button musicButton;
	private String musicFile;
	private Media sound;
	private MediaPlayer mediaPlayer;
	private boolean isMusicOn;

	@FXML
	private Label status;

	public MainWindowController() {

		this.viewSettings = initViewSettings("./resources/viewSettings/viewSettings.xml");
		this.secCount = 0;
		this.minCount = 0;
		this.loadFromGui = false;

		this.musicFile = "./resources/music/song1.mp3";
		this.sound = new Media(new File(musicFile).toURI().toString());
		this.mediaPlayer = new MediaPlayer(sound);
		this.isMusicOn = true;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// the focus on the SokobanDisplayer
		setFocus();
		// play music
		playSound();

		sokobanDisplayer.redrawStart();
		sokobanDisplayer.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> sokobanDisplayer.requestFocus());
		sokobanDisplayer.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {

				String commandInput = null;
				status.setText("");

				if (event.getCode() == viewSettings.getMoveUp()) {
					commandInput = "Move up";
					// sokobanDisplayer.setPlayerFileName("./resources/elements/Player1.png");
				} else if (event.getCode() == viewSettings.getMoveDown()) {
					commandInput = "Move down";
					// sokobanDisplayer.setPlayerFileName("./resources/elements/Player.png");
				} else if (event.getCode() == viewSettings.getMoveRight()) {
					commandInput = "Move right";
					// sokobanDisplayer.setPlayerFileName("./resources/elements/Player1.png");
				} else if (event.getCode() == viewSettings.getMoveLeft()) {
					commandInput = "Move left";
					// sokobanDisplayer.setPlayerFileName("./resources/elements/Player.png");
				}
				if (commandInput != null) {
					setChanged();
					notifyObservers(commandInput);
				} else
					displayError("Invalid key");

			}
		});

	}

	private void initTimer(int sCount, int mCount) {

		this.timerCount = new SimpleStringProperty();
		this.secCount = sCount;
		this.minCount = mCount;
		this.timer = new Timer();
		this.timerText.textProperty().bind(this.timerCount);

		this.timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				secCount++;
				if (secCount > 59) {
					minCount++;
					secCount = 0;
				}
				if (minCount < 10)
					if (secCount < 10)
						timerCount.set("0" + (minCount) + ":0" + (secCount));
					else
						timerCount.set("0" + (minCount) + ":" + (secCount));
				else
					timerCount.set("" + (minCount) + ":" + (secCount));

			}
		}, 0, 1000);

	}

	private void stopTimer() {
		
		if (timer != null)
			timer.cancel();

	}

	// bind the steps by string property
	@Override
	public void createBindSteps(StringProperty Counter) {
		this.countSteps.textProperty().bind(Counter);
	}

	// GUI CODE
	public void openFile() {

		FileChooser fc = new FileChooser();
		fc.setTitle("Open level file");
		fc.setInitialDirectory(new File("./resources/levels"));

		fc.getExtensionFilters().addAll(new ExtensionFilter("Text files", "*.txt"),
				new ExtensionFilter("XML files", "*.xml"), new ExtensionFilter("Object files", "*.obj"));

		File choosen = fc.showOpenDialog(null);// the window that will stack in
												// the backround-Jbutoon

		if (choosen != null) {
			setChanged();
			notifyObservers("load " + choosen.getPath());
		}
		this.loadFromGui = true;
		stopTimer();
		initTimer(0, 0);

	}

	public void saveFile() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Save level file");
		fc.setInitialDirectory(new File("./resources/levels"));

		fc.getExtensionFilters().addAll(new ExtensionFilter("Text files", "*.txt"),
				new ExtensionFilter("XML files", "*.xml"), new ExtensionFilter("Object files", "*.obj"));

		File choosen = fc.showSaveDialog(null);// the window that will stack in
												// the backround-Jbutoon

		if (choosen != null) {
			setChanged();
			notifyObservers("save " + choosen.getPath());
		}

	}

	@Override
	public void displayGUI(Level level) {

		sokobanDisplayer.setLevelData(level.getLevelBored());

		if (this.loadFromGui == false) {
			initTimer(0, 0);
			this.loadFromGui = true;
		}

		if (level.isEndOfLevel()) {
			finishLevel();
		}
	}

	public void finishLevel() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Finish Level");
				alert.setHeaderText("Congratulations!!!");
				try {
					ImageView imagVew = new ImageView(new Image(new FileInputStream("./resources/elements/winer.png")));
					imagVew.setFitWidth(70);
					imagVew.setFitHeight(80);
					alert.setGraphic(imagVew);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				alert.setContentText("Steps: " + countSteps.getText() + "\n Time: " + timerText.getText());
				alert.show();

			}
		});
		stopTimer();
	}

	// load XML file
	private ViewSettings initViewSettings(String filepath) {

		XMLDecoder decoder;
		ViewSettings vs = null;
		try {
			decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(new File(filepath))));
			vs = (ViewSettings) decoder.readObject();
			decoder.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return vs;
	}

	// exit window
	public void exitWindow() {
		stopTimer();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirm Exit");
		alert.setContentText("Exit Sokoban?");
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {
			setChanged();
			notifyObservers("exit");
			stopSound();
			Platform.exit();
		} else {
			initTimer(this.secCount, this.minCount);
		}
	}

	@Override
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		exitPrimaryStage();

	}

	// close the main window with the redX
	@Override
	public void exitPrimaryStage() {
		this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				setChanged();
				notifyObservers("exit");
			}
		});
	}

	// set the focus on the sokobanDisplayer
	private void setFocus() {
		sokobanDisplayer.focusedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
				Platform.runLater(new Runnable() {
					public void run() {
						sokobanDisplayer.requestFocus();
					}
				});
			}
		});
	}

	@Override
	public void displayError(String error) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				status.setText(error);

			}
		});
	}

	// music handle
	private void playSound() {
		mediaPlayer.play();
	}

	private void stopSound() {
		mediaPlayer.stop();
	}

	private void pauseSound() {
		mediaPlayer.pause();
	}

	public void startStopMusic() {
		this.musicButton.setText("");
		if (this.isMusicOn) {
			pauseSound();
			this.isMusicOn = false;
			try {
				ImageView imagVew = new ImageView(new Image(new FileInputStream("./resources/music/mute.png")));
				imagVew.setFitWidth(20);
				imagVew.setFitHeight(20);
				this.musicButton.setGraphic(imagVew);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			playSound();
			this.isMusicOn = true;
			try {
				ImageView imagVew = new ImageView(new Image(new FileInputStream("./resources/music/sound.png")));
				imagVew.setFitWidth(20);
				imagVew.setFitHeight(20);
				this.musicButton.setGraphic(imagVew);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
