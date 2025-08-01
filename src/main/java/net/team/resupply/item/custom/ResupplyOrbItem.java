package net.team.resupply.item.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
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

                // Load ItemStack from tag
                ItemStack reconstructed = ItemStack.of(storedItemTag);
                int randomInt = Mth.randomBetweenInclusive(RandomSource.create(), 1, 27);
                player.getInventory().add(getItemStacksFromContainerItem(reconstructed).get(randomInt));

                ResupplyOrbProjectileEntity resupplyOrb = new ResupplyOrbProjectileEntity(player, pLevel,
                        "Resupply", player.getDirection());
                float power = Math.min(i / 20F, 1.5F); // Max power after 1 second charge
                resupplyOrb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 1.5F, 0F);
                resupplyOrb.getPersistentData().put("StoredItem", storedItemTag);

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

    public static List<ItemStack> getItemStacksFromContainerItem(ItemStack containerItem) {
        List<ItemStack> items = new ArrayList<>();

        if (containerItem.hasTag()) {
            CompoundTag tag = containerItem.getTag();

            // Shulker boxes, chests, etc. store items under BlockEntityTag.Items
            if (tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
                CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");

                if (blockEntityTag.contains("Items", Tag.TAG_LIST)) {
                    // Reconstruct the item list
                    items = NonNullList.withSize(27, ItemStack.EMPTY); // 27 for shulker box, or dynamic size

                    ContainerHelper.loadAllItems(blockEntityTag, (NonNullList<ItemStack>) items);
                }
            }
        }

        return items;
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

    }
}
