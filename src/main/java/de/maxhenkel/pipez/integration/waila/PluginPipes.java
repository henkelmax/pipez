package de.maxhenkel.pipez.integration.waila;

import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class PluginPipes implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider(HUDHandlerPipes.INSTANCE, TooltipPosition.BODY, PipeBlock.class);
        registrar.registerBlockDataProvider(HUDHandlerPipes.INSTANCE, UpgradeTileEntity.class);
    }
}
