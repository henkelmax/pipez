package de.maxhenkel.pipez.recipes;

import de.maxhenkel.pipez.Main;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Main.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> COPY_NBT = RECIPE_REGISTER.register("copy_nbt", CopyNbtRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> CLEAR_NBT = RECIPE_REGISTER.register("clear_nbt", ClearNbtRecipe.Serializer::new);

    public static void init() {
        RECIPE_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
