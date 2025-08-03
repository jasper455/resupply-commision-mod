package net.team.resupply.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.team.resupply.ResupplyMod;
import net.team.resupply.client.model.entity.SupportHellpodModel;
import net.team.resupply.entity.custom.ResupplyPodEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ResupplyPodRenderer extends GeoEntityRenderer<ResupplyPodEntity> {
    public ResupplyPodRenderer(EntityRendererProvider.Context context) {
        super(context, new SupportHellpodModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public ResourceLocation getTextureLocation(ResupplyPodEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ResupplyMod.MOD_ID, "textures/entity/resupply_pod/resupply_pod.png");
    }

    @Override
    public void render(ResupplyPodEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        // Rotate the model 180 degrees around the Y axis
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(ResupplyPodEntity pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }
}