package com.reborn.gtceu.common.network.packets;

import com.reborn.gtceu.common.capability.WorldIDSaveData;
import com.reborn.gtceu.common.network.GTNetwork;
import com.reborn.gtceu.integration.map.ClientCacheManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SPacketSendWorldID implements GTNetwork.INetPacket {

    private String worldId;

    public SPacketSendWorldID(FriendlyByteBuf buf) {
        worldId = buf.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(WorldIDSaveData.getWorldID());
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ClientCacheManager.init(worldId);
    }
}
