package net.team.resupply.entity.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.team.resupply.ResupplyMod;

public class ModModelLayers {
    public static final ModelLayerLocation RESUPPLY_ORB = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResupplyMod.MOD_ID, "resupply_orb"), "main");

}
