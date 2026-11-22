package restserver;

import battleship.Game;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    @Test
    @DisplayName("Construtor e getters devem inicializar os campos e estado inicial corretamente")
    void testConstructorAndInitialState() {
        GameSession session = new GameSession("game-123", "Tiago", "http://localhost:9090");

        assertEquals("game-123", session.getGameId());
        assertEquals("Tiago", session.getPlayerName());
        assertEquals("http://localhost:9090", session.getCallbackUrl());
        assertNotNull(session.getGame());
        assertEquals(Game.NUMBER_SHOTS, session.getShotsPerTurn());
        assertFalse(session.isGameOver());
        assertNull(session.getWinner());
    }

    @Test
    @DisplayName("markAiWins deve marcar o jogo como terminado e definir o vencedor como AI_WINS")
    void testMarkAiWins() {
        GameSession session = new GameSession("game-1", "Tiago", "http://localhost:9090");

        session.markAiWins();

        assertTrue(session.isGameOver());
        assertEquals("AI_WINS", session.getWinner());
    }

    @Test
    @DisplayName("markStudentWins deve marcar o jogo como terminado e definir o vencedor como STUDENT_WINS")
    void testMarkStudentWins() {
        GameSession session = new GameSession("game-2", "Tiago", "http://localhost:9090");

        session.markStudentWins();

        assertTrue(session.isGameOver());
        assertEquals("STUDENT_WINS", session.getWinner());
    }
}