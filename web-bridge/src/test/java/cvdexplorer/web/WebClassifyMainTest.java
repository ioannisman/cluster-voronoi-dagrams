package cvdexplorer.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebClassifyMainTest {
    @Test
    void exportsOnlyRequestedExactSizedRasterArrays() {
        WebClassifyMain.computeFrame(
                48,
                32,
                false,
                WebClassifyMain.OUTPUT_ARGB | WebClassifyMain.OUTPUT_OWNERS
        );

        assertEquals(48, WebClassifyMain.lastWidth());
        assertEquals(32, WebClassifyMain.lastHeight());
        assertEquals(48 * 32, WebClassifyMain.lastArgb().length);
        assertEquals(48 * 32, WebClassifyMain.lastOwners().length);
        assertEquals(0, WebClassifyMain.lastMembers().length);
    }

    @Test
    void previewAfterCompletedFrameDoesNotRetainCompletedCapacity() {
        WebClassifyMain.computeFrame(96, 96, false, WebClassifyMain.OUTPUT_ALL);
        WebClassifyMain.computeFrame(24, 24, true, WebClassifyMain.OUTPUT_ALL);

        assertEquals(24 * 24, WebClassifyMain.lastArgb().length);
        assertEquals(24 * 24, WebClassifyMain.lastOwners().length);
        assertEquals(24 * 24, WebClassifyMain.lastMembers().length);
    }

    @Test
    void omittedDiagramSkipsArgbExport() {
        WebClassifyMain.computeFrame(16, 16, true, WebClassifyMain.OUTPUT_OWNERS);

        assertEquals(0, WebClassifyMain.lastArgb().length);
        assertEquals(16 * 16, WebClassifyMain.lastOwners().length);
        assertEquals(0, WebClassifyMain.lastMembers().length);
    }
}
