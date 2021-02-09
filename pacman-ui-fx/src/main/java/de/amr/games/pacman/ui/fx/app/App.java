package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.world.PacManGameWorld.TS;

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

	private float scaling = 2;

	@Override
	public void start(Stage stage) throws IOException {
		Scene scene = createPlayScene();
		stage.setScene(scene);

		stage.setTitle("Pac-Man / Ms. Pac-Man");
		stage.setOnCloseRequest(e -> {
			System.exit(0);
		});
		stage.show();
	}

	private Scene createPlayScene() {
		Scene scene = new Scene(createContent(), 28 * TS * scaling, 36 * TS * scaling);
		return scene;
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