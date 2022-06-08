package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.ClientRegistry;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.gui.containerfactory.FilterContainerFactory;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerFactory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Containers {

    private static final DeferredRegister<MenuType<?>> MENU_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, Main.MODID);

    public static final RegistryObject<MenuType<ExtractContainer>> EXTRACT = MENU_TYPE_REGISTER.register("extract", () ->
            IForgeMenuType.create(new PipeContainerFactory<>(ExtractContainer::new))
    );
    public static final RegistryObject<MenuType<FilterContainer>> FILTER = MENU_TYPE_REGISTER.register("filter", () ->
            IForgeMenuType.create(new FilterContainerFactory<>(FilterContainer::new))
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
