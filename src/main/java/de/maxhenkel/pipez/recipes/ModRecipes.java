package de.maxhenkel.pipez.recipes;

import de.maxhenkel.pipez.Main;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Main.MODID);

    public static final RegistryObject<RecipeSerializer<?>> COPY_NBT = RECIPE_REGISTER.register("copy_nbt", CopyNbtRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<?>> CLEAR_NBT = RECIPE_REGISTER.register("clear_nbt", ClearNbtRecipe.Serializer::new);

    public static void init() {
        RECIPE_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
