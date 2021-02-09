package de.amr.games.pacman.ui.fx.app;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

	private static Scene scene;

	@Override
	public void start(Stage stage) throws IOException {
		scene = new Scene(createContent(), 640, 480);
		stage.setScene(scene);
		stage.show();
	}

	private Parent createContent() {
		Pane pane = new StackPane();
		Text text = new Text("Hello, JavaFX!");
		text.setFont(Font.font("Serif", 20));
		text.setStroke(Color.BLACK);
		pane.getChildren().add(text);
		return pane;
	}

	public static void main(String[] args) {
		launch();
	}

}