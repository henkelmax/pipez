package de.maxhenkel.pipez.blocks.tileentity.render;

import de.maxhenkel.pipez.ModelRegistry.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class EnergyPipeRenderer extends PipeRenderer {

    public EnergyPipeRenderer(BlockEntityRendererProvider.Context renderer) {
        super(renderer);
    }

    @Override
    Model getModel() {
        return Model.ENERGY_PIPE_EXTRACT;
    }
}
