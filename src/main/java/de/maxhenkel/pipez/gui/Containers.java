package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.ClientRegistry;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.gui.containerfactory.FilterContainerFactory;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerFactory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public class Containers {

    private static final DeferredRegister<MenuType<?>> MENU_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Main.MODID);

    public static final RegistryObject<MenuType<ExtractContainer>> EXTRACT = MENU_TYPE_REGISTER.register("extract", () ->
            IMenuTypeExtension.create(new PipeContainerFactory<>(ExtractContainer::new))
    );
    public static final RegistryObject<MenuType<FilterContainer>> FILTER = MENU_TYPE_REGISTER.register("filter", () ->
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
