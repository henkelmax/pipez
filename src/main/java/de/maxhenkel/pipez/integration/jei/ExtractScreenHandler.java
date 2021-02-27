package de.maxhenkel.pipez.integration.jei;

import de.maxhenkel.pipez.gui.ExtractScreen;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rectangle2d;

import java.util.ArrayList;
import java.util.List;

public class ExtractScreenHandler implements IGuiContainerHandler<ExtractScreen> {

    @Override
    public List<Rectangle2d> getGuiExtraAreas(ExtractScreen screen) {
        List<Rectangle2d> areas = new ArrayList<>();
        if (screen.hasTabs()) {
            areas.add(new Rectangle2d(screen.getTabsX(), screen.getTabsY(), screen.getTabsWidth(), screen.getTabsHeight()));
        }
        return areas;
    }
}
