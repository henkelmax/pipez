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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "pipez";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static ServerConfig SERVER_CONFIG;
    public static ClientConfig CLIENT_CONFIG;

    public static SimpleChannel SIMPLE_CHANNEL;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(IMC::enqueueIMC);

        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig.class);
        CLIENT_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.CLIENT, ClientConfig.class);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ModelRegistry::onModelRegister);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ModelRegistry::onModelBake);
        });

        ModBlocks.init();
        ModItems.init();
        ModRecipes.init();
        Containers.init();
        ModTileEntities.init();
        ModCreativeTabs.init();
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new BlockEvents());

        SIMPLE_CHANNEL = CommonRegistry.registerChannel(Main.MODID, "default");
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 0, CycleDistributionMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 1, CycleRedstoneModeMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 2, CycleFilterModeMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 3, UpdateFilterMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 4, RemoveFilterMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 5, EditFilterMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 6, OpenExtractMessage.class);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        ModTileEntities.clientSetup();
        Containers.clientSetup();
    }

}
