package de.maxhenkel.pipez;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.concurrent.atomic.AtomicReference;

public class ModelRegistry {

    public enum Model {
        ENERGY_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/energy_pipe_extract")),
        FLUID_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/fluid_pipe_extract")),
        GAS_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/gas_pipe_extract")),
        ITEM_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/item_pipe_extract")),
        UNIVERSAL_PIPE_EXTRACT(ResourceLocation.fromNamespaceAndPath(Main.MODID, "block/universal_pipe_extract"));

        private final ResourceLocation resource;
        private final AtomicReference<BakedModel> model;

        Model(ResourceLocation rl) {
            resource = rl;
            model = new AtomicReference<>();
        }

        public ResourceLocation getResourceLocation() {
            return resource;
        }

        public AtomicReference<BakedModel> getModel() {
            return model;
        }
    }

    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        for (Model model : Model.values()) {
            event.register(model.getResourceLocation());
        }
    }

    public static void onModelBake(ModelEvent.BakingCompleted event) {
        for (Model model : Model.values()) {
            model.getModel().set(event.getBakingResult().standaloneModels().get(model.getResourceLocation()));
        }
    }
}
