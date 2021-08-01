package de.maxhenkel.pipez.recipes;

import com.google.gson.JsonObject;
import de.maxhenkel.corelib.helpers.Pair;
import de.maxhenkel.pipez.Main;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;

public class CopyNbtRecipe extends CustomRecipe {

    private Ingredient ingredient;

    public CopyNbtRecipe(ResourceLocation recipeID, Ingredient ingredient) {
        super(recipeID);
        this.ingredient = ingredient;
    }

    public Pair<ItemStack, List<ItemStack>> getResult(CraftingContainer inv) {
        ItemStack source = null;
        List<ItemStack> toCopy = new ArrayList<>();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            } else if (ingredient.test(stack)) {
                if (source == null && stack.hasTag()) {
                    source = stack;
                } else {
                    toCopy.add(stack);
                }
            } else {
                return new Pair<>(null, new ArrayList<>());
            }
        }
        return new Pair<>(source, toCopy);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        Pair<ItemStack, List<ItemStack>> result = getResult(inv);

        if (result.getKey() == null) {
            return false;
        }

        if (result.getValue().stream().allMatch(stack -> stack.getItem().equals(result.getKey().getItem()))) {
            return result.getKey() != null && result.getValue().size() > 0;
        } else if (result.getValue().size() == 1) {
            return true;
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        Pair<ItemStack, List<ItemStack>> result = getResult(inv);
        if (result.getKey() == null) {
            return ItemStack.EMPTY;
        }

        if (result.getValue().stream().allMatch(stack -> stack.getItem().equals(result.getKey().getItem()))) {
            ItemStack res = result.getKey().copy();
            res.setCount(1 + result.getValue().size());
            return res;
        } else if (result.getValue().size() == 1) {
            ItemStack stack = result.getValue().get(0).copy();
            stack.setCount(1);
            stack.setTag(result.getKey().getTag().copy());
            return stack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        Pair<ItemStack, List<ItemStack>> result = getResult(inv);
        if (result.getKey() == null) {
            return super.getRemainingItems(inv);
        }

        if (result.getValue().stream().allMatch(stack -> stack.getItem().equals(result.getKey().getItem()))) {
            return super.getRemainingItems(inv);
        } else if (result.getValue().size() == 1) {
            NonNullList<ItemStack> res = super.getRemainingItems(inv);
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).equals(result.getKey())) {
                    ItemStack r = result.getKey().copy();
                    r.setCount(1);
                    res.set(i, r);
                    break;
                }
            }
            return res;
        }

        return super.getRemainingItems(inv);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.COPY_NBT;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<CopyNbtRecipe> {
        public static final ResourceLocation NAME = new ResourceLocation(Main.MODID, "copy_nbt");

        @Override
        public CopyNbtRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new CopyNbtRecipe(recipeId, Ingredient.fromJson(json.get("item")));
        }

        @Override
        public CopyNbtRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new CopyNbtRecipe(recipeId, Ingredient.fromNetwork(buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CopyNbtRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
        }
    }
}
