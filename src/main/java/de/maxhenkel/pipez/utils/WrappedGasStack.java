package de.maxhenkel.pipez.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.corelib.client.RenderUtils;
import de.maxhenkel.corelib.helpers.AbstractStack;
import de.maxhenkel.corelib.helpers.WrappedFluidStack;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WrappedGasStack extends AbstractStack<GasStack> {

    public WrappedGasStack(GasStack stack) {
        super(stack);
    }

    @Nullable
    public static WrappedGasStack dummyStack(Object o) {
        if (o instanceof Gas) {
            GasStack stack = new GasStack((Gas) o, 1000);
            return new WrappedGasStack(stack);
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack matrixStack, int x, int y) {
        TextureAtlasSprite texture = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(stack.getType().getIcon());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        int chemicalTint = stack.getChemicalTint();
        RenderSystem.setShaderColor(RenderUtils.getRedFloat(chemicalTint), RenderUtils.getGreenFloat(chemicalTint), RenderUtils.getBlueFloat(chemicalTint), RenderUtils.getAlphaFloat(chemicalTint));
        RenderSystem.setShaderTexture(0, texture.atlas().location());
        WrappedFluidStack.fluidBlit(matrixStack, x, y, 16, 16, texture, stack.getType().getTint());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Component> getTooltip(Screen screen) {
        List<Component> tooltip = new ArrayList<>();

        tooltip.add(getDisplayName());

        if (Minecraft.getInstance().options.advancedItemTooltips) {
            ResourceLocation registryName = MekanismAPI.gasRegistry().getKey(stack.getType());
            if (registryName != null) {
                tooltip.add((new TextComponent(registryName.toString())).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        return tooltip;
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("").append(stack.getTextComponent());
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
