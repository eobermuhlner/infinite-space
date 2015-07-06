
package ch.obermuhlner.infinitespace;

import static ch.obermuhlner.infinitespace.model.random.Random.p;

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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class NodeToRenderConverter {

	private static final int PRIMITIVE_TYPE = Config.DEBUG_SHOW_LINES ? GL20.GL_LINES : GL20.GL_TRIANGLES;

	private static final boolean RENDER_PROCEDURAL_SHADERS_TO_TEXTURES = true;

	private static final double LAVA_TEMPERATURE = Units.celsiusToKelvin(700);

	private static final double ATMOSPHERE_PRESSURE_RELEVANT = 0.1;

	private static final double AU = Units.ASTRONOMICAL_UNIT;
	
	public static double SIZE_FACTOR = Config.SIZE_FACTOR;
	
	private static final int PLANET_SPHERE_DIVISIONS_U = 30;
	private static final int PLANET_SPHERE_DIVISIONS_V = 30;

	private static final int STATION_SPHERE_DIVISIONS_U = 20;
	private static final int STATION_SPHERE_DIVISIONS_V = 20;

	private final Map<Class<? extends Node>, NodeConverter<? extends Node>> nodeConverters = new HashMap<Class<? extends Node>, NodeConverter<? extends Node>>();

	private final AssetManager assetManager;

	private final ModelBuilder modelBuilder = new ModelBuilder();

	public boolean showNodeBoundingBox = Config.DEBUG_SHOW_NODE_BOUNDING_BOX;

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

	private <T extends Node> void convertNode (T node, RenderState renderState, boolean realUniverse) {
		@SuppressWarnings("unchecked")
		NodeConverter<T> nodeConverter = (NodeConverter<T>)nodeConverters.get(node.getClass());
		if (nodeConverter != null) {
			//StopWatch stopWatch = new StopWatch();
			Array<ModelInstance> instances = nodeConverter.convertToModelInstances(node, realUniverse);
			renderState.nodeToInstances.put(node, instances);
			
			BaseLight light = nodeConverter.convertToLight(node, realUniverse);
			if (light != null) {
				renderState.addLight(light);
			}
			//System.out.println("Converting to render " + node + " in " + stopWatch);
		}
	}

	public void createSkyBox(RenderState renderState) {
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


	private class StarConverter implements NodeConverter<Star> {

		@Override
		public Array<ModelInstance> convertToModelInstances (Star node, boolean realUniverse) {
			Array<ModelInstance> instances = new Array<ModelInstance>();

			float radius = calculateStarRadius(node);
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

				System.out.println("STAR " + node.seed + " " + node.name + " " + node.type + " " + Units.meterSizeToString(node.radius) + " " + Units.kelvinToString(node.temperature));

				instances.add(sphere);
			}

			return instances;
		}
		
		@Override
		public BaseLight convertToLight(Star node, boolean realUniverse) {
			float x = 0;
			float y = 0;
			float z = 0;
			float luminosity = 1.0f;

			// TODO color
			float r2 = 1.0f;
			float g2 = 0.9f;
			float b2 = 0.6f;

			return new PointLight().set(r2, g2, b2, x, y, z, luminosity);
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
		public Array<ModelInstance> convertToModelInstances (Planet node, boolean realUniverse) {
			Array<ModelInstance> instances = new Array<ModelInstance>();

			Random random = node.seed.getRandom();
			
			float radius = calculatePlanetRadius(node);
			float orbitRadius = calculateOrbitRadius(node);
			Vector3 position = calculatePosition(node);
			Vector3 parentPosition = calculatePosition(node.parent);

			Array<Attribute> materialAttributes = new Array<Attribute>();
			
			String textureName = node.textureName;
			String textureNormalName = node.textureNormalName;
			Texture textureNormal = null;
			String shaderName = null;
			Color[] planetColors = null;
			Color specularColor = null;
			
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
						if (node.temperature > LAVA_TEMPERATURE) {
							textureName = "lava_colors.png";
							shaderName = UberShaderProvider.TERRESTRIAL_PLANET_SHADER;
							materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightFrequency(random.nextFloat(3f, 22f)));
							materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorNoise(random.nextFloat(0.3f, 0.9f)));
							materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorFrequency(random.nextFloat(20f, 30f)));
							specularColor = new Color(0.2f, 0.2f, 0.2f, 1.0f);
						}
					}
					
					if (textureName == null) {
						if (node.atmospherePressure > ATMOSPHERE_PRESSURE_RELEVANT) {
							if (node.breathableAtmosphere) {
								if (node.hasLife) {
									textureName = "terrestrial_colors.png";
									float water = (float)node.water;
									float heightMin = MathUtil.transform(0f, 1f, 0.4f, 0.0f, water);
									float heightMax = MathUtil.transform(0f, 1f, 1.0f, 0.6f, water);
									float heightFrequency = random.nextFloat(2f, 15f);
									float iceLevel = MathUtil.transform((float)Units.celsiusToKelvin(-50), (float)Units.celsiusToKelvin(50), 1f, -1f, (float)node.temperature);
									materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightWater(0.45f)); // depends on texture
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
									//specularColor = new Color(0.5f, 0.5f, 0.5f, 1.0f);
								}
							}
							if (textureName == null && random.nextBoolean(0.5)) {
								textureName = "mars_colors.png";
								materialAttributes.add(TerrestrialPlanetFloatAttribute.createHeightFrequency(random.nextFloat(2f, 15f)));
								//materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorNoise(random.nextFloat(0.1f, 0.3f)));
								//materialAttributes.add(TerrestrialPlanetFloatAttribute.createColorFrequency(random.nextFloat(15f, 25f)));
								shaderName = UberShaderProvider.TERRESTRIAL_PLANET_SHADER;
								specularColor = new Color(0.2f, 0.2f, 0.2f, 1.0f);
								//textureNormalName = "random_craters_v2_normals.png";
								textureNormal = renderTextureNormalsCraters(node, random);
							}
						}
					}

					if (textureName == null) {
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
						specularColor = new Color(0.2f, 0.2f, 0.2f, 1.0f);
						//textureNormalName = "random_craters_v2_normals.png";
						textureNormal = renderTextureNormalsCraters(node, random);
					}

					if (textureName == null && node.radius < 1000E3) {
						textureName = random.next ("phobos.jpg", "deimos.jpg");
						specularColor = new Color(0.2f, 0.2f, 0.2f, 1.0f);
					}
					if (textureName == null) {
						textureName = random.next ("io.jpg", "callisto.jpg", "ganymede.jpg", "europa.jpg", "mercury.jpg", "mars.jpg", "moon.jpg", "iapetus.jpg");
						specularColor = new Color(0.2f, 0.2f, 0.2f, 1.0f);
					}
					break;
				case ICE:
					textureName = random.next ("europa.jpg", "mimas.jpg", "rhea.jpg", "enceladus.jpg", "tethys.jpg", "dione.jpg");
					specularColor = new Color(0.8f, 0.8f, 0.8f, 1.0f);
					break;
				}
			}

			System.out.println("PLANET " + node.seed + " " + node.name + " " + shaderName + " " + textureName + " " + Units.kelvinToString(node.temperature));
			
			{
				Material material;
				if (shaderName == null) {
					Texture textureDiffuse = assetManager.get(InfiniteSpaceGame.getTexturePath(textureName), Texture.class);
					materialAttributes.add(new TextureAttribute(TextureAttribute.Diffuse, textureDiffuse));
					
					if (textureNormal == null && textureNormalName != null) {
						textureNormal = assetManager.get(InfiniteSpaceGame.getTexturePath(textureNormalName), Texture.class);
					}
					if (textureNormal != null) {
						materialAttributes.add(new TextureAttribute(TextureAttribute.Normal, textureNormal));
					}
					
					if (specularColor != null) {
						materialAttributes.add(ColorAttribute.createSpecular(specularColor));
					}

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
						
						Texture textureDiffuse = renderTextureDiffuse(material, userData);
						Texture textureSpecular = null;
						if (specularColor == null && shaderName.equals(UberShaderProvider.TERRESTRIAL_PLANET_SHADER)) {
							textureSpecular = renderTextureSpecular(material, userData);
						}
						if (textureNormalName == null && shaderName.equals(UberShaderProvider.TERRESTRIAL_PLANET_SHADER)) {
							//textureNormal = renderTextureNormal(material, userData);
						}
						
						materialAttributes.clear();
						materialAttributes.add(new TextureAttribute(TextureAttribute.Diffuse, textureDiffuse));
						if (specularColor != null) {
							materialAttributes.add(ColorAttribute.createSpecular(specularColor));
						}
						if (textureSpecular != null) {
							materialAttributes.add(new TextureAttribute(TextureAttribute.Specular, textureSpecular));
							specularColor = null;
						}
						if (textureNormal != null) {
							materialAttributes.add(new TextureAttribute(TextureAttribute.Normal, textureNormal));
						} else if (textureNormalName != null) {
							textureNormal = assetManager.get(InfiniteSpaceGame.getTexturePath(textureNormalName), Texture.class);
							materialAttributes.add(new TextureAttribute(TextureAttribute.Normal, textureNormal));
						}
						material = new Material(materialAttributes);
						if (Config.DEBUG_PROFILING) {
							System.out.println("Render " + node + " to texture in " + stopWatch);
						}
					}
				}

				if (! realUniverse) {
					if (node.core != null) {
						float coreInnerRadius = 0;
						float angleFrom = 0;
						float angleTo = 270;
						for (int i = 0; i < node.core.size; i++) {
							PartInfo coreInfo = node.core.get(i);
							// TODO solve problem with cores z-fighting
							if (coreInfo.radius != node.radius) {
								float coreOuterRadius = calculatePlanetRadius(coreInfo.radius) * 0.95f; // make cores bit smaller to avoid z-fighting with surface 
								Material coreMaterial = new Material(temperatureToColorAttribute(coreInfo.temperature));
								ModelInstance sphere; 
								if (i == 0) {
									sphere = createSphere(node, coreInfo.name, coreOuterRadius, coreMaterial);
								} else {
									sphere = createSphereShell(node, coreInfo.name, coreInnerRadius, coreOuterRadius, angleFrom, angleTo, coreMaterial);									
								}
								instances.add(sphere);
								asUserData(sphere).description = (coreInfo.description == null ? "" : coreInfo.description) 
										+ ((coreInnerRadius == 0) ? "" : "\nInner Radius: " + Units.meterSizeToString(coreInnerRadius))
										+ "\nOuter Radius: " + Units.meterSizeToString(coreInfo.radius)
										+ "\nTemperature: " + Units.kelvinToString(coreInfo.temperature);
								asUserData(sphere).composition = coreInfo.composition;
								
								coreInnerRadius = coreOuterRadius;
								//angleFrom += 20;
								//angleTo -= 20;
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
					instances.add(gridInstance);
				}

				{
					ModelInstance sphere = createSphere(node, "Planet Surface", radius, material);
					instances.add(sphere);
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
					Material materialClouds = new Material(new TextureAttribute(TextureAttribute.Diffuse, texture), new BlendingAttribute(blend), ColorAttribute.createSpecular(0.7f, 0.7f, 0.7f, 1.0f));
					ModelInstance sphere = createSphere(node, "Atmosphere", atmosphereRadius, materialClouds);
					instances.add(sphere);
					asUserData(sphere).description = "Surface pressure: " + Units.pascalToString(node.atmospherePressure);
					asUserData(sphere).composition = node.atmosphere;
					if (realUniverse) {
						sphere.transform.setToTranslation(position);
					}
				}
			}

			instances.addAll(createOrbit(node, orbitRadius, parentPosition));
			
			return instances;
		}
		
		@Override
		public BaseLight convertToLight(Planet node, boolean realUniverse) {
			return null;
		}    
	}

	private class AsteroidBeltConverter implements NodeConverter<AsteroidBelt> {

		@Override
		public Array<ModelInstance>  convertToModelInstances (AsteroidBelt node, boolean realUniverse) {
			Array<ModelInstance> instances = new Array<ModelInstance>();
			
			float radius = calculateOrbitRadius(node);
			Vector3 position = calculatePosition(node.parent);

			float alpha = (float)node.density;

			UserData userData = new UserData();
			userData.shaderName = "ring";

			Material material = new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE), new BlendingAttribute(alpha));
			
			Model model;
			modelBuilder.begin();
			modelBuilder.part("rect-up", PRIMITIVE_TYPE, Usage.Position | Usage.Normal | Usage.TextureCoordinates, material)
			.rect(
				radius, 0f, -radius,
				-radius, 0f, -radius,
				-radius, 0f, radius,	
				radius, 0f, radius,
				0f, 1f, 0f);
			modelBuilder.part("rect-down", PRIMITIVE_TYPE, Usage.Position | Usage.Normal | Usage.TextureCoordinates, material)
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
			instances.add(ring);
			
			return instances;
		}

		@Override
		public BaseLight convertToLight(AsteroidBelt node, boolean realUniverse) {
			return null;
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

	public Texture renderTextureSpecular (Material material, UserData userData) {
		material.set(TerrestrialPlanetFloatAttribute.createCreateSpecular()); // FIXME just adding attribute is wrong, modifies the material
		return renderTextureDiffuse(material, userData);
	}
	
	public Texture renderTextureNormal (Material material, UserData userData) {
		material.set(TerrestrialPlanetFloatAttribute.createCreateNormal()); // FIXME just adding attribute is wrong, modifies the material
		return renderTextureDiffuse(material, userData);
	}
	
	public Texture renderTextureDiffuse (Material material, UserData userData) {
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
		//frameBuffer.dispose(); // FIXME memory leak
		
		return texture;
	}
	
	public Texture renderTextureNormalsCraters (Planet node, Random random) {
		final int targetTextureHeight = 1024;
		final int targetTextureWidth = 2048;
		
		int areaCount = 100;
		int craterCount = random.nextInt(100, 100000);
		boolean fillWithCraters = craterCount > 10000;
		float hugeCraterProbability = random.nextBoolean(0.6f) ? 2f : random.nextFloat(10, 500); 
		float areaProbability = random.nextFloat(0, 10);
		float vulcanoProbability = MathUtil.smoothstep(0.5f, 1.0f, random.nextFloat());
		int softCount = 0;
		if (node.atmospherePressure > ATMOSPHERE_PRESSURE_RELEVANT) {
			softCount = random.nextInt(5, 50);
			softCount += node.water;
		}

		System.out.println("Generating Normals craters=" + craterCount + " craterFill=" + fillWithCraters + " areaProb=" + areaProbability +" vulcanoProb=" + vulcanoProbability + " softCount=" + softCount);
		
		FrameBuffer frameBuffer = new FrameBuffer(Pixmap.Format.RGB888, targetTextureWidth, targetTextureHeight, false);
		frameBuffer.begin();

		Gdx.gl.glClearColor(0.5f, 0.5f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	       
		SpriteBatch spriteBatch = new SpriteBatch();
		spriteBatch.begin();
		
		Texture area1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_area1.png"), Texture.class);
		Texture area2 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_area2.png"), Texture.class);
		Texture area3 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_area3.png"), Texture.class);
		Texture craterArea1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_area1.png"), Texture.class);
		Texture craterHuge1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_huge1.png"), Texture.class);
		Texture craterHuge2 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_huge2.png"), Texture.class);
		Texture craterBig1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_big1.png"), Texture.class);
		Texture craterBig2 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_big2.png"), Texture.class);
		Texture craterMedium1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_medium1.png"), Texture.class);
		Texture craterMedium2 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_medium2.png"), Texture.class);
		Texture craterMedium3 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_medium3.png"), Texture.class);
		Texture craterSmall1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_small1.png"), Texture.class);
		Texture craterSmall2 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_small2.png"), Texture.class);
		Texture craterSmall3 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_small3.png"), Texture.class);
		Texture craterSmall4 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_small4.png"), Texture.class);
		Texture craterSmall5 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_small5.png"), Texture.class);
		Texture craterTiny1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_tiny1.png"), Texture.class);
		Texture craterTiny2 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_tiny2.png"), Texture.class);
		Texture craterTiny3 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_crater_tiny3.png"), Texture.class);
		Texture mountain1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_mountain1.png"), Texture.class);
		Texture mountain2 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_mountain2.png"), Texture.class);
		Texture vulcanoHuge1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_vulcano_huge1.png"), Texture.class);
		Texture vulcanoBig1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_vulcano_big1.png"), Texture.class);
		Texture vulcanoBig2 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_vulcano_big2.png"), Texture.class);
		Texture vulcanoBig3 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_vulcano_big3.png"), Texture.class);
		Texture vulcanoMedium1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_vulcano_medium1.png"), Texture.class);
		Texture vulcanoMedium2 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_vulcano_medium2.png"), Texture.class);
		Texture vulcanoMedium3 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_vulcano_medium3.png"), Texture.class);
		Texture vulcanoMedium4 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_vulcano_medium4.png"), Texture.class);
		Texture soft1 = assetManager.get(InfiniteSpaceGame.getTexturePath("normals_soft1.png"), Texture.class);

		if (fillWithCraters) {
			Texture texture = craterArea1;
			
			int nx = targetTextureWidth / texture.getWidth() * 2;
			int ny = targetTextureHeight / texture.getHeight() * 2;
			float stepx = (float)targetTextureWidth / nx;
			float stepy = (float)targetTextureHeight / ny;
			for (int iy = 0; iy < ny; iy++) {
				for (int ix = 0; ix < nx; ix++) {
					float x = ix * stepx + texture.getWidth() * random.nextFloat(-0.25f, 0.25f);
					float y = iy * stepy + texture.getHeight() * random.nextFloat(-0.25f, 0.25f);;
					spriteBatch.draw(texture, x, y);
				}
			}
		} else {
			for (int i = 0; i < areaCount; i++) {
				Texture texture = random.nextProbability(
						p(5, area1),
						p(10, area2),
						p(10, area3));
				float x = random.nextFloat(0, targetTextureWidth - texture.getWidth());
				float y = random.nextFloat(0, targetTextureHeight - texture.getHeight());
				spriteBatch.draw(texture, x, y);
			}
		}


		for (int i = 0; i < craterCount; i++) {
			Texture texture = random.nextProbability(
					p(hugeCraterProbability, craterHuge1),
					p(hugeCraterProbability, craterHuge2),
					p(20, craterBig1),
					p(20, craterBig2),
					p(100, craterMedium1),
					p(100, craterMedium2),
					p(100, craterMedium3),
					p(300, craterSmall1),
					p(300, craterSmall2),
					p(300, craterSmall3),
					p(300, craterSmall4),
					p(300, craterSmall5),
					p(2000, craterTiny1),
					p(3000, craterTiny2),
					p(3000, craterTiny3),
					p(500, mountain1),
					p(50, mountain2),
					p(vulcanoProbability * 1, vulcanoHuge1),
					p(vulcanoProbability * 5, vulcanoBig1),
					p(vulcanoProbability * 5, vulcanoBig2),
					p(vulcanoProbability * 5, vulcanoBig3),
					p(vulcanoProbability * 10, vulcanoMedium1),
					p(vulcanoProbability * 10, vulcanoMedium2),
					p(vulcanoProbability * 10, vulcanoMedium3),
					p(vulcanoProbability * 10, vulcanoMedium4),
					p(areaProbability, area2),
					p(areaProbability, area3));
			float x = random.nextFloat(0, targetTextureWidth - texture.getWidth());
			float y = random.nextFloat(0, targetTextureHeight - texture.getHeight());
			spriteBatch.draw(texture, x, y);
		}

		for (int i = 0; i < softCount; i++) {
			Texture texture = soft1;
			float x = random.nextFloat(0, targetTextureWidth - texture.getWidth());
			float y = random.nextFloat(0, targetTextureHeight - texture.getHeight());
			spriteBatch.draw(area1, x, y);
		}

		spriteBatch.end();

		frameBuffer.end();
		
		Texture texture = frameBuffer.getColorBufferTexture();

		//frameBuffer.dispose(); // FIXME memory leak
		
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
		public Array<ModelInstance> convertToModelInstances (SpaceStation node, boolean realUniverse) {
			Array<ModelInstance> instances = new Array<ModelInstance>();

			float orbitRadius = calculateOrbitRadius(node);
			Vector3 position = calculatePosition(node);
			Vector3 parentPosition = calculatePosition(node.parent);

			Random random = node.seed.getRandom();
			
			float width = (float)(node.width * SIZE_FACTOR);
			float height = (float)(node.height * SIZE_FACTOR);
			float length = (float)(node.length * SIZE_FACTOR);

			if (showNodeBoundingBox && !realUniverse) {
				instances.add(createBoundingBox(width, height, length,
						"Width: " + Units.meterSizeToString(node.width) + "\n" +
						"Height: " + Units.meterSizeToString(node.height) + "\n" +
						"Length: " + Units.meterSizeToString(node.length) + "\n"));
			}
			
			{
				ModelInstance station;
				
				Array<Attribute> materialPlainAttributes = new Array<Attribute>();
				Array<Attribute> materialWindowsAttributes = new Array<Attribute>();

				float plainTextureSize;
				float windowTextureSize; 
				
				// base material: diffuse texture or color + specular color
				if(random.nextBoolean(0.6)) {
					Texture textureDiffuse = assetManager.get(InfiniteSpaceGame.getTexturePath("spaceship3.jpg"), Texture.class);
					TextureAttribute diffuseAttribute = new TextureAttribute(TextureAttribute.Diffuse, textureDiffuse);
					materialPlainAttributes.add(diffuseAttribute);
					materialWindowsAttributes.add(diffuseAttribute);

					Texture textureNormal = assetManager.get(InfiniteSpaceGame.getTexturePath("spaceship3_normals.png"), Texture.class);
					TextureAttribute normalAttribute = new TextureAttribute(TextureAttribute.Normal, textureNormal);
					materialPlainAttributes.add(normalAttribute);

					plainTextureSize = (float)(100 * SIZE_FACTOR); // m
				} else {
					float grayLuminance = random.nextFloat(0.25f, 0.75f);
					ColorAttribute grayAttribute = ColorAttribute.createDiffuse(new Color(grayLuminance, grayLuminance, grayLuminance, 1f));
					materialPlainAttributes.add(grayAttribute);
					materialWindowsAttributes.add(grayAttribute);
					plainTextureSize = 1f;
				}
				float specularValue = random.nextFloat(0.4f, 0.7f);
				materialPlainAttributes.add(ColorAttribute.createSpecular(specularValue, specularValue, specularValue, 1.0f));

				// windows: emissive + specular texture
				Texture textureEmissive;
				Texture textureSpecular;
				if (random.nextBoolean(0.6)) {
					textureEmissive = assetManager.get(InfiniteSpaceGame.getTexturePath("windows1.jpg"), Texture.class);
					textureSpecular = assetManager.get(InfiniteSpaceGame.getTexturePath("windows1_specular.jpg"), Texture.class);
					windowTextureSize = (float)(60 * SIZE_FACTOR); // m
				} else {
					textureEmissive = assetManager.get(InfiniteSpaceGame.getTexturePath("windows2.jpg"), Texture.class);
					textureSpecular = assetManager.get(InfiniteSpaceGame.getTexturePath("windows2_specular.jpg"), Texture.class);
					windowTextureSize = (float)(60 * SIZE_FACTOR); // m
				}
				materialWindowsAttributes.add(new TextureAttribute(TextureAttribute.Emissive, textureEmissive));
				materialWindowsAttributes.add(new TextureAttribute(TextureAttribute.Specular, textureSpecular));

				Material materialPlain = new Material(materialPlainAttributes);
				Material materialWindows = new Material(materialWindowsAttributes);
				
				Model stationModel = null;
				switch(node.type) {
				case RING:
					stationModel = createRingModel(materialPlain, materialWindows, random, width, height, length);
					break;
				case BALANCED:
					stationModel = createBalancedModel(materialPlain, materialWindows, random, width, height, length);
					break;
				case VARIABLE_CYLINDER:
					stationModel = createVariableCylinderModel(materialPlain, materialWindows, random, width, height, length);
					break;
				case CYLINDER:
					stationModel = createCylinderModel(materialWindows, width, height, length);
					break;
				case SPHERE:
					stationModel = createSphereModel(materialWindows, windowTextureSize, width, height, length);
					break;
				case BLOCKY:
					stationModel = createBlockyModel(materialPlain, plainTextureSize, materialWindows, windowTextureSize, random, width, height, length);
					break;
				case CONGLOMERATE:
					stationModel = createConglomerateModel(materialPlain, materialWindows, random, width, height, length);
					break;
				case CUBE:
					stationModel = createCubeModel(materialWindows, windowTextureSize, width, height, length);
					break;
				}
				
				station = new ModelInstance(stationModel);
				if (realUniverse) {
					station.transform.setToTranslation(position);
				}

				UserData userData = new UserData();
				userData.node = node;
				station.userData = userData;
				instances.add(station);
			}
			
			instances.add(createOrbit(node, orbitRadius, parentPosition));

			return instances;
		}
		
		@Override
		public BaseLight convertToLight(SpaceStation node, boolean realUniverse) {
			return null;
		}
	}
	
	private Model createCubeModel(Material material, float textureSize, float width, float height, float length) {
		modelBuilder.begin();
		MeshPartBuilder part = modelBuilder.part("cube", PRIMITIVE_TYPE, (Usage.Position | Usage.Normal | Usage.TextureCoordinates), material);
		float minSize = Math.min(Math.min(width, height), length);
		float uvSize = minSize / textureSize;
		part.setUVRange(0f, 0f, uvSize, uvSize);

		part.box(width, height, length);

		return modelBuilder.end();
	}

	private Model createCylinderModel(Material material, float width, float height, float length) {
		modelBuilder.begin();

		createBasicPart(BasicPartType.CYLINDER, material, width, height, length);

		return modelBuilder.end();
	}

	private Model createSphereModel(Material material, float textureSize, float width, float height, float length) {
		modelBuilder.begin();

		MeshPartBuilder part = createBasicPart(BasicPartType.SPHERE, material, width, height, length);
		float minSize = Math.min(Math.min(width, height), length);
		float uvSize = minSize / textureSize;
		part.setUVRange(0f, 0f, uvSize, uvSize);

		return modelBuilder.end();
	}

	private Model createBlockyModel(Material materialPlain, float plainTextureSize, Material materialWindows, float windowsTextureSize, Random random, float width, float height, float length) {
		modelBuilder.begin();

		int noduleCount = random.nextInt(10, 20);

		for (int i = 0; i < noduleCount; i++) {
			com.badlogic.gdx.graphics.g3d.model.Node modelNode = modelBuilder.node();

			float noduleWidth = random.nextFloat(width/4, width/2); 
			float noduleHeight = random.nextFloat(height/4, height/2); 
			float noduleLength = random.nextFloat(length/4, length/2); 

			boolean useWindowsMaterial = random.nextBoolean(0.75);
			Material material = useWindowsMaterial ? materialWindows : materialPlain;
			float textureSize = useWindowsMaterial ? windowsTextureSize : plainTextureSize;
			
			BasicPartType partType = random.next(BasicPartType.CUBE, BasicPartType.SPHERE, BasicPartType.CYLINDER);
			MeshPartBuilder part = createBasicPart(partType, material, noduleWidth, noduleHeight, noduleLength);
			float minSize = Math.min(Math.min(width, height), length);
			float uvSize = minSize / textureSize / partType.textureFactor();
			part.setUVRange(0f, 0f, uvSize, uvSize);
			
			float w2 = (width - noduleWidth) / 2;
			float h2 = (height - noduleHeight) / 2;
			float l2 = (length - noduleLength) / 2;
			float x = random.nextFloat(-w2, w2);
			float y = random.nextFloat(-h2, h2);
			float z = random.nextFloat(-l2, l2);
			modelNode.translation.set(x, y, z);
			modelNode.calculateTransforms(false);
		}
		
		return modelBuilder.end();
	}
	
	private Model createConglomerateModel(Material materialPlain, Material materialWindows, Random random, float width, float height, float length) {
		modelBuilder.begin();

		int noduleCount = random.nextInt(2, 5);
		
		float noduleWidth = width / noduleCount; 
		float noduleHeight = height / noduleCount; 
		float noduleLength = length / noduleCount; 
		
		int xCount = noduleCount;
		int yCount = noduleCount;
		for (int x = 0; x < xCount; x++) {
			for (int y = 0; y < yCount; y++) {
				float noduleLengthRandomized = random.nextInt(1, 4) * noduleLength;
				
				Material material = random.nextBoolean(0.75) ? materialWindows : materialPlain;

				com.badlogic.gdx.graphics.g3d.model.Node modelNode = modelBuilder.node();
				int r = random.nextInt(5);
				int divisions = 10;
				switch(r) {
				case 0:
					break;
				case 1:
					modelBuilder.part("box", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), material).box(noduleWidth * 0.8f, noduleHeight * 0.8f, noduleLengthRandomized);
					break;
				case 2:
					modelBuilder.part("cylinder", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), material).cylinder(noduleWidth * 0.8f, noduleHeight * 0.8f, noduleLength, divisions);
					break;
				case 3:
					modelBuilder.part("sphere", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), material).sphere(noduleWidth * 0.8f, noduleHeight * 0.8f, noduleLength, divisions, divisions);
					break;
				case 4:
					modelBuilder.part("box", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), material).box(noduleWidth * 0.4f, noduleHeight * 0.4f, noduleLengthRandomized);
					modelBuilder.part("panel", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), materialPlain).box(noduleWidth * 0.9f, noduleHeight * 0.02f, noduleLength * 6);
					break;
				}
				modelNode.translation.add(x * noduleWidth, y * noduleHeight, 0);
				modelNode.calculateTransforms(false);
			}
		}
		return modelBuilder.end();
	}

	private Model createVariableCylinderModel(Material materialPlain, Material materialWindows, Random random, float width, float height, float length) {
		modelBuilder.begin();

		int stepCount = random.nextInt(3, 20);
		int extremeStepIndex = random.nextInt(0, stepCount);

		float stepHeight = height / stepCount;

		float stepRadius = random.nextFloat(width * 0.1f, width);

		for (int i = 0; i < stepCount; i++) {
			com.badlogic.gdx.graphics.g3d.model.Node node = modelBuilder.node();
			stepRadius += random.nextFloat(-width * 0.1f, +width * 0.1f);
			float currentStepRadius = i == extremeStepIndex ? width : stepRadius;
			createBasicPart(BasicPartType.CYLINDER, materialWindows, currentStepRadius, stepHeight, currentStepRadius);
			float y = i * stepHeight + stepHeight / 2 - height / 2;
			node.translation.add(0, y, 0);
			node.calculateTransforms(false);
		}
		
		return modelBuilder.end();
	}

	private Model createRingModel(Material materialPlain, Material materialWindows, Random random, float width, float height, float length) {
		modelBuilder.begin();

		// width and length are the diameter of the ring.
		// height is the height of the center part
		
		int axisCount = random.nextInt(3, 10);
		int ringSegmentCount = axisCount * random.nextInt(1, 4);
		float axisStepAngle = 360 / axisCount;
		float ringSegmentStepAngle = 360 / ringSegmentCount;
		float ringRadius = width / 2;
		float outerRingRadius = (float) (ringRadius * MathUtil.sec(Math.PI / ringSegmentCount));
		float centerRadius = ringRadius / random.nextInt(3, 6);
		float centerHeight = height;
		float torusWidth = height * random.nextFloat(0.1f, 0.5f);
		float torusHeight = height * random.nextFloat(0.1f, 0.5f); // maybe same as torusWidth
		float spokeWidth = torusHeight * random.nextFloat(0.02f, 0.2f);
		float spokeHeight = torusHeight * random.nextFloat(0.02f, 0.8f); // maybe same as spokeWidth
		boolean torusSphereAtSpokeEnd = random.nextBoolean(0.1);
		boolean torusSphereAtJoints = !torusSphereAtSpokeEnd && random.nextBoolean(0.5);
		float torusSphereWidth = torusWidth * 1.1f;
		float torusSphereHeight = torusHeight * 1.1f;
		float torusSphereLength = torusSphereWidth; // ??
		float segmentLength = (float) (2 * (ringRadius + torusWidth / 2) * Math.tan(Math.PI / ringSegmentCount));

		BasicPartType spokePartType = random.next(BasicPartType.CUBE, BasicPartType.CYLINDER, BasicPartType.SPHERE);

		com.badlogic.gdx.graphics.g3d.model.Node node = modelBuilder.node();
		BasicPartType centerPartType = random.next(BasicPartType.CYLINDER, BasicPartType.SPHERE);
		createBasicPart(centerPartType, random.nextBoolean(0.5) ? materialWindows : materialPlain, centerRadius, centerHeight, centerRadius);

		for (int i = 0; i < axisCount; i++) {
			float angle = axisStepAngle * i;
			float angleRad = MathUtils.degreesToRadians * angle;
			double sinAngle = Math.sin(angleRad);
			double cosAngle = Math.cos(angleRad);

			{
				node = modelBuilder.node();
				createBasicPart(spokePartType, materialPlain, spokeWidth, spokeHeight, ringRadius);
				node.rotation.setEulerAngles(-angle + 90, 0, 0);
				float x = (float) (cosAngle * ringRadius / 2);
				float y = 0;
				float z = (float) (sinAngle * ringRadius / 2);
				node.translation.add(x, y, z);
				node.calculateTransforms(false);
			}
			
			if (torusSphereAtSpokeEnd) {
				node = modelBuilder.node();
				Material materialTorus = random.nextBoolean(0.5) ? materialWindows : materialPlain;
				modelBuilder.part("torusSphere", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), materialTorus).sphere(torusSphereWidth, torusSphereHeight,
						torusSphereLength, STATION_SPHERE_DIVISIONS_U, STATION_SPHERE_DIVISIONS_V);
				float x = (float) (cosAngle * ringRadius);
				float y = 0;
				float z = (float) (sinAngle * ringRadius);
				node.translation.add(x, y, z);
				node.calculateTransforms(false);
			}
		}

		/*
		node = modelBuilder.node();
		MeshPartBuilder meshPartBuilder = modelBuilder.part("torus", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), material);
		createTorus(meshPartBuilder, 0, 0, 0, ringRadius, torusSphereHeight, STATION_SPHERE_DIVISIONS_U, STATION_SPHERE_DIVISIONS_V);
		node.rotation.setEulerAngles(0, 90, 0);
		 */

		for (int i = 0; i < ringSegmentCount; i++) {
			double angle = ringSegmentStepAngle * i;
			double angleRad = MathUtils.degreesToRadians * angle;
			double sinAngle = Math.sin(angleRad);
			double cosAngle = Math.cos(angleRad);

			{
				node = modelBuilder.node();
				modelBuilder.part("ring-segment", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), materialWindows)
					.cylinder(torusWidth, segmentLength, torusHeight, STATION_SPHERE_DIVISIONS_U);
				node.rotation.setFromAxis(1, 0, 0, 90);
				node.rotation.mul(new Quaternion(new Vector3(0, 0, 1), (float) angle));
				float x = (float) (cosAngle * ringRadius);
				float y = 0;
				float z = (float) (sinAngle * ringRadius);
				node.translation.add(x, y, z);
				node.calculateTransforms(false);
			}
			
			if (torusSphereAtJoints) {
				float jointAngle = ringSegmentStepAngle * i + ringSegmentStepAngle / 2;
				float jointAngleRad = MathUtils.degreesToRadians * jointAngle;

				node = modelBuilder.node();
				modelBuilder.part("torusSphere", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), materialPlain)
					.sphere(torusSphereWidth, torusSphereHeight, torusSphereLength, STATION_SPHERE_DIVISIONS_U, STATION_SPHERE_DIVISIONS_V);
				float x = (float) (Math.cos(jointAngleRad) * outerRingRadius);
				float y = 0;
				float z = (float) (Math.sin(jointAngleRad) * outerRingRadius);
				node.translation.add(x, y, z);
				node.calculateTransforms(false);
			}
		}
		
		return modelBuilder.end();
	}

	public ModelInstance createBoundingBox(float width, float height, float length, String description) {
		Material material = new Material(ColorAttribute.createDiffuse(Color.RED));
		Model boxModel = modelBuilder.createBox(width, height, length, GL20.GL_LINES, material, Usage.Position);
		ModelInstance box = new ModelInstance(boxModel);
		
		UserData userData = new UserData();
		userData.modelName = "Size";
		userData.description = description;
		box.userData = userData;
		
		return box;
	}

	private VertexInfo vertTmp3 = new VertexInfo();
	private VertexInfo vertTmp4 = new VertexInfo();

	private void createTorus(MeshPartBuilder builder, float X, float Y, float Z, float widthR, float height, int divisionsU, int divisionsV) {

		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		curr1.hasUV = curr1.hasPosition = curr1.hasNormal = true;
		VertexInfo curr2 = vertTmp4.set(null, null, null, null);
		curr2.hasUV = curr2.hasPosition = curr2.hasNormal = true;
		short i1, i2, i3 = 0, i4 = 0;

		int i, j, k;
		double s, t, twopi;
		twopi = 2 * Math.PI;

		for (i = 0; i < divisionsV; i++) {
			for (j = 0; j <= divisionsU; j++) {
				for (k = 1; k >= 0; k--) {
					s = (i + k) % divisionsV + 0.5;
					t = j % divisionsU;

					curr1.position.set((float) ((widthR + height * Math.cos(s * twopi / divisionsV)) * Math.cos(t * twopi / divisionsU)),
							(float) ((widthR + height * Math.cos(s * twopi / divisionsV)) * Math.sin(t * twopi / divisionsU)), (float) (height * Math.sin(s * twopi / divisionsV)));
					curr1.normal.set(curr1.position).nor();
					k--;
					s = (i + k) % divisionsV + 0.5;
					curr2.position.set((float) ((widthR + height * Math.cos(s * twopi / divisionsV)) * Math.cos(t * twopi / divisionsU)),
							(float) ((widthR + height * Math.cos(s * twopi / divisionsV)) * Math.sin(t * twopi / divisionsU)), (float) (height * Math.sin(s * twopi / divisionsV)));
					curr2.normal.set(curr1.normal);
					curr2.uv.set((float) s, (float) t);
					i1 = builder.vertex(curr1);
					i2 = builder.vertex(curr2);
					builder.rect(i4, i2, i1, i3);
					i4 = i2;
					i3 = i1;
				}
			}
		}
	}

	private Model createBalancedModel(Material materialPlain, Material material, Random random, float width, float height, float length) {
		modelBuilder.begin();

		float noduleWidth = width;
		float noduleHeight = height;
		float noduleLength = (width + height) / 2; // length is the length of the entire station, reuse width and height to make the nodule more cubic

		BasicPartType leftPartType = random.next(BasicPartType.CUBE, BasicPartType.SPHERE, BasicPartType.CYLINDER);
		BasicPartType centerPartType = random.next(BasicPartType.CUBE, BasicPartType.CYLINDER);
		BasicPartType rightPartType = random.next(BasicPartType.CUBE, BasicPartType.SPHERE, BasicPartType.CYLINDER);
		
		if (random.nextBoolean(0.5)) {
			leftPartType = rightPartType;
		}
		
		float spokeSizeFactor = 1f / random.nextFloat(8, 15);
		float centerSizeFactor = 1f / random.nextFloat(2, 5);
		
		com.badlogic.gdx.graphics.g3d.model.Node modelNode;
		
		modelNode = modelBuilder.node();
		modelBuilder.part("spoke", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), material).box(noduleWidth * spokeSizeFactor, noduleHeight * spokeSizeFactor, length - noduleLength);

		modelNode = modelBuilder.node();
		createBasicPart(leftPartType, material, noduleWidth, noduleHeight, noduleLength);
		modelNode.rotation.setEulerAngles(0, 90, 0);
		modelNode.translation.add(0, 0, length / 2 - noduleLength/2);
		modelNode.calculateTransforms(false);
			
		modelNode = modelBuilder.node();
		createBasicPart(centerPartType, material, noduleWidth * centerSizeFactor, noduleHeight * centerSizeFactor, noduleLength * centerSizeFactor);

		modelNode = modelBuilder.node();
		createBasicPart(rightPartType, material, noduleWidth, noduleHeight, noduleLength);
		modelNode.rotation.setEulerAngles(0, 90, 0);
		modelNode.translation.add(0, 0, -(length / 2 - noduleLength/2));
		modelNode.calculateTransforms(false);

		return modelBuilder.end();
	}
	
	public enum BasicPartType {
		CUBE(1),
		SPHERE(3.14f),
		CYLINDER(3.14f);

		private float textureFactor;

		private BasicPartType(float textureFactor) {
			this.textureFactor = textureFactor;
		}
		
		public float textureFactor() {
			return textureFactor;
		}
	};
	public MeshPartBuilder createBasicPart(BasicPartType basicPartType, Material material, float width, float height, float length) {
		MeshPartBuilder part = modelBuilder.part(basicPartType.name(), PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), material);
		
		switch(basicPartType) {
		default:
		case CUBE:
			part.box(width, height, length);
			break;
		case CYLINDER:
			part.cylinder(width, height, length, STATION_SPHERE_DIVISIONS_U);
			break;
		case SPHERE:
			part.sphere(width, height, length, STATION_SPHERE_DIVISIONS_U, STATION_SPHERE_DIVISIONS_V);
			break;
		}
		
		return part;
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
		return (float)(node.radius * SIZE_FACTOR);
	}
	
	public static float calculatePlanetRadius(Planet node) {
		return calculatePlanetRadius(node.radius);
	}

	public static float calculatePlanetRadius(double radius) {
		return (float)(radius * SIZE_FACTOR);
	}

	public static float calculateSpaceStationRadius (SpaceStation node) {
		return (float) (Math.max(Math.max(node.width, node.height), node.length) / 2 * SIZE_FACTOR);
	}


	public static float calculateOrbitRadius(OrbitingNode node) {
		float orbitRadius = (float) (node.orbitRadius * SIZE_FACTOR);
		if (node.parent instanceof OrbitingSpheroidNode) {
			OrbitingSpheroidNode parent = (OrbitingSpheroidNode)node.parent;
			if (parent instanceof Planet) {
				orbitRadius += (float) (parent.radius - parent.radius);
			}
		}
		return orbitRadius;
	}

	private ModelInstance createSphereShell(Planet node, String name, float innerRadius, float outerRadius, float angleFrom, float angleTo, Material material) {
		float size = outerRadius * 2;
		modelBuilder.begin();
		modelBuilder.node();
		MeshPartBuilder meshBuilder = modelBuilder.part("sphereShell", PRIMITIVE_TYPE, (long) (Usage.Position | Usage.Normal | Usage.TextureCoordinates), material);
		meshBuilder.sphere(size, size, size, PLANET_SPHERE_DIVISIONS_U, PLANET_SPHERE_DIVISIONS_V, angleFrom, angleTo, 0, 180);
		
		com.badlogic.gdx.graphics.g3d.model.Node nodeCircle1 = modelBuilder.node();
		float normalX = 0;
		float normalY = 1;
		float normalZ = 0;
		meshBuilder.circle(outerRadius, PLANET_SPHERE_DIVISIONS_V, 0, 0, 0, normalX, normalY, normalZ, 0, 180);
		
		Model sphereModel = modelBuilder.end();
		ModelInstance sphere = new ModelInstance(sphereModel);

		UserData userData = new UserData();
		userData.node = node;
		userData.modelName = name;
		sphere.userData = userData;
		
		return sphere;
	}
	
	private ModelInstance createSphere(Planet node, String name, float radius, Material material) {
		float size = radius * 2;
		Model sphereModel = modelBuilder.createSphere(size, size, size, PLANET_SPHERE_DIVISIONS_U, PLANET_SPHERE_DIVISIONS_V,
				material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		ModelInstance sphere = new ModelInstance(sphereModel);

		UserData userData = new UserData();
		userData.node = node;
		userData.modelName = name;
		sphere.userData = userData;
		
		return sphere;
	}

	private ModelInstance createOrbit (OrbitingNode node, float orbitRadius, Vector3 parentPosition) {
		Color color = node instanceof SpaceStation ? Color.NAVY : node.parent instanceof Planet ? Color.MAGENTA : Color.BLUE;
		Material material = new Material(ColorAttribute.createDiffuse(color));
		Model orbitModel = createOrbit(modelBuilder, orbitRadius, material, Usage.Position);
		ModelInstance orbit = new ModelInstance(orbitModel);
		orbit.transform.scl(orbitRadius);
		orbit.transform.setToTranslation(parentPosition);

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
