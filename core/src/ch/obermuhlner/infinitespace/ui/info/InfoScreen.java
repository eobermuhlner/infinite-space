package ch.obermuhlner.infinitespace.ui.info;

import java.util.Map;

import ch.obermuhlner.infinitespace.Config;
import ch.obermuhlner.infinitespace.I18N;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.UserData;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.OrbitingSpheroidNode;
import ch.obermuhlner.infinitespace.model.generator.Generator;
import ch.obermuhlner.infinitespace.model.universe.AsteroidBelt;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.model.universe.StarSystem;
import ch.obermuhlner.infinitespace.model.universe.Universe;
import ch.obermuhlner.infinitespace.model.universe.population.Industry;
import ch.obermuhlner.infinitespace.ui.AbstractGameScreen;
import ch.obermuhlner.infinitespace.ui.AbstractNodeStageScreen;
import ch.obermuhlner.infinitespace.util.Molecule;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class InfoScreen extends AbstractNodeStageScreen {

	private AbstractGameScreen backScreen;
	
	private static long debugUniverseIndex = 0;
	private static int debugPlanetIndex = 3;
	private static String debugNodeType = "SpaceStation";

	public InfoScreen (InfiniteSpaceGame game, Node node, AbstractGameScreen backScreen) {
		super(game, node);
		this.backScreen = backScreen;
	}

	@Override
	protected void prepareStage (final Stage stage, Table rootTable) {
		if (Config.DEBUG_TEST_GENERATOR) {
			rootTable.row();

			Table table = table();
			rootTable.add(table);
			
			table.add(button("Prev", new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					debugUniverseIndex--;
					updateRandomDebugInfoScreen();
				}
			}));
			
			table.add(new Label(String.valueOf(debugUniverseIndex), skin));

			table.add(button("Next", new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					debugUniverseIndex++;
					updateRandomDebugInfoScreen();
				}
			}));

			final SelectBox<String> selectNodeType = new SelectBox<String>(skin);
			selectNodeType.setItems("Star", "Planet", "Moon", "SpaceStation");
			selectNodeType.setSelected(debugNodeType);
			selectNodeType.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					debugNodeType = selectNodeType.getSelected();
					updateRandomDebugInfoScreen();
				}
			});
			table.add(selectNodeType);
			
			final SelectBox<Integer> selectPlanetIndex = new SelectBox<Integer>(skin);
			selectPlanetIndex.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9);
			selectPlanetIndex.setSelected(debugPlanetIndex);
			selectPlanetIndex.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					debugPlanetIndex = selectPlanetIndex.getSelected();
					updateRandomDebugInfoScreen();
				}
			});
			table.add(selectPlanetIndex);
		}
		
		rootTable.row();
		rootTable.add(new Label(node.getName(), skin, TITLE));

		Array<String> modelInstanceNames = getModelInstanceNames();
		if (modelInstanceNames.size > 0) {
			Table tableNames = table();
			rootTable.row();
			rootTable.add(new ScrollPane(tableNames, skin));
			
			for (final String name : modelInstanceNames) {
				visibleModelInstanceNames.add(name);

				tableNames.row();
				final CheckBox checkBoxName = new CheckBox(name, skin);
				tableNames.add(checkBoxName);
				checkBoxName.setChecked(true);
				checkBoxName.addListener(new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						if (checkBoxName.isChecked()) {
							visibleModelInstanceNames.add(name);
						} else {
							visibleModelInstanceNames.remove(name);
						}
					}
				});
				Button buttonDetails = new TextButton("Details", skin);
				tableNames.add(buttonDetails);
				buttonDetails.addListener(new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						showModelDetailWindow(stage, name);
					}

				});
			}
		}
		
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
			addRow(tablePhysics, "Temperature", Units.kelvinToString(star.temperature));
		}
		
		if (node instanceof Planet) {
			Planet planet = (Planet)node;
			addRow(tablePhysics, "Type", planet.type);
			addRow(tablePhysics, "Temperature", Units.kelvinToString(planet.temperature));
			addRow(tablePhysics, "Atmosphere", Units.atmosphereToString(planet.atmosphere));
			addRow(tablePhysics, "Atmosphere Pressure", Units.pascalToString(planet.atmospherePressure));
			addRow(tablePhysics, "Breathable Atmosphere", planet.breathableAtmosphere);
			addRow(tablePhysics, "Water", Units.percentToString(planet.water));
			addRow(tablePhysics, "Supports Life", planet.supportsLife);
			addRow(tablePhysics, "Has Life", planet.hasLife);
		}

		if (node instanceof AsteroidBelt) {
			AsteroidBelt asteroidBelt = (AsteroidBelt)node;
			addRow(tablePhysics, "Width", Units.meterSizeToString(asteroidBelt.width));
			addRow(tablePhysics, "Height", Units.meterSizeToString(asteroidBelt.height));
			addRow(tablePhysics, "Density", Units.toString(asteroidBelt.density) + "/m^3");
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
	
	private void showModelDetailWindow(final Stage stage, final String name) {
		final Window window = new Window(name, skin);

		boolean hasInfo = false;
		UserData userData = findUserData(name);
		if (userData != null) {
			if (userData.description != null) {
				Label labelText = new Label(userData.description, skin);
				window.row();
				window.add(labelText).left().colspan(2);
				hasInfo = true;
			}
			if (userData.composition != null) {
				for(Map.Entry<Molecule, Double> entry : userData.composition.entrySet()) {
					window.row();
					window.add(new Label(entry.getKey().name(), skin)).left();
					window.add(new Label(Units.percentToString(entry.getValue()), skin)).right();
					hasInfo = true;
				}
			}
		}
		
		if (!hasInfo){
			window.row();
			window.add(new Label("No information found.", skin)).left();
		}
		
		window.row().colspan(2);
		TextButton buttonOk = new TextButton("OK", skin);
		buttonOk.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				window.remove();
			}
		});
		window.add(buttonOk);
		
		window.pack();
		window.setPosition(Math.round((stage.getWidth() - window.getWidth()) / 2), Math.round((stage.getHeight() - window.getHeight()) / 2));
		stage.addActor(window);
	}

	private UserData findUserData(String name) {
		for(Array<ModelInstance> nodeInstances : getRenderState().nodeToInstances.values()) {
			for (int i = 0; i < nodeInstances.size; i++) {
				ModelInstance modelInstance = nodeInstances.get(i);
				if (modelInstance.userData instanceof UserData) {
					UserData userData = (UserData) modelInstance.userData;
					if (name.equals(userData.modelName)) {
						return userData;
					}
				}
			}
		}
		return null;
	}

	private void addRow(Table table, String label, Object data) {
		table.row();
		table.add(new Label(label, skin));
		table.add(new Label(data.toString(), skin));
	}

	private void updateRandomDebugInfoScreen() {
		Generator generator = new Generator();
		Universe universe = generator.generateUniverse(debugUniverseIndex);
		StarSystem starSystem = generator.generateStarSystem(universe, 0);
		Star star = generator.generateStar(starSystem, 0);
		Planet planet = generator.generatePlanet(star, debugPlanetIndex);
		Planet moon = generator.generatePlanet(planet, 0);
		SpaceStation spaceStation = generator.generateSpaceStation(moon, 0);
		
		Node randomNode = spaceStation;
		if (debugNodeType.equals("Star")) {
			randomNode = star;
		} else if (debugNodeType.equals("Planet")) {
			randomNode = planet;
		} else if (debugNodeType.equals("Moon")) {
			randomNode = moon;
		} else if (debugNodeType.equals("SpaceStation")) {
			randomNode = spaceStation;
		}
		setScreen(new InfoScreen(infiniteSpaceGame, randomNode, backScreen));
	}
}
