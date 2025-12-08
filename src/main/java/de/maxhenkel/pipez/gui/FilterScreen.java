package de.maxhenkel.pipez.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.maxhenkel.corelib.FontColorUtils;
import de.maxhenkel.corelib.helpers.AbstractStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.corelib.tag.TagUtils;
import de.maxhenkel.pipez.*;
import de.maxhenkel.pipez.items.FilterDestinationToolItem;
import de.maxhenkel.pipez.net.OpenExtractMessage;
import de.maxhenkel.pipez.net.UpdateFilterMessage;
import de.maxhenkel.pipez.utils.NbtUtils;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterScreen extends ScreenBase<FilterContainer> {
    public static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(PipezMod.MODID, "textures/gui/container/filter.png");

    private EditBox item;
    private EditBox nbt;

    private CycleIconButton nbtButton;
    private CycleIconButton invertButton;

    private Button submitButton;
    private Button cancelButton;

    private HoverArea itemHoverArea;
    private HoverArea itemTextHoverArea;
    private HoverArea nbtTextHoverArea;
    private HoverArea exactNBTHoverArea;
    private HoverArea invertHoverArea;
    private HoverArea destinationHoverArea;
    private HoverArea destinationTextHoverArea;

    private Filter<?, ?> filter;

    public FilterScreen(FilterContainer container, Inventory playerInventory, Component title) {
        super(BACKGROUND, container, playerInventory, title);
        imageWidth = 176;
        imageHeight = 222;

        filter = getMenu().getFilter();
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        clearWidgets();

        List<CycleIconButton.Icon> nbtIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 16), new CycleIconButton.Icon(BACKGROUND, 192, 16));
        nbtButton = new CycleIconButton(leftPos + 125, topPos + 81, nbtIcons, () -> filter.isExactMetadata() ? 1 : 0, button -> {
            filter.setExactMetadata(!filter.isExactMetadata());
        });
        addRenderableWidget(nbtButton);
        List<CycleIconButton.Icon> invertIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 32), new CycleIconButton.Icon(BACKGROUND, 192, 32));
        invertButton = new CycleIconButton(leftPos + 149, topPos + 81, invertIcons, () -> filter.isInvert() ? 1 : 0, button -> {
            filter.setInvert(!filter.isInvert());
        });
        addRenderableWidget(invertButton);

        cancelButton = Button.builder(Component.translatable("message.pipez.filter.cancel"), button -> {
            ClientPacketDistributor.sendToServer(new OpenExtractMessage(getMenu().getIndex()));
        }).bounds(leftPos + 25, topPos + 105, 60, 20).build();
        addRenderableWidget(cancelButton);

        submitButton = Button.builder(Component.translatable("message.pipez.filter.submit"), button -> {
            ClientPacketDistributor.sendToServer(new UpdateFilterMessage(filter, menu.getIndex()));
        }).bounds(leftPos + 91, topPos + 105, 60, 20).build();
        addRenderableWidget(submitButton);

        item = new EditBox(font, leftPos + 29, topPos + 17, 140, 18, Component.empty());
        item.setTextColor(FontColorUtils.WHITE);
        item.setBordered(true);
        item.setMaxLength(Integer.MAX_VALUE);
        if (filter.getTag() != null) {
            if (filter.getTag() instanceof SingleElementTag) {
                item.setValue(filter.getTag().getName().toString());
            } else {
                item.setValue("#" + filter.getTag().getName().toString());
            }
        }
        item.setResponder(this::onItemTextChanged);
        item.setFilter(s -> {
            if (s.startsWith("#")) {
                s = s.substring(1);
            }
            return Identifier.tryParse(s) != null;
        });
        addRenderableWidget(item);

        nbt = new EditBox(font, leftPos + 7, topPos + 49, 162, 18, Component.empty());
        nbt.setTextColor(FontColorUtils.WHITE);
        nbt.setBordered(true);
        nbt.setMaxLength(Integer.MAX_VALUE);
        nbt.setValue(filter.getMetadata() != null ? filter.getMetadata().toString() : "");
        nbt.setResponder(this::onNbtTextChanged);
        nbt.visible = hasNBT();

        addRenderableWidget(nbt);

        nbtButton.active = filter.getMetadata() != null;

        itemHoverArea = new HoverArea(8, 18, 16, 16, () -> {
            List<Component> tooltip = new ArrayList<>();

            AbstractStack<?> stack = FilterList.getStack(filter);
            if (stack != null) {
                tooltip = stack.getTooltip();
                if (filter.getTag() != null && !(filter.getTag() instanceof SingleElementTag)) {
                    tooltip.add(Component.translatable("tooltip.pipez.filter.accepts_tag", Component.literal(filter.getTag().getName().toString()).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
                }
            }
            return tooltip;
        });
        hoverAreas.add(itemHoverArea);
        itemTextHoverArea = new HoverArea(29, 17, 140, 18, () -> {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("tooltip.pipez.filter.item_tag.description"));
            return tooltip;
        });
        hoverAreas.add(itemTextHoverArea);
        nbtTextHoverArea = new HoverArea(7, 49, 162, 18, () -> {
            List<Component> tooltip = new ArrayList<>();
            if (hasNBT()) {
                tooltip.add(Component.translatable("tooltip.pipez.filter.nbt_string.description"));
            } else {
                tooltip.add(Component.translatable("tooltip.pipez.filter.nbt_string.no_nbt"));
            }
            return tooltip;
        });
        hoverAreas.add(nbtTextHoverArea);
        exactNBTHoverArea = new HoverArea(126, 82, 20, 20, () -> {
            List<Component> tooltip = new ArrayList<>();
            if (filter.isExactMetadata()) {
                tooltip.add(Component.translatable("tooltip.pipez.filter.nbt.exact"));
            } else {
                tooltip.add(Component.translatable("tooltip.pipez.filter.nbt.not_exact"));
            }
            return tooltip;
        });
        hoverAreas.add(exactNBTHoverArea);
        invertHoverArea = new HoverArea(150, 82, 20, 20, () -> {
            List<Component> tooltip = new ArrayList<>();
            if (filter.isInvert()) {
                tooltip.add(Component.translatable("tooltip.pipez.filter.inverted"));
            } else {
                tooltip.add(Component.translatable("tooltip.pipez.filter.not_inverted"));
            }
            return tooltip;
        });
        hoverAreas.add(invertHoverArea);
        destinationHoverArea = new HoverArea(8, 83, 16, 16, () -> {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("tooltip.pipez.filter.destination.description"));
            if (filter.getDestination() != null) {
                tooltip.add(Component.translatable("tooltip.pipez.filter.destination.click_to_remove").withStyle(ChatFormatting.GRAY));
            }
            return tooltip;
        });
        hoverAreas.add(destinationHoverArea);

        destinationTextHoverArea = new HoverArea(25, 82, 96, 18, () -> {
            List<Component> tooltip = new ArrayList<>();
            if (filter.getDestination() != null) {
                DirectionalPosition dst = filter.getDestination();
                tooltip.add(Component.translatable("tooltip.pipez.filter_destination_tool.destination", number(dst.getPos().getX()), number(dst.getPos().getY()), number(dst.getPos().getZ()), Component.translatable("message.pipez.direction." + dst.getDirection().getName()).withStyle(ChatFormatting.DARK_GREEN)));
            }
            return tooltip;
        });
        hoverAreas.add(destinationTextHoverArea);
    }

    private boolean hasNBT() {
        return !(filter instanceof GasFilter);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        submitButton.active = filter.getTag() != null || filter.getMetadata() != null;
    }

    public void onItemTextChanged(String text) {
        if (text.trim().isEmpty()) {
            nbt.setTextColor(FontColorUtils.WHITE);
            return;
        }

        if (filter instanceof ItemFilter) {
            Tag tag = TagUtils.getItem(text, true);
            filter.setTag(tag);
            if (filter.getTag() == null) {
                item.setTextColor(FontColorUtils.getFontColor(ChatFormatting.DARK_RED));
            } else {
                item.setTextColor(FontColorUtils.WHITE);
            }
        } else if (filter instanceof FluidFilter) {
            Tag tag = TagUtils.getFluid(text, true);
            filter.setTag(tag);
            if (filter.getTag() == null) {
                item.setTextColor(FontColorUtils.getFontColor(ChatFormatting.DARK_RED));
            } else {
                item.setTextColor(FontColorUtils.WHITE);
            }
        } else if (filter instanceof GasFilter) {
            // TODO Re-add once mekanism is updated
            //filter.setTag((Tag) GasUtils.getGas(text, true));
            if (filter.getTag() == null) {
                item.setTextColor(FontColorUtils.getFontColor(ChatFormatting.DARK_RED));
            } else {
                item.setTextColor(FontColorUtils.WHITE);
            }
        }
    }

    public void onNbtTextChanged(String text) {
        if (text.trim().isEmpty()) {
            nbt.setTextColor(FontColorUtils.WHITE);
            nbtButton.active = false;
            filter.setExactMetadata(false);
            filter.setMetadata(null);
            return;
        }
        nbtButton.active = true;
        try {
            filter.setMetadata(TagParser.parseCompoundFully(text));
            nbt.setTextColor(FontColorUtils.WHITE);
        } catch (CommandSyntaxException e) {
            nbt.setTextColor(FontColorUtils.getFontColor(ChatFormatting.DARK_RED));
            filter.setMetadata(null);
        }
    }

    public void onInsertStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (filter instanceof ItemFilter) {
            item.setValue(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            nbt.setValue(NbtUtils.componentPatchToNbtOptional(stack.getComponentsPatch()).map(CompoundTag::toString).orElse(""));
        } else if (filter instanceof FluidFilter) {
            FluidStack fluidStack = FluidUtil.getFirstStackContained(stack);
            if (!fluidStack.isEmpty()) {
                onInsertStack(fluidStack);
            }
        } else if (filter instanceof GasFilter) {
            // TODO Re-add once mekanism is updated
            /*ChemicalStack gas = GasUtils.getGasContained(stack);
            if (gas != null) {
                onInsertStack(gas);
            }*/
        }
    }

    public void onInsertStack(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (filter instanceof FluidFilter) {
            item.setValue(BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
            nbt.setValue(NbtUtils.componentPatchToNbtOptional(stack.getComponentsPatch()).map(CompoundTag::toString).orElse(""));
        }
    }

    public void onInsertStack(ChemicalStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        // TODO Re-add once mekanism is updated
        /*if (filter instanceof GasFilter) {
            item.setValue(stack.getChemical().getRegistryName().toString());
            nbt.setValue("");
        }*/
    }

    public void onInsertDestination(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof FilterDestinationToolItem)) {
            filter.setDestination(null);
            return;
        }
        DirectionalPosition dst = FilterDestinationToolItem.getDestination(stack);
        filter.setDestination(dst);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(font, Component.translatable("message.pipez.filter.item_tag"), 8, 7, FONT_COLOR, false);
        guiGraphics.drawString(font, Component.translatable("message.pipez.filter.nbt_string"), 8, 39, FONT_COLOR, false);
        guiGraphics.drawString(font, Component.translatable("message.pipez.filter.destination"), 8, 71, FONT_COLOR, false);
        guiGraphics.drawString(font, playerInventoryTitle.getVisualOrderText(), 8, imageHeight - 96 + 3, FONT_COLOR, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);

        AbstractStack<?> stack = FilterList.getStack(filter);
        if (stack != null) {
            stack.render(guiGraphics, leftPos + 8, topPos + 18);
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(leftPos + 31, topPos + 89);
        guiGraphics.pose().scale(0.5F, 0.5F);
        if (filter.getDestination() != null) {
            DirectionalPosition dst = filter.getDestination();
            guiGraphics.drawString(font, Component.translatable("message.pipez.filter_destination_tool.destination", number(dst.getPos().getX()), number(dst.getPos().getY()), number(dst.getPos().getZ()), Component.literal(String.valueOf(dst.getDirection().name().charAt(0))).withStyle(ChatFormatting.DARK_GREEN)), 0, 0, FontColorUtils.WHITE, false);
        } else {
            guiGraphics.drawString(font, Component.translatable("message.pipez.filter_destination_tool.destination.any"), 0, 0, FontColorUtils.WHITE, false);
        }
        guiGraphics.pose().popMatrix();

        if (itemHoverArea.isHovered(leftPos, topPos, mouseX, mouseY)) {
            drawHoverSlot(guiGraphics, leftPos + 8, topPos + 18);
        }
        if (destinationHoverArea.isHovered(leftPos, topPos, mouseX, mouseY)) {
            drawHoverSlot(guiGraphics, leftPos + 8, topPos + 83);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        super.render(guiGraphics, x, y, partialTicks);
        drawHoverAreas(guiGraphics, x, y);
    }

    private MutableComponent number(int num) {
        return Component.literal(String.valueOf(num)).withStyle(ChatFormatting.DARK_GREEN);
    }

    private void drawHoverSlot(GuiGraphics guiGraphics, int posX, int posY) {
        guiGraphics.fillGradient(posX, posY, posX + 16, posY + 16, slotColor, -2130706433);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean b) {
        if (itemHoverArea.isHovered(leftPos, topPos, (int) event.x(), (int) event.y())) {
            if (event.hasShiftDown()) {
                item.setValue("");
                filter.setTag(null);
            } else {
                onInsertStack(getMenu().getCarried());
            }
            return true;
        }
        if (destinationHoverArea.isHovered(leftPos, topPos, (int) event.x(), (int) event.y())) {
            onInsertDestination(getMenu().getCarried());
            return true;
        }

        if (event.hasShiftDown()) {
            Slot sl = this.getSlotUnderMouse();
            if (sl != null) {
                onInsertStack(sl.getItem());
            }
        }

        return super.mouseClicked(event, b);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            minecraft.player.closeContainer();
            return true;
        }

        return item.keyPressed(event) ||
                item.canConsumeInput() ||
                nbt.keyPressed(event) ||
                nbt.canConsumeInput() || super.keyPressed(event);
    }

    @Override
    public void resize(int x, int y) {
        String itemTxt = item.getValue();
        String nbtTxt = nbt.getValue();
        init(x, y);
        item.setValue(itemTxt);
        nbt.setValue(nbtTxt);
    }

}
