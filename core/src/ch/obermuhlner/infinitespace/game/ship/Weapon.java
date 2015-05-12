package ch.obermuhlner.infinitespace.game.ship;

public class Weapon extends ShipComponent {

	public enum Type {
		BULLET,
		LASER,
		PLASMA
	};
	
	public float hit;
	public float fireRate; //s
	public Type type;

	protected Weapon () {
	}

	public Weapon (Type type, float hit, float fireRate) {
		this(hit, hit, (int) (hit / fireRate * PRICE_UNIT), type, hit, fireRate);
	}
	
	public Weapon (float mass, float power, int price, Type type, float hit, float fireRate) {
		super(mass, power, price);
		
		this.type = type;
		this.hit = hit;
		this.fireRate = fireRate;
	}
}
