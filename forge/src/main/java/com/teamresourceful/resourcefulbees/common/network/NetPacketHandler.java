package com.teamresourceful.resourcefulbees.common.network;

import com.teamresourceful.resourcefulbees.ResourcefulBees;
import com.teamresourceful.resourcefulbees.common.network.packets.client.*;
import com.teamresourceful.resourcefulbees.common.network.packets.server.CommandResponsePacket;
import com.teamresourceful.resourcefulbees.common.network.packets.server.DimensionalBeesPacket;
import com.teamresourceful.resourcefulbees.common.network.packets.server.SyncBeepediaPacket;
import com.teamresourceful.resourcefulbees.common.network.packets.server.SyncGuiPacket;
import com.teamresourceful.resourcefulbees.common.lib.tools.UtilityClassError;
import com.teamresourceful.resourcefullib.common.networking.NetworkChannel;
import com.teamresourceful.resourcefullib.common.networking.base.NetworkDirection;

public final class NetPacketHandler {

    private NetPacketHandler() {
        throw new UtilityClassError();
    }

    public static final NetworkChannel CHANNEL = new NetworkChannel(ResourcefulBees.MOD_ID, 0, "main");

    public static void init() {
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, LockBeePacket.ID, LockBeePacket.HANDLER, LockBeePacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, BeeconChangePacket.ID, BeeconChangePacket.HANDLER, BeeconChangePacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, SelectFluidPacket.ID, SelectFluidPacket.HANDLER, SelectFluidPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, CommandPacket.ID, CommandPacket.HANDLER, CommandPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, LockBeePacket.ID, LockBeePacket.HANDLER, LockBeePacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, FindBeePacket.ID, FindBeePacket.HANDLER, FindBeePacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, OutputLocationSelectionPacket.ID, OutputLocationSelectionPacket.HANDLER, OutputLocationSelectionPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, VoidExcessPacket.ID, VoidExcessPacket.HANDLER, VoidExcessPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, PurgeContentsPacket.ID, PurgeContentsPacket.HANDLER, PurgeContentsPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, SetFilterSlotPacket.ID, SetFilterSlotPacket.HANDLER, SetFilterSlotPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, SwitchGuiPacket.ID, SwitchGuiPacket.HANDLER, SwitchGuiPacket.class);

        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT, SyncGuiPacket.ID, SyncGuiPacket.HANDLER, SyncGuiPacket.class);
        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT, CommandResponsePacket.ID, CommandResponsePacket.HANDLER, CommandResponsePacket.class);
        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT, SyncBeepediaPacket.ID, SyncBeepediaPacket.HANDLER, SyncBeepediaPacket.class);
        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT, DimensionalBeesPacket.ID, DimensionalBeesPacket.HANDLER, DimensionalBeesPacket.class);
    }
}
