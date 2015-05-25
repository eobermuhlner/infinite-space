package ch.obermuhlner.infinitespace.ui.ship;

import ch.obermuhlner.infinitespace.GameState;
import ch.obermuhlner.infinitespace.I18N;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.game.ship.Hull;
import ch.obermuhlner.infinitespace.game.ship.Ship;
import ch.obermuhlner.infinitespace.game.ship.ShipComponent;
import ch.obermuhlner.infinitespace.game.ship.ShipPart;
import ch.obermuhlner.infinitespace.game.ship.Thruster;
import ch.obermuhlner.infinitespace.game.ship.Weapon;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.ui.AbstractNodeStageScreen;
import ch.obermuhlner.infinitespace.ui.land.LandScreen;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class ShipInfoScreen extends AbstractNodeStageScreen {

	private Table tableComponents;
	
	private Label labelPartComponentCount;
	private Label labelPartMaxComponentVolume;
	private Label labelPartMaxTotalVolume;
	
	public ShipInfoScreen (InfiniteSpaceGame game, Node node) {
		super(game, node);
	}

	@Override
	protected void prepareStage (Stage stage, Table rootTable) {
		Ship ship = GameState.INSTANCE.ship;

		rootTable.row();
		rootTable.add(new Label("Ship Info", skin, TITLE)).colspan(4);

		Table tableTotalInfo = table();
		rootTable.row().colspan(2);
		rootTable.add(new ScrollPane(tableTotalInfo, skin));

		addRow(tableTotalInfo, "Allowed Mass", ship.allowedMass);
		addRow(tableTotalInfo, "Total Mass", ship.mass, ship.mass <= ship.allowedMass);
		addRow(tableTotalInfo, "Total Power", ship.power, ship.power >= 0);
		addRow(tableTotalInfo, "Total Shield", ship.shield);
		addRow(tableTotalInfo, "Total Cargo Space", ship.cargoSpace);
		addRow(tableTotalInfo, "Total Passenger Space", ship.passengerSpace);

		rootTable.row();
		final SelectBox<String> selectPartType = new SelectBox<String>(skin);
		rootTable.add(selectPartType);
		selectPartType.setItems(getPartTypeNames(ship));
		
		Table tablePart = table();
		rootTable.row();
		rootTable.add(new ScrollPane(tablePart, skin));

		tablePart.row();
		tablePart.add(new Label("Components", skin));
		labelPartComponentCount = new Label("", skin);
		tablePart.add(labelPartComponentCount).right();
		
		tablePart.row();
		tablePart.add(new Label("Max Component Volume", skin));
		labelPartMaxComponentVolume = new Label("", skin);
		tablePart.add(labelPartMaxComponentVolume).right();
		
		tablePart.row();
		tablePart.add(new Label("Max Total Volume", skin));
		labelPartMaxTotalVolume = new Label("", skin);
		tablePart.add(labelPartMaxTotalVolume).right();
		
		tableComponents = table();
		rootTable.row();
		rootTable.add(new ScrollPane(tableComponents, skin));
				
		updateTableComponents(selectPartType.getSelectedIndex());
		selectPartType.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				updateTableComponents(selectPartType.getSelectedIndex());
			}
		});
		
		rootTable.row().padTop(20);
		rootTable.add(button(I18N.OK, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				game.setScreen(new LandScreen(infiniteSpaceGame, node));
			}
		}));

		stage.addActor(rootTable);
	}

	private void updateTableComponents (int partTypeIndex) {
		Ship ship = GameState.INSTANCE.ship;

		tableComponents.clear();
		
		ShipPart part = ship.parts.get(partTypeIndex);

		if (part.minCount == part.maxCount) {
			labelPartComponentCount.setText(Units.toString(part.minCount));
		} else {
			labelPartComponentCount.setText(Units.toString(part.minCount) + " - " + Units.toString(part.maxCount));
		}
		labelPartMaxComponentVolume.setText(Units.toString(part.maxComponentVolume));
		labelPartMaxComponentVolume.setText(Units.toString(part.maxTotalVolume));

		int columnCount = 0;
		
		tableComponents.row();
		tableComponents.add(new Label("Type", skin, HEADER));
		tableComponents.add(new Label("Name", skin, HEADER));
		tableComponents.add(new Label("Price", skin, HEADER));
		tableComponents.add(new Label("Mass", skin, HEADER));
		tableComponents.add(new Label("Power", skin, HEADER));
		tableComponents.add(new Label("Volume", skin, HEADER));
		columnCount += 6;
		
		if (part.types.contains(Hull.class.getSimpleName(), false)) {
			tableComponents.add(new Label("Strength", skin, HEADER));
			tableComponents.add(new Label("Allowed Mass", skin, HEADER));
			columnCount += 2;
		} else if (part.types.contains(Weapon.class.getSimpleName(), false)) {
			tableComponents.add(new Label("Hit", skin, HEADER));
			tableComponents.add(new Label("Fire Rate", skin, HEADER));
			columnCount += 2;
		}

		if (part.components.size < part.maxCount) {
			addBuyComponentRow(tableComponents, part, columnCount);
		}

		for (ShipComponent component : part.components) {
			addSellReplaceComponentRow(tableComponents, "", part, component, true);
		}
	}

	private String[] getPartTypeNames (Ship ship) {
		String[] result = new String[ship.parts.size()];
		for (int i = 0; i < ship.parts.size(); i++) {
			result[i] = ship.parts.get(i).name;
		}
		return result;
	}

	private void addSellReplaceComponentRow (Table table, String name, ShipPart part, final ShipComponent component, boolean allowSell) {
		table.row();
		table.add(component.getClass().getSimpleName());
		table.add(name);
		table.add(Units.moneyToString(component.price));
		table.add(Units.toString(component.mass)).right();
		table.add(Units.toString(component.power)).right();
		table.add(Units.volumeToString(component.volume)).right();
		
		if (component instanceof Thruster) {
			Thruster thruster = (Thruster)component;
			table.add(Units.toString(thruster.thrust)).right();
		} else if (component instanceof Hull) {
			Hull hull = (Hull)component;
			table.add(Units.toString(hull.strength)).right();
			table.add(Units.toString(hull.allowedMass)).right();
		} else if (component instanceof Weapon) {
			Weapon weapon = (Weapon)component;
			table.add(Units.toString(weapon.hit)).right();
			table.add(Units.toString(weapon.fireRate)).right();
		}
		
		boolean canSell = part == null || part.minCount < part.components.size;
		Button buttonSell = button("Sell", new SellShipComponentScreen(infiniteSpaceGame, component, this));
		buttonSell.setDisabled(!allowSell || !canSell);
		table.add(buttonSell);
		
		table.add(button("Replace", new ReplaceShipComponentScreen(infiniteSpaceGame, part, component, node)));
	}

	private void addBuyComponentRow (Table table, ShipPart part, int columnCount) {
		table.row();
		table.add(part.name);
		String partType = part.types.size == 1 ? part.types.get(0) : "...";
		table.add(partType).colspan(columnCount - 1);
		
		Button buttonBuy = button("Buy", new BuyShipComponentScreen(infiniteSpaceGame, part, node));
		table.add(buttonBuy);
	}

	private void addRow (Table table, String label, float value) {
		addRow(table, label, value, "default");
	}
	
	private void addRow (Table table, String label, float value, boolean good) {
		addRow(table, label, value, good ? "good" : "bad");
	}
	
	private void addRow (Table table, String label, float value, String style) {
		table.row();
		table.add(new Label(label, skin)).colspan(2);
		table.add(new Label(Units.toString(value), skin, style)).right();
	}
}
