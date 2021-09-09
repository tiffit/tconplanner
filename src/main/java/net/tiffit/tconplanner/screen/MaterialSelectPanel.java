package net.tiffit.tconplanner.screen;

import com.google.common.collect.Lists;
import net.minecraft.util.SoundEvents;
import net.tiffit.tconplanner.screen.buttons.IconButton;
import net.tiffit.tconplanner.screen.buttons.MatPageButton;
import net.tiffit.tconplanner.screen.buttons.MaterialButton;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.util.MaterialSort;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class MaterialSelectPanel extends PlannerPanel{
    private static final int materialPageSize = 3*9;

    public MaterialSelectPanel(int x, int y, int width, int height, PlannerScreen parent) {
        super(x, y, width, height, parent);
        Blueprint blueprint = parent.blueprint;
        //Add material list for the tool part
        IToolPart part = blueprint.parts[parent.selectedPart];
        List<IMaterial> usable = MaterialRegistry.getMaterials().stream().filter(part::canUseMaterial).collect(Collectors.toList());
        MaterialStatsId statsId = part.getStatType();
        if(parent.sorter != null)usable.sort((o1, o2) -> parent.sorter.compare(o1, o2, statsId) * -1);
        int loopMin = parent.materialPage*materialPageSize;
        int loopMax = Math.min(usable.size(), (parent.materialPage+1)*materialPageSize);
        for (int i = loopMin; i < loopMax; i++) {
            int posIndex = i - loopMin;
            IMaterial mat = usable.get(i);
            MaterialButton data = new MaterialButton(i, mat, part.withMaterialForDisplay(mat.getIdentifier()), (posIndex % 9) * 18 + 8, parent.guiHeight - 87 + (posIndex / 9) * 18, parent);
            if(blueprint.materials[parent.selectedPart] == mat)data.selected = true;
            addChild(data);
        }
        //Add material pagination buttons
        MatPageButton leftPage = new MatPageButton(6, parent.guiHeight - 30, -1, parent);
        MatPageButton rightPage = new MatPageButton(parent.guiWidth - 6 - 37, parent.guiHeight - 30, 1, parent);
        leftPage.active = parent.materialPage > 0;
        rightPage.active = loopMax < usable.size();
        addChild(leftPage);
        addChild(rightPage);
        //Add sorting buttons
        Class<? extends IMaterialStats> statClass = MaterialRegistry.getClassForStat(part.getStatType());
        if(statClass != null){
            List<MaterialSort<?>> sorts = MaterialSort.MAP.getOrDefault(statClass, Lists.newArrayList());
            int startX = parent.guiWidth/2 - 6*sorts.size();
            for (int i = 0; i < sorts.size(); i++) {
                MaterialSort<?> sort = sorts.get(i);
                addChild(new IconButton(startX + i*12, parent.guiHeight - 30 + 3, sort.icon, TranslationUtil.createComponent("sort", sort.text), parent, e -> parent.sort(sort))
                        .withColor(sort == parent.sorter ? Color.WHITE : new Color(0.4f, 0.4f, 0.4f)).withSound(SoundEvents.PAINTING_PLACE));
            }
        }
    }
}