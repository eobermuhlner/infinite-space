
package ch.obermuhlner.infinitespace;

import java.util.HashMap;
import java.util.Map;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.OrbitingSpheroidNode;
import ch.obermuhlner.infinitespace.model.random.Random;
import ch.obermuhlner.infinitespace.model.universe.AsteroidBelt;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.render.ColorArrayAttribute;
import ch.obermuhlner.infinitespace.render.FloatArrayAttribute;
import ch.obermuhlner.infinitespace.render.UberShaderProvider;
import ch.obermuhlner.infinitespace.util.MathUtil;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
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

public class NodeToRenderConverter {

	private static final boolean RENDER_PROCEDURAL_SHADERS_TO_TEXTURES = true;

	private static final double SUN_RADIUS = Units.SUN_RADIUS;

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
		{
			// create grid
			Material material = new Material(ColorAttribute.createDiffuse(new Color(0, 0.2f, 0, 1f)));
			Model gridModel = modelBuilder.createLineGrid(2000, 2000, 5f, 5f, material, Usage.Position);
			renderState.instancesAlways.add(new ModelInstance(gridModel));
		}
		
		for (Node node : universe) {
			convertNode(node, renderState, true);
		}

		createSkyBox(renderState);
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


	private <T extends Node> void convertNode (T node, RenderState renderState, boolean calculatePosition) {
		@SuppressWarnings("unchecked")
		NodeConverter<T> nodeConverter = (NodeConverter<T>)nodeConverters.get(node.getClass());
		if (nodeConverter != null) {
			nodeConverter.convert(node, renderState, calculatePosition);
		}
	}

	private class StarConverter implements NodeConverter<Star> {

		@Override
		public void convert (Star node, RenderState renderState, boolean calculatePosition) {
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
		public void convert (Planet node, RenderState renderState, boolean calculatePosition) {
			Random random = node.seed.getRandom();
			
			float radius = calculatePlanetRadius(node);
			float orbitRadius = calculateOrbitRadius(node);
			Vector3 position = calculatePosition(node);
			Vector3 parentPosition = calculatePosition(node.parent);

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
					if (node.radius < 1000E3) {
						textureName = random.next ("phobos.jpg", "deimos.jpg");
					} else {
						if (node.breathableAtmosphere && node.water > 0.5) {
							textureName = "earth.jpg";
						} else {
							textureName = random.next ("io.jpg", "callisto.jpg", "ganymede.jpg", "europa.jpg", "mercury.jpg", "mars.jpg", "moon.jpg", "iapetus.jpg", "rhea.jpg");
						}
					}
					break;
				case ICE:
					textureName = random.next ("europa.jpg", "mimas.jpg");
					break;
				}
			}
			
			{
				Material material;
				if (textureName != null) {
					Texture texture = assetManager.get(InfiniteSpaceGame.getTexturePath(textureName), Texture.class);
					material = new Material(new TextureAttribute(TextureAttribute.Diffuse, texture));
				} else {
					ColorArrayAttribute randomColors = new ColorArrayAttribute(ColorArrayAttribute.PlanetColors, randomPlanetColors(random, planetColors));
					float floatArray[] = new float[10];
					for (int i = 0; i < floatArray.length; i++) {
						floatArray[i] = random.nextFloat();
					}
					FloatArrayAttribute randomFloats = new FloatArrayAttribute(FloatArrayAttribute.FloatArray, floatArray);
					material = new Material(randomColors, randomFloats);
					if (RENDER_PROCEDURAL_SHADERS_TO_TEXTURES) {
						UserData userData = new UserData();
						userData.shaderName = shaderName;
						Texture texture = renderTexture(material, userData);
						material = new Material(new TextureAttribute(TextureAttribute.Diffuse, texture));
					}
				}

				{
					Model sphereModel = modelBuilder.createSphere(radius, radius, radius, PLANET_SPHERE_DIVISIONS_U, PLANET_SPHERE_DIVISIONS_V,
						material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
					ModelInstance sphere = new ModelInstance(sphereModel);
					if (calculatePosition) {
						sphere.transform.setToTranslation(position);
					}
	
					UserData userData = new UserData();
					userData.node = node;
					sphere.userData = userData;
					renderState.instances.add(sphere);
				}
				
				if (node.breathableAtmosphere && node.water > 0.2) {
					float atmosphereRadiusFactor = 1.01f;
					float atmosphereRadius = radius * atmosphereRadiusFactor; 
					Texture texture = assetManager.get(InfiniteSpaceGame.getTexturePath("clouds.png"), Texture.class);
					Material materialClouds = new Material(new TextureAttribute(TextureAttribute.Diffuse, texture), new BlendingAttribute(1.0f));
					Model sphereModel = modelBuilder.createSphere(atmosphereRadius, atmosphereRadius, atmosphereRadius, PLANET_SPHERE_DIVISIONS_U, PLANET_SPHERE_DIVISIONS_V,
							materialClouds, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
					ModelInstance sphereClouds = new ModelInstance(sphereModel);
					if (calculatePosition) {
						sphereClouds.transform.setToTranslation(position);
					}
					renderState.instances.add(sphereClouds);
				}
			}

			createOrbit(renderState, node, orbitRadius, parentPosition);
		}
	}

	private class AsteroidBeltConverter implements NodeConverter<AsteroidBelt> {

		@Override
		public void convert (AsteroidBelt node, RenderState renderState, boolean calculatePosition) {
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

			if (calculatePosition) {
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
			x += orbitRadius;
			y += 0;
			z += 0;
			current = current.parent;
		}
		
		return new Vector3 (x, y, z);
	}

	public Texture renderTexture (Material material, UserData userData) {
		final int textureSize = GamePreferences.INSTANCE.preferences.getInteger(GamePreferences.INT_GENERATED_TEXTURES_SIZE);
		
		Model model;
		model = modelBuilder.createRect(
			textureSize, 0f, -textureSize,
			-textureSize, 0f, -textureSize,
			-textureSize, 0f, textureSize,
			textureSize, 0f, textureSize,
			0, 1, 0,
			material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);

		ModelInstance instance = new ModelInstance(model);
		instance.userData = userData;

		ModelBatch modelBatch = new ModelBatch(UberShaderProvider.DEFAULT);

		FrameBuffer frameBuffer = new FrameBuffer(Pixmap.Format.RGB888, textureSize, textureSize, false);
		frameBuffer.begin();

		Camera camera = new OrthographicCamera(textureSize, textureSize);
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
		//frameBuffer.dispose();
		
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
		public void convert (SpaceStation node, RenderState renderState, boolean calculatePosition) {
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
				if (calculatePosition) {
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
		return (float)(node.radius * SIZE_FACTOR * SIZE_ZOOM_FACTOR);
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
	
	private void createOrbit (RenderState renderState, OrbitingNode node, float orbitRadius, Vector3 parentPosition) {
		{
			Color color = node instanceof SpaceStation ? Color.NAVY : node.parent instanceof Planet ? Color.MAGENTA : Color.BLUE;
			Material material = new Material(ColorAttribute.createDiffuse(color));
			Model orbitModel = createOrbit(modelBuilder, orbitRadius, material, Usage.Position);
			ModelInstance orbit = new ModelInstance(orbitModel);
			orbit.transform.scl(orbitRadius);
			orbit.transform.setToTranslation(parentPosition);

			renderState.instancesAlways.add(orbit);
		}
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
