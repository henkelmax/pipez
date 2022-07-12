package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.render.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class ModTileEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Main.MODID);

    public static final RegistryObject<BlockEntityType<ItemPipeTileEntity>> ITEM_PIPE = BLOCK_ENTITY_REGISTER.register("item_pipe", () ->
            BlockEntityType.Builder.of(ItemPipeTileEntity::new, ModBlocks.ITEM_PIPE.get()).build(null)
    );
    public static final RegistryObject<BlockEntityType<FluidPipeTileEntity>> FLUID_PIPE = BLOCK_ENTITY_REGISTER.register("fluid_pipe", () ->
            BlockEntityType.Builder.of(FluidPipeTileEntity::new, ModBlocks.FLUID_PIPE.get()).build(null)
    );
    public static final RegistryObject<BlockEntityType<EnergyPipeTileEntity>> ENERGY_PIPE = BLOCK_ENTITY_REGISTER.register("energy_pipe", () ->
            BlockEntityType.Builder.of(EnergyPipeTileEntity::new, ModBlocks.ENERGY_PIPE.get()).build(null)
    );
    // TODO Add back Mekanism
    /*public static final RegistryObject<BlockEntityType<GasPipeTileEntity>> GAS_PIPE = BLOCK_ENTITY_REGISTER.register("gas_pipe", () ->
            BlockEntityType.Builder.of(GasPipeTileEntity::new, ModBlocks.GAS_PIPE.get()).build(null)
    );*/
    public static final RegistryObject<BlockEntityType<UniversalPipeTileEntity>> UNIVERSAL_PIPE = BLOCK_ENTITY_REGISTER.register("universal_pipe", () ->
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
        // TODO Add back Mekanism
        // BlockEntityRenderers.register(GAS_PIPE.get(), GasPipeRenderer::new);
        BlockEntityRenderers.register(UNIVERSAL_PIPE.get(), UniversalPipeRenderer::new);
    }

}
