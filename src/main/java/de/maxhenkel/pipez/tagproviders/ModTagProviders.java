package de.maxhenkel.pipez.tagproviders;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public class ModTagProviders {

    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        if (event.includeServer() || event.includeClient()) {
            gen.addProvider(false, new WrenchTagProvider(gen, existingFileHelper));
            gen.addProvider(false, new UpgradeTagProvider(gen, existingFileHelper));
        }
    }

}
