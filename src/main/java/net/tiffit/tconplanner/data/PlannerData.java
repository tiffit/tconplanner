package net.tiffit.tconplanner.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlannerData {

    public final List<Blueprint> saved = new ArrayList<>();
    public Blueprint starred;

    private final File bookmarkFile;
    private boolean hasLoaded;

    public PlannerData(File folder){
        bookmarkFile = new File(folder, "bookmark.dat");
        try {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdir();
            if (!bookmarkFile.exists()) {
                assert bookmarkFile.createNewFile();
                save();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void refresh() throws IOException {
        save();
        load();
    }

    public boolean isBookmarked(Blueprint bp){
        return saved.stream().anyMatch(blueprint -> blueprint.equals(bp));
    }

    public void save() throws IOException {
        ListNBT nbt = new ListNBT();
        List<CompoundNBT> others = new ArrayList<>();
        for (Blueprint bp : saved) {
            CompoundNBT cnbt = bp.toNBT();
            if(bp.isComplete() && !others.contains(cnbt)){
                nbt.add(cnbt);
                others.add(cnbt);
            }
        }
        CompoundNBT data = new CompoundNBT();
        data.put("list", nbt);
        if(starred != null){
            if(starred.isComplete()){
                CompoundNBT cnbt = starred.toNBT();
                data.put("starred", cnbt);
            }
        }
        CompressedStreamTools.write(data, bookmarkFile);
    }

    public void firstLoad() throws IOException {
        if(!hasLoaded) {
            load();
        }
    }

    public void load() throws IOException {
        hasLoaded = true;
        saved.clear();
        CompoundNBT data = CompressedStreamTools.read(bookmarkFile);
        assert data != null;
        ListNBT nbt = data.getList("list", data.getId());
        for(int i = 0; i < nbt.size(); i++){
            CompoundNBT tag = nbt.getCompound(i);
            saved.add(Blueprint.fromNBT(tag));
        }
        saved.removeIf(Objects::isNull);
        if(data.contains("starred")){
            starred = Blueprint.fromNBT(data.getCompound("starred"));
        }else{
            starred = null;
        }
    }

}
