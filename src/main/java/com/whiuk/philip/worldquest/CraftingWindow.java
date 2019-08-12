package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

class CraftingWindow extends Window {
    private WorldQuest game;
    private CraftingOptions craftingOptions;

    CraftingWindow(WorldQuest game, CraftingOptions craftingOptions) {
        super(10, 10, 400, 200, craftingOptions.name);
        this.game = game;
        this.craftingOptions = craftingOptions;
    }

    @Override
    public void renderWindowFrame(Graphics2D g) {
        paintRecipes(g, craftingOptions.recipes);
    }

    @Override
    public void onContentClick(MouseEvent e) {
        for (int i = 0; i < craftingOptions.recipes.size(); i++) {
            if (craftButton(i).contains(e.getPoint())) {
                game.craftRecipe(craftingOptions, i);
            }
        }
    }

    private Rectangle craftButton(int i) {
        return new Rectangle(250, (i*20)+88, 15, 15);
    }

    private void paintRecipes(Graphics2D g, List<Recipe> recipes) {
        for (int i = 0; i < recipes.size(); i++) {
            Recipe listing = recipes.get(i);
            String text = listing.outputName;
            g.setColor(Color.WHITE);
            g.drawString(text, 40, 100+(i*20));

            ButtonPainter.paintButton(g, Color.GREEN, Color.BLACK, 250, (i*20)+88, "+");
        }
    }
}
