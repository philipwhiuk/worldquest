package com.whiuk.philip.worldquest;

import com.whiuk.philip.worldquest.ui.Component;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;

class EquipmentView extends Component {
    WorldQuest game;

    EquipmentView(WorldQuest game) {
        this.game = game;
    }

    @Override
    public void render(Graphics2D g) {
        int offset = y;
        g.setColor(Color.WHITE);
        String mainHandWeaponName = game.player.mainHandWeapon != null ?
                game.player.mainHandWeapon.name + " (+"+game.player.mainHandWeapon.damage+")" :
                "";
        g.drawString("Weapon: " + mainHandWeaponName, 5, offset);
        offset += 20;

        g.drawString("Armour:", 5, offset);
        offset += 20;

        for (Map.Entry<Slot, Armour> piece : game.player.armour.entrySet()) {
            Slot slot = piece.getKey();
            Armour armour = piece.getValue();
            String armourDescription = armour != null ?
                    armour.name + " (+"+armour.protection+")" :
                    "";
            g.drawString(slot.friendlyName+": " + armourDescription, 30, offset);
            offset += 20;
        }
    }

    @Override
    public void onClick(MouseEvent e) {

    }
}
