package net.team.resupply.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.team.resupply.ResupplyMod;
import net.team.resupply.client.renderer.entity.ResupplyPodRenderer;
import net.team.resupply.entity.ModEntities;

@Mod.EventBusSubscriber(modid = ResupplyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void onComputerFovModifierEvent(ComputeFovModifierEvent event) {
        if (Minecraft.getInstance().options.keySaveHotbarActivator.isDown()) {
            float fovModifier = 1f;
            float deltaTicks = ((float) 20) / 20f;
            deltaTicks *= deltaTicks;
            fovModifier *= 1f - deltaTicks * 0.5f;
            event.setNewFovModifier(fovModifier);
        }

        if (Minecraft.getInstance().options.keyLoadHotbarActivator.isDown()) {
            float fovModifier = 1f;
            float deltaTicks = ((float) 30) / 20f;
            deltaTicks *= deltaTicks;
            fovModifier *= 1f - deltaTicks * 0.5f;
            event.setNewFovModifier(fovModifier);
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        Entity vehicle = event.getEntity().getVehicle();
        if (vehicle != null && vehicle.getType() == ModEntities.RESUPPLY_POD.get()) {
            event.setCanceled(true); // Prevent damage
        }
    }

    @Mod.EventBusSubscriber(modid = ResupplyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public class ModClientBusEvents {

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.RESUPPLY_POD.get(), ResupplyPodRenderer::new);
        }
    }
}
