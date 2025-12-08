package de.maxhenkel.pipez.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CycleIconButton extends AbstractButton {

    private List<Icon> icons;
    private Supplier<Integer> index;
    private Consumer<CycleIconButton> onPress;

    public CycleIconButton(int x, int y, List<Icon> icons, Supplier<Integer> index, Consumer<CycleIconButton> onPress) {
        super(x, y, 20, 20, Component.empty());
        this.icons = icons;
        this.index = index;
        this.onPress = onPress;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderDefaultSprite(guiGraphics);
        Icon icon = icons.get(index.get());
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon.texture, getX() + 2, getY() + 2, icon.offsetX, icon.offsetY, 16, 16, 256, 256);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onPress.accept(this);
    }

    public static class Icon {
        private Identifier texture;
        private int offsetX, offsetY;

        public Icon(Identifier texture, int offsetX, int offsetY) {
            this.texture = texture;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

}
