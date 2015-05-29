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

public class ReplaceShipComponentScreen extends AbstractShipComponentScreen {

	private ShipComponent originalComponent;

	public ReplaceShipComponentScreen (InfiniteSpaceGame game, ShipPart part, ShipComponent originalComponent, Node node) {
		super(game, part, originalComponent.getClass().getSimpleName(), node);
		
		this.originalComponent = originalComponent;
	}

	@Override
	protected void prepareStage (final Stage stage, Table rootTable) {
		rootTable.row();
		rootTable.add(new Label("Replace " + part.types, skin, TITLE));
		
		addOverviewTable(rootTable);

		rootTable.row();
		rootTable.add(new Label("Sell the following component", skin)).colspan(2);
		
		// original component table
		Table tableOriginal = table();
		rootTable.row().colspan(2);
		rootTable.add(new ScrollPane(tableOriginal, skin));

		addHeaderRow(tableOriginal, componentType);
		addComponentRow(tableOriginal, originalComponent, false);

		rootTable.row();
		rootTable.add(new Label("and replace it with one of:", skin)).colspan(2);

		// replace component table
		Table tableReplacement = table();
		rootTable.row().colspan(2);
		rootTable.add(new ScrollPane(tableReplacement, skin));

		addHeaderRow(tableReplacement, componentType);
		
		for (ShipComponent component: ShipFactory.getShipComponents(componentType)) {
			if (component != originalComponent) {
				addComponentRow(tableReplacement, component, component.volume <= part.maxComponentVolume);
			}
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
	
	@Override
	protected void buySellComponents() {
		if (soldComponents.size == 0 && boughtComponents.size > 0) {
			soldComponents.add(originalComponent);
		}
 		super.buySellComponents();
	}
}
