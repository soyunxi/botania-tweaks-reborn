package org.yunxi.botania_tweaks;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.yunxi.botania_tweaks.common.config.BotaniaTweaksConfig;
import org.yunxi.botania_tweaks.data.recipes.TerrestrialTweaksRecipe;
import org.yunxi.botania_tweaks.data.recipes.TerrestrialTweaksRecipeSerializer;

// The value here should match an entry in the META-INF/mods.toml file
@SuppressWarnings("removal")
@Mod(BotaniaTweaks.MODID)
public class BotaniaTweaks {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "botania_tweaks";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MODID);

    public static final RegistryObject<RecipeSerializer<TerrestrialTweaksRecipe>> TERRESTRIAL_TWEAKS_SERIALIZER =
            SERIALIZERS.register("terrestrial_tweaks", TerrestrialTweaksRecipeSerializer::new);
    public static final RegistryObject<RecipeType<TerrestrialTweaksRecipe>> TERRESTRIAL_TWEAKS_TYPE = TYPES.register("terrestrial_tweaks", () -> new RecipeType<>() {
        @Override
        public String toString() {
            return MODID + ":terrestrial_tweaks";
        }
    });

    public BotaniaTweaks() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext modLoadingContext = ModLoadingContext.get();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        SERIALIZERS.register(modEventBus);
        TYPES.register(modEventBus);

        modLoadingContext.registerConfig(ModConfig.Type.COMMON, BotaniaTweaksConfig.SPEC, "botania_tweaks-common.toml");

    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }


}
