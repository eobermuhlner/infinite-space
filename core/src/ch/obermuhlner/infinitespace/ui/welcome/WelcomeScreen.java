package ch.obermuhlner.infinitespace.ui.welcome;

import ch.obermuhlner.infinitespace.GameState;
import ch.obermuhlner.infinitespace.I18N;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.ui.AbstractStageScreen;
import ch.obermuhlner.infinitespace.ui.game.GameScreen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class WelcomeScreen extends AbstractStageScreen {

	private ProgressBar progressBarLoading;
	private TextButton buttonPlay;

	public WelcomeScreen (InfiniteSpaceGame game) {
		super(game);
	}

	protected void prepareStage(Stage stage) {
		Table table = new Table(skin);
		table.setFillParent(true);
		
		table.row();
		progressBarLoading = new ProgressBar(0, 1, 0.01f, false, skin);
		table.add(progressBarLoading).colspan(2);
		
		table.row();
		buttonPlay = button(I18N.PLAY, new GameScreen(infiniteSpaceGame));
		buttonPlay.setDisabled(true);
		table.add(buttonPlay);
		table.add(button(I18N.NEW, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				GameState.INSTANCE.reset();
			}
		}));
		table.add(button(I18N.OPTIONS, new OptionsScreen(infiniteSpaceGame)));

		stage.addActor(table);
	}

	@Override
	public void render (float delta) {
		super.render(delta);
		
		boolean finishedLoading = infiniteSpaceGame.assetManager.update();
		
		progressBarLoading.setValue(infiniteSpaceGame.assetManager.getProgress());
		
		if (finishedLoading) {
			buttonPlay.setDisabled(false);
		}
	}
	
}
