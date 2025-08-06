package net.team.resupply.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.team.resupply.ResupplyMod;
import net.team.resupply.item.custom.ResupplyEntityOrbItem;
import net.team.resupply.item.custom.ResupplyOrbItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ResupplyMod.MOD_ID);

    public static final RegistryObject<Item> RESUPPLY_ORB = ITEMS.register("resupply_orb",
            () -> new ResupplyOrbItem(new Item.Properties()));

    public static final RegistryObject<Item> RESUPPLY_ENTITY_ORB = ITEMS.register("resupply_entity_orb",
            () -> new ResupplyEntityOrbItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}