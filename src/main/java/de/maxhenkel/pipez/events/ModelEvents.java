package de.maxhenkel.pipez.events;

import de.maxhenkel.pipez.Main;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;

public class ModelEvents {

    private static final String[] MODELS = new String[] {
        "block/energy_pipe_extract",
        "block/fluid_pipe_extract",
        "block/gas_pipe_extract",
        "block/item_pipe_extract",
        "block/universal_pipe_extract",
    };

    public static void onModelRegister(ModelRegistryEvent event) {
        for (String model : MODELS) {
            ModelLoader.instance().addSpecialModel(new ResourceLocation(Main.MODID, model));
        }
    }

}
