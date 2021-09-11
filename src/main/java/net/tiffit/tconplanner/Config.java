package net.tiffit.tconplanner;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {

    public static final Config CONFIG;
    public static final ForgeConfigSpec SPEC;

    public final ForgeConfigSpec.IntValue buttonX;
    public final ForgeConfigSpec.IntValue buttonY;
    public final ForgeConfigSpec.EnumValue<ScrollDirectionEnum> scrollDirection;

    public Config(ForgeConfigSpec.Builder builder){
        builder.push("UI Button");
        buttonX = builder.comment("X position of the \"Open Planner\" button. Default: 155").defineInRange("X Position", 155, -300, 300);
        buttonY = builder.comment("Y position of the \"Open Planner\" button. Default: 8").defineInRange("Y Position", 8, -300, 300);
        builder.pop();
        builder.push("Planner");
        scrollDirection = builder.comment("The scroll direction for paginated lists").defineEnum("Scroll Direction", ScrollDirectionEnum.DOWN);
        builder.pop();
    }

    static {
        Pair<Config, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Config::new);
        CONFIG = commonSpecPair.getLeft();
        SPEC = commonSpecPair.getRight();
    }

    public enum ScrollDirectionEnum{
        UP(1), DOWN(-1);

        public final int mult;

        ScrollDirectionEnum(int mult){
            this.mult = mult;
        }
    }

}
