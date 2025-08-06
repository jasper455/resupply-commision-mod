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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.team.resupply.ResupplyMod;
import net.team.resupply.entity.custom.ResupplyOrbProjectileEntity;
import net.team.resupply.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ResupplyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ResupplyEntityOrbItem extends Item {
    public ResupplyEntityOrbItem(Properties properties) {
        super(properties);
    }

//    @Override
//    public UseAnim getUseAnimation(ItemStack pStack) {
//        return UseAnim.SPEAR;
//    }
//
//    @Override
//    public int getUseDuration(ItemStack pStack) {
//        return 72000;
//    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
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
                        tooltip.add(Component.literal("§eStored Entity: ").append(entity.getDisplayName()));
                        return;
                    }
                } catch (Exception ignored) {
                    // Handle edge case: corrupt or invalid NBT
                }
            }

            // Fallback if entity can't be created
            tooltip.add(Component.literal("§eStored Entity: " + entityId));
        } else {
            tooltip.add(Component.literal("§eRight-click an entity to capture it."));
        }
    }

    @SubscribeEvent
    public static void onEntityRightClick(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        Level level = player.level();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);

        CompoundTag tag = stack.getOrCreateTag();

        // Check if the item already has stored entity data
        if (tag.contains("StoredEntity") || tag.contains("StoredEntityId")) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("This capsule already contains an entity!"), true);
            }
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }

        // Only proceed with your custom item
        if (stack.getItem() == ModItems.RESUPPLY_ENTITY_ORB.get()) {
            Entity target = event.getTarget();
            if (target instanceof Player) {
                return;
            }

            // Serialize entity NBT
            CompoundTag entityData = new CompoundTag();
            if (target.saveAsPassenger(entityData)) { // better than save() for storing non-world placement
                // Remove UUID to avoid conflicts on respawn
                entityData.remove("UUID");

                // Save to item NBT
                stack.getOrCreateTag().put("StoredEntity", entityData);
                stack.getOrCreateTag().putString("StoredEntityId", target.getEncodeId());

                if (!level.isClientSide) {
                    target.remove(Entity.RemovalReason.DISCARDED); // Despawn original entity
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide && stack.hasTag() && stack.getTag().contains("StoredEntity")) {
            CompoundTag tag = stack.getTag().getCompound("StoredEntity");
            String id = stack.getTag().getString("StoredEntityId");

            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));

            if (type != null) {
                Entity entity = type.create(level);
                if (entity != null) {
                    entity.load(tag);
                    entity.setPos(context.getClickLocation().x, context.getClickLocation().y + 1, context.getClickLocation().z);
                    level.addFreshEntity(entity);

                    // Optional: remove entity data after spawning
                    stack.removeTagKey("StoredEntity");
                    stack.removeTagKey("StoredEntityId");
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
