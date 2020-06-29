package ultima6;

import java.util.ArrayList;
import java.util.List;

public class Party {

    public static final int NPC_AVATAR = 1;
    public static final int NPC_DUPRE = 2;
    public static final int NPC_SHAMINO = 3;
    public static final int NPC_IOLO = 4;
    public static final int NPC_LORD_BRITISH = 5;
    public static final int NPC_NYSTUL = 6;
    public static final int NPC_GEOFFREY = 7;
    public static final int NPC_THOLDEN = 8;
    public static final int NPC_SHERRY = 9;
    public static final int NPC_SELF = 235;

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

    public Player get(int idx) {
        //return players.get(idx);
        return idx > 0 && players.size() > 0 && idx - 1 < players.size() ? players.get(idx - 1) : null;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isObjectInParty(Objects.Object obj, int quality) {
        for (Player player : players) {
            if (player.hasItem(obj, quality)) {
                return true;
            }
        }
        return false;
    }

}
