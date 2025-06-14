package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.FontColorUtils;
import de.maxhenkel.corelib.helpers.AbstractStack;
import de.maxhenkel.corelib.helpers.Pair;
import de.maxhenkel.corelib.helpers.WrappedFluidStack;
import de.maxhenkel.corelib.helpers.WrappedItemStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.utils.ComponentUtils;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.utils.MekanismUtils;
import de.maxhenkel.pipez.utils.WrappedGasStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterList extends WidgetBase {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/container/extract.png");

    protected Supplier<List<Filter<?, ?>>> filters;
    protected int offset;
    protected int selected;
    private ScreenBase.HoverArea[] hoverAreas;
    private ScreenBase.HoverArea[] itemHoverAreas;
    private ScreenBase.HoverArea[] blockHoverAreas;
    private int columnHeight;
    private int columnCount;
    private CachedMap<DirectionalPosition, Pair<BlockState, ItemStack>> filterPosCache;

    public FilterList(ExtractScreen screen, int posX, int posY, int xSize, int ySize, Supplier<List<Filter<?, ?>>> filters) {
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
    protected void drawGuiContainerForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiGraphics, mouseX, mouseY);
        List<Filter<?, ?>> f = filters.get();
        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= f.size()) {
                break;
            }
            Filter<?, ?> filter = f.get(getOffset() + i);
            if (itemHoverAreas[i].isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                AbstractStack<?> stack = getStack(filter);
                if (stack != null && !stack.isEmpty()) {
                    List<Component> tooltip = stack.getTooltip();
                    if (filter.isInvert()) {
                        tooltip.set(0, Component.translatable("tooltip.pipez.filter.not").withStyle(ChatFormatting.DARK_RED).append(" ").append(tooltip.get(0)));
                    }
                    if (filter.getTag() != null && !(filter.getTag() instanceof SingleElementTag)) {
                        tooltip.add(Component.translatable("tooltip.pipez.filter.accepts_tag", Component.literal(filter.getTag().getName().toString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
                    }
                    guiGraphics.setTooltipForNextFrame(mc.font, tooltip.stream().map(Component::getVisualOrderText).collect(Collectors.toList()), mouseX - screen.getGuiLeft(), mouseY - screen.getGuiTop());
                }
            } else if (blockHoverAreas[i].isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                if (filter.getDestination() != null) {
                    List<Component> tooltip = new ArrayList<>();
                    Pair<BlockState, ItemStack> destPair = getBlockAt(filter.getDestination());
                    if (destPair.getKey() == null) {
                        tooltip.add(net.minecraft.network.chat.ComponentUtils.wrapInSquareBrackets(Component.translatable("tooltip.pipez.filter.unknown_block")).withStyle(ChatFormatting.DARK_RED));
                    } else {
                        tooltip.add(destPair.getKey().getBlock().getName().withStyle(ChatFormatting.BLUE));
                    }
                    BlockPos pos = filter.getDestination().getPos();
                    tooltip.add(Component.translatable("tooltip.pipez.filter.destination_location", number(pos.getX()), number(pos.getY()), number(pos.getZ())));
                    tooltip.add(Component.translatable("tooltip.pipez.filter.destination_distance", number(pos.distManhattan(getContainer().getPipe().getBlockPos()))));
                    tooltip.add(Component.translatable("tooltip.pipez.filter.destination_side", Component.translatable("message.pipez.direction." + filter.getDestination().getDirection().getName()).withStyle(ChatFormatting.DARK_GREEN)));
                    guiGraphics.setTooltipForNextFrame(mc.font, tooltip.stream().map(Component::getVisualOrderText).collect(Collectors.toList()), mouseX - screen.getGuiLeft(), mouseY - screen.getGuiTop());
                }
            }
        }
    }

    private MutableComponent number(int num) {
        return Component.literal(String.valueOf(num)).withStyle(ChatFormatting.DARK_GREEN);
    }

    @Nullable
    public static AbstractStack<?> getStack(Filter<?, ?> filter) {
        Object o = null;

        if (filter.getTag() != null) {
            o = get(filter.getTag());
        }

        if (o instanceof Item) {
            ItemStack stack = new ItemStack((Item) o);
            if (filter.getMetadata() != null) {
                stack.applyComponents(ComponentUtils.getPatch(Minecraft.getInstance().level.registryAccess(), filter.getMetadata().copy()));
            }
            return new WrappedItemStack(stack);
        } else if (o instanceof Fluid) {
            FluidStack stack = new FluidStack((Fluid) o, 1000);
            if (filter.getMetadata() != null) {
                stack.applyComponents(ComponentUtils.getPatch(Minecraft.getInstance().level.registryAccess(), filter.getMetadata().copy()));
            }
            return new WrappedFluidStack(stack);
        }

        if (MekanismUtils.isMekanismInstalled()) {
            AbstractStack<?> gasStack = WrappedGasStack.dummyStack(o);
            if (gasStack != null) {
                return gasStack;
            }
        }

        return null;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(guiGraphics, partialTicks, mouseX, mouseY);

        List<Filter<?, ?>> f = filters.get();
        for (int i = getOffset(); i < f.size() && i < getOffset() + columnCount; i++) {
            int pos = i - getOffset();
            int startY = guiTop + pos * columnHeight;
            Filter<?, ?> filter = f.get(i);
            if (i == getSelected()) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, guiLeft, startY, 0, 218, 125, columnHeight, 256, 256);
            } else {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, guiLeft, startY, 0, 196, 125, columnHeight, 256, 256);
            }

            AbstractStack<?> stack = getStack(filter);
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

            if (filter.getDestination() != null) {
                Pair<BlockState, ItemStack> dstPair = getBlockAt(filter.getDestination());
                guiGraphics.renderItem(dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY + 3, 0);
                guiGraphics.renderItemDecorations(mc.font, dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY + 3, String.valueOf(filter.getDestination().getDirection().name().charAt(0)));
            }
        }

        if (f.size() > columnCount) {
            float h = 66 - 17;
            float perc = (float) getOffset() / (float) (f.size() - columnCount);
            int posY = guiTop + (int) (h * perc);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, guiLeft + xSize - 10, posY, 125, 196, 10, 17, 256, 256);
        } else {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, guiLeft + xSize - 10, guiTop, 135, 196, 10, 17, 256, 256);
        }
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

    private void drawStringSmall(GuiGraphics guiGraphics, int x, int y, Component text) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(0.5F, 0.5F);
        guiGraphics.drawString(mc.font, text, 0, 0, FontColorUtils.BLACK, false);
        guiGraphics.pose().popMatrix();
    }

    public static <T> T get(Tag<T> tag) {
        long time = Minecraft.getInstance().level.getGameTime();
        List<T> allElements = tag.getAll().stream().toList();
        return allElements.get((int) (time / 20L % allElements.size()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        List<Filter<?, ?>> f = filters.get();
        if (f.size() > columnCount) {
            if (deltaY < 0D) {
                offset = Math.min(getOffset() + 1, f.size() - columnCount);
            } else {
                offset = Math.max(getOffset() - 1, 0);
            }
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
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
