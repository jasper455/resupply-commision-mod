package net.team.resupply.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.team.resupply.ResupplyMod;

public class PacketHandler {
    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(
            ResourceLocation.fromNamespaceAndPath(ResupplyMod.MOD_ID, "main"))
            .serverAcceptedVersions((status) -> true)
            .clientAcceptedVersions((status) -> true)
            .networkProtocolVersion(() -> "1")
            .simpleChannel();

    public static void register() {
        int index = 0;
        INSTANCE.messageBuilder(STeleportPlayerPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(STeleportPlayerPacket::encode)
                .decoder(STeleportPlayerPacket::new)
                .consumerMainThread(STeleportPlayerPacket::handle)
                .add();
        INSTANCE.messageBuilder(SResupplyPodDestroyBlocksPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SResupplyPodDestroyBlocksPacket::encode)
                .decoder(SResupplyPodDestroyBlocksPacket::new)
                .consumerMainThread(SResupplyPodDestroyBlocksPacket::handle)
                .add();
    }

    public static void sendToServer(Object msg) {
        INSTANCE.sendToServer(msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), msg);
    }

    public static void sendToAllClients(Object msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}