package net.tiffit.tconplanner.api;

import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotPosition;

public class TCSlotPos {
    public static final int partsOffsetX = 13, partsOffsetY = 15;

    private final SlotPosition pos;

    TCSlotPos(SlotPosition pos){
        this.pos = pos;
    }

    public int getX(){
        return pos.getX() + partsOffsetX;
    }

    public int getY(){
        return pos.getY() + partsOffsetY;
    }

}
