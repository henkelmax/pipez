package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.ClientRegistry;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.gui.containerfactory.FilterContainerFactory;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Containers {

    private static final DeferredRegister<MenuType<?>> MENU_TYPE_REGISTER = DeferredRegister.create(BuiltInRegistries.MENU, Main.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ExtractContainer>> EXTRACT = MENU_TYPE_REGISTER.register("extract", () ->
            IMenuTypeExtension.create(new PipeContainerFactory<>(ExtractContainer::new))
    );
    public static final DeferredHolder<MenuType<?>, MenuType<FilterContainer>> FILTER = MENU_TYPE_REGISTER.register("filter", () ->
            IMenuTypeExtension.create(new FilterContainerFactory<>(FilterContainer::new))
    );

    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        ClientRegistry.registerScreen(EXTRACT.get(), ExtractScreen::new);
        ClientRegistry.registerScreen(FILTER.get(), FilterScreen::new);
    }

    public static void init() {
        MENU_TYPE_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
