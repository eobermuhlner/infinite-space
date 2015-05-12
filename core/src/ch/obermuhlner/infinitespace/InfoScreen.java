package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.OrbitingSpheroidNode;
import ch.obermuhlner.infinitespace.model.universe.AsteroidBelt;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.model.universe.population.Industry;
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
		
		Table table = table();
		rootTable.row();
		rootTable.add(new ScrollPane(table, skin));

		addRow(table, "Name", node.getFullName());

		if (node instanceof Star) {
			Star star = (Star)node;
			addRow(table, "Type", star.type);
			addRow(table, "Temperature", Units.celsiusToString(star.temperature));
		}
		
		if (node instanceof Planet) {
			Planet planet = (Planet)node;
			addRow(table, "Type", planet.type);
			addRow(table, "Breathable Atmosphere", planet.breathableAtmosphere);
			addRow(table, "Water", Units.percentToString(planet.water));
			addRow(table, "Supports Life", planet.supportsLife);
			addRow(table, "Has Life", planet.hasLife);
		}

		if (node instanceof AsteroidBelt) {
			AsteroidBelt asteroidBelt = (AsteroidBelt)node;
			addRow(table, "Width", Units.meterSizeToString(asteroidBelt.width));
			addRow(table, "Height", Units.meterSizeToString(asteroidBelt.height));
			addRow(table, "Density", Units.toString(asteroidBelt.density) + "1/m^3");
			addRow(table, "Average Radius", Units.meterSizeToString(asteroidBelt.averageRadius));
		}

		if (node instanceof SpaceStation) {
			SpaceStation spaceStation = (SpaceStation)node;
			addRow(table, "Type", spaceStation.type);
			addRow(table, "Width", Units.meterSizeToString(spaceStation.width));
			addRow(table, "Height", Units.meterSizeToString(spaceStation.height));
			addRow(table, "Length", Units.meterSizeToString(spaceStation.length));
			addRow(table, "Starport", spaceStation.starport);
		}

		if (node instanceof OrbitingSpheroidNode) {
			OrbitingSpheroidNode orbitingSpheroidNode = (OrbitingSpheroidNode)node;
			addRow(table, "Radius", Units.meterSizeToString(orbitingSpheroidNode.radius));
		}

		if (node instanceof OrbitingNode) {
			OrbitingNode orbitingNode = (OrbitingNode)node;
			addRow(table, "Mass", Units.kilogramsToString(orbitingNode.mass));
			addRow(table, "Orbit Radius", Units.meterOrbitToString(orbitingNode.orbitRadius));
			addRow(table, "Orbit Period", Units.secondsToString(orbitingNode.orbitPeriod));
			addRow(table, "Rotation Period", Units.secondsToString(orbitingNode.rotation));
			if (orbitingNode.population != null) {
				addRow(table, "Population", Units.toString(orbitingNode.population.population));
				addRow(table, "TechLevel", orbitingNode.population.techLevel.toString());
				for (Industry industry : Industry.values()) {
					if (orbitingNode.population.industry.containsKey(industry)) {
						addRow(table, industry.toString(), Units.percentToString(orbitingNode.population.industry.get(industry)));
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
