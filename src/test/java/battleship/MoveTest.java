package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    private IShip stubShip(String category) {
        return new IShip() {
            @Override
            public String getCategory() {
                return category;
            }

            @Override
            public Integer getSize() {
                return 2;
            }

            @Override
            public List<IPosition> getPositions() {
                return List.of();
            }

            @Override
            public List<IPosition> getAdjacentPositions() {
                return List.of();
            }

            @Override
            public IPosition getPosition() {
                return null;
            }

            @Override
            public Compass getBearing() {
                return Compass.NORTH;
            }

            @Override
            public boolean stillFloating() {
                return true;
            }

            @Override
            public int getTopMostPos() {
                return 0;
            }

            @Override
            public int getBottomMostPos() {
                return 0;
            }

            @Override
            public int getLeftMostPos() {
                return 0;
            }

            @Override
            public int getRightMostPos() {
                return 0;
            }

            @Override
            public boolean occupies(IPosition pos) {
                return false;
            }

            @Override
            public boolean tooCloseTo(IShip other) {
                return false;
            }

            @Override
            public boolean tooCloseTo(IPosition pos) {
                return false;
            }

            @Override
            public boolean isInsideBoard() {
                return true;
            }

            @Override
            public void shoot(IPosition pos) {
                // no-op para teste
            }

            @Override
            public void sink() {
                // no-op para teste
            }
        };
    }

    @Test
    @DisplayName("Construtor, getters e toString devem devolver os valores esperados")
    void testGettersAndToString() {
        IPosition p1 = new Position(1, 1);
        List<IPosition> shots = List.of(p1);
        IGame.ShotResult r1 = new IGame.ShotResult(true, false, null, false);
        List<IGame.ShotResult> results = List.of(r1);

        Move move = new Move(2, shots, results);

        assertEquals(2, move.getNumber());
        assertEquals(shots, move.getShots());
        assertEquals(results, move.getShotResults());
        assertTrue(move.toString().contains("number=2"));
        assertTrue(move.toString().contains("shots=1"));
        assertTrue(move.toString().contains("results=1"));
    }

    @Test
    @DisplayName("processEnemyFire com verbose=false deve gerar JSON com tiros validos, falhados e fora do tabuleiro")
    void testProcessEnemyFireVerboseFalse() {
        IShip caravel = stubShip("Caravel");
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, caravel, false),
                new IGame.ShotResult(true, true, caravel, false)
        );

        Move move = new Move(1, List.of(), results);
        String json = move.processEnemyFire(false);

        assertNotNull(json);
        assertTrue(json.contains("\"validShots\""));
        assertTrue(json.contains("\"repeatedShots\""));
        assertTrue(json.contains("\"missedShots\""));
        assertTrue(json.contains("\"outsideShots\""));
        assertTrue(json.contains("\"hitsOnBoats\""));
    }

    @Test
    @DisplayName("processEnemyFire com verbose=true deve formatar barcos afundados, acertos, repetidos e tiros exteriores")
    void testProcessEnemyFireVerboseTrueWithSunkAndHits() {
        IShip caravel = stubShip("Caravel");
        IShip barge = stubShip("Barge");
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, caravel, true),
                new IGame.ShotResult(true, false, barge, false),
                new IGame.ShotResult(true, true, barge, false)
        );

        Move move = new Move(3, List.of(), results);
        String json = move.processEnemyFire(true);

        assertNotNull(json);
        assertTrue(json.contains("\"sunkBoats\""));
        assertTrue(json.contains("Caravel"));
    }

    @Test
    @DisplayName("processEnemyFire com verbose=true sem tiros validos mas com tiros repetidos deve formatar repetidos")
    void testProcessEnemyFireOnlyRepeatedVerboseTrue() {
        IShip barge = stubShip("Barge");
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, true, barge, false),
                new IGame.ShotResult(true, true, barge, false)
        );

        Move move = new Move(4, List.of(), results);
        String json = move.processEnemyFire(true);

        assertNotNull(json);
        assertTrue(json.contains("\"repeatedShots\" : 2"));
    }

    @Test
    @DisplayName("processEnemyFire com tiros afundados e sem missedShots deve remover o separador final")
    void testProcessEnemyFireSunkWithoutMissedShots() {
        IShip caravel = stubShip("Caravel");
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, caravel, true),
                new IGame.ShotResult(true, false, caravel, true)
        );

        Move move = new Move(5, List.of(), results);
        String json = move.processEnemyFire(true);

        assertNotNull(json);
        assertTrue(json.contains("\"count\" : 2"));
    }
    @Test
    @DisplayName("processEnemyFire com verbose=true e sem tiros validos nem repetidos deve formatar tiros exteriores")
    void testProcessEnemyFireOnlyOutsideShotsVerboseTrue() {
        Move move = new Move(6, List.of(), List.of());
        String json = move.processEnemyFire(true);

        assertNotNull(json);
        assertTrue(json.contains("\"validShots\" : 0"));
    }

    @Test
    @DisplayName("processEnemyFire com verbose=true, barco afundado e tiro na agua deve formatar ambos")
    void testProcessEnemyFireSunkAndMissedVerboseTrue() {
        IShip caravel = stubShip("Caravel");
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, caravel, true),
                new IGame.ShotResult(true, false, null, false)
        );

        Move move = new Move(7, List.of(), results);
        String json = move.processEnemyFire(true);

        assertNotNull(json);
        assertTrue(json.contains("\"missedShots\" : 1"));
    }

}