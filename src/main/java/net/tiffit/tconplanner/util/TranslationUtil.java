package net.tiffit.tconplanner.util;


import net.minecraft.network.chat.TranslatableComponent;

public final class TranslationUtil {

    public static TranslatableComponent createComponent(String key, Object... inserts){
        return new TranslatableComponent("gui.tconplanner." + key, inserts);
    }

}
