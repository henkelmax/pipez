package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

public abstract class WidgetBase {

    protected ExtractScreen screen;
    protected Minecraft mc;
    protected int posX, posY, width, height, guiLeft, guiTop, xSize, ySize;

    public WidgetBase(ExtractScreen screen, int posX, int posY, int xSize, int ySize) {
        this.screen = screen;
        mc = Minecraft.getInstance();
        this.posX = posX;
        this.posY = posY;
        this.xSize = xSize;
        this.ySize = ySize;

        width = screen.width;
        height = screen.height;
        guiLeft = screen.getGuiLeft() + posX;
        guiTop = screen.getGuiTop() + posY;
    }

    public void tick() {

    }

    protected void drawGuiContainerForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    protected void drawGuiContainerBackgroundLayer(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }

    protected void addButton(Button widget) {
        screen.addRenderableWidget(widget);
    }

    public ExtractContainer getContainer() {
        return screen.getMenu();
    }

    public void addHoverArea(ScreenBase.HoverArea hoverArea) {
        screen.addHoverArea(hoverArea);
    }

}
