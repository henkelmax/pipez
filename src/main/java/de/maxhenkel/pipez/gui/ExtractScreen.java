package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.net.CycleDistributionMessage;
import de.maxhenkel.pipez.net.CycleFilterModeMessage;
import de.maxhenkel.pipez.net.CycleRedstoneModeMessage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ExtractScreen extends ScreenBase<ExtractContainer> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Main.MODID, "textures/gui/container/extract.png");

    private CycleIconButton redstoneButton;
    private CycleIconButton sortButton;
    private CycleIconButton filterButton;

    private HoverArea redstoneArea;
    private HoverArea sortArea;
    private HoverArea filterArea;

    public ExtractScreen(ExtractContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(BACKGROUND, container, playerInventory, title);
        xSize = 176;
        ySize = 196;
    }

    @Override
    protected void init() {
        super.init();
        UpgradeTileEntity pipe = getContainer().getPipe();
        Direction side = getContainer().getSide();
        Supplier<Integer> redstoneModeIndex = () -> pipe.getRedstoneMode(getContainer().getSide()).ordinal();
        List<CycleIconButton.Icon> redstoneModeIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 16), new CycleIconButton.Icon(BACKGROUND, 192, 16), new CycleIconButton.Icon(BACKGROUND, 208, 16));
        redstoneButton = new CycleIconButton(guiLeft + 7, guiTop + 7, redstoneModeIcons, redstoneModeIndex, button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new CycleRedstoneModeMessage());
        });
        Supplier<Integer> distributionIndex = () -> pipe.getDistribution(getContainer().getSide()).ordinal();
        List<CycleIconButton.Icon> distributionIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 0), new CycleIconButton.Icon(BACKGROUND, 192, 0), new CycleIconButton.Icon(BACKGROUND, 208, 0), new CycleIconButton.Icon(BACKGROUND, 224, 0));
        sortButton = new CycleIconButton(guiLeft + 7, guiTop + 31, distributionIcons, distributionIndex, button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new CycleDistributionMessage());
        });
        Supplier<Integer> filterModeIndex = () -> pipe.getFilterMode(getContainer().getSide()).ordinal();
        List<CycleIconButton.Icon> filterModeIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 32), new CycleIconButton.Icon(BACKGROUND, 192, 32));
        filterButton = new CycleIconButton(guiLeft + 7, guiTop + 55, filterModeIcons, filterModeIndex, button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new CycleFilterModeMessage());
        });
        addButton(redstoneButton);
        addButton(sortButton);
        addButton(filterButton);

        hoverAreas.clear();

        redstoneArea = new HoverArea(7, 7, 20, 20, () -> {
            if (redstoneButton.active) {
                return Arrays.asList(new TranslationTextComponent("tooltip.pipez.redstone_mode", new TranslationTextComponent("tooltip.pipez.redstone_mode." + pipe.getRedstoneMode(side).getName())).func_241878_f());
            } else {
                return Collections.emptyList();
            }
        });
        sortArea = new HoverArea(7, 31, 20, 20, () -> {
            if (sortButton.active) {
                return Arrays.asList(new TranslationTextComponent("tooltip.pipez.distribution", new TranslationTextComponent("tooltip.pipez.distribution." + pipe.getDistribution(side).getName())).func_241878_f());
            } else {
                return Collections.emptyList();
            }
        });
        filterArea = new HoverArea(7, 55, 20, 20, () -> {
            if (filterButton.active) {
                return Arrays.asList(new TranslationTextComponent("tooltip.pipez.filter_mode", new TranslationTextComponent("tooltip.pipez.filter_mode." + pipe.getFilterMode(side).getName())).func_241878_f());
            } else {
                return Collections.emptyList();
            }
        });
        hoverAreas.add(redstoneArea);
        hoverAreas.add(sortArea);
        hoverAreas.add(filterArea);

        checkButtons();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        Upgrade upgrade = getContainer().getPipe().getUpgrade(getContainer().getSide());
        if (upgrade == null) {
            redstoneButton.active = false;
            sortButton.active = false;
            filterButton.active = false;
        } else if (upgrade.equals(Upgrade.BASIC)) {
            redstoneButton.active = true;
            sortButton.active = false;
            filterButton.active = false;
        } else if (upgrade.equals(Upgrade.IMPROVED)) {
            redstoneButton.active = true;
            sortButton.active = true;
            filterButton.active = false;
        } else {
            redstoneButton.active = true;
            sortButton.active = true;
            filterButton.active = true;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        font.func_243248_b(matrixStack, playerInventory.getDisplayName(), 8F, (float) (ySize - 96 + 3), FONT_COLOR);

        drawHoverAreas(matrixStack, mouseX, mouseY);
    }
}
