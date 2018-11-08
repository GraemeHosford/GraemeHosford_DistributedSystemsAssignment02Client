// Name: Graeme Hosford
// Student ID: R00147327
// Distributed Systems Programming Project 01

package main;

import controllers.Monitor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import threads.ClientSideCheckForChange;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class Main extends Application {

    @FXML
    public Button uploadButton;

    @FXML
    private ListView<String> sharedMusicList, clientMusicList;

    @FXML
    private Button downloadButton, playButton;

    private Monitor monitor;

    private boolean songPlaying = false;

    private MediaPlayer player;
    private String[] fileNames;

    private static final int PORT = 9090;

    private Socket clientSocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        clientSocket = new Socket("localhost", PORT);
        oos = new ObjectOutputStream(clientSocket.getOutputStream());
        ois = new ObjectInputStream(clientSocket.getInputStream());
        oos.writeObject(1);

        fileNames = (String[]) ois.readObject();

        // Load XML layout file
        Parent root = FXMLLoader.load(getClass().getResource("main_layout.fxml"));

        // Set height and width
        Scene scene = new Scene(root, 500, 500);

        // Set title and scene then show window
        primaryStage.setTitle("R00147327- Distributed Systems Programming Project");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Get an instance of the monitor
        monitor = Monitor.getInstance();

        // Get lists of both files on client folder and files on shared folder
        ObservableList<String> sharedMusic = FXCollections.observableArrayList(fileNames);
        ObservableList<String> clientMusic = FXCollections.observableArrayList(monitor.getClientNames());

        // Initialise the shared music listview
        sharedMusicList = (ListView<String>) root.lookup("#sharedMusicList");
        sharedMusicList.setItems(sharedMusic);
        sharedMusicList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                downloadButton.setDisable(false));

        // Initialise the client music listview
        clientMusicList = (ListView<String>) root.lookup("#clientMusicList");
        clientMusicList.setItems(clientMusic);

        // Give behaviour to how buttons should act when file is selected
        clientMusicList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            playButton.setDisable(false);

            if(oldValue != null && !oldValue.equals(newValue)) {
                if(!oldValue.equals(newValue)) {
                    playButton.setText("Play");
                } else {
                    playButton.setText("Pause");
                }
            }
        });

        // Initialise download button
        downloadButton = (Button) root.lookup("#downloadButton");
        downloadButton.setDisable(true);

        downloadButton.setOnAction(e -> downloadFile());

        // initialise the play button
        playButton = (Button) root.lookup("#playButton");
        playButton.setDisable(true);
        playButton.setOnAction(e -> playSong(clientMusicList.getSelectionModel().getSelectedItem()));


        // Initialise the upload button
        uploadButton = (Button) root.lookup("#uploadButton");
        uploadButton.setOnAction(e -> uploadFile(primaryStage));


        // Start the thread used to monitor the shared folder for new files
        ClientSideCheckForChange clientSideFileChangeChecker = new ClientSideCheckForChange(clientSocket, ois, oos, () -> {

            try {
                oos.writeObject(1);
                fileNames = (String[]) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> sharedMusicList.setItems(FXCollections.observableArrayList(fileNames)));
        });

        clientSideFileChangeChecker.start();

        // Change close behaviour so background thread is also shutdown when program is closed
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Uploads a selected file to the shared folder
     * @param stage The window to show the FileChooser in
     */
    private void uploadFile(Stage stage) {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(".MP3", "*.mp3"));

        File file = fileChooser.showOpenDialog(stage);

        if(file != null) {

            byte[] fileContents = null;

            try {
                fileContents = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            monitor.uploadFile(file.getName(), fileContents, clientSocket, oos);
        }

    }

    /**
     * Downloads a selected file form the shared folder
     */
    private void downloadFile() {

        String fileName = sharedMusicList.getSelectionModel().getSelectedItem();

        byte[] file = monitor.getFile(fileName, clientSocket, ois, oos);

        File newFile = new File("local" + File.separator + fileName);

        try {
            Files.write(newFile.toPath(), file, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObservableList<String> music = FXCollections.observableArrayList(monitor.getClientNames());
        clientMusicList.setItems(music);
    }

    /**
     * Plays a selected song from the client folder
     * @param songName The song selected to be played
     */
    private void playSong(String songName) {

        // Sets the song as a media object
        Media song = new Media(new File("local/" + songName).toURI().toString());

        // Creates a new player if necessary and sets behaviour for how thee play/pause button should act
        if(player == null) {
            player = new MediaPlayer(song);
            player.play();
            playButton.setText("Pause");
            songPlaying = true;
        } else {
            if(song.getSource().equals(player.getMedia().getSource())){
                if(songPlaying) {
                    player.stop();
                    playButton.setText("Play");
                    songPlaying = false;
                } else {
                    player.play();
                    songPlaying = true;
                    playButton.setText("Pause");
                }
            } else {
                if(songPlaying) {
                    player.stop();
                }
                player = new MediaPlayer(song);
                player.play();
                playButton.setText("Pause");
                songPlaying = true;
            }
        }
    }
}