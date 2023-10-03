package de.maxhenkel.pipez.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.corelib.client.RenderUtils;
import de.maxhenkel.corelib.helpers.AbstractStack;
import de.maxhenkel.corelib.helpers.WrappedFluidStack;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WrappedGasStack extends AbstractStack<ChemicalStack> {

    public WrappedGasStack(ChemicalStack stack) {
        super(stack);
    }

    @Nullable
    public static WrappedGasStack dummyStack(Object o) {
        if (o instanceof Chemical<?>) {
            ChemicalStack stack = GasUtils.createChemicalStack((Chemical) o, 1000);
            return new WrappedGasStack(stack);
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(GuiGraphics guiGraphics, int x, int y) {
        TextureAtlasSprite texture = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(stack.getType().getIcon());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        int chemicalTint = stack.getChemicalTint();
        RenderSystem.setShaderColor(RenderUtils.getRedFloat(chemicalTint), RenderUtils.getGreenFloat(chemicalTint), RenderUtils.getBlueFloat(chemicalTint), 1);
        RenderSystem.setShaderTexture(0, texture.atlasLocation());
        WrappedFluidStack.fluidBlit(guiGraphics, x, y, 16, 16, texture, stack.getType().getTint());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();

        tooltip.add(getDisplayName());

        if (Minecraft.getInstance().options.advancedItemTooltips) {
            ResourceLocation registryName = GasUtils.getResourceLocation(stack.getType());
            if (registryName != null) {
                tooltip.add((Component.literal(registryName.toString())).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        return tooltip;
    }

    @Override
    public Component getDisplayName() {
        return Component.empty().append(stack.getTextComponent());
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
