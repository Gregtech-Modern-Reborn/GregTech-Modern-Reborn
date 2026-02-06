package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;

public class SerializerBlockState implements IContentSerializer<BlockState> {

    public static SerializerBlockState INSTANCE = new SerializerBlockState();

    private SerializerBlockState() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, BlockState content) {
        buf.writeById(Block.BLOCK_STATE_REGISTRY::getId, content);
    }

    @Override
    public BlockState fromNetwork(RegistryFriendlyByteBuf buf) {
        return buf.readById(Block.BLOCK_STATE_REGISTRY::byId);
    }

    @Override
    public BlockState of(Object o) {
        if (o instanceof BlockState state) {
            return state;
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockState defaultValue() {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public Class<BlockState> contentClass() {
        return BlockState.class;
    }

    @Override
    public Codec<BlockState> codec() {
        return BlockState.CODEC;
    }
}
