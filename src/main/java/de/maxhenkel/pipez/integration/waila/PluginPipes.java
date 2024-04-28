package de.maxhenkel.pipez.integration.waila;

import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

//TODO Re-add once Jade is updated
//@WailaPlugin
public class PluginPipes implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(HUDHandlerPipes.INSTANCE, PipeBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(HUDHandlerPipes.INSTANCE, UpgradeTileEntity.class);
    }
}
