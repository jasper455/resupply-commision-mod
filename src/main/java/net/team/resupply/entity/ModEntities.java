package net.team.resupply.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.team.resupply.ResupplyMod;
import net.team.resupply.entity.custom.ResupplyOrbProjectileEntity;
import net.team.resupply.entity.custom.ResupplyPodEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ResupplyMod.MOD_ID);

    public static final RegistryObject<EntityType<ResupplyOrbProjectileEntity>> RESUPPLY_ORB =
            ENTITY_TYPES.register("resupply_orb", () -> EntityType.Builder.<ResupplyOrbProjectileEntity>of(ResupplyOrbProjectileEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f).build("resupply_orb"));

    public static final RegistryObject<EntityType<ResupplyPodEntity>> RESUPPLY_POD =
            ENTITY_TYPES.register("resupply_pod", () -> EntityType.Builder.<ResupplyPodEntity>of(ResupplyPodEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f).build("resupply_pod"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
