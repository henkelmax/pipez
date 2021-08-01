package de.maxhenkel.pipez.blocks.tileentity.render;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModelRegistry.Model;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class FluidPipeRenderer extends PipeRenderer {

    public FluidPipeRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    Model getModel() {
        return Model.FLUID_PIPE_EXTRACT;
    }
}
