package ch.obermuhlner.infinitespace.render;

import ch.obermuhlner.infinitespace.UserData;
import ch.obermuhlner.infinitespace.util.StopWatch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

public class UberShaderProvider extends BaseShaderProvider {

	public static UberShaderProvider DEFAULT = new UberShaderProvider("default");
	public static UberShaderProvider EMISSIVE = new UberShaderProvider("emissive");
	
	public static final String GAS_PLANET_SHADER = "jupiter";
	public static final String TERRESTRIAL_PLANET_SHADER = "terrestrial";
	public static final String SUN_SHADER = "sun";
	
	private String shaderName;

	public UberShaderProvider (String shaderName) {
		this.shaderName = shaderName;
	}
	
	@Override
	protected Shader createShader(Renderable renderable) {
		String name = shaderName;
		
		if (renderable.userData instanceof UserData) {
			UserData userData = (UserData)renderable.userData;
			if (userData.shaderName != null) {
				name = userData.shaderName;
			}
		}
		
		String vert = Gdx.files.internal("data/shaders/" + name + ".vertex.glsl").readString();
		String frag = Gdx.files.internal("data/shaders/" + name + ".fragment.glsl").readString();
		
		if (GAS_PLANET_SHADER.equals(name)) {
			return new GasPlanetShader(vert, frag);
		}
		if (TERRESTRIAL_PLANET_SHADER.equals(name)) {
			return new TerrestrialPlanetShader(renderable, vert, frag);
		}
		return new DefaultShader(renderable, new DefaultShader.Config(vert, frag));
	}
	
	private Shader createShader2(Renderable renderable) {
		String name = shaderName;
		
		if (renderable.userData instanceof UserData) {
			UserData userData = (UserData)renderable.userData;
			if (userData.shaderName != null) {
				name = userData.shaderName;
			}
		}
		
		String vert = Gdx.files.internal("data/shaders/" + name + ".vertex.glsl").readString();
		String frag = Gdx.files.internal("data/shaders/" + name + ".fragment.glsl").readString();
		
		if (GAS_PLANET_SHADER.equals(name)) {
			return new GasPlanetShader(vert, frag);
		}
		if (TERRESTRIAL_PLANET_SHADER.equals(name)) {
			return new TerrestrialPlanetShader(renderable, vert, frag);
		}
		return new DefaultShader(renderable, new DefaultShader.Config(vert, frag));
	}

}
