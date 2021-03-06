package de.maxhenkel.pipez.recipes;

import com.google.gson.JsonObject;
import de.maxhenkel.pipez.Main;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class ClearNbtRecipe extends SpecialRecipe {

    private Ingredient ingredient;

    public ClearNbtRecipe(ResourceLocation recipeID, Ingredient ingredient) {
        super(recipeID);
        this.ingredient = ingredient;
    }

    @Nullable
    public ItemStack getIngredient(CraftingInventory inv) {
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
    public boolean matches(CraftingInventory inv, World worldIn) {
        return getIngredient(inv) != null;
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
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
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.CLEAR_NBT;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ClearNbtRecipe> {
        public static final ResourceLocation NAME = new ResourceLocation(Main.MODID, "clear_nbt");

        @Override
        public ClearNbtRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new ClearNbtRecipe(recipeId, Ingredient.fromJson(json.get("item")));
        }

        @Override
        public ClearNbtRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
            return new ClearNbtRecipe(recipeId, Ingredient.fromNetwork(buffer));
        }

        @Override
        public void toNetwork(PacketBuffer buffer, ClearNbtRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
        }
    }
}
