package ch.obermuhlner.infinitespace.game.ship;

public class CargoBay extends InternalComponent {

	public int space;

	public CargoBay () {
	}

	public CargoBay (int space) {
		this(0.1f * space, 0,  space * CargoBay.PRICE_UNIT, space);
	}
	
	public CargoBay (float mass, float power, int price, int space) {
		super(mass, power, price);

		this.space = space;
		this.volume = space * 5;
	}
}
