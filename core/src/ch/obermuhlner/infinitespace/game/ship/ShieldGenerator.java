package ch.obermuhlner.infinitespace.game.ship;

public class ShieldGenerator extends InternalComponent {

	public float shield;
	
	protected ShieldGenerator () {
	}
	
	public ShieldGenerator (float shield) {
		this(0.2f + 0.2f * shield, -0.1f * shield*shield, (int) (shield*shield * PRICE_UNIT), shield);

	}
	
	public ShieldGenerator (float mass, float power, int price, float shield) {
		super(mass, power, price);
		
		this.shield = shield;
	}

}
