package com.whiuk.philip.worldquest;

import java.awt.*;
import java.util.List;

import static com.whiuk.philip.worldquest.MapConstants.*;

public class ConversationPainter {
    public static void paintConversation(Graphics2D g, WorldQuest.MessageState messageState, NPC talkingTo) {
        String text = "...";
        if (messageState == WorldQuest.MessageState.PLAYER_TALKING) {
            text = "Player: "+talkingTo.currentConversation.playerText;
        } else if (messageState == WorldQuest.MessageState.NPC_TALKING) {
            text = talkingTo.type.name+": "+talkingTo.currentConversation.npcResponse;
        }
        g.setColor(Color.WHITE);
        g.drawRect(9, CONVERSATION_Y, BORDER_WIDTH, CONVERSATION_HEIGHT);
        g.drawString(text,25, CONVERSATION_Y+40);
        g.setColor(Color.CYAN);
        g.drawString("Continue",25, CONVERSATION_Y+65);
    }

    public static void paintConversationOptions(Graphics2D g, ConversationChoiceSelection ccs) {
        g.setColor(Color.WHITE);
        g.drawRect(9, CONVERSATION_Y, BORDER_WIDTH, CONVERSATION_HEIGHT);
        List<ConversationChoice> options = ccs.conversationOptions;
        for (int i = 0; i < options.size(); i++) {
            g.setColor(Color.CYAN);
            g.drawString((i+1)+".", 25, CONVERSATION_Y+40+(i*25));
            g.setColor(Color.WHITE);
            g.drawString(options.get(i).playerText, 50, CONVERSATION_Y+40+(i*25));
        }
    }
}
