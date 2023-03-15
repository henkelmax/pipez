package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
    public void renderWidget(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
        Icon icon = icons.get(index.get());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, icon.texture);
        blit(matrixStack, getX() + 2, getY() + 2, icon.offsetX, icon.offsetY, 16, 16);
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
