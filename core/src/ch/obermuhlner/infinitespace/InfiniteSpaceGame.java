
package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.model.UniverseModel;
import ch.obermuhlner.infinitespace.ui.welcome.WelcomeScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class InfiniteSpaceGame extends Game {
	
	public final AssetManager assetManager = new AssetManager();
	public final UniverseModel universeModel = new UniverseModel();

	@Override
	public void create () {
		{
			TextureParameter textureParameter = new TextureParameter();
			textureParameter.genMipMaps = true;
			textureParameter.minFilter = TextureFilter.MipMapLinearLinear;
			textureParameter.magFilter = TextureFilter.MipMapLinearLinear;
			for (String textureName : new String[] {
				"mercury.jpg", "venus.jpg", "earth.jpg", "mars.png", "jupiter.jpg", "saturn.jpg",
				"uranus.jpg", "neptune.jpg", "ceres.jpg", "phobos.jpg", "deimos.jpg", "moon.jpg", "io.jpg", "ganymede.jpg", "europa.jpg", "callisto.jpg",
				"mimas.jpg", "enceladus.jpg", "tethys.jpg", "dione.jpg", "rhea.jpg", "titan.jpg", "iapetus.jpg", "miranda.jpg", "ariel.jpg", "umbriel.jpg", "titania.jpg", "oberon.jpg", "triton.jpg",
				"earth_normals.jpg", "moon_normals.png", "mars_normals.png", "jupiter_normals.jpg",
				"normals_area1.png", "normals_area2.png", "normals_area3.png",
				"normals_crater_area1.png", "normals_crater_area2.png", "normals_crater_area3.png",
				"normals_crater_huge1.png", "normals_crater_huge2.png",
				"normals_crater_big1.png", "normals_crater_big2.png",
				"normals_crater_medium1.png", "normals_crater_medium2.png", "normals_crater_medium3.png",
				"normals_crater_small1.png", "normals_crater_small2.png", "normals_crater_small3.png", "normals_crater_small4.png", "normals_crater_small5.png",
				"normals_crater_tiny1.png", "normals_crater_tiny2.png", "normals_crater_tiny3.png",
				"normals_mountain1.png", "normals_mountain2.png",  
				"normals_vulcano_huge1.png",
				"normals_vulcano_big1.png", "normals_vulcano_big2.png", "normals_vulcano_big3.png",  
				"normals_vulcano_medium1.png", "normals_vulcano_medium2.png", "normals_vulcano_medium3.png", "normals_vulcano_medium4.png",    
				"normals_soft1.png",
				"clouds.png",
				}) {
				assetManager.load(InfiniteSpaceGame.getTexturePath(textureName), Texture.class, textureParameter);
			}
		}

		{
			TextureParameter textureParameter = new TextureParameter();
			textureParameter.genMipMaps = true;
			textureParameter.minFilter = TextureFilter.MipMapLinearLinear;
			textureParameter.magFilter = TextureFilter.MipMapLinearLinear;
			textureParameter.wrapU = TextureWrap.Repeat;
			textureParameter.wrapV = TextureWrap.Repeat;
			for (String textureName : new String[] {
				"spaceship.jpg", "spaceship_emissive.jpg",
				"spaceship3.jpg", "spaceship3_normals.png",
				"windows1.jpg", "windows1_specular.jpg",
				"windows2.jpg", "windows2_specular.jpg", 
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
