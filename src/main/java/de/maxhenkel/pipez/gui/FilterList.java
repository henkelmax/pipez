package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.helpers.Pair;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Main;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterList extends WidgetBase {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Main.MODID, "textures/gui/container/extract.png");

    protected Supplier<List<Filter<?>>> filters;
    protected int offset;
    protected int selected;
    private ScreenBase.HoverArea[] hoverAreas;
    private ScreenBase.HoverArea[] itemHoverAreas;
    private ScreenBase.HoverArea[] blockHoverAreas;
    private int columnHeight;
    private int columnCount;
    private CachedMap<DirectionalPosition, Pair<BlockState, ItemStack>> filterPosCache;

    public FilterList(ExtractScreen screen, int posX, int posY, int xSize, int ySize, Supplier<List<Filter<?>>> filters) {
        super(screen, posX, posY, xSize, ySize);
        this.filters = filters;
        columnHeight = 22;
        columnCount = 3;
        selected = -1;

        hoverAreas = new ScreenBase.HoverArea[columnCount];
        itemHoverAreas = new ScreenBase.HoverArea[columnCount];
        blockHoverAreas = new ScreenBase.HoverArea[columnCount];
        for (int i = 0; i < hoverAreas.length; i++) {
            hoverAreas[i] = new ScreenBase.HoverArea(0, i * columnHeight, xSize, columnHeight);
            itemHoverAreas[i] = new ScreenBase.HoverArea(3, 3 + i * columnHeight, 16, 16);
            blockHoverAreas[i] = new ScreenBase.HoverArea(xSize - 3 - 16 - 11, 3 + i * columnHeight, 16, 16);
        }

        filterPosCache = new CachedMap<>(1000);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        List<Filter<?>> f = filters.get();
        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= f.size()) {
                break;
            }
            Filter<?> filter = f.get(getOffset() + i);
            if (itemHoverAreas[i].isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                ItemStack stack = getStack(filter);
                if (stack != null && !stack.isEmpty()) {
                    List<ITextComponent> tooltip = screen.getTooltipFromItem(stack);
                    if (filter.isInvert()) {
                        tooltip.set(0, new TranslationTextComponent("tooltip.pipez.filter.not").mergeStyle(TextFormatting.DARK_RED).appendString(" ").append(tooltip.get(0)));
                    }
                    if (filter.getTag() != null) {
                        tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.accepts_tag", new StringTextComponent(filter.getTag().getName().toString()).mergeStyle(TextFormatting.BLUE)).mergeStyle(TextFormatting.GRAY));
                    }
                    screen.renderToolTip(matrixStack, tooltip.stream().map(ITextComponent::func_241878_f).collect(Collectors.toList()), mouseX - screen.getGuiLeft(), mouseY - screen.getGuiTop(), mc.fontRenderer);
                }
            } else if (blockHoverAreas[i].isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                if (filter.getDestination() != null) {
                    List<ITextComponent> tooltip = new ArrayList<>();
                    Pair<BlockState, ItemStack> destPair = getBlockAt(filter.getDestination());
                    if (destPair.getKey() == null) {
                        tooltip.add(TextComponentUtils.wrapWithSquareBrackets(new TranslationTextComponent("tooltip.pipez.filter.unknown_block")).mergeStyle(TextFormatting.DARK_RED));
                    } else {
                        tooltip.add(destPair.getKey().getBlock().getTranslatedName().mergeStyle(TextFormatting.BLUE));
                    }
                    BlockPos pos = filter.getDestination().getPos();
                    tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.destination_location", number(pos.getX()), number(pos.getY()), number(pos.getZ())));
                    tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.destination_distance", number(pos.manhattanDistance(getContainer().getPipe().getPos()))));
                    tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.destination_side", new TranslationTextComponent("message.pipez.direction." + filter.getDestination().getDirection().getName2()).mergeStyle(TextFormatting.DARK_GREEN)));
                    screen.renderToolTip(matrixStack, tooltip.stream().map(ITextComponent::func_241878_f).collect(Collectors.toList()), mouseX - screen.getGuiLeft(), mouseY - screen.getGuiTop(), mc.fontRenderer);
                }
            }
        }
    }

    private IFormattableTextComponent number(int num) {
        return new StringTextComponent(String.valueOf(num)).mergeStyle(TextFormatting.DARK_GREEN);
    }

    @Nullable
    public ItemStack getStack(Filter<?> filter) {
        Object o = null;

        if (filter.getTag() != null) {
            o = get(filter.getTag());
        }

        if (o instanceof Item) {
            ItemStack stack = new ItemStack((Item) o);
            if (filter.getMetadata() != null) {
                stack.setTag(filter.getMetadata());
            }
            return stack;
        }
        return null;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        List<Filter<?>> f = filters.get();
        for (int i = getOffset(); i < f.size() && i < getOffset() + columnCount; i++) {
            mc.getTextureManager().bindTexture(BACKGROUND);
            int pos = i - getOffset();
            int startY = guiTop + pos * columnHeight;
            Filter<?> filter = f.get(i);
            if (i == getSelected()) {
                AbstractGui.blit(matrixStack, guiLeft, startY, 0, 218, 125, columnHeight, 256, 256);
            } else {
                AbstractGui.blit(matrixStack, guiLeft, startY, 0, 196, 125, columnHeight, 256, 256);
            }

            ItemStack stack = getStack(filter);
            if (stack != null && !stack.isEmpty()) {
                mc.getItemRenderer().renderItemAndEffectIntoGUI(mc.player, stack, guiLeft + 3, startY + 3);
                if (filter.getTag() != null) {
                    if (filter.getTag() instanceof SingleElementTag) {
                        drawStringSmall(matrixStack, guiLeft + 22, startY + 5, new TranslationTextComponent("message.pipez.filter.item", new TranslationTextComponent(stack.getDisplayName().getString()).mergeStyle(TextFormatting.BLUE)).mergeStyle(TextFormatting.WHITE));
                    } else {
                        drawStringSmall(matrixStack, guiLeft + 22, startY + 5, new TranslationTextComponent("message.pipez.filter.tag", new StringTextComponent(filter.getTag().getName().toString()).mergeStyle(TextFormatting.BLUE)).mergeStyle(TextFormatting.WHITE));
                    }
                    if (filter.getMetadata() != null && filter.getMetadata().size() > 0) {
                        IFormattableTextComponent tags = new TranslationTextComponent("message.pipez.filter.nbt.tag" + (filter.getMetadata().size() != 1 ? "s" : ""), filter.getMetadata().size()).mergeStyle(TextFormatting.DARK_PURPLE);
                        IFormattableTextComponent nbtStr = new TranslationTextComponent("message.pipez.filter.nbt", tags).mergeStyle(TextFormatting.WHITE);
                        if (filter.isExactMetadata()) {
                            nbtStr.appendString(" ").append(new TranslationTextComponent("message.pipez.filter.nbt.exact"));
                        }
                        drawStringSmall(matrixStack, guiLeft + 22, startY + 10, nbtStr);
                    }
                }
            }

            if (filter.isInvert()) {
                drawStringSmall(matrixStack, guiLeft + 22, startY + 15, new TranslationTextComponent("message.pipez.filter.inverted").mergeStyle(TextFormatting.DARK_RED));
            }

            if (filter.getDestination() != null) {
                Pair<BlockState, ItemStack> dstPair = getBlockAt(filter.getDestination());
                mc.getItemRenderer().renderItemAndEffectIntoGUI(mc.player, dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY + 3);
                mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY + 3, String.valueOf(filter.getDestination().getDirection().name().charAt(0)));
            }
        }

        mc.getTextureManager().bindTexture(BACKGROUND);

        if (f.size() > columnCount) {
            float h = 66 - 17;
            float perc = (float) getOffset() / (float) (f.size() - columnCount);
            int posY = guiTop + (int) (h * perc);
            AbstractGui.blit(matrixStack, guiLeft + xSize - 10, posY, 125, 196, 10, 17, 256, 256);
        } else {
            AbstractGui.blit(matrixStack, guiLeft + xSize - 10, guiTop, 135, 196, 10, 17, 256, 256);
        }
    }

    private Pair<BlockState, ItemStack> getBlockAt(DirectionalPosition destination) {
        return filterPosCache.get(destination, () -> {
            ItemStack stack = new ItemStack(Items.WHITE_CONCRETE);
            BlockState state = null;
            if (mc.world.isAreaLoaded(destination.getPos(), 1)) {
                state = mc.world.getBlockState(destination.getPos());
                ItemStack pickBlock = state.getBlock().getPickBlock(state, new BlockRayTraceResult(new Vector3d(destination.getPos().getX() + 0.5D, destination.getPos().getY() + 0.5D, destination.getPos().getZ() + 0.5D), destination.getDirection(), destination.getPos(), true), mc.world, destination.getPos(), mc.player);
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
        List<Filter<?>> f = filters.get();
        if (f.size() <= columnCount) {
            offset = 0;
        } else if (offset > f.size() - columnCount) {
            offset = f.size() - columnCount;
        }
        return offset;
    }

    public int getSelected() {
        if (selected >= filters.get().size()) {
            selected = -1;
        }
        return selected;
    }

    private void drawStringSmall(MatrixStack matrixStack, int x, int y, ITextComponent text) {
        matrixStack.push();
        matrixStack.translate(x, y, 0);
        matrixStack.scale(0.5F, 0.5F, 1F);
        mc.fontRenderer.func_243248_b(matrixStack, text, 0, 0, 0);
        matrixStack.pop();
    }

    public static <T> T get(ITag.INamedTag<T> tag) {
        long time = Minecraft.getInstance().world.getGameTime();
        List<T> allElements = tag.getAllElements();
        return allElements.get((int) (time / 20L % allElements.size()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<Filter<?>> f = filters.get();
        if (f.size() > columnCount) {
            if (delta < 0D) {
                offset = Math.min(getOffset() + 1, f.size() - columnCount);
            } else {
                offset = Math.max(getOffset() - 1, 0);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<Filter<?>> f = filters.get();
        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= f.size()) {
                break;
            }
            if (!hoverAreas[i].isHovered(guiLeft, guiTop, (int) mouseX, (int) mouseY)) {
                continue;
            }
            selected = getOffset() + i;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
