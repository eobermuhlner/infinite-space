package ch.obermuhlner.infinitespace;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.infinitespace.game.ship.Ship;
import ch.obermuhlner.infinitespace.game.ship.ShipFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;

public class GameState {

	public static GameState INSTANCE = new GameState();
	
	private static Json json = new Json();
	
	private final Preferences preferences;
	
	public int starSystem;
	public final Vector3 position = new Vector3();
	public final Vector3 direction = new Vector3();
	public final Vector3 up = new Vector3();
	
	public long cash;
	public Ship ship;
	public List<CommodityItem> cargo;
	
	private GameState () {
		preferences = Gdx.app.getPreferences(GameState.class.getName());
	}
	
	public void load() {
		starSystem = preferences.getInteger("starSystem");
		
		{
			float x = preferences.getFloat("position.x", 10);
			float y = preferences.getFloat("position.y", 10);
			float z = preferences.getFloat("position.z", 10);
			position.set(x, y, z);
		}
		{
			float x = preferences.getFloat("direction.x", 0);
			float y = preferences.getFloat("direction.y", 0);
			float z = preferences.getFloat("direction.z", -1);
			direction.set(x, y, z);
		}
		{
			float x = preferences.getFloat("up.x", 0);
			float y = preferences.getFloat("up.y", 1);
			float z = preferences.getFloat("up.z", 0);
			up.set(x, y, z);
		}
		
		cash = preferences.getLong("cash", 1000);
		ship = json.fromJson(Ship.class, preferences.getString("ship", json.toJson(ShipFactory.getStandardShip())));
		@SuppressWarnings("unchecked")
		List<CommodityItem> loadedCargo = json.fromJson(ArrayList.class, preferences.getString("cargo", json.toJson(new ArrayList<CommodityItem>())));
		cargo = loadedCargo;
	}
	
	public void save() {
		preferences.putInteger("starSystem", starSystem);
		
		preferences.putFloat("position.x", position.x);
		preferences.putFloat("position.y", position.y);
		preferences.putFloat("position.z", position.z);
		
		preferences.putFloat("direction.x", direction.x);
		preferences.putFloat("direction.y", direction.y);
		preferences.putFloat("direction.z", direction.z);
		
		preferences.putFloat("up.x", up.x);
		preferences.putFloat("up.y", up.y);
		preferences.putFloat("up.z", up.z);
		
		preferences.putLong("cash", cash);
		preferences.putString("ship", json.toJson(ship));
		preferences.putString("cargo", json.toJson(cargo));
		
		preferences.flush();
	}
	
	public void reset() {
		preferences.clear();
		preferences.flush();
	}
}
