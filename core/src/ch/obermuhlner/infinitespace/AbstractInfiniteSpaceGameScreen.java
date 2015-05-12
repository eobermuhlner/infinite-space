package ch.obermuhlner.infinitespace;


public class AbstractInfiniteSpaceGameScreen extends AbstractGameScreen {

	protected InfiniteSpaceGame infiniteSpaceGame;

	public AbstractInfiniteSpaceGameScreen (InfiniteSpaceGame game) {
		super(game);
		
		infiniteSpaceGame = game;
	}
	
	protected void showWelcomeScreen() {
		infiniteSpaceGame.setScreen(new WelcomeScreen(infiniteSpaceGame));
	}

	protected void showOptionsScreen() {
		infiniteSpaceGame.setScreen(new OptionsScreen(infiniteSpaceGame));
	}

	protected void showGameScreen() {
		infiniteSpaceGame.setScreen(new GameScreen(infiniteSpaceGame));
	}

}
