package net.team.resupply.screen.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.team.resupply.block.entity.custom.ResupplyTerminalBlockEntity;
import net.team.resupply.screen.ModMenuTypes;

public class ResupplyTerminalMenu extends AbstractContainerMenu {
    private final ResupplyTerminalBlockEntity blockEntity;
    private final Level level;

    public ResupplyTerminalMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenuTypes.RESUPPLY_TERMINAL_MENU.get(), containerId);

        this.blockEntity = ((ResupplyTerminalBlockEntity) blockEntity);
        this.level = playerInventory.player.level();

        // Add the inventory slot
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 80, 35));
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    // Client-side constructor
    public ResupplyTerminalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));

        // Add the inventory slot
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 80, 35));
        });


        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }


    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 1) {
                // If the item is in the hellpod inventory, try to move it to player inventory
                if (!this.moveItemStackTo(itemstack1, 1, 1 + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // If the item is in the player inventory, try to move it to hellpod inventory
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
                onChanged(player);
            }
        }

        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
//            BlockEntity blockEntity = player.level().getBlockEntity(this.pos);
//            if (blockEntity instanceof ExtractionTerminalBlockEntity extractionTerminal) {
//                extractionTerminal.savePlayerInventory(player, this.inventory);
//            }

        }
    }

    public void onChanged(Player player) {
        Slot slot = this.slots.get(0);
//        BlockEntity blockEntity = player.level().getBlockEntity(this.pos);
//            if (blockEntity instanceof ResupplyTerminalBlockEntity extractionTerminal) {
//                extractionTerminal.savePlayerInventory(player, this.inventory);
//            }
    }

}
