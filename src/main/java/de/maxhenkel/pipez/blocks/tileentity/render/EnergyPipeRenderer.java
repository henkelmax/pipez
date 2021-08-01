package de.maxhenkel.pipez.blocks.tileentity.render;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModelRegistry.Model;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class EnergyPipeRenderer extends PipeRenderer {

    public EnergyPipeRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    Model getModel() {
        return Model.ENERGY_PIPE_EXTRACT;
    }
}
