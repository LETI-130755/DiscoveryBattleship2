package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Carrack.
 * Author: Tomás Santos (98458)
 * Date: 2026-11-22
 * Cyclomatic Complexity (CC) of the methods declared in Carrack:
 * - constructor Carrack(Compass, IPosition): 9  (switch with 4 cases + 4 for loops)
 *
 * The methods getSize, stillFloating, getPositions and get*MostPos are inherited
 * from Ship: they are only checked here through a Carrack, and are tested in
 * depth in ShipTest.
 */
class CarrackTest {

    private Carrack carrack;

    @BeforeEach
    void setUp() {
        carrack = new Carrack(Compass.NORTH, new Position(5, 5));
    }

    @AfterEach
    void tearDown() {
        carrack = null;
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
    @DisplayName("constructor1: direção NORTH gera 3 posições na vertical a partir da âncora")
    void constructor1() {
        assertPositionsEqual(carrack.getPositions(),
                new Position(5, 5), new Position(6, 5), new Position(7, 5));
    }

    @Test
    @DisplayName("constructor2: direção SOUTH gera 3 posições na vertical a partir da âncora")
    void constructor2() {
        Carrack south = new Carrack(Compass.SOUTH, new Position(5, 5));
        assertPositionsEqual(south.getPositions(),
                new Position(5, 5), new Position(6, 5), new Position(7, 5));
    }

    @Test
    @DisplayName("constructor3: direção EAST gera 3 posições na horizontal a partir da âncora")
    void constructor3() {
        Carrack east = new Carrack(Compass.EAST, new Position(5, 5));
        assertPositionsEqual(east.getPositions(),
                new Position(5, 5), new Position(5, 6), new Position(5, 7));
    }

    @Test
    @DisplayName("constructor4: direção WEST gera 3 posições na horizontal a partir da âncora")
    void constructor4() {
        Carrack west = new Carrack(Compass.WEST, new Position(5, 5));
        assertPositionsEqual(west.getPositions(),
                new Position(5, 5), new Position(5, 6), new Position(5, 7));
    }

    @Test
    @DisplayName("constructor5: a categoria da Carrack é 'Nau'")
    void constructor5() {
        assertEquals("Nau", carrack.getCategory(),
                "Error: expected category 'Nau' but got '" + carrack.getCategory() + "'");
    }

    @Test
    @DisplayName("constructor6: o tamanho é 3 e há exatamente 3 posições")
    void constructor6() {
        assertAll("size",
                () -> assertEquals(3, carrack.getSize(),
                        "Error: expected size 3 but got " + carrack.getSize()),
                () -> assertEquals(3, carrack.getPositions().size(),
                        "Error: expected 3 positions but got " + carrack.getPositions().size()));
    }

    @Test
    @DisplayName("constructor7: a direção indicada fica guardada no navio")
    void constructor7() {
        assertAll("bearing",
                () -> assertEquals(Compass.NORTH, carrack.getBearing(),
                        "Error: expected NORTH but got " + carrack.getBearing()),
                () -> assertEquals(Compass.EAST, new Carrack(Compass.EAST, new Position(1, 1)).getBearing(),
                        "Error: expected EAST for the second carrack"));
    }

    @Test
    @DisplayName("constructor8: direção nula provoca exceção")
    void constructor8() {
        assertThrows(NullPointerException.class, () -> new Carrack(null, new Position(5, 5)),
                "Error: expected NullPointerException for a null bearing");
    }

    @Test
    @DisplayName("constructor9: posição nula provoca exceção")
    void constructor9() {
        assertThrows(NullPointerException.class, () -> new Carrack(Compass.NORTH, null),
                "Error: expected NullPointerException for a null position");
    }

    // ------------------------------------------------------------------
    // Behaviour inherited from Ship, checked through a Carrack
    // ------------------------------------------------------------------

    @Test
    @DisplayName("stillFloating1: navio intacto está a flutuar")
    void stillFloating1() {
        assertTrue(carrack.stillFloating(), "Error: expected a floating carrack but got a sunk one");
    }

    @Test
    @DisplayName("stillFloating2: navio com apenas uma posição atingida continua a flutuar")
    void stillFloating2() {
        carrack.getPositions().get(0).shoot();
        assertTrue(carrack.stillFloating(),
                "Error: expected a floating carrack with one hit position but got a sunk one");
    }

    @Test
    @DisplayName("stillFloating3: navio com todas as posições atingidas está afundado")
    void stillFloating3() {
        carrack.getPositions().forEach(IPosition::shoot);
        assertFalse(carrack.stillFloating(),
                "Error: expected a sunk carrack with all positions hit but it is still floating");
    }

    @Test
    @DisplayName("get*MostPos1: extremos do navio na vertical (NORTH)")
    void getMostPos1() {
        assertAll("vertical extremes",
                () -> assertEquals(5, carrack.getTopMostPos(),
                        "Error: expected top 5 but got " + carrack.getTopMostPos()),
                () -> assertEquals(7, carrack.getBottomMostPos(),
                        "Error: expected bottom 7 but got " + carrack.getBottomMostPos()),
                () -> assertEquals(5, carrack.getLeftMostPos(),
                        "Error: expected left 5 but got " + carrack.getLeftMostPos()),
                () -> assertEquals(5, carrack.getRightMostPos(),
                        "Error: expected right 5 but got " + carrack.getRightMostPos()));
    }

    @Test
    @DisplayName("get*MostPos2: extremos do navio na horizontal (EAST)")
    void getMostPos2() {
        Carrack east = new Carrack(Compass.EAST, new Position(5, 5));
        assertAll("horizontal extremes",
                () -> assertEquals(5, east.getTopMostPos(),
                        "Error: expected top 5 but got " + east.getTopMostPos()),
                () -> assertEquals(5, east.getBottomMostPos(),
                        "Error: expected bottom 5 but got " + east.getBottomMostPos()),
                () -> assertEquals(5, east.getLeftMostPos(),
                        "Error: expected left 5 but got " + east.getLeftMostPos()),
                () -> assertEquals(7, east.getRightMostPos(),
                        "Error: expected right 7 but got " + east.getRightMostPos()));
    }
}