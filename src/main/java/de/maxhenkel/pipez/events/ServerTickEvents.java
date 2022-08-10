package de.maxhenkel.pipez.events;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.capabilities.CapabilityCache;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerTickEvents {
    private static final int LOAD_CONFIG_INTERVAL = 20 * 30; // 30 seconds
    private static final int CAPABILITY_INTERVAL = 10; // 0.5 seconds

    public static int itemPipeSpeed;
    public static int itemPipeSpeedBasic;

    public static int itemPipeSpeedImproved;
    public static int itemPipeSpeedAdvanced;
    public static int itemPipeSpeedUltimate;
    public static int itemPipeAmount;
    public static int itemPipeAmountBasic;
    public static int itemPipeAmountImproved;
    public static int itemPipeAmountAdvanced;
    public static int itemPipeAmountUltimate;

    public static int fluidPipeAmount;
    public static int fluidPipeAmountBasic;
    public static int fluidPipeAmountImproved;
    public static int fluidPipeAmountAdvanced;
    public static int fluidPipeAmountUltimate;

    public static int energyPipeAmount;
    public static int energyPipeAmountBasic;
    public static int energyPipeAmountImproved;
    public static int energyPipeAmountAdvanced;
    public static int energyPipeAmountUltimate;

    public static int gasPipeAmount;
    public static int gasPipeAmountBasic;
    public static int gasPipeAmountImproved;
    public static int gasPipeAmountAdvanced;
    public static int gasPipeAmountUltimate;

    public static CapabilityCache capabilityCache = new CapabilityCache();

    private static long tickCount = 0;
    protected Logger logger = LogManager.getLogger(Main.MODID);

    public ServerTickEvents() {
        updateConfig();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (tickCount % LOAD_CONFIG_INTERVAL == 0) {
            logger.log(Level.DEBUG, "Updating Config");
            updateConfig();
        }
        tickCount += 1;
        capabilityCache.addTick();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        capabilityCache = new CapabilityCache();
        tickCount = 0;
        logger.log(Level.DEBUG, "On server started!");
    }

    /**
     * Update Config value to latest
     */
    private void updateConfig() {
        // Items
        itemPipeSpeed = Main.SERVER_CONFIG.itemPipeSpeed.get();
        itemPipeAmount = Main.SERVER_CONFIG.itemPipeAmount.get();
        itemPipeSpeedBasic = Main.SERVER_CONFIG.itemPipeSpeedBasic.get();
        itemPipeAmountBasic = Main.SERVER_CONFIG.itemPipeAmountBasic.get();
        itemPipeSpeedImproved = Main.SERVER_CONFIG.itemPipeSpeedImproved.get();
        itemPipeAmountImproved = Main.SERVER_CONFIG.itemPipeAmountImproved.get();
        itemPipeSpeedAdvanced = Main.SERVER_CONFIG.itemPipeSpeedAdvanced.get();
        itemPipeAmountAdvanced = Main.SERVER_CONFIG.itemPipeAmountAdvanced.get();
        itemPipeSpeedUltimate = Main.SERVER_CONFIG.itemPipeSpeedUltimate.get();
        itemPipeAmountUltimate = Main.SERVER_CONFIG.itemPipeAmountUltimate.get();

        // Fluids
        fluidPipeAmount = Main.SERVER_CONFIG.fluidPipeAmount.get();
        fluidPipeAmountBasic = Main.SERVER_CONFIG.fluidPipeAmountBasic.get();
        fluidPipeAmountImproved = Main.SERVER_CONFIG.fluidPipeAmountImproved.get();
        fluidPipeAmountAdvanced = Main.SERVER_CONFIG.fluidPipeAmountAdvanced.get();
        fluidPipeAmountUltimate = Main.SERVER_CONFIG.fluidPipeAmountUltimate.get();

        // Energy
        energyPipeAmount = Main.SERVER_CONFIG.energyPipeAmount.get();
        energyPipeAmountBasic = Main.SERVER_CONFIG.energyPipeAmountBasic.get();
        energyPipeAmountImproved = Main.SERVER_CONFIG.energyPipeAmountImproved.get();
        energyPipeAmountAdvanced = Main.SERVER_CONFIG.energyPipeAmountAdvanced.get();
        energyPipeAmountUltimate = Main.SERVER_CONFIG.energyPipeAmountUltimate.get();

        // Gases
        gasPipeAmount = Main.SERVER_CONFIG.gasPipeAmount.get();
        gasPipeAmountBasic = Main.SERVER_CONFIG.gasPipeAmountBasic.get();
        gasPipeAmountImproved = Main.SERVER_CONFIG.gasPipeAmountImproved.get();
        gasPipeAmountAdvanced = Main.SERVER_CONFIG.gasPipeAmountAdvanced.get();
        gasPipeAmountUltimate = Main.SERVER_CONFIG.gasPipeAmountUltimate.get();
    }
}
