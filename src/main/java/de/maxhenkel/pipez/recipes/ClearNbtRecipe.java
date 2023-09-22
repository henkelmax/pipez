package de.maxhenkel.pipez.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ClearNbtRecipe extends CustomRecipe {

    private Ingredient ingredient;

    public ClearNbtRecipe(Ingredient ingredient) {
        super(CraftingBookCategory.MISC);
        this.ingredient = ingredient;
    }

    @Nullable
    public ItemStack getIngredient(CraftingContainer inv) {
        ItemStack found = null;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackInSlot = inv.getItem(i);
            if (ingredient.test(stackInSlot)) {
                if (found != null) {
                    return null;
                } else {
                    found = stackInSlot;
                }
            } else if (!stackInSlot.isEmpty()) {
                return null;
            }
        }
        return found;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return getIngredient(inv) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack ingredient = getIngredient(inv);
        if (ingredient == null) {
            return ItemStack.EMPTY;
        } else {
            ItemStack stack = ingredient.copy();
            stack.setTag(null);
            stack.setCount(1);
            return stack;
        }
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CLEAR_NBT.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<ClearNbtRecipe> {

        private final Codec<ClearNbtRecipe> codec;

        public Serializer() {
            codec = RecordCodecBuilder.create((builder) -> builder
                    .group(
                            Ingredient.CODEC_NONEMPTY
                                    .fieldOf("item")
                                    .forGetter((recipe) -> recipe.ingredient)
                    ).apply(builder, ClearNbtRecipe::new));
        }

        @Override
        public Codec<ClearNbtRecipe> codec() {
            return codec;
        }

        @Override
        public @Nullable ClearNbtRecipe fromNetwork(FriendlyByteBuf buffer) {
            return new ClearNbtRecipe(Ingredient.fromNetwork(buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ClearNbtRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
        }
    }
}
