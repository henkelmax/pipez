package de.maxhenkel.pipez.recipes;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;

public class ModRecipes {

    public static CopyNbtRecipe.Serializer COPY_NBT;
    public static ClearNbtRecipe.Serializer CLEAR_NBT;

    public static void registerRecipes(RegistryEvent.Register<RecipeSerializer<?>> event) {
        COPY_NBT = new CopyNbtRecipe.Serializer();
        COPY_NBT.setRegistryName(CopyNbtRecipe.Serializer.NAME);
        event.getRegistry().register(COPY_NBT);
        CLEAR_NBT = new ClearNbtRecipe.Serializer();
        CLEAR_NBT.setRegistryName(ClearNbtRecipe.Serializer.NAME);
        event.getRegistry().register(CLEAR_NBT);
    }

}
