package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;

interface UI {
    void render(Graphics2D g);
    void onClick(MouseEvent e);
}