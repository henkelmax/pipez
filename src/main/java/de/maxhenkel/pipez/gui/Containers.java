package de.maxhenkel.pipez.gui;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerFactory;
import de.maxhenkel.corelib.ClientRegistry;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;

public class Containers {

    public static ContainerType<ExtractContainer> EXTRACT;

    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        ClientRegistry.registerScreen(EXTRACT, ExtractScreen::new);
    }

    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        EXTRACT = new ContainerType<>(new PipeContainerFactory<>(ExtractContainer::new));
        EXTRACT.setRegistryName(new ResourceLocation(Main.MODID, "extract"));
        event.getRegistry().register(EXTRACT);
    }

}
