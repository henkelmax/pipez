package de.maxhenkel.pipez.blocks.tileentity.render;

import de.maxhenkel.pipez.Main;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;

public class GasPipeRenderer extends PipeRenderer {

    public static final ResourceLocation EXTRACT_MODEL = new ResourceLocation(Main.MODID, "block/gas_pipe_extract");

    public GasPipeRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    ResourceLocation getModel() {
        return EXTRACT_MODEL;
    }
}
