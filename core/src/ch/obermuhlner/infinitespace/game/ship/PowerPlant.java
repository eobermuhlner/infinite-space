package ch.obermuhlner.infinitespace.game.ship;

public class PowerPlant extends InternalComponent {

	protected PowerPlant () {
	}

	public PowerPlant(float power) {
		this(0.02f * power, power, (int) (2 * power));
	}
	
	public PowerPlant (float mass, float power, int price) {
		super(mass, power, price * PRICE_UNIT);
	}

}
