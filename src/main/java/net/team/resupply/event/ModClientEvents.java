package net.team.resupply.event;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.team.resupply.ResupplyMod;
import net.team.resupply.client.renderer.entity.ResupplyPodRenderer;
import net.team.resupply.entity.ModEntities;
import software.bernie.geckolib.GeckoLib;

import java.io.IOException;

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

    @Mod.EventBusSubscriber(modid = ResupplyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public class ModClientBusEvents {

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.RESUPPLY_POD.get(), ResupplyPodRenderer::new);
        }
    }
}
