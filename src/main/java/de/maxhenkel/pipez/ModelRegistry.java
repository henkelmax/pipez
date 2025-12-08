package de.maxhenkel.pipez;

import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.concurrent.atomic.AtomicReference;

public class ModelRegistry {

    public enum Model {
        ENERGY_PIPE_EXTRACT(Identifier.fromNamespaceAndPath(PipezMod.MODID, "block/energy_pipe_extract")),
        FLUID_PIPE_EXTRACT(Identifier.fromNamespaceAndPath(PipezMod.MODID, "block/fluid_pipe_extract")),
        GAS_PIPE_EXTRACT(Identifier.fromNamespaceAndPath(PipezMod.MODID, "block/gas_pipe_extract")),
        ITEM_PIPE_EXTRACT(Identifier.fromNamespaceAndPath(PipezMod.MODID, "block/item_pipe_extract")),
        UNIVERSAL_PIPE_EXTRACT(Identifier.fromNamespaceAndPath(PipezMod.MODID, "block/universal_pipe_extract"));

        private final Identifier resource;
        private final StandaloneModelKey<QuadCollection> modelKey;
        private final AtomicReference<QuadCollection> model;

        Model(Identifier rl) {
            resource = rl;
            modelKey = new StandaloneModelKey<>(rl::toString);
            model = new AtomicReference<>();
        }

        public Identifier getIdentifier() {
            return resource;
        }

        public StandaloneModelKey<QuadCollection> getModelKey() {
            return modelKey;
        }

        public AtomicReference<QuadCollection> getModel() {
            return model;
        }
    }

    public static void onModelRegister(ModelEvent.RegisterStandalone event) {
        for (Model model : Model.values()) {
            event.register(model.getModelKey(), new SimpleUnbakedStandaloneModel<>(model.resource, (resolvedModel, baker) -> {
                ResolvedModel resolvedmodel = baker.getModel(model.getIdentifier());
                TextureSlots textureslots = resolvedmodel.getTopTextureSlots();
                return resolvedModel.bakeTopGeometry(textureslots, baker, BlockModelRotation.IDENTITY);
            }));
        }
    }

    public static void onModelBake(ModelEvent.BakingCompleted event) {
        for (Model model : Model.values()) {
            QuadCollection quads = event.getBakingResult().standaloneModels().get(model.getModelKey());
            if (quads == null) {
                continue;
            }
            model.getModel().set(quads);
        }
    }
}
