package ch.obermuhlner.infinitespace.ui.ship;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.infinitespace.GameState;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.game.ship.CargoBay;
import ch.obermuhlner.infinitespace.game.ship.Hull;
import ch.obermuhlner.infinitespace.game.ship.InternalComponent;
import ch.obermuhlner.infinitespace.game.ship.PassengerBay;
import ch.obermuhlner.infinitespace.game.ship.PowerPlant;
import ch.obermuhlner.infinitespace.game.ship.ShieldGenerator;
import ch.obermuhlner.infinitespace.game.ship.Ship;
import ch.obermuhlner.infinitespace.game.ship.ShipComponent;
import ch.obermuhlner.infinitespace.game.ship.ShipPart;
import ch.obermuhlner.infinitespace.game.ship.Thruster;
import ch.obermuhlner.infinitespace.game.ship.Weapon;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.ui.AbstractStageScreen;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class AbstractShipComponentScreen<T extends ShipComponent> extends AbstractStageScreen {

	protected ShipPart<T> part;
	protected Node node;
	
	protected String componentType;
	
	private List<T> boughtComponents = new ArrayList<T>();
	private List<T> soldComponents = new ArrayList<T>();
	
	private Label labelCash;
	private Label labelTotalMass;
	private Label labelTotalPower;
	private Label labelShield;
	private Label labelCargoSpace;
	private Label labelPassengerSpace;

	public AbstractShipComponentScreen (InfiniteSpaceGame game, ShipPart<T> part, String componentType, Node node) {
		super(game);
		
		this.part = part;
		this.componentType = componentType;
		this.node = node;
	}

	protected void addOverviewTable(Table rootTable) {
		Table table2 = table();
		rootTable.row().colspan(2);
		rootTable.add(new ScrollPane(table2, skin));

		Ship ship = GameState.INSTANCE.ship;
		labelCash = addInfoRow(table2, "Remaining Cash");
		addInfoRow(table2, "Allowed Mass").setText(Units.toString(ship.allowedMass));
		labelTotalMass = addInfoRow(table2, "Total Mass");
		labelTotalPower = addInfoRow(table2, "Total Power");
		labelShield = addInfoRow(table2, "Total Shield");
		labelCargoSpace = addInfoRow(table2, "Total Cargo Space");
		labelPassengerSpace = addInfoRow(table2, "Total Passenger Space");
	}

	protected void addTypeSelection(Table rootTable, final Stage stage) {
		rootTable.row();
		final SelectBox<String> selectComponentType = new SelectBox<String>(skin);
		rootTable.add(selectComponentType);
		if (InternalComponent.class.getSimpleName().equals(part.type)) {
			selectComponentType.setItems(CargoBay.class.getSimpleName(), PassengerBay.class.getSimpleName(), PowerPlant.class.getSimpleName(), ShieldGenerator.class.getSimpleName(), Weapon.class.getSimpleName());
			if (componentType.equals(part.type)) {
				componentType = CargoBay.class.getSimpleName();
			}
		} else {
			selectComponentType.setItems(part.type);
		}
		selectComponentType.setSelected(componentType);
		selectComponentType.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				componentType = selectComponentType.getSelected();
				prepareStageRoot(stage);
			}
		});
	}

	private Label addInfoRow (Table table, String text) {
		table.row();
		table.add(new Label(text, skin));
		Label label = new Label("", skin);
		table.add(label).right();
		return label;
	}

	protected void update() {
		long cash = GameState.INSTANCE.cash;
		float mass = GameState.INSTANCE.ship.mass;
		float power = GameState.INSTANCE.ship.power;
		float shield = GameState.INSTANCE.ship.shield;
		int cargoSpace = GameState.INSTANCE.ship.cargoSpace;
		int passengerSpace = GameState.INSTANCE.ship.passengerSpace;
		
		for (ShipComponent component : boughtComponents) {
			cash -= component.price;
			mass += component.mass;
			power += component.power;
			if (component instanceof ShieldGenerator) {
				shield += ((ShieldGenerator)component).shield;
			}
			if (component instanceof CargoBay) {
				cargoSpace += ((CargoBay)component).space;
			}
			if (component instanceof PassengerBay) {
				passengerSpace += ((PassengerBay)component).space;
			}
		}
		
		for (ShipComponent component : soldComponents) {
			cash += component.price;
			mass -= component.mass;
			power -= component.power;
			if (component instanceof ShieldGenerator) {
				shield -= ((ShieldGenerator)component).shield;
			}
			if (component instanceof CargoBay) {
				cargoSpace -= ((CargoBay)component).space;
			}
			if (component instanceof PassengerBay) {
				passengerSpace -= ((PassengerBay)component).space;
			}
		}
		
		labelCash.setText(Units.moneyToString(cash));
		labelTotalMass.setText(Units.toString(mass));
		labelTotalMass.setColor(mass >= GameState.INSTANCE.ship.allowedMass ? Color.RED : Color.GREEN);
		labelTotalPower.setText(Units.toString(power));
		labelTotalPower.setColor(power < 0 ? Color.RED : Color.GREEN);
		labelShield.setText(Units.toString(shield));
		labelCargoSpace.setText(Units.toString(cargoSpace));
		labelPassengerSpace.setText(Units.toString(passengerSpace));
	}
	
	protected void addHeaderRow (Table table, String componentType) {
		table.add("Name", HEADER);
		table.add("Price", HEADER);
		table.add("Mass", HEADER);
		table.add("Power", HEADER);
		
		if (componentType.equals(Hull.class.getSimpleName())) {
			table.add("Type", HEADER);
			table.add("Strength", HEADER);
			table.add("Allowed Mass", HEADER);
		} else if (componentType.equals(CargoBay.class.getSimpleName())) {
			table.add("Volume", HEADER);
			table.add("Cargo Space", HEADER);
		} else if (componentType.equals(PassengerBay.class.getSimpleName())) {
			table.add("Volume", HEADER);
			table.add("Type", HEADER);
			table.add("Passenger Space", HEADER);
		} else if (componentType.equals(PowerPlant.class.getSimpleName())) {
			table.add("Volume", HEADER);
		} else if (componentType.equals(ShieldGenerator.class.getSimpleName())) {
			table.add("Volume", HEADER);
			table.add("Shield", HEADER);
		} else if (componentType.equals(Weapon.class.getSimpleName())) {
			table.add("Volume", HEADER);
			table.add("Type", HEADER);
			table.add("Hit", HEADER);
			table.add("Fire Rate", HEADER);
		} else if (componentType.equals(Thruster.class.getSimpleName())) {
			table.add("Volume", HEADER);
			table.add("Thrust", HEADER);
		}
	}
	
	protected void addComponentRow (Table table, final T component, boolean allowBuy) {
		String componentType = component.getClass().getSimpleName();

		table.row();
		table.add(componentType);
		table.add(Units.moneyToString(component.price));
		table.add(Units.toString(component.mass)).right();
		table.add(Units.toString(component.power)).right();

		if (componentType.equals(Hull.class.getSimpleName())) {
			Hull hull = (Hull) component;
			table.add(hull.type.toString());
			table.add(Units.toString(hull.strength)).right();
			table.add(Units.kilogramsToString(hull.allowedMass)).right();
		} else if (componentType.equals(CargoBay.class.getSimpleName())) {
			CargoBay cargoBay = (CargoBay) component;
			table.add(Units.toString(cargoBay.volume)).right();
			table.add(Units.toString(cargoBay.space)).right();
		} else if (componentType.equals(PassengerBay.class.getSimpleName())) {
			PassengerBay passengerBay = (PassengerBay) component;
			table.add(Units.toString(passengerBay.volume)).right();
			table.add(passengerBay.type.toString());
			table.add(Units.toString(passengerBay.space)).right();
		} else if (componentType.equals(PowerPlant.class.getSimpleName())) {
			PowerPlant powerPlant = (PowerPlant) component;
			table.add(Units.toString(powerPlant.volume)).right();
		} else if (componentType.equals(ShieldGenerator.class.getSimpleName())) {
			ShieldGenerator shieldGenerator = (ShieldGenerator)component;
			table.add(Units.toString(shieldGenerator.volume)).right();
			table.add(Units.toString(shieldGenerator.shield)).right();
		} else if (componentType.equals(Weapon.class.getSimpleName())) {
			Weapon weapon = (Weapon) component;
			table.add(Units.toString(weapon.volume)).right();
			table.add(weapon.type.toString());
			table.add(Units.toString(weapon.hit)).right();
			table.add(Units.toString(weapon.fireRate)).right();
		} else if (componentType.equals(Thruster.class.getSimpleName())) {
			Thruster thruster = (Thruster) component;
			table.add(Units.toString(thruster.volume)).right();
			table.add(Units.toString(thruster.thrust)).right();
		}

		if (allowBuy) {
			table.add(button("Buy", new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					boughtComponents.add(component);
					update();
				}
			}));
		}
	}

	protected void buySellComponents () {
		long cash = GameState.INSTANCE.cash;
		for (ShipComponent component : soldComponents) {
			cash += component.price;
		}
		for (ShipComponent component : boughtComponents) {
			cash -= component.price;
		}
		
		GameState.INSTANCE.cash = cash;
		
		part.components.removeAll(soldComponents);
		part.components.addAll(boughtComponents);
		
		GameState.INSTANCE.ship.update();
		GameState.INSTANCE.save();
	}

}
