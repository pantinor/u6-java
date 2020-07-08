package ultima6;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Player {

    private int id;
    private String name;
    private final List<InventoryItem> inventory = new ArrayList<>();

    private int strength;
    private int dex;
    private int intelligence;
    private int hp;
    private int level;
    private int exp;
    private int magic;
    private int karma;

    private int combat_mode;
    private int alignment;
    private int body_armor_class;
    private int readied_armor_class;

    private int gender;
    private int questf;
    private int gargishf; // learned Gargish
    private int alcohol; // number of alcoholic drinks consumed

    private final BitSet obj_flags = new BitSet();
    private final BitSet status_flags = new BitSet();
    private final BitSet talk_flags = new BitSet();

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getTalkFlag(int idx) {
        return this.talk_flags.get(idx);
    }

    public void setTalkFlag(int idx) {
        this.talk_flags.set(idx);
    }

    public void clearTalkFlag(int idx) {
        this.talk_flags.clear(idx);
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getDex() {
        return dex;
    }

    public void setDex(int dex) {
        this.dex = dex;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public int getCombat_mode() {
        return combat_mode;
    }

    public void setCombat_mode(int combat_mode) {
        this.combat_mode = combat_mode;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public int getBody_armor_class() {
        return body_armor_class;
    }

    public void setBody_armor_class(int body_armor_class) {
        this.body_armor_class = body_armor_class;
    }

    public int getReadied_armor_class() {
        return readied_armor_class;
    }

    public void setReadied_armor_class(int readied_armor_class) {
        this.readied_armor_class = readied_armor_class;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getQuestf() {
        return questf;
    }

    public void setQuestf(int questf) {
        this.questf = questf;
    }

    public int getGargishf() {
        return gargishf;
    }

    public void setGargishf(int gargishf) {
        this.gargishf = gargishf;
    }

    public int getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(int alcohol) {
        this.alcohol = alcohol;
    }

    public boolean hasItem(Objects.Object id) {
        for (InventoryItem i : this.inventory) {
            if (i.id == id) {
                return true;
            }
        }
        return false;
    }

    public boolean hasItem(Objects.Object id, int quality) {
        for (InventoryItem i : this.inventory) {
            if (i.id == id && i.quality == quality) {
                return true;
            }
        }
        return false;
    }

    public int quantity(Objects.Object id) {
        for (InventoryItem i : this.inventory) {
            if (i.id == id) {
                return i.quantity;
            }
        }
        return 0;
    }

    public InventoryItem addItem(Objects.Object id, int quantity, int quality) {
        InventoryItem i = new InventoryItem(id, quantity, 0);
        if (this.inventory.contains(i)) {
            for (InventoryItem ii : this.inventory) {
                i = ii;
                if (ii.id == id) {
                    ii.quantity = ii.quantity + quantity;
                    break;
                }
            }
        } else {
            this.inventory.add(i);
        }
        return i;
    }

    public InventoryItem removeItem(Objects.Object id, int quantity, int quality) {
        InventoryItem ret = null;
        for (InventoryItem ii : this.inventory) {
            if (ii.id == id) {
                ii.quantity = ii.quantity - quantity;
                break;
            }
        }
        if (ret != null && ret.quantity <= 0) {
            this.inventory.remove(ret);
        }
        return ret;
    }
    
    public int getInventoryWeight() {
        int amt = 0;
        for (InventoryItem ii : this.inventory) {
            amt += (Ultima6.OBJ_WEIGHTS[ii.id.getId()] & 0xff * ii.quantity) / 10;
        }
        return amt;
    }
    
    public int getMaxInventoryWeight() {
        return this.strength * 2;
    }
    
    @Override
    public String toString() {
        return "Player{" + "name=" + name + ", talk flags=" + this.talk_flags + ", inventory=" + inventory + ", hp=" + hp + ", level=" + level + ", exp=" + exp + ", karma=" + karma + '}';
    }

    public static class InventoryItem {

        Objects.Object id;
        int quantity;
        int quality;

        public InventoryItem(Objects.Object id, int quantity, int quality) {
            this.id = id;
            this.quantity = quantity;
            this.quality = quality;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getQuality() {
            return quality;
        }

        public void setQuality(int quality) {
            this.quality = quality;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + this.id.getId();
            hash = 53 * hash + this.quality;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InventoryItem other = (InventoryItem) obj;
            if (this.id != other.id) {
                return false;
            }
            if (this.quality != other.quality) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "InventoryItem{" + "id=" + id + ", quantity=" + quantity + ", quality=" + quality + '}';
        }

    }
}
