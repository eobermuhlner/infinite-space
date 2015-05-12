package ch.obermuhlner.infinitespace.game.ship;

public class Hull extends ShipComponent {

	public enum Type {
		ALUMINIUM(0.1f, 0.2f),
		STEEL(1.0f, 1.0f),
		TITANIUM(0.6f, 1.2f),
		PLASTEEL(0.8f, 2.0f);
	
		public final float weightFactor;
		public final float strengthFactor;

		private Type (float weightFactor, float strengthFactor) {
			this.weightFactor = weightFactor;
			this.strengthFactor = strengthFactor;
		}
	}
	
	public Type type;
	public float strength;
	public float allowedMass;
	
	protected Hull () {
	}
	
	protected Hull(Type type, float strength, float allowedMass) {
		this(strength / type.strengthFactor * type.weightFactor * allowedMass * 0.1f, 0, (int) (strength * type.strengthFactor / type.weightFactor * allowedMass * 100 * PRICE_UNIT), type, strength, allowedMass);
	}
	
	public Hull (float mass, float power, int price, Type type, float strength, float allowedMass) {
		super(mass, power, price);
		
		this.type = type;
		this.strength = strength;
		this.allowedMass = allowedMass;
	}

}
