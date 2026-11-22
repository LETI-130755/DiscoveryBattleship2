package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Ship.
 * Author: Tomás Santos (98458)
 * Date: 2026-11-22
 * Cyclomatic Complexity (CC) of each tested method:
 * - buildShip(): 6                 (switch with 5 cases)
 * - constructor Ship(): 1
 * - getCategory(): 1
 * - getPositions(): 1
 * - getAdjacentPositions(): 5      (2 for + if + &&)
 * - getPosition(): 1
 * - getBearing(): 1
 * - getSize(): 1
 * - stillFloating(): 3             (for + if)
 * - getTopMostPos(): 3             (for + if)
 * - getBottomMostPos(): 3
 * - getLeftMostPos(): 3
 * - getRightMostPos(): 3
 * - occupies(): 3                  (for + if)
 * - tooCloseTo(IShip): 3           (while + if)
 * - tooCloseTo(IPosition): 3       (for + if)
 * - isInsideBoard(): 4             (three &&)
 * - shoot(): 3                     (for + if)
 * - sink(): 2                      (for)
 * - toString(): 1
 *
 * Ship is abstract: a concrete Barge is used for the basic cases and a small
 * auxiliary subclass (TestShip) is used when specific positions are needed.
 * The "assert" statements need the JVM option -ea (default in IntelliJ and Maven).
 */
class ShipTest {

    /** Auxiliary concrete ship with explicitly chosen positions. */
    private static class TestShip extends Ship {
        TestShip(String category, Compass bearing, IPosition pos, IPosition... cells) {
            super(category, bearing, pos, cells.length);
            for (IPosition c : cells)
                getPositions().add(c);
        }
    }

    private Ship ship;        // Barge at (5,5)
    private Ship shipBorder;  // Barge at (0,0)

    @BeforeEach
    void setUp() {
        ship = new Barge(Compass.NORTH, new Position(5, 5));
        shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
    }

    @AfterEach
    void tearDown() {
        ship = null;
        shipBorder = null;
    }

    private static TestShip custom(Position... cells) {
        return new TestShip("Teste", Compass.NORTH, cells[0], cells);
    }

    // ------------------------------------------------------------------
    // buildShip (CC = 6)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("buildShip1: 'barca' constrói uma Barge")
    void buildShip1() {
        Ship s = Ship.buildShip("barca", Compass.NORTH, new Position(5, 5));
        assertInstanceOf(Barge.class, s, "Error: expected a Barge but got " + s);
    }

    @Test
    @DisplayName("buildShip2: 'caravela' constrói uma Caravel")
    void buildShip2() {
        Ship s = Ship.buildShip("caravela", Compass.NORTH, new Position(5, 5));
        assertInstanceOf(Caravel.class, s, "Error: expected a Caravel but got " + s);
    }

    @Test
    @DisplayName("buildShip3: 'nau' constrói uma Carrack")
    void buildShip3() {
        Ship s = Ship.buildShip("nau", Compass.NORTH, new Position(5, 5));
        assertInstanceOf(Carrack.class, s, "Error: expected a Carrack but got " + s);
    }

    @Test
    @DisplayName("buildShip4: 'fragata' constrói uma Frigate")
    void buildShip4() {
        Ship s = Ship.buildShip("fragata", Compass.NORTH, new Position(5, 5));
        assertInstanceOf(Frigate.class, s, "Error: expected a Frigate but got " + s);
    }

    @Test
    @DisplayName("buildShip5: 'galeao' constrói um Galleon")
    void buildShip5() {
        Ship s = Ship.buildShip("galeao", Compass.NORTH, new Position(5, 5));
        assertInstanceOf(Galleon.class, s, "Error: expected a Galleon but got " + s);
    }

    @Test
    @DisplayName("buildShip6: tipo desconhecido (ou com maiúsculas) devolve null")
    void buildShip6() {
        assertAll("unknown kinds",
                () -> assertNull(Ship.buildShip("submarino", Compass.NORTH, new Position(5, 5)),
                        "Error: expected null for an unknown kind but got a ship"),
                () -> assertNull(Ship.buildShip("Barca", Compass.NORTH, new Position(5, 5)),
                        "Error: expected null for a kind with different case but got a ship"));
    }

    @Test
    @DisplayName("buildShip7: o navio construído guarda a direção e a posição indicadas")
    void buildShip7() {
        Position p = new Position(3, 4);
        Ship s = Ship.buildShip("barca", Compass.EAST, p);
        assertAll("built ship",
                () -> assertEquals(Compass.EAST, s.getBearing(), "Error: expected EAST but got " + s.getBearing()),
                () -> assertEquals(p, s.getPosition(), "Error: expected " + p + " but got " + s.getPosition()));
    }

    @Test
    @DisplayName("buildShip8: argumentos nulos provocam AssertionError")
    void buildShip8() {
        assertAll("null arguments",
                () -> assertThrows(AssertionError.class,
                        () -> Ship.buildShip(null, Compass.NORTH, new Position(5, 5)),
                        "Error: expected AssertionError for a null kind"),
                () -> assertThrows(AssertionError.class,
                        () -> Ship.buildShip("barca", null, new Position(5, 5)),
                        "Error: expected AssertionError for a null bearing"),
                () -> assertThrows(AssertionError.class,
                        () -> Ship.buildShip("barca", Compass.NORTH, null),
                        "Error: expected AssertionError for a null position"));
    }

    // ------------------------------------------------------------------
    // Constructor (CC = 1)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("constructor1: atributos ficam guardados corretamente")
    void constructor1() {
        Position anchor = new Position(2, 3);
        TestShip s = new TestShip("Teste", Compass.SOUTH, anchor, new Position(2, 3));
        assertAll("constructor",
                () -> assertEquals("Teste", s.getCategory(), "Error: expected category 'Teste' but got " + s.getCategory()),
                () -> assertEquals(Compass.SOUTH, s.getBearing(), "Error: expected SOUTH but got " + s.getBearing()),
                () -> assertSame(anchor, s.getPosition(), "Error: expected the same anchor position"),
                () -> assertEquals(1, s.getSize(), "Error: expected size 1 but got " + s.getSize()),
                () -> assertNotNull(s.getPositions(), "Error: expected a non-null positions list"));
    }

    @Test
    @DisplayName("constructor2: categoria nula provoca NullPointerException com mensagem")
    void constructor2() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new TestShip(null, Compass.NORTH, new Position(0, 0)),
                "Error: expected NullPointerException for a null category");
        assertEquals("Ship's category must not be null", ex.getMessage(),
                "Error: unexpected message '" + ex.getMessage() + "'");
    }

    @Test
    @DisplayName("constructor3: direção nula provoca NullPointerException com mensagem")
    void constructor3() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new TestShip("Teste", null, new Position(0, 0)),
                "Error: expected NullPointerException for a null bearing");
        assertEquals("Ship's bearing must not be null", ex.getMessage(),
                "Error: unexpected message '" + ex.getMessage() + "'");
    }

    @Test
    @DisplayName("constructor4: posição nula provoca NullPointerException com mensagem")
    void constructor4() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new TestShip("Teste", Compass.NORTH, null),
                "Error: expected NullPointerException for a null position");
        assertEquals("Ship's position must not be null", ex.getMessage(),
                "Error: unexpected message '" + ex.getMessage() + "'");
    }

    // ------------------------------------------------------------------
    // Simple getters (CC = 1 each)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getCategory: devolve a categoria do navio")
    void getCategory() {
        assertEquals("Barca", ship.getCategory(), "Error: expected 'Barca' but got " + ship.getCategory());
    }

    @Test
    @DisplayName("getPositions: devolve as posições ocupadas pelo navio")
    void getPositions() {
        List<IPosition> positions = ship.getPositions();
        assertAll("positions",
                () -> assertEquals(1, positions.size(), "Error: expected 1 position but got " + positions.size()),
                () -> assertEquals(new Position(5, 5), positions.get(0),
                        "Error: expected (5,5) but got " + positions.get(0)));
    }

    @Test
    @DisplayName("getPosition: devolve a posição âncora do navio")
    void getPosition() {
        assertEquals(new Position(5, 5), ship.getPosition(),
                "Error: expected (5,5) but got " + ship.getPosition());
    }

    @Test
    @DisplayName("getBearing: devolve a direção do navio")
    void getBearing() {
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: expected NORTH but got " + ship.getBearing());
    }

    @Test
    @DisplayName("getSize: devolve o tamanho do navio")
    void getSize() {
        assertEquals(1, ship.getSize(), "Error: expected size 1 but got " + ship.getSize());
    }

    // ------------------------------------------------------------------
    // getAdjacentPositions (CC = 5)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAdjacentPositions1: barca no meio do tabuleiro tem 8 posições adjacentes")
    void getAdjacentPositions1() {
        List<IPosition> adj = ship.getAdjacentPositions();
        assertAll("middle barge",
                () -> assertEquals(8, adj.size(), "Error: expected 8 adjacent positions but got " + adj.size()),
                () -> assertFalse(adj.contains(new Position(5, 5)),
                        "Error: adjacent positions must not include the ship's own position"));
    }

    @Test
    @DisplayName("getAdjacentPositions2: barca no canto tem 3 posições adjacentes")
    void getAdjacentPositions2() {
        assertEquals(3, shipBorder.getAdjacentPositions().size(),
                "Error: expected 3 adjacent positions but got " + shipBorder.getAdjacentPositions().size());
    }

    @Test
    @DisplayName("getAdjacentPositions3: navio vertical de 2 posições tem 10 adjacentes, sem repetidas nem próprias")
    void getAdjacentPositions3() {
        TestShip vertical = custom(new Position(5, 5), new Position(6, 5));
        List<IPosition> adj = vertical.getAdjacentPositions();
        assertEquals(10, adj.size(), "Error: expected 10 adjacent positions but got " + adj.size());
        for (IPosition a : adj) {
            assertFalse(vertical.getPositions().contains(a), "Error: own position " + a + " listed as adjacent");
            assertEquals(adj.indexOf(a), adj.lastIndexOf(a), "Error: duplicate adjacent position " + a);
        }
    }

    @Test
    @DisplayName("getAdjacentPositions4: navio horizontal de 2 posições tem 10 adjacentes")
    void getAdjacentPositions4() {
        TestShip horizontal = custom(new Position(5, 5), new Position(5, 6));
        assertEquals(10, horizontal.getAdjacentPositions().size(),
                "Error: expected 10 adjacent positions but got " + horizontal.getAdjacentPositions().size());
    }

    // ------------------------------------------------------------------
    // stillFloating (CC = 3)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("stillFloating1: navio intacto está a flutuar")
    void stillFloating1() {
        assertTrue(custom(new Position(5, 5), new Position(6, 5)).stillFloating(),
                "Error: expected a floating ship but got a sunk one");
    }

    @Test
    @DisplayName("stillFloating2: só a primeira posição atingida, continua a flutuar")
    void stillFloating2() {
        TestShip s = custom(new Position(5, 5), new Position(6, 5));
        s.getPositions().get(0).shoot();
        assertTrue(s.stillFloating(), "Error: expected a floating ship with one hit position but got a sunk one");
    }

    @Test
    @DisplayName("stillFloating3: todas as posições atingidas, navio afundado")
    void stillFloating3() {
        TestShip s = custom(new Position(5, 5), new Position(6, 5));
        s.getPositions().forEach(IPosition::shoot);
        assertFalse(s.stillFloating(), "Error: expected a sunk ship but it is still floating");
    }

    // ------------------------------------------------------------------
    // getTopMostPos (CC = 3)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getTopMostPos1: navio de uma posição devolve a sua linha")
    void getTopMostPos1() {
        assertEquals(5, ship.getTopMostPos(), "Error: expected top 5 but got " + ship.getTopMostPos());
    }

    @Test
    @DisplayName("getTopMostPos2: linhas crescentes, o topo é a primeira")
    void getTopMostPos2() {
        TestShip s = custom(new Position(5, 5), new Position(6, 5), new Position(7, 5));
        assertEquals(5, s.getTopMostPos(), "Error: expected top 5 but got " + s.getTopMostPos());
    }

    @Test
    @DisplayName("getTopMostPos3: linhas decrescentes, o topo é a última")
    void getTopMostPos3() {
        TestShip s = custom(new Position(7, 5), new Position(6, 5), new Position(5, 5));
        assertEquals(5, s.getTopMostPos(), "Error: expected top 5 but got " + s.getTopMostPos());
    }

    // ------------------------------------------------------------------
    // getBottomMostPos (CC = 3)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getBottomMostPos1: navio de uma posição devolve a sua linha")
    void getBottomMostPos1() {
        assertEquals(5, ship.getBottomMostPos(), "Error: expected bottom 5 but got " + ship.getBottomMostPos());
    }

    @Test
    @DisplayName("getBottomMostPos2: linhas decrescentes, a base é a primeira")
    void getBottomMostPos2() {
        TestShip s = custom(new Position(7, 5), new Position(6, 5), new Position(5, 5));
        assertEquals(7, s.getBottomMostPos(), "Error: expected bottom 7 but got " + s.getBottomMostPos());
    }

    @Test
    @DisplayName("getBottomMostPos3: linhas crescentes, a base é a última")
    void getBottomMostPos3() {
        TestShip s = custom(new Position(5, 5), new Position(6, 5), new Position(7, 5));
        assertEquals(7, s.getBottomMostPos(), "Error: expected bottom 7 but got " + s.getBottomMostPos());
    }

    // ------------------------------------------------------------------
    // getLeftMostPos (CC = 3)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getLeftMostPos1: navio de uma posição devolve a sua coluna")
    void getLeftMostPos1() {
        assertEquals(5, ship.getLeftMostPos(), "Error: expected left 5 but got " + ship.getLeftMostPos());
    }

    @Test
    @DisplayName("getLeftMostPos2: colunas crescentes, a esquerda é a primeira")
    void getLeftMostPos2() {
        TestShip s = custom(new Position(5, 5), new Position(5, 6), new Position(5, 7));
        assertEquals(5, s.getLeftMostPos(), "Error: expected left 5 but got " + s.getLeftMostPos());
    }

    @Test
    @DisplayName("getLeftMostPos3: colunas decrescentes, a esquerda é a última")
    void getLeftMostPos3() {
        TestShip s = custom(new Position(5, 7), new Position(5, 6), new Position(5, 5));
        assertEquals(5, s.getLeftMostPos(), "Error: expected left 5 but got " + s.getLeftMostPos());
    }

    // ------------------------------------------------------------------
    // getRightMostPos (CC = 3)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getRightMostPos1: navio de uma posição devolve a sua coluna")
    void getRightMostPos1() {
        assertEquals(5, ship.getRightMostPos(), "Error: expected right 5 but got " + ship.getRightMostPos());
    }

    @Test
    @DisplayName("getRightMostPos2: colunas decrescentes, a direita é a primeira")
    void getRightMostPos2() {
        TestShip s = custom(new Position(5, 7), new Position(5, 6), new Position(5, 5));
        assertEquals(7, s.getRightMostPos(), "Error: expected right 7 but got " + s.getRightMostPos());
    }

    @Test
    @DisplayName("getRightMostPos3: colunas crescentes, a direita é a última")
    void getRightMostPos3() {
        TestShip s = custom(new Position(5, 5), new Position(5, 6), new Position(5, 7));
        assertEquals(7, s.getRightMostPos(), "Error: expected right 7 but got " + s.getRightMostPos());
    }

    // ------------------------------------------------------------------
    // occupies (CC = 3)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("occupies1: posição da primeira célula está ocupada")
    void occupies1() {
        assertTrue(ship.occupies(new Position(5, 5)), "Error: expected (5,5) to be occupied");
    }

    @Test
    @DisplayName("occupies2: posição de uma célula posterior também está ocupada")
    void occupies2() {
        TestShip s = custom(new Position(5, 5), new Position(6, 5));
        assertTrue(s.occupies(new Position(6, 5)), "Error: expected (6,5) to be occupied");
    }

    @Test
    @DisplayName("occupies3: posição livre não está ocupada")
    void occupies3() {
        assertFalse(ship.occupies(new Position(1, 1)), "Error: expected (1,1) not to be occupied");
    }

    @Test
    @DisplayName("occupies4: posição nula provoca AssertionError")
    void occupies4() {
        assertThrows(AssertionError.class, () -> ship.occupies(null),
                "Error: expected AssertionError for a null position");
    }

    // ------------------------------------------------------------------
    // tooCloseTo(IShip) (CC = 3)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("tooCloseToShip1: navio com a primeira posição adjacente está demasiado perto")
    void tooCloseToShip1() {
        assertTrue(ship.tooCloseTo(new Barge(Compass.NORTH, new Position(5, 6))),
                "Error: expected ships to be too close");
    }

    @Test
    @DisplayName("tooCloseToShip2: navio com uma posição posterior adjacente está demasiado perto")
    void tooCloseToShip2() {
        TestShip other = custom(new Position(9, 9), new Position(5, 6));
        assertTrue(ship.tooCloseTo(other), "Error: expected ships to be too close via the second position");
    }

    @Test
    @DisplayName("tooCloseToShip3: navio afastado não está demasiado perto")
    void tooCloseToShip3() {
        assertFalse(ship.tooCloseTo(new Barge(Compass.NORTH, new Position(9, 9))),
                "Error: expected ships not to be too close");
    }

    @Test
    @DisplayName("tooCloseToShip4: navio nulo provoca AssertionError")
    void tooCloseToShip4() {
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IShip) null),
                "Error: expected AssertionError for a null ship");
    }

    // ------------------------------------------------------------------
    // tooCloseTo(IPosition) (CC = 3)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("tooCloseToPosition1: posição adjacente à primeira célula está demasiado perto")
    void tooCloseToPosition1() {
        assertAll("adjacent positions",
                () -> assertTrue(ship.tooCloseTo(new Position(5, 6)), "Error: expected (5,6) to be too close"),
                () -> assertTrue(ship.tooCloseTo(new Position(4, 5)), "Error: expected (4,5) to be too close"));
    }

    @Test
    @DisplayName("tooCloseToPosition2: posição adjacente a uma célula posterior está demasiado perto")
    void tooCloseToPosition2() {
        TestShip s = custom(new Position(5, 5), new Position(6, 5));
        assertTrue(s.tooCloseTo(new Position(7, 5)), "Error: expected (7,5) to be too close to the second cell");
    }

    @Test
    @DisplayName("tooCloseToPosition3: posição afastada não está demasiado perto")
    void tooCloseToPosition3() {
        assertFalse(ship.tooCloseTo(new Position(7, 7)), "Error: expected (7,7) not to be too close");
    }

    @Test
    @DisplayName("tooCloseToPosition4: posição nula provoca AssertionError")
    void tooCloseToPosition4() {
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IPosition) null),
                "Error: expected AssertionError for a null position");
    }

    // ------------------------------------------------------------------
    // isInsideBoard (CC = 4)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("isInsideBoard1: navio dentro do tabuleiro, incluindo os cantos extremos")
    void isInsideBoard1() {
        int last = Game.BOARD_SIZE - 1;
        assertAll("inside",
                () -> assertTrue(ship.isInsideBoard(), "Error: expected (5,5) to be inside the board"),
                () -> assertTrue(shipBorder.isInsideBoard(), "Error: expected (0,0) to be inside the board"),
                () -> assertTrue(custom(new Position(last, last)).isInsideBoard(),
                        "Error: expected (" + last + "," + last + ") to be inside the board"));
    }

    @Test
    @DisplayName("isInsideBoard2: coluna à esquerda do tabuleiro está fora")
    void isInsideBoard2() {
        assertFalse(custom(new Position(5, -1)).isInsideBoard(), "Error: expected column -1 to be outside");
    }

    @Test
    @DisplayName("isInsideBoard3: coluna à direita do tabuleiro está fora")
    void isInsideBoard3() {
        assertFalse(custom(new Position(5, Game.BOARD_SIZE)).isInsideBoard(),
                "Error: expected column " + Game.BOARD_SIZE + " to be outside");
    }

    @Test
    @DisplayName("isInsideBoard4: linha acima do tabuleiro está fora")
    void isInsideBoard4() {
        assertFalse(custom(new Position(-1, 5)).isInsideBoard(), "Error: expected row -1 to be outside");
    }

    @Test
    @DisplayName("isInsideBoard5: linha abaixo do tabuleiro está fora")
    void isInsideBoard5() {
        assertFalse(custom(new Position(Game.BOARD_SIZE, 5)).isInsideBoard(),
                "Error: expected row " + Game.BOARD_SIZE + " to be outside");
    }

    // ------------------------------------------------------------------
    // shoot (CC = 3)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("shoot1: tiro na posição do navio marca-a como atingida")
    void shoot1() {
        ship.shoot(new Position(5, 5));
        assertTrue(ship.getPositions().get(0).isHit(), "Error: expected position (5,5) to be hit");
    }

    @Test
    @DisplayName("shoot2: tiro noutra posição não atinge o navio")
    void shoot2() {
        ship.shoot(new Position(0, 0));
        assertFalse(ship.getPositions().get(0).isHit(), "Error: expected position (5,5) not to be hit");
    }

    @Test
    @DisplayName("shoot3: tiro na segunda célula atinge só essa célula")
    void shoot3() {
        TestShip s = custom(new Position(5, 5), new Position(6, 5));
        s.shoot(new Position(6, 5));
        assertAll("second cell",
                () -> assertFalse(s.getPositions().get(0).isHit(), "Error: expected (5,5) not to be hit"),
                () -> assertTrue(s.getPositions().get(1).isHit(), "Error: expected (6,5) to be hit"));
    }

    @Test
    @DisplayName("shoot4: posição nula provoca AssertionError")
    void shoot4() {
        assertThrows(AssertionError.class, () -> ship.shoot(null),
                "Error: expected AssertionError for a null position");
    }

    @Test
    @DisplayName("shoot5: posição fora do tabuleiro provoca AssertionError")
    void shoot5() {
        assertThrows(AssertionError.class, () -> ship.shoot(new Position(99, 99)),
                "Error: expected AssertionError for a position outside the board");
    }

    // ------------------------------------------------------------------
    // sink (CC = 2)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("sink1: navio sem posições não provoca erros")
    void sink1() {
        TestShip empty = new TestShip("Teste", Compass.NORTH, new Position(0, 0));
        assertDoesNotThrow(empty::sink, "Error: sinking a ship with no positions should not throw");
    }

    @Test
    @DisplayName("sink2: afunda todas as posições do navio")
    void sink2() {
        TestShip s = custom(new Position(5, 5), new Position(6, 5));
        s.sink();
        assertAll("sunk ship",
                () -> assertTrue(s.getPositions().get(0).isHit(), "Error: expected (5,5) to be hit"),
                () -> assertTrue(s.getPositions().get(1).isHit(), "Error: expected (6,5) to be hit"),
                () -> assertFalse(s.stillFloating(), "Error: expected a sunk ship"));
    }

    // ------------------------------------------------------------------
    // toString (CC = 1)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("toString: formato [categoria direção posição]")
    void testToString() {
        String expected = "[Barca " + Compass.NORTH + " " + ship.getPosition() + "]";
        assertEquals(expected, ship.toString(), "Error: expected '" + expected + "' but got '" + ship + "'");
    }
}