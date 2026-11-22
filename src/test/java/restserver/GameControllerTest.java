package restserver;

import battleship.*;
import restserver.Dtos.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class GameController (no mocking library).
 * Author: Tomás Santos (98458)
 * Date: 2026-11-22
 * Cyclomatic Complexity (CC) of each tested method:
 * - constructor GameController(): 1
 * - register(): 5                 (2 if, each with one ||)
 * - receiveShots(): 12            (7 if, 3 ||, 1 for, + the extra || operands)
 * - evaluateShots() [private]: 6  (for + if / else-if chain with 4 conditions)
 * - error() [private]: 1
 *
 * A real GameRegistry (and therefore real sessions and games) is used. The
 * CallbackClient and AiShotStrategy are replaced by small hand-written fakes.
 * Since the AI fleet is random, outcomes are obtained by shooting every cell of
 * the board, which always produces MISS, HIT, SUNK and the final victory.
 * The private methods are exercised through register() and receiveShots().
 */
class GameControllerTest {

    // ------------------------------------------------------------------
    // Hand-written fakes
    // ------------------------------------------------------------------

    /** Records the calls and returns a configurable result instead of doing HTTP. */
    private static class FakeCallbackClient extends CallbackClient {
        boolean result = true;
        int calls = 0;
        List<IPosition> lastShots = null;

        @Override
        public boolean sendShots(GameSession session, List<IPosition> shots) {
            calls++;
            lastShots = shots;
            return result;
        }
    }

    /** Always answers with the same fixed shot and records the game it received. */
    private static class FakeAiShotStrategy extends AiShotStrategy {
        final List<IPosition> shots = List.of(new Position('A', 1));
        int calls = 0;
        IGame lastGame = null;

        @Override
        public List<IPosition> computeShots(IGame game) {
            calls++;
            lastGame = game;
            return shots;
        }
    }

    private GameRegistry registry;
    private FakeCallbackClient callbackClient;
    private FakeAiShotStrategy aiStrategy;
    private GameController controller;

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream capturedOut;
    private ByteArrayOutputStream capturedErr;

    @BeforeEach
    void setUp() {
        registry = new GameRegistry();
        callbackClient = new FakeCallbackClient();
        aiStrategy = new FakeAiShotStrategy();
        controller = new GameController(registry, callbackClient, aiStrategy);

        originalOut = System.out;
        originalErr = System.err;
        capturedOut = new ByteArrayOutputStream();
        capturedErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        controller = null;
        registry = null;
        callbackClient = null;
        aiStrategy = null;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static RegistrationRequest regRequest(String name, String url) {
        RegistrationRequest r = new RegistrationRequest();
        r.playerName = name;
        r.callbackUrl = url;
        return r;
    }

    private static ShotPosition shot(String row, int column) {
        ShotPosition sp = new ShotPosition();
        sp.row = row;
        sp.column = column;
        return sp;
    }

    /** A request with exactly Game.NUMBER_SHOTS valid, different shots. */
    private static ShotRequest validRequest() {
        ShotRequest r = new ShotRequest();
        r.shots = new ArrayList<>();
        for (int i = 0; i < Game.NUMBER_SHOTS; i++)
            r.shots.add(shot(String.valueOf((char) ('A' + i)), i + 1));
        return r;
    }

    private static ShotRequest requestWithFirstShot(String row, int column) {
        ShotRequest r = validRequest();
        r.shots.set(0, shot(row, column));
        return r;
    }

    private static ShotRequest requestAllInRow(String row) {
        ShotRequest r = new ShotRequest();
        r.shots = new ArrayList<>();
        for (int i = 0; i < Game.NUMBER_SHOTS; i++)
            r.shots.add(shot(row, i + 1));
        return r;
    }

    private static int status(ResponseEntity<?> response) {
        return response.getStatusCode().value();
    }

    private String output() {
        return capturedOut.toString(StandardCharsets.UTF_8);
    }

    private String errorOutput() {
        return capturedErr.toString(StandardCharsets.UTF_8);
    }

    /** Registers a player and returns the new game id. */
    private String newGame() {
        ResponseEntity<?> response = controller.register(regRequest("Ana", "http://callback"));
        RegistrationResponse body = assertInstanceOf(RegistrationResponse.class, response.getBody(),
                "Error: expected a RegistrationResponse body but got " + response.getBody());
        return body.gameId;
    }

    private ShotResponse okBody(ResponseEntity<?> response) {
        assertEquals(HttpStatus.OK.value(), status(response),
                "Error: expected status 200 but got " + status(response));
        return assertInstanceOf(ShotResponse.class, response.getBody(),
                "Error: expected a ShotResponse body but got " + response.getBody());
    }

    /** Shoots every cell of the board, Game.NUMBER_SHOTS at a time, until the game is over. */
    private List<ShotResponse> playFullGame(String gameId) {
        List<ShotPosition> cells = new ArrayList<>();
        for (int r = 0; r < Game.BOARD_SIZE; r++)
            for (int c = 0; c < Game.BOARD_SIZE; c++)
                cells.add(shot(String.valueOf((char) ('A' + r)), c + 1));

        List<ShotResponse> responses = new ArrayList<>();
        for (int i = 0; i < cells.size(); i += Game.NUMBER_SHOTS) {
            ShotRequest request = new ShotRequest();
            request.shots = new ArrayList<>();
            for (int k = 0; k < Game.NUMBER_SHOTS; k++)
                request.shots.add(cells.get(Math.min(i + k, cells.size() - 1)));
            ShotResponse body = okBody(controller.receiveShots(gameId, request));
            responses.add(body);
            if ("GAME_OVER".equals(body.gameStatus))
                break;
        }
        return responses;
    }

    private static List<ShotResult> allResults(List<ShotResponse> responses) {
        List<ShotResult> all = new ArrayList<>();
        for (ShotResponse r : responses)
            all.addAll(r.results);
        return all;
    }

    // ------------------------------------------------------------------
    // Constructor (CC = 1)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("constructor: cria o controlador com os três colaboradores")
    void constructor() {
        assertNotNull(controller, "Error: expected a non-null GameController but got null");
    }

    // ------------------------------------------------------------------
    // register (CC = 5)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("register1: pedido válido regista o jogador e devolve 200 com o gameId")
    void register1() {
        ResponseEntity<?> response = controller.register(regRequest("Ana", "http://callback"));

        assertEquals(HttpStatus.OK.value(), status(response),
                "Error: expected status 200 but got " + status(response));
        RegistrationResponse body = assertInstanceOf(RegistrationResponse.class, response.getBody(),
                "Error: expected a RegistrationResponse body but got " + response.getBody());
        GameSession created = registry.getSession(body.gameId);
        assertAll("registration response",
                () -> assertNotNull(body.gameId, "Error: expected a gameId but got null"),
                () -> assertNotNull(created, "Error: expected the session to be stored in the registry"),
                () -> assertEquals("REGISTERED", body.status, "Error: expected status 'REGISTERED' but got " + body.status),
                () -> assertEquals(created.getShotsPerTurn(), body.shotsPerTurn,
                        "Error: expected shotsPerTurn " + created.getShotsPerTurn() + " but got " + body.shotsPerTurn),
                () -> assertTrue(body.message.contains("Ana"),
                        "Error: expected the message to mention the player but got '" + body.message + "'"),
                () -> assertTrue(output().contains("Registered player 'Ana'"),
                        "Error: expected the registration to be logged but got '" + output() + "'"));
    }

    @Test
    @DisplayName("register2: playerName nulo devolve 400")
    void register2() {
        ResponseEntity<?> response = controller.register(regRequest(null, "http://callback"));
        assertAll("null player name",
                () -> assertEquals(HttpStatus.BAD_REQUEST.value(), status(response),
                        "Error: expected status 400 but got " + status(response)),
                () -> assertInstanceOf(ErrorResponse.class, response.getBody(),
                        "Error: expected an ErrorResponse body but got " + response.getBody()));
    }

    @Test
    @DisplayName("register3: playerName em branco devolve 400")
    void register3() {
        ResponseEntity<?> response = controller.register(regRequest("   ", "http://callback"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), status(response),
                "Error: expected status 400 but got " + status(response));
    }

    @Test
    @DisplayName("register4: callbackUrl nulo devolve 400")
    void register4() {
        ResponseEntity<?> response = controller.register(regRequest("Ana", null));
        assertEquals(HttpStatus.BAD_REQUEST.value(), status(response),
                "Error: expected status 400 but got " + status(response));
    }

    @Test
    @DisplayName("register5: callbackUrl em branco devolve 400")
    void register5() {
        ResponseEntity<?> response = controller.register(regRequest("Ana", " "));
        assertEquals(HttpStatus.BAD_REQUEST.value(), status(response),
                "Error: expected status 400 but got " + status(response));
    }

    // ------------------------------------------------------------------
    // receiveShots (CC = 12)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("receiveShots1: jogo inexistente devolve 404")
    void receiveShots1() {
        ResponseEntity<?> response = controller.receiveShots("desconhecido", validRequest());
        assertAll("unknown game",
                () -> assertEquals(HttpStatus.NOT_FOUND.value(), status(response),
                        "Error: expected status 404 but got " + status(response)),
                () -> assertInstanceOf(ErrorResponse.class, response.getBody(),
                        "Error: expected an ErrorResponse body but got " + response.getBody()));
    }

    @Test
    @DisplayName("receiveShots2: jogo já terminado devolve 410")
    void receiveShots2() {
        String gameId = newGame();
        playFullGame(gameId);
        ResponseEntity<?> response = controller.receiveShots(gameId, validRequest());
        assertEquals(HttpStatus.GONE.value(), status(response),
                "Error: expected status 410 for a finished game but got " + status(response));
    }

    @Test
    @DisplayName("receiveShots3: lista de tiros nula devolve 400")
    void receiveShots3() {
        ShotRequest request = new ShotRequest();
        request.shots = null;
        ResponseEntity<?> response = controller.receiveShots(newGame(), request);
        assertEquals(HttpStatus.BAD_REQUEST.value(), status(response),
                "Error: expected status 400 but got " + status(response));
    }

    @Test
    @DisplayName("receiveShots4: número de tiros diferente do exigido devolve 400")
    void receiveShots4() {
        String gameId = newGame();
        ShotRequest tooMany = validRequest();
        tooMany.shots.add(shot("J", 10));
        ShotRequest none = new ShotRequest();
        none.shots = new ArrayList<>();
        assertAll("wrong number of shots",
                () -> assertEquals(HttpStatus.BAD_REQUEST.value(), status(controller.receiveShots(gameId, tooMany)),
                        "Error: expected status 400 for too many shots"),
                () -> assertEquals(HttpStatus.BAD_REQUEST.value(), status(controller.receiveShots(gameId, none)),
                        "Error: expected status 400 for no shots"),
                () -> assertEquals(0, aiStrategy.calls, "Error: the AI must not fire after an invalid request"));
    }

    @Test
    @DisplayName("receiveShots5: linha nula numa posição devolve 400")
    void receiveShots5() {
        ResponseEntity<?> response = controller.receiveShots(newGame(), requestWithFirstShot(null, 3));
        assertEquals(HttpStatus.BAD_REQUEST.value(), status(response),
                "Error: expected status 400 but got " + status(response));
    }

    @Test
    @DisplayName("receiveShots6: linha em branco numa posição devolve 400")
    void receiveShots6() {
        ResponseEntity<?> response = controller.receiveShots(newGame(), requestWithFirstShot("  ", 3));
        assertEquals(HttpStatus.BAD_REQUEST.value(), status(response),
                "Error: expected status 400 but got " + status(response));
    }

    @Test
    @DisplayName("receiveShots7: coluna menor que 1 devolve 400")
    void receiveShots7() {
        ResponseEntity<?> response = controller.receiveShots(newGame(), requestWithFirstShot("A", 0));
        assertEquals(HttpStatus.BAD_REQUEST.value(), status(response),
                "Error: expected status 400 but got " + status(response));
    }

    @Test
    @DisplayName("receiveShots8: coluna maior que 10 devolve 400")
    void receiveShots8() {
        ResponseEntity<?> response = controller.receiveShots(newGame(), requestWithFirstShot("A", 11));
        assertEquals(HttpStatus.BAD_REQUEST.value(), status(response),
                "Error: expected status 400 but got " + status(response));
    }

    @Test
    @DisplayName("receiveShots9: disparando todo o tabuleiro o estudante ganha e a IA não dispara no último turno")
    void receiveShots9() {
        String gameId = newGame();
        List<ShotResponse> responses = playFullGame(gameId);
        ShotResponse last = responses.get(responses.size() - 1);
        GameSession session = registry.getSession(gameId);
        assertAll("student wins",
                () -> assertEquals("GAME_OVER", last.gameStatus, "Error: expected 'GAME_OVER' but got " + last.gameStatus),
                () -> assertEquals("STUDENT_WINS", last.winner, "Error: expected 'STUDENT_WINS' but got " + last.winner),
                () -> assertEquals(0, last.shipsRemaining, "Error: expected 0 ships but got " + last.shipsRemaining),
                () -> assertTrue(session.isGameOver(), "Error: expected the session to be marked as over"),
                () -> assertEquals(responses.size() - 1, callbackClient.calls,
                        "Error: expected the callback only on the turns before the last one but got " + callbackClient.calls),
                () -> assertEquals(responses.size() - 1, aiStrategy.calls,
                        "Error: expected the AI to fire only on the turns before the last one but got " + aiStrategy.calls),
                () -> assertTrue(output().contains("GAME OVER"), "Error: expected the end of the game to be logged"));
    }

    @Test
    @DisplayName("receiveShots10: jogo continua, a IA calcula tiros e envia-os pelo callback")
    void receiveShots10() {
        String gameId = newGame();
        ShotResponse body = okBody(controller.receiveShots(gameId, validRequest()));
        assertAll("ongoing game",
                () -> assertEquals("ONGOING", body.gameStatus, "Error: expected 'ONGOING' but got " + body.gameStatus),
                () -> assertNull(body.winner, "Error: expected no winner but got " + body.winner),
                () -> assertTrue(body.shipsRemaining > 0, "Error: expected ships remaining but got " + body.shipsRemaining),
                () -> assertEquals(Game.NUMBER_SHOTS, body.results.size(),
                        "Error: expected " + Game.NUMBER_SHOTS + " results but got " + body.results.size()),
                () -> assertEquals(1, aiStrategy.calls, "Error: expected the AI to fire once but got " + aiStrategy.calls),
                () -> assertSame(registry.getSession(gameId).getGame(), aiStrategy.lastGame,
                        "Error: expected the AI to receive the session's game"),
                () -> assertEquals(1, callbackClient.calls,
                        "Error: expected one callback call but got " + callbackClient.calls),
                () -> assertSame(aiStrategy.shots, callbackClient.lastShots,
                        "Error: expected the AI shots to be sent through the callback"));
    }

    @Test
    @DisplayName("receiveShots11: se o callback falhar, regista aviso mas devolve na mesma 200")
    void receiveShots11() {
        callbackClient.result = false;
        ShotResponse body = okBody(controller.receiveShots(newGame(), validRequest()));
        assertAll("callback failure",
                () -> assertEquals("ONGOING", body.gameStatus, "Error: expected 'ONGOING' but got " + body.gameStatus),
                () -> assertEquals(1, callbackClient.calls, "Error: expected one callback attempt"),
                () -> assertTrue(errorOutput().contains("WARNING"),
                        "Error: expected a warning on System.err but got '" + errorOutput() + "'"));
    }

    @Test
    @DisplayName("receiveShots12: linha em minúsculas é convertida para maiúsculas na resposta")
    void receiveShots12() {
        ShotResponse body = okBody(controller.receiveShots(newGame(), requestWithFirstShot("b", 2)));
        ShotResult first = body.results.get(0);
        assertAll("first result",
                () -> assertEquals("B", first.row, "Error: expected row 'B' but got " + first.row),
                () -> assertEquals(2, first.column, "Error: expected column 2 but got " + first.column));
    }

    // ------------------------------------------------------------------
    // evaluateShots - private (CC = 6), exercised through receiveShots
    // ------------------------------------------------------------------

    @Test
    @DisplayName("evaluateShots1: tiro fora do tabuleiro resulta em 'INVALID'")
    void evaluateShots1() {
        // Row 'K' is accepted by the controller but is outside the 10x10 board
        ShotResponse body = okBody(controller.receiveShots(newGame(), requestAllInRow("K")));
        for (ShotResult r : body.results)
            assertEquals("INVALID", r.outcome, "Error: expected 'INVALID' but got " + r.outcome);
    }

    @Test
    @DisplayName("evaluateShots2: tiro repetido na mesma rajada resulta em 'REPEATED'")
    void evaluateShots2() {
        // validRequest() starts with A1; make the last shot of the same turn repeat it
        ShotRequest request = validRequest();
        request.shots.set(Game.NUMBER_SHOTS - 1, shot("A", 1));

        ShotResponse body = okBody(controller.receiveShots(newGame(), request));

        for (int i = 0; i < Game.NUMBER_SHOTS - 1; i++)
            assertNotEquals("REPEATED", body.results.get(i).outcome,
                    "Error: shot " + i + " is not a repetition but got 'REPEATED'");
        assertEquals("REPEATED", body.results.get(Game.NUMBER_SHOTS - 1).outcome,
                "Error: expected 'REPEATED' for the repeated shot but got "
                        + body.results.get(Game.NUMBER_SHOTS - 1).outcome);
    }

    @Test
    @DisplayName("evaluateShots3: tiro na água resulta em 'MISS' e sem tipo de navio")
    void evaluateShots3() {
        List<ShotResult> misses = allResults(playFullGame(newGame())).stream()
                .filter(r -> "MISS".equals(r.outcome)).toList();
        assertFalse(misses.isEmpty(), "Error: expected at least one MISS when shooting the whole board");
        for (ShotResult r : misses)
            assertNull(r.shipType, "Error: expected no ship type for a MISS but got " + r.shipType);
    }

    @Test
    @DisplayName("evaluateShots4: navios afundados resultam em 'SUNK', com a composição completa da frota")
    void evaluateShots4() {
        List<ShotResult> sunk = allResults(playFullGame(newGame())).stream()
                .filter(r -> "SUNK".equals(r.outcome)).toList();
        assertAll("sunk ships",
                () -> assertEquals(11, sunk.size(), "Error: expected 11 sunk ships but got " + sunk.size()),
                () -> assertEquals(4, sunk.stream().filter(r -> "Barca".equals(r.shipType)).count(),
                        "Error: expected 4 sunk 'Barca'"),
                () -> assertEquals(3, sunk.stream().filter(r -> "Caravela".equals(r.shipType)).count(),
                        "Error: expected 3 sunk 'Caravela'"),
                () -> assertEquals(2, sunk.stream().filter(r -> "Nau".equals(r.shipType)).count(),
                        "Error: expected 2 sunk 'Nau'"),
                () -> assertEquals(1, sunk.stream().filter(r -> "Fragata".equals(r.shipType)).count(),
                        "Error: expected 1 sunk 'Fragata'"),
                () -> assertEquals(1, sunk.stream().filter(r -> "Galeao".equals(r.shipType)).count(),
                        "Error: expected 1 sunk 'Galeao'"));
    }

    @Test
    @DisplayName("evaluateShots5: navios atingidos mas a flutuar resultam em 'HIT' com o tipo do navio")
    void evaluateShots5() {
        List<ShotResult> hits = allResults(playFullGame(newGame())).stream()
                .filter(r -> "HIT".equals(r.outcome)).toList();
        assertFalse(hits.isEmpty(), "Error: expected at least one HIT when shooting the whole board");
        for (ShotResult r : hits)
            assertNotNull(r.shipType, "Error: expected a ship type for a HIT but got null");
    }

    @Test
    @DisplayName("evaluateShots6: a mesma posição repetida no turno só conta como nova da primeira vez")
    void evaluateShots6() {
        ShotRequest request = new ShotRequest();
        request.shots = new ArrayList<>();
        for (int i = 0; i < Game.NUMBER_SHOTS; i++)
            request.shots.add(shot("A", 1));

        ShotResponse body = okBody(controller.receiveShots(newGame(), request));

        assertNotEquals("REPEATED", body.results.get(0).outcome,
                "Error: the first shot at A1 must not be a REPEATED one");
        for (int i = 1; i < body.results.size(); i++)
            assertEquals("REPEATED", body.results.get(i).outcome,
                    "Error: expected 'REPEATED' for shot " + i + " but got " + body.results.get(i).outcome);
    }
}