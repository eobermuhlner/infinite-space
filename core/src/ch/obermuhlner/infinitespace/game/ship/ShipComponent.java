package ch.obermuhlner.infinitespace.game.ship;

public class ShipComponent {

	protected static int PRICE_UNIT = 1000;
	
	public String name = "";
	public float mass;
	public float power;
	public int price;
	public float volume = 1f;
	
	protected ShipComponent () {
	}
	
	public ShipComponent (float mass, float power, int price) {
		this.mass = mass;
		this.power = power;
		this.price = price;
	}
	
}
