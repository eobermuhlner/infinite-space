
package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.model.UniverseModel;
import ch.obermuhlner.infinitespace.ui.welcome.WelcomeScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

public class InfiniteSpaceGame extends Game {
	
	public final AssetManager assetManager = new AssetManager();
	public final UniverseModel universeModel = new UniverseModel();
	public final NodeToRenderConverter genericNodeConverter = new NodeToRenderConverter(assetManager);

	@Override
	public void create () {
		{
			TextureParameter textureParameter = new TextureParameter();
			textureParameter.genMipMaps = true;
			textureParameter.minFilter = TextureFilter.MipMapLinearLinear;
			textureParameter.magFilter = TextureFilter.MipMapLinearLinear;
			for (String textureName : new String[] {
				"mercury.jpg", "venus.jpg", "earth.jpg", "mars.jpg", "jupiter.jpg", "saturn.jpg",
				"uranus.jpg", "neptune.jpg", "ceres.jpg", "phobos.jpg", "deimos.jpg", "moon.jpg", "io.jpg", "ganymede.jpg", "europa.jpg", "callisto.jpg",
				"mimas.jpg", "enceladus.jpg", "tethys.jpg", "dione.jpg", "rhea.jpg", "titan.jpg", "iapetus.jpg", "miranda.jpg", "ariel.jpg", "umbriel.jpg", "titania.jpg", "oberon.jpg", "triton.jpg",
				"clouds.png",
				"spaceship.jpg", "spaceship_emissive.jpg", "spaceship3.jpg", "pixelcity_windows7.jpg",
				}) {
				assetManager.load(InfiniteSpaceGame.getTexturePath(textureName), Texture.class, textureParameter);
			}
		}

		{
			TextureParameter textureParameter = new TextureParameter();
			textureParameter.minFilter = TextureFilter.Linear;
			textureParameter.magFilter = TextureFilter.Linear;
			for (String textureName : new String[] {
				"skybox_neg_x.png", "skybox_pos_x.png", "skybox_neg_y.png", "skybox_pos_y.png", "skybox_neg_z.png", "skybox_pos_z.png",  
				"terrestrial_colors.png", "mars_colors.png", "moon_colors.png", "lava_colors.png",
				}) {
				assetManager.load(InfiniteSpaceGame.getTexturePath(textureName), Texture.class);
			}
		}
			
		setScreen(new WelcomeScreen(this));
	}
	
	@Override
	public void dispose () {
		super.dispose();
		
		assetManager.dispose();
	}

	public static String getTexturePath (String textureName) {
		return "data/textures/" + textureName;
	}
	
}
