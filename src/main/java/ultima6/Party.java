package ultima6;

import java.util.ArrayList;
import java.util.List;

public class Party {

    final private List<Player> players = new ArrayList<>();

    public void add(Player player) {
        this.players.add(player);
    }

    public void remove(Player player) {
        this.players.remove(player);
    }

    public boolean isInParty(int npc) {
        for (Player player : players) {
            if (player.getId() == npc) {
                return true;
            }
        }
        return false;
    }

    public boolean isInParty(Player player) {
        if (players.contains(player)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isObjectInParty(int objId) {
        return false;
    }

}
