package de.maxhenkel.pipez.integration.jei;

import de.maxhenkel.pipez.gui.ExtractScreen;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collections;
import java.util.List;

public class ExtractScreenHandler implements IGuiContainerHandler<ExtractScreen> {

    @Override
    public List<Rect2i> getGuiExtraAreas(ExtractScreen screen) {
        if (screen.hasTabs()) {
            return Collections.singletonList(new Rect2i(screen.getTabsX() - 10, screen.getTabsY(), screen.getTabsWidth(), screen.getTabsHeight() + 10));
        }
        return Collections.emptyList();
    }
}
