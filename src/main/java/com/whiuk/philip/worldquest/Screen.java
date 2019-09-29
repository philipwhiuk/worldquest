package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.Component;

abstract class Screen extends Component {
    public Screen() {
        super();
    }

    public Screen(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
}
