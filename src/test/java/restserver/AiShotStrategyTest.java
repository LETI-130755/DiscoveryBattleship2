package restserver;

import battleship.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AiShotStrategyTest {

    private IGame stubGame(List<IMove> alienMoves) {
        return new IGame() {
            @Override
            public String randomEnemyFire() {
                return "";
            }

            @Override
            public String readEnemyFire(Scanner in) {
                return "";
            }

            @Override
            public void fireShots(List<IPosition> shots) {
            }

            @Override
            public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) {
                return new ShotResult(false, false, null, false);
            }

            @Override
            public IFleet getMyFleet() {
                return null;
            }

            @Override
            public List<IMove> getAlienMoves() {
                return alienMoves;
            }

            @Override
            public IFleet getAlienFleet() {
                return null;
            }

            @Override
            public List<IMove> getMyMoves() {
                return List.of();
            }

            @Override
            public int getRepeatedShots() {
                return 0;
            }

            @Override
            public int getInvalidShots() {
                return 0;
            }

            @Override
            public int getHits() {
                return 0;
            }

            @Override
            public int getSunkShips() {
                return 0;
            }

            @Override
            public int getRemainingShips() {
                return 0;
            }

            @Override
            public void printMyBoard(boolean show_shots, boolean show_legend) {
            }

            @Override
            public void printAlienBoard(boolean show_shots, boolean show_legend) {
            }

            @Override
            public void over() {
            }
        };
    }

    private IMove stubMove(int number, List<IPosition> shots, List<IGame.ShotResult> results) {
        return new IMove() {
            @Override
            public int getNumber() {
                return number;
            }

            @Override
            public List<IPosition> getShots() {
                return shots;
            }

            @Override
            public List<IGame.ShotResult> getShotResults() {
                return results;
            }

            @Override
            public String processEnemyFire(boolean verbose) {
                return "";
            }
        };
    }

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
            }

            @Override
            public void sink() {
            }
        };
    }

    @Test
    @DisplayName("Sem jogadas anteriores deve escolher Game.NUMBER_SHOTS posicoes no padrao xadrez (HUNT)")
    void testHuntPhaseWithoutPriorMoves() {
        AiShotStrategy strategy = new AiShotStrategy();
        IGame game = stubGame(List.of());

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertEquals(new HashSet<>(shots).size(), shots.size());
        for (IPosition p : shots) {
            assertEquals(0, (p.getRow() + p.getColumn()) % 2);
        }
    }

    @Test
    @DisplayName("Com acerto nao afundado deve priorizar vizinhos ainda nao disparados (TARGET)")
    void testTargetPhasePrioritizesNeighbours() {
        AiShotStrategy strategy = new AiShotStrategy();
        IPosition hitPos = new Position(4, 4);
        IShip ship = stubShip("Caravel");

        IMove previousMove = stubMove(
                1,
                List.of(hitPos),
                List.of(new IGame.ShotResult(true, false, ship, false))
        );
        IGame game = stubGame(List.of(previousMove));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertFalse(shots.contains(hitPos));
        assertTrue(shots.containsAll(hitPos.adjacentPositions().subList(
                0, Math.min(Game.NUMBER_SHOTS, hitPos.adjacentPositions().size())
        )));
    }

    @Test
    @DisplayName("Deve ignorar tiros invalidos, repetidos, na agua, afundados ou resultados incompletos")
    void testIgnoresNonTargetResults() {
        AiShotStrategy strategy = new AiShotStrategy();
        IShip ship = stubShip("Barge");

        IPosition pInvalid = new Position(0, 0);
        IPosition pRepeated = new Position(0, 2);
        IPosition pMiss = new Position(0, 4);
        IPosition pSunk = new Position(0, 6);
        IPosition pNoResult = new Position(1, 1);

        IMove move = stubMove(
                1,
                List.of(pInvalid, pRepeated, pMiss, pSunk, pNoResult),
                List.of(
                        new IGame.ShotResult(false, false, ship, false),
                        new IGame.ShotResult(true, true, ship, false),
                        new IGame.ShotResult(true, false, null, false),
                        new IGame.ShotResult(true, false, ship, true)
                )
        );
        IGame game = stubGame(List.of(move));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertFalse(shots.contains(pInvalid));
        assertFalse(shots.contains(pRepeated));
        assertFalse(shots.contains(pMiss));
        assertFalse(shots.contains(pSunk));
        assertFalse(shots.contains(pNoResult));
    }

    @Test
    @DisplayName("Quando o padrao xadrez esta esgotado deve usar o fallback para completar os tiros")
    void testFallbackWhenCheckerboardExhausted() {
        AiShotStrategy strategy = new AiShotStrategy();
        List<IPosition> allCheckerboard = new ArrayList<>();
        List<IGame.ShotResult> results = new ArrayList<>();

        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                if ((r + c) % 2 == 0) {
                    allCheckerboard.add(new Position(r, c));
                    results.add(new IGame.ShotResult(true, false, null, false));
                }
            }
        }

        IMove move = stubMove(1, allCheckerboard, results);
        IGame game = stubGame(List.of(move));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        for (IPosition p : shots) {
            assertEquals(1, Math.abs((p.getRow() + p.getColumn()) % 2));
        }
    }
    @Test
    @DisplayName("Com multiplos acertos adjacentes e vizinhos ja disparados deve evitar duplicados e respeitar limite de tiros")
    void testTargetPhaseWithSharedAndAlreadyShotNeighbours() {
        AiShotStrategy strategy = new AiShotStrategy();
        IPosition hit1 = new Position(4, 4);
        IPosition hit2 = new Position(4, 5);
        IPosition alreadyShotNeighbour = new Position(3, 4);
        IShip ship = stubShip("Frigate");

        IMove move = stubMove(
                1,
                List.of(hit1, hit2, alreadyShotNeighbour),
                List.of(
                        new IGame.ShotResult(true, false, ship, false),
                        new IGame.ShotResult(true, false, ship, false),
                        new IGame.ShotResult(true, false, null, false)
                )
        );
        IGame game = stubGame(List.of(move));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertEquals(new HashSet<>(shots).size(), shots.size());
        assertFalse(shots.contains(alreadyShotNeighbour));
    }
}