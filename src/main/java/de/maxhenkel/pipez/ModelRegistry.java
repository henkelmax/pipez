package de.maxhenkel.pipez;

import de.maxhenkel.corelib.CachedValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;

public class ModelRegistry {

    public enum Model {
        ENERGY_PIPE_EXTRACT("block/energy_pipe_extract"),
        FLUID_PIPE_EXTRACT("block/fluid_pipe_extract"),
        GAS_PIPE_EXTRACT("block/gas_pipe_extract"),
        ITEM_PIPE_EXTRACT("block/item_pipe_extract"),
        UNIVERSAL_PIPE_EXTRACT("block/universal_pipe_extract");

        private final ModelResourceLocation resource;
        private final CachedValue<BakedModel> cachedModel;

        Model(String name) {
            resource = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Main.MODID, name));
            cachedModel = new CachedValue<>(() -> Minecraft.getInstance().getModelManager().getModel(resource));
        }

        public ModelResourceLocation getResourceLocation() {
            return resource;
        }

        public CachedValue<BakedModel> getCachedModel() {
            return cachedModel;
        }
    }

    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        for (Model model : Model.values()) {
            event.register(model.getResourceLocation());
        }
    }

    public static void onModelBake(ModelEvent.BakingCompleted event) {
        for (Model model : Model.values()) {
            model.getCachedModel().invalidate();
        }
    }

}
