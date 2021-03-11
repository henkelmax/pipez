package de.maxhenkel.pipez.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.helpers.AbstractStack;
import de.maxhenkel.corelib.helpers.WrappedFluidStack;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class WrappedGasStack extends AbstractStack<GasStack> {

    public WrappedGasStack(GasStack stack) {
        super(stack);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(MatrixStack matrixStack, int x, int y) {
        TextureAtlasSprite texture = Minecraft.getInstance().getModelManager().getAtlas(PlayerContainer.BLOCK_ATLAS).getSprite(stack.getType().getIcon());
        Minecraft.getInstance().getTextureManager().bind(texture.atlas().location());
        WrappedFluidStack.fluidBlit(matrixStack, x, y, 16, 16, texture, stack.getType().getTint());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<ITextComponent> getTooltip(Screen screen) {
        List<ITextComponent> tooltip = new ArrayList<>();

        tooltip.add(getDisplayName());

        if (Minecraft.getInstance().options.advancedItemTooltips) {
            ResourceLocation registryName = MekanismAPI.gasRegistry().getKey(stack.getType());
            if (registryName != null) {
                tooltip.add((new StringTextComponent(registryName.toString())).withStyle(TextFormatting.DARK_GRAY));
            }
        }

        return tooltip;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("").append(stack.getTextComponent());
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
