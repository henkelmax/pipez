package de.maxhenkel.pipez.blocks.tileentity.render;

import de.maxhenkel.pipez.ModelRegistry.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class GasPipeRenderer extends PipeRenderer {

    public GasPipeRenderer(BlockEntityRendererProvider.Context renderer) {
        super(renderer);
    }

    @Override
    Model getModel() {
        return Model.GAS_PIPE_EXTRACT;
    }
}
