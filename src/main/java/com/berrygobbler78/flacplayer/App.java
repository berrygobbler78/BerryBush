package com.berrygobbler78.flacplayer;

import java.io.*;
import java.util.Objects;

import com.berrygobbler78.flacplayer.userdata.Playlist;
import com.berrygobbler78.flacplayer.userdata.References;
import com.berrygobbler78.flacplayer.util.FileUtils;
import com.pixelduke.window.ThemeWindowManagerFactory;
import com.pixelduke.window.Win11ThemeWindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

public class App extends Application {
    public static MusicPlayer musicPlayer = new MusicPlayer();
    public static References references;

    private static final File refrencesFile = new File(
            "src/main/resources/com/berrygobbler78/flacplayer/cache/References.ser");

    private static Stage primaryStage;

    void main(String[] args) {
        System.out.println("Hello World!");
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException, ClassNotFoundException {
        deleteTempFile();

        // Checks if userData already exists, if not prompt for new directory

        if(refrencesFile.exists()){
            FileInputStream fis;
            ObjectInputStream ois;
            fis = new FileInputStream(refrencesFile);
            ois = new ObjectInputStream(fis);
            references = (References) ois.readObject();

            fis.close();
            ois.close();
        } else {
            references = new References();
            setupWizard();

            FileOutputStream fos = new FileOutputStream(refrencesFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(references);
            oos.close();
            fos.close();

        }

        references.clearPlaylists();
        loadPlaylists();

        // For coloring window border
        Win11ThemeWindowManager themeWindowManager = (Win11ThemeWindowManager) ThemeWindowManagerFactory.create();

        FXMLLoader fxmlLoader =  new FXMLLoader(new File("src/main/resources/com/berrygobbler78/flacplayer/fxml/revised.fxml").getAbsoluteFile().toURI().toURL());

        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(Objects.requireNonNull(App.class.getResource("css/styles.css")).toExternalForm());

        primaryStage = stage;
        primaryStage.setTitle("BerryBush");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            saveReferences();
        });

        themeWindowManager.setWindowFrameColor(primaryStage, Color.web("#121212"));
        themeWindowManager.setDarkModeForWindowFrame(primaryStage, true);

        musicPlayer.setController(fxmlLoader.getController());
    }

    public void saveUserData() {
        savePlaylists();
        saveReferences();
    }

    public static void saveReferences() {
        references.clearPlaylists();

        try {
            FileOutputStream fos = new FileOutputStream(refrencesFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(references);
            oos.close();
            fos.close();
        } catch (IOException e) {
            System.err.println("Could not save references." + e);
        }

    }

    public static void savePlaylists() {
        try{
            for(Playlist playlist : references.getPlaylists()){
                FileOutputStream fos = new FileOutputStream(new File("src/main/resources/com/berrygobbler78/flacplayer/cache/playlists/" + playlist.getName().toLowerCase().replace(" ", "-") + ".ser"));
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(playlist);
                oos.close();
                fos.close();
            }

        } catch(IOException e){
            System.err.println("Could not save playlists." + e);

        }
    }

    public void loadPlaylists() {
        try {
            for (File file : new File("src/main/resources/com/berrygobbler78/flacplayer/cache/playlists").listFiles()) {
                if (file.getName().endsWith(".ser")) {
                    FileInputStream fis;
                    ObjectInputStream ois;
                    fis = new FileInputStream(file);
                    ois = new ObjectInputStream(fis);
                    references.addPlaylist((Playlist) ois.readObject());

                    fis.close();
                    ois.close();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load playlists." + e);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    static void deleteTempFile() {
        try {
            File tempFile = new File("src/main/resources/com/berrygobbler78/flacplayer/cache/temp.wav");

            if(tempFile.exists()) {
                try {
                    musicPlayer.closeMediaPlayer();
                } catch (Exception e) {
                    System.out.println("No media player to close");
                }
                if(tempFile.delete()) {
                    System.out.println("Deleted temp file");
                } else {
                    System.out.println("Failed to delete temp file");
                }
            }
        } catch (Exception e) {
            System.err.println("No temp file to delete!");
        }
    }

    static void setupWizard() {
        Wizard wizard = new Wizard();

        // Page 1
        Label question1 = new Label("Enter Directory Location:");

        TextField textField1 = new TextField();
        textField1.setEditable(true);
        textField1.setPromptText("Select Directory");
        textField1.setMinWidth(300.0);
        textField1.setMaxHeight(10.0);
        textField1.setMaxWidth(300.0);

        Button button1 = new Button();
        button1.setOnAction(event -> {
            textField1.setText(FileUtils.openDirectoryChooser(primaryStage, "Choose directory", "C://").getAbsolutePath());
        });
        button1.setText("Open Explorer");

        HBox hbox1 = new HBox();
        hbox1.getChildren().addAll(textField1, button1);
        hbox1.setSpacing(10.0);

        VBox vbox1 = new VBox();
        vbox1.getChildren().addAll(question1, hbox1);

        WizardPane page1 = new WizardPane();
        page1.setContent(vbox1);

        // Page2
        Label question2 = new Label("Enter Username:");

        TextField textField2 = new TextField();
        textField2.setEditable(true);
        textField2.setPromptText("Username...");
        textField2.setMinWidth(300.0);
        textField2.setMaxHeight(10.0);
        textField2.setMaxWidth(300.0);

        VBox vbox2 = new VBox();
        vbox2.getChildren().addAll(question2, textField2);

        WizardPane page2 = new WizardPane();
        page2.setContent(vbox2);

        wizard.setUserData(textField2.getText());

        // Wizard settings
        wizard.setTitle("BerryBush Setup Wizard");
        wizard.setFlow(new Wizard.LinearFlow(page1, page2));

        // Show wizard and wait, set userData
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                references.setRootDirectoryPath(textField1.getText());
                references.setUserName(textField2.getText());
            }
        });
    }

}