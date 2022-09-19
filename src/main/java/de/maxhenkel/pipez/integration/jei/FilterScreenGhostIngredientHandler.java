package de.maxhenkel.pipez.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.maxhenkel.pipez.gui.FilterScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

public class FilterScreenGhostIngredientHandler implements IGhostIngredientHandler<FilterScreen> {

    @Override
    public <I> List<Target<I>> getTargets(FilterScreen gui, I ingredient, boolean doStart) {
        if (ingredient instanceof ItemStack stack) {
            List<Target<I>> list = new ArrayList<>();
            list.add(new IGhostIngredientHandler.Target<I>() {

                @Override
                public Rect2i getArea() {
                    return new Rect2i(gui.getGuiLeft() + 8, gui.getGuiTop() + 18, 16, 16);
                }

                @Override
                public void accept(I ingredient) {
                    gui.onInsertStack(stack);
                }
            });
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void onComplete() {

    }

}
