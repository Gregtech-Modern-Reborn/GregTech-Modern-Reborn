package com.reborn.gtceu.data;

import com.reborn.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.reborn.gtceu.common.registry.GTRegistration;
import com.reborn.gtceu.core.mixins.registrate.RegistrateDataProviderAccessor;
import com.reborn.gtceu.data.lang.LangHandler;
import com.reborn.gtceu.data.model.BlockstateModelLoader;
import com.reborn.gtceu.data.tags.*;

import com.tterrag.registrate.providers.ProviderType;

public class GregTechDatagen {

    // we only register this so the class gets loaded. the key gets overwritten in #initPre.
    private static final ProviderType<GTBlockstateProvider> BLOCKSTATE_PROVIDER = ProviderType.register("ex_blockstate",
            GTBlockstateProvider::new);

    public static void initPre() {
        // replace some default providers with ours
        RegistrateDataProviderAccessor.gtceu$getTypes().forcePut("blockstate", BLOCKSTATE_PROVIDER);

        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCKSTATE,
                p -> BlockstateModelLoader.init((GTBlockstateProvider) p));
    }

    public static void initPost() {
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, BlockTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, ItemTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, FluidTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, EntityTypeTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
    }
}
