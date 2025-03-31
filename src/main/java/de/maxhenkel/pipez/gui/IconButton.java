package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.pipez.gui.sprite.SpriteRect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class IconButton extends AbstractButton {
    private Icon icon;
    private Consumer<IconButton> onPress;

    public IconButton(int x, int y, Icon icon, Consumer<IconButton> onPress) {
        // TODO ---> 20
        super(x, y, 20, 20, Component.empty());
        this.icon = icon;
        this.onPress = onPress;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        guiGraphics.blit(icon.texture, getX() + 2, getY() + 2, icon.spriteRect.x, icon.spriteRect.y, icon.spriteRect.w, icon.spriteRect.h);
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
        public ResourceLocation texture;
        public SpriteRect spriteRect;

        public Icon(ResourceLocation texture, SpriteRect spriteRect) {
            this.texture = texture;
            this.spriteRect = spriteRect;
        }
    }

}
