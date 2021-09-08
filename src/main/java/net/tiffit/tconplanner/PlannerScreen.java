package net.tiffit.tconplanner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.screen.*;
import net.tiffit.tconplanner.util.MaterialSort;
import net.tiffit.tconplanner.util.TranslationUtil;
import org.lwjgl.glfw.GLFW;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.RecipeTypes;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.client.SlotInformationLoader;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotInformation;
import slimeknights.tconstruct.tables.client.inventory.table.TinkerStationScreen;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlannerScreen extends Screen {

    public static ResourceLocation TEXTURE = new ResourceLocation(TConPlanner.MODID, "textures/gui/planner.png");
    private final HashMap<String, Object> cache = new HashMap<>();
    public Deque<Runnable> postRenderTasks = new ArrayDeque<>();
    private final TinkerStationScreen child;
    private final List<SlotInformation> tools = new ArrayList<>();
    private final List<IDisplayModifierRecipe> modifiers;
    private final PlannerData data;
    public Blueprint blueprint;
    public int selectedPart = 0;
    public int materialPage = 0;
    public MaterialSort<?> sorter;
    public ModifierInfo selectedModifier;
    public int left, top, guiWidth, guiHeight;

    public PlannerScreen(TinkerStationScreen child) {
        super(TranslationUtil.createComponent("name"));
        this.child = child;
        data = TConPlanner.DATA;
        for (SlotInformation info : SlotInformationLoader.getSlotInformationList()) {
            if (!info.isRepair()) {
                tools.add(info);
            }
        }
        try {
            data.load();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        modifiers = getModifierRecipes();
    }

    @Override
    protected void init() {
        guiWidth = 175;
        guiHeight = 204;
        left = width / 2 - guiWidth/2;
        top = height / 2 - guiHeight/2;
        refresh();
    }

    public void refresh(){
        // Reset screen
        buttons.clear();
        children.clear();
        int toolSpace = 20;
        addButton(new ToolSelectPanel(left - toolSpace * 5, top, toolSpace*5, toolSpace*3 + 23 + 4, tools, this));
        if(data.saved.size() > 0) {
            addButton(new BookmarkSelectPanel(left - toolSpace * 5, top + 15 + 18*4, toolSpace * 5, toolSpace * 5 + 23 + 4, data, this));
        }
        //Everything in here should only be added if there is a tool selected
        if(blueprint != null){
            ItemStack result = blueprint.createOutput();
            ToolStack resultStack = result.isEmpty() ? null : ToolStack.from(result);
            addButton(new ToolTopPanel(left, top, guiWidth, guiHeight, result, resultStack, data,this));
            if(selectedPart != -1){
                addButton(new MaterialSelectPanel(left, top, guiWidth, guiHeight, this));
            }
            if(resultStack != null) {
                addButton(new ModifierPanel(left + guiWidth, top, 100, guiHeight, result, resultStack, modifiers, this));
            }
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
        renderBackground(stack);
        bindTexture();
        this.blit(stack, left, top, 0, 0, guiWidth, guiHeight);
        ITextComponent title = blueprint == null ? TranslationUtil.createComponent("notool") : blueprint.toolSlotInfo.getToolForRendering().getHoverName();
        drawCenteredString(stack, font, title, left + guiWidth / 2, top + 7, 0xffffffff);

        super.render(stack, mouseX, mouseY, partialTick);
        Runnable task;
        while((task = postRenderTasks.poll()) != null)task.run();
    }

    public void setSelectedTool(int index) {
        setBlueprint(new Blueprint(tools.get(index)));
    }

    public void setBlueprint(Blueprint bp){
        blueprint = bp;
        this.materialPage = 0;
        sorter = null;
        selectedModifier = null;
        setSelectedPart(-1);
    }

    public void setSelectedPart(int index) {
        this.selectedPart = index;
        this.materialPage = 0;
        sorter = null;
        refresh();
    }

    public void setPart(IMaterial material){
        blueprint.materials[selectedPart] = material;
        selectedModifier = null;
        refresh();
    }

    @Override
    public boolean keyPressed(int key, int p_231046_2_, int p_231046_3_) {
        InputMappings.Input mouseKey = InputMappings.getKey(key, p_231046_2_);
        if (super.keyPressed(key, p_231046_2_, p_231046_3_)) {
            return true;
        } else if (minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        if(key == GLFW.GLFW_KEY_B && blueprint != null && blueprint.isComplete()){
            if(data.isBookmarked(blueprint))unbookmarkCurrent();
            else bookmarkCurrent();
            return true;
        }
        return false;
    }

    public void renderItemTooltip(MatrixStack mstack, ItemStack stack, int x, int y) {
        renderTooltip(mstack, stack, x, y);
    }


    @Override
    public void onClose() {
        minecraft.setScreen(child);
    }

    public static void bindTexture(){
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
    }

    public void bookmarkCurrent(){
        if(blueprint.isComplete()){
            data.saved.add(blueprint);
            try {
                data.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        refresh();
    }

    public void unbookmarkCurrent(){
        if(blueprint.isComplete()){
            data.saved.removeIf(blueprint1 -> blueprint1.equals(blueprint));
            try {
                data.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        refresh();
    }

    public void randomize(){
        if(blueprint != null){
            setBlueprint(new Blueprint(blueprint.toolSlotInfo));
            Random r = new Random();
            List<IMaterial> materials = new ArrayList<>(MaterialRegistry.getMaterials());
            for (int i = 0; i < blueprint.parts.length; i++) {
                IToolPart part = blueprint.parts[i];
                List<IMaterial> usable = materials.stream().filter(part::canUseMaterial).collect(Collectors.toList());
                if(usable.size() > 0)blueprint.materials[i] = usable.get(r.nextInt(usable.size()));
            }
            selectedModifier = null;
            refresh();
        }
    }

    public void sort(MaterialSort<?> sort){
        if(sorter == sort)sorter = null;
        else sorter = sort;
        refresh();
    }

    public <T> T getCacheValue(String key, T defaultVal){
        return (T)cache.getOrDefault(key, defaultVal);
    }

    public void setCacheValue(String key, Object value){
        cache.put(key, value);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static List<IDisplayModifierRecipe> getModifierRecipes(){
        Set<ModifierId> modifierNameSet = new HashSet<>();
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        return RecipeHelper.getJEIRecipes(recipeManager, RecipeTypes.TINKER_STATION, IDisplayModifierRecipe.class)
                .stream().filter(e -> modifierNameSet.add(e.getDisplayResult().getModifier().getRegistryName()))
                .filter(recipe -> recipe instanceof ITinkerStationRecipe).collect(Collectors.toList());
    }
}
