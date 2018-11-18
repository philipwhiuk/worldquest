package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;

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

        Armour chestArmour = game.player.armour.get(Slot.CHEST);
        String chestArmourName = chestArmour != null ?
                chestArmour.name + " (+"+chestArmour.protection+")" :
                "";
        g.drawString("Chest: " + chestArmourName, 450, offset);
    }

    @Override
    public void onClick(MouseEvent e) {

    }
}
