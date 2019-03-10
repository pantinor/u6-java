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

    public Player getPlayer(int npc) {
        for (Player player : players) {
            if (player.getId() == npc) {
                return player;
            }
        }
        return null;
    }
    
    public Player getNPC(int idx) {
         return players.get(idx) != null ? players.get(idx) : null;
    }

    public boolean isObjectInParty(int objId) {
        return false;
    }

}
