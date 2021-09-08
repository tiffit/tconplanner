package net.tiffit.tconplanner.util;

import net.minecraft.util.text.TranslationTextComponent;

public final class TranslationUtil {

    public static TranslationTextComponent createComponent(String key, Object... inserts){
        return new TranslationTextComponent("gui.tconplanner." + key, inserts);
    }

}
