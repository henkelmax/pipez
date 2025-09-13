package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.pipez.*;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.gui.sprite.ExtractElementsSprite;
import de.maxhenkel.pipez.gui.sprite.ExtractUISprite;
import de.maxhenkel.pipez.gui.sprite.SpriteRect;
import de.maxhenkel.pipez.net.*;
import de.maxhenkel.pipez.utils.GasUtils;
import de.maxhenkel.pipez.utils.NbtUtils;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.*;
import java.util.function.Supplier;

public class ExtractScreen extends ScreenBase<ExtractContainer> {
    protected enum SORT_FILTER_LIST_TYPE {
        NAME_ASC, NAME_DESC, DISTANCE_ASC, DISTANCE_DESC;
    }

    private CycleIconButton redstoneButton;
    private CycleIconButton distributionButton;
    private CycleIconButton filterButton;
    private CycleIconButton sortFilterButton;

    private IconButton addFilterButton;
    private IconButton editFilterButton;
    private IconButton removeFilterButton;

    private HoverArea redstoneArea;
    private HoverArea distributionArea;
    private HoverArea filterArea;
    private HoverArea sortFilterArea;
    private HoverArea addFilterArea;
    private HoverArea editFilterArea;
    private HoverArea deleteFilterArea;

    private HoverArea[] tabs;
    private PipeType<?, ?>[] pipeTypes;
    private int currentindex;
    private int currentSortFilterListType;

    private static int currentSortFilterListTypeLast = 0;

    private FilterList filterList;

    public ExtractScreen(ExtractContainer container, Inventory playerInventory, Component title) {
        super(ExtractUISprite.IMAGE, container, playerInventory, title);
        this.imageWidth = ExtractUISprite.SCREEN.w;
        this.imageHeight = ExtractUISprite.SCREEN.h;

        this.pipeTypes = container.getPipe().getPipeTypes();
        if (this.pipeTypes.length > 1) {
            this.tabs = new HoverArea[pipeTypes.length];
        }
        this.currentindex = container.getIndex();
        if (this.currentindex < 0) {
            this.currentindex = getMenu().getPipe().getPreferredPipeIndex(getMenu().getSide());
        }

        this.currentSortFilterListType = ExtractScreen.currentSortFilterListTypeLast;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        clearWidgets();

        PipeLogicTileEntity pipe = getMenu().getPipe();
        Direction side = getMenu().getSide();

        filterList = new FilterList(this, ExtractUISprite.FILTER_LIST, ExtractUISprite.ROW_HEIGHT, ExtractUISprite.VISIBLE_ROW_COUNT, this::getSortedList);

        // redstone button
        Supplier<Integer> redstoneModeIndex = () -> pipe.getRedstoneMode(getMenu().getSide(), pipeTypes[currentindex]).ordinal();
        List<IconButton.Icon> redstoneModeIcons = Arrays.asList(
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.REDSTONE_MODE_ICON_IGNORE),
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.REDSTONE_MODE_ICON_OFF_WHEN_POWERED),
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.REDSTONE_MODE_ICON_ON_WHEN_POWERED),
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.REDSTONE_MODE_ICON_ALWAYS_OFF)
        );
        redstoneButton = new CycleIconButton(this.leftPos + ExtractElementsSprite.REDSTONE_BUTTON.x, this.topPos + ExtractElementsSprite.REDSTONE_BUTTON.y, redstoneModeIcons, redstoneModeIndex, button -> ClientPacketDistributor.sendToServer(new CycleRedstoneModeMessage(currentindex)));

        // distribution button
        Supplier<Integer> distributionIndex = () -> pipe.getDistribution(getMenu().getSide(), pipeTypes[currentindex]).ordinal();
        List<IconButton.Icon> distributionIcons = Arrays.asList(
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.DISTRIBUTION_MODE_ICON_NEAREST),
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.DISTRIBUTION_MODE_ICON_FURTHEST),
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.DISTRIBUTION_MODE_ICON_ROUND_ROBIN),
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.DISTRIBUTION_MODE_ICON_RANDOM)
        );
        distributionButton = new CycleIconButton(this.leftPos + ExtractElementsSprite.DISTRIBUTION_BUTTON.x, this.topPos + ExtractElementsSprite.DISTRIBUTION_BUTTON.y, distributionIcons, distributionIndex, button -> ClientPacketDistributor.sendToServer(new CycleDistributionMessage(currentindex)));

        // filter mode button
        Supplier<Integer> filterModeIndex = () -> pipeTypes[currentindex].hasFilter() ? pipe.getFilterMode(getMenu().getSide(), pipeTypes[currentindex]).ordinal() : 0;
        List<IconButton.Icon> filterModeIcons = Arrays.asList(
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.FILTER_MODE_ICON_WHITELIST),
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.FILTER_MODE_ICON_BLACKLIST)
        );
        filterButton = new CycleIconButton(this.leftPos + ExtractElementsSprite.FILTER_MODE_BUTTON.x, this.topPos + ExtractElementsSprite.FILTER_MODE_BUTTON.y, filterModeIcons, filterModeIndex, button -> ClientPacketDistributor.sendToServer(new CycleFilterModeMessage(currentindex)));

        // sort filter list type
        Supplier<Integer> currentSortFilterListTypeIndex = () -> this.currentSortFilterListType;
        List<IconButton.Icon> sortFilterIcons = Arrays.asList(
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.SORT_ICON_NAME_ASC),
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.SORT_ICON_NAME_DESC),
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.SORT_ICON_DISTANCE_ASC),
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.SORT_ICON_DISTANCE_DESC)
        );
        sortFilterButton = new CycleIconButton(
                this.leftPos + ExtractElementsSprite.SORT_FILTER_LIST_BUTTON.x,
                this.topPos + ExtractElementsSprite.SORT_FILTER_LIST_BUTTON.y,
                sortFilterIcons,
                currentSortFilterListTypeIndex,
                button -> {
                    this.currentSortFilterListType = this.currentSortFilterListType + 1 >= sortFilterIcons.size() ? 0 : this.currentSortFilterListType + 1;
                    ExtractScreen.currentSortFilterListTypeLast = this.currentSortFilterListType;
                }
        );

        // Filter Entry Action Button
        addFilterButton = new IconButton(
                this.leftPos + ExtractElementsSprite.FILTER_ENTRY_BUTTON_ADD.x, this.topPos + ExtractElementsSprite.FILTER_ENTRY_BUTTON_ADD.y,
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.FILTER_ENTRY_ICON_ADD),
                button -> ClientPacketDistributor.sendToServer(new EditFilterMessage(pipeTypes[currentindex].createFilter(), currentindex))
        );
        editFilterButton = new IconButton(
                this.leftPos + ExtractElementsSprite.FILTER_ENTRY_BUTTON_EDIT.x, this.topPos + ExtractElementsSprite.FILTER_ENTRY_BUTTON_EDIT.y,
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.FILTER_ENTRY_ICON_EDIT),
                button -> {
                    if (filterList.getSelected() >= 0) {
                        ClientPacketDistributor.sendToServer(new EditFilterMessage(pipe.getFilters(side, pipeTypes[currentindex]).get(filterList.getSelected()), currentindex));
                    }
                }
        );
        removeFilterButton = new IconButton(
                this.leftPos + ExtractElementsSprite.FILTER_ENTRY_BUTTON_DELETE.x, this.topPos + ExtractElementsSprite.FILTER_ENTRY_BUTTON_DELETE.y,
                new IconButton.Icon(ExtractElementsSprite.IMAGE, ExtractElementsSprite.FILTER_ENTRY_ICON_DELETE),
                button -> {
                    if (filterList.getSelected() >= 0) {
                        ClientPacketDistributor.sendToServer(new RemoveFilterMessage(pipe.getFilters(side, pipeTypes[currentindex]).get(filterList.getSelected()).getId(), currentindex));
                    }
                }
        );

        // render all buttons
        addRenderableWidget(redstoneButton);
        addRenderableWidget(distributionButton);
        addRenderableWidget(filterButton);
        addRenderableWidget(sortFilterButton);
        addRenderableWidget(addFilterButton);
        addRenderableWidget(editFilterButton);
        addRenderableWidget(removeFilterButton);

        // adding hover information for tabs, only available if more then one pipe type is used
        if (hasTabs()) {
            for (int i = 0; i < pipeTypes.length; i++) {
                int tabIndex = i;
                tabs[i] = new HoverArea(ExtractElementsSprite.TAB_BUTTON.x, ExtractElementsSprite.TAB_BUTTON.y + (ExtractElementsSprite.TAB_BUTTON.h + ExtractElementsSprite.TAB_BUTTON_MARGIN) * i, ExtractElementsSprite.TAB_BUTTON.w, ExtractElementsSprite.TAB_BUTTON.h, () -> {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.translatable(pipeTypes[tabIndex].getTranslationKey()));
                    return tooltip;
                });
                hoverAreas.add(tabs[i]);
            }
        }

        // define the hover area for tooltips on the buttons
        redstoneArea = new HoverArea(ExtractElementsSprite.REDSTONE_BUTTON.x, ExtractElementsSprite.REDSTONE_BUTTON.y, ExtractElementsSprite.REDSTONE_BUTTON.w, ExtractElementsSprite.REDSTONE_BUTTON.h, () -> {
            if (redstoneButton.active) {
                return Arrays.asList(Component.translatable("tooltip.pipez.redstone_mode", Component.translatable("tooltip.pipez.redstone_mode." + pipe.getRedstoneMode(side, pipeTypes[currentindex]).getName())));
            } else {
                return Collections.emptyList();
            }
        });
        distributionArea = new HoverArea(ExtractElementsSprite.DISTRIBUTION_BUTTON.x, ExtractElementsSprite.DISTRIBUTION_BUTTON.y, ExtractElementsSprite.DISTRIBUTION_BUTTON.w, ExtractElementsSprite.DISTRIBUTION_BUTTON.h, () -> {
            if (distributionButton.active) {
                return Arrays.asList(Component.translatable("tooltip.pipez.distribution", Component.translatable("tooltip.pipez.distribution." + pipe.getDistribution(side, pipeTypes[currentindex]).getName())));
            } else {
                return Collections.emptyList();
            }
        });
        filterArea = new HoverArea(ExtractElementsSprite.FILTER_MODE_BUTTON.x, ExtractElementsSprite.FILTER_MODE_BUTTON.y, ExtractElementsSprite.FILTER_MODE_BUTTON.w, ExtractElementsSprite.FILTER_MODE_BUTTON.h, () -> {
            if (filterButton.active) {
                return Arrays.asList(Component.translatable("tooltip.pipez.filter_mode", Component.translatable("tooltip.pipez.filter_mode." + pipe.getFilterMode(side, pipeTypes[currentindex]).getName())));
            } else {
                return Collections.emptyList();
            }
        });
        sortFilterArea = new HoverArea(ExtractElementsSprite.SORT_FILTER_LIST_BUTTON.x, ExtractElementsSprite.SORT_FILTER_LIST_BUTTON.y, ExtractElementsSprite.SORT_FILTER_LIST_BUTTON.w, ExtractElementsSprite.SORT_FILTER_LIST_BUTTON.h,
                () -> Arrays.asList(Component.translatable("tooltip.pipez.sort_filter_list_mode", Component.translatable("tooltip.pipez.sort_filter_list_mode." + String.valueOf(this.currentSortFilterListType))))
        );
        addFilterArea = new HoverArea(ExtractElementsSprite.FILTER_ENTRY_BUTTON_ADD.x, ExtractElementsSprite.FILTER_ENTRY_BUTTON_ADD.y, ExtractElementsSprite.FILTER_ENTRY_BUTTON_ADD.w, ExtractElementsSprite.FILTER_ENTRY_BUTTON_ADD.h, () -> {
            if (filterButton.active) {
                return Arrays.asList(Component.translatable("message.pipez.filter.add"));
            } else {
                return Collections.emptyList();
            }
        });
        editFilterArea = new HoverArea(ExtractElementsSprite.FILTER_ENTRY_BUTTON_EDIT.x, ExtractElementsSprite.FILTER_ENTRY_BUTTON_EDIT.y, ExtractElementsSprite.FILTER_ENTRY_BUTTON_EDIT.w, ExtractElementsSprite.FILTER_ENTRY_BUTTON_EDIT.h, () -> {
            if (filterButton.active) {
                return Arrays.asList(Component.translatable("message.pipez.filter.edit"));
            } else {
                return Collections.emptyList();
            }
        });
        deleteFilterArea = new HoverArea(ExtractElementsSprite.FILTER_ENTRY_BUTTON_DELETE.x, ExtractElementsSprite.FILTER_ENTRY_BUTTON_DELETE.y, ExtractElementsSprite.FILTER_ENTRY_BUTTON_DELETE.w, ExtractElementsSprite.FILTER_ENTRY_BUTTON_DELETE.h, () -> {
            if (filterButton.active) {
                return Arrays.asList(Component.translatable("message.pipez.filter.remove"));
            } else {
                return Collections.emptyList();
            }
        });

        hoverAreas.add(redstoneArea);
        hoverAreas.add(distributionArea);
        hoverAreas.add(filterArea);
        hoverAreas.add(sortFilterArea);
        hoverAreas.add(addFilterArea);
        hoverAreas.add(editFilterArea);
        hoverAreas.add(deleteFilterArea);

        checkButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        checkButtons();
        filterList.tick();
    }

    private void checkButtons() {
        Upgrade upgrade = getMenu().getPipe().getUpgrade(getMenu().getSide());
        redstoneButton.active = Upgrade.canChangeRedstoneMode(upgrade);
        distributionButton.active = Upgrade.canChangeDistributionMode(upgrade);
        filterButton.active = Upgrade.canChangeFilter(upgrade) && pipeTypes[currentindex].hasFilter();
        addFilterButton.active = filterButton.active;
        editFilterButton.active = Upgrade.canChangeFilter(upgrade) && filterList.getSelected() >= 0;
        removeFilterButton.active = editFilterButton.active;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(font, playerInventoryTitle.getVisualOrderText(), (int) ExtractUISprite.INVENTORY_TITLE.x, (int) ExtractUISprite.INVENTORY_TITLE.y, FONT_COLOR, false);

        filterList.drawGuiContainerForegroundLayer(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        filterList.drawGuiContainerBackgroundLayer(guiGraphics, partialTicks, mouseX, mouseY);

        if (hasTabs()) {
            for (int i = 0; i < pipeTypes.length; i++) {
                SpriteRect tabSpriteRect = ExtractElementsSprite.TAB_INACTIVE;
                if (i == currentindex) {
                    tabSpriteRect = ExtractElementsSprite.TAB_ACTIVE;
                }
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ExtractElementsSprite.IMAGE, this.leftPos + ExtractElementsSprite.TAB_BUTTON.x, this.topPos + ExtractElementsSprite.TAB_BUTTON.y + (ExtractElementsSprite.TAB_BUTTON.h + ExtractElementsSprite.TAB_BUTTON_MARGIN) * i, tabSpriteRect.x, tabSpriteRect.y, tabSpriteRect.w, tabSpriteRect.h, 256, 256);
            }
            for (int i = 0; i < pipeTypes.length; i++) {
                if (i == currentindex) {
                    guiGraphics.renderItem(pipeTypes[i].getIcon(), this.leftPos + ExtractElementsSprite.TAB_BUTTON_ICON_SELECTED.x, this.topPos + ExtractElementsSprite.TAB_BUTTON_ICON_SELECTED.y + (ExtractElementsSprite.TAB_BUTTON.h + ExtractElementsSprite.TAB_BUTTON_MARGIN) * i, 0);
                    guiGraphics.renderItemDecorations(font, pipeTypes[i].getIcon(), this.leftPos + ExtractElementsSprite.TAB_BUTTON_ICON_SELECTED.x, this.topPos + ExtractElementsSprite.TAB_BUTTON_ICON_SELECTED.y + (ExtractElementsSprite.TAB_BUTTON.h + ExtractElementsSprite.TAB_BUTTON_MARGIN) * i);
                } else {
                    guiGraphics.renderItem(pipeTypes[i].getIcon(), this.leftPos + ExtractElementsSprite.TAB_BUTTON_ICON.x, this.topPos + ExtractElementsSprite.TAB_BUTTON_ICON.y + (ExtractElementsSprite.TAB_BUTTON.h + ExtractElementsSprite.TAB_BUTTON_MARGIN) * i, 0);
                    guiGraphics.renderItemDecorations(font, pipeTypes[i].getIcon(), this.leftPos + ExtractElementsSprite.TAB_BUTTON_ICON.x, this.topPos + ExtractElementsSprite.TAB_BUTTON_ICON.y + (ExtractElementsSprite.TAB_BUTTON.h + ExtractElementsSprite.TAB_BUTTON_MARGIN) * i);
                }
            }
        }
    }

    protected List<Filter<?, ?>> getSortedList() {
        PipeLogicTileEntity pipe = getMenu().getPipe();
        List<Filter<?, ?>> f = pipe.getFilters(getMenu().getSide(), pipeTypes[currentindex]);

        int sortDirection = this.currentSortFilterListType == SORT_FILTER_LIST_TYPE.NAME_ASC.ordinal() || this.currentSortFilterListType == SORT_FILTER_LIST_TYPE.DISTANCE_ASC.ordinal() ? 1 : -1;

        Comparator<Filter> comparatorName = (filterA, filterB) -> filterA.getTranslatedName().withStyle(ChatFormatting.RESET).toString().compareTo(filterB.getTranslatedName().toString()) * sortDirection;
        Comparator<Filter> comparatorDistance = (filterA, filterB) -> {
            if (filterA.hasDestination() == false && filterB.hasDestination() == false) {
                return 0;
            }
            if (filterA.hasDestination() == true && filterB.hasDestination() == false) {
                return -1;
            }
            if (filterA.hasDestination() == false && filterB.hasDestination() == true) {
                return 1;
            }

            // if name identical, compare by distance
            //noinspection DataFlowIssue
            int distanceA = filterA.getDistanceTo(pipe.getBlockPos());
            //noinspection DataFlowIssue
            int distanceB = filterB.getDistanceTo(pipe.getBlockPos());
            if (distanceA < distanceB) {
                return -1 * sortDirection;
            }
            if (distanceA > distanceB) {
                return sortDirection;
            }

            //noinspection DataFlowIssue
            return filterA.getDestination().getDirection().getName().compareTo(filterB.getDestination().getDirection().getName()) * sortDirection;
        };

        List<Comparator<Filter>> comparators;
        if (this.currentSortFilterListType == SORT_FILTER_LIST_TYPE.DISTANCE_ASC.ordinal() || this.currentSortFilterListType == SORT_FILTER_LIST_TYPE.DISTANCE_DESC.ordinal()) {
            comparators = Arrays.asList(comparatorDistance, comparatorName);
        } else {
            comparators = Arrays.asList(comparatorName, comparatorDistance);
        }

        f.sort((filterA, filterB) -> {
            int i = 0;
            while (i < comparators.size()) {
                Comparator<Filter> comparator = comparators.get(i);
                int result = comparator.compare(filterA, filterB);

                if (result != 0) {
                    return result;
                }
                i++;
            }

            return 0;
        });

        return f;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        super.render(guiGraphics, x, y, partialTicks);
        drawHoverAreas(guiGraphics, x, y);
    }

    public int getTabsX() {
        return this.leftPos - getTabsWidth();
    }

    public int getTabsY() {
        return this.topPos + 5;
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
                if (currentindex != i && hoverArea.isHovered(this.leftPos, this.topPos, (int) mouseX, (int) mouseY)) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                    currentindex = i;
                    init();
                    return true;
                }
            }
        }

        if (hasShiftDown()) {
            Slot sl = this.getSlotUnderMouse();
            if (sl != null && !(sl instanceof UpgradeSlot)) {
                addQuickFilter(sl.getItem());
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void addQuickFilter(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (!filterButton.active) {
            return;
        }

        Filter<?, ?> filter = pipeTypes[currentindex].createFilter();
        filter.setExactMetadata(true);

        if (filter instanceof ItemFilter) {
            filter.setTag(new SingleElementTag(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getItem()));
            filter.setMetadata(NbtUtils.componentPatchToNbtOptional(stack.getComponentsPatch()).orElse(null));
            ClientPacketDistributor.sendToServer(new UpdateFilterMessage(filter, currentindex));
        } else if (filter instanceof FluidFilter) {
            FluidUtil.getFluidContained(stack).ifPresent(s -> {
                filter.setTag(new SingleElementTag(BuiltInRegistries.FLUID.getKey(s.getFluid()), s.getFluid()));
                filter.setMetadata(NbtUtils.componentPatchToNbtOptional(stack.getComponentsPatch()).orElse(null));
                ClientPacketDistributor.sendToServer(new UpdateFilterMessage(filter, currentindex));
            });
        } else if (filter instanceof GasFilter) {
            ChemicalStack gas = GasUtils.getGasContained(stack);
            if (gas != null) {
                filter.setTag(new SingleElementTag(gas.getChemical().getRegistryName(), gas.getChemical()));
                filter.setMetadata(null);
                ClientPacketDistributor.sendToServer(new UpdateFilterMessage(filter, currentindex));
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (filterList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (filterList.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
            return true;
        }
        return false;
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    public void addHoverArea(HoverArea hoverArea) {
        hoverAreas.add(hoverArea);
    }
}
