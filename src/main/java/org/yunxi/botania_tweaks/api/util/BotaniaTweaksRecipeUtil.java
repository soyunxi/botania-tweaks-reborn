package org.yunxi.botania_tweaks.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.yunxi.botania_tweaks.BotaniaTweaks;
import org.yunxi.botania_tweaks.data.recipes.TerrestrialTweaksRecipe;

import java.util.ArrayList;
import java.util.List;

public class BotaniaTweaksRecipeUtil {

    public static TerrestrialTweaksRecipe getTerrestrialTweaksRecipe(Level level, BlockPos pos) {
        if (level == null) return null;

        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class,
                new AABB(pos, pos.offset(1, 1, 1)), EntitySelector.ENTITY_STILL_ALIVE);
        List<ItemStack> items = new ArrayList<>();
        for (ItemEntity entity : itemEntities) {
            if (!entity.getItem().isEmpty()) {
                items.add(entity.getItem());
            }
        }
        if (items.isEmpty()) return null;

        SimpleContainer inv = new SimpleContainer(items.toArray(new ItemStack[0]));

        return level.getRecipeManager()
                .getAllRecipesFor(BotaniaTweaks.TERRESTRIAL_TWEAKS_TYPE.get()).stream()
                .filter(r -> r.matches(inv, level))
                .filter(r -> r.checkMultiblock(level, pos.below()))
                .findAny().orElse(null);
    }

    public static List<TerrestrialTweaksRecipe> getTerrestrialTweaksRecipeNoContainer(Level level, BlockPos pos) {
        if (level == null) return null;
        return level.getRecipeManager()
                .getAllRecipesFor(BotaniaTweaks.TERRESTRIAL_TWEAKS_TYPE.get()).stream()
                .filter(r -> r.checkMultiblock(level, pos.below()))
                .findAny().stream().toList();
    }
}
