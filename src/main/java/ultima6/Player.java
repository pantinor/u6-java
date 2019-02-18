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

    public boolean hasItem(int id, int quantity, int quality) {
        for (InventoryItem i : this.inventory) {
            if (i.id == id) {
                if (i.quality == quality) {
                    return true;
                }
            }
        }
        return false;
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

    public static class InventoryItem {

        int id;
        int quantity;
        int quality;

        public InventoryItem(int id, int quantity, int quality) {
            this.id = id;
            this.quantity = quantity;
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
