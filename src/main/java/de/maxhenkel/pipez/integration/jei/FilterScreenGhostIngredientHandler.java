package de.maxhenkel.pipez.integration.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.FluidFilter;
import de.maxhenkel.pipez.GasFilter;
import de.maxhenkel.pipez.gui.FilterScreen;
import de.maxhenkel.pipez.utils.GasUtils;
import mekanism.api.chemical.ChemicalStack;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class FilterScreenGhostIngredientHandler implements IGhostIngredientHandler<FilterScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(FilterScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
        Filter<?, ?> filter = gui.getMenu().getFilter();
        List<Target<I>> list = new ArrayList<>();

        if (ingredient.getIngredient() instanceof ItemStack stack) {
            if (testItemStack(filter, stack)) {
                list.add(this.getItemHoverAreaTarget(gui, i -> gui.onInsertStack(stack)));
            }
        } else if (filter instanceof FluidFilter && ingredient.getIngredient() instanceof FluidStack stack) {
            list.add(this.getItemHoverAreaTarget(gui, i -> gui.onInsertStack(stack)));
        } else if (filter instanceof GasFilter && ingredient.getIngredient() instanceof ChemicalStack stack) {
            list.add(this.getItemHoverAreaTarget(gui, i -> gui.onInsertStack(stack)));
        }

        return list;
    }

    private boolean testItemStack(Filter<?, ?> filter, ItemStack stack) {
        if (filter instanceof FluidFilter) {
            return FluidUtil.getFluidContained(stack).isPresent();
        } else if (filter instanceof GasFilter) {
            return GasUtils.getGasContained(stack) != null;
        } else {
            return true;
        }
    }

    private <I> Target<I> getItemHoverAreaTarget(FilterScreen gui, Consumer<I> consumer) {
        return new IGhostIngredientHandler.Target<I>() {

            @Override
            public Rect2i getArea() {
                return new Rect2i(gui.getGuiLeft() + 8, gui.getGuiTop() + 18, 16, 16);
            }

            @Override
            public void accept(I ingredient) {
                consumer.accept(ingredient);
            }
        };
    }

    @Override
    public void onComplete() {

    }

}
