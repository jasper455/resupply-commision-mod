package net.team.resupply.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.team.resupply.ResupplyMod;
import net.team.resupply.entity.custom.ResupplyOrbProjectileEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ResupplyMod.MOD_ID);

    public static final RegistryObject<EntityType<ResupplyOrbProjectileEntity>> RESUPPLY_ORB =
            ENTITY_TYPES.register("resupply_orb", () -> EntityType.Builder.<ResupplyOrbProjectileEntity>of(ResupplyOrbProjectileEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f).build("resupply_orb"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
