package com.reborn.gtceu.client;

import com.reborn.gtceu.GTCEu;
import com.reborn.gtceu.api.GTValues;
import com.reborn.gtceu.api.data.worldgen.GTOreDefinition;
import com.reborn.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.reborn.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.reborn.gtceu.api.item.DrumMachineItem;
import com.reborn.gtceu.api.item.IComponentItem;
import com.reborn.gtceu.api.item.IGTTool;
import com.reborn.gtceu.api.item.LampBlockItem;
import com.reborn.gtceu.api.item.QuantumTankMachineItem;
import com.reborn.gtceu.client.model.item.FacadeUnbakedModel;
import com.reborn.gtceu.client.model.machine.MachineModelLoader;
import com.reborn.gtceu.client.particle.HazardParticle;
import com.reborn.gtceu.client.particle.MufflerParticle;
import com.reborn.gtceu.client.renderer.entity.GTBoatRenderer;
import com.reborn.gtceu.client.renderer.entity.GTExplosiveRenderer;
import com.reborn.gtceu.client.renderer.item.decorator.GTComponentItemDecorator;
import com.reborn.gtceu.client.renderer.item.decorator.GTLampItemOverlayRenderer;
import com.reborn.gtceu.client.renderer.item.decorator.GTTankItemFluidPreview;
import com.reborn.gtceu.client.renderer.item.decorator.GTToolBarRenderer;
import com.reborn.gtceu.client.renderer.machine.DynamicRenderManager;
import com.reborn.gtceu.client.renderer.machine.impl.*;
import com.reborn.gtceu.client.renderer.machine.impl.BoilerMultiPartRender;
import com.reborn.gtceu.common.CommonProxy;
import com.reborn.gtceu.common.data.GTBlockEntities;
import com.reborn.gtceu.common.data.GTEntityTypes;
import com.reborn.gtceu.common.data.GTParticleTypes;
import com.reborn.gtceu.common.entity.GTBoat;
import com.reborn.gtceu.common.machine.owner.MachineOwner;
import com.reborn.gtceu.config.ConfigHolder;
import com.reborn.gtceu.integration.map.ClientCacheManager;
import com.reborn.gtceu.integration.map.cache.client.GTClientCache;
import com.reborn.gtceu.integration.map.ftbchunks.FTBChunksPlugin;
import com.reborn.gtceu.integration.map.layer.Layers;
import com.reborn.gtceu.integration.map.layer.builtin.FluidRenderLayer;
import com.reborn.gtceu.integration.map.layer.builtin.OreRenderLayer;
import com.reborn.gtceu.utils.input.KeyBind;
import com.reborn.gtceu.utils.input.SyncedKeyMapping;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ClientProxy extends CommonProxy {

    public static final BiMap<ResourceLocation, GTOreDefinition> CLIENT_ORE_VEINS = HashBiMap.create();
    public static final BiMap<ResourceLocation, BedrockFluidDefinition> CLIENT_FLUID_VEINS = HashBiMap.create();
    public static final BiMap<ResourceLocation, BedrockOreDefinition> CLIENT_BEDROCK_ORE_VEINS = HashBiMap.create();

    public ClientProxy() {
        super();
        init();
    }

    public static void init() {
        if (!GTCEu.isDataGen()) {
            ClientCacheManager.registerClientCache(GTClientCache.instance, "gtceu");
            Layers.registerLayer(OreRenderLayer::new, "ore_veins");
            Layers.registerLayer(FluidRenderLayer::new, "bedrock_fluids");
        }
        initializeDynamicRenders();
    }

    @SubscribeEvent
    public void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GTEntityTypes.DYNAMITE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.POWDERBARREL.get(), GTExplosiveRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.INDUSTRIAL_TNT.get(), GTExplosiveRenderer::new);

        event.registerBlockEntityRenderer(GTBlockEntities.GT_SIGN.get(), SignRenderer::new);
        event.registerBlockEntityRenderer(GTBlockEntities.GT_HANGING_SIGN.get(), HangingSignRenderer::new);

        event.registerEntityRenderer(GTEntityTypes.BOAT.get(), c -> new GTBoatRenderer(c, false));
        event.registerEntityRenderer(GTEntityTypes.CHEST_BOAT.get(), c -> new GTBoatRenderer(c, true));

        for (var type : GTBoat.BoatType.values()) {
            ForgeHooksClient.registerLayerDefinition(GTBoatRenderer.getBoatModelName(type), BoatModel::createBodyModel);
            ForgeHooksClient.registerLayerDefinition(GTBoatRenderer.getChestBoatModelName(type),
                    ChestBoatModel::createBodyModel);
        }
    }

    @SubscribeEvent
    public void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        for (Item item : ForgeRegistries.ITEMS) {
            if (item instanceof IComponentItem) {
                event.register(item, GTComponentItemDecorator.INSTANCE);
            }
            if (item instanceof IGTTool) {
                event.register(item, GTToolBarRenderer.INSTANCE);
            }
            if (item instanceof LampBlockItem) {
                event.register(item, GTLampItemOverlayRenderer.INSTANCE);
            }
            if (item instanceof DrumMachineItem) {
                event.register(item, GTTankItemFluidPreview.DRUM);
            }
            if (item instanceof QuantumTankMachineItem) {
                event.register(item, GTTankItemFluidPreview.QUANTUM_TANK);
            }
        }
    }

    @SubscribeEvent
    public void registerKeyBindings(RegisterKeyMappingsEvent event) {
        KeyBind.onRegisterKeyBinds(event);
        SyncedKeyMapping.onRegisterKeyBinds(event);
    }

    @SubscribeEvent
    public void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("hud", new HudGuiOverlay());
    }

    @SubscribeEvent
    public void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(GTParticleTypes.HAZARD_PARTICLE.get(), HazardParticle.Provider::new);
        event.registerSpriteSet(GTParticleTypes.MUFFLER_PARTICLE.get(), MufflerParticle.Provider::new);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        MachineOwner.init();
        if (ConfigHolder.INSTANCE.compat.minimap.toggle.ftbChunksIntegration &&
                GTCEu.isModLoaded(GTValues.MODID_FTB_CHUNKS)) {
            FTBChunksPlugin.addEventListeners();
        }
    }

    public static void initializeDynamicRenders() {
        DynamicRenderManager.register(GTCEu.id("quantum_tank_fluid"), QuantumTankFluidRender.TYPE);
        DynamicRenderManager.register(GTCEu.id("quantum_chest_item"), QuantumChestItemRender.TYPE);

        DynamicRenderManager.register(GTCEu.id("fusion_ring"), FusionRingRender.TYPE);
        DynamicRenderManager.register(GTCEu.id("boiler_multi_parts"), BoilerMultiPartRender.TYPE);

        DynamicRenderManager.register(GTCEu.id("fluid_area"), FluidAreaRender.TYPE);
        DynamicRenderManager.register(GTCEu.id("growing_plant"), GrowingPlantRender.TYPE);

        DynamicRenderManager.register(GTCEu.id("central_monitor"), CentralMonitorRender.TYPE);
    }

    @SubscribeEvent
    public void onRegisterModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(MachineModelLoader.ID.getPath(), MachineModelLoader.INSTANCE);
        event.register("facade", FacadeUnbakedModel.Loader.INSTANCE);
    }
}
