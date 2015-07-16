package ch.obermuhlner.infinitespace;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.infinitespace.game.ship.Ship;
import ch.obermuhlner.infinitespace.game.ship.ShipFactory;
import ch.obermuhlner.infinitespace.graphics.CenterPerspectiveCamera;
import ch.obermuhlner.infinitespace.util.DoubleVector3;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;

public class GameState {

	public static GameState INSTANCE = new GameState();
	
	private static Json json = new Json();
	
	private final Preferences preferences;
	
	public int starSystem;
	public final DoubleVector3 position = new DoubleVector3();
	public final DoubleVector3 direction = new DoubleVector3();
	public final DoubleVector3 up = new DoubleVector3();
	
	public long cash;
	public Ship ship;
	public List<CommodityItem> cargo;
	
	private GameState () {
		preferences = Gdx.app.getPreferences(GameState.class.getName());
	}
	
	public void pullFromCamera(CenterPerspectiveCamera camera, double sizeFactor) {
		position.set(camera.position);
		position.add(camera.positionOffset);
		position.mul(1.0 / sizeFactor);
		
		direction.set(camera.direction);
		up.set(camera.up);
	}
	
	public void pushToCamera(CenterPerspectiveCamera camera, double sizeFactor) {
		DoubleVector3.setToVector3(camera.position, GameState.INSTANCE.position, sizeFactor);
		camera.positionOffset.setZero();
		DoubleVector3.setToVector3(camera.direction, GameState.INSTANCE.direction);
		DoubleVector3.setToVector3(camera.up, GameState.INSTANCE.up);
	}
	
	private static double getDouble(Preferences preferences, String key, double defValue) {
		String string = preferences.getString(key);
		if (string != null) {
			try {
				return Double.parseDouble(string);
			} catch (NumberFormatException ex) {
				// ignore
			}
		}
		return defValue;
	}
	
	private static void putDouble(Preferences preferences, String key, double value) {
		preferences.putString(key, String.valueOf(value));
	}
	
	public void load() {
		starSystem = preferences.getInteger("starSystem");
		
		{
			double x = getDouble(preferences, "position.x", Units.EARTH_ORBIT_RADIUS);
			double y = getDouble(preferences, "position.y", Units.EARTH_ORBIT_RADIUS);
			double z = getDouble(preferences, "position.z", Units.EARTH_ORBIT_RADIUS);
			position.set(x, y, z);
		}
		{
			double x = getDouble(preferences, "direction.x", 0);
			double y = getDouble(preferences, "direction.y", 0);
			double z = getDouble(preferences, "direction.z", -1);
			direction.set(x, y, z);
		}
		{
			double x = getDouble(preferences, "up.x", 0);
			double y = getDouble(preferences, "up.y", 1);
			double z = getDouble(preferences, "up.z", 0);
			up.set(x, y, z);
		}
		
		cash = preferences.getLong("cash", 1000);
		ship = json.fromJson(Ship.class, preferences.getString("ship", json.toJson(ShipFactory.getStandardShip())));
		List<CommodityItem> loadedCargo = json.fromJson(ArrayList.class, preferences.getString("cargo", json.toJson(new ArrayList<CommodityItem>())));
		cargo = loadedCargo;
	}
	
	public void save() {
		preferences.putInteger("starSystem", starSystem);
		
		putDouble(preferences, "position.x", position.x);
		putDouble(preferences, "position.y", position.y);
		putDouble(preferences, "position.z", position.z);
		
		putDouble(preferences, "direction.x", direction.x);
		putDouble(preferences, "direction.y", direction.y);
		putDouble(preferences, "direction.z", direction.z);
		
		putDouble(preferences, "up.x", up.x);
		putDouble(preferences, "up.y", up.y);
		putDouble(preferences, "up.z", up.z);
		
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
