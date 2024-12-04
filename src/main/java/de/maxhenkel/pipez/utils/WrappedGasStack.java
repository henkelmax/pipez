package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.helpers.AbstractStack;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WrappedGasStack extends AbstractStack<ChemicalStack> {

    public WrappedGasStack(ChemicalStack stack) {
        super(stack);
    }

    @Nullable
    public static WrappedGasStack dummyStack(Object o) {
        if (o instanceof Chemical chemical) {
            return new WrappedGasStack(new ChemicalStack(chemical, 1000));
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(GuiGraphics guiGraphics, int x, int y) {
        TextureAtlasSprite texture = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(stack.getChemical().getIcon());
        int chemicalTint = stack.getChemicalTint();
        guiGraphics.blitSprite(RenderType::guiTextured, texture, x, y, 16, 16, chemicalTint);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();

        tooltip.add(getDisplayName());

        if (Minecraft.getInstance().options.advancedItemTooltips) {
            ResourceLocation registryName = MekanismAPI.CHEMICAL_REGISTRY.getKey(stack.getChemical());
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
