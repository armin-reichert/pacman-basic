package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.PacManGameUI;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PacManGameFXUI implements PacManGameUI {

	private final Stage stage;
	private PacManGameModel game;

	Text text;

	public PacManGameFXUI(Stage stage, PacManGameModel game, int scaling) {
		this.stage = stage;
		this.game = game;
		Scene scene = createPlayScene(scaling);
		stage.setScene(scene);
		stage.setTitle("Pac-Man / Ms. Pac-Man");
		stage.setOnCloseRequest(e -> {
			System.exit(0);
		});
	}

	private Scene createPlayScene(float scaling) {
		Pane pane = new StackPane();
		text = new Text("Hello, JavaFX!");
		text.setFont(Font.font("Serif", 20));
		text.setStroke(Color.BLACK);
		pane.getChildren().add(text);
		Scene scene = new Scene(pane, 28 * TS * scaling, 36 * TS * scaling);
		return scene;
	}

	@Override
	public void setGame(PacManGameModel game) {
		this.game = game;
	}

	@Override
	public void setCloseHandler(Runnable handler) {
	}

	@Override
	public void updateScene() {
		text.setText(game.stateDescription());
	}

	@Override
	public void show() {
		stage.show();

	}

	@Override
	public void render() {
	}

	@Override
	public void showFlashMessage(String message) {
	}

	@Override
	public boolean keyPressed(String keySpec) {
		return false;
	}

	@Override
	public Optional<SoundManager> sounds() {
		return null;
	}

	@Override
	public void mute(boolean muted) {
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return null;
	}

}