package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.function.Supplier;

public class CycleIconButton extends Button {

    private List<Icon> icons;
    private Supplier<Integer> index;

    public CycleIconButton(int x, int y, List<Icon> icons, Supplier<Integer> index, IPressable pressedAction) {
        super(x, y, 20, 20, StringTextComponent.EMPTY, pressedAction);
        this.icons = icons;
        this.index = index;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        Icon icon = icons.get(index.get());
        Minecraft.getInstance().getTextureManager().bindTexture(icon.texture);
        RenderSystem.color4f(1F, 1F, 1F, 1F);
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
