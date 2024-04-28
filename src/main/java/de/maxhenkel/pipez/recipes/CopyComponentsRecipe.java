package de.maxhenkel.pipez.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.maxhenkel.corelib.helpers.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.*;

public class CopyComponentsRecipe extends CustomRecipe {

    private final Ingredient sourceIngredient;
    private final Ingredient targetIngredient;
    private final List<ResourceLocation> components;

    public CopyComponentsRecipe(Ingredient sourceIngredient, Ingredient targetIngredient, List<ResourceLocation> components) {
        super(CraftingBookCategory.MISC);
        this.sourceIngredient = sourceIngredient;
        this.targetIngredient = targetIngredient;
        this.components = components;
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
                if (source == null && hasComponent(stack)) {
                    source = stack;
                    continue;
                }
            }
            if (matchesTarget) {
                if (!hasComponent(stack)) {
                    toCopy.add(stack);
                    continue;
                }
            }
            return new Pair<>(null, new ArrayList<>());
        }
        return new Pair<>(source, toCopy);
    }

    private boolean hasComponent(ItemStack stack) {
        return stack.getComponentsPatch().entrySet().stream().map(Map.Entry::getKey).map(BuiltInRegistries.DATA_COMPONENT_TYPE::getKey).anyMatch(components::contains);
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
    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider provider) {
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
            DataComponentPatch patch = result.getKey().getComponentsPatch();

            for (Map.Entry<DataComponentType<?>, Optional<?>> e : patch.entrySet()) {
                if (e.getValue().isEmpty()) {
                    continue;
                }
                ResourceLocation key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(e.getKey());
                if (key == null) {
                    continue;
                }
                if (!components.contains(key)) {
                    continue;
                }
                stack.set((DataComponentType) e.getKey(), e.getValue().get());
            }
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
        return ModRecipes.COPY_NBT.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<CopyComponentsRecipe> {

        private static final MapCodec<CopyComponentsRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
                .group(
                        Ingredient.CODEC_NONEMPTY
                                .fieldOf("source")
                                .forGetter((recipe) -> recipe.sourceIngredient),
                        Ingredient.CODEC_NONEMPTY
                                .fieldOf("target")
                                .forGetter((recipe) -> recipe.targetIngredient),
                        Codec.list(ResourceLocation.CODEC)
                                .fieldOf("components")
                                .forGetter((recipe) -> recipe.components)
                ).apply(builder, CopyComponentsRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CopyComponentsRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                r -> r.sourceIngredient,
                Ingredient.CONTENTS_STREAM_CODEC,
                r -> r.targetIngredient,
                ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC),
                r -> r.components,
                CopyComponentsRecipe::new
        );

        public Serializer() {

        }

        @Override
        public MapCodec<CopyComponentsRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CopyComponentsRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
