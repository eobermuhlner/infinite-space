package ch.obermuhlner.infinitespace.ui.info;

import ch.obermuhlner.infinitespace.I18N;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.OrbitingSpheroidNode;
import ch.obermuhlner.infinitespace.model.universe.AsteroidBelt;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.model.universe.population.Industry;
import ch.obermuhlner.infinitespace.ui.AbstractGameScreen;
import ch.obermuhlner.infinitespace.ui.AbstractNodeStageScreen;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class InfoScreen extends AbstractNodeStageScreen {

	private AbstractGameScreen backScreen;

	public InfoScreen (InfiniteSpaceGame game, Node node, AbstractGameScreen backScreen) {
		super(game, node);
		this.backScreen = backScreen;
	}

	@Override
	protected void prepareStage (Stage stage, Table rootTable) {
		rootTable.row();
		rootTable.add(new Label("Info " + node.getName(), skin, TITLE));
		
		Table tablePhysics = table();
		rootTable.row();
		rootTable.add(new ScrollPane(tablePhysics, skin));

		Table tablePopulation = table();
		rootTable.row();
		rootTable.add(new ScrollPane(tablePopulation, skin));

		addRow(tablePhysics, "Name", node.getFullName());

		if (node instanceof Star) {
			Star star = (Star)node;
			addRow(tablePhysics, "Type", star.type);
			addRow(tablePhysics, "Temperature", Units.celsiusToString(star.temperature));
		}
		
		if (node instanceof Planet) {
			Planet planet = (Planet)node;
			addRow(tablePhysics, "Type", planet.type);
			addRow(tablePhysics, "Breathable Atmosphere", planet.breathableAtmosphere);
			addRow(tablePhysics, "Water", Units.percentToString(planet.water));
			addRow(tablePhysics, "Supports Life", planet.supportsLife);
			addRow(tablePhysics, "Has Life", planet.hasLife);
		}

		if (node instanceof AsteroidBelt) {
			AsteroidBelt asteroidBelt = (AsteroidBelt)node;
			addRow(tablePhysics, "Width", Units.meterSizeToString(asteroidBelt.width));
			addRow(tablePhysics, "Height", Units.meterSizeToString(asteroidBelt.height));
			addRow(tablePhysics, "Density", Units.toString(asteroidBelt.density) + "1/m^3");
			addRow(tablePhysics, "Average Radius", Units.meterSizeToString(asteroidBelt.averageRadius));
		}

		if (node instanceof SpaceStation) {
			SpaceStation spaceStation = (SpaceStation)node;
			addRow(tablePhysics, "Type", spaceStation.type);
			addRow(tablePhysics, "Width", Units.meterSizeToString(spaceStation.width));
			addRow(tablePhysics, "Height", Units.meterSizeToString(spaceStation.height));
			addRow(tablePhysics, "Length", Units.meterSizeToString(spaceStation.length));
			addRow(tablePhysics, "Starport", spaceStation.starport);
		}

		if (node instanceof OrbitingSpheroidNode) {
			OrbitingSpheroidNode orbitingSpheroidNode = (OrbitingSpheroidNode)node;
			addRow(tablePhysics, "Radius", Units.meterSizeToString(orbitingSpheroidNode.radius));
		}

		if (node instanceof OrbitingNode) {
			OrbitingNode orbitingNode = (OrbitingNode)node;
			addRow(tablePhysics, "Mass", Units.kilogramsToString(orbitingNode.mass));
			addRow(tablePhysics, "Orbit Radius", Units.meterOrbitToString(orbitingNode.orbitRadius));
			addRow(tablePhysics, "Orbit Period", Units.secondsToString(orbitingNode.orbitPeriod));
			addRow(tablePhysics, "Rotation Period", Units.secondsToString(orbitingNode.rotation));
			if (orbitingNode.population != null) {
				addRow(tablePopulation, "Population", Units.toString(orbitingNode.population.population));
				addRow(tablePopulation, "TechLevel", orbitingNode.population.techLevel.toString());
				for (Industry industry : Industry.values()) {
					if (orbitingNode.population.industry.containsKey(industry)) {
						addRow(tablePopulation, industry.toString(), Units.percentToString(orbitingNode.population.industry.get(industry)));
					}
				}
			}
		}
		
		rootTable.row().padTop(20);
		rootTable.add(button(I18N.OK, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				setScreen(backScreen);
			}
		}));
	}
	
	private void addRow(Table table, String label, Object data) {
		table.row();
		table.add(new Label(label, skin));
		table.add(new Label(data.toString(), skin));
	}

}
