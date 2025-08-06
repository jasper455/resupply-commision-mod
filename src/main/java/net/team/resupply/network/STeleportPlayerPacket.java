package net.team.resupply.network;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class STeleportPlayerPacket {
    private final UUID playerUUID;
    private final ResourceLocation dimension;
    private final double x, y, z;

    public STeleportPlayerPacket(UUID playerUUID, ResourceLocation dimension, double x, double y, double z) {
        this.playerUUID = playerUUID;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public STeleportPlayerPacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID(),
                buffer.readResourceLocation(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeResourceLocation(dimension);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerLevel targetWorld = context.getSender().server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        if (targetWorld == null) return;
        ServerPlayer targetPlayer = targetWorld.getServer().getPlayerList().getPlayer(playerUUID);
        if (targetPlayer == null) return;
        if (targetPlayer.level() != targetWorld) {
            targetPlayer.changeDimension(targetWorld);
        }
        targetPlayer.teleportTo(targetWorld, x, y, z, targetPlayer.getYRot(), targetPlayer.getXRot());
        context.setPacketHandled(true);
    }
}