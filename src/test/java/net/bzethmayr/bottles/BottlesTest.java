package net.bzethmayr.bottles;

import net.zethmayr.fungu.test.TestHelper;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static net.bzethmayr.bottles.Bottles.emitVerse;
import static net.zethmayr.fungu.test.MatcherFactory.has;
import static net.zethmayr.fungu.test.TestHelper.concurrently;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Local threaded cases.
 */
class BottlesTest {

    private Matcher<List<String>> containsStartingLine() {
        return has(List::getFirst, "99 bottles of beer on the wall");
    }

    private Matcher<List<String>> containsStartingLines() {
        return allOf(
                containsStartingLine(),
                has(c -> c.get(1), "99 bottles of beer"),
                has(c -> c.get(2), "take one down"),
                has(c -> c.get(3), "pass it around")
        );
    }


    /**
     * An uninteresting case in terms of synchronization.
     * Emits a verse using independent suppliers
     */
    @Test
    void emitVerse_givenIndependentSuppliers_emitsExpected() {
        final List<String> result = new ArrayList<>();

        emitVerse(result::add, () -> 99, () -> 98);

        assertThat(result, allOf(
                hasSize(5),
                containsStartingLines(),
                has(List::getLast, "98 bottles of beer on the wall")
        ));
    }

    private Matcher<List<String>> containsEndingLine() {
        return has(List::getLast, "0 bottles of beer on the wall");
    }


    /**
     * An uninteresting case in terms of synchronization.
     * Emits all verses using independent suppliers
     */
    @Test
    void emitVerses_givenIndependentSuppliers_emitsAll() {
        final List<String> result = new ArrayList<>();

        IntStream.iterate(99, n -> n > 0, n -> n - 1)
                .forEach(n -> emitVerse(result::add, () -> n, () -> n - 1));

        assertThat(result, allOf(
                hasSize(495),
                containsStartingLines(),
                containsEndingLine()
        ));
    }

    /**
     * Illustrates need for thread-safe collection
     * and verifies lack of coordination between non-coordinating threads.
     * Emits all verses concurrently from many emitters to the same collection
     */
    @Test
    void multipleEmitters_givenIndependentSuppliers_emitsWithOnlyTerminalGuarantees() {
        final List<String> result = new CopyOnWriteArrayList<>();
        // ArrayList has expected undefined behavior here,
        // either silently or loudly failing under concurrent modification
        // with index (vs concurrent modification) exceptions

        final Runnable emitsVerses = () -> IntStream.iterate(99, n -> n > 0, n -> n - 1)
                .forEach(n -> emitVerse(result::add, () -> n, () -> n - 1));
        concurrently(99, IntStream.rangeClosed(0, 99).mapToObj(n -> emitsVerses).toArray(Runnable[]::new));

        assertThat(result, allOf(
                hasSize(49500),
                containsStartingLine(),
                containsEndingLine()
        ));
    }

    /**
     * The non-problematic single-thread case using mutable state.
     * Emits verses using stateful suppliers.
     */
    @Test
    void emitVerses_givenDependentSuppliers_emitsFromSharedValue() {
        final List<String> result = new ArrayList<>();
        final AtomicInteger counter = new AtomicInteger(99);

        IntStream.iterate(99, n -> n > 0, n -> n - 1)
                .forEach(n -> emitVerse(result::add, counter::get, counter::decrementAndGet, counter::get));

        assertThat(result, allOf(
                hasSize(495),
                containsStartingLines(),
                containsEndingLine()
        ));
    }
}
