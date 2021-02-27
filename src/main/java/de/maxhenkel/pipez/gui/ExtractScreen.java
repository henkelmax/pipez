package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.net.*;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ExtractScreen extends ScreenBase<ExtractContainer> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Main.MODID, "textures/gui/container/extract.png");

    private CycleIconButton redstoneButton;
    private CycleIconButton sortButton;
    private CycleIconButton filterButton;

    private Button addFilterButton;
    private Button editFilterButton;
    private Button removeFilterButton;

    private HoverArea redstoneArea;
    private HoverArea sortArea;
    private HoverArea filterArea;


    private HoverArea[] tabs;
    private PipeType<?>[] pipeTypes;
    private int currentindex;

    private FilterList filterList;

    public ExtractScreen(ExtractContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(BACKGROUND, container, playerInventory, title);
        xSize = 176;
        ySize = 196;

        pipeTypes = container.getPipe().getPipeTypes();
        if (pipeTypes.length > 1) {
            tabs = new HoverArea[pipeTypes.length];
        }
        currentindex = container.getIndex();
        if (currentindex < 0) {
            currentindex = getContainer().getPipe().getPreferredPipeIndex(getContainer().getSide());
        }
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        buttons.clear();
        children.clear();

        PipeLogicTileEntity pipe = getContainer().getPipe();
        Direction side = getContainer().getSide();

        filterList = new FilterList(this, 32, 8, 136, 66, () -> pipe.getFilters(side, pipeTypes[currentindex]));

        Supplier<Integer> redstoneModeIndex = () -> pipe.getRedstoneMode(getContainer().getSide(), pipeTypes[currentindex]).ordinal();
        List<CycleIconButton.Icon> redstoneModeIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 16), new CycleIconButton.Icon(BACKGROUND, 192, 16), new CycleIconButton.Icon(BACKGROUND, 208, 16), new CycleIconButton.Icon(BACKGROUND, 224, 16));
        redstoneButton = new CycleIconButton(guiLeft + 7, guiTop + 7, redstoneModeIcons, redstoneModeIndex, button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new CycleRedstoneModeMessage(currentindex));
        });
        Supplier<Integer> distributionIndex = () -> pipe.getDistribution(getContainer().getSide(), pipeTypes[currentindex]).ordinal();
        List<CycleIconButton.Icon> distributionIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 0), new CycleIconButton.Icon(BACKGROUND, 192, 0), new CycleIconButton.Icon(BACKGROUND, 208, 0), new CycleIconButton.Icon(BACKGROUND, 224, 0));
        sortButton = new CycleIconButton(guiLeft + 7, guiTop + 31, distributionIcons, distributionIndex, button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new CycleDistributionMessage(currentindex));
        });
        Supplier<Integer> filterModeIndex = () -> pipeTypes[currentindex].hasFilter() ? pipe.getFilterMode(getContainer().getSide(), pipeTypes[currentindex]).ordinal() : 0;
        List<CycleIconButton.Icon> filterModeIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 32), new CycleIconButton.Icon(BACKGROUND, 192, 32));
        filterButton = new CycleIconButton(guiLeft + 7, guiTop + 55, filterModeIcons, filterModeIndex, button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new CycleFilterModeMessage(currentindex));
        });
        addFilterButton = new Button(guiLeft + 31, guiTop + 79, 40, 20, new TranslationTextComponent("message.pipez.filter.add"), button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new EditFilterMessage(pipeTypes[currentindex].createFilter(), currentindex));
        });
        editFilterButton = new Button(guiLeft + 80, guiTop + 79, 40, 20, new TranslationTextComponent("message.pipez.filter.edit"), button -> {
            if (filterList.getSelected() >= 0) {
                Main.SIMPLE_CHANNEL.sendToServer(new EditFilterMessage(pipe.getFilters(side, pipeTypes[currentindex]).get(filterList.getSelected()), currentindex));
            }
        });
        removeFilterButton = new Button(guiLeft + 129, guiTop + 79, 40, 20, new TranslationTextComponent("message.pipez.filter.remove"), button -> {
            if (filterList.getSelected() >= 0) {
                Main.SIMPLE_CHANNEL.sendToServer(new RemoveFilterMessage(pipe.getFilters(side, pipeTypes[currentindex]).get(filterList.getSelected()).getId(), currentindex));
            }
        });

        addButton(redstoneButton);
        addButton(sortButton);
        addButton(filterButton);
        addButton(addFilterButton);
        addButton(editFilterButton);
        addButton(removeFilterButton);

        if (hasTabs()) {
            for (int i = 0; i < pipeTypes.length; i++) {
                int tabIndex = i;
                tabs[i] = new HoverArea(-26 + 3, 5 + 25 * i, 24, 24, () -> {
                    List<IReorderingProcessor> tooltip = new ArrayList<>();
                    tooltip.add(new TranslationTextComponent(pipeTypes[tabIndex].getTranslationKey()).func_241878_f());
                    return tooltip;
                });
                hoverAreas.add(tabs[i]);
            }
        }

        redstoneArea = new HoverArea(7, 7, 20, 20, () -> {
            if (redstoneButton.active) {
                return Arrays.asList(new TranslationTextComponent("tooltip.pipez.redstone_mode", new TranslationTextComponent("tooltip.pipez.redstone_mode." + pipe.getRedstoneMode(side, pipeTypes[currentindex]).getName())).func_241878_f());
            } else {
                return Collections.emptyList();
            }
        });
        sortArea = new HoverArea(7, 31, 20, 20, () -> {
            if (sortButton.active) {
                return Arrays.asList(new TranslationTextComponent("tooltip.pipez.distribution", new TranslationTextComponent("tooltip.pipez.distribution." + pipe.getDistribution(side, pipeTypes[currentindex]).getName())).func_241878_f());
            } else {
                return Collections.emptyList();
            }
        });
        filterArea = new HoverArea(7, 55, 20, 20, () -> {
            if (filterButton.active) {
                return Arrays.asList(new TranslationTextComponent("tooltip.pipez.filter_mode", new TranslationTextComponent("tooltip.pipez.filter_mode." + pipe.getFilterMode(side, pipeTypes[currentindex]).getName())).func_241878_f());
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
        filterList.tick();
    }

    private void checkButtons() {
        Upgrade upgrade = getContainer().getPipe().getUpgrade(getContainer().getSide());
        if (upgrade == null) {
            redstoneButton.active = false;
            sortButton.active = false;
            filterButton.active = false;
            addFilterButton.active = false;
            editFilterButton.active = false;
            removeFilterButton.active = false;
        } else if (upgrade.equals(Upgrade.BASIC)) {
            redstoneButton.active = true;
            sortButton.active = false;
            filterButton.active = false;
            addFilterButton.active = false;
            editFilterButton.active = false;
            removeFilterButton.active = false;
        } else if (upgrade.equals(Upgrade.IMPROVED)) {
            redstoneButton.active = true;
            sortButton.active = true;
            filterButton.active = false;
            addFilterButton.active = false;
            editFilterButton.active = false;
            removeFilterButton.active = false;
        } else {
            redstoneButton.active = true;
            sortButton.active = true;
            filterButton.active = pipeTypes[currentindex].hasFilter();
            addFilterButton.active = pipeTypes[currentindex].hasFilter();
            editFilterButton.active = filterList.getSelected() >= 0;
            removeFilterButton.active = filterList.getSelected() >= 0;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        font.func_243248_b(matrixStack, playerInventory.getDisplayName(), 8F, (float) (ySize - 96 + 3), FONT_COLOR);

        filterList.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        drawHoverAreas(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        filterList.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        if (hasTabs()) {
            for (int i = 0; i < pipeTypes.length; i++) {
                if (i == currentindex) {
                    blit(matrixStack, guiLeft - 26 + 3, guiTop + 5 + 25 * i, 176, 48, 26, 24);
                } else {
                    blit(matrixStack, guiLeft - 26 + 3, guiTop + 5 + 25 * i, 176, 72, 26, 24);
                }
            }
            for (int i = 0; i < pipeTypes.length; i++) {
                if (i == currentindex) {
                    itemRenderer.renderItemAndEffectIntoGUI(minecraft.player, pipeTypes[i].getIcon(), guiLeft - 26 + 3 + 4, guiTop + 5 + 25 * i + 4);
                } else {
                    itemRenderer.renderItemAndEffectIntoGUI(minecraft.player, pipeTypes[i].getIcon(), guiLeft - 26 + 3 + 4 + 2, guiTop + 5 + 25 * i + 4);
                }
            }
        }
    }

    public int getTabsX() {
        return guiLeft - getTabsWidth();
    }

    public int getTabsY() {
        return guiTop + 5;
    }

    public int getTabsHeight() {
        if (hasTabs()) {
            return 25 * tabs.length;
        } else {
            return 0;
        }
    }

    public int getTabsWidth() {
        return 26 - 3;
    }

    public boolean hasTabs() {
        return tabs != null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (filterList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (hasTabs()) {
            for (int i = 0; i < tabs.length; i++) {
                HoverArea hoverArea = tabs[i];
                if (currentindex != i && hoverArea.isHovered(guiLeft, guiTop, (int) mouseX, (int) mouseY)) {
                    minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1F));
                    currentindex = i;
                    init();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (filterList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (filterList.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return false;
    }

    @Override
    public <T extends Widget> T addButton(T button) {
        return super.addButton(button);
    }

    public void addHoverArea(HoverArea hoverArea) {
        hoverAreas.add(hoverArea);
    }
}
