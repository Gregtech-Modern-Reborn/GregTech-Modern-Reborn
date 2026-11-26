package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Right-side functional color legend for {@link PatternPreviewWidget}.
 * Displays only categories present in the current multiblock view and lets players click a category to solo it.
 */
@OnlyIn(Dist.CLIENT)
public class PatternHighlightLegendWidget extends Widget {

    private static final int HEADER_HEIGHT = 15;
    private static final int ROW_HEIGHT = 13;
    private static final int SCROLLBAR_WIDTH = 3;
    private static final float TEXT_SCALE = 0.78f;

    private record Entry(PatternBlockCategory category, int count) {}

    private final List<Entry> entries = new ArrayList<>();
    private final Consumer<@Nullable PatternBlockCategory> onSoloChanged;

    @Nullable
    private PatternBlockCategory soloCategory;
    private int scrollOffset;

    public PatternHighlightLegendWidget(int x, int y, int width, int height,
                                        Consumer<@Nullable PatternBlockCategory> onSoloChanged) {
        super(x, y, width, height);
        this.onSoloChanged = onSoloChanged;
        setClientSideWidget();
    }

    public void updateEntries(@NotNull Map<PatternBlockCategory, Integer> counts,
                              @Nullable PatternBlockCategory currentSolo) {
        entries.clear();
        for (PatternBlockCategory category : PatternBlockCategory.values()) {
            int count = counts.getOrDefault(category, 0);
            if (count > 0) {
                entries.add(new Entry(category, count));
            }
        }
        this.soloCategory = currentSolo;
        clampScrollOffset();
    }

    public void setSoloCategory(@Nullable PatternBlockCategory soloCategory) {
        this.soloCategory = soloCategory;
    }

    private int getMaxVisibleRows() {
        return Math.max(1, (getSizeHeight() - HEADER_HEIGHT - 3) / ROW_HEIGHT);
    }

    private int getMaxScrollOffset() {
        return Math.max(0, entries.size() - getMaxVisibleRows());
    }

    private void clampScrollOffset() {
        scrollOffset = Mth.clamp(scrollOffset, 0, getMaxScrollOffset());
    }

    private @Nullable Entry getHoveredEntry(double mouseX, double mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        int listTop = pos.y + HEADER_HEIGHT + 1;
        int listLeft = pos.x + 2;
        int listRight = pos.x + size.width - 2;
        if (mouseX < listLeft || mouseX >= listRight || mouseY < listTop) {
            return null;
        }
        int visibleRows = getMaxVisibleRows();
        int rowIndex = (int) ((mouseY - listTop) / ROW_HEIGHT);
        if (rowIndex < 0 || rowIndex >= visibleRows) {
            return null;
        }
        int entryIndex = scrollOffset + rowIndex;
        if (entryIndex < 0 || entryIndex >= entries.size()) {
            return null;
        }
        return entries.get(entryIndex);
    }

    private boolean isMouseOverHeader(double mouseX, double mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return mouseX >= pos.x + 1 && mouseX < pos.x + size.width - 1 &&
                mouseY >= pos.y + 1 && mouseY < pos.y + HEADER_HEIGHT;
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        Position pos = getPosition();
        Size size = getSize();
        int x = pos.x;
        int y = pos.y;
        int w = size.width;
        int h = size.height;

        // Panel backdrop and crisp outer border
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, 0xD8202228);
        DrawerHelper.drawBorder(graphics, x, y, w, h, 0xFF565B66, 1);

        // Header bar
        boolean headerHovered = soloCategory != null && isMouseOverHeader(mouseX, mouseY);
        int headerBg = soloCategory != null ? (headerHovered ? 0xFF47363A : 0xFF392B2F) : 0xFF2A2D35;
        DrawerHelper.drawSolidRect(graphics, x + 1, y + 1, w - 2, HEADER_HEIGHT - 1, headerBg);
        DrawerHelper.drawSolidRect(graphics, x + 1, y + HEADER_HEIGHT, w - 2, 1, 0xFF4A4E58);

        Font font = Minecraft.getInstance().font;
        String headerKey = soloCategory != null ?
                "gtceu.jei.multiblock_info.legend.solo_reset" :
                "gtceu.jei.multiblock_info.legend.title";
        String headerText = Component.translatable(headerKey).getString();
        int maxHeaderTextWidth = (int) ((w - 8) / TEXT_SCALE);
        String clippedHeader = font.plainSubstrByWidth(headerText, maxHeaderTextWidth);
        int headerColor = soloCategory != null ? 0xFFFFD54F : 0xFFEAECEF;
        drawScaledText(graphics, font, clippedHeader, x + 4, y + 4, headerColor);

        // Category rows
        int visibleRows = getMaxVisibleRows();
        boolean hasScrollbar = entries.size() > visibleRows;
        int rowWidth = w - 4 - (hasScrollbar ? SCROLLBAR_WIDTH + 1 : 0);
        int listTop = y + HEADER_HEIGHT + 1;
        Entry hovered = getHoveredEntry(mouseX, mouseY);

        int end = Math.min(entries.size(), scrollOffset + visibleRows);
        for (int i = scrollOffset; i < end; i++) {
            Entry entry = entries.get(i);
            int rowX = x + 2;
            int rowY = listTop + (i - scrollOffset) * ROW_HEIGHT;

            boolean isSolo = soloCategory == entry.category();
            boolean isDimmed = soloCategory != null && !isSolo;
            boolean isHovered = hovered == entry;

            if (isSolo) {
                DrawerHelper.drawSolidRect(graphics, rowX, rowY, rowWidth, ROW_HEIGHT - 1, 0x38FFFFFF);
                DrawerHelper.drawBorder(graphics, rowX, rowY, rowWidth, ROW_HEIGHT - 1,
                        entry.category().getArgbColor(), 1);
            } else if (isHovered) {
                DrawerHelper.drawSolidRect(graphics, rowX, rowY, rowWidth, ROW_HEIGHT - 1, 0x28FFFFFF);
            }

            // Color swatch
            int swatchX = rowX + 2;
            int swatchY = rowY + 2;
            int swatchColor = isDimmed ? dimColor(entry.category().getArgbColor()) : entry.category().getArgbColor();
            DrawerHelper.drawSolidRect(graphics, swatchX, swatchY, 7, 7, 0xFF101216);
            DrawerHelper.drawSolidRect(graphics, swatchX + 1, swatchY + 1, 5, 5, swatchColor);

            // Count (right-aligned)
            String countStr = Integer.toString(entry.count());
            int countScaledWidth = Mth.ceil(font.width(countStr) * TEXT_SCALE);
            int countX = rowX + rowWidth - 2 - countScaledWidth;
            int countColor = isSolo ? 0xFFFFFFFF : (isDimmed ? 0xFF6B707A : 0xFFB8BDC7);
            drawScaledText(graphics, font, countStr, countX, rowY + 3, countColor);

            // Category name (left-aligned, clipped before count)
            int nameX = swatchX + 9;
            int availableNamePx = Math.max(8, countX - nameX - 2);
            int maxNameTextWidth = (int) (availableNamePx / TEXT_SCALE);
            String label = Component.translatable(entry.category().getLangKey()).getString();
            String clippedLabel = font.plainSubstrByWidth(label, maxNameTextWidth);
            int textColor = isSolo ? 0xFFFFFFFF : (isDimmed ? 0xFF777C86 : 0xFFE6E8EC);
            drawScaledText(graphics, font, clippedLabel, nameX, rowY + 3, textColor);
        }

        // Scrollbar when more categories exist than fit vertically
        if (hasScrollbar) {
            int trackX = x + w - SCROLLBAR_WIDTH - 1;
            int trackY = listTop;
            int trackH = visibleRows * ROW_HEIGHT - 1;
            DrawerHelper.drawSolidRect(graphics, trackX, trackY, SCROLLBAR_WIDTH, trackH, 0xFF16181D);

            int maxScroll = getMaxScrollOffset();
            int thumbH = Math.max(10, (trackH * visibleRows) / entries.size());
            int thumbY = trackY + (maxScroll == 0 ? 0 : ((trackH - thumbH) * scrollOffset) / maxScroll);
            DrawerHelper.drawSolidRect(graphics, trackX, thumbY, SCROLLBAR_WIDTH, thumbH, 0xFF7D8596);
        }
    }

    @Override
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        if (gui == null || gui.getModularUIGui() == null || !isMouseOverElement(mouseX, mouseY)) {
            return;
        }
        Entry hovered = getHoveredEntry(mouseX, mouseY);
        if (hovered != null) {
            List<Component> tips = new ArrayList<>(3);
            tips.add(Component.translatable(hovered.category().getLangKey())
                    .withStyle(style -> style.withColor(hovered.category().getRgbColor())));
            tips.add(Component.translatable("gtceu.jei.multiblock_info.legend.count", hovered.count())
                    .withStyle(ChatFormatting.GRAY));
            String hintKey = soloCategory == hovered.category() ?
                    "gtceu.jei.multiblock_info.legend.click_all" :
                    "gtceu.jei.multiblock_info.legend.click_solo";
            tips.add(Component.translatable(hintKey).withStyle(ChatFormatting.DARK_GRAY));
            gui.getModularUIGui().setHoverTooltip(tips, ItemStack.EMPTY, null, null);
        } else if (soloCategory != null && isMouseOverHeader(mouseX, mouseY)) {
            gui.getModularUIGui().setHoverTooltip(
                    List.of(Component.translatable("gtceu.jei.multiblock_info.legend.click_all")),
                    ItemStack.EMPTY, null, null);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible() || !isActive() || button != 0 || !isMouseOverElement(mouseX, mouseY)) {
            return false;
        }
        if (soloCategory != null && isMouseOverHeader(mouseX, mouseY)) {
            playButtonClickSound();
            onSoloChanged.accept(null);
            return true;
        }
        Entry clicked = getHoveredEntry(mouseX, mouseY);
        if (clicked != null) {
            playButtonClickSound();
            PatternBlockCategory nextSolo = soloCategory == clicked.category() ? null : clicked.category();
            onSoloChanged.accept(nextSolo);
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isVisible() || !isActive() || !isMouseOverElement(mouseX, mouseY)) {
            return false;
        }
        int maxScroll = getMaxScrollOffset();
        if (maxScroll <= 0) {
            return false;
        }
        scrollOffset = Mth.clamp(scrollOffset + (wheelDelta > 0 ? -1 : 1), 0, maxScroll);
        return true;
    }

    private static void drawScaledText(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static int dimColor(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = ((argb >>> 16) & 0xFF) / 2;
        int g = ((argb >>> 8) & 0xFF) / 2;
        int b = (argb & 0xFF) / 2;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
