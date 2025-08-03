package net.team.resupply.entity.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.team.resupply.entity.ModEntities;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ResupplyPodEntity extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final RawAnimation DEPLOY = RawAnimation.begin().thenPlayAndHold("deploy");
    public static final RawAnimation EMPTY = RawAnimation.begin().thenPlayAndHold("empty");
    public static final RawAnimation FALL = RawAnimation.begin().thenLoop("fall");
    public static final RawAnimation LAND = RawAnimation.begin().thenPlayAndHold("land");

    public SimpleContainer inventory;

    private int groundedTicks = 0;
    private int clickedTicks = 0;
    private boolean shouldStopCounting = false;
    private boolean hasBeenClicked = false;
    private boolean hasLanded = false;
    private boolean hasBeenSet = false;
    public String stratagemType = "";

    public ResupplyPodEntity(EntityType<? extends Entity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.inventory = new SimpleContainer(2); // Initialize with 2 slots
        initializeInventory();
    }

    public ResupplyPodEntity(Level level, String stratagemType) {
        super(ModEntities.RESUPPLY_POD.get(), level);
        this.inventory = new SimpleContainer(2); // Initialize with 2 slots
        this.stratagemType = stratagemType;
        initializeInventory();
    }

    public boolean isGrounded() {
        return hasLanded;
    }

    @Override
    protected float getEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height * 0.5F; // This centers the eye height
    }


    @Override
    public void tick() {
        // Check if we've landed by looking for solid blocks below
        BlockPos belowPos = this.blockPosition().below();
        boolean onSolidBlock = this.level().getBlockState(belowPos).isSolid();

        if (onSolidBlock && this.getDeltaMovement().y <= 0) {
            if (!hasLanded) {
                hasLanded = true;
                // Play landing sound if needed
                this.playSound(SoundEvents.DRAGON_FIREBALL_EXPLODE, 1.0F, 1.0F);
                this.setDeltaMovement(Vec3.ZERO);
                if (!this.level().isClientSide()) {
                    for (int i = 0; i < getItemStacksFromContainerItem().size(); i++) {
                        ItemEntity itemEntity = new ItemEntity(
                                level(),
                                this.blockPosition().getX() + 0.5,
                                this.blockPosition().getY() + 1,
                                this.blockPosition().getZ() + 0.5,
                                getItemStacksFromContainerItem().get(i)
                        );
                        this.level().addFreshEntity(itemEntity);
                    }
                }
            }
            groundedTicks++;
        }

        if (!hasLanded) {
            // Apply gravity
            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(movement.x, movement.y - 0.04, movement.z);
            this.move(MoverType.SELF, this.getDeltaMovement());
        }

        if (this.isGrounded()) {
            groundedTicks++;
        }
        if (this.isGrounded() && !hasBeenSet) {
            this.setPos(this.getX(), this.getY(), this.getZ());
            hasBeenSet = true;
        }
        if (hasBeenClicked && hasBeenSet && !shouldStopCounting) {
            clickedTicks = groundedTicks;
            shouldStopCounting = true;
        }
        if (groundedTicks >= clickedTicks + 60 && hasBeenClicked) {
            this.discard();
        }

        if (!this.isGrounded()) {
            this.level().getEntitiesOfClass(LivingEntity.class, new AABB(this.getOnPos()).inflate(1.0)).forEach(entity -> {
                entity.hurt(level().damageSources().explosion(null), 9999.0F);
            });
            BlockPos pos = new BlockPos((int) this.getX(), (int) (this.getY() - 5), (int) this.getZ());
//            PacketHandler.sendToServer(new SHellpodDestroyBlocksPacket(pos, 2));
        }
        if (this.isGrounded() && groundedTicks <= 10 && !this.level().isClientSide()) {
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, this.getX(), this.getY(), this.getZ(),
                    5, 0f, 0f, 0f, 0.25f);
        }
        super.tick();
    }

//    @Override
//    public InteractionResult interactAt(Player pPlayer, Vec3 pVec, InteractionHand pHand) {
//        if (!this.level().isClientSide && this.isGrounded()) {
//            if (pPlayer instanceof ServerPlayer serverPlayer) {
//                NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
//                    @Override
//                    public Component getDisplayName() {
//                        return Component.literal("Support Hellpod");
//                    }
//
//                    @Override
//                    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
//                        return new SupportHellpodMenu(pContainerId, pPlayerInventory, inventory, ResupplyPodEntity.this);
//                    }
//                }, buffer -> buffer.writeInt(getId()));
//            }
//            return InteractionResult.CONSUME;
//        }
//
//        return InteractionResult.SUCCESS;
//    }

    private PlayState animations(AnimationState event) {
        if (isGrounded() && groundedTicks >= 20 && hasBeenClicked) {
            event.getController().setAnimation(EMPTY);
            return PlayState.CONTINUE;
        }
        if (isGrounded() && groundedTicks >= 20) {
            event.getController().setAnimation(DEPLOY);
            return PlayState.CONTINUE;
        }
        if (!isGrounded()) {
            event.getController().setAnimation(FALL);
            return PlayState.CONTINUE;
        } else {
            event.getController().setAnimation(LAND);
            return PlayState.CONTINUE;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        AnimationController animationController = new AnimationController(this, "animationController", 0, this::animations);
        data.add(animationController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !hasBeenClicked;  // Allow the entity to be collided with, enabling interaction
    }

    @Override
    public boolean isPushable() {
        return false;  // Prevent the entity from being pushed by other entities
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemstack = inventory.getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemstack.save(itemTag);
                listTag.add(itemTag);
            }
        }
        pCompound.put("Items", listTag);
    }
    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        ListTag listTag = pCompound.getList("Items", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompound(i);
            int slot = itemTag.getInt("Slot");
            if (slot >= 0 && slot < inventory.getContainerSize()) {
                inventory.setItem(slot, ItemStack.of(itemTag));
            }
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    private void checkInventoryEmpty() {
        if (level().isClientSide) {
            return; // Only run this on server side
        }

        boolean isEmpty = true;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                isEmpty = false;
                break;
            }
        }

        if (isEmpty) {
            hasBeenClicked = true;
            // Sync the hasBeenClicked state to clients
            this.level().broadcastEntityEvent(this, (byte) 1);
        }
    }

    // Add this method to handle the client-side sync

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 1) {
            hasBeenClicked = true;
        } else {
            super.handleEntityEvent(id);
        }
    }

    private void initializeInventory() {
        if (stratagemType.equals("")) {
            this.inventory.setItem(0, new ItemStack(Items.AIR));
            this.inventory.setItem(1, new ItemStack(Items.AIR));
        }
        if (stratagemType.equals("Resupply")) {
//            this.inventory.setItem(0, new ItemStack(ModBlocks.AMMO_CRATE.get(), 2));
//            this.inventory.setItem(1, new ItemStack(ModBlocks.AMMO_CRATE.get(), 2));
        }
    }

    public void onInventoryClosed() {
        checkInventoryEmpty();
    }

    public int getGroundedTicks() {
        return groundedTicks;
    }

    @Override
    protected void defineSynchedData() {}

    public java.util.List<ItemStack> getItemStacksFromContainerItem() {
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
