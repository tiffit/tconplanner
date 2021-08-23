package net.tiffit.tconplanner;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {

    public static final Config CONFIG;
    public static final ForgeConfigSpec SPEC;

    public final ForgeConfigSpec.ConfigValue<Integer> buttonX;
    public final ForgeConfigSpec.ConfigValue<Integer> buttonY;

    public Config(ForgeConfigSpec.Builder builder){
        builder.push("UI Button");
        buttonX = builder.comment("X position of the \"Open Planner\" button. Default: 155").defineInRange("X Position", 155, -300, 300);
        buttonY = builder.comment("Y position of the \"Open Planner\" button. Default: 8").defineInRange("Y Position", 8, -300, 300);
        builder.pop();
    }

    static {
        Pair<Config, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Config::new);
        CONFIG = commonSpecPair.getLeft();
        SPEC = commonSpecPair.getRight();
    }

}
