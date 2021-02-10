package de.maxhenkel.pipez.blocks.tileentity.render;

import de.maxhenkel.pipez.Main;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;

public class EnergyPipeRenderer extends PipeRenderer {

    public static final ResourceLocation EXTRACT_MODEL = new ResourceLocation(Main.MODID, "block/energy_pipe_extract");

    public EnergyPipeRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    ResourceLocation getModel() {
        return EXTRACT_MODEL;
    }
}
