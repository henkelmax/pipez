package de.maxhenkel.pipez;

import de.maxhenkel.pipez.blocks.tileentity.ModTileEntities;
import de.maxhenkel.pipez.gui.Containers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = PipezMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = PipezMod.MODID, value = Dist.CLIENT)
public class PipezClientMod {

    public PipezClientMod(IEventBus eventBus) {
        eventBus.addListener(ModelRegistry::onModelRegister);
        eventBus.addListener(ModelRegistry::onModelBake);
        Containers.initClient(eventBus);
    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        ModTileEntities.clientSetup();
    }

}
