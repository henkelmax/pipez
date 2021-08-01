package de.maxhenkel.pipez.gui;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.gui.containerfactory.FilterContainerFactory;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerFactory;
import de.maxhenkel.corelib.ClientRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;

public class Containers {

    public static MenuType<ExtractContainer> EXTRACT;
    public static MenuType<FilterContainer> FILTER;

    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        ClientRegistry.registerScreen(EXTRACT, ExtractScreen::new);
        ClientRegistry.registerScreen(FILTER, FilterScreen::new);
    }

    public static void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
        EXTRACT = new MenuType<>(new PipeContainerFactory<>(ExtractContainer::new));
        EXTRACT.setRegistryName(new ResourceLocation(Main.MODID, "extract"));
        event.getRegistry().register(EXTRACT);

        FILTER = new MenuType<>(new FilterContainerFactory<>(FilterContainer::new));
        FILTER.setRegistryName(new ResourceLocation(Main.MODID, "filter"));
        event.getRegistry().register(FILTER);
    }

}
