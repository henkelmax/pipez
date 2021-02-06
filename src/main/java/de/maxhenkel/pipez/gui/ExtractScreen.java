package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ExtractScreen extends ScreenBase<ExtractContainer> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Main.MODID, "textures/gui/container/extract.png");

    private Button redstoneButton;
    private Button sortButton;
    private Button filterButton;

    public ExtractScreen(ExtractContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(BACKGROUND, container, playerInventory, title);
        xSize = 176;
        ySize = 154;
    }

    @Override
    protected void init() {
        super.init();
        redstoneButton = new Button(guiLeft + 7, guiTop + 37, 50, 20, new TranslationTextComponent("1"), button -> {

        });
        sortButton = new Button(guiLeft + 63, guiTop + 37, 50, 20, new TranslationTextComponent("2"), button -> {

        });
        filterButton = new Button(guiLeft + 119, guiTop + 37, 50, 20, new TranslationTextComponent("3"), button -> {

        });
        addButton(redstoneButton);
        addButton(sortButton);
        addButton(filterButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        drawCentered(matrixStack, title, 6, FONT_COLOR);
        font.func_243248_b(matrixStack, playerInventory.getDisplayName(), 8F, (float) (ySize - 96 + 3), FONT_COLOR);
    }
}
