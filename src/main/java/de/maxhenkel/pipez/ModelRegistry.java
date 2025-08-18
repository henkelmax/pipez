package de.maxhenkel.pipez;

import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.concurrent.atomic.AtomicReference;

public class ModelRegistry {

    public enum Model {
        ENERGY_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "block/energy_pipe_extract")),
        FLUID_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "block/fluid_pipe_extract")),
        GAS_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "block/gas_pipe_extract")),
        ITEM_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "block/item_pipe_extract")),
        UNIVERSAL_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "block/universal_pipe_extract"));

        private final ResourceLocation resource;
        private final StandaloneModelKey<QuadCollection> modelKey;
        private final AtomicReference<QuadCollection> model;

        Model(ResourceLocation rl) {
            resource = rl;
            modelKey = new StandaloneModelKey<>(rl::toString);
            model = new AtomicReference<>();
        }

        public ResourceLocation getResourceLocation() {
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
                ResolvedModel resolvedmodel = baker.getModel(model.getResourceLocation());
                TextureSlots textureslots = resolvedmodel.getTopTextureSlots();
                return resolvedModel.bakeTopGeometry(textureslots, baker, BlockModelRotation.X0_Y0);
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
