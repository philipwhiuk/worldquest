package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.UI;

import java.awt.*;

abstract class SidebarTab extends Rectangle implements UI {
    SidebarTab() {
        super(420, 170, 240, 330);
    }
}
