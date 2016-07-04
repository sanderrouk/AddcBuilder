package com.sonictest.addcbuilder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by s on 29.06.16.
 */
public class Main extends Application{
    public static void main(String[] args){
        Application.launch();
    }

    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/layout.fxml"));
        Scene scene = new Scene(root, 800, 400);

        primaryStage.setTitle("Sonictest ADDC Builder");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

    }
}
