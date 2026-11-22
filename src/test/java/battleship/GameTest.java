package battleship;

import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - fire: 7
 * - getShots: 1
 * - getRepeatedShots: 1
 * - getInvalidShots: 1
 * - getHits: 1
 * - getSunkShips: 1
 * - getRemainingShips: 1
 * - validShot: 3
 * - repeatedShot: 2
 * - printBoard: 1
 * - printValidShots: 1
 * - printFleet: 1
 */
public class GameTest {

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet());
	}

	@AfterEach
	void tearDown() {
		game = null;
	}

	private Fleet fleetWithSingleBarge(IPosition pos) {
		Fleet fleet = new Fleet();
		fleet.addShip(new Barge(Compass.NORTH, pos));
		return fleet;
	}

	@Test
	@DisplayName("Construtor deve inicializar listas e contadores a zero")
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	@DisplayName("fireSingleShot com posicao fora do tabuleiro deve incrementar invalidShots")
	void fire2() {
		Position invalidPosition = new Position(-1, 5);
		game.fireSingleShot(invalidPosition, false);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	@DisplayName("fireSingleShot com tiro repetido deve incrementar repeatedShots")
	void fire3() {
		Position position = new Position(2, 3);
		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	@DisplayName("repeatedShot deve devolver true para posicao ja disparada")
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	@DisplayName("repeatedShot deve devolver false para posicao ainda nao disparada")
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	@DisplayName("getAlienMoves deve devolver uma jogada apos disparar uma rajada")
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	@DisplayName("getRemainingShips deve refletir navios adicionados e afundados na frota")
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}

	@Test
	@DisplayName("fireSingleShot deve registar tiro na agua, acerto e afundamento de navio")
	void testFireSingleShotWaterHitAndSink() {
		IPosition shipPos = new Position(0, 0);
		Game gameWithShip = new Game(fleetWithSingleBarge(shipPos));

		IGame.ShotResult waterResult = gameWithShip.fireSingleShot(new Position(5, 5), false);
		assertTrue(waterResult.valid());
		assertNull(waterResult.ship());
		assertEquals(0, gameWithShip.getHits());

		IGame.ShotResult hitAndSinkResult = gameWithShip.fireSingleShot(shipPos, false);
		assertTrue(hitAndSinkResult.valid());
		assertNotNull(hitAndSinkResult.ship());
		assertTrue(hitAndSinkResult.sunk());
		assertEquals(1, gameWithShip.getHits());
		assertEquals(1, gameWithShip.getSunkShips());
	}

	@Test
	@DisplayName("fireShots deve rejeitar lista com tamanho diferente de NUMBER_SHOTS")
	void testFireShotsInvalidCount() {
		assertThrows(IllegalArgumentException.class, () ->
				game.fireShots(List.of(new Position(0, 0))));
	}

	@Test
	@DisplayName("readEnemyFire deve ler tokens combinados ou separados e lancar excecao em input invalido")
	void testReadEnemyFire() {
		Game g1 = new Game(fleetWithSingleBarge(new Position(0, 0)));
		String json1 = g1.readEnemyFire(new Scanner("A1 B2 C3\n"));
		assertNotNull(json1);
		assertTrue(json1.contains("\"row\" : \"A\""));

		Game g2 = new Game(fleetWithSingleBarge(new Position(0, 0)));
		String json2 = g2.readEnemyFire(new Scanner("A 1 B 2 C 3\n"));
		assertNotNull(json2);
		assertEquals(1, g2.getAlienMoves().size());

		assertThrows(IllegalArgumentException.class, () ->
				game.readEnemyFire(new Scanner("A\n")));

		assertThrows(IllegalArgumentException.class, () ->
				game.readEnemyFire(new Scanner("A1 B2\n")));
	}

	@Test
	@DisplayName("randomEnemyFire deve gerar tiros, executar jogada e devolver JSON")
	void testRandomEnemyFire() {
		Game g = new Game(fleetWithSingleBarge(new Position(0, 0)));
		String json = g.randomEnemyFire();
		assertNotNull(json);
		assertEquals(1, g.getAlienMoves().size());
	}

	@Test
	@DisplayName("jsonShots, printBoard, printMyBoard, printAlienBoard e over devem funcionar sem erros")
	void testJsonShotsBoardsAndOver() {
		Game g = new Game(fleetWithSingleBarge(new Position(0, 0)));
		List<IPosition> shots = List.of(new Position(0, 0), new Position(0, 1));

		String json = Game.jsonShots(shots);
		assertTrue(json.contains("\"column\" : 1"));

		assertDoesNotThrow(() -> {
			Game.printBoard(g.getMyFleet(), List.of(), false, false);
			Game.printBoard(g.getMyFleet(), g.getAlienMoves(), true, true);
			g.printMyBoard(true, false);
			g.printAlienBoard(false, true);
			g.over();
		});
	}
	@Test
	@DisplayName("Deve cobrir getAlienFleet, getMyMoves, navio afundado no tabuleiro e randomEnemyFire com poucas posicoes")
	void testAlienFleetMyMovesSunkBoardAndLowCandidates() {
		assertNotNull(game.getAlienFleet());
		assertNotNull(game.getMyMoves());

		Fleet fleet = new Fleet();
		Ship barge = new Barge(Compass.NORTH, new Position(0, 0));
		fleet.addShip(barge);
		barge.sink();

		Game g = new Game(fleet);
		g.fireShots(List.of(
				new Position(0, 0),
				new Position(5, 5),
				new Position(6, 6)
		));

		assertDoesNotThrow(() -> {
			Game.printBoard(g.getMyFleet(), g.getAlienMoves(), true, true);
			g.printMyBoard(true, true);
		});

		// Preencher quase to do o tabuleiro (98 posições) para forçar o ramo else em randomEnemyFire
		List<IPosition> batch = new java.util.ArrayList<>();
		for (int r = 0; r < Game.BOARD_SIZE; r++) {
			for (int c = 0; c < Game.BOARD_SIZE; c++) {
				if (r == 9 && c >= 8) continue; // deixa apenas 2 posições livres (< NUMBER_SHOTS)
				batch.add(new Position(r, c));
				if (batch.size() == Game.NUMBER_SHOTS) {
					g.fireShots(List.copyOf(batch));
					batch.clear();
				}
			}
		}

		assertDoesNotThrow(g::randomEnemyFire);
	}
}