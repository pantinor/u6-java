package ultima6;



public enum Sound {
    MOONGATE("moongate_flash.ogg", false, 0.3f),
    BLOCKED("blocked.ogg", false, 0.3f),
    BOOTUP("01 Can't Remove the Pain (Bootup).wav", false, 0.3f),
    MAIN_MENU("02 Ultima Theme (Main Menu).wav", false, 0.3f),
    CHAR_GEN("03 I Hear You Crying (Character Generation).wav", false, 0.3f),
    INTRO("04 Fall Leaves (Introduction).wav", false, 0.3f),
    COMBAT("05 Engagement and Melee (Combat).wav", false, 0.3f),
    RULE_BRIT("06 Rule, Britannia!.wav", false, 0.3f),
    FOREST("07 Black Forest.wav", false, 0.3f),
    WANDER("08 The Wander (Dungeon).wav", false, 0.3f),
    STONES("09 Stones (by Iolo & Gwenno).wav", false, 0.3f),
    CAPN_PIPE("10 Cap'n Johne's Hornpipe.wav", false, 0.3f),
    GARGL("11 Audchar Gargl Zenmur.wav", false, 0.3f),
    ENDING("12 Ending.wav", false, 0.3f);
   
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
