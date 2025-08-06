package net.team.resupply.item.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.registries.ForgeRegistries;
import net.team.resupply.entity.custom.ResupplyOrbProjectileEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ResupplyOrbItem extends Item {
    public ResupplyOrbItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        pPlayer.startUsingItem(pUsedHand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (pLivingEntity instanceof Player player) {
            int i = this.getUseDuration(stack) - pTimeCharged;
            if (!pLevel.isClientSide() && stack.hasTag() && stack.getTag().contains("StoredItem")) {
                CompoundTag storedItemTag = stack.getTag().getCompound("StoredItem");

                ResupplyOrbProjectileEntity resupplyOrb = new ResupplyOrbProjectileEntity(player, pLevel,
                        "Resupply", player.getDirection());
                float power = Math.min(i / 20F, 1.5F); // Max power after 1 second charge
                resupplyOrb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 1.5F, 0F);
                resupplyOrb.getPersistentData().put("StoredItem", storedItemTag);

                pLevel.addFreshEntity(resupplyOrb);
            }

            if (!pLevel.isClientSide() && stack.hasTag() && stack.getTag().contains("StoredEntity") && stack.getTag().contains("StoredEntityId")) {
                CompoundTag storedEntityTag = stack.getTag().getCompound("StoredEntity");
                CompoundTag storedEntityId = stack.getTag().getCompound("StoredEntityId");

                ResupplyOrbProjectileEntity resupplyOrb = new ResupplyOrbProjectileEntity(player, pLevel,
                        "Resupply", player.getDirection());
                float power = Math.min(i / 20F, 1.5F); // Max power after 1 second charge
                resupplyOrb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 1.5F, 0F);
                resupplyOrb.getPersistentData().put("StoredEntity", storedEntityTag);
                resupplyOrb.getPersistentData().put("StoredEntityId", storedEntityId);

                pLevel.addFreshEntity(resupplyOrb);

            }
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (stack.hasTag()) {
            CompoundTag storedItemTag = stack.getTag().getCompound("StoredItem");
            // Load ItemStack from tag
            ItemStack reconstructed = ItemStack.of(storedItemTag);
            if (reconstructed.hasTag()) {
                CompoundTag tag = reconstructed.getTag();
                if (tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
                    tooltipComponents.add(Component.literal("§eLinked!"));
                }
            }
        }

        if (stack.hasTag() && stack.getTag().contains("StoredEntity") && stack.getTag().contains("StoredEntityId")) {
            CompoundTag entityTag = stack.getTag().getCompound("StoredEntity");
            String entityId = stack.getTag().getString("StoredEntityId");

            // Get EntityType from registry
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityId));
            if (type != null && level != null) {
                try {
                    // Create a temporary entity to get its display name
                    Entity entity = type.create(level);
                    if (entity != null) {
                        entity.load(entityTag);
                        tooltipComponents.add(Component.literal("§eStored Entity Data: ").append(entity.getDisplayName()));
                        return;
                    }
                } catch (Exception ignored) {
                    // Handle edge case: corrupt or invalid NBT
                }
            }

            if (entityTag.contains("UUID")) {
                if (level != null) {
                    tooltipComponents.add(Component.literal("§eStored Player Data: " + level.getPlayerByUUID(entityTag.getUUID("UUID")).getName().getString()));
                }
            } else {
                // Fallback if entity can't be created
                tooltipComponents.add(Component.literal("§eStored Entity Data: " + entityId));
            }

        }
    }
}
