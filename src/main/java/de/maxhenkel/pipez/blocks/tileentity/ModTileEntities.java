package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.render.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTileEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Main.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeTileEntity>> ITEM_PIPE = BLOCK_ENTITY_REGISTER.register("item_pipe", () ->
            BlockEntityType.Builder.of(ItemPipeTileEntity::new, ModBlocks.ITEM_PIPE.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeTileEntity>> FLUID_PIPE = BLOCK_ENTITY_REGISTER.register("fluid_pipe", () ->
            BlockEntityType.Builder.of(FluidPipeTileEntity::new, ModBlocks.FLUID_PIPE.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyPipeTileEntity>> ENERGY_PIPE = BLOCK_ENTITY_REGISTER.register("energy_pipe", () ->
            BlockEntityType.Builder.of(EnergyPipeTileEntity::new, ModBlocks.ENERGY_PIPE.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GasPipeTileEntity>> GAS_PIPE = BLOCK_ENTITY_REGISTER.register("gas_pipe", () ->
            BlockEntityType.Builder.of(GasPipeTileEntity::new, ModBlocks.GAS_PIPE.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UniversalPipeTileEntity>> UNIVERSAL_PIPE = BLOCK_ENTITY_REGISTER.register("universal_pipe", () ->
            BlockEntityType.Builder.of(UniversalPipeTileEntity::new, ModBlocks.UNIVERSAL_PIPE.get()).build(null)
    );

    public static void init() {
        BLOCK_ENTITY_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        BlockEntityRenderers.register(ITEM_PIPE.get(), ItemPipeRenderer::new);
        BlockEntityRenderers.register(FLUID_PIPE.get(), FluidPipeRenderer::new);
        BlockEntityRenderers.register(ENERGY_PIPE.get(), EnergyPipeRenderer::new);
        BlockEntityRenderers.register(GAS_PIPE.get(), GasPipeRenderer::new);
        BlockEntityRenderers.register(UNIVERSAL_PIPE.get(), UniversalPipeRenderer::new);
    }

}
