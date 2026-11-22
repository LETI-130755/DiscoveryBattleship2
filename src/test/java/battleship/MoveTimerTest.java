package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveTimerTest {

    @Test
    @DisplayName("Estado inicial do temporizador deve ter lastMoveMillis igual a 0")
    void testInitialState() {
        MoveTimer timer = new MoveTimer();
        assertEquals(0L, timer.getLastMoveMillis());
    }

    @Test
    @DisplayName("start e stopAndShow devem medir o tempo decorrido da jogada")
    void testStartAndStopAndShow() throws InterruptedException {
        MoveTimer timer = new MoveTimer();
        timer.start();
        Thread.sleep(30);
        timer.stopAndShow(1);

        assertTrue(timer.getLastMoveMillis() >= 20L,
                "O tempo registado deve ser maior ou igual ao tempo de espera");
    }

    @Test
    @DisplayName("start deve reiniciar a contagem numa nova jogada")
    void testRestartResetsTimer() throws InterruptedException {
        MoveTimer timer = new MoveTimer();

        timer.start();
        Thread.sleep(30);
        timer.stopAndShow(1);

        timer.start();
        timer.stopAndShow(2);

        assertTrue(timer.getLastMoveMillis() >= 0L);
    }

    @Test
    @DisplayName("stopAndShow sem start prévio deve lançar IllegalStateException")
    void testStopWithoutStartThrowsException() {
        MoveTimer timer = new MoveTimer();
        assertThrows(IllegalStateException.class, () -> timer.stopAndShow(1));
    }
}