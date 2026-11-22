package restserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class GameRegistry.
 * Author: ${user.name}
 * Date: 2026-11-22 16:46
 * Cyclomatic Complexity:
 * constructor: 1
 * createSession(): 1
 * getSession(): 1
 * removeSession(): 1
 */
class GameRegistryTest {

    private GameRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new GameRegistry();
    }

    @AfterEach
    void tearDown() {
        registry = null;
    }

    @Test
    @DisplayName("Default constructor initializes an empty GameRegistry instance")
    void GameRegistry() {
        GameRegistry newRegistry = new GameRegistry();
        assertNotNull(
                newRegistry,
                "Error: expected non-null GameRegistry instance from default constructor, but got null"
        );
        assertNull(
                newRegistry.getSession("non-existent-id"),
                "Error: expected null when querying an unknown session ID on a new registry, but got a non-null session"
        );
    }

    @Test
    @DisplayName("createSession creates, stores, and returns a new GameSession with a unique ID")
    void createSession() {
        String playerName = "Alice";
        String callbackUrl = "https://example.com/callback";

        GameSession created = registry.createSession(playerName, callbackUrl);
        GameSession retrieved = registry.getSession(created.getGameId());

        assertAll(
                "Verify newly created session fields and registry storage",
                () -> assertNotNull(
                        created,
                        "Error: expected non-null GameSession from createSession, but got null"
                ),
                () -> assertNotNull(
                        created.getGameId(),
                        "Error: expected non-null generated gameId, but got null"
                ),
                () -> assertFalse(
                        created.getGameId().isBlank(),
                        "Error: expected non-blank generated gameId, but got '" + created.getGameId() + "'"
                ),
                () -> assertEquals(
                        playerName,
                        created.getPlayerName(),
                        "Error: expected playerName '" + playerName + "', but got '" + created.getPlayerName() + "'"
                ),
                () -> assertEquals(
                        callbackUrl,
                        created.getCallbackUrl(),
                        "Error: expected callbackUrl '" + callbackUrl + "', but got '" + created.getCallbackUrl() + "'"
                ),
                () -> assertSame(
                        created,
                        retrieved,
                        "Error: expected getSession to return the exact stored session instance, but got a different reference"
                )
        );
    }

    @Test
    @DisplayName("getSession returns stored session for existing ID and null for missing ID")
    void getSession() {
        GameSession created = registry.createSession("Bob", "https://example.com/bob");
        String existingId = created.getGameId();

        GameSession found = registry.getSession(existingId);
        GameSession missing = registry.getSession("missing-game-id");

        assertAll(
                "Verify session retrieval for existing and non-existing IDs",
                () -> assertSame(
                        created,
                        found,
                        "Error: expected session matching ID '" + existingId + "', but got '" + found + "'"
                ),
                () -> assertNull(
                        missing,
                        "Error: expected null for non-existent session ID 'missing-game-id', but got '" + missing + "'"
                )
        );
    }

    @Test
    @DisplayName("removeSession deletes an existing session and leaves registry safe for unknown IDs")
    void removeSession() {
        GameSession created = registry.createSession("Charlie", "https://example.com/charlie");
        String gameId = created.getGameId();

        registry.removeSession(gameId);
        GameSession afterRemoval = registry.getSession(gameId);

        assertDoesNotThrow(
                () -> registry.removeSession("unknown-id"),
                "Error: expected removeSession on an unknown ID not to throw an exception"
        );
        assertNull(
                afterRemoval,
                "Error: expected null after removing session '" + gameId + "', but session still exists"
        );
    }
}