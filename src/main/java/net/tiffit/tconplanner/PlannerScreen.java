package net.tiffit.tconplanner;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.*;
import net.tiffit.tconplanner.buttons.*;
import net.tiffit.tconplanner.buttons.modifiers.ModExitButton;
import net.tiffit.tconplanner.buttons.modifiers.ModLevelButton;
import net.tiffit.tconplanner.buttons.modifiers.ModPreviewWidget;
import net.tiffit.tconplanner.buttons.modifiers.ModifierSelectButton;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.util.DummyTinkersStationInventory;
import net.tiffit.tconplanner.util.MaterialSort;
import net.tiffit.tconplanner.util.ToolValidator;
import net.tiffit.tconplanner.util.TranslationUtil;
import org.lwjgl.glfw.GLFW;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.recipe.RecipeTypes;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.client.SlotInformationLoader;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotInformation;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotPosition;
import slimeknights.tconstruct.tables.client.inventory.table.TinkerStationScreen;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PlannerScreen extends Screen {

    public static ResourceLocation TEXTURE = new ResourceLocation(TConPlanner.MODID, "textures/gui/planner.png");
    protected static final String KEY_MAX_LEVEL = TConstruct.makeTranslationKey("recipe", "modifier.max_level");
    private final HashMap<String, Object> cache = new HashMap<>();
    public Deque<Runnable> postRenderTasks = new ArrayDeque<>();
    private final TinkerStationScreen child;
    private final List<SlotInformation> tools = new ArrayList<>();
    private final List<IDisplayModifierRecipe> modifiers;
    private final PlannerData data;
    public ToolStack resultStack;
    public Blueprint blueprint;
    public int selectedPart = 0;
    public int materialPage = 0;
    public MaterialSort<?> sorter;
    public ModifierInfo selectedModifier;
    public int left, top, guiWidth, guiHeight;

    private static final int partsOffsetX = 13, partsOffsetY = 15;
    public static final int materialPageSize = 3*9;

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
        resultStack = null;
        buttons.clear();
        children.clear();

        // List of tool types
        int toolSpace = 20;
        addButton(new BannerWidget(left - 95, top, TranslationUtil.createComponent("banner.tools"), this));
        PaginatedButtonGroup<ToolTypeButton> toolsGroup = new PaginatedButtonGroup<>(left - toolSpace * 5, top + 23, 18, 18, 5, 3, 2,"toolsgroup", this);
        addButton(toolsGroup);
        for (int i = 0; i < tools.size(); i++) {
            SlotInformation info = tools.get(i);
            toolsGroup.addChild(new ToolTypeButton(i, info, this));
        }
        toolsGroup.refresh();

        //List of bookmarked items
        if(data.saved.size() > 0) {
            addButton(new BannerWidget(left - 95, top + 15 + 18*4, TranslationUtil.createComponent("banner.bookmarked"), this));
            PaginatedButtonGroup<BookmarkedButton> bookmarkGroup = new PaginatedButtonGroup<>(left - toolSpace * 5, top + 15 + 18*4 + 23, 18, 18, 5, 5, 2,"bookmarkedgroup", this);
            addButton(bookmarkGroup);
            for (int i = 0; i < data.saved.size(); i++) {
                Blueprint bookmarked = data.saved.get(i);
                bookmarkGroup.addChild(new BookmarkedButton(i, bookmarked, this));
            }
            bookmarkGroup.refresh();
        }
        //Everything in here should only be added if there is a tool selected
        if(blueprint != null){
            //Add the tool part buttons
            List<SlotPosition> positions = blueprint.toolSlotInfo.getPoints();
            for(int i = 0; i < blueprint.parts.length; i++){
                SlotPosition pos = positions.get(i);
                IToolPart part = blueprint.parts[i];
                addButton(new ToolPartButton(i, left + pos.getX() + partsOffsetX, top + pos.getY() + partsOffsetY, part, blueprint.materials[i], this));
            }
            //Everything in here should only be added if there is a tool part selected
            if(selectedPart != -1){
                //Add material list for the tool part
                IToolPart part = blueprint.parts[selectedPart];
                List<IMaterial> usable = MaterialRegistry.getMaterials().stream().filter(part::canUseMaterial).collect(Collectors.toList());
                MaterialStatsId statsId = part.getStatType();
                if(sorter != null)usable.sort((o1, o2) -> sorter.compare(o1, o2, statsId) * -1);
                int loopMin = materialPage*materialPageSize;
                int loopMax = Math.min(usable.size(), (materialPage+1)*materialPageSize);
                for (int i = loopMin; i < loopMax; i++) {
                    int posIndex = i - loopMin;
                    IMaterial mat = usable.get(i);
                    MaterialButton data = new MaterialButton(i, mat, part.withMaterialForDisplay(mat.getIdentifier()), left + (posIndex % 9) * 18 + 8, top + guiHeight - 87 + (posIndex / 9) * 18, this);
                    if(blueprint.materials[selectedPart] == mat)data.selected = true;
                    addButton(data);
                }
                //Add material pagination buttons
                MatPageButton leftPage = new MatPageButton(left + 6, top + guiHeight - 30, -1, this);
                MatPageButton rightPage = new MatPageButton(left + guiWidth - 6 - 37, top + guiHeight - 30, 1, this);
                leftPage.active = materialPage > 0;
                rightPage.active = loopMax < usable.size();
                addButton(leftPage);
                addButton(rightPage);
                //Add sorting buttons
                Class<? extends IMaterialStats> statClass = MaterialRegistry.getClassForStat(part.getStatType());
                if(statClass != null){
                    List<MaterialSort<?>> sorts = MaterialSort.MAP.getOrDefault(statClass, Lists.newArrayList());
                    int startX = left + guiWidth/2 - 6*sorts.size();
                    for (int i = 0; i < sorts.size(); i++) {
                        MaterialSort<?> sort = sorts.get(i);
                        addButton(new IconButton(startX + i*12, leftPage.y + 3, sort.iconU, sort.iconV, TranslationUtil.createComponent("sort", sort.text), this, e -> sort(sort))
                                .withColor(sort == sorter ? Color.WHITE : new Color(0.4f, 0.4f, 0.4f)).withSound(SoundEvents.PAINTING_PLACE));
                    }
                }
            }
            //Add buttons if there is fully finished tool
            ItemStack result = blueprint.createOutput();
            resultStack = result.isEmpty() ? null : ToolStack.from(result);
            if(resultStack != null) {
                addButton(new OutputToolWidget(left + guiWidth - 34, top + 58, result, this));
                boolean bookmarked = data.isBookmarked(blueprint);
                addButton(new IconButton(left + guiWidth - 33, top + 88, 190 + (bookmarked ? 12 : 0), 78,
                        TranslationUtil.createComponent(bookmarked ? "bookmark.remove" : "bookmark.add"), this, e -> {if(bookmarked) unbookmarkCurrent(); else bookmarkCurrent();})
                        .withSound(bookmarked ? SoundEvents.UI_STONECUTTER_TAKE_RESULT : SoundEvents.BOOK_PAGE_TURN));
                //Show available modifier slots
                int slotIndex = 0;
                for (SlotType slotType : SlotType.getAllSlotTypes()) {
                    int slots = resultStack.getFreeSlots(slotType);
                    if(slots > 0) {
                        addButton(new TooltipTextWidget(left + 100 + slotIndex*15, top + 30,
                                new StringTextComponent("" + slots),
                                TranslationUtil.createComponent("slots.available", slotType.getDisplayName()), this)
                                .withColor(slotType.getColor().getValue() + 0xff_00_00_00));
                        slotIndex++;
                    }
                }

                //Show modifier buttons
                addButton(new BannerWidget(left + guiWidth + 7, top, TranslationUtil.createComponent("banner.modifiers"), this));
                int modGroupStartY = top + 23;
                int modGroupStartX = left + guiWidth + 2;
                if(selectedModifier == null) {
                    PaginatedButtonGroup<ModifierSelectButton> modifiersGroup = new PaginatedButtonGroup<>(modGroupStartX, modGroupStartY, 100, 18, 1, 9, 2, "modifiersgroup", this);
                    addButton(modifiersGroup);
                    for (IDisplayModifierRecipe recipe : modifiers) {
                        if (recipe.getDisplayItems().get(0).stream().anyMatch(stack -> ToolStack.from(stack).getDefinition() == blueprint.toolDefinition)) {
                            modifiersGroup.addChild(ModifierSelectButton.create(recipe, resultStack, result, this));
                        }
                    }
                    modifiersGroup.sort(Comparator.comparingInt(value -> value.state.ordinal()));
                    modifiersGroup.refresh();
                } else {
                    ModifierSelectButton modSelectButton = ModifierSelectButton.create(selectedModifier.recipe, resultStack, result, this);
                    modSelectButton.x = modGroupStartX;
                    modSelectButton.y = modGroupStartY;
                    addButton(modSelectButton);

                    Modifier modifier = selectedModifier.modifier;
                    ITinkerStationRecipe tsrecipe = (ITinkerStationRecipe) selectedModifier.recipe;

                    addButton(new ModPreviewWidget(left + guiWidth + 2 + 50 - 9, top + 50, result, this));
                    int arrowOffset = 11;
                    ModLevelButton addButton = new ModLevelButton(left + guiWidth + 2 + 50 + arrowOffset - 2, top + 50, 1, this);
                    ValidatedResult validatedResultAdd = modifier instanceof SingleUseModifier && resultStack.getModifierLevel(modifier) == 1 ?
                            ValidatedResult.failure(KEY_MAX_LEVEL, modifier.getDisplayName(), 1) :tsrecipe.getValidatedResult(new DummyTinkersStationInventory(result));
                    if(!validatedResultAdd.isSuccess()){
                        addButton.disable(validatedResultAdd.getMessage().copy().setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
                        addButton(new ModPreviewWidget(addButton.x + addButton.getWidth() + 2, top + 50, ItemStack.EMPTY, this));
                    }else if(blueprint.modStack.getIncrementalDiff(modifier) > 0){
                        addButton.disable(TranslationUtil.createComponent("modifiers.error.incrementnotmax").setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
                        addButton(new ModPreviewWidget(addButton.x + addButton.getWidth() + 2, top + 50, ItemStack.EMPTY, this));
                    } else {
                        Blueprint copy = blueprint.clone();
                        copy.modStack.push(selectedModifier);
                        addButton(new ModPreviewWidget(addButton.x + addButton.getWidth() + 2, top + 50, copy.createOutput(), this));
                    }
                    addButton(addButton);

                    ModLevelButton subtractButton = new ModLevelButton(left + guiWidth + 2 + 50 - arrowOffset - 18, top + 50, -1, this);
                    ValidatedResult validatedResultSubtract = ToolValidator.validateModRemoval(blueprint, resultStack, selectedModifier);
                    if(validatedResultSubtract.hasError()){
                        subtractButton.disable(((IFormattableTextComponent)validatedResultSubtract.getMessage()).setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
                    }
                    addButton(new ModPreviewWidget(subtractButton.x - 2 - 18, top + 50, subtractButton.isDisabled() ? ItemStack.EMPTY : validatedResultSubtract.getResult(), this));
                    addButton(subtractButton);
                    int perLevel = ModifierRecipeLookup.getNeededPerLevel(modifier);
                    if(perLevel > 0 && blueprint.modStack.getLevel(selectedModifier) > 0){
                        addButton(new SliderWidget(left + guiWidth + 2 + 10, top + 70, 80, 20, val -> {blueprint.modStack.setIncrementalDiff(modifier, perLevel-val); refresh();},
                                1, perLevel, perLevel - blueprint.modStack.getIncrementalDiff(modifier), this));
                    }

                    addButton(new ModExitButton(left + guiWidth + 2 + 50 - 30, top + 150, 60, 20, this));
                }
            }
            //Add randomize tool button
            addButton(new IconButton(left + guiWidth - 70, top + 88, 176, 104,
                    TranslationUtil.createComponent("randomize"), this, e -> randomize())
                    .withSound(SoundEvents.ENDERMAN_TELEPORT));
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick) {

        renderBackground(stack);
        bindTexture();
        this.blit(stack, left, top, 0, 0, guiWidth, guiHeight);
        if(blueprint != null) {
            RenderSystem.pushMatrix();
            RenderSystem.translated(left + partsOffsetX + 7, top + partsOffsetY + 22, -100);
            RenderSystem.scalef(3.7F, 3.7F, 1.0F);
            minecraft.getItemRenderer().renderGuiItem(blueprint.toolStack, 0, 0);
            RenderSystem.popMatrix();
            bindTexture();
            RenderSystem.pushMatrix();
            RenderSystem.translated(0, 0, 1);
            int boxX = 13, boxY = 24, boxL = 81;
            if(mouseX > boxX + left && mouseY > boxY + top && mouseX < boxX + left + boxL && mouseY < boxY + top + boxL)
            RenderSystem.color4f(1f, 1f, 1f, 0.75f);
            else RenderSystem.color4f(1f, 1f, 1f, 0.5f);
            RenderSystem.enableBlend();
            this.blit(stack, left + boxX, top + boxY, boxX, boxY, boxL, boxL);
            RenderSystem.popMatrix();
        }
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
