package net.team.resupply.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.team.resupply.ResupplyMod;
import net.team.resupply.block.ModBlocks;
import net.team.resupply.block.entity.custom.ResupplyTerminalBlockEntity;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ResupplyMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<ResupplyTerminalBlockEntity>> RESUPPLY_TERMINAL = BLOCK_ENTITIES
            .register("resupply_terminal", () -> BlockEntityType.Builder
                    .of(ResupplyTerminalBlockEntity::new, ModBlocks.RESUPPLY_TERMINAL.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
