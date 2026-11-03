package com.voxivoid.recipelab;

/**
 * Hold MENU → Display overlays: what may sit over the live image, one ON / OFF row each — the live histogram, the
 * rule-of-thirds grid, and three parts of the panel (the recipe counter, the Label pill, the key legend) — and Restore
 * defaults. Each is kept in the app's preferences under its own key ({@link #KEYS}), never in the camera's store.
 *
 * A fresh install looks as before: histogram and grid off, the rest on. The panel's own Full / No keys / Label / Hidden
 * cycle (trash, Panel visibility) is not one of these and is never touched by them.
 *
 * The autofocus-point overlay is a separate feature: when it lands it adds its row here (an id before
 * {@link #DEFAULTS}, its key, default, label and detail). No android.* import may appear in this class (tools/test.sh).
 */
final class DisplayOptions {
    private DisplayOptions() {}

    /** the submenu's rows, in display order; every row but the last is an option, indexing {@link #KEYS} */
    static final int HISTOGRAM = 0, GRID = 1, COUNTER = 2, MINI = 3, HINTS = 4, DEFAULTS = 5, ROWS = 6;

    /** each option's preference key */
    static final String[] KEYS = { "display.histogram", "display.grid", "display.counter", "display.miniLabel", "display.hints" };
    private static final boolean[] DEFAULT = { false, false, true, true, true };

    private static final String[] LABEL = { "display_histogram", "display_grid", "display_counter", "display_label", "display_hints", "display_defaults" };
    private static final String[] DETAIL = { "display_histogram_detail", "display_grid_detail", "display_counter_detail",
            "display_label_detail", "display_hints_detail", "display_defaults_detail" };

    static String title() { return Lang.t("display_title"); }

    /** what a fresh install shows, one entry per option */
    static boolean[] defaults() { return DEFAULT.clone(); }

    /** whether a row is an option with an ON / OFF value — not Restore defaults */
    static boolean hasValue(int row) { return row >= 0 && row < KEYS.length; }

    /** left (off) / right (on) on a row: sets it, the same value again changing nothing; false on a row with no value */
    static boolean set(boolean[] on, int row, boolean value) {
        if (!hasValue(row)) return false;
        on[row] = value;
        return true;
    }

    /** the centre button on an option: on ↔ off; false on a row with no value */
    static boolean toggle(boolean[] on, int row) {
        if (!hasValue(row)) return false;
        on[row] = !on[row];
        return true;
    }

    /** Restore defaults: every option as on a fresh install, and nothing else */
    static void restore(boolean[] on) { System.arraycopy(DEFAULT, 0, on, 0, DEFAULT.length); }

    static String label(int row) { return Lang.t(LABEL[row]); }
    static String detail(int row) { return Lang.t(DETAIL[row]); }

    /** the value at a row's right edge: on / off, or null for Restore defaults */
    static String value(int row, boolean[] on) { return hasValue(row) ? Lang.t(on[row] ? "value_on" : "value_off") : null; }

    /**
     * Where the preview image sits in a {@code w × h} view: {left, top, right, bottom} of the largest rectangle of the
     * preview's {@code pw × ph} shape, centred, so letterbox or pillarbox bars are left out. The whole view while the
     * preview size is unknown. Whether the camera letterboxes this way or stretches the image over the view is a
     * question for the camera (docs/DEVELOPMENT.md).
     */
    static int[] contentRect(int w, int h, int pw, int ph) {
        if (pw <= 0 || ph <= 0) return new int[] { 0, 0, w, h };
        long wide = (long) w * ph, tall = (long) h * pw;
        if (wide > tall) {                                        // the view is wider than the image: bars left and right
            int cw = (int) (tall / ph), x = (w - cw) / 2;
            return new int[] { x, 0, x + cw, h };
        }
        int ch = (int) (wide / pw), y = (h - ch) / 2;             // taller, or the same shape: bars top and bottom (or none)
        return new int[] { 0, y, w, y + ch };
    }
}
