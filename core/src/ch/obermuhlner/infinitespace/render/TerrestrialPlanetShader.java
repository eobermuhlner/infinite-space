
package ch.obermuhlner.infinitespace.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TerrestrialPlanetShader implements Shader {

	private final String vertexProgram;
	private final String fragmentProgram;

	private ShaderProgram program;
	
	private int u_projViewTrans;
	private int u_worldTrans;
	private int u_diffuseTexture;
	private int u_time;

	private int u_heightMin;
	private int u_heightMax;
	private int u_heightFrequency;
	private int u_heightWater;
	private int u_iceLevel;
	private int u_colorNoise;
	private int u_colorFrequency;
	
	private int u_random0;
	private int u_random1;
	private int u_random2;
	private int u_random3;
	private int u_random4;
	private int u_random5;
	private int u_random6;
	private int u_random7;
	private int u_random8;
	private int u_random9;
	
	private RenderContext context;
	
	private float time;
	
	public TerrestrialPlanetShader (String vertexProgram, String fragmentProgram) {
		this.vertexProgram = vertexProgram;
		this.fragmentProgram = fragmentProgram;
	}
	
	@Override
	public void init () {
		program = new ShaderProgram(vertexProgram, fragmentProgram);
		if (!program.isCompiled()) {
			throw new GdxRuntimeException(program.getLog());
		}

		u_projViewTrans = program.getUniformLocation("u_projViewTrans");
		u_worldTrans = program.getUniformLocation("u_worldTrans");
		u_diffuseTexture = program.getUniformLocation("u_diffuseTexture");
		u_time = program.getUniformLocation("u_time");

		u_heightMin = program.getUniformLocation("u_heightMin");
		u_heightMax = program.getUniformLocation("u_heightMax");
		u_heightFrequency = program.getUniformLocation("u_heightFrequency");
		u_heightWater = program.getUniformLocation("u_heightWater");
		u_iceLevel = program.getUniformLocation("u_iceLevel");
		u_colorNoise = program.getUniformLocation("u_colorNoise");
		u_colorFrequency = program.getUniformLocation("u_colorFrequency");
		
		u_random0 = program.getUniformLocation("u_random0");
		u_random1 = program.getUniformLocation("u_random1");
		u_random2 = program.getUniformLocation("u_random2");
		u_random3 = program.getUniformLocation("u_random3");
		u_random4 = program.getUniformLocation("u_random4");
		u_random5 = program.getUniformLocation("u_random5");
		u_random6 = program.getUniformLocation("u_random6");
		u_random7 = program.getUniformLocation("u_random7");
		u_random8 = program.getUniformLocation("u_random8");
		u_random9 = program.getUniformLocation("u_random9");
	}

	@Override
	public void begin (Camera camera, RenderContext context) {
		this.context = context;
		
		context.setDepthTest(GL20.GL_LEQUAL);
		context.setCullFace(GL20.GL_BACK);

		program.begin();
		program.setUniformMatrix(u_projViewTrans, camera.combined);
		program.setUniformf(u_time, System.currentTimeMillis() / 1000.0f);
		
	}

	@Override
	public void end () {
		program.end();
	}

	@Override
	public void render (Renderable renderable) {
		// world transformation
		program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
		
		// diffuse texture
		TextureAttribute textureAttribute = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
		int textureUnit = context.textureBinder.bind(textureAttribute.textureDescription);
		program.setUniformi(u_diffuseTexture, textureUnit);

		// planet data
		program.setUniformf(u_heightMin, getFloatAttributeValue(renderable, TerrestrialPlanetFloatAttribute.HeightMin, 0.0f));
		program.setUniformf(u_heightMax, getFloatAttributeValue(renderable, TerrestrialPlanetFloatAttribute.HeightMax, 1.0f));
		program.setUniformf(u_heightFrequency, getFloatAttributeValue(renderable, TerrestrialPlanetFloatAttribute.HeightFrequency, 5f));
		program.setUniformf(u_heightWater, getFloatAttributeValue(renderable, TerrestrialPlanetFloatAttribute.HeightWater, 0.0f));
		program.setUniformf(u_iceLevel, getFloatAttributeValue(renderable, TerrestrialPlanetFloatAttribute.IceLevel, 0.0f));
		program.setUniformf(u_colorNoise, getFloatAttributeValue(renderable, TerrestrialPlanetFloatAttribute.ColorNoise, 0.2f));
		program.setUniformf(u_colorFrequency, getFloatAttributeValue(renderable, TerrestrialPlanetFloatAttribute.ColorFrequency, 20.0f));
		
		// random
		FloatArrayAttribute floatArrayAttribute = (FloatArrayAttribute)renderable.material.get(FloatArrayAttribute.FloatArray);
		program.setUniformf(u_random0, floatArrayAttribute.values[0]);
		program.setUniformf(u_random1, floatArrayAttribute.values[0]);
		program.setUniformf(u_random2, floatArrayAttribute.values[0]);
		program.setUniformf(u_random3, floatArrayAttribute.values[0]);
		program.setUniformf(u_random4, floatArrayAttribute.values[0]);
		program.setUniformf(u_random5, floatArrayAttribute.values[0]);
		program.setUniformf(u_random6, floatArrayAttribute.values[0]);
		program.setUniformf(u_random7, floatArrayAttribute.values[0]);
		program.setUniformf(u_random8, floatArrayAttribute.values[0]);
		program.setUniformf(u_random9, floatArrayAttribute.values[0]);

		// time
		program.setUniformf(u_time, time += Gdx.graphics.getDeltaTime());
		
		// mesh
		renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
	}

	private float getFloatAttributeValue(Renderable renderable, long attributeType, float defaultValue) {
		FloatAttribute floatAttribute = (FloatAttribute) renderable.material.get(attributeType);
		return floatAttribute == null ? defaultValue : floatAttribute.value;
	}

	@Override
	public void dispose () {
		program.dispose();
	}

	@Override
	public int compareTo (Shader other) {
		return 0;
	}

	@Override
	public boolean canRender (Renderable instance) {
		return true;
	}

}
