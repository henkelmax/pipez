package de.maxhenkel.pipez.tagproviders;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public class ModTagProviders {

    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        if (event.includeServer() || event.includeClient()) {
            gen.addProvider(new WrenchTagProvider(gen, existingFileHelper));
            gen.addProvider(new UpgradeTagProvider(gen, existingFileHelper));
        }
    }

}
