package net.team.resupply.client.model.entity;

import net.minecraft.resources.ResourceLocation;
import net.team.resupply.ResupplyMod;
import net.team.resupply.entity.custom.ResupplyPodEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SupportHellpodModel extends DefaultedEntityGeoModel<ResupplyPodEntity> {
    public SupportHellpodModel() {
        super(ResourceLocation.fromNamespaceAndPath(ResupplyMod.MOD_ID, "resupply_pod"));
    }
}