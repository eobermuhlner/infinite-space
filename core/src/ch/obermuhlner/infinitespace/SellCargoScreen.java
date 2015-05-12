package ch.obermuhlner.infinitespace;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.generator.Generator;
import ch.obermuhlner.infinitespace.model.random.Random;
import ch.obermuhlner.infinitespace.model.universe.population.Population;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class SellCargoScreen extends AbstractNodeStageScreen {

	private Label labelCash;
	private Label labelCargo;
	
	private List<CommodityItemUserInterface> commodityItemUserInterfaces = new ArrayList<CommodityItemUserInterface>();
	
	private long cargoCash = 0;
	private long cargoSpace = 0;

	public SellCargoScreen (InfiniteSpaceGame game, Node node) {
		super(game, node);
	}

	@Override
	protected void prepareStage (Stage stage, Table rootTable) {
		updateCargoPrices();
		
		rootTable.row();
		rootTable.add(new Label("Sell Cargo: " + node.getName(), skin, TITLE)).colspan(2);

		Table table = table();
		rootTable.row().colspan(2);
		rootTable.add(new ScrollPane(table, skin));
		
		table.row();
		table.add(new Label("Name", skin));
		table.add(new Label("Type", skin));
		table.add(new Label("Cargo", skin));
		table.add(new Label("Original Price", skin));
		table.add(new Label("Price", skin));
		table.add(new Label("Sell -", skin));
		table.add(new Label("Sell +", skin));
		table.add(new Label("Amount", skin));
		table.add(new Label("Total Price", skin));

		if (node instanceof OrbitingNode) {
			OrbitingNode orbitingNode = (OrbitingNode)node;
			Population population = orbitingNode.population;
			if (population != null) {
				for (CommodityItem item : GameState.INSTANCE.cargo) {
					final CommodityItemUserInterface commodityItemUserInterface = new CommodityItemUserInterface();
					commodityItemUserInterfaces.add(commodityItemUserInterface);
					commodityItemUserInterface.item = item;
					
					table.row();
					table.add(new Label(item.name, skin));
					table.add(new Label(item.commodity.toString(), skin));
					commodityItemUserInterface.labelAvailableAmount = new Label("", skin);
					table.add(new Label(Units.toString(item.amount), skin)).right();
					table.add(new Label(Units.moneyToString(item.priceSell), skin)).right();
					table.add(new Label(Units.moneyToString(item.priceBuy), skin)).right();
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
							cargoCash += commodityItemUserInterface.item.priceBuy;
							cargoSpace--;
							update();
						}
					});
					commodityItemUserInterface.buttonMinus.addListener(new ChangeListener() {
						@Override
						public void changed (ChangeEvent event, Actor actor) {
							commodityItemUserInterface.amount--;
							cargoCash -= commodityItemUserInterface.item.priceBuy;
							cargoSpace++;
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
				sellCargo();
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
	
	private void updateCargoPrices () {
		Population population = getNodePopulation();
		Random random = node.seed.getRandom();
		for(CommodityItem item : GameState.INSTANCE.cargo) {
			if (population != null) {
				double price = Generator.calculatePrice(item, node, population);
				item.priceBuy = Generator.calculatePriceBuy(price, population);
			} else {
				item.priceBuy = 0;
			}
		}
	}

	private Population getNodePopulation () {
		if (node instanceof OrbitingNode) {
			OrbitingNode orbitingNode = (OrbitingNode)node;
			Population population = orbitingNode.population;
			return population;
		}
		return null;
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
			commodityItemUserInterface.buttonPlus.setDisabled(availableAmount == 0);
			commodityItemUserInterface.buttonMinus.setDisabled(commodityItemUserInterface.amount == 0);
			commodityItemUserInterface.labelAmount.setText(Units.toString(commodityItemUserInterface.amount));
			commodityItemUserInterface.labelCash.setText(Units.moneyToString(commodityItemUserInterface.amount * commodityItemUserInterface.item.priceBuy));
		}
	}

	protected void sellCargo () {
		for (CommodityItemUserInterface commodityItemUserInterface: commodityItemUserInterfaces) {
			if (commodityItemUserInterface.amount > 0) {
				GameState.INSTANCE.cash += commodityItemUserInterface.item.amount * commodityItemUserInterface.item.priceBuy;
				commodityItemUserInterface.item.amount -= commodityItemUserInterface.amount;
				if (commodityItemUserInterface.item.amount == 0) {
					GameState.INSTANCE.cargo.remove(commodityItemUserInterface.item);
				}
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
