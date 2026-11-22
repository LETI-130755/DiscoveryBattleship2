package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Frigate.
 * Author: Tomás Santos (98458)
 * Date: 2026-11-22
 * Cyclomatic Complexity (CC) of the methods declared in Frigate:
 * - constructor Frigate(Compass, IPosition): 9  (switch with 4 cases + 4 for loops)
 *
 * The methods getSize, stillFloating, getPositions and get*MostPos are inherited
 * from Ship: they are only checked here through a Frigate, and are tested in
 * depth in ShipTest.
 */
class FrigateTest {

	private Frigate frigate;

	@BeforeEach
	void setUp() {
		frigate = new Frigate(Compass.NORTH, new Position(5, 5));
	}

	@AfterEach
	void tearDown() {
		frigate = null;
	}

	private static void assertPositionsEqual(List<IPosition> actual, IPosition... expected) {
		assertEquals(expected.length, actual.size(),
				"Error: expected " + expected.length + " positions but got " + actual.size());
		for (int i = 0; i < expected.length; i++)
			assertEquals(expected[i], actual.get(i),
					"Error: expected " + expected[i] + " at index " + i + " but got " + actual.get(i));
	}

	// ------------------------------------------------------------------
	// Constructor (CC = 9)
	// constructor1..4 cover each switch case and its for loop;
	// constructor5..7 check the attributes; constructor8..9 the invalid input.
	// ------------------------------------------------------------------

	@Test
	@DisplayName("constructor1: direção NORTH gera 4 posições na vertical a partir da âncora")
	void constructor1() {
		assertPositionsEqual(frigate.getPositions(),
				new Position(5, 5), new Position(6, 5), new Position(7, 5), new Position(8, 5));
	}

	@Test
	@DisplayName("constructor2: direção SOUTH gera 4 posições na vertical a partir da âncora")
	void constructor2() {
		Frigate south = new Frigate(Compass.SOUTH, new Position(5, 5));
		assertPositionsEqual(south.getPositions(),
				new Position(5, 5), new Position(6, 5), new Position(7, 5), new Position(8, 5));
	}

	@Test
	@DisplayName("constructor3: direção EAST gera 4 posições na horizontal a partir da âncora")
	void constructor3() {
		Frigate east = new Frigate(Compass.EAST, new Position(5, 5));
		assertPositionsEqual(east.getPositions(),
				new Position(5, 5), new Position(5, 6), new Position(5, 7), new Position(5, 8));
	}

	@Test
	@DisplayName("constructor4: direção WEST gera 4 posições na horizontal a partir da âncora")
	void constructor4() {
		Frigate west = new Frigate(Compass.WEST, new Position(5, 5));
		assertPositionsEqual(west.getPositions(),
				new Position(5, 5), new Position(5, 6), new Position(5, 7), new Position(5, 8));
	}

	@Test
	@DisplayName("constructor5: a categoria da Frigate é 'Fragata'")
	void constructor5() {
		assertEquals("Fragata", frigate.getCategory(),
				"Error: expected category 'Fragata' but got '" + frigate.getCategory() + "'");
	}

	@Test
	@DisplayName("constructor6: o tamanho é 4 e há exatamente 4 posições")
	void constructor6() {
		assertAll("size",
				() -> assertEquals(4, frigate.getSize(),
						"Error: expected size 4 but got " + frigate.getSize()),
				() -> assertEquals(4, frigate.getPositions().size(),
						"Error: expected 4 positions but got " + frigate.getPositions().size()));
	}

	@Test
	@DisplayName("constructor7: a direção indicada fica guardada no navio")
	void constructor7() {
		assertAll("bearing",
				() -> assertEquals(Compass.NORTH, frigate.getBearing(),
						"Error: expected NORTH but got " + frigate.getBearing()),
				() -> assertEquals(Compass.EAST, new Frigate(Compass.EAST, new Position(1, 1)).getBearing(),
						"Error: expected EAST for the second frigate"));
	}

	@Test
	@DisplayName("constructor8: direção nula provoca exceção")
	void constructor8() {
		assertThrows(NullPointerException.class, () -> new Frigate(null, new Position(5, 5)),
				"Error: expected NullPointerException for a null bearing");
	}

	@Test
	@DisplayName("constructor9: posição nula provoca exceção")
	void constructor9() {
		assertThrows(NullPointerException.class, () -> new Frigate(Compass.NORTH, null),
				"Error: expected NullPointerException for a null position");
	}

	// ------------------------------------------------------------------
	// Behaviour inherited from Ship, checked through a Frigate
	// ------------------------------------------------------------------

	@Test
	@DisplayName("stillFloating1: navio intacto está a flutuar")
	void stillFloating1() {
		assertTrue(frigate.stillFloating(), "Error: expected a floating frigate but got a sunk one");
	}

	@Test
	@DisplayName("stillFloating2: navio com apenas uma posição atingida continua a flutuar")
	void stillFloating2() {
		frigate.getPositions().get(0).shoot();
		assertTrue(frigate.stillFloating(),
				"Error: expected a floating frigate with one hit position but got a sunk one");
	}

	@Test
	@DisplayName("stillFloating3: navio com todas as posições atingidas está afundado")
	void stillFloating3() {
		frigate.getPositions().forEach(IPosition::shoot);
		assertFalse(frigate.stillFloating(),
				"Error: expected a sunk frigate with all positions hit but it is still floating");
	}

	@Test
	@DisplayName("get*MostPos1: extremos do navio na vertical (NORTH)")
	void getMostPos1() {
		assertAll("vertical extremes",
				() -> assertEquals(5, frigate.getTopMostPos(),
						"Error: expected top 5 but got " + frigate.getTopMostPos()),
				() -> assertEquals(8, frigate.getBottomMostPos(),
						"Error: expected bottom 8 but got " + frigate.getBottomMostPos()),
				() -> assertEquals(5, frigate.getLeftMostPos(),
						"Error: expected left 5 but got " + frigate.getLeftMostPos()),
				() -> assertEquals(5, frigate.getRightMostPos(),
						"Error: expected right 5 but got " + frigate.getRightMostPos()));
	}

	@Test
	@DisplayName("get*MostPos2: extremos do navio na horizontal (EAST)")
	void getMostPos2() {
		Frigate east = new Frigate(Compass.EAST, new Position(5, 5));
		assertAll("horizontal extremes",
				() -> assertEquals(5, east.getTopMostPos(),
						"Error: expected top 5 but got " + east.getTopMostPos()),
				() -> assertEquals(5, east.getBottomMostPos(),
						"Error: expected bottom 5 but got " + east.getBottomMostPos()),
				() -> assertEquals(5, east.getLeftMostPos(),
						"Error: expected left 5 but got " + east.getLeftMostPos()),
				() -> assertEquals(8, east.getRightMostPos(),
						"Error: expected right 8 but got " + east.getRightMostPos()));
	}
}