package net.team.resupply.entity.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.team.resupply.entity.ModEntities;

import java.util.ArrayList;
import java.util.List;

public class ResupplyOrbProjectileEntity extends AbstractArrow {
    private static final EntityDataAccessor<String> STRATAGEM_TYPE =
            SynchedEntityData.defineId(ResupplyOrbProjectileEntity.class, EntityDataSerializers.STRING);
    private Direction ownerDirection;
    private float rotation;
    public Vec3 groundedOffset;
    private int groundedTicks = 0;

    public ResupplyOrbProjectileEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ResupplyOrbProjectileEntity(LivingEntity shooter, Level level, String stratagemType, Direction ownerDirection) {
        super(ModEntities.RESUPPLY_ORB.get(), shooter, level);
        this.ownerDirection = ownerDirection;
        setStratagemType(stratagemType);
    }

    public float getRenderingRotation() {
            rotation += 0.5f;
            if (rotation >= 360) {
                rotation = 0;
            }
        return rotation;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }


    public boolean isGrounded() {
        return inGround;
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        if(pResult.getDirection() == Direction.SOUTH) {
            groundedOffset = new Vec3(180f, 180f, Mth.randomBetween(RandomSource.create(), 0f, 360f));
        }
        if(pResult.getDirection() == Direction.NORTH) {
            groundedOffset = new Vec3(180f, 0f, Mth.randomBetween(RandomSource.create(), 0f, 360f));
        }
        if(pResult.getDirection() == Direction.EAST) {
            groundedOffset = new Vec3(180f, -90f, Mth.randomBetween(RandomSource.create(), 0f, 360f));
        }
        if(pResult.getDirection() == Direction.WEST) {
            groundedOffset = new Vec3(180f, 90f, Mth.randomBetween(RandomSource.create(), 0f, 360f));
        }

        if(pResult.getDirection() == Direction.DOWN) {
            groundedOffset = new Vec3(0f, 0f, Mth.randomBetween(RandomSource.create(), 0f, 360f));
        }
        if(pResult.getDirection() == Direction.UP) {
            groundedOffset = new Vec3(270f, 0f, Mth.randomBetween(RandomSource.create(), 0f, 360f));
        }

        if (getStratagemType() == null) this.discard();

    }

    @Override
    public void tick() {
            super.tick();
        if (this.isGrounded()) {
            groundedTicks++;
        }
//        if (this.isGrounded() && groundedTicks == 1 && !this.level().isClientSide()) {
//            this.playSound(ModSounds.STRATAGEM_ORB_LAND.get(), 3f, 1.0f);
//        }

        // Resupply Entity Stuff
        if (getStratagemType().equals("Resupply") && !this.level().isClientSide) {
            if (groundedTicks == 300) {
                if (this.getPersistentData().contains("StoredItem")) {
                    ResupplyPodEntity resupplyPodEntity = new ResupplyPodEntity(this.level(), "Resupply");
                    resupplyPodEntity.setPos(this.getX(), 200, this.getZ());
                    
                    resupplyPodEntity.getPersistentData().put("StoredItem", this.getPersistentData().getCompound("StoredItem"));

                    this.level().addFreshEntity(resupplyPodEntity);

                    // Get the owner (player)
                    if (this.getOwner() instanceof Player player) {
                        // Set the entity's rotation to face the player
                        double deltaX = player.getX() - this.getBlockX();
                        double deltaZ = player.getZ() - this.getBlockZ();
                        float yRot = (float) (Math.atan2(deltaZ, deltaX) * (180.0D / Math.PI)) - 90.0F;
                        resupplyPodEntity.setYRot(yRot);
                    }
                }
                if (this.getPersistentData().contains("StoredEntity") && this.getPersistentData().contains("StoredEntityId")) {
                    ResupplyPodEntity resupplyPodEntity = new ResupplyPodEntity(this.level(), "Entity");
                    resupplyPodEntity.setPos(this.getX(), 200, this.getZ());

                    resupplyPodEntity.getPersistentData().put("StoredEntity", this.getPersistentData().getCompound("StoredEntity"));
                    resupplyPodEntity.getPersistentData().put("StoredEntityId", this.getPersistentData().getCompound("StoredEntityId"));

                    this.level().addFreshEntity(resupplyPodEntity);

                    // Get the owner (player)
                    if (this.getOwner() instanceof Player player) {
                        // Set the entity's rotation to face the player
                        double deltaX = player.getX() - this.getBlockX();
                        double deltaZ = player.getZ() - this.getBlockZ();
                        float yRot = (float) (Math.atan2(deltaZ, deltaX) * (180.0D / Math.PI)) - 90.0F;
                        resupplyPodEntity.setYRot(yRot);
                    }
                }
            }
        }
        if (getStratagemType().equals("Resupply") && groundedTicks > 320) {
            this.discard();
            groundedTicks = 0;
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.AIR);
    }

//    @Override
//    protected SoundEvent getDefaultHitGroundSoundEvent() {
//        return ModSounds.STRATAGEM_ORB_LAND.get();
//    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STRATAGEM_TYPE, "");
    }

    public String getStratagemType() {
        return this.entityData.get(STRATAGEM_TYPE);
    }

    public void setStratagemType(String type) {
        this.entityData.set(STRATAGEM_TYPE, type);
    }

    public List<ItemStack> getItemStacksFromContainerItem() {
        List<ItemStack> items = new ArrayList<>();
        CompoundTag tag = this.getPersistentData().getCompound("StoredItem").getCompound("tag");
        // Shulker boxes, chests, etc. store items under BlockEntityTag.Items
        if (tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
            if (blockEntityTag.contains("Items", Tag.TAG_LIST)) {
                // Reconstruct the item list
                items = NonNullList.withSize(27, ItemStack.EMPTY); // 27 for shulker box, or dynamic size
                ContainerHelper.loadAllItems(blockEntityTag, (NonNullList<ItemStack>) items);
            }
        }

        return items;
    }

}