package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class CycleIconButton extends Button {

    private List<Icon> icons;
    private Supplier<Integer> index;

    public CycleIconButton(int x, int y, List<Icon> icons, Supplier<Integer> index, OnPress pressedAction) {
        super(x, y, 20, 20, TextComponent.EMPTY, pressedAction);
        this.icons = icons;
        this.index = index;
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        Icon icon = icons.get(index.get());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, icon.texture);
        blit(matrixStack, x + 2, y + 2, icon.offsetX, icon.offsetY, 16, 16);
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
