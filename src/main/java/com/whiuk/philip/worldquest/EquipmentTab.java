package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;

class EquipmentTab extends SidebarTab {
    WorldQuest game;

    EquipmentTab(WorldQuest game) {
        this.game = game;
    }

    @Override
    public void render(Graphics2D g) {
        int offset = y;
        g.setColor(Color.WHITE);
        String mainHandWeaponName = game.player.mainHandWeapon != null ?
                game.player.mainHandWeapon.name + " (+"+game.player.mainHandWeapon.damage+")" :
                "";
        g.drawString("Weapon: " + mainHandWeaponName, 425, offset);
        offset += 20;

        g.drawString("Armour:", 425, offset);
        offset += 20;

        for (Map.Entry<Slot, Armour> piece : game.player.armour.entrySet()) {
            Slot slot = piece.getKey();
            Armour armour = piece.getValue();
            String armourDescription = armour != null ?
                    armour.name + " (+"+armour.protection+")" :
                    "";
            g.drawString(slot.friendlyName+": " + armourDescription, 450, offset);
            offset += 20;
        }
    }

    @Override
    public void onClick(MouseEvent e) {

    }
}
