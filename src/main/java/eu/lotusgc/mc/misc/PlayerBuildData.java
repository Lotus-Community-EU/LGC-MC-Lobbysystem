package eu.lotusgc.mc.misc;

public class PlayerBuildData {

    private int brokenBlocks, placedBlocks;

    public PlayerBuildData() {
        this.brokenBlocks = 0;
        this.placedBlocks = 0;
    }

    public int getBrokenBlocks() {
        return brokenBlocks;
    }

    public int getPlacedBlocks() {
        return placedBlocks;
    }

    public void incrementBrokenBlocks() {
        this.brokenBlocks++;
    }

    public void incrementPlacedBlocks() {
        this.placedBlocks++;
    }

}