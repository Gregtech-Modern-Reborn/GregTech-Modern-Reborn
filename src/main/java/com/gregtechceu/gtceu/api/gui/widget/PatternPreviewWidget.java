package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemStackHandler;

import com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.BlockPosFace;
import com.lowdragmc.lowdraglib.utils.ItemStackKey;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.emi.emi.screen.RecipeScreen;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.shedaniel.rei.impl.client.gui.screen.AbstractDisplayViewingScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class PatternPreviewWidget extends WidgetGroup {

    public static final int PREVIEW_WIDTH = 160;
    public static final int LEGEND_WIDTH = 86;
    public static final int WIDGET_WIDTH = PREVIEW_WIDTH + LEGEND_WIDTH;
    public static final int WIDGET_HEIGHT = 160;

    private boolean isLoaded;
    private static TrackedDummyWorld LEVEL;

    private static final int REGION_SIZE = 512;
    private static int LAST_OFFSET_INDEX = 0;

    private static final Map<MultiblockMachineDefinition, MBPattern[]> CACHE = new HashMap<>();

    private final PreviewSceneWidget sceneWidget;
    private final DraggableScrollableWidgetGroup scrollableWidgetGroup;
    private final PatternHighlightLegendWidget legendWidget;
    private final ButtonWidget pageButton;
    private final ButtonWidget layerButton;
    private final ButtonWidget highlightButton;

    public final MultiblockMachineDefinition controllerDefinition;
    public final MBPattern[] patterns;

    private final List<SimplePredicate> predicates;

    private boolean isHighLight;
    @Nullable
    private PatternBlockCategory soloCategory;
    private int index;
    public int layer;

    private SlotWidget[] slotWidgets;
    private SlotWidget[] candidates;

    protected PatternPreviewWidget(MultiblockMachineDefinition controllerDefinition) {
        super(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT);

        setClientSideWidget();

        this.controllerDefinition = controllerDefinition;
        predicates = new ArrayList<>();

        layer = -1;
        isHighLight = false;
        soloCategory = null;

        sceneWidget = new PreviewSceneWidget(3, 3, 150, 150, LEVEL);
        sceneWidget.setOnSelected(this::onPosSelected);
        sceneWidget.setRenderFacing(false);

        addWidget(sceneWidget);

        scrollableWidgetGroup = new DraggableScrollableWidgetGroup(3, 132, 154, 22)
                .setXScrollBarHeight(4)
                .setXBarStyle(GuiTextures.SLIDER_BACKGROUND, GuiTextures.BUTTON)
                .setScrollable(true)
                .setDraggable(true);

        scrollableWidgetGroup.setScrollWheelDirection(
                DraggableScrollableWidgetGroup.ScrollWheelDirection.HORIZONTAL);

        addWidget(scrollableWidgetGroup);
        if (ConfigHolder.INSTANCE.client.useVBO) {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(sceneWidget::useCacheBuffer);
            } else {
                sceneWidget.useCacheBuffer();
            }
        }
        addWidget(new ImageWidget(
                3, 3, 154, 10,
                new TextTexture(controllerDefinition.getDescriptionId(), -1)
                        .setType(TextTexture.TextType.ROLL)
                        .setWidth(154)
                        .setDropShadow(true)));

        this.patterns = CACHE.computeIfAbsent(controllerDefinition, definition -> {

            HashSet<ItemStackKey> drops = new HashSet<>();
            drops.add(new ItemStackKey(controllerDefinition.asStack()));

            return controllerDefinition.getMatchingShapes()
                    .stream()
                    .map(it -> initializePattern(it, drops))
                    .filter(Objects::nonNull)
                    .toArray(MBPattern[]::new);

        });

        pageButton = new ButtonWidget(
                138, 30, 18, 18,
                new GuiTextureGroup(
                        ColorPattern.T_GRAY.rectTexture(),
                        new TextTexture("1").setSupplier(() -> "P:" + index)),
                (x) -> setPage((index + 1 >= patterns.length) ? 0 : index + 1)).setHoverBorderTexture(1, -1);
        addWidget(pageButton);

        layerButton = new ButtonWidget(
                138, 50, 18, 18,
                new GuiTextureGroup(
                        ColorPattern.T_GRAY.rectTexture(),
                        new TextTexture("1").setSupplier(
                                () -> layer >= 0 ? "L:" + layer : "ALL")),
                cd -> updateLayer()).setHoverBorderTexture(1, -1);
        addWidget(layerButton);

        highlightButton = new ButtonWidget(
                138, 70, 18, 18,
                new GuiTextureGroup(
                        ColorPattern.T_GRAY.rectTexture(),
                        new TextTexture("1").setSupplier(
                                () -> isHighLight ? "ON" : "OFF")),
                cd -> updateHighLight()).setHoverBorderTexture(1, -1);
        addWidget(highlightButton);

        // Added AFTER scrollableWidgetGroup so JEI ProxyRecipeWidget flat slot indices stay invariant
        legendWidget = new PatternHighlightLegendWidget(160, 3, 83, 154, this::onSoloCategoryChanged);
        legendWidget.setVisible(false);
        legendWidget.setActive(false);
        addWidget(legendWidget);

        setPage(0);
        refreshControlTooltips();
    }

    private void updateHighLight() {
        isHighLight = !isHighLight;
        if (!isHighLight) {
            soloCategory = null;
        }
        legendWidget.setVisible(isHighLight);
        legendWidget.setActive(isHighLight);
        updateHighlightButtonVisual();
        refreshHighlightState();
        refreshControlTooltips();
    }

    private void onSoloCategoryChanged(@Nullable PatternBlockCategory category) {
        soloCategory = category;
        legendWidget.setSoloCategory(soloCategory);
        sceneWidget.markHighlightDirty();
    }

    private void updateHighlightButtonVisual() {
        TextTexture label = new TextTexture("1").setSupplier(() -> isHighLight ? "ON" : "OFF");
        if (isHighLight) {
            highlightButton.setButtonTexture(
                    ColorPattern.T_GRAY.rectTexture(),
                    new ColorBorderTexture(1, 0xFF00E676),
                    label);
        } else {
            highlightButton.setButtonTexture(
                    ColorPattern.T_GRAY.rectTexture(),
                    label);
        }
    }

    private void refreshControlTooltips() {
        int totalPages = Math.max(1, patterns.length);
        pageButton.setHoverTooltips(Component.translatable(
                "gtceu.jei.multiblock_info.page.tooltip", index + 1, totalPages));

        if (layer >= 0 && index >= 0 && index < patterns.length) {
            MBPattern pattern = patterns[index];
            int totalLayers = Math.max(1, pattern.maxY - pattern.minY + 1);
            layerButton.setHoverTooltips(Component.translatable(
                    "gtceu.jei.multiblock_info.layer.single.tooltip", layer + 1, totalLayers));
        } else {
            layerButton.setHoverTooltips(Component.translatable(
                    "gtceu.jei.multiblock_info.layer.all.tooltip"));
        }

        highlightButton.setHoverTooltips(Component.translatable(isHighLight ?
                "gtceu.jei.multiblock_info.highlight.on.tooltip" :
                "gtceu.jei.multiblock_info.highlight.off.tooltip"));
    }

    private void refreshHighlightState() {
        sceneWidget.rebuildCategoryCache();
        Map<PatternBlockCategory, Integer> counts = sceneWidget.getCategoryCounts();
        if (soloCategory != null && counts.getOrDefault(soloCategory, 0) == 0) {
            soloCategory = null;
        }
        legendWidget.updateEntries(counts, soloCategory);
        sceneWidget.markHighlightDirty();
    }

    private void updateLayer() {
        if (index < 0 || index >= patterns.length) return;
        MBPattern pattern = patterns[index];

        if (layer + 1 >= -1 && layer + 1 <= pattern.maxY - pattern.minY) {
            layer++;
            if (pattern.controllerBase.isFormed()) {
                onFormedSwitch(false);
            }
        } else {
            layer = -1;
            if (!pattern.controllerBase.isFormed()) {
                onFormedSwitch(true);
            }
        }

        setupScene(pattern);
        refreshHighlightState();
        refreshControlTooltips();
    }

    public static BlockPos locateNextRegion() {
        int currentIndex = LAST_OFFSET_INDEX++;

        int x = 0, z = 0;

        if (currentIndex > 0) {

            int v = (int) (Mth.sqrt(currentIndex + 0.25f) - 0.5f);
            int nextV = v + 1;

            int spiralBaseIndex = v * nextV;

            int flipFlop = (v & 1) * 2 - 1;

            int offset = flipFlop * nextV / 2;

            x += offset;
            z += offset;

            int cornerIndex = spiralBaseIndex + nextV;

            if (currentIndex < cornerIndex) {
                x -= flipFlop * (currentIndex - spiralBaseIndex + 1);
            } else {
                x -= flipFlop * nextV;
                z -= flipFlop * (currentIndex - cornerIndex + 1);
            }
        }

        return new BlockPos(x * REGION_SIZE, 50, z * REGION_SIZE);
    }

    private void loadControllerFormed(Collection<BlockPos> positions, IMultiController controllerBase) {
        BlockPattern pattern = controllerBase.getPattern();

        if (pattern != null && pattern.checkPatternAt(controllerBase.getMultiblockState(), true)) {
            controllerBase.onStructureFormed();
        }

        if (controllerBase.isFormed()) {

            LongSet modelDisabled = controllerBase.getMultiblockState()
                    .getMatchContext()
                    .getOrDefault("renderMask", LongSets.EMPTY_SET);

            if (!modelDisabled.isEmpty()) {

                positions = new HashSet<>(positions);

                positions.removeIf(pos -> modelDisabled.contains(pos.asLong()));
            }

            sceneWidget.setRenderedCore(positions, null);

        } else {

            GTCEu.LOGGER.warn(
                    "Pattern formed checking failed: {}",
                    controllerBase.self().getDefinition());
        }
    }

    private Map<ItemStackKey, PartInfo> gatherBlockDrops(Map<BlockPos, BlockInfo> blocks) {
        Map<ItemStackKey, PartInfo> partsMap = new Object2ObjectOpenHashMap<>();

        for (Map.Entry<BlockPos, BlockInfo> entry : blocks.entrySet()) {

            BlockPos pos = entry.getKey();

            BlockState blockState = LEVEL.getBlockState(pos);

            ItemStack itemStack = blockState.getBlock()
                    .getCloneItemStack(LEVEL, pos, blockState);

            if (itemStack.isEmpty() && !blockState.getFluidState().isEmpty()) {

                Fluid fluid = blockState.getFluidState().getType();
                itemStack = fluid.getBucket().getDefaultInstance();
            }

            ItemStackKey key = new ItemStackKey(itemStack);

            partsMap.computeIfAbsent(key,
                    k -> new PartInfo(k, entry.getValue())).amount++;
        }

        return partsMap;
    }

    private MBPattern initializePattern(MultiblockShapeInfo shapeInfo, HashSet<ItemStackKey> blockDrops) {
        Map<BlockPos, BlockInfo> blockMap = new HashMap<>();

        IMultiController controllerBase = null;

        BlockPos multiPos = locateNextRegion();

        BlockInfo[][][] blocks = shapeInfo.getBlocks();

        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];

            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];

                for (int z = 0; z < column.length; z++) {

                    BlockState blockState = column[z].getBlockState();
                    BlockPos pos = multiPos.offset(x, y, z);

                    if (column[z].getBlockEntity(pos) instanceof IMachineBlockEntity holder &&
                            holder.getMetaMachine() instanceof IMultiController controller) {

                        holder.getSelf().setLevel(LEVEL);
                        controllerBase = controller;
                    }

                    blockMap.put(pos, BlockInfo.fromBlockState(blockState));
                }
            }
        }

        LEVEL.addBlocks(blockMap);

        if (controllerBase != null) {
            LEVEL.setInnerBlockEntity(controllerBase.self().holder.getSelf());
        }

        Map<ItemStackKey, PartInfo> parts = gatherBlockDrops(blockMap);

        blockDrops.addAll(parts.keySet());

        Map<BlockPos, TraceabilityPredicate> predicateMap = new HashMap<>();

        if (controllerBase != null) {

            loadControllerFormed(blockMap.keySet(), controllerBase);

            Map<BlockPos, TraceabilityPredicate> loaded = controllerBase.getMultiblockState().getMatchContext()
                    .get("predicates");

            if (loaded != null) {
                predicateMap = loaded;
            }
        }

        return controllerBase == null ? null :
                new MBPattern(
                        blockMap,
                        parts.values().stream()
                                .sorted((one, two) -> {

                                    if (one.isController) return -1;
                                    if (two.isController) return +1;

                                    if (one.isTile && !two.isTile) return -1;
                                    if (two.isTile && !one.isTile) return +1;

                                    if (one.blockId != two.blockId)
                                        return two.blockId - one.blockId;

                                    return two.amount - one.amount;

                                })
                                .map(PartInfo::getItemStack)
                                .filter(list -> !list.isEmpty())
                                .collect(Collectors.toList()),
                        predicateMap,
                        controllerBase);
    }

    private void setupScene(MBPattern pattern) {
        Stream<BlockPos> stream = pattern.blockMap.keySet().stream()
                .filter(pos -> layer == -1 || layer + pattern.minY == pos.getY());

        if (pattern.controllerBase.isFormed()) {

            LongSet disabled = pattern.controllerBase.getMultiblockState()
                    .getMatchContext()
                    .getOrDefault("renderMask", LongSets.EMPTY_SET);

            if (!disabled.isEmpty()) {
                stream = stream.filter(pos -> !disabled.contains(pos.asLong()));
            }
        }

        sceneWidget.setRenderedCore(stream.toList(), null);
    }

    public static PatternPreviewWidget getPatternWidget(
                                                        MultiblockMachineDefinition controllerDefinition) {
        if (LEVEL == null) {

            if (Minecraft.getInstance().level == null) {
                GTCEu.LOGGER.error("Try to init pattern previews before level load");
                throw new IllegalStateException();
            }

            LEVEL = new TrackedDummyWorld();
        }

        return new PatternPreviewWidget(controllerDefinition);
    }

    public void setPage(int index) {
        if (index >= patterns.length || index < 0) return;

        this.index = index;
        this.layer = -1;
        this.soloCategory = null;

        MBPattern pattern = patterns[index];

        setupScene(pattern);
        refreshHighlightState();
        refreshControlTooltips();

        if (slotWidgets != null) {
            for (SlotWidget s : slotWidgets) {
                scrollableWidgetGroup.removeWidget(s);
            }
        }

        slotWidgets = new SlotWidget[pattern.parts.size()];

        var handler = new CycleItemStackHandler(pattern.parts);

        int xOffset = 0;

        for (int i = 0; i < slotWidgets.length; i++) {

            int padding = 1;

            if (handler.getStackInSlot(i).getCount() / 100000 >= 1) padding = 10;
            else if (handler.getStackInSlot(i).getCount() / 10000 >= 1) padding = 7;
            else if (handler.getStackInSlot(i).getCount() / 1000 >= 1) padding = 4;

            slotWidgets[i] = new PatternPreviewSlotWidget(
                    handler, i, 4 + xOffset + padding, 0, false, false)
                    .setBackgroundTexture(ColorPattern.T_GRAY.rectTexture())
                    .setIngredientIO(IngredientIO.INPUT);

            xOffset += 18 + (2 * padding);

            scrollableWidgetGroup.addWidget(slotWidgets[i]);
        }
    }

    private void onFormedSwitch(boolean isFormed) {
        MBPattern pattern = patterns[index];
        IMultiController controllerBase = pattern.controllerBase;

        if (isFormed) {
            layer = -1;
            loadControllerFormed(pattern.blockMap.keySet(), controllerBase);
        } else {
            sceneWidget.setRenderedCore(pattern.blockMap.keySet(), null);
            controllerBase.onStructureInvalid();
        }

        refreshHighlightState();
    }

    private void onPosSelected(BlockPos pos, Direction facing) {
        // sceneWidget.markHighlightDirty();

        if (index >= patterns.length || index < 0) return;

        TraceabilityPredicate predicate = patterns[index].predicateMap.get(pos);

        if (predicate == null) return;

        predicates.clear();
        predicates.addAll(predicate.common);
        predicates.addAll(predicate.limited);

        predicates.removeIf(p -> p == null || p.candidates == null);

        if (candidates != null) {
            for (SlotWidget s : candidates) {
                removeWidget(s);
            }
        }

        List<List<ItemStack>> stacks = new ArrayList<>();
        List<List<Component>> tips = new ArrayList<>();

        for (SimplePredicate p : predicates) {

            List<ItemStack> list = p.getCandidates();

            if (!list.isEmpty()) {
                stacks.add(list);
                tips.add(p.getToolTips(predicate));
            }
        }

        candidates = new SlotWidget[stacks.size()];
        CycleItemStackHandler handler = new CycleItemStackHandler(stacks);

        int maxCol = Math.max(1, (160 - (((slotWidgets.length - 1) / 9 + 1) * 18) - 35) % 18);

        for (int i = 0; i < stacks.size(); i++) {

            int fi = i;

            candidates[i] = new SlotWidget(
                    handler, i,
                    3 + (i / maxCol) * 18,
                    3 + (i % maxCol) * 18,
                    false, false)
                    .setIngredientIO(IngredientIO.INPUT)
                    .setBackgroundTexture(new ColorRectTexture(0x4fffffff))
                    .setOnAddedTooltips((slot, list) -> list.addAll(tips.get(fi)));

            addWidget(candidates[i]);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (!isLoaded && GTCEu.Mods.isEMILoaded() && Minecraft.getInstance().screen instanceof RecipeScreen) {

            setPage(0);
            isLoaded = true;

        } else if (!isLoaded && GTCEu.Mods.isREILoaded() &&
                Minecraft.getInstance().screen instanceof AbstractDisplayViewingScreen) {

                    setPage(0);
                    isLoaded = true;
                }
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    /* ====================================================== */
    /* =================== VBO SceneWidget ================== */
    /* ====================================================== */

    private final class PreviewSceneWidget extends SceneWidget {

        private static final float FACE_EXPAND = 0.004f;
        private static final float LINE_EXPAND = 0.0055f;

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!this.intractable) {
                return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            } else if (this.dragging && button == 0) {
                this.rotationPitch = (float) ((double) this.rotationPitch + dragX + 360.0);
                this.rotationPitch %= 360.0F;
                this.rotationYaw = (float) Mth.clamp((double) this.rotationYaw + dragY, -89.9, 89.9);
                if (this.renderer != null) {
                    this.renderer.setCameraLookAt(this.center, (double) this.camZoom(),
                            Math.toRadians((double) this.rotationPitch), Math.toRadians((double) this.rotationYaw));
                }
                return false;
            } else if (this.dragging && button == 1) {// 右键情况下
                if (this.renderer == null) return false;

                Vector3f eyePos = new Vector3f(this.renderer.getEyePos());
                Vector3f lookAt = new Vector3f(this.renderer.getLookAt());
                Vector3f worldUp = new Vector3f(this.renderer.getWorldUp());

                float speed = 1.0f;

                // 建议与 zoom 绑定（体验提升非常明显）
                speed *= (float) this.camZoom();

                // forward = lookAt - eyePos
                Vector3f forward = new Vector3f(lookAt).sub(eyePos).normalize();

                // right = forward × worldUp
                Vector3f right = new Vector3f(forward).cross(worldUp).normalize();

                // camera up = right × forward
                Vector3f up = new Vector3f(right).cross(forward).normalize();

                // 移动向量
                Vector3f move = new Vector3f();

                move.add(new Vector3f(right).mul((float) (-dragX / getSizeWidth() * speed)));
                move.add(new Vector3f(up).mul((float) (dragY / getSizeHeight() * speed)));

                eyePos.add(move);
                lookAt.add(move);

                this.center.add(move);

                this.renderer.setCameraLookAt(eyePos, lookAt, worldUp);

                return false;

            } else {
                return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
        }

        private final Map<BlockPos, PatternBlockCategory> categoryCache = new HashMap<>();
        private final EnumMap<PatternBlockCategory, Integer> categoryCounts = new EnumMap<>(PatternBlockCategory.class);

        @Nullable
        private VertexBuffer highlightFaceVbo;
        @Nullable
        private VertexBuffer highlightLineVbo;
        private boolean highlightDirty = true;

        public PreviewSceneWidget(int x, int y, int w, int h, TrackedDummyWorld world) {
            super(x, y, w, h, world);
        }

        public void markHighlightDirty() {
            highlightDirty = true;
        }

        public void rebuildCategoryCache() {
            categoryCache.clear();
            categoryCounts.clear();
            if (core.isEmpty() || index < 0 || index >= patterns.length) {
                return;
            }
            MBPattern pattern = patterns[index];
            Map<TraceabilityPredicate, EnumSet<PatternBlockCategory>> predicateCache = new IdentityHashMap<>();
            for (BlockPos pos : core) {
                BlockInfo blockInfo = pattern.blockMap.get(pos);
                BlockState state = blockInfo != null ? blockInfo.getBlockState() : LEVEL.getBlockState(pos);
                TraceabilityPredicate predicate = pattern.predicateMap.get(pos);
                PatternBlockCategory category = PatternBlockCategory.classify(
                        state, LEVEL.getBlockEntity(pos), predicate, predicateCache);
                if (category != null) {
                    categoryCache.put(pos, category);
                    categoryCounts.merge(category, 1, Integer::sum);
                }
            }
        }

        public Map<PatternBlockCategory, Integer> getCategoryCounts() {
            return categoryCounts;
        }

        private void closeHighlightBuffers() {
            if (highlightFaceVbo != null) {
                highlightFaceVbo.close();
                highlightFaceVbo = null;
            }
            if (highlightLineVbo != null) {
                highlightLineVbo.close();
                highlightLineVbo = null;
            }
            highlightDirty = true;
        }

        @Override
        public void releaseCacheBuffer() {
            super.releaseCacheBuffer();
            closeHighlightBuffers();
        }

        private void rebuildHighlightBuffer() {
            closeHighlightBuffers();
            highlightDirty = false;

            if (!isHighLight || core.isEmpty() || index < 0 || index >= patterns.length) return;

            if (categoryCache.isEmpty()) {
                rebuildCategoryCache();
            }

            Map<BlockPos, PatternBlockCategory> activeBlocks = new HashMap<>();
            for (Map.Entry<BlockPos, PatternBlockCategory> entry : categoryCache.entrySet()) {
                PatternBlockCategory category = entry.getValue();
                if (category == null) continue;
                if (soloCategory != null && category != soloCategory) continue;
                activeBlocks.put(entry.getKey(), category);
            }

            if (activeBlocks.isEmpty()) return;

            boolean isSolo = soloCategory != null;
            BufferBuilder builder = Tesselator.getInstance().getBuilder();

            // 1. Translucent faces VBO (skip internal faces shared with same-category neighbors)
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            int faceQuads = 0;
            for (Map.Entry<BlockPos, PatternBlockCategory> entry : activeBlocks.entrySet()) {
                BlockPos pos = entry.getKey();
                PatternBlockCategory cat = entry.getValue();
                float r = cat.getRed();
                float g = cat.getGreen();
                float b = cat.getBlue();
                float a = cat.getFaceAlpha(isSolo);
                if (a <= 0.0f) continue;

                faceQuads += appendBoxFaces(builder, pos, cat, activeBlocks, r, g, b, a);
            }
            BufferBuilder.RenderedBuffer faceRendered = builder.end();
            if (faceQuads > 0) {
                highlightFaceVbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
                highlightFaceVbo.bind();
                highlightFaceVbo.upload(faceRendered);
                VertexBuffer.unbind();
            } else {
                faceRendered.release();
            }

            // 2. Crisp wireframe outline VBO (1px GL_LINES, immune to JEI sub-viewport scaling)
            builder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            int lineEdges = 0;
            for (Map.Entry<BlockPos, PatternBlockCategory> entry : activeBlocks.entrySet()) {
                BlockPos pos = entry.getKey();
                PatternBlockCategory cat = entry.getValue();
                float r = cat.getRed();
                float g = cat.getGreen();
                float b = cat.getBlue();
                float a = cat.getLineAlpha(isSolo);
                if (a <= 0.0f) continue;

                appendBoxLines(builder, pos, r, g, b, a);
                lineEdges += 12;
            }
            BufferBuilder.RenderedBuffer lineRendered = builder.end();
            if (lineEdges > 0) {
                highlightLineVbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
                highlightLineVbo.bind();
                highlightLineVbo.upload(lineRendered);
                VertexBuffer.unbind();
            } else {
                lineRendered.release();
            }
        }

        private int appendBoxFaces(BufferBuilder builder, BlockPos pos, PatternBlockCategory category,
                                   Map<BlockPos, PatternBlockCategory> activeBlocks,
                                   float r, float g, float b, float a) {
            float x1 = pos.getX() - FACE_EXPAND;
            float y1 = pos.getY() - FACE_EXPAND;
            float z1 = pos.getZ() - FACE_EXPAND;
            float x2 = pos.getX() + 1.0f + FACE_EXPAND;
            float y2 = pos.getY() + 1.0f + FACE_EXPAND;
            float z2 = pos.getZ() + 1.0f + FACE_EXPAND;

            int quads = 0;
            if (activeBlocks.get(pos.south()) != category) {
                addQuad(builder, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, r, g, b, a);
                quads++;
            }
            if (activeBlocks.get(pos.north()) != category) {
                addQuad(builder, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, r, g, b, a);
                quads++;
            }
            if (activeBlocks.get(pos.above()) != category) {
                addQuad(builder, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, r, g, b, a);
                quads++;
            }
            if (activeBlocks.get(pos.below()) != category) {
                addQuad(builder, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r, g, b, a);
                quads++;
            }
            if (activeBlocks.get(pos.east()) != category) {
                addQuad(builder, x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, r, g, b, a);
                quads++;
            }
            if (activeBlocks.get(pos.west()) != category) {
                addQuad(builder, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r, g, b, a);
                quads++;
            }
            return quads;
        }

        private void appendBoxLines(BufferBuilder builder, BlockPos pos, float r, float g, float b, float a) {
            float x1 = pos.getX() - LINE_EXPAND;
            float y1 = pos.getY() - LINE_EXPAND;
            float z1 = pos.getZ() - LINE_EXPAND;
            float x2 = pos.getX() + 1.0f + LINE_EXPAND;
            float y2 = pos.getY() + 1.0f + LINE_EXPAND;
            float z2 = pos.getZ() + 1.0f + LINE_EXPAND;

            // Bottom rectangle
            addLine(builder, x1, y1, z1, x2, y1, z1, r, g, b, a);
            addLine(builder, x2, y1, z1, x2, y1, z2, r, g, b, a);
            addLine(builder, x2, y1, z2, x1, y1, z2, r, g, b, a);
            addLine(builder, x1, y1, z2, x1, y1, z1, r, g, b, a);

            // Top rectangle
            addLine(builder, x1, y2, z1, x2, y2, z1, r, g, b, a);
            addLine(builder, x2, y2, z1, x2, y2, z2, r, g, b, a);
            addLine(builder, x2, y2, z2, x1, y2, z2, r, g, b, a);
            addLine(builder, x1, y2, z2, x1, y2, z1, r, g, b, a);

            // Vertical edges
            addLine(builder, x1, y1, z1, x1, y2, z1, r, g, b, a);
            addLine(builder, x2, y1, z1, x2, y2, z1, r, g, b, a);
            addLine(builder, x2, y1, z2, x2, y2, z2, r, g, b, a);
            addLine(builder, x1, y1, z2, x1, y2, z2, r, g, b, a);
        }

        private void addQuad(BufferBuilder b,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float r, float g, float bl, float a) {
            b.vertex(x1, y1, z1).color(r, g, bl, a).endVertex();
            b.vertex(x2, y2, z2).color(r, g, bl, a).endVertex();
            b.vertex(x3, y3, z3).color(r, g, bl, a).endVertex();
            b.vertex(x4, y4, z4).color(r, g, bl, a).endVertex();
        }

        private void addLine(BufferBuilder b,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float bl, float a) {
            b.vertex(x1, y1, z1).color(r, g, bl, a).endVertex();
            b.vertex(x2, y2, z2).color(r, g, bl, a).endVertex();
        }

        private void renderHighlight() {
            if (!isHighLight) return;

            if (highlightDirty) {
                rebuildHighlightBuffer();
            }

            if (highlightFaceVbo == null && highlightLineVbo == null) return;

            var shader = GameRenderer.getPositionColorShader();
            if (shader == null) return;

            boolean xRay = soloCategory != null;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.depthMask(false);
            if (xRay) {
                RenderSystem.disableDepthTest();
            } else {
                RenderSystem.enableDepthTest();
            }
            RenderSystem.enableCull();

            if (highlightFaceVbo != null) {
                highlightFaceVbo.bind();
                highlightFaceVbo.drawWithShader(
                        RenderSystem.getModelViewMatrix(),
                        RenderSystem.getProjectionMatrix(),
                        shader);
            }

            if (highlightLineVbo != null) {
                highlightLineVbo.bind();
                highlightLineVbo.drawWithShader(
                        RenderSystem.getModelViewMatrix(),
                        RenderSystem.getProjectionMatrix(),
                        shader);
            }

            VertexBuffer.unbind();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }

        @Override
        public void renderBlockOverLay(WorldSceneRenderer renderer) {
            PoseStack poseStack = new PoseStack();
            hoverPosFace = null;
            hoverItem = null;
            renderHighlight();
            if (isMouseOverElement(currentMouseX, currentMouseY)) {
                BlockHitResult hit = renderer.getLastTraceResult();
                if (hit != null) {
                    if (core.contains(hit.getBlockPos())) {
                        hoverPosFace = new BlockPosFace(hit.getBlockPos(), hit.getDirection());
                    } else if (!useOrtho) {
                        Vector3f hitPos = hit.getLocation().toVector3f();
                        Level world = renderer.world;
                        Vec3 eyePos = new Vec3(renderer.getEyePos());
                        hitPos.mul(2); // Double view range to ensure pos can be seen.
                        Vec3 endPos = new Vec3((hitPos.x - eyePos.x), (hitPos.y - eyePos.y), (hitPos.z - eyePos.z));
                        double min = Float.MAX_VALUE;
                        for (BlockPos pos : core) {
                            BlockState blockState = world.getBlockState(pos);
                            if (blockState.getBlock() == Blocks.AIR) {
                                continue;
                            }
                            hit = world.clipWithInteractionOverride(eyePos, endPos, pos,
                                    blockState.getShape(world, pos), blockState);
                            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                                double dist = eyePos.distanceToSqr(hit.getLocation());
                                if (dist < min) {
                                    min = dist;
                                    hoverPosFace = new BlockPosFace(hit.getBlockPos(), hit.getDirection());
                                }
                            }
                        }
                    }
                }
            }
            if (hoverPosFace != null) {
                var state = getDummyWorld().getBlockState(hoverPosFace.pos);
                hoverItem = state.getBlock().getCloneItemStack(getDummyWorld(), hoverPosFace.pos, state);
            }
            BlockPosFace tmp = dragging ? clickPosFace : hoverPosFace;
            if (selectedPosFace != null || tmp != null) {
                if (selectedPosFace != null && renderFacing) {
                    drawFacingBorder(poseStack, selectedPosFace, 0xff00ff00);
                }
                if (tmp != null && !tmp.equals(selectedPosFace) && renderFacing) {
                    drawFacingBorder(poseStack, tmp, 0xffffffff);
                }
            }
            if (selectedPosFace != null && renderSelect) {
                RenderUtils.renderBlockOverLay(poseStack, selectedPosFace.pos, 0.6f, 0, 0, 1.03f);
            }

            if (this.afterWorldRender != null) {
                this.afterWorldRender.accept(this);
            }
        }
    }

    private static class PartInfo {

        final ItemStackKey itemStackKey;

        boolean isController = false;
        boolean isTile = false;

        final int blockId;

        int amount = 0;

        PartInfo(ItemStackKey itemStackKey, BlockInfo blockInfo) {
            this.itemStackKey = itemStackKey;

            this.blockId = Block.getId(blockInfo.getBlockState());

            this.isTile = blockInfo.hasBlockEntity();

            if (blockInfo.getBlockState().getBlock() instanceof MetaMachineBlock block) {
                if (block.definition instanceof MultiblockMachineDefinition) {
                    this.isController = true;
                }
            }
        }

        public List<ItemStack> getItemStack() {
            return Arrays.stream(itemStackKey.getItemStack())
                    .map(stack -> stack.copyWithCount(amount))
                    .filter(item -> !item.isEmpty())
                    .toList();
        }
    }
    /* ====================================================== */

    public static class MBPattern {

        @NotNull
        final List<List<ItemStack>> parts;

        @NotNull
        final Map<BlockPos, TraceabilityPredicate> predicateMap;

        @NotNull
        final Map<BlockPos, BlockInfo> blockMap;

        @NotNull
        final IMultiController controllerBase;

        final int maxY;
        final int minY;

        public MBPattern(@NotNull Map<BlockPos, BlockInfo> blockMap,
                         @NotNull List<List<ItemStack>> parts,
                         @NotNull Map<BlockPos, TraceabilityPredicate> predicateMap,
                         @NotNull IMultiController controllerBase) {
            this.parts = parts;
            this.blockMap = blockMap;
            this.predicateMap = predicateMap;
            this.controllerBase = controllerBase;

            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;

            for (BlockPos pos : blockMap.keySet()) {
                min = Math.min(min, pos.getY());
                max = Math.max(max, pos.getY());
            }

            minY = min;
            maxY = max;
        }
    }
}
