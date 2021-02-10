package de.maxhenkel.pipez.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.maxhenkel.corelib.helpers.AbstractStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.TagUtils;
import de.maxhenkel.pipez.*;
import de.maxhenkel.pipez.items.FilterDestinationToolItem;
import de.maxhenkel.pipez.net.OpenExtractMessage;
import de.maxhenkel.pipez.net.UpdateFilterMessage;
import de.maxhenkel.pipez.utils.GasUtils;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.fluids.FluidUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilterScreen extends ScreenBase<FilterContainer> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Main.MODID, "textures/gui/container/filter.png");

    private TextFieldWidget item;
    private TextFieldWidget nbt;

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

    private Filter<?> filter;

    public FilterScreen(FilterContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(BACKGROUND, container, playerInventory, title);
        xSize = 176;
        ySize = 222;

        filter = getContainer().getFilter();
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        buttons.clear();
        children.clear();
        minecraft.keyboardListener.enableRepeatEvents(true);

        List<CycleIconButton.Icon> nbtIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 16), new CycleIconButton.Icon(BACKGROUND, 192, 16));
        nbtButton = new CycleIconButton(guiLeft + 125, guiTop + 81, nbtIcons, () -> filter.isExactMetadata() ? 1 : 0, button -> {
            filter.setExactMetadata(!filter.isExactMetadata());
        });
        addButton(nbtButton);
        List<CycleIconButton.Icon> invertIcons = Arrays.asList(new CycleIconButton.Icon(BACKGROUND, 176, 32), new CycleIconButton.Icon(BACKGROUND, 192, 32));
        invertButton = new CycleIconButton(guiLeft + 149, guiTop + 81, invertIcons, () -> filter.isInvert() ? 1 : 0, button -> {
            filter.setInvert(!filter.isInvert());
        });
        addButton(invertButton);

        cancelButton = new Button(guiLeft + 25, guiTop + 105, 60, 20, new TranslationTextComponent("message.pipez.filter.cancel"), button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new OpenExtractMessage());
        });
        addButton(cancelButton);

        submitButton = new Button(guiLeft + 91, guiTop + 105, 60, 20, new TranslationTextComponent("message.pipez.filter.submit"), button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new UpdateFilterMessage(filter));
        });
        addButton(submitButton);

        item = new TextFieldWidget(font, guiLeft + 30, guiTop + 18, 138, 16, StringTextComponent.EMPTY);
        item.setTextColor(TextFormatting.WHITE.getColor());
        item.setEnableBackgroundDrawing(true);
        item.setMaxStringLength(1024);
        if (filter.getTag() != null) {
            if (filter.getTag() instanceof SingleElementTag) {
                item.setText(filter.getTag().getName().toString());
            } else {
                item.setText("#" + filter.getTag().getName().toString());
            }
        }
        item.setResponder(this::onItemTextChanged);
        item.setValidator(s -> {
            if (s.startsWith("#")) {
                s = s.substring(1);
            }
            return ResourceLocation.isResouceNameValid(s);
        });
        addButton(item);

        nbt = new TextFieldWidget(font, guiLeft + 8, guiTop + 50, 160, 16, StringTextComponent.EMPTY);
        nbt.setTextColor(TextFormatting.WHITE.getColor());
        nbt.setEnableBackgroundDrawing(true);
        nbt.setMaxStringLength(1024);
        nbt.setText(filter.getMetadata() != null ? filter.getMetadata().toString() : "");
        nbt.setResponder(this::onNbtTextChanged);
        nbt.visible = hasNBT();

        addButton(nbt);

        nbtButton.active = filter.getMetadata() != null;

        itemHoverArea = new HoverArea(8, 18, 16, 16, () -> {
            List<ITextComponent> tooltip = new ArrayList<>();

            AbstractStack<?> stack = FilterList.getStack(filter);
            if (stack != null) {
                tooltip = stack.getTooltip(this);
                if (filter.getTag() != null && !(filter.getTag() instanceof SingleElementTag)) {
                    tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.accepts_tag", new StringTextComponent(filter.getTag().getName().toString()).mergeStyle(TextFormatting.BLUE)).mergeStyle(TextFormatting.GRAY));
                }
            }
            return tooltip.stream().map(ITextComponent::func_241878_f).collect(Collectors.toList());
        });
        hoverAreas.add(itemHoverArea);
        itemTextHoverArea = new HoverArea(29, 17, 140, 18, () -> {
            List<ITextComponent> tooltip = new ArrayList<>();
            tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.item_tag.description"));
            return tooltip.stream().map(ITextComponent::func_241878_f).collect(Collectors.toList());
        });
        hoverAreas.add(itemTextHoverArea);
        nbtTextHoverArea = new HoverArea(7, 49, 162, 18, () -> {
            List<ITextComponent> tooltip = new ArrayList<>();
            if (hasNBT()) {
                tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.nbt_string.description"));
            } else {
                tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.nbt_string.no_nbt"));
            }
            return tooltip.stream().map(ITextComponent::func_241878_f).collect(Collectors.toList());
        });
        hoverAreas.add(nbtTextHoverArea);
        exactNBTHoverArea = new HoverArea(126, 82, 20, 20, () -> {
            List<ITextComponent> tooltip = new ArrayList<>();
            if (filter.isExactMetadata()) {
                tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.nbt.exact"));
            } else {
                tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.nbt.not_exact"));
            }
            return tooltip.stream().map(ITextComponent::func_241878_f).collect(Collectors.toList());
        });
        hoverAreas.add(exactNBTHoverArea);
        invertHoverArea = new HoverArea(150, 82, 20, 20, () -> {
            List<ITextComponent> tooltip = new ArrayList<>();
            if (filter.isInvert()) {
                tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.inverted"));
            } else {
                tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.not_inverted"));
            }
            return tooltip.stream().map(ITextComponent::func_241878_f).collect(Collectors.toList());
        });
        hoverAreas.add(invertHoverArea);
        destinationHoverArea = new HoverArea(8, 83, 16, 16, () -> {
            List<ITextComponent> tooltip = new ArrayList<>();
            tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.destination.description"));
            if (filter.getDestination() != null) {
                tooltip.add(new TranslationTextComponent("tooltip.pipez.filter.destination.click_to_remove").mergeStyle(TextFormatting.GRAY));
            }
            return tooltip.stream().map(ITextComponent::func_241878_f).collect(Collectors.toList());
        });
        hoverAreas.add(destinationHoverArea);

        destinationTextHoverArea = new HoverArea(25, 82, 96, 18, () -> {
            List<ITextComponent> tooltip = new ArrayList<>();
            if (filter.getDestination() != null) {
                DirectionalPosition dst = filter.getDestination();
                tooltip.add(new TranslationTextComponent("tooltip.pipez.filter_destination_tool.destination", number(dst.getPos().getX()), number(dst.getPos().getY()), number(dst.getPos().getZ()), new TranslationTextComponent("message.pipez.direction." + dst.getDirection().getName2()).mergeStyle(TextFormatting.DARK_GREEN)));
            }
            return tooltip.stream().map(ITextComponent::func_241878_f).collect(Collectors.toList());
        });
        hoverAreas.add(destinationTextHoverArea);
    }

    private boolean hasNBT() {
        return !(filter instanceof GasFilter);
    }

    @Override
    public void tick() {
        super.tick();
        submitButton.active = filter.getTag() != null;
    }

    public void onItemTextChanged(String text) {
        if (text.trim().isEmpty()) {
            nbt.setTextColor(TextFormatting.WHITE.getColor());
            return;
        }

        if (filter instanceof ItemFilter) {
            ITag.INamedTag tag = TagUtils.getItem(text, true);
            filter.setTag(tag);
            if (filter.getTag() == null) {
                item.setTextColor(TextFormatting.DARK_RED.getColor());
            } else {
                item.setTextColor(TextFormatting.WHITE.getColor());
            }
        } else if (filter instanceof FluidFilter) {
            ITag.INamedTag tag = TagUtils.getFluid(text, true);
            filter.setTag(tag);
            if (filter.getTag() == null) {
                item.setTextColor(TextFormatting.DARK_RED.getColor());
            } else {
                item.setTextColor(TextFormatting.WHITE.getColor());
            }
        } else if (filter instanceof GasFilter) {
            ITag.INamedTag tag = GasUtils.getGas(text, true);
            filter.setTag(tag);
            if (filter.getTag() == null) {
                item.setTextColor(TextFormatting.DARK_RED.getColor());
            } else {
                item.setTextColor(TextFormatting.WHITE.getColor());
            }
        }
    }

    public void onNbtTextChanged(String text) {
        if (text.trim().isEmpty()) {
            nbt.setTextColor(TextFormatting.WHITE.getColor());
            nbtButton.active = false;
            filter.setExactMetadata(false);
            return;
        }
        nbtButton.active = true;
        try {
            filter.setMetadata(JsonToNBT.getTagFromJson(text));
            nbt.setTextColor(TextFormatting.WHITE.getColor());
        } catch (CommandSyntaxException e) {
            nbt.setTextColor(TextFormatting.DARK_RED.getColor());
            filter.setMetadata(null);
        }
    }

    public void onInsertStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (filter instanceof ItemFilter) {
            item.setText(stack.getItem().getRegistryName().toString());
            if (stack.hasTag()) {
                nbt.setText(stack.getTag().toString());
            } else {
                nbt.setText("");
            }
        } else if (filter instanceof FluidFilter) {
            FluidUtil.getFluidContained(stack).ifPresent(s -> {
                item.setText(s.getFluid().getRegistryName().toString());
                if (s.hasTag()) {
                    nbt.setText(s.getTag().toString());
                } else {
                    nbt.setText("");
                }
            });
        } else if (filter instanceof GasFilter) {
            GasStack gas = GasUtils.getGasContained(stack);
            if (gas != null) {
                item.setText(gas.getType().getRegistryName().toString());
                nbt.setText("");
            }
        }
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
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        font.func_243248_b(matrixStack, new TranslationTextComponent("message.pipez.filter.item_tag"), 8, 7, FONT_COLOR);
        font.func_243248_b(matrixStack, new TranslationTextComponent("message.pipez.filter.nbt_string"), 8, 39, FONT_COLOR);
        font.func_243248_b(matrixStack, new TranslationTextComponent("message.pipez.filter.destination"), 8, 71, FONT_COLOR);
        font.func_243248_b(matrixStack, playerInventory.getDisplayName(), 8, (float) (ySize - 96 + 3), FONT_COLOR);

        drawHoverAreas(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        AbstractStack<?> stack = FilterList.getStack(filter);
        if (stack != null) {
            stack.render(matrixStack, guiLeft + 8, guiTop + 18);
        }

        matrixStack.push();
        matrixStack.translate(guiLeft + 31, guiTop + 89, 0D);
        matrixStack.scale(0.5F, 0.5F, 1F);
        if (filter.getDestination() != null) {
            DirectionalPosition dst = filter.getDestination();
            font.func_243248_b(matrixStack, new TranslationTextComponent("message.pipez.filter_destination_tool.destination", number(dst.getPos().getX()), number(dst.getPos().getY()), number(dst.getPos().getZ()), new StringTextComponent(String.valueOf(dst.getDirection().name().charAt(0))).mergeStyle(TextFormatting.DARK_GREEN)), 0, 0, 0xFFFFFF);
        } else {
            font.func_243248_b(matrixStack, new TranslationTextComponent("message.pipez.filter_destination_tool.destination.any"), 0, 0, 0xFFFFFF);
        }
        matrixStack.pop();

        if (itemHoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
            drawHoverSlot(matrixStack, guiLeft + 8, guiTop + 18);
        }
        if (destinationHoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
            drawHoverSlot(matrixStack, guiLeft + 8, guiTop + 83);
        }
    }

    private IFormattableTextComponent number(int num) {
        return new StringTextComponent(String.valueOf(num)).mergeStyle(TextFormatting.DARK_GREEN);
    }

    private void drawHoverSlot(MatrixStack matrixStack, int posX, int posY) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        this.fillGradient(matrixStack, posX, posY, posX + 16, posY + 16, slotColor, -2130706433);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (itemHoverArea.isHovered(guiLeft, guiTop, (int) mouseX, (int) mouseY)) {
            onInsertStack(minecraft.player.inventory.getItemStack());
            return true;
        }
        if (destinationHoverArea.isHovered(guiLeft, guiTop, (int) mouseX, (int) mouseY)) {
            onInsertDestination(minecraft.player.inventory.getItemStack());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeScreen();
            return true;
        }

        return item.keyPressed(key, scanCode, modifiers) ||
                item.canWrite() ||
                nbt.keyPressed(key, scanCode, modifiers) ||
                nbt.canWrite() || super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void resize(Minecraft mc, int x, int y) {
        String itemTxt = item.getText();
        String nbtTxt = nbt.getText();
        init(mc, x, y);
        item.setText(itemTxt);
        nbt.setText(nbtTxt);
    }

}
