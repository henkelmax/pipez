package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.ItemFilter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.items.ModItems;
import de.maxhenkel.pipez.net.*;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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

    private Button addFilterButton;
    private Button editFilterButton;
    private Button removeFilterButton;

    private HoverArea redstoneArea;
    private HoverArea sortArea;
    private HoverArea filterArea;

    private FilterList filterList;

    public ExtractScreen(ExtractContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(BACKGROUND, container, playerInventory, title);
        xSize = 176;
        ySize = 196;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        buttons.clear();
        children.clear();

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
        addFilterButton = new Button(guiLeft + 31, guiTop + 79, 40, 20, new TranslationTextComponent("message.pipez.filter.add"), button -> {
            ItemFilter test = new ItemFilter();
            test.setElement(ModItems.ADVANCED_UPGRADE);
            CompoundNBT compoundNBT = new CompoundNBT();
            compoundNBT.putBoolean("Test", true);
            test.setMetadata(compoundNBT);
            test.setInvert(true);
            test.setDestination(new DirectionalPosition(new BlockPos(153, 4, -182), Direction.WEST));
            Main.SIMPLE_CHANNEL.sendToServer(new AddFilterMessage(test));
        });
        editFilterButton = new Button(guiLeft + 80, guiTop + 79, 40, 20, new TranslationTextComponent("message.pipez.filter.edit"), button -> {

        });
        removeFilterButton = new Button(guiLeft + 129, guiTop + 79, 40, 20, new TranslationTextComponent("message.pipez.filter.remove"), button -> {
            if (filterList.getSelected() >= 0) {
                Main.SIMPLE_CHANNEL.sendToServer(new RemoveFilterMessage(pipe.getFilters(side).get(filterList.getSelected()).getId()));
            }
        });

        addButton(redstoneButton);
        addButton(sortButton);
        addButton(filterButton);
        addButton(addFilterButton);
        addButton(editFilterButton);
        addButton(removeFilterButton);

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

        filterList = new FilterList(this, 32, 8, 136, 66, () -> pipe.getFilters(side));

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
            filterButton.active = true;
            addFilterButton.active = true;
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
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (filterList.mouseClicked(mouseX, mouseY, button)) {
            return true;
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
