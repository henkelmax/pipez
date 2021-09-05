package de.maxhenkel.pipez.integration.jei;

import de.maxhenkel.pipez.gui.ExtractScreen;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.List;

public class ExtractScreenHandler implements IGuiContainerHandler<ExtractScreen> {

    @Override
    public List<Rect2i> getGuiExtraAreas(ExtractScreen screen) {
        List<Rect2i> areas = new ArrayList<>();
        if (screen.hasTabs()) {
            areas.add(new Rect2i(screen.getTabsX(), screen.getTabsY(), screen.getTabsWidth(), screen.getTabsHeight()));
        }
        return areas;
    }
}
