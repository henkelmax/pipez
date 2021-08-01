package de.maxhenkel.pipez.blocks.tileentity.render;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModelRegistry.Model;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class UniversalPipeRenderer extends PipeRenderer {

    public UniversalPipeRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    Model getModel() {
        return Model.UNIVERSAL_PIPE_EXTRACT;
    }
}
