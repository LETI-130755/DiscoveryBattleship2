package battleship;

import org.apache.commons.lang3.time.StopWatch;
import java.util.concurrent.TimeUnit;

public class MoveTimer {
    private final StopWatch watch = new StopWatch();
    private long lastMoveMillis = 0;

    public void start() {
        watch.reset();
        watch.start();
    }

    public void stopAndShow(int moveNumber) {
        watch.stop();
        lastMoveMillis = watch.getTime(TimeUnit.MILLISECONDS);
        System.out.printf("⏱  Tempo da jogada %d: %.2f segundos%n",
                moveNumber, lastMoveMillis / 1000.0);
    }

    public long getLastMoveMillis() {
        return lastMoveMillis;
    }
}
