package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.helpers.AbstractStack;
import de.maxhenkel.corelib.helpers.Pair;
import de.maxhenkel.corelib.helpers.WrappedFluidStack;
import de.maxhenkel.corelib.helpers.WrappedItemStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Main;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

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
    protected void drawGuiContainerForegroundLayer(PoseStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        List<Filter<?>> f = filters.get();
        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= f.size()) {
                break;
            }
            Filter<?> filter = f.get(getOffset() + i);
            if (itemHoverAreas[i].isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                AbstractStack<?> stack = getStack(filter);
                if (stack != null && !stack.isEmpty()) {
                    List<Component> tooltip = stack.getTooltip(screen);
                    if (filter.isInvert()) {
                        tooltip.set(0, new TranslatableComponent("tooltip.pipez.filter.not").withStyle(ChatFormatting.DARK_RED).append(" ").append(tooltip.get(0)));
                    }
                    if (filter.getTag() != null && !(filter.getTag() instanceof SingleElementTag)) {
                        tooltip.add(new TranslatableComponent("tooltip.pipez.filter.accepts_tag", new TextComponent(filter.getTag().getName().toString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
                    }
                    screen.renderToolTip(matrixStack, tooltip.stream().map(Component::getVisualOrderText).collect(Collectors.toList()), mouseX - screen.getGuiLeft(), mouseY - screen.getGuiTop(), mc.font);
                }
            } else if (blockHoverAreas[i].isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                if (filter.getDestination() != null) {
                    List<Component> tooltip = new ArrayList<>();
                    Pair<BlockState, ItemStack> destPair = getBlockAt(filter.getDestination());
                    if (destPair.getKey() == null) {
                        tooltip.add(ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("tooltip.pipez.filter.unknown_block")).withStyle(ChatFormatting.DARK_RED));
                    } else {
                        tooltip.add(destPair.getKey().getBlock().getName().withStyle(ChatFormatting.BLUE));
                    }
                    BlockPos pos = filter.getDestination().getPos();
                    tooltip.add(new TranslatableComponent("tooltip.pipez.filter.destination_location", number(pos.getX()), number(pos.getY()), number(pos.getZ())));
                    tooltip.add(new TranslatableComponent("tooltip.pipez.filter.destination_distance", number(pos.distManhattan(getContainer().getPipe().getBlockPos()))));
                    tooltip.add(new TranslatableComponent("tooltip.pipez.filter.destination_side", new TranslatableComponent("message.pipez.direction." + filter.getDestination().getDirection().getName()).withStyle(ChatFormatting.DARK_GREEN)));
                    screen.renderToolTip(matrixStack, tooltip.stream().map(Component::getVisualOrderText).collect(Collectors.toList()), mouseX - screen.getGuiLeft(), mouseY - screen.getGuiTop(), mc.font);
                }
            }
        }
    }

    private MutableComponent number(int num) {
        return new TextComponent(String.valueOf(num)).withStyle(ChatFormatting.DARK_GREEN);
    }

    @Nullable
    public static AbstractStack<?> getStack(Filter<?> filter) {
        Object o = null;

        if (filter.getTag() != null) {
            o = get(filter.getTag());
        }

        if (o instanceof Item) {
            ItemStack stack = new ItemStack((Item) o);
            if (filter.getMetadata() != null) {
                stack.setTag(filter.getMetadata());
            }
            return new WrappedItemStack(stack);
        } else if (o instanceof Fluid) {
            FluidStack stack = new FluidStack((Fluid) o, 1000);
            if (filter.getMetadata() != null) {
                stack.setTag(filter.getMetadata());
            }
            return new WrappedFluidStack(stack);
        }
        // TODO add back Mekanism
        /* else if (o instanceof Gas) {
            GasStack stack = new GasStack((Gas) o, 1000);
            return new WrappedGasStack(stack);
        }*/

        return null;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        List<Filter<?>> f = filters.get();
        for (int i = getOffset(); i < f.size() && i < getOffset() + columnCount; i++) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, BACKGROUND);
            int pos = i - getOffset();
            int startY = guiTop + pos * columnHeight;
            Filter<?> filter = f.get(i);
            if (i == getSelected()) {
                GuiComponent.blit(matrixStack, guiLeft, startY, 0, 218, 125, columnHeight, 256, 256);
            } else {
                GuiComponent.blit(matrixStack, guiLeft, startY, 0, 196, 125, columnHeight, 256, 256);
            }

            AbstractStack<?> stack = getStack(filter);
            if (stack != null && !stack.isEmpty()) {
                stack.render(matrixStack, guiLeft + 3, startY + 3);
                if (filter.getTag() != null) {
                    if (filter.getTag() instanceof SingleElementTag) {
                        drawStringSmall(matrixStack, guiLeft + 22, startY + 5, new TranslatableComponent("message.pipez.filter.item", new TranslatableComponent(stack.getDisplayName().getString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.WHITE));
                    } else {
                        drawStringSmall(matrixStack, guiLeft + 22, startY + 5, new TranslatableComponent("message.pipez.filter.tag", new TextComponent(filter.getTag().getName().toString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.WHITE));
                    }
                }
            } else {
                drawStringSmall(matrixStack, guiLeft + 22, startY + 5, new TranslatableComponent("message.pipez.filter.any_item").withStyle(ChatFormatting.WHITE));
            }
            if (filter.getMetadata() != null && filter.getMetadata().size() > 0) {
                MutableComponent tags = new TranslatableComponent("message.pipez.filter.nbt.tag" + (filter.getMetadata().size() != 1 ? "s" : ""), filter.getMetadata().size()).withStyle(ChatFormatting.DARK_PURPLE);
                MutableComponent nbtStr = new TranslatableComponent("message.pipez.filter.nbt", tags).withStyle(ChatFormatting.WHITE);
                if (filter.isExactMetadata()) {
                    nbtStr.append(" ").append(new TranslatableComponent("message.pipez.filter.nbt.exact"));
                }
                drawStringSmall(matrixStack, guiLeft + 22, startY + 10, nbtStr);
            }

            if (filter.isInvert()) {
                drawStringSmall(matrixStack, guiLeft + 22, startY + 15, new TranslatableComponent("message.pipez.filter.inverted").withStyle(ChatFormatting.DARK_RED));
            }

            if (filter.getDestination() != null) {
                Pair<BlockState, ItemStack> dstPair = getBlockAt(filter.getDestination());
                mc.getItemRenderer().renderAndDecorateItem(mc.player, dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY + 3, 0);
                mc.getItemRenderer().renderGuiItemDecorations(mc.font, dstPair.getValue(), guiLeft + xSize - 3 - 16 - 11, startY + 3, String.valueOf(filter.getDestination().getDirection().name().charAt(0)));
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, BACKGROUND);

        if (f.size() > columnCount) {
            float h = 66 - 17;
            float perc = (float) getOffset() / (float) (f.size() - columnCount);
            int posY = guiTop + (int) (h * perc);
            GuiComponent.blit(matrixStack, guiLeft + xSize - 10, posY, 125, 196, 10, 17, 256, 256);
        } else {
            GuiComponent.blit(matrixStack, guiLeft + xSize - 10, guiTop, 135, 196, 10, 17, 256, 256);
        }
    }

    private Pair<BlockState, ItemStack> getBlockAt(DirectionalPosition destination) {
        return filterPosCache.get(destination, () -> {
            ItemStack stack = new ItemStack(Items.WHITE_CONCRETE);
            BlockState state = null;
            if (mc.level.isAreaLoaded(destination.getPos(), 1)) {
                state = mc.level.getBlockState(destination.getPos());
                ItemStack pickBlock = state.getBlock().getPickBlock(state, new BlockHitResult(new Vec3(destination.getPos().getX() + 0.5D, destination.getPos().getY() + 0.5D, destination.getPos().getZ() + 0.5D), destination.getDirection(), destination.getPos(), true), mc.level, destination.getPos(), mc.player);
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

    private void drawStringSmall(PoseStack matrixStack, int x, int y, Component text) {
        matrixStack.pushPose();
        matrixStack.translate(x, y, 0);
        matrixStack.scale(0.5F, 0.5F, 1F);
        mc.font.draw(matrixStack, text, 0, 0, 0);
        matrixStack.popPose();
    }

    public static <T> T get(Tag.Named<T> tag) {
        long time = Minecraft.getInstance().level.getGameTime();
        List<T> allElements = tag.getValues();
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
