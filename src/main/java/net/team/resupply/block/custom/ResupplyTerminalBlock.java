package net.team.resupply.block.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.team.resupply.block.entity.custom.ResupplyTerminalBlockEntity;
import net.team.resupply.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResupplyTerminalBlock extends BaseEntityBlock {
    public ResupplyTerminalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            ItemStack stack = pPlayer.getMainHandItem();
            if (entity instanceof ResupplyTerminalBlockEntity resupplyTerminalBlockEntity) {
                if (!stack.is(ModItems.RESUPPLY_ORB.get())) {
                    NetworkHooks.openScreen(((ServerPlayer)pPlayer), resupplyTerminalBlockEntity, pPos);
                } else {
                    // Check for your custom item
                    if (stack.is(ModItems.RESUPPLY_ORB.get())) {
                        if (!pLevel.hasNeighborSignal(pPos)) {
                            ItemStack slotItem = resupplyTerminalBlockEntity.getItem(); // Assuming slot 0
                            if (!slotItem.isEmpty()) {
                                // Copy full NBT of slot item
                                CompoundTag slotItemTag = slotItem.save(new CompoundTag());
                                // Write the slot item tag to the held item
                                stack.getOrCreateTag().put("StoredItem", slotItemTag);
                                // Optional: feedback
                                pPlayer.displayClientMessage(Component.literal("Stored Block data!"), true);
                                slotItem.shrink(999);
                            }
                            return InteractionResult.SUCCESS;
                        } else {
                            List<LivingEntity> entitiesAbove = pLevel.getEntitiesOfClass(LivingEntity.class, new AABB(pPos.above()));
                            if (!entitiesAbove.isEmpty()) {
                                pPlayer.displayClientMessage(Component.literal("Stored Entity Data"), true);
                                saveEntityData(stack, pLevel, pPlayer, entitiesAbove.get(0));
                            }
                        }
                    }
                }
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new ResupplyTerminalBlockEntity(blockPos, state);
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            level.scheduleTick(pos, this, 1); // Schedule tick 1 tick later
        }
    }

    private void saveEntityData(ItemStack stack, Level level, Player player, Entity target) {
        CompoundTag tag = stack.getOrCreateTag();

        if (tag.contains("StoredEntity") || tag.contains("StoredEntityId")) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("This capsule already contains an entity!"), true);
            }
            return;
        }

        if (target instanceof Player) {
            CompoundTag entityData = new CompoundTag();

            // Save only UUID — or use saveWithoutId(entityData) if you want full data
            entityData.putUUID("UUID", target.getUUID());
            tag.put("StoredEntity", entityData);

            ResourceLocation typeId = EntityType.getKey(target.getType());
            if (typeId != null) {
                tag.putString("StoredEntityId", typeId.toString());
            }

            return;
        }

        CompoundTag entityData = new CompoundTag();
        if (target.saveAsPassenger(entityData)) {
            entityData.remove("UUID");
            tag.put("StoredEntity", entityData);

            // ✅ FIXED THIS LINE
            ResourceLocation typeId = EntityType.getKey(target.getType());
            if (typeId != null) {
                tag.putString("StoredEntityId", typeId.toString());
            }

            if (!level.isClientSide()) {
                target.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

}
