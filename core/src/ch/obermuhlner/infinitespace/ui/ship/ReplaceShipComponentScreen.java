package ch.obermuhlner.infinitespace.ui.ship;

import ch.obermuhlner.infinitespace.GameState;
import ch.obermuhlner.infinitespace.I18N;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.game.ship.ShipComponent;
import ch.obermuhlner.infinitespace.ui.AbstractGameScreen;
import ch.obermuhlner.infinitespace.ui.AbstractStageScreen;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class ReplaceShipComponentScreen extends AbstractStageScreen {

	private ShipComponent component;
	private AbstractGameScreen backScreen;

	public ReplaceShipComponentScreen (InfiniteSpaceGame game, ShipComponent component, AbstractGameScreen backScreen) {
		super(game);
		
		this.component = component;
		this.backScreen = backScreen;
	}

	@Override
	protected void prepareStage (Stage stage) {
		Table table = table();

		table.row();
		table.add(new Label("Replace " + component.getClass().getSimpleName(), skin, TITLE));

		table.row();
		table.add(new Label("Price", skin));
		table.add(new Label(Units.moneyToString(component.price), skin));
		
		table.row();
		table.add(new Label("Cash", skin));
		table.add(new Label(Units.moneyToString(GameState.INSTANCE.cash), skin));

		// root table
		Table rootTable = rootTable();
		
		rootTable.row().colspan(2);
		ScrollPane scrollPane = new ScrollPane(table, skin);
		rootTable.add(scrollPane);
		
		rootTable.row().padTop(20);
		rootTable.add(button(I18N.OK, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				game.setScreen(backScreen);
			}
		}));
		rootTable.add(button(I18N.CANCEL, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				game.setScreen(backScreen);
			}
		}));
		
		stage.addActor(rootTable);
	}

}
