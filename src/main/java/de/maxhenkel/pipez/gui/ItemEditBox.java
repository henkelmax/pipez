package de.maxhenkel.pipez.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ItemEditBox extends EditBox {

    public ItemEditBox(Font font, int x, int y, int width, int height, Component narration) {
        super(font, x, y, width, height, narration);
    }

    @Override
    public void insertText(String input) {
        String newText = getValue() + input;
        if (newText.startsWith("#")) {
            newText = newText.substring(1);
        }
        if (Identifier.tryParse(newText) != null) {
            super.insertText(input);
        }
    }
}
