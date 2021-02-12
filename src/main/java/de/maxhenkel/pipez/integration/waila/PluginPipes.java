package de.maxhenkel.pipez.integration.waila;

import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.util.ResourceLocation;

@WailaPlugin
public class PluginPipes implements IWailaPlugin {

    static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation("waila", "object_name");
    static final ResourceLocation CONFIG_SHOW_REGISTRY = new ResourceLocation("waila", "show_registry");
    static final ResourceLocation REGISTRY_NAME_TAG = new ResourceLocation("waila", "registry_name");

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider(HUDHandlerPipes.INSTANCE, TooltipPosition.BODY, UpgradeTileEntity.class);
        registrar.registerBlockDataProvider(HUDHandlerPipes.INSTANCE, UpgradeTileEntity.class);
    }
}
