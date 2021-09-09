package net.tiffit.tconplanner.util;

import com.google.common.collect.Lists;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MaterialSort<T extends IMaterialStats> {

    public static final HashMap<Class<? extends IMaterialStats>, List<MaterialSort<?>>> MAP = new HashMap<>();

    public final Comparator<T> comparator;
    public final String text;
    public final Icon icon;

    public MaterialSort(Comparator<T> comparator, String text, Icon icon) {
        this.comparator = comparator;
        this.text = text;
        this.icon = icon;
    }

    @SuppressWarnings("unchecked")
    public int compare(IMaterialStats stats1, IMaterialStats stats2){
        return comparator.compare((T) stats1, (T) stats2);
    }

    public int compare(IMaterial mat1, IMaterial mat2, MaterialStatsId statsId){
        IMaterialRegistry registry = MaterialRegistry.getInstance();
        Optional<IMaterialStats> ostats1 = registry.getMaterialStats(mat1.getIdentifier(), statsId);
        Optional<IMaterialStats> ostats2 = registry.getMaterialStats(mat2.getIdentifier(), statsId);
        if(ostats1.isPresent() && !ostats2.isPresent())return -1;
        if(!ostats1.isPresent() && ostats2.isPresent())return 1;
        if(!ostats1.isPresent())return 0;
        IMaterialStats stats1 = ostats1.get();
        IMaterialStats stats2 = ostats2.get();
        return compare(stats1, stats2);
    }

    private static <T extends IMaterialStats> void add(Class<T> type, MaterialSort<T> sort){
        List<MaterialSort<?>> list;
        if((list = MAP.putIfAbsent(type, Lists.newArrayList(sort))) != null)list.add(sort);
    }

    static {
        add(HandleMaterialStats.class, new MaterialSort<>(Comparator.comparingDouble(HandleMaterialStats::getDurability), "Durability Multiplier", new Icon(0, 1)));
        add(HandleMaterialStats.class, new MaterialSort<>(Comparator.comparingDouble(HandleMaterialStats::getMiningSpeed), "Mining Speed", new Icon(2, 1)));
        add(HandleMaterialStats.class, new MaterialSort<>(Comparator.comparingDouble(HandleMaterialStats::getAttackSpeed), "Attack Speed", new Icon(3, 1)));
        add(HandleMaterialStats.class, new MaterialSort<>(Comparator.comparingDouble(HandleMaterialStats::getAttackDamage), "Attack Damage", new Icon(4, 1)));


        add(HeadMaterialStats.class, new MaterialSort<>(Comparator.comparingInt(HeadMaterialStats::getDurability), "Durability", new Icon(1, 1)));
        add(HeadMaterialStats.class, new MaterialSort<>(Comparator.comparingInt(HeadMaterialStats::getHarvestLevel), "Harvest Level", new Icon(5, 1)));
        add(HeadMaterialStats.class, new MaterialSort<>(Comparator.comparingDouble(HeadMaterialStats::getMiningSpeed), "Mining Speed", new Icon(2, 1)));
        add(HeadMaterialStats.class, new MaterialSort<>(Comparator.comparingDouble(HeadMaterialStats::getAttack), "Attack Damage", new Icon(4, 1)));
    }
}
