package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.syncdata.payload.ItemStackPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStackPayload.class, remap = false)
public abstract class ItemStackPayloadMixin extends ObjectTypedPayload<ItemStack> {

    @Inject(method = "serializeNBT", at = @At("HEAD"), cancellable = true)
    private void gtceu$fixEmptyStackSerialize(HolderLookup.Provider provider, CallbackInfoReturnable<Tag> cir) {
        if (payload.isEmpty()) {
            cir.setReturnValue(new CompoundTag());
        }
    }

    @Inject(method = "deserializeNBT", at = @At("HEAD"), cancellable = true)
    public void gtceu$fixEmptyStackDeserialize(Tag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (!(tag instanceof CompoundTag compoundTag) || compoundTag.isEmpty()) {
            payload = ItemStack.EMPTY;
            ci.cancel();
        }
    }
}
