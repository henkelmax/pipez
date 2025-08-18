package de.maxhenkel.pipez.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CycleIconButton extends AbstractButton {

    private List<IconButton.Icon> icons;
    private Supplier<Integer> index;
    private Consumer<CycleIconButton> onPress;

    public CycleIconButton(int x, int y, List<IconButton.Icon> icons, Supplier<Integer> index, Consumer<CycleIconButton> onPress) {
        // TODO ---> 20
        super(x, y, 20, 20, Component.empty());
        this.icons = icons;
        this.index = index;
        this.onPress = onPress;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        IconButton.Icon icon = icons.get(index.get());
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon.texture, getX() + 2, getY() + 2, icon.spriteRect.x, icon.spriteRect.y, icon.spriteRect.w, icon.spriteRect.h, 256, 256);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    public void onPress() {
        onPress.accept(this);
    }

    public static class Icon {
        private ResourceLocation texture;
        private int offsetX, offsetY;

        public Icon(ResourceLocation texture, int offsetX, int offsetY) {
            this.texture = texture;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

}
