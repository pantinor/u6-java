package ultima6;



public enum Sound {
    MOONGATE("moongate_flash.ogg", false, 0.3f),
    BLOCKED("blocked.ogg", false, 0.3f),
    BOOTUP("BOOTUP.ogg", false, 0.3f),
    MAIN_MENU("ULTIMA.ogg", false, 0.3f),
    CHAR_GEN("CREATE.ogg", false, 0.3f),
    INTRO("INTRO.ogg", false, 0.3f),
    COMBAT("ENGAGE.ogg", false, 0.3f),
    RULE_BRIT("BRIT.ogg", false, 0.3f),
    FOREST("FOREST.ogg", false, 0.3f),
    WANDER("DUNGEON.ogg", false, 0.3f),
    STONES("STONES.ogg", false, 0.3f),
    CAPN_PIPE("HORNPIPE.ogg", false, 0.3f),
    GARGL("GARGOYLE.ogg", false, 0.3f),
    ENDING("END.ogg", false, 0.3f);
   
    String file;
    boolean looping;
    float volume;

    private Sound(String name, boolean looping, float volume) {
        this.file = name;
        this.looping = looping;
        this.volume = volume;
    }

    public String getFile() {
        return this.file;
    }

    public boolean getLooping() {
        return this.looping;
    }

    public float getVolume() {
        return this.volume;
    }

}
