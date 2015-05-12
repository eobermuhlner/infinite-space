package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.game.ship.Ship;
import ch.obermuhlner.infinitespace.game.ship.ShipFactory;

public class TestShip {
	public static void main (String[] args) {
		testShip();
	}

	private static void testShip () {
		Ship ship = ShipFactory.getStandardShip();
		System.out.println(ship);
	}
}
