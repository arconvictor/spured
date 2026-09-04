package de.victorarcon.spured;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Test-only helper for generating random numeric IDs, so integration tests that
 * create Grosskunde entities don't collide with each other or with existing data.
 */
public final class RandomIdGenerator {

    private RandomIdGenerator() {
    }

    /**
     * Generates a random positive ID to use as a test fixture's identifier.
     *
     * @return a random {@link Long} between 1 and {@link Long#MAX_VALUE}
     */
    public static Long generateUserId() {
        return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }
}
