package ch.obermuhlner.infinitespace.game.ship;

public class PassengerBay extends InternalComponent {

	public enum Type {
		ECONOMY(1),
		BUSINESS(2),
		FIRST(4),
		VIP(6);
		
		public final int priceFactor;

		private Type(int priceFactor) {
			this.priceFactor = priceFactor;
		}
	}
	
	public Type type;
	
	public int space;

	public PassengerBay () {
	}

	public PassengerBay (Type type, int space) {
		this(space * 0.2f, -space, space * type.priceFactor * PRICE_UNIT, type, space);
	}
	
	public PassengerBay (float mass, float power, int price, Type type, int space) {
		super(mass, power, price);
	
		this.type = type;
		this.space = space;
		this.volume = space;
	}
}
