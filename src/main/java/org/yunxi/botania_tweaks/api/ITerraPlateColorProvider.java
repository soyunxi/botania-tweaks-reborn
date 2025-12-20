package org.yunxi.botania_tweaks.api;

public interface ITerraPlateColorProvider {
    int botaniatweaks$getColor1();

    int botaniatweaks$getColor2();

    boolean botaniatweaks$hasCustomRecipe();

    void botaniatweaks$setColors(int color1, int color2, boolean hasCustomRecipe);
}