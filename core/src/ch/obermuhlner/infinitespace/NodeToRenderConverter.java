
package ch.obermuhlner.infinitespace;

import java.util.HashMap;
import java.util.Map;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.OrbitingSpheroidNode;
import ch.obermuhlner.infinitespace.model.random.Random;
import ch.obermuhlner.infinitespace.model.universe.AsteroidBelt;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.Planet.PartInfo;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.render.ColorArrayAttribute;
import ch.obermuhlner.infinitespace.render.FloatArrayAttribute;
import ch.obermuhlner.infinitespace.render.TerrestrialPlanetFloatAttribute;
import ch.obermuhlner.infinitespace.render.UberShaderProvider;
import ch.obermuhlner.infinitespace.util.MathUtil;
import ch.obermuhlner.infinitespace.util.StopWatch;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class NodeToRenderConverter {

	private static final boolean RENDER_PROCEDURAL_SHADERS_TO_TEXTURES = true;

	private static final double PROBABILITY_GENERATED_PLANET = 1.0;

	private static final double SUN_RADIUS = Units.SUN_RADIUS;

	private static final double LAVA_TEMPERATURE = Units.celsiusToKelvin(700);

	private static final double AU = Units.ASTRONOMICAL_UNIT;
	
	public static double SIZE_FACTOR = 1 / SUN_RADIUS / 10;
	private static double SIZE_STAR_ZOOM_FACTOR = 10;
	private static double SIZE_ZOOM_FACTOR = 10;
	private static float SIZE_MOON_ORBIT_ZOOM_FACTOR = 10;
	
	private static final int PLANET_SPHERE_DIVISIONS_U = 30;
	private static final int PLANET_SPHERE_DIVISIONS_V = 30;

	private static final int STATION_SPHERE_DIVISIONS_U = 10;
	private static final int STATION_SPHERE_DIVISIONS_V = 10;

	private final Map<Class<? extends Node>, NodeConverter<? extends Node>> nodeConverters = new HashMap<Class<? extends Node>, NodeConverter<? extends Node>>();

	private final AssetManager assetManager;

	private final ModelBuilder modelBuilder = new ModelBuilder();

	public NodeToRenderConverter (AssetManager assetManager) {
		this.assetManager = assetManager;

		nodeConverters.put(Star.class, new StarConverter());
		nodeConverters.put(Planet.class, new PlanetConverter());
		nodeConverters.put(AsteroidBelt.class, new AsteroidBeltConverter());
		nodeConverters.put(SpaceStation.class, new SpaceStationConverter());
	}

	public void convertNode (Node node, RenderState renderState) {
		convertNode(node, renderState, false);
	}
	
	public void convertNodes (Iterable<Node> universe, RenderState renderState) {
		StopWatch stopWatch = new StopWatch();
		
		{
			// create grid
			Material material = new Material(ColorAttribute.createDiffuse(new Color(0, 0.2f, 0, 1f)));
			Model gridModel = modelBuilder.createLineGrid(2000, 2000, 5f, 5f, material, Usage.Position);
			renderState.instancesAlways.add(new ModelInstance(gridModel));
		}
		
		int nodeCount = 0;
		for (Node node : universe) {
			convertNode(node, renderState, true);
			nodeCount++;
		}

		createSkyBox(renderState);
		
		System.out.println("Converted " + nodeCount + " nodes to rendering in " + stopWatch);
	}

	private void createSkyBox(RenderState renderState) {
		float boxSize = 500f;
		Vector3 v000 = new Vector3(-boxSize, -boxSize, -boxSize);
		Vector3 v001 = new Vector3(-boxSize, -boxSize, boxSize);
		Vector3 v010 = new Vector3(-boxSize, boxSize, -boxSize);
		Vector3 v011 = new Vector3(-boxSize, boxSize, boxSize);
		Vector3 v100 = new Vector3(boxSize, -boxSize, -boxSize);
		Vector3 v101 = new Vector3(boxSize, -boxSize, boxSize);
		Vector3 v110 = new Vector3(boxSize, boxSize, -boxSize);
		Vector3 v111 = new Vector3(boxSize, boxSize, boxSize);
		
		createSkyBoxRect(renderState, "skybox_neg_x.png", v001, v000, v010, v011);
		createSkyBoxRect(renderState, "skybox_pos_x.png", v100, v101, v111, v110);
		createSkyBoxRect(renderState, "skybox_neg_y.png", v100, v000, v001, v101);
		createSkyBoxRect(renderState, "skybox_pos_y.png", v111, v011, v010, v110);
		createSkyBoxRect(renderState, "skybox_neg_z.png", v000, v100, v110, v010);
		createSkyBoxRect(renderState, "skybox_pos_z.png", v101, v001, v011, v111);
	}
	
	private void createSkyBoxRect(RenderState renderState, String textureName, Vector3 corner1, Vector3 corner2, Vector3 corner3, Vector3 corner4) {
		Texture texture = assetManager.get(InfiniteSpaceGame.getTexturePath(textureName), Texture.class);
		Material material = new Material(new TextureAttribute(TextureAttribute.Emissive, texture), new DepthTestAttribute(0));
		Model model = modelBuilder.createRect(
				corner1.x, corner1.y, corner1.z,
				corner2.x, corner2.y, corner2.z,
				corner3.x, corner3.y, corner3.z,
				corner4.x, corner4.y, corner4.z,
				0, 1, 0,
				material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		ModelInstance instance = new ModelInstance(model);
		renderState.instancesFar.add(instance);
	}


	private <T extends Node> void convertNode (T node, RenderState renderState, boolean realUniverse) {
		@SuppressWarnings("unchecked")
		NodeConverter<T> nodeConverter = (NodeConverter<T>)nodeConverters.get(node.getClass());
		if (nodeConverter != null) {
			//StopWatch stopWatch = new StopWatch();
			nodeConverter.convert(node, renderState, realUniverse);
			//System.out.println("Converting to render " + node + " in " + stopWatch);
		}
	}

	private class StarConverter implements NodeConverter<Star> {

		@Override
		public void convert (Star node, RenderState renderState, boolean realUniverse) {
			float radius = calculateStarRadius(node);
			float x = 0;
			float y = 0;
			float z = 0;
			float luminosity = 1.0f;
			// TODO color
			float r2 = 1.0f;
			float g2 = 0.9f;
			float b2 = 0.6f;

			float r1 = r2 * 1.0f;
			float g1 = g2 * 0.7f;
			float b1 = b2 * 0.0f;

			{
				Material material = new Material(new ColorAttribute(ColorAttribute.Diffuse, r1, g1, b1, 1.0f), new ColorAttribute(ColorAttribute.Emissive, r2, g2, b2, 1.0f));
				Model sphereModel = modelBuilder.createSphere(radius, radius, radius, PLANET_SPHERE_DIVISIONS_U, PLANET_SPHERE_DIVISIONS_V,
					material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
				ModelInstance sphere = new ModelInstance(sphereModel);
				UserData userData = new UserData();
				userData.node = node;
				sphere.userData = userData;
				sphere.transform.setToTranslation(0, 0, 0);
				if (GamePreferences.INSTANCE.preferences.getBoolean(GamePreferences.BOOL_PROCEDURAL_TEXTURES)) {
					userData.shaderName = UberShaderProvider.SUN_SHADER;
				}

				renderState.instances.add(sphere);
				
			}

			renderState.environment.add(new PointLight().set(r2, g2, b2, x, y, z, luminosity));
		}
	}

	private static final Color[] GAS_PLANET_COLORS = new Color[] {
		new Color(0.3333f, 0.2222f, 0.1111f, 1.0f),
		new Color(0.8555f, 0.8125f, 0.7422f, 1.0f),
		new Color(0.4588f, 0.4588f, 0.4297f, 1.0f),
		new Color(0.5859f, 0.3906f, 0.2734f, 1.0f),
	};
	private static final Color[] ICE_GAS_PLANET_COLORS = new Color[] {
		new Color(0.6094f, 0.6563f, 0.7695f, 1.0f),
		new Color(0.5820f, 0.6406f, 0.6406f, 1.0f),
		new Color(0.2695f, 0.5234f, 0.9102f, 1.0f),
		new Color(0.3672f, 0.4609f, 0.7969f, 1.0f),
		new Color(0.7344f, 0.8594f, 0.9102f, 1.0f),
	};

	private class PlanetConverter implements NodeConverter<Planet> {

		@Override
		public void convert (Planet node, RenderState renderState, boolean realUniverse) {
			Random random = node.seed.getRandom();
			
			float radius = calculatePlanetRadius(node);
			float orbitRadius = calculateOrbitRadius(node);
			Vector3 position = calculatePosition(node);
			Vector3 parentPosition = calculatePosition(node.parent);

			Array<Attribute> materialAttributes = new Array<Attribute>();
			
			String textureName = node.textureName;
			String shaderName = null;
			Color[] planetColors = null;
			
			if (textureName == null) {
				switch (node.type) {
				case GAS:
					shaderName = UberShaderProvider.GAS_PLANET_SHADER;
					if (node.orbitRadius < 17*AU) {
						planetColors = GAS_PLANET_COLORS;
					} else {
						planetColors = ICE_GAS_PLANET_COLORS;							
					}
					break;
				case STONE:
					if (textureName == null) {
						// TODO use heat of planet to decide whether is is lava
						if (node.temperature > LAVA_TEMPERATURE) {
							textureName = "lava_colors.png";
							shaderName = UberShaderProvider.TERRESTRIAL_PLANET_SHADER;
							materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightFrequency(random.nextFloat(20f, 22f)));
							materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorNoise(random.nextFloat(0.3f, 0.9f)));
							materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorFrequency(random.nextFloat(20f, 30f)));
						}
					}
					
					if (textureName == null && node.radius < 1000E3) {
						textureName = random.next ("phobos.jpg", "deimos.jpg");
					} else {
						if (textureName == null) {
							if (node.atmospherePressure > 0.1) {
								if (node.breathableAtmosphere) {
									if (node.hasLife) {
										textureName = "terrestrial_colors.png";
										float water = (float)node.water;
										float heightMin = MathUtil.transform(0f, 1f, 0.4f, 0.0f, water);
										float heightMax = MathUtil.transform(0f, 1f, 1.0f, 0.6f, water);
										float heightFrequency = random.nextFloat(2f, 15f);
										float iceLevel = MathUtil.transform((float)Units.celsiusToKelvin(-50), (float)Units.celsiusToKelvin(50), 1f, -1f, (float)node.temperature);
										materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightWater(0.4f)); // depends on texture
										materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightMin(heightMin));
										materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightMax(heightMax));
										materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightFrequency(heightFrequency));
										if (heightFrequency < random.nextFloat(5f, 10f)) {
											materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightMountains(random.nextFloat(0.8f, 1.0f)));
										}
										materialAttributes.add(TerrestrialPlanetFloatAttribute.createIceLevel(iceLevel));
										materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorNoise(random.nextFloat(0.1f, 0.3f)));
										materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorFrequency(random.nextFloat(15f, 25f)));
										shaderName = UberShaderProvider.TERRESTRIAL_PLANET_SHADER;
									}
								}
								if (textureName == null && random.nextBoolean(PROBABILITY_GENERATED_PLANET)) {
									textureName = "mars_colors.png";
									materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightFrequency(random.nextFloat(2f, 15f)));
									//materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorNoise(random.nextFloat(0.1f, 0.3f)));
									//materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorFrequency(random.nextFloat(15f, 25f)));
									shaderName = UberShaderProvider.TERRESTRIAL_PLANET_SHADER;
								}
							}
						}

						if (textureName == null && random.nextBoolean(PROBABILITY_GENERATED_PLANET)) {
							textureName = "moon_colors.png";
							float heightRange = random.nextFloat(0.3f, 1.0f);
							float heightMin = random.nextFloat(1.0f - heightRange);
							float heightMax = heightMin + heightRange;
							materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightMin(heightMin));
							materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightMax(heightMax));
							materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightFrequency(random.nextFloat(2f, 15f)));
							//materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorNoise(random.nextFloat(0.1f, 0.3f)));
							//materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorFrequency(random.nextFloat(15f, 25f)));
							shaderName = UberShaderProvider.TERRESTRIAL_PLANET_SHADER;
						}

						if (textureName == null) {
							textureName = random.next ("io.jpg", "callisto.jpg", "ganymede.jpg", "europa.jpg", "mercury.jpg", "mars.jpg", "moon.jpg", "iapetus.jpg");
						}
					}
					break;
				case ICE:
					textureName = random.next ("europa.jpg", "mimas.jpg", "rhea.jpg", "enceladus.jpg", "tethys.jpg", "dione.jpg");
					break;
				}
			}

			//System.out.println("PLANET " + node.seed + " " + node.name + " " + shaderName + " " + textureName + " " + Units.kelvinToString(node.temperature));
			
			{
				Material material;
				if (shaderName == null) {
					Texture texture = assetManager.get(InfiniteSpaceGame.getTexturePath(textureName), Texture.class);
					materialAttributes.add(new TextureAttribute(TextureAttribute.Diffuse, texture));
					material = new Material(materialAttributes);
				} else {
					if (textureName != null) {
						Texture texture = assetManager.get(InfiniteSpaceGame.getTexturePath(textureName), Texture.class);
						materialAttributes.add(new TextureAttribute(TextureAttribute.Diffuse, texture));
					}
					if (planetColors != null) {
						materialAttributes.add(new ColorArrayAttribute(ColorArrayAttribute.PlanetColors, randomPlanetColors(random, planetColors)));
					}
					{
						float floatArray[] = new float[10];
						for (int i = 0; i < floatArray.length; i++) {
							floatArray[i] = random.nextFloat();
						}
						materialAttributes.add(new FloatArrayAttribute(FloatArrayAttribute.FloatArray, floatArray));
					}
					material = new Material(materialAttributes);
					if (RENDER_PROCEDURAL_SHADERS_TO_TEXTURES) {
						StopWatch stopWatch = new StopWatch();
						UserData userData = new UserData();
						userData.shaderName = shaderName;
						Texture texture = renderTexture(material, userData);
						material = new Material(new TextureAttribute(TextureAttribute.Diffuse, texture));
						if (Config.DEBUG_PROFILING) {
							System.out.println("Render " + node + " to texture in " + stopWatch);
						}
					}
				}

				if (! realUniverse) {
					if (node.core != null) {
						for (int i = 0; i < node.core.size; i++) {
							PartInfo coreInfo = node.core.get(i);
							// TODO solve problem with cores z-fighting
							if (coreInfo.radius != node.radius) {
								float coreRadius = calculatePlanetRadius(coreInfo.radius) * 0.95f; // make cores bit smaller to avoid z-fighting with surface 
								ModelInstance sphere = createSphere(renderState, node, coreInfo.name, coreRadius, new Material(temperatureToColorAttribute(coreInfo.temperature)));
								asUserData(sphere).description = coreInfo.description;
								asUserData(sphere).composition = coreInfo.composition;
							}
						}
					}
					
					double gridSize = MathUtil.nextPowerOfTen(node.radius * 2) / 100;
					int gridSteps = (int) Math.round(node.radius * 2 * 1.5 / gridSize);
					float gridRenderSize = calculatePlanetRadius(gridSize);
					Material materialGrid = new Material(ColorAttribute.createDiffuse(new Color(0, 0.2f, 0, 1f)));
					Model gridModel = modelBuilder.createLineGrid(gridSteps, gridSteps, gridRenderSize, gridRenderSize, materialGrid, Usage.Position);
					ModelInstance gridInstance = new ModelInstance(gridModel);
					UserData userData = new UserData();
					userData.modelName = "Grid";
					userData.description = "Grid shows " + Units.meterSizeToString(gridSteps*gridSize) + "\nin steps of " + Units.meterSizeToString(gridSize) + ".";
					gridInstance.userData = userData;
					renderState.instances.add(gridInstance);
				}

				{
					ModelInstance sphere = createSphere(renderState, node, "Planet Surface", radius, material);
					if (realUniverse) {
						sphere.transform.setToTranslation(position);
					}
	
					asUserData(sphere).description = "Temperature: " + Units.kelvinToString(node.temperature);
					if (!RENDER_PROCEDURAL_SHADERS_TO_TEXTURES) {
						asUserData(sphere).shaderName = shaderName;
					}
				}
				
				if (node.breathableAtmosphere) {
					float atmosphereRadiusFactor = 1.01f;
					float atmosphereRadius = radius * atmosphereRadiusFactor; 
					Texture texture = assetManager.get(InfiniteSpaceGame.getTexturePath("clouds.png"), Texture.class);
					float blend = MathUtil.transform(0.0f, 0.7f, 0.0f, 1.0f, (float)node.water);
					Material materialClouds = new Material(new TextureAttribute(TextureAttribute.Diffuse, texture), new BlendingAttribute(blend));
					ModelInstance sphere = createSphere(renderState, node, "Atmosphere", atmosphereRadius, materialClouds);
					asUserData(sphere).description = "Surface pressure: " + Units.pascalToString(node.atmospherePressure);
					asUserData(sphere).composition = node.atmosphere;
					if (realUniverse) {
						sphere.transform.setToTranslation(position);
					}
				}
			}

			createOrbit(renderState, node, orbitRadius, parentPosition);
		}
	}

	private class AsteroidBeltConverter implements NodeConverter<AsteroidBelt> {

		@Override
		public void convert (AsteroidBelt node, RenderState renderState, boolean realUniverse) {
			float radius = calculateOrbitRadius(node);
			Vector3 position = calculatePosition(node.parent);

			float alpha = (float)node.density;

			UserData userData = new UserData();
			userData.shaderName = "ring";

			Material material = new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE), new BlendingAttribute(alpha));
			
			Model model;
			modelBuilder.begin();
			modelBuilder.part("rect-up", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, material)
			.rect(
				radius, 0f, -radius,
				-radius, 0f, -radius,
				-radius, 0f, radius,	
				radius, 0f, radius,
				0f, 1f, 0f);
			modelBuilder.part("rect-down", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, material)
			.rect(
				radius, 0f, radius,
				-radius, 0f, radius,
				-radius, 0f, -radius,
				radius, 0f, -radius,
				0f, -1f, 0f);
			model = modelBuilder.end();

			ModelInstance ring = new ModelInstance(model);
			ring.userData = userData;

			if (realUniverse) {
				ring.transform.setToTranslation(position);
			}
			renderState.instances.add(ring);
		}

}
	
	public static Vector3 calculatePosition(Node node) {
		float x = 0;
		float y = 0;
		float z = 0;

		Node current = node;
		while (current instanceof OrbitingNode) {
			OrbitingNode orbiting = (OrbitingNode) current;
			float orbitRadius = calculateOrbitRadius (orbiting);
			if (Config.DEBUG_ORBIT_LINEUP) {
				x += orbitRadius;
				y += 0;
				z += 0;
			} else {
				x += Math.sin(orbiting.orbitStartAngle) * orbitRadius;
				y += 0;
				z += Math.cos(orbiting.orbitStartAngle) * orbitRadius;
			}
			current = current.parent;
		}
		
		return new Vector3 (x, y, z);
	}

	public ColorAttribute temperatureToColorAttribute(double temperature) {
		if (temperature > 500) {
			return new ColorAttribute(ColorAttribute.Emissive, colorFromRange(Color.RED, Color.YELLOW, 500, 5000, temperature));
		} else {
			return new ColorAttribute(ColorAttribute.Diffuse, colorFromRange(Color.MAROON, Color.RED, 0, 500, temperature));
		}
	}

	private Color colorFromRange(Color startColor, Color endColor, int start, int end, double value) {
		if (value < start) {
			return startColor;
		}
		if (value > end) {
			return endColor;
		}
		float mix = (float) ((value - start) / (end - start)); 
		return new Color(
				(endColor.r - startColor.r) * mix + startColor.r,
				(endColor.g - startColor.g) * mix + startColor.g,
				(endColor.b - startColor.b) * mix + startColor.b,
				(endColor.a - startColor.a) * mix + startColor.a);
	}

	public Texture renderTexture (Material material, UserData userData) {
		final int textureSize = GamePreferences.INSTANCE.preferences.getInteger(GamePreferences.INT_GENERATED_TEXTURES_SIZE);
		
		final int rectSize = 1;
		Model model;
		model = modelBuilder.createRect(
			rectSize, 0f, -rectSize,
			-rectSize, 0f, -rectSize,
			-rectSize, 0f, rectSize,
			rectSize, 0f, rectSize,
			0, 1, 0,
			material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);

		ModelInstance instance = new ModelInstance(model);
		instance.userData = userData;

		ModelBatch modelBatch = new ModelBatch(UberShaderProvider.DEFAULT);

		FrameBuffer frameBuffer = new FrameBuffer(Pixmap.Format.RGB888, textureSize, textureSize, false);
		frameBuffer.begin();

		OrthographicCamera camera = new OrthographicCamera(rectSize*2, rectSize*2);
		camera.position.set(0, 1, 0);
		camera.lookAt(0, 0, 0);
		camera.update();

		modelBatch.begin(camera);
		modelBatch.render(instance);
		modelBatch.end();

		frameBuffer.end();
		
		Texture texture = frameBuffer.getColorBufferTexture();

		model.dispose();
		modelBatch.dispose();
		//frameBuffer.dispose(); // FIXME
		
		return texture;
	}

	public Color[] randomPlanetColors(Random random, Color[] colors) {
		return new Color[] {
			randomGasPlanetColor(random, colors),
			randomGasPlanetColor(random, colors),
			randomGasPlanetColor(random, colors)
		};
	}

	public Color randomGasPlanetColor (Random random, Color[] colors) {
		return randomDeviation(random, colors[random.nextInt(colors.length)]);
	}

	private Color randomDeviation(Random random, Color color) {
		return new Color(
			MathUtil.clamp(color.r * random.nextFloat(0.9f, 1.1f), 0.0f, 1.0f),
			MathUtil.clamp(color.g * random.nextFloat(0.9f, 1.1f), 0.0f, 1.0f),
			MathUtil.clamp(color.b * random.nextFloat(0.9f, 1.1f), 0.0f, 1.0f),
			1.0f);
	}
	
	private class SpaceStationConverter implements NodeConverter<SpaceStation> {

		@Override
		public void convert (SpaceStation node, RenderState renderState, boolean realUniverse) {
			float orbitRadius = calculateOrbitRadius(node);
			Vector3 position = calculatePosition(node);
			Vector3 parentPosition = calculatePosition(node.parent);

			float width = (float)(node.width * SIZE_FACTOR * SIZE_ZOOM_FACTOR);
			float height = (float)(node.height * SIZE_FACTOR * SIZE_ZOOM_FACTOR);
			float length = (float)(node.length * SIZE_FACTOR * SIZE_ZOOM_FACTOR);

			{
				ModelInstance station;
				
				Texture texture = assetManager.get(InfiniteSpaceGame.getTexturePath("spaceship.jpg"), Texture.class);
				Material material = new Material(new TextureAttribute(TextureAttribute.Diffuse, texture));

				Model stationModel;
				switch(node.type) {
				case RING:
				case CYLINDER:
					stationModel = modelBuilder.createCylinder(width, height, length, STATION_SPHERE_DIVISIONS_U, material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
					break;
				case SPHERE:
					stationModel = modelBuilder.createSphere(width, height, length, STATION_SPHERE_DIVISIONS_U, STATION_SPHERE_DIVISIONS_V, material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
					break;
				default:
				case CONGLOMERATE:
				case CUBE:
					stationModel = modelBuilder.createBox(width, height, length, material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
					break;
				}
				station = new ModelInstance(stationModel);
				if (realUniverse) {
					station.transform.setToTranslation(position);
				}

				UserData userData = new UserData();
				userData.node = node;
				station.userData = userData;
				renderState.instances.add(station);
			}
			
			createOrbit(renderState, node, orbitRadius, parentPosition);
		}
	}
	
	public static float calculateRadius(Node node) {
		if (node instanceof Star) {
			return calculateStarRadius((Star) node);
		}
		if (node instanceof Planet) {
			return calculatePlanetRadius((Planet) node);
		}
		if (node instanceof SpaceStation) {
			return calculateSpaceStationRadius((SpaceStation) node);
		}
		return 0.0001f;
		
	}

	public static float calculateStarRadius(Star node) {
		return (float)(node.radius * SIZE_FACTOR * SIZE_STAR_ZOOM_FACTOR);
	}
	
	public static float calculatePlanetRadius(Planet node) {
		return calculatePlanetRadius(node.radius);
	}

	public static float calculatePlanetRadius(double radius) {
		return (float)(radius * SIZE_FACTOR * SIZE_ZOOM_FACTOR);
	}

	private static float calculateSpaceStationRadius (SpaceStation node) {
		return (float) (Math.max(Math.max(node.width, node.height), node.length) * SIZE_FACTOR * SIZE_ZOOM_FACTOR);
	}


	public static float calculateOrbitRadius(OrbitingNode node) {
		float orbitRadius = (float) (node.orbitRadius * SIZE_FACTOR);
		if (node.parent instanceof OrbitingSpheroidNode) {
			OrbitingSpheroidNode parent = (OrbitingSpheroidNode)node.parent;
			if (parent instanceof Planet) {
				orbitRadius *= SIZE_MOON_ORBIT_ZOOM_FACTOR;
				orbitRadius += (float) (parent.radius * SIZE_FACTOR * SIZE_ZOOM_FACTOR - parent.radius * SIZE_FACTOR);
			}
		}
		return orbitRadius;
	}
	
	private ModelInstance createSphere(RenderState renderState, Planet node, String name, float radius, Material material) {
		float size = radius * 2;
		Model sphereModel = modelBuilder.createSphere(size, size, size, PLANET_SPHERE_DIVISIONS_U, PLANET_SPHERE_DIVISIONS_V,
			material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		ModelInstance sphere = new ModelInstance(sphereModel);

		UserData userData = new UserData();
		userData.node = node;
		userData.modelName = name;
		sphere.userData = userData;
		
		renderState.instances.add(sphere);
		
		return sphere;
	}

	private ModelInstance createOrbit (RenderState renderState, OrbitingNode node, float orbitRadius, Vector3 parentPosition) {
		Color color = node instanceof SpaceStation ? Color.NAVY : node.parent instanceof Planet ? Color.MAGENTA : Color.BLUE;
		Material material = new Material(ColorAttribute.createDiffuse(color));
		Model orbitModel = createOrbit(modelBuilder, orbitRadius, material, Usage.Position);
		ModelInstance orbit = new ModelInstance(orbitModel);
		orbit.transform.scl(orbitRadius);
		orbit.transform.setToTranslation(parentPosition);

		renderState.instancesAlways.add(orbit);
		
		return orbit;
	}
	
	private static UserData asUserData(ModelInstance modelInstance) {
		UserData userData = (UserData) modelInstance.userData;
		if (userData == null) {
			userData = new UserData();
			modelInstance.userData = userData;
		}
		return userData;
	}

	private static Model createOrbit (ModelBuilder modelBuilder, float radius, Material material, long attributes) {
		modelBuilder.begin();
		MeshPartBuilder partBuilder = modelBuilder.part("orbit", GL20.GL_LINES, attributes, material);

		float lastX = 0;
		float lastZ = 0;

		double thetaStep = 2 * Math.PI / 180;
		double theta = 0;
		while (theta < 2 * Math.PI) {
			float x = (float)Math.sin(theta) * radius;
			float z = (float)Math.cos(theta) * radius;

			if (theta != 0) {
				partBuilder.line(lastX, 0, lastZ, x, 0, z);
			}

			lastX = x;
			lastZ = z;

			theta += thetaStep;
		}

		return modelBuilder.end();
	}
}
