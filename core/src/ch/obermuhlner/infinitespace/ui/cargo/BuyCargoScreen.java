package ch.obermuhlner.infinitespace.ui.cargo;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.infinitespace.CommodityItem;
import ch.obermuhlner.infinitespace.GameState;
import ch.obermuhlner.infinitespace.I18N;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.universe.population.Population;
import ch.obermuhlner.infinitespace.ui.AbstractNodeStageScreen;
import ch.obermuhlner.infinitespace.ui.land.LandScreen;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class BuyCargoScreen extends AbstractNodeStageScreen {

	private Label labelCash;
	private Label labelCargo;
	
	private List<CommodityItemUserInterface> commodityItemUserInterfaces = new ArrayList<CommodityItemUserInterface>();
	
	private long cargoCash = 0;
	private long cargoSpace = 0;

	public BuyCargoScreen (InfiniteSpaceGame game, Node node) {
		super(game, node);
	}

	@Override
	protected void prepareStage (Stage stage, Table rootTable) {
		rootTable.row();
		rootTable.add(new Label("Buy Cargo: " + node.getName(), skin, TITLE)).colspan(2);

		Table table = table();
		rootTable.row().colspan(2);
		rootTable.add(new ScrollPane(table, skin));
	
		table.row();
		table.add(new Label("Name", skin));
		table.add(new Label("Type", skin));
		table.add(new Label("Available", skin));
		table.add(new Label("Price", skin));
		table.add(new Label("Buy -", skin));
		table.add(new Label("Buy +", skin));
		table.add(new Label("Amount", skin));
		table.add(new Label("Total Price", skin));

		if (node instanceof OrbitingNode) {
			OrbitingNode orbitingNode = (OrbitingNode)node;
			Population population = orbitingNode.population;
			if (population != null && population.commodities != null) {
				for (CommodityItem item : population.commodities) {
					final CommodityItemUserInterface commodityItemUserInterface = new CommodityItemUserInterface();
					commodityItemUserInterfaces.add(commodityItemUserInterface);
					commodityItemUserInterface.item = item;
					
					table.row();
					table.add(new Label(item.name, skin));
					table.add(new Label(item.commodity.toString(), skin));
					commodityItemUserInterface.labelAvailableAmount = new Label("", skin);
					table.add(new Label(Units.toString(item.amount), skin)).right();
					table.add(new Label(Units.moneyToString(item.priceSell), skin)).right();
					commodityItemUserInterface.buttonMinus = new TextButton("-1", skin);
					table.add(commodityItemUserInterface.buttonMinus);
					commodityItemUserInterface.buttonPlus = new TextButton("+1", skin);
					table.add(commodityItemUserInterface.buttonPlus);
					commodityItemUserInterface.labelAmount = new Label("", skin);
					table.add(commodityItemUserInterface.labelAmount).right();
					commodityItemUserInterface.labelCash = new Label("", skin);
					table.add(commodityItemUserInterface.labelCash).right();
					
					commodityItemUserInterface.buttonPlus.addListener(new ChangeListener() {
						@Override
						public void changed (ChangeEvent event, Actor actor) {
							commodityItemUserInterface.amount++;
							cargoCash += commodityItemUserInterface.item.priceSell;
							cargoSpace++;
							update();
						}
					});
					commodityItemUserInterface.buttonMinus.addListener(new ChangeListener() {
						@Override
						public void changed (ChangeEvent event, Actor actor) {
							commodityItemUserInterface.amount--;
							cargoCash -= commodityItemUserInterface.item.priceSell;
							cargoSpace--;
							update();
						}
					});
				}
			}
		}

		rootTable.row();
		rootTable.add(new Label("Remaining Cash", skin));
		labelCash = new Label("", skin);
		rootTable.add(labelCash).right();
		
		rootTable.row();
		rootTable.add(new Label("Remaining Cargo Space", skin));
		labelCargo = new Label("", skin);
		rootTable.add(labelCargo).right();
		
		rootTable.row().padTop(20);
		rootTable.add(button(I18N.OK, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				buyCargo();
				game.setScreen(new LandScreen(infiniteSpaceGame, node));
			}
		}));
		rootTable.add(button(I18N.CANCEL, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				game.setScreen(new LandScreen(infiniteSpaceGame, node));
			}
		}));

		stage.addActor(rootTable);
		
		update();
	}
	
	private void update() {
		long availableCash = GameState.INSTANCE.cash - cargoCash;
		labelCash.setText(Units.moneyToString(availableCash));
		int maxCargoSpace = GameState.INSTANCE.ship.cargoSpace - GameState.INSTANCE.cargo.size();
		long availableCargoSpace = maxCargoSpace - cargoSpace;
		labelCargo.setText(Units.toString(availableCargoSpace));
		
		for (CommodityItemUserInterface commodityItemUserInterface: commodityItemUserInterfaces) {
			int availableAmount = commodityItemUserInterface.item.amount - commodityItemUserInterface.amount;
			commodityItemUserInterface.labelAvailableAmount.setText(Units.toString(availableAmount));
			commodityItemUserInterface.buttonPlus.setDisabled(availableCargoSpace == 0 || availableAmount == 0 || availableCash < commodityItemUserInterface.item.priceSell);
			commodityItemUserInterface.buttonMinus.setDisabled(commodityItemUserInterface.amount == 0);
			commodityItemUserInterface.labelAmount.setText(Units.toString(commodityItemUserInterface.amount));
			commodityItemUserInterface.labelCash.setText(Units.moneyToString(commodityItemUserInterface.amount * commodityItemUserInterface.item.priceSell));
		}
	}

	protected void buyCargo () {
		for (CommodityItemUserInterface commodityItemUserInterface: commodityItemUserInterfaces) {
			if (commodityItemUserInterface.amount > 0) {
				CommodityItem item = new CommodityItem(commodityItemUserInterface.item);
				item.amount = commodityItemUserInterface.amount;
				GameState.INSTANCE.cargo.add(item);
				GameState.INSTANCE.cash -= item.amount * item.priceSell;
			}
		}
		GameState.INSTANCE.save();
	}
	
	private static class CommodityItemUserInterface {
		public CommodityItem item;
		public int amount;
		
		public Label labelAvailableAmount;
		public Button buttonPlus;
		public Button buttonMinus;
		public Label labelAmount;
		public Label labelCash;
	}
}
