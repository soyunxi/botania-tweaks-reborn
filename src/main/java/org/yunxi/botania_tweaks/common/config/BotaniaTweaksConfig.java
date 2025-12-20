package org.yunxi.botania_tweaks.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class BotaniaTweaksConfig {
    public static final ForgeConfigSpec SPEC;
    public static final Server SERVER;

    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static class Server {
        //魔力池
        public final ForgeConfigSpec.IntValue dilutedManaPool;
        public final ForgeConfigSpec.IntValue manaPool;
        public final ForgeConfigSpec.IntValue fabulousManaPool;
        public final ForgeConfigSpec.IntValue everlastingGuiltyPool;
        //火花
        public final ForgeConfigSpec.IntValue sparkScanRange;
        public final ForgeConfigSpec.IntValue sparkTransferRate;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("manaPool");
            dilutedManaPool = builder
                    .comment("Maximum mana storage capacity of the Diluted Mana Pool. Defaults of 10000")
                    .defineInRange("maxDilutedManaPool", 10000, 1, Integer.MAX_VALUE);

            manaPool = builder
                    .comment("Maximum mana storage capacity of the Fabulous Mana Pool. Defaults of 1000000")
                    .defineInRange("maxManaPool", 1000000, 1, Integer.MAX_VALUE);
            fabulousManaPool = builder
                    .comment("Maximum mana storage capacity of the Fabulous Mana Pool. Defaults of 5000000")
                    .defineInRange("maxFabulousManaPool", 5000000, 1, Integer.MAX_VALUE);
            everlastingGuiltyPool = builder
                    .comment("Maximum mana storage capacity of the The Everlasting Guilty Pool. Defaults of 2147483647")
                    .defineInRange("maxEverlastingGuiltyPool", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
            builder.pop();

            builder.push("spark");
            sparkScanRange = builder
                    .comment("Spark transmission range. Defaults of 12")
                    .defineInRange("sparkScanRange", 12, 1, Integer.MAX_VALUE);
            sparkTransferRate = builder
                    .comment("Spark TransferRate. Defaults of 1000")
                    .defineInRange("sparkTransferRate", 1000, 1, Integer.MAX_VALUE);
            builder.pop();
        }

        public static int getMaxDilutedManaPool() {
            return SERVER.dilutedManaPool.get();
        }

        public static int getMaxManaPool() {
            return SERVER.manaPool.get();
        }

        public static int getMaxFabulousManaPool() {
            return SERVER.fabulousManaPool.get();
        }

        public static int getEverlastingGuiltyPool() {
            return SERVER.everlastingGuiltyPool.get();
        }

        public static int getSparkScanRange() {
            return SERVER.sparkScanRange.get();
        }

        public static int getSparkTransferRate() {
            return SERVER.sparkTransferRate.get();
        }

    }
}
