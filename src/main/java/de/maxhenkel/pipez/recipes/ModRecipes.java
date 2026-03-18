package de.maxhenkel.pipez.recipes;

import de.maxhenkel.pipez.PipezMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, PipezMod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CopyComponentsRecipe>> COPY_NBT = RECIPE_REGISTER.register("copy_components", () -> CopyComponentsRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ClearComponentsRecipe>> CLEAR_NBT = RECIPE_REGISTER.register("clear_components", () -> ClearComponentsRecipe.SERIALIZER);

    public static void init(IEventBus eventBus) {
        RECIPE_REGISTER.register(eventBus);
    }

}
