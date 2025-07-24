package net.bzethmayr.bottles;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;

/**
 * The consistent test subject. This imposes no synchronization,
 * and makes no assumptions as to how state is managed.
 */
public class Bottles {

    /**
     * The well-known verse formats.
     */
    protected static final String[] VERSE_FORMATS = {
            "%s bottles of beer on the wall",
            "%s bottles of beer",
            "take one down",
            "pass it around",
            "%s bottles of beer on the wall"
    };

    /**
     * Emits a line according to the well-known rules.
     *
     * @param emitter   We have some way of emitting the line
     * @param start     How to get the starting number for the first two lines
     * @param takenDown Called when the song says a bottle is taken down
     * @param next      How to get the ending number for the last line
     * @param verseLine Which line we're emitting
     */
    public static void emitLine(
            final Consumer<String> emitter,
            final IntSupplier start,
            final Runnable takenDown,
            final IntSupplier next,
            final int verseLine
    ) {
        final String verseFormat = VERSE_FORMATS[verseLine];
        switch (verseLine) {
            case 0:
            case 1:
                emitter.accept(verseFormat.formatted(start.getAsInt()));
                break;
            case 2:
                takenDown.run();
            case 3:
                emitter.accept(verseFormat);
                break;
            case 4:
                emitter.accept(verseFormat.formatted(next.getAsInt()));
                break;
            default:
                throw becauseIllegal("%s isn't a valid line number", verseLine);
        }
    }

    /**
     * Emits a line according to the well-known rules.
     * Does nothing when the song says a bottle is taken down.
     *
     * @param emitter   We have some way of emitting the line
     * @param start     How to get the starting number for the first two lines
     * @param next      How to get the ending number for the last line
     * @param verseLine Which line we're emitting
     */
    public static void emitLine(
            final Consumer<String> emitter, final IntSupplier start, final IntSupplier next, final int verseLine
    ) {
        emitLine(emitter, start, () -> {}, next, verseLine);
    }

    /**
     * Emits a verse according to the well-known rules.
     * Does nothing when the song says a bottle is taken down.
     *
     * @param emitter   We have some way of emitting the line
     * @param start     How to get the starting number for the first two lines
     * @param next      How to get the ending number for the last line
     */
    public static void emitVerse(final Consumer<String> emitter, final IntSupplier start, final IntSupplier next) {
        IntStream.range(0, VERSE_FORMATS.length)
                .forEach(n -> emitLine(emitter, start, next, n));
    }

    /**
     * Emits a verse according to the well-known rules.
     *
     * @param emitter   We have some way of emitting the line
     * @param start     How to get the starting number for the first two lines
     * @param takenDown Called when the song says a bottle is taken down
     * @param next      How to get the ending number for the last line
     */
    public static void emitVerse(
            final Consumer<String> emitter, final IntSupplier start, final Runnable takenDown, final IntSupplier next
    ) {
        IntStream.range(0, VERSE_FORMATS.length)
                .forEach(n -> emitLine(emitter, start, takenDown, next, n));
    }

}
