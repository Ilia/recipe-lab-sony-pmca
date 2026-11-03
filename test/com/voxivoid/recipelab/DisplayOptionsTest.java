package com.voxivoid.recipelab;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Hold MENU → Display overlays: its rows, defaults, ON / OFF rules, Restore defaults, the menu's routing and the grid's rectangle. */
class DisplayOptionsTest {
    @AfterEach void english() { Lang.use(Lang.EN); }

    private static boolean[] on(boolean... v) { return v; }

    @Test void aFreshInstallLooksAsBefore() {
        boolean[] d = DisplayOptions.defaults();
        assertArrayEquals(on(false, false, true, true, true), d, "histogram and grid off; counter, label and hints on");
        assertEquals(DisplayOptions.KEYS.length, d.length, "one default per option");
        assertEquals(DisplayOptions.DEFAULTS, DisplayOptions.KEYS.length, "every row before Restore defaults is an option");
        d[DisplayOptions.HISTOGRAM] = true;
        assertFalse(DisplayOptions.defaults()[DisplayOptions.HISTOGRAM], "a caller cannot change the defaults");
    }

    @Test void eachOptionHasItsOwnPreferenceKey() {
        assertEquals(Arrays.asList("display.histogram", "display.grid", "display.counter", "display.miniLabel", "display.hints"), Arrays.asList(DisplayOptions.KEYS));
        assertEquals(DisplayOptions.KEYS.length, new HashSet<String>(Arrays.asList(DisplayOptions.KEYS)).size());
    }

    @Test void leftIsOffRightIsOnAndRepeatingChangesNothing() {
        boolean[] o = DisplayOptions.defaults();
        assertTrue(DisplayOptions.set(o, DisplayOptions.GRID, true));
        assertTrue(DisplayOptions.set(o, DisplayOptions.GRID, true));
        assertArrayEquals(on(false, true, true, true, true), o, "only the grid changed, and it stays on");
        assertTrue(DisplayOptions.set(o, DisplayOptions.HINTS, false));
        assertTrue(DisplayOptions.set(o, DisplayOptions.HINTS, false));
        assertArrayEquals(on(false, true, true, true, false), o);
    }

    @Test void centreTogglesOneOptionAndLeavesTheOthers() {
        for (int row = 0; row < DisplayOptions.KEYS.length; row++) {
            boolean[] o = DisplayOptions.defaults(), want = DisplayOptions.defaults();
            want[row] = !want[row];
            assertTrue(DisplayOptions.toggle(o, row));
            assertArrayEquals(want, o, "row " + row);
            assertTrue(DisplayOptions.toggle(o, row));
            assertArrayEquals(DisplayOptions.defaults(), o, "and back, row " + row);
        }
    }

    @Test void restoreDefaultsIsNotAnOption() {
        boolean[] o = on(true, true, false, false, false);
        assertFalse(DisplayOptions.hasValue(DisplayOptions.DEFAULTS));
        assertFalse(DisplayOptions.set(o, DisplayOptions.DEFAULTS, true), "left / right do nothing on it");
        assertFalse(DisplayOptions.toggle(o, DisplayOptions.DEFAULTS));
        assertFalse(DisplayOptions.set(o, -1, true));
        assertArrayEquals(on(true, true, false, false, false), o, "nothing changed");
        assertNull(DisplayOptions.value(DisplayOptions.DEFAULTS, o), "an action, not a value");
        assertTrue(DisplayOptions.hasValue(0));
        assertTrue(DisplayOptions.hasValue(DisplayOptions.KEYS.length - 1));
    }

    @Test void restoreDefaultsPutsBackOnlyTheseSettings() {
        boolean[] o = on(true, true, false, false, false);
        DisplayOptions.restore(o);
        assertArrayEquals(DisplayOptions.defaults(), o);
    }

    @Test void everyRowHasALabelADetailAndOnOrOff() {
        for (int lang = 0; lang < Lang.COUNT; lang++) {
            Lang.use(lang);
            Set<String> labels = new HashSet<String>();
            for (int row = 0; row < DisplayOptions.ROWS; row++) {
                assertFalse(DisplayOptions.label(row).isEmpty(), Lang.CODES[lang] + " row " + row);
                assertFalse(DisplayOptions.detail(row).isEmpty(), Lang.CODES[lang] + " row " + row);
                labels.add(DisplayOptions.label(row));
            }
            assertEquals(DisplayOptions.ROWS, labels.size(), Lang.CODES[lang] + ": every row is named apart");
            assertFalse(DisplayOptions.title().isEmpty());
            boolean[] o = DisplayOptions.defaults();
            assertEquals(Lang.t("value_off"), DisplayOptions.value(DisplayOptions.HISTOGRAM, o));
            assertEquals(Lang.t("value_on"), DisplayOptions.value(DisplayOptions.COUNTER, o));
            assertNotEquals(Lang.t("value_on"), Lang.t("value_off"), Lang.CODES[lang]);
        }
    }

    @Test void theRowsSayWhatTheyAre() {
        Lang.use(Lang.EN);
        assertEquals("DISPLAY OVERLAYS", DisplayOptions.title());
        assertEquals("Live histogram", DisplayOptions.label(DisplayOptions.HISTOGRAM));
        assertEquals("Composition grid", DisplayOptions.label(DisplayOptions.GRID));
        assertEquals("Recipe counter", DisplayOptions.label(DisplayOptions.COUNTER));
        assertEquals("Recipe label", DisplayOptions.label(DisplayOptions.MINI));
        assertEquals("Button hints", DisplayOptions.label(DisplayOptions.HINTS));
        assertEquals("Restore defaults", DisplayOptions.label(DisplayOptions.DEFAULTS));
        assertEquals("Rule-of-thirds lines over the image", DisplayOptions.detail(DisplayOptions.GRID));
        assertEquals("on", DisplayOptions.value(DisplayOptions.GRID, on(false, true, false, false, false)));
        assertEquals("off", DisplayOptions.value(DisplayOptions.GRID, on(true, false, true, true, true)));
    }

    // ---- the app menu routes to it
    @Test void theAppMenuHasDisplayOverlaysBesidePanelVisibility() {
        assertEquals(DevTools.APP_PANEL + 1, DevTools.APP_DISPLAY);
        assertEquals("Display overlays  >", DevTools.appLabel(DevTools.APP_DISPLAY));
        assertEquals("Histogram, grid and what the panel shows", DevTools.appDetail(DevTools.APP_DISPLAY));
        assertNull(DevTools.appValue(DevTools.APP_DISPLAY, Params.OV_FULL, Lang.AUTO), "it opens a submenu, it has no value");
        assertEquals(DevTools.IC_DISPLAY, DevTools.icons(DevTools.LEVEL_APP)[DevTools.APP_DISPLAY]);
    }

    @Test void theSubmenuIsItsOwnLevelNotTheDeveloperMenu() {
        assertEquals(3, new HashSet<Integer>(Arrays.asList(DevTools.LEVEL_APP, DevTools.LEVEL_DEV, DevTools.LEVEL_DISPLAY)).size());
        assertEquals(DisplayOptions.ROWS, DevTools.rows(DevTools.LEVEL_DISPLAY));
        assertEquals(DevTools.ROWS, DevTools.rows(DevTools.LEVEL_DEV));
        assertEquals(DevTools.APP_ROWS, DevTools.rows(DevTools.LEVEL_APP));
        assertNull(DevTools.icons(DevTools.LEVEL_DISPLAY), "its rows are drawn without icons");
        Set<Integer> visited = new HashSet<Integer>();
        int r = DisplayOptions.HISTOGRAM;
        for (int i = 0; i < DisplayOptions.ROWS; i++) { visited.add(r); r = DevTools.nextRow(DevTools.LEVEL_DISPLAY, r, +1); }
        assertEquals(DisplayOptions.ROWS, visited.size(), "one turn reaches every row, Restore defaults included");
        assertEquals(DisplayOptions.HISTOGRAM, r);
        assertEquals(DisplayOptions.DEFAULTS, DevTools.nextRow(DevTools.LEVEL_DISPLAY, DisplayOptions.HISTOGRAM, -1));
    }

    // ---- where the grid goes
    @Test void theGridFollowsThePreviewImageNotTheBars() {
        assertArrayEquals(new int[] { 0, 0, 640, 480 }, DisplayOptions.contentRect(640, 480, 640, 480), "same shape: the whole view");
        assertArrayEquals(new int[] { 0, 0, 640, 480 }, DisplayOptions.contentRect(640, 480, 1280, 960), "the size does not matter, the shape does");
        assertArrayEquals(new int[] { 0, 27, 640, 453 }, DisplayOptions.contentRect(640, 480, 3, 2), "a 3:2 image on a 4:3 view: bars top and bottom");
        assertArrayEquals(new int[] { 0, 60, 640, 420 }, DisplayOptions.contentRect(640, 480, 16, 9), "16:9 on 4:3");
        assertArrayEquals(new int[] { 80, 0, 560, 480 }, DisplayOptions.contentRect(640, 480, 1, 1), "a square image: bars left and right");
        assertArrayEquals(new int[] { 102, 0, 742, 480 }, DisplayOptions.contentRect(845, 480, 4, 3), "4:3 on a wider view");
    }

    @Test void anUnknownPreviewSizeIsTheWholeView() {
        assertArrayEquals(new int[] { 0, 0, 640, 480 }, DisplayOptions.contentRect(640, 480, 0, 0));
        assertArrayEquals(new int[] { 0, 0, 640, 480 }, DisplayOptions.contentRect(640, 480, 640, 0));
        assertArrayEquals(new int[] { 0, 0, 640, 480 }, DisplayOptions.contentRect(640, 480, 0, 480));
        assertArrayEquals(new int[] { 0, 0, 640, 480 }, DisplayOptions.contentRect(640, 480, -3, 2));
        assertArrayEquals(new int[] { 0, 0, 640, 480 }, DisplayOptions.contentRect(640, 480, 3, -2));
    }

    @Test void largeSizesDoNotOverflow() {
        assertArrayEquals(new int[] { 0, 0, 100000, 75000 }, DisplayOptions.contentRect(100000, 75000, 40000, 30000));
    }
}
