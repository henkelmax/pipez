package de.maxhenkel.pipez.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClearComponentsRecipe extends CustomRecipe {

    private final Ingredient ingredient;
    private final List<ResourceLocation> components;

    public ClearComponentsRecipe(Ingredient ingredient, List<ResourceLocation> components) {
        super(CraftingBookCategory.MISC);
        this.ingredient = ingredient;
        this.components = components;
    }

    @Nullable
    public ItemStack getIngredient(CraftingInput inv) {
        ItemStack found = null;
        for (int i = 0; i < inv.size(); i++) {
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
    public boolean matches(CraftingInput inv, Level worldIn) {
        return getIngredient(inv) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack ingredient = getIngredient(inv);
        if (ingredient == null) {
            return ItemStack.EMPTY;
        } else {
            ItemStack stack = ingredient.copy();
            for (Map.Entry<DataComponentType<?>, Optional<?>> e : stack.getComponentsPatch().entrySet()) {
                ResourceLocation key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(e.getKey());
                if (key == null) {
                    continue;
                }
                if (!components.contains(key)) {
                    continue;
                }
                stack.remove(e.getKey());
            }

            stack.setCount(1);
            return stack;
        }
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipes.CLEAR_NBT.get();
    }

    public static class Serializer implements RecipeSerializer<ClearComponentsRecipe> {

        private static final MapCodec<ClearComponentsRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
                .group(
                        Ingredient.CODEC
                                .fieldOf("item")
                                .forGetter((recipe) -> recipe.ingredient),
                        Codec.list(ResourceLocation.CODEC)
                                .fieldOf("components")
                                .forGetter((recipe) -> recipe.components)
                ).apply(builder, ClearComponentsRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ClearComponentsRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                r -> r.ingredient,
                ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC),
                r -> r.components,
                ClearComponentsRecipe::new
        );

        public Serializer() {

        }

        @Override
        public MapCodec<ClearComponentsRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ClearComponentsRecipe> streamCodec() {
            return STREAM_CODEC;
        }

    }
}
