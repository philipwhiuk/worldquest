package com.whiuk.philip.worldquest;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        return new Rectangle(270, (i*20)+68, 15, 15);
    }

    private void paintRecipes(Graphics2D g, List<Recipe> recipes) {
        for (int i = 0; i < recipes.size(); i++) {
            Recipe listing = recipes.get(i);
            Function<Recipe.RecipeItem, String> ingredientToString = ingredient -> {
                if (ingredient.quantity == 1) {
                    return ingredient.item.name;
                } else {
                    return "" + ingredient.quantity + " " + ingredient.item.name;
                }
            };
            String ingredients = listing.input.stream().map(ingredientToString).collect(Collectors.joining(","));
            String text = listing.outputName + " - " + ingredients;
            g.setColor(Color.WHITE);
            g.drawString(text, 40, 80+(i*20));

            ButtonPainter.paintButton(g, Color.GREEN, Color.BLACK, 270, (i*20)+68, "+");
        }
    }
}
