package net.team.resupply.event;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.team.resupply.ResupplyMod;
import net.team.resupply.entity.client.ModModelLayers;
import net.team.resupply.entity.client.ResupplyOrbProjectileModel;
import net.team.resupply.network.PacketHandler;


@Mod.EventBusSubscriber(modid = ResupplyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.RESUPPLY_ORB, ResupplyOrbProjectileModel::createBodyLayer);
    }

//    @SubscribeEvent
//    public static void registerAttributes(EntityAttributeCreationEvent event) {
//        event.put(ModEntities.EAGLE_AIRSHIP.get(), EagleAirshipEntity.createAttributes().build());
//        event.put(ModEntities.ORBITAL_LASER.get(), OrbitalLaserEntity.createAttributes().build());
//    }
//
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
        });
    }
}
