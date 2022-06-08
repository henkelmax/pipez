package de.maxhenkel.pipez.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.maxhenkel.corelib.helpers.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CopyNbtRecipe extends CustomRecipe {

    private final Ingredient sourceIngredient;
    private final Ingredient targetIngredient;
    private final List<String> tags;

    public CopyNbtRecipe(ResourceLocation recipeID, Ingredient sourceIngredient, Ingredient targetIngredient, List<String> tags) {
        super(recipeID);
        this.sourceIngredient = sourceIngredient;
        this.targetIngredient = targetIngredient;
        this.tags = tags;
    }

    public Pair<ItemStack, List<ItemStack>> getResult(CraftingContainer inv) {
        ItemStack source = null;
        List<ItemStack> toCopy = new ArrayList<>();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            boolean matchesSource = sourceIngredient.test(stack);
            boolean matchesTarget = targetIngredient.test(stack);

            if (!matchesSource && !matchesTarget) {
                return new Pair<>(null, new ArrayList<>());
            }

            if (matchesSource) {
                if (source == null && stack.hasTag()) {
                    source = stack;
                    continue;
                }
            }
            if (matchesTarget) {
                if (!stack.hasTag()) {
                    toCopy.add(stack);
                    continue;
                }
            }
            return new Pair<>(null, new ArrayList<>());
        }
        return new Pair<>(source, toCopy);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        Pair<ItemStack, List<ItemStack>> result = getResult(inv);

        if (result.getKey() == null || result.getValue().isEmpty()) {
            return false;
        }

        return result.getValue().size() == 1 || result.getValue().stream().allMatch(stack -> stack.getItem().equals(result.getKey().getItem()));
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
            CompoundTag tag = result.getKey().getTag();
            CompoundTag copy = new CompoundTag();
            for (String s : tags) {
                Tag element = getPath(s, tag);
                if (element != null) {
                    setPath(s, copy, element);
                }
            }
            stack.setTag(copy);
            return stack;
        }

        return ItemStack.EMPTY;
    }

    @Nullable
    private Tag getPath(String path, CompoundTag tag) {
        String[] p = path.split("\\.");
        CompoundTag c = tag;
        for (int i = 0; i < p.length - 1; i++) {
            if (c.contains(p[i], Tag.TAG_COMPOUND)) {
                c = c.getCompound(p[i]);
            } else {
                return null;
            }
        }
        return c.get(p[p.length - 1]);
    }

    private void setPath(String path, CompoundTag tag, Tag element) {
        String[] p = path.split("\\.");
        CompoundTag c = tag;
        for (int i = 0; i < p.length - 1; i++) {
            if (c.contains(p[i], Tag.TAG_COMPOUND)) {
                c = c.getCompound(p[i]);
            } else {
                CompoundTag newTag = new CompoundTag();
                c.put(p[i], newTag);
                c = newTag;
            }
        }
        c.put(p[p.length - 1], element);
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
        return ModRecipes.COPY_NBT.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<CopyNbtRecipe> {

        @Override
        public CopyNbtRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            List<String> tags = new ArrayList<>();
            if (json.has("tags")) {
                JsonArray tagArray = json.get("tags").getAsJsonArray();
                for (int i = 0; i < tagArray.size(); i++) {
                    tags.add(tagArray.get(i).getAsString());
                }
            }
            return new CopyNbtRecipe(recipeId, Ingredient.fromJson(json.get("source")), Ingredient.fromJson(json.get("target")), tags);
        }

        @Override
        public CopyNbtRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ArrayList<String> tags = new ArrayList<>();
            int tagSize = buffer.readVarInt();
            for (int i = 0; i < tagSize; i++) {
                tags.add(buffer.readUtf());
            }
            return new CopyNbtRecipe(recipeId, Ingredient.fromNetwork(buffer), Ingredient.fromNetwork(buffer), tags);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CopyNbtRecipe recipe) {
            buffer.writeVarInt(recipe.tags.size());
            for (String s : recipe.tags) {
                buffer.writeUtf(s);
            }
            recipe.sourceIngredient.toNetwork(buffer);
            recipe.targetIngredient.toNetwork(buffer);
        }
    }
}
