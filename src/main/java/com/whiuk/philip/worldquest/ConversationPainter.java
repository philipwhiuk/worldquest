package com.whiuk.philip.worldquest;

import java.awt.*;

import static com.whiuk.philip.worldquest.MapConstants.*;

public class ConversationPainter {
    public static void paintConversation(Graphics2D g, WorldQuest.GameState gameState, NPC talkingTo) {
        String text = "...";
        if (gameState == WorldQuest.GameState.PLAYER_TALKING) {
            text = "Player: "+talkingTo.currentConversation.playerText;
        } else if (gameState == WorldQuest.GameState.NPC_TALKING) {
            text = talkingTo.type.name+": "+talkingTo.currentConversation.npcResponse;
        }
        g.setColor(Color.WHITE);
        g.drawRect(9, CONVERSATION_Y, BORDER_WIDTH, CONVERSATION_HEIGHT);
        g.drawString(text,25, CONVERSATION_Y+40);
        g.setColor(Color.CYAN);
        g.drawString("Continue",25, CONVERSATION_Y+65);
    }
}
