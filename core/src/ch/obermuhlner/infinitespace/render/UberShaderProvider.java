package ch.obermuhlner.infinitespace.render;

import ch.obermuhlner.infinitespace.UserData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

public class UberShaderProvider extends BaseShaderProvider {

	public static UberShaderProvider DEFAULT = new UberShaderProvider("default");
	public static UberShaderProvider EMISSIVE = new UberShaderProvider("emissive");
	
	public static final String GAS_PLANET_SHADER = "jupiter";
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
			return new PlanetShader(vert, frag);
		}
		return new DefaultShader(renderable, new DefaultShader.Config(vert, frag));
	}

}
