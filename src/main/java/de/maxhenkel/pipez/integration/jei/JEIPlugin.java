package de.maxhenkel.pipez.integration.jei;

import de.maxhenkel.pipez.PipezMod;
import de.maxhenkel.pipez.gui.ExtractScreen;
import de.maxhenkel.pipez.gui.FilterScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "pipez");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(ExtractScreen.class, new ExtractScreenHandler());
        registration.addGhostIngredientHandler(FilterScreen.class, new FilterScreenGhostIngredientHandler());
    }
}
