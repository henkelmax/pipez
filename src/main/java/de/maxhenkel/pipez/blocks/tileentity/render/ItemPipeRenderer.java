package de.maxhenkel.pipez.blocks.tileentity.render;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModelRegistry.Model;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class ItemPipeRenderer extends PipeRenderer {

    public ItemPipeRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    Model getModel() {
        return Model.ITEM_PIPE_EXTRACT;
    }
}
