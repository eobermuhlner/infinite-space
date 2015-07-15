package ch.obermuhlner.infinitespace.ui.land;

import ch.obermuhlner.infinitespace.I18N;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.ui.AbstractNodeStageScreen;
import ch.obermuhlner.infinitespace.ui.cargo.BuyCargoScreen;
import ch.obermuhlner.infinitespace.ui.cargo.MarketInfoScreen;
import ch.obermuhlner.infinitespace.ui.cargo.SellCargoScreen;
import ch.obermuhlner.infinitespace.ui.game.GameScreen;
import ch.obermuhlner.infinitespace.ui.game.GameScreen.RenderMode;
import ch.obermuhlner.infinitespace.ui.info.InfoScreen;
import ch.obermuhlner.infinitespace.ui.ship.ShipInfoScreen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class LandScreen extends AbstractNodeStageScreen {

	public LandScreen (InfiniteSpaceGame game, Node node) {
		super(game, node);
	}

	@Override
	protected void prepareStage (Stage stage, Table rootTable) {
		rootTable.row();
		rootTable.add(new Label("Landed: " + node.getName(), skin, TITLE));

		rootTable.row();
		Table table = table();
		table.defaults().fillX();
		rootTable.add(new ScrollPane(table, skin));

		if (node instanceof OrbitingNode) {
			OrbitingNode orbitingNode = (OrbitingNode)node;
			if (orbitingNode.population != null) {
				table.row();
				table.add(button("Info", new InfoScreen(infiniteSpaceGame, node, this)));

				table.row();
				table.add(new Label("Ship", skin, TITLE));

				table.row();
				table.add(button("Ship Info", new ShipInfoScreen(infiniteSpaceGame, node)));
				table.row();
				table.add(new TextButton("Shipyard", skin));

				table.row();
				table.add(new Label("Market and Cargo", skin, TITLE));

				table.row();
				table.add(button("Market Info", new MarketInfoScreen(infiniteSpaceGame, node)));
				table.row();
				table.add(button("Sell Cargo", new SellCargoScreen(infiniteSpaceGame, node)));
				table.row();
				table.add(button("Buy Cargo", new BuyCargoScreen(infiniteSpaceGame, node)));

				table.row();
				table.add(new Label("News and Missions", skin, TITLE));

				table.row();
				table.add(new TextButton("News", skin));
				table.row();
				table.add(new TextButton("Missions", skin));
				table.row();
				table.add(new TextButton("Passenger", skin));
			}
		}

		rootTable.row().padTop(20);
		rootTable.add(button(I18N.LAUNCH, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				infiniteSpaceGame.setScreen(new GameScreen(infiniteSpaceGame, RenderMode.NORMALSPACE));
			}
		}));

		stage.addActor(rootTable);
	}
}
