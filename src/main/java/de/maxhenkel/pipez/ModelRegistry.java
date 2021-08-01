package de.maxhenkel.pipez;

import de.maxhenkel.corelib.CachedValue;
import de.maxhenkel.pipez.Main;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;

public class ModelRegistry {

    public enum Model {
        ENERGY_PIPE_EXTRACT("block/energy_pipe_extract"),
        FLUID_PIPE_EXTRACT("block/fluid_pipe_extract"),
        GAS_PIPE_EXTRACT("block/gas_pipe_extract"),
        ITEM_PIPE_EXTRACT("block/item_pipe_extract"),
        UNIVERSAL_PIPE_EXTRACT("block/universal_pipe_extract");

        private final ResourceLocation resource;
        private final CachedValue<IBakedModel> cachedModel;

        Model(String name) {
            resource = new ResourceLocation(Main.MODID, name);
            cachedModel = new CachedValue<>(() -> {
                IUnbakedModel modelOrMissing = ModelLoader.instance().getModelOrMissing(resource);
                return modelOrMissing.bake(ModelLoader.instance(), ModelLoader.instance().getSpriteMap()::getSprite, ModelRotation.X0_Y0, resource);
            });
        }

        public ResourceLocation getResourceLocation() {
            return resource;
        }

        public CachedValue<IBakedModel> getCachedModel() {
            return cachedModel;
        }
    }

    public static void onModelRegister(ModelRegistryEvent event) {
        for (Model model : Model.values()) {
            ModelLoader.instance().addSpecialModel(model.getResourceLocation());
        }
    }

    public static void onModelBake(ModelBakeEvent event) {
        for (Model model : Model.values()) {
            model.getCachedModel().invalidate();
        }
    }

}
