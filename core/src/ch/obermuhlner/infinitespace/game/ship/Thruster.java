package ch.obermuhlner.infinitespace.game.ship;

public class Thruster extends ShipComponent {

	public float thrust;
	
	protected Thruster() {
	}
	
	public Thruster(float thrust) {
		super(thrust * 0.1f, -thrust * 0.5f, (int) (thrust * 10 * PRICE_UNIT));
		volume = thrust;
		
		this.thrust = thrust;
	}
	
}
