package net.emeraude.betterexp;

/**
 * The shimmer. Vanilla's experience orb is not an animated texture — it is one static sprite whose
 * vertex colours are driven by two offset sines, green pinned at full while red rises and falls.
 * This reproduces that cycle so the bottle pulses green to yellow the way an orb does.
 *
 * <p>Time comes from {@link System#currentTimeMillis()} rather than the client's tick counter on
 * purpose: this class lives in {@code common/}, which must never touch a client-only Minecraft
 * class, and the wall clock is good enough for a cosmetic cycle.
 */
public final class XpBoostColor {

    /** One full green-to-yellow-to-green cycle. The orb's own is ~0.6s, which reads as a flicker. */
    private static final float CYCLE_MILLIS = 2000.0F;

    private XpBoostColor() {}

    /** {@return an opaque RGB colour for the current instant} */
    public static int pulsing() {
        double phase = System.currentTimeMillis() % (long) CYCLE_MILLIS / CYCLE_MILLIS * (Math.PI * 2.0);

        // Same channel maths as ExperienceOrbRenderer: green stays at full, red sweeps the whole
        // range, blue barely moves and trails a third of a cycle behind.
        int red = (int) ((Math.sin(phase) + 1.0) * 0.5 * 255.0);
        int green = 255;
        int blue = (int) ((Math.sin(phase + Math.PI * 4.0 / 3.0) + 1.0) * 0.1 * 255.0);

        return red << 16 | green << 8 | blue;
    }
}
