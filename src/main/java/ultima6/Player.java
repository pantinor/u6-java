package ultima6;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Player {

    private int id;
    private String name;
    private final BitSet flags = new BitSet();
    private Party party;
    private final List<InventoryItem> inventory = new ArrayList<>();

    private int strength = 18;
    private int dex = 18;
    private int intelligence = 18;
    private int hp = 32;
    private int level = 1;
    private int exp = 0;
    private int magic = 18;
    private int combat_mode;
    private int alignment;
    private int body_armor_class;
    private int readied_armor_class;

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getFlag(int idx) {
        return this.flags.get(idx);
    }

    public void setFlag(int idx) {
        this.flags.set(idx);
    }

    public Party getParty() {
        return this.party;
    }

    public void setParty(Party party) {
        this.party = party;
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

    public boolean hasItem(int id) {
        for (InventoryItem i : this.inventory) {
            if (i.id == id) {
                return true;
            }
        }
        return false;
    }
    
    public int quantity(int id) {
        for (InventoryItem i : this.inventory) {
            if (i.id == id) {
                return i.quantity;
            }
        }
        return 0;
    }

    public InventoryItem addItem(int id, int quantity, int quality) {
        InventoryItem i = new InventoryItem(id, quantity, quality);
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

    public InventoryItem removeItem(int id, int quantity) {
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

    public int canCarryWeight() {
        int amt = 0;
        for (InventoryItem ii : this.inventory) {
            amt += Ultima6.OBJ_WEIGHTS[ii.id] & 0xff * ii.quantity;
        }
        int max = this.strength * 2;
        return max - amt;
    }

    public static class InventoryItem {

        int id;
        int quantity;
        int quality;

        public InventoryItem(int id, int quantity, int quality) {
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
            return true;
        }

    }
}
