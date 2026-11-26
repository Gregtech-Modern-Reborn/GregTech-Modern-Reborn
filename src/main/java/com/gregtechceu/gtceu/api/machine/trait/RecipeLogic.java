package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamWorkableMachine;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.common.cover.MachineControllerCover;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.RecipeUtils;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class RecipeLogic extends MachineTrait implements IEnhancedManaged, IWorkable, IFancyTooltip {

    public enum Status implements StringRepresentable {

        IDLE("idle"),
        WORKING("working"),
        WAITING("waiting"),
        SUSPEND("suspend");

        @Getter
        private final String serializedName;

        Status(String name) {
            this.serializedName = name;
        }
    }

    public static final EnumProperty<Status> STATUS_PROPERTY = GTMachineModelProperties.RECIPE_LOGIC_STATUS;
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RecipeLogic.class);
    public final int maxLoopCount = 2048;
    public IRecipeLogicMachine machine;
    @Setter
    public List<GTRecipe> lastFailedMatches;
    @Getter
    @Setter
    @Persisted
    // @UpdateListener(methodName = "onStatusSynced")
    @DescSynced
    private boolean isMultiParallelLogic;// This Flag control the "MultiParallel Mode" ON/OFF, when the MultiParallel
    // Mode on,the machine can merge the different recipes to one recipe and run
    // it(Must these recipes are in same recipe type;
    @Persisted
    @Getter
    @Setter
    @DescSynced
    // @UpdateListener(methodName = "onStatusSynced")
    private List<Boolean> ActiveModesList = new ArrayList<>();
    @Getter
    @Setter
    @Persisted
    private int MultiParallelCount = 1;
    @Getter
    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onStatusSynced")
    private Status status = Status.IDLE;

    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onActiveSynced")
    protected boolean isActive;

    @Nullable
    @Getter
    @Persisted
    @DescSynced
    private Component waitingReason = null;
    @Nullable
    @Setter
    private Component lastModifierFailReason = null;
    @Nullable
    @Setter
    private Component lastBeforeWorkingFailReason = null;
    @Nullable
    private Component candidateFailReason = null;
    /**
     * unsafe, it may not be found from {@link RecipeManager}. Do not index it.
     */
    @Nullable
    @Getter
    @Setter
    @Persisted
    @DescSynced
    protected GTRecipe lastRecipe;

    @Getter
    @Persisted
    @DescSynced
    protected int consecutiveRecipes = 0; // Consecutive recipes that have been run
    /**
     * safe, it is the origin recipe before {@link IRecipeLogicMachine#fullModifyRecipe(GTRecipe)}'
     * which can be found
     * from {@link RecipeManager}.
     */
    @Nullable
    @Setter
    @Getter
    @Persisted
    protected GTRecipe lastOriginRecipe;

    @Persisted
    @Getter
    @Setter
    protected int progress;
    @Getter
    @Persisted
    protected int duration;
    @Getter(onMethod_ = @VisibleForTesting)
    protected boolean recipeDirty;
    @Persisted
    @Getter
    protected long totalContinuousRunningTime;
    protected int runAttempt = 0;
    protected int runDelay = 0;
    @Persisted
    @Setter
    @Getter
    protected boolean suspendAfterFinish = false;
    @Getter
    protected final Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches = makeChanceCaches();
    protected TickableSubscription subscription;
    protected Object workingSound;

    public RecipeLogic(IRecipeLogicMachine machine) {
        super(machine.self());
        this.isMultiParallelLogic = false;

        if (machine instanceof MultiblockControllerMachine) {
            this.isMultiParallelLogic = ((MultiblockControllerMachine) machine).getMultiParallelHatch().isPresent();
        }

        // In Default Situation,Use normal parallel;
        this.machine = machine;
        try {
            if (machine.getRecipeTypes() == null) return;
        } catch (Exception e) {
            return;
        }
        if (this.getActiveModesList().isEmpty()) {
            for (int i = 0; i < machine.getRecipeTypes().length; i++) {
                this.ActiveModesList.add(false);
            }
            this.ActiveModesList.set(this.machine.getActiveRecipeType(), true);
        }
    }

    @SuppressWarnings("unused")
    protected void onStatusSynced(Status newValue, Status oldValue) {
        scheduleRenderUpdate();
        updateSound();
    }

    @SuppressWarnings("unused")
    protected void onActiveSynced(boolean newActive, boolean oldActive) {
        scheduleRenderUpdate();
    }

    /**
     * Call it to abort current recipe and reset the first state.
     */
    public void resetRecipeLogic() {
        recipeDirty = false;
        lastRecipe = null;
        lastOriginRecipe = null;

        consecutiveRecipes = 0;
        progress = 0;
        duration = 0;
        isActive = false;
        lastFailedMatches = null;
        lastModifierFailReason = null;
        lastBeforeWorkingFailReason = null;
        candidateFailReason = null;
        if (status != Status.SUSPEND) {
            setStatus(Status.IDLE);
        }
        setWaitingReason(null);
        MultiParallelCount = 1;
        isMultiParallelLogic = false;
        ActiveModesList = new ArrayList<Boolean>();

        for (int i = 0; i < machine.getRecipeTypes().length; i++) {
            if (i == 0) {
                ActiveModesList.add(true);
                continue;
            }
            ActiveModesList.add(false);
        }
        updateTickSubscription();
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        updateTickSubscription();
    }

    public void updateTickSubscription() {
        if (isSuspend() || !machine.isRecipeLogicAvailable()) {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        } else {
            subscription = getMachine().subscribeServerTick(subscription, this::serverTick);
        }
    }

    public double getProgressPercent() {
        return duration == 0 ? 0.0 : progress / (duration * 1.0);
    }

    /**
     * it should be called on the server side restrictively.
     */
    public RecipeManager getRecipeManager() {
        return GTCEu.getMinecraftServer().getRecipeManager();
    }

    public void serverTick() {
        if (!isSuspend()) {

            if (!isIdle() && lastRecipe != null) {
                if (progress < duration) {
                    if (runDelay > 0) {
                        runDelay--;
                    } else {
                        handleRecipeWorking();
                    }
                }
                if (progress >= duration) {
                    onRecipeFinish();
                }
            } else if (lastRecipe != null && !isMultiParallelLogic) {
                findAndHandleRecipe();
            } else if (!machine.keepSubscribing() || getMachine().getOffsetTimer() % 5 == 0) {
                findAndHandleRecipe();

            }
        }
        boolean unsubscribe = false;
        if (isSuspend()) {
            // Machine is paused and can unsubscribe
            unsubscribe = true;
        } else if (lastRecipe == null && isIdle() && !machine.keepSubscribing() && !recipeDirty &&
                lastFailedMatches == null) {
                    // No recipes available and the machine wants to unsubscribe until notified
                    unsubscribe = true;
                }

        if (unsubscribe && subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    protected ActionResult matchRecipe(GTRecipe recipe) {
        return RecipeHelper.matchContents(machine, recipe);
    }

    protected ActionResult checkRecipe(GTRecipe recipe) {
        var conditionResult = RecipeHelper.checkConditions(recipe, this);
        if (!conditionResult.isSuccess()) return conditionResult;

        return matchRecipe(recipe);
    }

    public boolean checkMatchedRecipeAvailable(GTRecipe match) {
        lastModifierFailReason = null;
        var modified = machine.fullModifyRecipe(match);
        if (modified != null) {
            var recipeMatch = checkRecipe(modified);
            if (recipeMatch.isSuccess()) {
                setupRecipe(modified);
            } else {
                recordCandidateFailReason(formatActionFailure(recipeMatch));
            }
            if (lastRecipe != null && getStatus() == Status.WORKING) {
                lastOriginRecipe = match;
                lastFailedMatches = null;
                candidateFailReason = null;
                return true;
            }
        } else {
            recordCandidateFailReason(lastModifierFailReason != null ? lastModifierFailReason :
                    Component.translatable("gtceu.recipe_logic.modifier_fails"));
        }
        return false;
    }

    public boolean checkMatchedRecipeAvailableForMultiParallel(GTRecipe match) {
        var modified = machine.fullModifyRecipe(match);
        if (modified != null) {
            var recipeMatch = checkRecipe(modified);
            if (recipeMatch.isSuccess()) {
                return true;
            }

        }
        return false;
    }

    public void handleRecipeWorking() {
        assert lastRecipe != null;
        var conditionResult = RecipeHelper.checkConditions(lastRecipe, this);
        if (conditionResult.isSuccess()) {
            var handleTick = handleTickRecipe(lastRecipe);
            if (handleTick.isSuccess()) {
                setStatus(Status.WORKING);
                if (!machine.onWorking()) {
                    this.interruptRecipe();
                    return;
                }
                progress++;
                totalContinuousRunningTime++;
            } else {
                setWaiting(formatActionFailure(handleTick));
                // Machine isn't getting enough power, suspend after 5 attempts.
                if (handleTick.io() == IO.IN && handleTick.capability() == EURecipeCapability.CAP) {

                    runAttempt++;
                    runAttempt = (int) GTMath.clamp(runAttempt, 0, 5);
                    if (runAttempt == 5) {
                        boolean preventPowerFail = false;
                        if (machine.self() instanceof IMultiController) {
                            var covers = machine.self().getCoverContainer().getCovers();
                            for (var cover : covers) {
                                if (cover instanceof MachineControllerCover mcc) {
                                    if (mcc.preventPowerFail()) {
                                        preventPowerFail = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (machine.self() instanceof IMultiController && !preventPowerFail) {
                            runAttempt = 0;
                            setStatus(Status.SUSPEND);
                            setWaitingReason(Component.translatable("gtceu.recipe_logic.power_fail_suspended"));
                        }
                    }
                    runDelay = runAttempt * 60;
                }
            }
        } else {
            setWaiting(formatActionFailure(conditionResult));
        }
        if (isWaiting()) {
            regressRecipe();
        }
    }

    protected void regressRecipe() {
        if (progress > 0 && machine.regressWhenWaiting()) {
            this.progress = 1;
        }
    }

    public @NotNull Iterator<GTRecipe> searchRecipe() {
        if (isMultiParallelLogic) {
            return machine.getRecipeType().searchRecipe(machine, r -> matchRecipe(r).isSuccess(), isMultiParallelLogic);
        }
        return machine.getRecipeType().searchRecipe(machine, r -> matchRecipe(r).isSuccess());
    }

    public void findAndHandleRecipe() {
        lastFailedMatches = null;
        candidateFailReason = null;

        if (!isMultiParallelLogic) {
            // NormalMode
            if (!recipeDirty && lastRecipe != null && checkRecipe(lastRecipe).isSuccess()) {
                GTRecipe recipe = lastRecipe;
                GTRecipe origin = lastOriginRecipe;
                lastRecipe = null;
                setupRecipe(recipe);
                lastOriginRecipe = origin;
            } else {
                lastRecipe = null;
                lastOriginRecipe = null;
                handleSearchingRecipes(searchRecipe());
            }
            recipeDirty = false;
            if (lastRecipe == null || getStatus() != Status.WORKING) {
                Component reason = candidateFailReason != null ? candidateFailReason : diagnoseRecipeFailure();
                if (getStatus() == Status.IDLE || reason != null) {
                    setWaitingReason(reason);
                }
            }
        } else {

            GTRecipe recipeAll = null;
            Component multiParallelFailReason = null;
            if (machine.getRecipeTypes() == null) return;
            for (int i = 0; i < machine.getRecipeTypes().length; ++i) {
                if (!ActiveModesList.get(i)) continue;
                List<GTRecipe> Recipe_List = new ArrayList<>();
                // MultiParallelCount is a thread count: how many recipes may run side by side.
                // Every thread keeps whatever parallels its own recipe modifiers granted it, so
                // per-recipe parallels must never be charged against this budget.
                int threadCount = 0;
                int LoopCount = 0;
                int FailesCount = 0;
                while (true) {
                    machine.setActiveRecipeType(i);
                    if (threadCount >= MultiParallelCount) break;

                    handleSearchingRecipes(searchRecipe());
                    if (lastRecipe == null) {
                        if (Recipe_List.isEmpty() && multiParallelFailReason == null) {
                            multiParallelFailReason = candidateFailReason != null ? candidateFailReason :
                                    diagnoseRecipeFailure();
                        }
                        break;
                    }

                    var handledIO = handleRecipeIO(lastRecipe, IO.IN);
                    if (handledIO.isSuccess()) {
                        Recipe_List.add(lastRecipe);
                        threadCount++;
                        lastRecipe = null;
                        lastOriginRecipe = null;
                        lastFailedMatches = null;
                    } else {
                        if (multiParallelFailReason == null) {
                            multiParallelFailReason = formatActionFailure(handledIO);
                        }
                        FailesCount++;
                    }
                    if (FailesCount > 2) break;
                    LoopCount++;
                    if (LoopCount > maxLoopCount) break;
                }
                GTRecipe recipe = mergeAllRecipes(Recipe_List);
                if (recipe == null) continue;
                recipeAll = mergeTwoRecipes(recipeAll, recipe);
                // if (recipe == null) return;
                // setupRecipe(recipe);
            }
            if (recipeAll == null) {
                if (getStatus() == Status.IDLE || multiParallelFailReason != null) {
                    setWaitingReason(multiParallelFailReason);
                }
                return;
            }
            setupRecipe(recipeAll);
            if (lastRecipe == null || getStatus() != Status.WORKING) {
                Component reason = candidateFailReason != null ? candidateFailReason : multiParallelFailReason;
                if (getStatus() == Status.IDLE || reason != null) {
                    setWaitingReason(reason);
                }
            }
        }
    }

    protected void handleSearchingRecipes(@NotNull Iterator<GTRecipe> matches) {
        candidateFailReason = null;
        while (matches.hasNext()) {
            GTRecipe match = matches.next();
            if (match == null) continue;

            // If a new recipe was found, cache found recipe.
            if (checkMatchedRecipeAvailable(match))
                return;

            // cache matching recipes.
            if (lastFailedMatches == null) {
                lastFailedMatches = new ArrayList<>();
            }
            lastFailedMatches.add(match);
        }
    }

    protected void recordCandidateFailReason(@Nullable Component reason) {
        if (candidateFailReason == null && reason != null && !reason.equals(Component.empty())) {
            candidateFailReason = reason;
        }
    }

    @Nullable
    protected Component formatActionFailure(@Nullable ActionResult result) {
        if (result == null || result.isSuccess()) return null;
        if (result.io() == IO.IN && result.capability() == EURecipeCapability.CAP &&
                (machine instanceof SteamWorkableMachine || machine instanceof SteamParallelMultiblockMachine)) {
            return Component.translatable("gtceu.multiblock.steam.low_steam");
        }
        Component reason = result.reason();
        return (reason == null || reason.equals(Component.empty())) ? null : reason;
    }

    protected ActionResult matchRecipeInputsOnly(GTRecipe recipe) {
        if (!machine.hasCapabilityProxies()) return ActionResult.FAIL_NO_CAPABILITIES;
        return RecipeHelper.handleRecipe(machine, recipe, IO.IN, recipe.inputs,
                Collections.emptyMap(), false, true);
    }

    @Nullable
    protected Component diagnoseRecipeFailure() {
        if (!machine.hasCapabilityProxies()) return null;
        var matches = machine.getRecipeType().searchRecipe(machine, r -> matchRecipeInputsOnly(r).isSuccess());
        while (matches.hasNext()) {
            GTRecipe candidate = matches.next();
            if (candidate == null) continue;
            Component reason = diagnoseCandidate(candidate);
            if (reason != null) return reason;
        }
        return null;
    }

    @Nullable
    protected Component diagnoseCandidate(GTRecipe recipe) {
        if (machine instanceof IMultiController controller) {
            for (IMultiPart part : controller.getParts()) {
                Component partReason = part.getModifyRecipeFailReason(recipe);
                if (partReason != null) return partReason;
            }
        }
        var conditionResult = RecipeHelper.checkConditions(recipe, this);
        if (!conditionResult.isSuccess()) {
            return formatActionFailure(conditionResult);
        }
        int recipeTier = RecipeHelper.getRecipeEUtTier(recipe);
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine && recipe.data.contains("ebf_temp")) {
            int blastTemp = coilMachine.getCoilType().getCoilTemperature() +
                    (100 * Math.max(0, coilMachine.getTier() - GTValues.MV));
            int recipeTemp = recipe.data.getInt("ebf_temp");
            if (recipeTemp > blastTemp) {
                return Component.translatable("gtceu.recipe_logic.insufficient_temperature",
                        FormattingUtil.formatNumbers(recipeTemp));
            }
        }
        if (machine instanceof FusionReactorMachine fusion && recipe.data.contains("eu_to_start")) {
            if (recipeTier > fusion.getTier()) {
                return Component.translatable("gtceu.recipe_logic.insufficient_tier",
                        Component.literal(GTValues.VNF[Math.min(recipeTier, GTValues.VNF.length - 1)]));
            }
            long euToStart = recipe.data.getLong("eu_to_start");
            if (euToStart > fusion.getInternalEnergyContainer().getEnergyCapacity()) {
                return Component.translatable("gtceu.recipe_logic.insufficient_startup_capacity",
                        FormattingUtil.formatNumberReadable2F(euToStart, false));
            }
            long heatDiff = euToStart - fusion.getHeat();
            if (heatDiff > 0 && fusion.getInternalEnergyContainer().getEnergyStored() < heatDiff) {
                return Component.translatable("gtceu.recipe_logic.insufficient_startup_energy",
                        FormattingUtil.formatNumberReadable2F(heatDiff, false));
            }
        }
        if (!recipe.getInputEUt().isEmpty()) {
            if (machine instanceof IOverclockMachine ocMachine && recipeTier > ocMachine.getMaxOverclockTier()) {
                return Component.translatable("gtceu.recipe_logic.insufficient_tier",
                        Component.literal(GTValues.VNF[Math.min(recipeTier, GTValues.VNF.length - 1)]));
            }
            if ((machine instanceof SimpleSteamMachine || machine instanceof SteamParallelMultiblockMachine) &&
                    recipeTier > GTValues.LV) {
                return Component.translatable("gtceu.recipe_logic.insufficient_tier",
                        Component.literal(GTValues.VNF[Math.min(recipeTier, GTValues.VNF.length - 1)]));
            }
        }
        if (machine instanceof SimpleSteamMachine steamMachine && steamMachine.isNeedsVenting() &&
                steamMachine.isVentingBlocked()) {
            return Component.translatable("gtceu.recipe_logic.vent_obstructed");
        }
        var matchResult = matchRecipe(recipe);
        if (!matchResult.isSuccess()) {
            return formatActionFailure(matchResult);
        }
        return null;
    }

    // In this function,our target to merge all recipes to one recipe
    // The new recipe time is maximum of all recipes,the conditions is
    private GTRecipe mergeAllRecipes(List<GTRecipe> recipes) {
        GTRecipe after_merge_recipe = null;
        for (GTRecipe recipe : recipes) {
            after_merge_recipe = mergeTwoRecipes(after_merge_recipe, recipe.copy());
        }
        return after_merge_recipe;
    }

    public <K> Map<K, List<Content>> makeValuesMutable(Map<K, List<Content>> map) {
        Map<K, List<Content>> mutableMap = new HashMap<>(); // 用 JDK HashMap，确保可变
        for (Map.Entry<K, List<Content>> entry : map.entrySet()) {
            List<Content> list = entry.getValue();
            if (list == null) continue;
            List<Content> mutableList = (list instanceof ArrayList) ? list : new ArrayList<>(list);
            mutableMap.put(entry.getKey(), mutableList);
        }
        return mutableMap;
    }

    public <K> Map<K, ChanceLogic> makeMapMutable(Map<K, ChanceLogic> map) {
        if (map instanceof HashMap) {
            return map; // 已经是可变的
        }
        return new HashMap<>(map);
    }

    public <T> List<T> makeListMutable(List<T> list) {
        if (list == null) return new ArrayList<>();
        if (list instanceof ArrayList) return list;
        return new ArrayList<>(list);
    }

    private GTRecipe mergeTwoRecipes(GTRecipe afterMergeRecipe, GTRecipe recipe) {
        if (afterMergeRecipe == null && recipe == null) {
            throw new RuntimeException("Fatal Error, Two Recipes both are Null!");
        }
        if (afterMergeRecipe == null) {
            return recipe;
        }
        if (recipe == null) {
            return afterMergeRecipe;
        }

        afterMergeRecipe.duration = max(afterMergeRecipe.duration, recipe.duration);
        afterMergeRecipe.getInputEUt();// Calculate the input EU and the output EU
        afterMergeRecipe.getOutputEUt();
        boolean isOutputEU = false;
        if (afterMergeRecipe.getOutputEUt().getTotalEU() > 0) {
            isOutputEU = true;
        }
        afterMergeRecipe.parallels = min(afterMergeRecipe.parallels, recipe.parallels);
        afterMergeRecipe.batchParallels = min(afterMergeRecipe.batchParallels, recipe.batchParallels);
        afterMergeRecipe.inputs = makeValuesMutable(afterMergeRecipe.inputs);
        afterMergeRecipe.outputs = makeValuesMutable(afterMergeRecipe.outputs);
        afterMergeRecipe.tickInputs = makeValuesMutable(afterMergeRecipe.tickInputs);
        afterMergeRecipe.tickOutputs = makeValuesMutable(afterMergeRecipe.tickOutputs);
        afterMergeRecipe.tickOutputChanceLogics = makeMapMutable(afterMergeRecipe.tickOutputChanceLogics);
        afterMergeRecipe.tickInputChanceLogics = makeMapMutable(afterMergeRecipe.tickInputChanceLogics);
        afterMergeRecipe.conditions = makeListMutable(afterMergeRecipe.conditions);
        afterMergeRecipe.inputChanceLogics = makeMapMutable(afterMergeRecipe.inputChanceLogics);
        afterMergeRecipe.outputChanceLogics = makeMapMutable(afterMergeRecipe.outputChanceLogics);
        RecipeUtils.mergeMapOfLists(afterMergeRecipe.inputs, recipe.inputs);
        RecipeUtils.mergeMapOfLists(afterMergeRecipe.outputs, recipe.outputs);
        afterMergeRecipe.ocLevel = min(afterMergeRecipe.ocLevel, recipe.ocLevel);
        if (recipe.conditions != null && afterMergeRecipe.conditions != null) {
            afterMergeRecipe.conditions.addAll(recipe.conditions);
        }
        if (recipe.inputChanceLogics != null && afterMergeRecipe.inputChanceLogics != null) {
            afterMergeRecipe.inputChanceLogics = RecipeUtils.mergeChanceLogicMap(afterMergeRecipe.inputChanceLogics,
                    recipe.inputChanceLogics);
        } else if (afterMergeRecipe.inputChanceLogics == null && recipe.inputChanceLogics != null) {
            afterMergeRecipe.inputChanceLogics = recipe.inputChanceLogics;
        }
        if (recipe.outputChanceLogics != null && afterMergeRecipe.outputChanceLogics != null) {
            afterMergeRecipe.outputChanceLogics = RecipeUtils.mergeChanceLogicMap(afterMergeRecipe.outputChanceLogics,
                    recipe.outputChanceLogics);
        } else if (recipe.outputChanceLogics == null && afterMergeRecipe.outputChanceLogics != null) {
            afterMergeRecipe.outputChanceLogics = recipe.outputChanceLogics;
        }
        if (recipe.tickInputs != null && afterMergeRecipe.tickInputs != null) {
            if (isOutputEU) {
                RecipeUtils.mergeMapOfLists(afterMergeRecipe.tickInputs, recipe.tickInputs);
            }
        }
        if (recipe.tickOutputs != null && afterMergeRecipe.tickOutputs != null) {
            if (isOutputEU) {
                RecipeUtils.mergeMapOfLists(afterMergeRecipe.tickOutputs, recipe.tickOutputs);
            }
        }
        if (recipe.tickInputChanceLogics != null && afterMergeRecipe.tickInputChanceLogics != null) {
            afterMergeRecipe.tickInputChanceLogics = RecipeUtils
                    .mergeChanceLogicMap(afterMergeRecipe.tickInputChanceLogics, recipe.tickInputChanceLogics);
        }
        if (recipe.tickOutputChanceLogics != null && afterMergeRecipe.tickOutputChanceLogics != null) {
            afterMergeRecipe.tickOutputChanceLogics = RecipeUtils
                    .mergeChanceLogicMap(afterMergeRecipe.tickOutputChanceLogics, recipe.tickOutputChanceLogics);
        }
        return afterMergeRecipe;
    }

    public ActionResult handleTickRecipe(GTRecipe recipe) {
        if (!recipe.hasTick()) return ActionResult.SUCCESS;

        var result = RecipeHelper.matchTickRecipe(machine, recipe);
        if (!result.isSuccess()) return result;

        result = handleTickRecipeIO(recipe, IO.IN);
        if (!result.isSuccess()) return result;

        result = handleTickRecipeIO(recipe, IO.OUT);
        return result;
    }

    public void setupRecipe(GTRecipe recipe) {
        lastBeforeWorkingFailReason = null;
        if (!machine.beforeWorking(recipe)) {
            Component failReason = lastBeforeWorkingFailReason != null ? lastBeforeWorkingFailReason :
                    Component.translatable("gtceu.recipe_logic.before_working_fails");
            recordCandidateFailReason(failReason);
            setStatus(Status.IDLE);
            consecutiveRecipes = 0;
            progress = 0;
            duration = 0;
            isActive = false;
            return;
        }
        ActionResult handledIO;
        if (!isMultiParallelLogic) {
            handledIO = handleRecipeIO(recipe, IO.IN);
        } else {
            handledIO = ActionResult.SUCCESS;
        }
        if (isMultiParallelLogic || handledIO.isSuccess()) {
            if (lastRecipe != null && !recipe.equals(lastRecipe)) {
                chanceCaches.clear();
            }
            recipeDirty = false;
            lastRecipe = recipe;
            setStatus(Status.WORKING);
            progress = 0;
            duration = recipe.duration;
            isActive = true;
        } else {
            recordCandidateFailReason(formatActionFailure(handledIO));
        }
    }

    public void setStatus(Status status) {
        if (this.status != status) {
            if (this.status == Status.WORKING) {
                this.totalContinuousRunningTime = 0;
            }
            if (status == Status.SUSPEND && suspendAfterFinish) {
                suspendAfterFinish = false;
            }
            machine.notifyStatusChanged(this.status, status);
            this.status = status;
            setRenderState(getRenderState().setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, status));
            updateTickSubscription();
            if (this.status != Status.WAITING) {
                setWaitingReason(null);
            }
        }
    }

    public void setWaitingReason(@Nullable Component reason) {
        if (reason != null && reason.equals(Component.empty())) {
            reason = null;
        }
        if (!Objects.equals(this.waitingReason, reason)) {
            this.waitingReason = reason;
        }
    }

    public void setWaiting(@Nullable Component reason) {
        setStatus(Status.WAITING);
        setWaitingReason(reason);
        machine.onWaiting();
    }

    public boolean shouldDisplayHudReason() {
        if (isWorking() || waitingReason == null) return false;
        if (machine instanceof IMultiController controller && !controller.isFormed()) {
            return false;
        }
        if (containsTranslationKey(waitingReason, "gtceu.multiblock.universal.has_problems") &&
                machine instanceof IMultiController controller) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof IMaintenanceMachine maintenance && maintenance.hasMaintenanceProblems()) {
                    return false;
                }
            }
        }
        if ((containsTranslationKey(waitingReason, "gtceu.recipe_logic.vent_obstructed") ||
                containsTranslationKey(waitingReason, "recipe.condition.steam_vent.tooltip")) &&
                machine instanceof IExhaustVentMachine vent && vent.isVentingBlocked() && vent.isNeedsVenting()) {
            return false;
        }
        return true;
    }

    private static boolean containsTranslationKey(@Nullable Component component, String key) {
        if (component == null) return false;
        if (component.getContents() instanceof TranslatableContents tc && key.equals(tc.getKey())) {
            return true;
        }
        for (Component sibling : component.getSiblings()) {
            if (containsTranslationKey(sibling, key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * mark current handling recipe (if exist) as dirty.
     * do not try it immediately in the next round
     */
    public void markLastRecipeDirty() {
        this.recipeDirty = true;
    }

    public boolean isWorking() {
        return status == Status.WORKING;
    }

    public boolean isIdle() {
        return status == Status.IDLE;
    }

    public boolean isWaiting() {
        return status == Status.WAITING;
    }

    public boolean isSuspend() {
        return status == Status.SUSPEND;
    }

    public boolean isWorkingEnabled() {
        return !isSuspend() && !isSuspendAfterFinish();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (!isWorkingAllowed && getStatus() == Status.IDLE) {
            setStatus(Status.SUSPEND);
        } else {
            setSuspendAfterFinish(!isWorkingAllowed);
            if (isWorkingAllowed) {
                if (lastRecipe != null && duration > 0) {
                    setStatus(Status.WORKING);
                } else {
                    setStatus(Status.IDLE);
                }
            }
        }
    }

    @Override
    public int getMaxProgress() {
        return duration;
    }

    public boolean isActive() {
        return isWorking() || isWaiting() || (isSuspend() && isActive);
    }

    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            runAttempt = 0;
            runDelay = 0;
            consecutiveRecipes++;
            modifyChanceRecipes(lastRecipe);
            handleRecipeIO(lastRecipe, IO.OUT);

            if (isMultiParallelLogic) {
                lastRecipe = null;
                lastOriginRecipe = null;
                lastFailedMatches = null;
                if (!suspendAfterFinish) {
                    findAndHandleRecipe();
                }
                if (lastRecipe == null || getStatus() != Status.WORKING) {
                    if (suspendAfterFinish) {
                        setStatus(Status.SUSPEND);
                    } else {
                        Component failReason = this.waitingReason;
                        setStatus(Status.IDLE);
                        if (failReason != null) {
                            setWaitingReason(failReason);
                        }
                    }
                    consecutiveRecipes = 0;
                    progress = 0;
                    duration = 0;
                    isActive = false;
                }
                return;
            }

            if (machine.alwaysTryModifyRecipe()) {
                if (lastOriginRecipe != null) {
                    var modified = machine.fullModifyRecipe(lastOriginRecipe.copy());
                    if (modified == null) {
                        markLastRecipeDirty();
                    } else {
                        lastRecipe = modified;
                    }
                } else {
                    markLastRecipeDirty();
                }
            }
            // try it again
            var recipeCheck = checkRecipe(lastRecipe);
            if (!recipeDirty && !suspendAfterFinish && recipeCheck.isSuccess()) {
                setupRecipe(lastRecipe);
            } else if (!suspendAfterFinish) {
                lastRecipe = null;
                lastOriginRecipe = null;
                findAndHandleRecipe();
            }
            if (suspendAfterFinish || lastRecipe == null || getStatus() != Status.WORKING) {
                if (suspendAfterFinish) {
                    setStatus(Status.SUSPEND);
                } else {
                    Component failReason = this.waitingReason;
                    setStatus(Status.IDLE);
                    if (failReason != null) {
                        setWaitingReason(failReason);
                    } else if (!recipeCheck.isSuccess() &&
                            (recipeCheck.io() != IO.IN || recipeCheck.capability() == EURecipeCapability.CAP)) {
                        setWaitingReason(formatActionFailure(recipeCheck));
                    }
                }
                consecutiveRecipes = 0;
                progress = 0;
                duration = 0;
                isActive = false;
            }
        }
    }

    private void modifyChanceRecipes(@Nullable GTRecipe lastRecipe) {
        for (var entry : lastRecipe.outputs.entrySet()) {
            var contents = entry.getValue();
            for (var content : contents) {
                if (content.getContent() instanceof SizedIngredient sizedIngredient) {
                    int total_chance = content.chance + lastRecipe.ocLevel * content.tierChanceBoost;
                    if (total_chance >= content.maxChance) content.chance = content.maxChance;
                    else {
                        if (((int) (sizedIngredient.getAmount() * (total_chance * 1.0 / content.maxChance))) > 0) {
                            content.chance = content.maxChance;
                            sizedIngredient.setAmount(
                                    (int) (sizedIngredient.getAmount() * (total_chance * 1.0 / content.maxChance)));
                        }
                    }
                }
                if (content.getContent() instanceof FluidIngredient fluidIngredient) {
                    int total_chance = content.chance + lastRecipe.ocLevel * content.tierChanceBoost;
                    if (total_chance >= content.maxChance) content.chance = content.maxChance;
                    else {
                        if (((int) (fluidIngredient.getAmount() * (total_chance * 1.0 / content.maxChance))) > 0) {
                            content.chance = content.maxChance;
                            fluidIngredient.setAmount(
                                    (int) (fluidIngredient.getAmount() * (total_chance * 1.0 / content.maxChance)));
                        }
                    }
                }
            }
        }
        for (var entry : lastRecipe.inputs.entrySet()) {
            var contents = entry.getValue();
            for (var content : contents) {
                if (content.getContent() instanceof SizedIngredient sizedIngredient) {
                    int total_chance = content.chance + lastRecipe.ocLevel * content.tierChanceBoost;
                    if (total_chance >= content.maxChance) content.chance = content.maxChance;
                    else {
                        if (((int) (sizedIngredient.getAmount() * (total_chance * 1.0 / content.maxChance))) > 0) {
                            content.chance = content.maxChance;
                            sizedIngredient.setAmount(
                                    (int) (sizedIngredient.getAmount() * (total_chance * 1.0 / content.maxChance)));
                        }
                    }
                }
                if (content.getContent() instanceof FluidIngredient fluidIngredient) {
                    int total_chance = content.chance + lastRecipe.ocLevel * content.tierChanceBoost;
                    if (total_chance >= content.maxChance) content.chance = content.maxChance;
                    else {
                        if (((int) (fluidIngredient.getAmount() * (total_chance * 1.0 / content.maxChance))) > 0) {
                            content.chance = content.maxChance;
                            fluidIngredient.setAmount(
                                    (int) (fluidIngredient.getAmount() * (total_chance * 1.0 / content.maxChance)));
                        }
                    }
                }
            }
        }
    }

    protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
        return RecipeHelper.handleRecipeIO(machine, recipe, io, this.chanceCaches);
    }

    protected ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
        return RecipeHelper.handleTickRecipeIO(machine, recipe, io, this.chanceCaches);
    }

    /**
     * Interrupt current recipe without io.
     */
    public void interruptRecipe() {
        machine.afterWorking();
        if (lastRecipe != null) {
            setStatus(Status.IDLE);
            progress = 0;
            duration = 0;
        }
    }

    // Remains for legacy + for subclasses
    public void inValid() {}

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    // ******** MISC *********//
    //////////////////////////////////////
    @OnlyIn(Dist.CLIENT)
    public void updateSound() {
        if (isWorking() && machine.shouldWorkingPlaySound()) {
            var sound = machine.getRecipeType().getSound();
            if (workingSound instanceof AutoReleasedSound soundEntry) {
                if (soundEntry.soundEntry == sound && !soundEntry.isStopped()) {
                    return;
                }
                soundEntry.release();
                workingSound = null;
            }
            if (sound != null) {
                workingSound = sound.playAutoReleasedSound(
                        () -> machine.shouldWorkingPlaySound() && isWorking() && !getMachine().isInValid() &&
                                getMachine().getLevel().isLoaded(getMachine().getPos()) &&
                                MetaMachine.getMachine(getMachine().getLevel(), getMachine().getPos()) == getMachine(),
                        getMachine().getPos(), true, 0, 1, 1);
            }
        } else if (workingSound instanceof AutoReleasedSound soundEntry) {
            soundEntry.release();
            workingSound = null;
        }
    }

    @Override
    public IGuiTexture getFancyTooltipIcon() {
        if (waitingReason != null) {
            return GuiTextures.INSUFFICIENT_INPUT;
        }
        return IGuiTexture.EMPTY;
    }

    @Override
    public List<Component> getFancyTooltip() {
        if (waitingReason != null) {
            return List.of(waitingReason);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean showFancyTooltip() {
        return waitingReason != null;
    }

    protected Map<RecipeCapability<?>, Object2IntMap<?>> makeChanceCaches() {
        Map<RecipeCapability<?>, Object2IntMap<?>> map = new IdentityHashMap<>();
        for (RecipeCapability<?> cap : GTRegistries.RECIPE_CAPABILITIES.values()) {
            map.put(cap, cap.makeChanceCache());
        }
        return map;
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        CompoundTag chanceCache = new CompoundTag();
        this.chanceCaches.forEach((cap, cache) -> {
            ListTag cacheTag = new ListTag();
            for (var entry : cache.object2IntEntrySet()) {
                CompoundTag compoundTag = new CompoundTag();
                var obj = cap.contentToNbt(entry.getKey());
                compoundTag.put("entry", obj);
                compoundTag.putInt("cached_chance", entry.getIntValue());
                cacheTag.add(compoundTag);
            }
            chanceCache.put(cap.name, cacheTag);
        });
        tag.put("chance_cache", chanceCache);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        CompoundTag chanceCache = tag.getCompound("chance_cache");
        for (String key : chanceCache.getAllKeys()) {
            RecipeCapability<?> cap = GTRegistries.RECIPE_CAPABILITIES.get(key);
            if (cap == null) continue; // Necessary since we removed a RecipeCapability when nuking Create
            // noinspection rawtypes
            Object2IntMap map = this.chanceCaches.computeIfAbsent(cap, RecipeCapability::makeChanceCache);

            ListTag chanceTag = chanceCache.getList(key, Tag.TAG_COMPOUND);
            for (int i = 0; i < chanceTag.size(); ++i) {
                CompoundTag chanceKey = chanceTag.getCompound(i);
                var entry = cap.serializer.fromNbt(chanceKey.get("entry"));
                int value = chanceKey.getInt("cached_chance");
                // noinspection unchecked
                map.put(entry, value);
            }
        }
        this.chanceCaches.forEach((cap, cache) -> {
            ListTag cacheTag = new ListTag();
            for (var entry : cache.object2IntEntrySet()) {
                CompoundTag compoundTag = new CompoundTag();
                var obj = cap.contentToNbt(entry.getKey());
                compoundTag.put("entry", obj);
                compoundTag.putInt("cached_chance", entry.getIntValue());
                cacheTag.add(compoundTag);
            }
            chanceCache.put(cap.name, cacheTag);
        });
        tag.put("chance_cache", chanceCache);
    }
}
