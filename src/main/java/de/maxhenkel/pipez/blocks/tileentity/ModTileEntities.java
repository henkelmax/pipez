package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.render.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;

public class ModTileEntities {

    public static BlockEntityType<ItemPipeTileEntity> ITEM_PIPE;
    public static BlockEntityType<FluidPipeTileEntity> FLUID_PIPE;
    public static BlockEntityType<EnergyPipeTileEntity> ENERGY_PIPE;
    public static BlockEntityType<GasPipeTileEntity> GAS_PIPE;
    public static BlockEntityType<UniversalPipeTileEntity> UNIVERSAL_PIPE;

    public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
        ITEM_PIPE = BlockEntityType.Builder.of(ItemPipeTileEntity::new, ModBlocks.ITEM_PIPE).build(null);
        ITEM_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "item_pipe"));
        event.getRegistry().register(ITEM_PIPE);

        FLUID_PIPE = BlockEntityType.Builder.of(FluidPipeTileEntity::new, ModBlocks.FLUID_PIPE).build(null);
        FLUID_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "fluid_pipe"));
        event.getRegistry().register(FLUID_PIPE);

        ENERGY_PIPE = BlockEntityType.Builder.of(EnergyPipeTileEntity::new, ModBlocks.ENERGY_PIPE).build(null);
        ENERGY_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "energy_pipe"));
        event.getRegistry().register(ENERGY_PIPE);

        if (ModList.get().isLoaded("mekanism")) {
            GAS_PIPE = BlockEntityType.Builder.of(GasPipeTileEntity::new, ModBlocks.GAS_PIPE).build(null);
            GAS_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "gas_pipe"));
            event.getRegistry().register(GAS_PIPE);
        }

        UNIVERSAL_PIPE = BlockEntityType.Builder.of(UniversalPipeTileEntity::new, ModBlocks.UNIVERSAL_PIPE).build(null);
        UNIVERSAL_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "universal_pipe"));
        event.getRegistry().register(UNIVERSAL_PIPE);
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        BlockEntityRenderers.register(ITEM_PIPE, ItemPipeRenderer::new);
        BlockEntityRenderers.register(FLUID_PIPE, FluidPipeRenderer::new);
        BlockEntityRenderers.register(ENERGY_PIPE, EnergyPipeRenderer::new);
        if (ModList.get().isLoaded("mekanism")) {
            BlockEntityRenderers.register(GAS_PIPE, GasPipeRenderer::new);
        }
        BlockEntityRenderers.register(UNIVERSAL_PIPE, UniversalPipeRenderer::new);
    }

}
