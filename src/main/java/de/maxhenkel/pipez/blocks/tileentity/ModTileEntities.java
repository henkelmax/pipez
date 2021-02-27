package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.render.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ModTileEntities {

    public static TileEntityType<ItemPipeTileEntity> ITEM_PIPE;
    public static TileEntityType<FluidPipeTileEntity> FLUID_PIPE;
    public static TileEntityType<EnergyPipeTileEntity> ENERGY_PIPE;
    public static TileEntityType<UniversalPipeTileEntity> UNIVERSAL_PIPE;
    public static TileEntityType<GasPipeTileEntity> GAS_PIPE;

    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        ITEM_PIPE = TileEntityType.Builder.create(ItemPipeTileEntity::new, ModBlocks.ITEM_PIPE).build(null);
        ITEM_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "item_pipe"));
        event.getRegistry().register(ITEM_PIPE);

        FLUID_PIPE = TileEntityType.Builder.create(FluidPipeTileEntity::new, ModBlocks.FLUID_PIPE).build(null);
        FLUID_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "fluid_pipe"));
        event.getRegistry().register(FLUID_PIPE);

        ENERGY_PIPE = TileEntityType.Builder.create(EnergyPipeTileEntity::new, ModBlocks.ENERGY_PIPE).build(null);
        ENERGY_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "energy_pipe"));
        event.getRegistry().register(ENERGY_PIPE);

        UNIVERSAL_PIPE = TileEntityType.Builder.create(UniversalPipeTileEntity::new, ModBlocks.UNIVERSAL_PIPE).build(null);
        UNIVERSAL_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "universal_pipe"));
        event.getRegistry().register(UNIVERSAL_PIPE);

        if (ModList.get().isLoaded("mekanism")) {
            GAS_PIPE = TileEntityType.Builder.create(GasPipeTileEntity::new, ModBlocks.GAS_PIPE).build(null);
            GAS_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "gas_pipe"));
            event.getRegistry().register(GAS_PIPE);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        ClientRegistry.bindTileEntityRenderer(ITEM_PIPE, ItemPipeRenderer::new);
        ClientRegistry.bindTileEntityRenderer(FLUID_PIPE, FluidPipeRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ENERGY_PIPE, EnergyPipeRenderer::new);
        ClientRegistry.bindTileEntityRenderer(UNIVERSAL_PIPE, UniversalPipeRenderer::new);
        if (ModList.get().isLoaded("mekanism")) {
            ClientRegistry.bindTileEntityRenderer(GAS_PIPE, GasPipeRenderer::new);
        }
    }

}
