package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.pipez.gui.sprite.SpriteRect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        Icon icon = icons.get(index.get());
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
        private ResourceLocation texture;
        private SpriteRect spriteRect;

        public Icon(ResourceLocation texture, SpriteRect spriteRect) {
            this.texture = texture;
            this.spriteRect = spriteRect;
        }
    }

}
