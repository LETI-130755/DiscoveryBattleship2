package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Fleet.
 * Author: Tomás Santos (98458)
 * Date: 2026-11-22
 * Cyclomatic Complexity (CC) of each tested method:
 * - constructor Fleet(): 1
 * - createRandom(): 4   (while + if + &&)
 * - getShips(): 1
 * - addShip(): 4        (if + two &&)
 * - getShipsLike(): 3   (for + if)
 * - getFloatingShips(): 3
 * - getSunkShips(): 3
 * - shipAt(): 3
 * - colisionRisk() [private]: 3
 * - printShips(): 2     (for)
 * - printStatus(): 1
 * - printShipsByCategory(): 1
 * - printFloatingShips(): 1
 * - printAllShips(): 1
 *
 * Note: the "assert" statements in Fleet throw AssertionError only when the JVM
 * runs with -ea (enabled by default in Maven Surefire and in IntelliJ JUnit runs).
 */
class FleetTest {

	private Fleet fleet;
	private PrintStream originalOut;
	private ByteArrayOutputStream capturedOut;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
		originalOut = System.out;
		capturedOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
		capturedOut = null;
		fleet = null;
	}

	// ------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------

	private static IShip barge(int row, int column) {
		return new Barge(Compass.NORTH, new Position(row, column));
	}

	private static IShip caravel(int row, int column) {
		return new Caravel(Compass.NORTH, new Position(row, column));
	}

	/** Shoots every position of the ship so that it is sunk. */
	private static void sink(IShip ship) {
		for (IPosition p : ship.getPositions())
			p.shoot();
	}

	private String output() {
		return capturedOut.toString(StandardCharsets.UTF_8);
	}

	private boolean invokeColisionRisk(IShip ship) throws Exception {
		Method m = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
		m.setAccessible(true);
		return (Boolean) m.invoke(fleet, ship);
	}

	// ------------------------------------------------------------------
	// Constructor (CC = 1)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("Construtor: cria uma frota não nula e sem navios")
	void constructor() {
		assertAll("constructor",
				() -> assertNotNull(fleet, "Error: expected a non-null Fleet instance but got null"),
				() -> assertNotNull(fleet.getShips(), "Error: expected a non-null ships list but got null"),
				() -> assertTrue(fleet.getShips().isEmpty(),
						"Error: expected an empty ships list but got " + fleet.getShips().size() + " ships"));
	}

	// ------------------------------------------------------------------
	// createRandom (CC = 4)
	// The branches "ship != null" == false and "while not entered" cannot be
	// reached: the ship types array is a non-empty constant and buildShip
	// never receives an unknown type. The "addShip == false" (retry) path is
	// exercised by createRandom3, which builds many random fleets.
	// ------------------------------------------------------------------

	@Test
	@DisplayName("createRandom1: devolve uma frota não nula com 11 navios")
	void createRandom1() {
		IFleet random = Fleet.createRandom();
		assertAll("random fleet",
				() -> assertNotNull(random, "Error: expected a non-null fleet but got null"),
				() -> assertEquals(11, random.getShips().size(),
						"Error: expected 11 ships but got " + random.getShips().size()));
	}

	@Test
	@DisplayName("createRandom2: a frota tem a composição correta por categoria")
	void createRandom2() {
		IFleet random = Fleet.createRandom();
		assertAll("fleet composition",
				() -> assertEquals(1, random.getShipsLike("Galeao").size(),
						"Error: expected 1 Galeao but got " + random.getShipsLike("Galeao").size()),
				() -> assertEquals(1, random.getShipsLike("Fragata").size(),
						"Error: expected 1 Fragata but got " + random.getShipsLike("Fragata").size()),
				() -> assertEquals(2, random.getShipsLike("Nau").size(),
						"Error: expected 2 Nau but got " + random.getShipsLike("Nau").size()),
				() -> assertEquals(3, random.getShipsLike("Caravela").size(),
						"Error: expected 3 Caravela but got " + random.getShipsLike("Caravela").size()),
				() -> assertEquals(4, random.getShipsLike("Barca").size(),
						"Error: expected 4 Barca but got " + random.getShipsLike("Barca").size()));
	}

	@Test
	@DisplayName("createRandom3: em 10 frotas aleatórias todos os navios estão no tabuleiro e sem colisões (exercita as tentativas rejeitadas)")
	void createRandom3() {
		assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
			for (int n = 0; n < 10; n++) {
				List<IShip> ships = Fleet.createRandom().getShips();
				assertEquals(11, ships.size(), "Error: expected 11 ships but got " + ships.size());
				for (int a = 0; a < ships.size(); a++) {
					final IShip shipA = ships.get(a);
					assertTrue(
							shipA.isInsideBoard(),
							"Error: expected ship inside the board but got " + shipA
					);
					for (int b = a + 1; b < ships.size(); b++) {
						final IShip shipB = ships.get(b);
						assertFalse(
								shipA.tooCloseTo(shipB),
								"Error: expected no collision risk between " + shipA + " and " + shipB
						);
					}
				}
			}
		}, "Error: expected createRandom3 to complete within 5 seconds but it timed out");
	}

	@Test
	@DisplayName("createRandom4: chamadas sucessivas devolvem instâncias diferentes")
	void createRandom4() {
		IFleet first = Fleet.createRandom();
		IFleet second = Fleet.createRandom();
		assertNotSame(first, second, "Error: expected different fleet instances but got the same one");
	}

	// ------------------------------------------------------------------
	// getShips (CC = 1)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("getShips: devolve a lista de navios, que reflete os navios adicionados")
	void getShips() {
		IShip ship = barge(1, 1);
		assertTrue(fleet.getShips().isEmpty(), "Error: expected an empty list initially");
		fleet.addShip(ship);
		assertAll("getShips",
				() -> assertEquals(1, fleet.getShips().size(),
						"Error: expected 1 ship but got " + fleet.getShips().size()),
				() -> assertSame(ship, fleet.getShips().get(0),
						"Error: expected the added ship at index 0"));
	}

	// ------------------------------------------------------------------
	// addShip (CC = 4)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("addShip1: navio válido (dentro do tabuleiro, sem colisão, frota não cheia) é adicionado")
	void addShip1() {
		IShip ship = barge(1, 1);
		assertAll("valid ship",
				() -> assertTrue(fleet.addShip(ship), "Error: expected true for a valid ship but got false"),
				() -> assertEquals(1, fleet.getShips().size(),
						"Error: expected 1 ship in the fleet but got " + fleet.getShips().size()));
	}

	@Test
	@DisplayName("addShip2: navio rejeitado quando a frota atingiu o limite de tamanho")
	void addShip2() {
		IShip rejected = null;
		// Barges spaced by 2 cells never collide, so the first refusal can only be due to the size limit.
		for (int r = 0; r < 10 && rejected == null; r += 2) {
			for (int c = 0; c < 10 && rejected == null; c += 2) {
				IShip b = barge(r, c);
				if (!fleet.addShip(b))
					rejected = b;
			}
		}
		final IShip rejectedShip = rejected;
		assertNotNull(rejectedShip, "Error: expected the fleet to refuse a ship once full but it never did");
		assertAll("full fleet",
				() -> assertTrue(fleet.getShips().size() >= Fleet.FLEET_SIZE,
						"Error: expected at least " + Fleet.FLEET_SIZE + " ships but got " + fleet.getShips().size()),
				() -> assertFalse(fleet.getShips().contains(rejectedShip),
						"Error: rejected ship should not be in the fleet"));
	}

	@Test
	@DisplayName("addShip3: navio fora do tabuleiro é rejeitado")
	void addShip3() {
		IShip outside = barge(99, 99);
		assertAll("outside board",
				() -> assertFalse(fleet.addShip(outside), "Error: expected false for a ship outside the board but got true"),
				() -> assertTrue(fleet.getShips().isEmpty(),
						"Error: expected an empty fleet but got " + fleet.getShips().size() + " ships"));
	}

	@Test
	@DisplayName("addShip4: navio sobreposto a outro é rejeitado")
	void addShip4() {
		fleet.addShip(barge(1, 1));
		assertAll("overlapping ship",
				() -> assertFalse(fleet.addShip(barge(1, 1)), "Error: expected false for an overlapping ship but got true"),
				() -> assertEquals(1, fleet.getShips().size(),
						"Error: expected 1 ship but got " + fleet.getShips().size()));
	}

	@Test
	@DisplayName("addShip5: navio encostado (sem sobreposição) a outro é rejeitado")
	void addShip5() {
		fleet.addShip(barge(1, 1));
		assertFalse(fleet.addShip(barge(1, 2)), "Error: expected false for an adjacent ship but got true");
	}

	@Test
	@DisplayName("addShip6: navio nulo provoca AssertionError")
	void addShip6() {
		assertThrows(AssertionError.class, () -> fleet.addShip(null),
				"Error: expected AssertionError for a null ship");
	}

	// ------------------------------------------------------------------
	// getShipsLike (CC = 3)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("getShipsLike1: frota vazia devolve lista vazia")
	void getShipsLike1() {
		assertTrue(fleet.getShipsLike("Barca").isEmpty(),
				"Error: expected an empty list for an empty fleet");
	}

	@Test
	@DisplayName("getShipsLike2: devolve só os navios da categoria pedida")
	void getShipsLike2() {
		IShip b = barge(1, 1);
		IShip c = caravel(5, 5);
		fleet.addShip(b);
		fleet.addShip(c);
		List<IShip> result = fleet.getShipsLike("Barca");
		assertAll("category match",
				() -> assertEquals(1, result.size(), "Error: expected 1 Barca but got " + result.size()),
				() -> assertSame(b, result.get(0), "Error: expected the barge to be returned"));
	}

	@Test
	@DisplayName("getShipsLike3: categoria sem navios devolve lista vazia")
	void getShipsLike3() {
		fleet.addShip(barge(1, 1));
		assertTrue(fleet.getShipsLike("Nau").isEmpty(),
				"Error: expected an empty list for a category without ships");
	}

	@Test
	@DisplayName("getShipsLike4: categoria nula provoca AssertionError")
	void getShipsLike4() {
		assertThrows(AssertionError.class, () -> fleet.getShipsLike(null),
				"Error: expected AssertionError for a null category");
	}

	// ------------------------------------------------------------------
	// getFloatingShips (CC = 3)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("getFloatingShips1: frota vazia devolve lista vazia")
	void getFloatingShips1() {
		assertTrue(fleet.getFloatingShips().isEmpty(), "Error: expected an empty list for an empty fleet");
	}

	@Test
	@DisplayName("getFloatingShips2: todos os navios a flutuar são devolvidos")
	void getFloatingShips2() {
		fleet.addShip(barge(1, 1));
		fleet.addShip(caravel(5, 5));
		assertEquals(2, fleet.getFloatingShips().size(),
				"Error: expected 2 floating ships but got " + fleet.getFloatingShips().size());
	}

	@Test
	@DisplayName("getFloatingShips3: navios afundados não são devolvidos")
	void getFloatingShips3() {
		IShip sunk = barge(1, 1);
		IShip floating = caravel(5, 5);
		fleet.addShip(sunk);
		fleet.addShip(floating);
		sink(sunk);
		List<IShip> result = fleet.getFloatingShips();
		assertAll("floating ships",
				() -> assertEquals(1, result.size(), "Error: expected 1 floating ship but got " + result.size()),
				() -> assertSame(floating, result.get(0), "Error: expected the caravel to be the floating ship"));
	}

	// ------------------------------------------------------------------
	// getSunkShips (CC = 3)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("getSunkShips1: frota vazia devolve lista vazia")
	void getSunkShips1() {
		assertTrue(fleet.getSunkShips().isEmpty(), "Error: expected an empty list for an empty fleet");
	}

	@Test
	@DisplayName("getSunkShips2: sem navios afundados devolve lista vazia")
	void getSunkShips2() {
		fleet.addShip(barge(1, 1));
		assertTrue(fleet.getSunkShips().isEmpty(), "Error: expected no sunk ships but got some");
	}

	@Test
	@DisplayName("getSunkShips3: devolve apenas os navios afundados")
	void getSunkShips3() {
		IShip sunk = barge(1, 1);
		IShip floating = caravel(5, 5);
		fleet.addShip(sunk);
		fleet.addShip(floating);
		sink(sunk);
		List<IShip> result = fleet.getSunkShips();
		assertAll("sunk ships",
				() -> assertEquals(1, result.size(), "Error: expected 1 sunk ship but got " + result.size()),
				() -> assertSame(sunk, result.get(0), "Error: expected the barge to be the sunk ship"));
	}

	// ------------------------------------------------------------------
	// shipAt (CC = 3)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("shipAt1: frota vazia devolve null")
	void shipAt1() {
		assertNull(fleet.shipAt(new Position(1, 1)), "Error: expected null for an empty fleet");
	}

	@Test
	@DisplayName("shipAt2: devolve o navio que ocupa a posição (não o primeiro da lista)")
	void shipAt2() {
		IShip first = barge(1, 1);
		IShip second = caravel(5, 5);
		fleet.addShip(first);
		fleet.addShip(second);
		assertSame(second, fleet.shipAt(new Position(5, 5)),
				"Error: expected the caravel at (5,5) but got another result");
	}

	@Test
	@DisplayName("shipAt3: posição livre devolve null")
	void shipAt3() {
		fleet.addShip(barge(1, 1));
		assertNull(fleet.shipAt(new Position(8, 8)), "Error: expected null for an empty position");
	}

	@Test
	@DisplayName("shipAt4: posição nula provoca AssertionError")
	void shipAt4() {
		assertThrows(AssertionError.class, () -> fleet.shipAt(null),
				"Error: expected AssertionError for a null position");
	}

	// ------------------------------------------------------------------
	// colisionRisk - private (CC = 3)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("colisionRisk1: frota vazia não tem risco de colisão")
	void colisionRisk1() throws Exception {
		assertFalse(invokeColisionRisk(barge(1, 1)), "Error: expected no collision risk in an empty fleet");
	}

	@Test
	@DisplayName("colisionRisk2: navio sobreposto/encostado tem risco de colisão")
	void colisionRisk2() throws Exception {
		fleet.addShip(barge(1, 1));
		assertTrue(invokeColisionRisk(barge(1, 1)), "Error: expected collision risk for an overlapping ship");
	}

	@Test
	@DisplayName("colisionRisk3: navio afastado de todos não tem risco de colisão")
	void colisionRisk3() throws Exception {
		fleet.addShip(barge(1, 1));
		fleet.addShip(barge(1, 4));
		assertFalse(invokeColisionRisk(barge(8, 8)), "Error: expected no collision risk for a distant ship");
	}

	@Test
	@DisplayName("colisionRisk4: navio nulo provoca AssertionError (via reflexão)")
	void colisionRisk4() {
		InvocationTargetException ex = assertThrows(InvocationTargetException.class,
				() -> invokeColisionRisk(null),
				"Error: expected InvocationTargetException wrapping an AssertionError");
		assertInstanceOf(AssertionError.class, ex.getCause(),
				"Error: expected the cause to be an AssertionError but got " + ex.getCause());
	}

	// ------------------------------------------------------------------
	// printShips (CC = 2)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("printShips1: lista vazia não imprime nada")
	void printShips1() {
		fleet.printShips(new ArrayList<>());
		assertEquals("", output(), "Error: expected no output for an empty list but got '" + output() + "'");
	}

	@Test
	@DisplayName("printShips2: imprime cada navio numa linha")
	void printShips2() {
		IShip b = barge(1, 1);
		IShip c = caravel(5, 5);
		fleet.printShips(List.of(b, c));
		String expected = b + System.lineSeparator() + c + System.lineSeparator();
		assertEquals(expected, output(), "Error: expected '" + expected + "' but got '" + output() + "'");
	}

	@Test
	@DisplayName("printShips3: lista nula provoca AssertionError")
	void printShips3() {
		assertThrows(AssertionError.class, () -> fleet.printShips(null),
				"Error: expected AssertionError for a null list");
	}

	// ------------------------------------------------------------------
	// printStatus (CC = 1)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("printStatus: imprime o número de navios a flutuar e afundados")
	void printStatus() {
		IShip sunk = barge(1, 1);
		fleet.addShip(sunk);
		fleet.addShip(caravel(5, 5));
		sink(sunk);
		fleet.printStatus();
		String expected = "Estado da Frota: 1 a flutuar, 1 afundados!" + System.lineSeparator();
		assertEquals(expected, output(), "Error: expected '" + expected + "' but got '" + output() + "'");
	}

	// ------------------------------------------------------------------
	// printShipsByCategory (CC = 1)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("printShipsByCategory: imprime só os navios da categoria pedida")
	void printShipsByCategory() {
		IShip b = barge(1, 1);
		fleet.addShip(b);
		fleet.addShip(caravel(5, 5));
		fleet.printShipsByCategory("Barca");
		String expected = b + System.lineSeparator();
		assertEquals(expected, output(), "Error: expected '" + expected + "' but got '" + output() + "'");
	}

	@Test
	@DisplayName("printShipsByCategory (nula): categoria nula provoca AssertionError")
	void printShipsByCategoryNull() {
		assertThrows(AssertionError.class, () -> fleet.printShipsByCategory(null),
				"Error: expected AssertionError for a null category");
	}

	// ------------------------------------------------------------------
	// printFloatingShips (CC = 1)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("printFloatingShips: imprime só os navios ainda a flutuar")
	void printFloatingShips() {
		IShip sunk = barge(1, 1);
		IShip floating = caravel(5, 5);
		fleet.addShip(sunk);
		fleet.addShip(floating);
		sink(sunk);
		fleet.printFloatingShips();
		String expected = floating + System.lineSeparator();
		assertEquals(expected, output(), "Error: expected '" + expected + "' but got '" + output() + "'");
	}

	// ------------------------------------------------------------------
	// printAllShips (CC = 1)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("printAllShips: imprime todos os navios da frota")
	void printAllShips() {
		IShip b = barge(1, 1);
		IShip c = caravel(5, 5);
		fleet.addShip(b);
		fleet.addShip(c);
		fleet.printAllShips();
		String expected = b + System.lineSeparator() + c + System.lineSeparator();
		assertEquals(expected, output(), "Error: expected '" + expected + "' but got '" + output() + "'");
	}
}