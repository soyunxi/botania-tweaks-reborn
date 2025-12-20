package org.yunxi.botania_tweaks.data.recipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yunxi.botania_tweaks.BotaniaTweaks;
import org.yunxi.botania_tweaks.api.util.MultiblockStructure;
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;

import java.util.ArrayList;
import java.util.List;

public class TerrestrialTweaksRecipe implements Recipe<Container>, TerrestrialAgglomerationRecipe {

    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack output;
    private final int manaCost;
    private final int color1;
    private final int color2;
    private final MultiblockStructure structure;

    @Nullable
    private final MultiblockStructure replacement;

    public TerrestrialTweaksRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack output, int manaCost, int color1, int color2, MultiblockStructure structure, @Nullable MultiblockStructure replacement) {
        this.id = id;
        this.ingredients = ingredients;
        this.output = output;
        this.manaCost = manaCost;
        this.color1 = color1;
        this.color2 = color2;
        this.structure = structure;
        this.replacement = replacement;
    }

    public @Nullable MultiblockStructure getReplacement() {
        return replacement;
    }

    @Override
    public int getMana() {
        return manaCost;
    }

    public int getColor1() {
        return color1;
    }

    public int getColor2() {
        return color2;
    }

    public MultiblockStructure getStructure() {
        return structure;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                for (int j = 0; j < stack.getCount(); j++) {
                    inputs.add(stack.copyWithCount(1));
                }
            }
        }

        if (inputs.size() != ingredients.size()) return false;

        List<Ingredient> tempIngredients = new ArrayList<>(ingredients);

        for (ItemStack input : inputs) {
            boolean found = false;
            for (int i = 0; i < tempIngredients.size(); i++) {
                if (tempIngredients.get(i).test(input)) {
                    tempIngredients.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        return tempIngredients.isEmpty();
    }

    public boolean checkMultiblock(Level level, BlockPos centerPos) {
        if (replacement != null) {
            int outputSize = replacement.getSize();
            return structure.matchesWithOutputCheck(level, centerPos, outputSize);
        }
        return structure.matches(level, centerPos);
    }

    public void transformBlocks(Level level, BlockPos centerPos) {
        if (replacement != null) {
            replacement.placeBlocks(level, centerPos);
        }
    }

    @Override
    public @NotNull ItemStack assemble(Container inv, RegistryAccess access) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return output;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BotaniaTweaks.TERRESTRIAL_TWEAKS_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return BotaniaTweaks.TERRESTRIAL_TWEAKS_TYPE.get();
    }
}