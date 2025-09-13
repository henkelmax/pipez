package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.FontColorUtils;
import de.maxhenkel.corelib.helpers.AbstractStack;
import de.maxhenkel.corelib.helpers.Pair;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.gui.sprite.ExtractElementsSprite;
import de.maxhenkel.pipez.gui.sprite.ExtractUISprite;
import de.maxhenkel.pipez.gui.sprite.SpriteRect;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterList extends WidgetBase {
    protected Supplier<List<Filter<?, ?>>> filters;
    protected int offset;
    protected int selected;
    private ScreenBase.HoverArea[] hoverAreas;
    private ScreenBase.HoverArea[] itemHoverAreas;
    private ScreenBase.HoverArea[] blockHoverAreas;
    private int rowHeight;
    private int rowCount;
    private CachedMap<DirectionalPosition, Pair<BlockState, ItemStack>> filterPosCache;

    private static int offsetLast = 0;
    private static int selectedLast = -1;

    public FilterList(ExtractScreen screen, SpriteRect rect, int rowHeight, int rowCount, Supplier<List<Filter<?, ?>>> filters) {
        super(screen, rect.x, rect.y, rect.w, rect.h);

        this.rowHeight = rowHeight;
        this.rowCount = rowCount;
        this.filters = filters;

        this.offset = FilterList.offsetLast;
        this.selected = FilterList.selectedLast;

        // TODO
        hoverAreas = new ScreenBase.HoverArea[this.rowCount];
        itemHoverAreas = new ScreenBase.HoverArea[this.rowCount];
        blockHoverAreas = new ScreenBase.HoverArea[this.rowCount];
        for (int i = 0; i < hoverAreas.length; i++) {
            hoverAreas[i] = new ScreenBase.HoverArea(0, i * this.rowHeight, rect.w, this.rowHeight);
            itemHoverAreas[i] = new ScreenBase.HoverArea(3, 3 + i * this.rowHeight, 16, 16);
            blockHoverAreas[i] = new ScreenBase.HoverArea(rect.w - 3 - 16 - 11, 3 + i * this.rowHeight, 16, 16);
        }

        filterPosCache = new CachedMap<>(1000);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiGraphics, mouseX, mouseY);
        List<Filter<?, ?>> f = filters.get();

        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= f.size()) {
                break;
            }
            Filter<?, ?> filter = f.get(getOffset() + i);
            if (itemHoverAreas[i].isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                AbstractStack<?> stack = filter.getStack();
                if (stack != null && !stack.isEmpty()) {
                    List<Component> tooltip = stack.getTooltip();
                    if (filter.isInvert()) {
                        tooltip.set(0, Component.translatable("tooltip.pipez.filter.not").withStyle(ChatFormatting.DARK_RED).append(" ").append(tooltip.get(0)));
                    }
                    if (filter.getTag() != null && !(filter.getTag() instanceof SingleElementTag)) {
                        tooltip.add(Component.translatable("tooltip.pipez.filter.accepts_tag", Component.literal(filter.getTag().getName().toString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
                    }
                    guiGraphics.setTooltipForNextFrame(mc.font, tooltip.stream().map(Component::getVisualOrderText).collect(Collectors.toList()), mouseX, mouseY);
                }
            } else if (blockHoverAreas[i].isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                if (filter.hasDestination()) {
                    List<Component> tooltip = new ArrayList<>();
                    Pair<BlockState, ItemStack> destPair = getBlockAt(filter.getDestination());
                    if (destPair.getKey() == null) {
                        tooltip.add(net.minecraft.network.chat.ComponentUtils.wrapInSquareBrackets(Component.translatable("tooltip.pipez.filter.unknown_block")).withStyle(ChatFormatting.DARK_RED));
                    } else {
                        tooltip.add(destPair.getKey().getBlock().getName().withStyle(ChatFormatting.BLUE));
                    }
                    BlockPos pos = filter.getDestination().getPos();
                    tooltip.add(Component.translatable("tooltip.pipez.filter.destination_location", number(pos.getX()), number(pos.getY()), number(pos.getZ())));
                    tooltip.add(Component.translatable("tooltip.pipez.filter.destination_distance", number(filter.getDistanceTo(getContainer().getPipe().getBlockPos()))));
                    tooltip.add(Component.translatable("tooltip.pipez.filter.destination_side", Component.translatable("message.pipez.direction." + filter.getDestination().getDirection().getName()).withStyle(ChatFormatting.DARK_GREEN)));
                    guiGraphics.setTooltipForNextFrame(mc.font, tooltip.stream().map(Component::getVisualOrderText).collect(Collectors.toList()), mouseX, mouseY);
                }
            }
        }
    }

    private MutableComponent number(int num) {
        return Component.literal(String.valueOf(num)).withStyle(ChatFormatting.DARK_GREEN);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(guiGraphics, partialTicks, mouseX, mouseY);

        List<Filter<?, ?>> f = filters.get();
        for (int i = getOffset(); i < f.size() && i < getOffset() + this.rowCount; i++) {
            int pos = i - getOffset();
            int startY = guiTop + pos * this.rowHeight;

            SpriteRect entryImageSpriteRect = ExtractElementsSprite.FILTER_LIST_ENTRY;
            if (i == getSelected()) {
                entryImageSpriteRect = ExtractElementsSprite.FILTER_LIST_ENTRY_SELECTED;
            }
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ExtractElementsSprite.IMAGE, guiLeft, startY, entryImageSpriteRect.x, entryImageSpriteRect.y, entryImageSpriteRect.w, entryImageSpriteRect.h, 256, 256);

            Filter<?, ?> filter = f.get(i);
            AbstractStack<?> stack = filter.getStack();
            if (stack != null && !stack.isEmpty()) {
                stack.render(guiGraphics, guiLeft + 3, startY + 3);
                if (filter.getTag() != null) {
                    if (filter.getTag() instanceof SingleElementTag) {
                        drawStringSmall(guiGraphics, guiLeft + 22, startY + 5, Component.translatable("message.pipez.filter.item", Component.translatable(stack.getDisplayName().getString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.WHITE));
                    } else {
                        drawStringSmall(guiGraphics, guiLeft + 22, startY + 5, Component.translatable("message.pipez.filter.tag", Component.literal(filter.getTag().getName().toString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.WHITE));
                    }
                }
            } else {
                drawStringSmall(guiGraphics, guiLeft + 22, startY + 5, Component.translatable("message.pipez.filter.any_item").withStyle(ChatFormatting.WHITE));
            }
            if (filter.getMetadata() != null && filter.getMetadata().size() > 0) {
                MutableComponent tags = Component.translatable("message.pipez.filter.nbt.tag" + (filter.getMetadata().size() != 1 ? "s" : ""), filter.getMetadata().size()).withStyle(ChatFormatting.DARK_PURPLE);
                MutableComponent nbtStr = Component.translatable("message.pipez.filter.nbt", tags).withStyle(ChatFormatting.WHITE);
                if (filter.isExactMetadata()) {
                    nbtStr.append(" ").append(Component.translatable("message.pipez.filter.nbt.exact"));
                }
                drawStringSmall(guiGraphics, guiLeft + 22, startY + 10, nbtStr);
            }

            if (filter.isInvert()) {
                drawStringSmall(guiGraphics, guiLeft + 22, startY + 15, Component.translatable("message.pipez.filter.inverted").withStyle(ChatFormatting.DARK_RED));
            }

            if (filter.hasDestination()) {
                Pair<BlockState, ItemStack> dstPair = getBlockAt(filter.getDestination());
                guiGraphics.renderItem(dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY + 3, 0);

                String distanceText = Component.translatable("message.pipez.filter.destination_distance", number(filter.getDistanceTo(getContainer().getPipe().getBlockPos()))).getString();
                guiGraphics.renderItemDecorations(mc.font, dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY - 7, distanceText);

                String directionText = Component.translatable("message.pipez.direction." + filter.getDestination().getDirection().getName()).getString();
                guiGraphics.renderItemDecorations(mc.font, dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY + 3, String.valueOf(directionText.charAt(0)));
            }
        }

        drawStringSmall(guiGraphics, this.guiLeft + ExtractUISprite.ENTRY_COUNT_TEXT.x, this.guiTop + ExtractUISprite.ENTRY_COUNT_TEXT.y, Component.translatable("message.pipez.filter.entry_count", number(f.size())));

        SpriteRect scrollerImageSpriteRect = ExtractElementsSprite.FILTER_LIST_SCROLLER_INACTIVE;
        int posY = guiTop;
        if (f.size() > this.rowCount) {
            float h = ExtractUISprite.ROW_HEIGHT * ExtractUISprite.VISIBLE_ROW_COUNT - ExtractElementsSprite.FILTER_LIST_SCROLLER_ACTIVE.h;
            float perc = (float) getOffset() / (float) (f.size() - this.rowCount);
            posY = guiTop + (int) (h * perc);
            scrollerImageSpriteRect = ExtractElementsSprite.FILTER_LIST_SCROLLER_ACTIVE;
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ExtractElementsSprite.IMAGE, guiLeft + xSize - 10, posY, scrollerImageSpriteRect.x, scrollerImageSpriteRect.y, scrollerImageSpriteRect.w, scrollerImageSpriteRect.h, 256, 256);
    }

    private Pair<BlockState, ItemStack> getBlockAt(DirectionalPosition destination) {
        return filterPosCache.get(destination, () -> {
            ItemStack stack = new ItemStack(Items.WHITE_CONCRETE);
            BlockState state = null;
            if (mc.level.isAreaLoaded(destination.getPos(), 1)) {
                state = mc.level.getBlockState(destination.getPos());
                ItemStack pickBlock = state.getBlock().getCloneItemStack(mc.level, destination.getPos(), state, true, mc.player);
                if (pickBlock != null && !pickBlock.isEmpty()) {
                    stack = pickBlock;
                }
            }
            return new Pair<>(state, stack);
        });
    }

    @Override
    public void tick() {
        super.tick();
        filterPosCache.clear();
    }

    public int getOffset() {
        List<Filter<?, ?>> f = filters.get();
        if (f.size() <= this.rowCount) {
            offset = 0;
        } else if (offset > f.size() - this.rowCount) {
            offset = f.size() - this.rowCount;
        }
        FilterList.offsetLast = offset;
        return offset;
    }

    public int getSelected() {
        if (selected >= filters.get().size()) {
            selected = -1;
        }
        FilterList.selectedLast = selected;
        return selected;
    }

    private void drawStringSmall(GuiGraphics guiGraphics, int x, int y, Component text) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(0.5F, 0.5F);
        guiGraphics.drawString(mc.font, text, 0, 0, FontColorUtils.BLACK, false);
        guiGraphics.pose().popMatrix();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        List<Filter<?, ?>> f = filters.get();
        if (f.size() > this.rowCount) {
            if (deltaY < 0D) {
                offset = Math.min(getOffset() + 1, f.size() - this.rowCount);
            } else {
                offset = Math.max(getOffset() - 1, 0);
            }
            FilterList.offsetLast = offset;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<Filter<?, ?>> f = filters.get();
        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= f.size()) {
                break;
            }
            if (!hoverAreas[i].isHovered(guiLeft, guiTop, (int) mouseX, (int) mouseY)) {
                continue;
            }
            selected = getOffset() + i;
            FilterList.selectedLast = selected;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
