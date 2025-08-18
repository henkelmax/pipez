package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.PipezMod;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.render.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTileEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, PipezMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeTileEntity>> ITEM_PIPE = BLOCK_ENTITY_REGISTER.register("item_pipe", () ->
            new BlockEntityType<>(ItemPipeTileEntity::new, ModBlocks.ITEM_PIPE.get())
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeTileEntity>> FLUID_PIPE = BLOCK_ENTITY_REGISTER.register("fluid_pipe", () ->
            new BlockEntityType<>(FluidPipeTileEntity::new, ModBlocks.FLUID_PIPE.get())
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeTileEntity>> ENERGY_PIPE = BLOCK_ENTITY_REGISTER.register("energy_pipe", () ->
            new BlockEntityType<>(EnergyPipeTileEntity::new, ModBlocks.ENERGY_PIPE.get())
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GasPipeTileEntity>> GAS_PIPE = BLOCK_ENTITY_REGISTER.register("gas_pipe", () ->
            new BlockEntityType<>(GasPipeTileEntity::new, ModBlocks.GAS_PIPE.get())
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UniversalPipeTileEntity>> UNIVERSAL_PIPE = BLOCK_ENTITY_REGISTER.register("universal_pipe", () ->
            new BlockEntityType<>(UniversalPipeTileEntity::new, ModBlocks.UNIVERSAL_PIPE.get())
    );

    public static void init(IEventBus eventBus) {
        BLOCK_ENTITY_REGISTER.register(eventBus);
        eventBus.addListener(ModTileEntities::onRegisterCapabilities);
    }

    public static void clientSetup() {
        BlockEntityRenderers.register(ITEM_PIPE.get(), ItemPipeRenderer::new);
        BlockEntityRenderers.register(FLUID_PIPE.get(), FluidPipeRenderer::new);
        BlockEntityRenderers.register(ENERGY_PIPE.get(), EnergyPipeRenderer::new);
        BlockEntityRenderers.register(GAS_PIPE.get(), GasPipeRenderer::new);
        BlockEntityRenderers.register(UNIVERSAL_PIPE.get(), UniversalPipeRenderer::new);
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        onRegisterPipeCapabilities(event, ITEM_PIPE);
        onRegisterPipeCapabilities(event, FLUID_PIPE);
        onRegisterPipeCapabilities(event, ENERGY_PIPE);
        onRegisterPipeCapabilities(event, GAS_PIPE);
        onRegisterPipeCapabilities(event, UNIVERSAL_PIPE);
    }

    public static <U extends PipeLogicTileEntity, T extends BlockEntityType<U>> void onRegisterPipeCapabilities(RegisterCapabilitiesEvent event, DeferredHolder<BlockEntityType<?>, T> holder) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, holder.get(), (object, context) -> object.onRegisterCapability(Capabilities.ItemHandler.BLOCK, context));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, holder.get(), (object, context) -> object.onRegisterCapability(Capabilities.EnergyStorage.BLOCK, context));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, holder.get(), (object, context) -> object.onRegisterCapability(Capabilities.FluidHandler.BLOCK, context));
    }

}
