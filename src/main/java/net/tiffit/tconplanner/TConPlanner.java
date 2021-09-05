package net.tiffit.tconplanner;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.tiffit.tconplanner.data.PlannerData;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(TConPlanner.MODID)
public class TConPlanner {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "tconplanner";

    public static PlannerData DATA;

    public TConPlanner() {
        ModLoadingContext mlctx = ModLoadingContext.get();
        mlctx.registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        mlctx.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
    }

    private void setupClient(final FMLClientSetupEvent event) {
        File gameDir = event.getMinecraftSupplier().get().gameDirectory;
        File folder = new File(gameDir, MODID);
        DATA = new PlannerData(folder);
    }


}
