package net.team.resupply.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.team.resupply.block.entity.custom.ResupplyTerminalBlockEntity;
import net.team.resupply.item.ModItems;
import org.jetbrains.annotations.Nullable;

public class ResupplyTerminalBlock extends BaseEntityBlock {
    public ResupplyTerminalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            ItemStack stack = pPlayer.getMainHandItem();

                if(entity instanceof ResupplyTerminalBlockEntity resupplyTerminalBlockEntity) {
                    if (!stack.is(ModItems.RESUPPLY_ORB.get())) {
                        NetworkHooks.openScreen(((ServerPlayer)pPlayer), resupplyTerminalBlockEntity, pPos);
                    } else {
                        // Check for your custom item
                        if (stack.is(ModItems.RESUPPLY_ORB.get())) {
                            ItemStack slotItem = resupplyTerminalBlockEntity.getItem(); // Assuming slot 0
                            if (!slotItem.isEmpty()) {
                                // Copy full NBT of slot item
                                CompoundTag slotItemTag = slotItem.save(new CompoundTag());
                                // Write the slot item tag to the held item
                                stack.getOrCreateTag().put("StoredItem", slotItemTag);
                                // Optional: feedback
                                pPlayer.displayClientMessage(Component.literal("Stored item data!"), true);
                            }
                            return InteractionResult.SUCCESS;
                        }
                    }
                } else {
                    throw new IllegalStateException("Our Container provider is missing!");
                }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new ResupplyTerminalBlockEntity(blockPos, state);
    }
}
