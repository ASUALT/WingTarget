package com.asualt.wingtarget.item;

import com.asualt.wingtarget.WingTarget;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item ONION = registerItem("onion", new Item(new Item.Settings()));

    public static Item registerItem(String name, Item item){
        return Registry.register(Registries.ITEM, Identifier.of(WingTarget.MOD_ID, name), item);
    }

    public static void registerModItems(){
        WingTarget.LOGGER.info("Registering mod item for" + WingTarget.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(ONION);
        });
    }
}
