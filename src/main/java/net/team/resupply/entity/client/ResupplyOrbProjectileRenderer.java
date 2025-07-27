package net.team.resupply.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.team.resupply.ResupplyMod;
import net.team.resupply.entity.custom.ResupplyOrbProjectileEntity;

import static net.minecraft.client.renderer.blockentity.BeaconRenderer.BEAM_LOCATION;

public class ResupplyOrbProjectileRenderer extends EntityRenderer<ResupplyOrbProjectileEntity> {
    private ResupplyOrbProjectileModel model;
    public ResupplyOrbProjectileRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        model = new ResupplyOrbProjectileModel(pContext.bakeLayer(ModModelLayers.RESUPPLY_ORB));
    }

    @Override
    public void render(ResupplyOrbProjectileEntity pEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (pEntity.isGrounded()) {
            poseStack.pushPose();
            poseStack.translate(-0.5, 0, -0.5);

            float[] color = new float[]{0.51f, 0.996f, 1.0f, 1.0f};

            BeaconRenderer.renderBeaconBeam(poseStack, buffer, BEAM_LOCATION, partialTicks, 1,
                    Minecraft.getInstance().level.getGameTime(), 0, 999999,
                    color, 0.1f, 0.125f);

            poseStack.popPose();
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, pEntity.yRotO, pEntity.getYRot())));
            poseStack.mulPose(Axis.XP.rotationDegrees(pEntity.getRenderingRotation() * 5f));
        }
        poseStack.pushPose();
        poseStack.translate(0, 1, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(
                buffer, this.model.renderType(this.getTextureLocation(pEntity)), false, false);

        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);

        poseStack.popPose();

        super.render(pEntity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ResupplyOrbProjectileEntity pEntity) {
        return ResourceLocation.fromNamespaceAndPath(ResupplyMod.MOD_ID, "textures/entity/resupply_orb/resupply_orb.png");
    }

    @Override
    public boolean shouldRender(ResupplyOrbProjectileEntity pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }
}
