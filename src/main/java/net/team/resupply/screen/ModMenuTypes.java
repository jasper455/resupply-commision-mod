package net.team.resupply.screen;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.team.resupply.ResupplyMod;
import net.team.resupply.entity.custom.ResupplyPodEntity;
import net.team.resupply.screen.custom.ResupplyPodMenu;
import net.team.resupply.screen.custom.ResupplyTerminalMenu;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ResupplyMod.MOD_ID);

    public static final RegistryObject<MenuType<ResupplyTerminalMenu>> RESUPPLY_TERMINAL_MENU =
            registerMenuType("resupply_terminal_menu", ResupplyTerminalMenu::new);

    public static final RegistryObject<MenuType<ResupplyPodMenu>> RESUPPLY_POD_MENU =
            MENUS.register("resupply_pod_menu",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        // Read the entity ID from the packet
                        int entityId = data.readInt();
                        Level level = inv.player.level();
                        Entity entity = level.getEntity(entityId);

                        if (entity instanceof ResupplyPodEntity hellpodEntity) {
                            return new ResupplyPodMenu(windowId, inv, hellpodEntity);
                        }

                        throw new IllegalStateException("Invalid entity type for menu!");
                    }));

    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
