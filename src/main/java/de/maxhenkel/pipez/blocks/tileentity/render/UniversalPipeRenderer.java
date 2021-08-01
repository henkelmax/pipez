package de.maxhenkel.pipez.blocks.tileentity.render;

import de.maxhenkel.pipez.ModelRegistry.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class UniversalPipeRenderer extends PipeRenderer {

    public UniversalPipeRenderer(BlockEntityRendererProvider.Context renderer) {
        super(renderer);
    }

    @Override
    Model getModel() {
        return Model.UNIVERSAL_PIPE_EXTRACT;
    }
}
