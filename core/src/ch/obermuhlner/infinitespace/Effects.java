package ch.obermuhlner.infinitespace;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class Effects {

	private static final float FADE_OUT_TIME = 0.2f;
	private static final float FADE_IN_TIME = 0.5f;
	
	private static final Action FADE_IN_ACTION = sequence(alpha(0), Actions.fadeIn(FADE_IN_TIME));
	private static final Action FADE_OUT_ACTION = sequence(Actions.fadeOut(FADE_OUT_TIME));

	private static final Action PULL_IN_ACTION = sequence(scaleTo(1, 0), scaleBy(0, 1, 0.2f));
	
	public static Action fadeIn() {
		return FADE_IN_ACTION;
	}

	public static Action fadeOut() {
		return FADE_OUT_ACTION;
	}
	
	public static Action fadeOut(final Game game, final Screen screen) {
		return sequence(Actions.fadeOut(FADE_OUT_TIME), run(new Runnable() {
			@Override
			public void run () {
				game.setScreen(screen);
			}
		}));
	}
	
	public static Action pullIn() {
		return PULL_IN_ACTION;
	}
}
