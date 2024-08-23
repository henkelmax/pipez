package de.maxhenkel.pipez;

import de.maxhenkel.corelib.CommonRegistry;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.ModTileEntities;
import de.maxhenkel.pipez.events.BlockEvents;
import de.maxhenkel.pipez.gui.Containers;
import de.maxhenkel.pipez.integration.IMC;
import de.maxhenkel.pipez.items.ModItems;
import de.maxhenkel.pipez.net.*;
import de.maxhenkel.pipez.recipes.ModRecipes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "pipez";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static ServerConfig SERVER_CONFIG;
    public static ClientConfig CLIENT_CONFIG;

    public Main(IEventBus eventBus) {
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::onRegisterPayloadHandler);
        eventBus.addListener(IMC::enqueueIMC);

        SERVER_CONFIG = CommonRegistry.registerConfig(MODID, ModConfig.Type.SERVER, ServerConfig.class);
        CLIENT_CONFIG = CommonRegistry.registerConfig(MODID, ModConfig.Type.CLIENT, ClientConfig.class);

        if (FMLEnvironment.dist.isClient()) {
            eventBus.addListener(Main.this::clientSetup);
            eventBus.addListener(ModelRegistry::onModelRegister);
            eventBus.addListener(ModelRegistry::onModelBake);
            Containers.initClient(eventBus);
        }

        ModBlocks.init(eventBus);
        ModItems.init(eventBus);
        ModRecipes.init(eventBus);
        Containers.init(eventBus);
        ModTileEntities.init(eventBus);
        ModCreativeTabs.init(eventBus);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new BlockEvents());
    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        ModTileEntities.clientSetup();
    }

    public void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("1");
        CommonRegistry.registerMessage(registrar, CycleDistributionMessage.class);
        CommonRegistry.registerMessage(registrar, CycleRedstoneModeMessage.class);
        CommonRegistry.registerMessage(registrar, CycleFilterModeMessage.class);
        CommonRegistry.registerMessage(registrar, UpdateFilterMessage.class);
        CommonRegistry.registerMessage(registrar, RemoveFilterMessage.class);
        CommonRegistry.registerMessage(registrar, EditFilterMessage.class);
        CommonRegistry.registerMessage(registrar, OpenExtractMessage.class);
    }

}
