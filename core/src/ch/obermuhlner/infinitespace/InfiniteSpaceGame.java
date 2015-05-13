
package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.model.UniverseModel;
import ch.obermuhlner.infinitespace.ui.welcome.WelcomeScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class InfiniteSpaceGame extends Game {
	
	public final AssetManager assetManager = new AssetManager();
	public final UniverseModel universeModel = new UniverseModel();
	public final NodeToRenderConverter genericNodeConverter = new NodeToRenderConverter(assetManager);

	@Override
	public void create () {
		for (String textureName : new String[] {"mercury.jpg", "venus.jpg", "earth.jpg", "mars.jpg", "jupiter.jpg", "saturn.jpg",
			"uranus.jpg", "neptune.jpg", "ceres.jpg", "phobos.jpg", "deimos.jpg", "moon.jpg", "io.jpg", "ganymede.jpg", "europa.jpg", "callisto.jpg",
			"mimas.jpg", "enceladus.jpg", "tethys.jpg", "dione.jpg", "rhea.jpg", "titan.jpg", "iapetus.jpg", "miranda.jpg", "ariel.jpg", "umbriel.jpg", "titania.jpg", "oberon.jpg", "triton.jpg",
			"spaceship.jpg",
			"skybox_neg_x.png", "skybox_pos_x.png", "skybox_neg_y.png", "skybox_pos_y.png", "skybox_neg_z.png", "skybox_pos_z.png",  
			}) {
			assetManager.load(InfiniteSpaceGame.getTexturePath(textureName), Texture.class);
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
