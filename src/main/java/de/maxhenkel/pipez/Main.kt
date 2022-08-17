package de.maxhenkel.pipez

import de.maxhenkel.corelib.CommonRegistry
import de.maxhenkel.pipez.blocks.ModBlocks
import de.maxhenkel.pipez.blocks.tileentity.ModTileEntities
import de.maxhenkel.pipez.events.BlockEvents
import de.maxhenkel.pipez.events.ServerTickEvents
import de.maxhenkel.pipez.events.StitchEvents
import de.maxhenkel.pipez.gui.Containers
import de.maxhenkel.pipez.integration.IMC
import de.maxhenkel.pipez.items.ModItems
import de.maxhenkel.pipez.net.CycleDistributionMessage
import de.maxhenkel.pipez.net.CycleFilterModeMessage
import de.maxhenkel.pipez.net.CycleRedstoneModeMessage
import de.maxhenkel.pipez.net.EditFilterMessage
import de.maxhenkel.pipez.net.OpenExtractMessage
import de.maxhenkel.pipez.net.RemoveFilterMessage
import de.maxhenkel.pipez.net.UpdateFilterMessage
import de.maxhenkel.pipez.recipes.ModRecipes
import de.maxhenkel.pipez.tagproviders.ModTagProviders
import de.maxhenkel.pipez.tags.ModItemTags
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.network.simple.SimpleChannel
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist

@Mod(Main.MODID)
object Main {
    const val MODID = "pipez"

    // Logger
    val LOGGER = LogManager.getLogger(MODID)

    val SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig::class.java)
    val CLIENT_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.CLIENT, ClientConfig::class.java)

    lateinit var SIMPLE_CHANNEL:SimpleChannel
    private var hasMekanism:Boolean? = null

    init {
        LOGGER.log(Level.DEBUG, ModItemTags.TOOLS_TAG)
        MOD_BUS.apply {
            // Item / Block register
            addGenericListener(Item::class.java, ModBlocks::registerItems)
            addGenericListener(Item::class.java, ModItems::registerItems)
            addGenericListener(Block::class.java, ModBlocks::registerBlocks)
            // Block Entity Register
            addGenericListener(BlockEntityType::class.java, ModTileEntities::registerTileEntities)
            // GUI
            addGenericListener(MenuType::class.java, Containers::registerContainers)
            // Recipe
            addGenericListener(RecipeSerializer::class.java, ModRecipes::registerRecipes)

            // Common Setup
            addListener(::onCommonSetup)
            addListener(IMC::enqueueIMC)
            addListener(ModTagProviders::gatherData)
        }
        // DistExecutor
        runForDist(
            clientTarget = {
                MOD_BUS.apply {
                    addListener(::onClientSetup)
                    addListener(StitchEvents::onStitch)
                    addListener(ModelRegistry::onModelRegister)
                    addListener(ModelRegistry::onModelBake)
                }
                // MOD_BUS.addListener(::onClientSetup)
            },
            serverTarget = {},
        )
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        MinecraftForge.EVENT_BUS.apply {
            register(BlockEvents())
            register(ServerTickEvents())
        }
        SIMPLE_CHANNEL = CommonRegistry.registerChannel(MODID, "default")
        arrayOf(
            CycleDistributionMessage::class.java,
            CycleRedstoneModeMessage::class.java,
            CycleFilterModeMessage::class.java,
            UpdateFilterMessage::class.java,
            RemoveFilterMessage::class.java,
            EditFilterMessage::class.java,
            OpenExtractMessage::class.java,
        ).forEachIndexed { id, clz ->
            CommonRegistry.registerMessage(SIMPLE_CHANNEL, id, clz)
        }
    }

    @OnlyIn(Dist.CLIENT)
    private fun onClientSetup(event: FMLClientSetupEvent) {
        ModTileEntities.clientSetup()
        Containers.clientSetup()
    }

    /**
     * Is Mekanism loaded?
     */
    fun isMekanismLoaded():Boolean {
        var _hasMekanism = hasMekanism
        return if (_hasMekanism != null) {
            _hasMekanism
        } else {
            _hasMekanism = ModList.get().isLoaded("mekanism")
            hasMekanism = _hasMekanism
            _hasMekanism
        }
    }

    fun logDebug(msg:Any) {
        LOGGER.debug(msg)
    }
}