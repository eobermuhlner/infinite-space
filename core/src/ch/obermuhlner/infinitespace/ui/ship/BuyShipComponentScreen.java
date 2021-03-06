package ch.obermuhlner.infinitespace.ui.ship;

import ch.obermuhlner.infinitespace.I18N;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.game.ship.ShipComponent;
import ch.obermuhlner.infinitespace.game.ship.ShipFactory;
import ch.obermuhlner.infinitespace.game.ship.ShipPart;
import ch.obermuhlner.infinitespace.model.Node;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class BuyShipComponentScreen extends AbstractShipComponentScreen {

	public BuyShipComponentScreen (InfiniteSpaceGame game, ShipPart part, Node node) {
		super(game, part, part.types.get(0), node);
	}

	@Override
	protected void prepareStage (final Stage stage, Table rootTable) {
		rootTable.row();
		rootTable.add(new Label("Buy " + part.name, skin, TITLE));
		
		addOverviewTable(rootTable);

		// type selection
		addTypeSelection(rootTable, stage);

		// component table
		Table table = table();
		rootTable.row().colspan(2);
		rootTable.add(new ScrollPane(table, skin));

		addHeaderRow(table, componentType);
		
		for (ShipComponent component: ShipFactory.getShipComponents(componentType)) {
			addComponentRow(table, component, true);
		}
		
		// buttons
		rootTable.row().padTop(20);
		rootTable.add(button(I18N.OK, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				buySellComponents();
				game.setScreen(new ShipInfoScreen(infiniteSpaceGame, node, part.name));
			}
		}));
		rootTable.add(button(I18N.CANCEL, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				game.setScreen(new ShipInfoScreen(infiniteSpaceGame, node, part.name));
			}
		}));
		
		stage.addActor(rootTable);

		updateWidgets();
	}
}
