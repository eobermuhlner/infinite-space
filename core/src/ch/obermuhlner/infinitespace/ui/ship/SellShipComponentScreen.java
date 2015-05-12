package ch.obermuhlner.infinitespace.ui.ship;

import ch.obermuhlner.infinitespace.GameState;
import ch.obermuhlner.infinitespace.I18N;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.game.ship.Ship;
import ch.obermuhlner.infinitespace.game.ship.ShipComponent;
import ch.obermuhlner.infinitespace.game.ship.ShipPart;
import ch.obermuhlner.infinitespace.ui.AbstractGameScreen;
import ch.obermuhlner.infinitespace.ui.AbstractStageScreen;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class SellShipComponentScreen extends AbstractStageScreen {

	private ShipComponent componentToSell;
	private AbstractGameScreen backScreen;

	public SellShipComponentScreen (InfiniteSpaceGame game, ShipComponent component, AbstractGameScreen backScreen) {
		super(game);
		
		this.componentToSell = component;
		this.backScreen = backScreen;
	}

	@Override
	protected void prepareStage (Stage stage, Table rootTable) {
		rootTable.row();
		rootTable.add(new Label("Sell " + componentToSell.getClass().getSimpleName(), skin, TITLE)).colspan(2);

		Table table = table();
		rootTable.row().colspan(2);
		rootTable.add(new ScrollPane(table, skin));

		table.row();
		table.add(new Label("Price", skin));
		table.add(new Label(Units.moneyToString(componentToSell.price), skin));
		
		table.row();
		table.add(new Label("Cash", skin));
		table.add(new Label(Units.moneyToString(GameState.INSTANCE.cash), skin));

		rootTable.row().padTop(20);
		rootTable.add(button(I18N.OK, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				sellShipComponent();
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

	private void sellShipComponent () {
		Ship ship = GameState.INSTANCE.ship;
		
		for (ShipPart<?> part : ship.parts) {
			for (int i = 0; i < part.components.size(); i++) {
				ShipComponent component = part.components.get(i);
				if (component == componentToSell) {
					part.components.remove(component);
					ship.update();
					GameState.INSTANCE.cash += component.price;
					GameState.INSTANCE.save();
					return;
				}
			}
		}
	}
	

}
