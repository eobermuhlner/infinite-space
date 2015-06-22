package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.model.Node;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class NodeMenuWindow extends Window {

	private InfiniteSpaceGame game;

	private AbstractGameScreen screen;

	private Node node;
	
	private Skin skin;

	public NodeMenuWindow (InfiniteSpaceGame game, AbstractGameScreen screen, Node node, Skin skin) {
		super(getTitle(node), skin);
		this.game = game;
		this.screen = screen;
		this.node = node;
		this.skin = skin;
		
		fillMenuItems();
	}

	private void fillMenuItems () {
		//defaults().expandX();
		
		//addNodeAction("System Info", SystemInfoNodeAction.INSTANCE);
		addNodeAction("Info", InfoNodeAction.INSTANCE);
		addNodeAction("Land", LandNodeAction.INSTANCE);
		addNodeAction("Leave Hyperspace", LeaveHyperspaceNodeAction.INSTANCE);
	}

	private void addNodeAction (String name, final NodeAction nodeAction) {
		if (nodeAction.isValid(node)) {
			row();
			
			TextButton textButton = new TextButton(name, skin);
			textButton.addListener(new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					nodeAction.execute(game, node, screen);
					remove();
				}
			});
			add(textButton).fillX();
		}
	}

	private static String getTitle (Node node) {
		return node.getName();
	}

}
