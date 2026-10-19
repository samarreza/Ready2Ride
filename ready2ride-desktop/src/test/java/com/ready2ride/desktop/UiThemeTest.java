package com.ready2ride.desktop;

import com.ready2ride.desktop.util.UiTheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UiThemeTest {

    @Test
    @DisplayName("Should verify UI theme color palette and typography constants")
    void testThemeConstants() {
        assertNotNull(UiTheme.COLOR_PRIMARY);
        assertNotNull(UiTheme.COLOR_PRIMARY_HOVER);
        assertNotNull(UiTheme.COLOR_NAVY_DARK);
        assertNotNull(UiTheme.COLOR_WHITE);
        assertNotNull(UiTheme.COLOR_BACKGROUND_LIGHT);

        assertEquals("Segoe UI", UiTheme.FONT_FAMILY);
        assertNotNull(UiTheme.FONT_TITLE);
        assertNotNull(UiTheme.FONT_HEADER);
        assertNotNull(UiTheme.FONT_BODY);
        assertNotNull(UiTheme.FONT_BODY_BOLD);
    }
}
