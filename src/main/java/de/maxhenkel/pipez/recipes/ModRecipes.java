package de.maxhenkel.pipez.recipes;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;

public class ModRecipes {

    public static CopyNbtRecipe.Serializer COPY_NBT;

    public static void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        COPY_NBT = new CopyNbtRecipe.Serializer();
        COPY_NBT.setRegistryName(CopyNbtRecipe.Serializer.NAME);
        event.getRegistry().register(COPY_NBT);
    }

}
