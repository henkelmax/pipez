package de.maxhenkel.pipez.blocks.tileentity.render;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModelRegistry.Model;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class GasPipeRenderer extends PipeRenderer {

    public GasPipeRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    Model getModel() {
        return Model.GAS_PIPE_EXTRACT;
    }
}
