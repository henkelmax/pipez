package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.render.PipeRenderer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ModTileEntities {

    public static TileEntityType<ItemPipeTileEntity> ITEM_PIPE;

    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        ITEM_PIPE = TileEntityType.Builder.create(ItemPipeTileEntity::new, ModBlocks.ITEM_PIPE).build(null);
        ITEM_PIPE.setRegistryName(new ResourceLocation(Main.MODID, "item_pipe"));
        event.getRegistry().register(ITEM_PIPE);
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.ITEM_PIPE, PipeRenderer::new);
    }

}
