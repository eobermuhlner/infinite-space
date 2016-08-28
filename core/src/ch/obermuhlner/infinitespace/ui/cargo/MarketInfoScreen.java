package ch.obermuhlner.infinitespace.ui.cargo;

import ch.obermuhlner.infinitespace.I18N;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.generator.Generator;
import ch.obermuhlner.infinitespace.model.universe.population.Commodity;
import ch.obermuhlner.infinitespace.model.universe.population.Population;
import ch.obermuhlner.infinitespace.ui.AbstractNodeStageScreen;
import ch.obermuhlner.infinitespace.ui.land.LandScreen;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MarketInfoScreen extends AbstractNodeStageScreen {

	public MarketInfoScreen (InfiniteSpaceGame game, Node node) {
		super(game, node);
	}

	@Override
	protected void prepareStage (Stage stage, Table rootTable) {
		rootTable.row();
		rootTable.add(new Label("Market Info: " + node.getName(), skin, TITLE)).colspan(2);

		Table table = table();
		rootTable.row().colspan(2);
		rootTable.add(new ScrollPane(table, skin));
	
		if (node instanceof OrbitingNode) {
			OrbitingNode orbitingNode = (OrbitingNode)node;
			Population population = orbitingNode.population;
			if (population != null) {
				table.row();
				table.add(new Label("Commodity", skin, HEADER));
				table.add(new Label("Supply", skin, HEADER));
				table.add(new Label("Demand", skin, HEADER));
				table.add(new Label("Base Price", skin, HEADER));
				
				for(Commodity commodity : Commodity.values()) {
					if (population.hasCommodity(commodity)) {
						table.row();
						table.add(new Label(Units.toString(commodity), skin, HEADER));
						table.add(new Label(Units.percentToString(Generator.calculateSupply(commodity, node, population)), skin)).right();
						table.add(new Label(Units.percentToString(Generator.calculateDemand(commodity, node, population)), skin)).right();
						int calculatePrice = (int) (Generator.calculatePrice(commodity, node, population) + 0.5);
						table.add(new Label(Units.moneyToString(calculatePrice), skin)).right();
					}
				}
			}
		}

		rootTable.row().padTop(20);
		rootTable.add(button(I18N.OK, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				game.setScreen(new LandScreen(infiniteSpaceGame, node));
			}
		}));

		stage.addActor(rootTable);
	}
}
