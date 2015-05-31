package ch.obermuhlner.infinitespace.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public abstract class AbstractGameScreen implements Screen {

	protected Game game;

	protected Skin skin = GameSkin.getSkin();
	
	public AbstractGameScreen(Game game) {
		this.game = game;
	}
	
	protected boolean needsContinuousRendering() {
		return true;
	}

	@Override
	public void render (float delta) {
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void show () {
		Gdx.graphics.setContinuousRendering(needsContinuousRendering());
	}

	@Override
	public void hide () {
		Gdx.graphics.setContinuousRendering(true);
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}
}
