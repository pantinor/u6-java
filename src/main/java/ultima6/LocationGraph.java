package ultima6;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class LocationGraph implements IndexedGraph<LocationGraph.Location> {

    protected Array<Location> nodes;

    public LocationGraph(Array<Location> nodes) {
        this.nodes = nodes;
    }

    @Override
    public int getIndex(Location node) {
        return node.getIndex();
    }

    @Override
    public Array<Connection<Location>> getConnections(Location fromNode) {
        return fromNode.getConnections();
    }

    @Override
    public int getNodeCount() {
        return nodes.size;
    }

    public static class Location {

        private final TileFlags flags;
        private final int index;
        private final int x;
        private final int y;
        private final Array<Connection<Location>> connections = new Array<>(4);

        public Location(int index, int x, int y, TileFlags flags) {
            this.flags = flags;
            this.index = index;
            this.x = x;
            this.y = y;
        }

        public TileFlags getFlags() {
            return flags;
        }

        public int getIndex() {
            return index;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Array<Connection<Location>> getConnections() {
            return connections;
        }

    }

    public static class TileConnection implements Connection<Location> {

        protected Location fromNode;
        protected Location toNode;

        public TileConnection(Location fromNode, Location toNode) {
            this.fromNode = fromNode;
            this.toNode = toNode;
        }

        @Override
        public float getCost() {

            TileFlags tf = toNode.getFlags();
            if (tf.isWall() || tf.isImpassable() || tf.isWet()) {
                return 10f;
            }

            return 1f;
        }

        @Override
        public Location getFromNode() {
            return fromNode;
        }

        @Override
        public Location getToNode() {
            return toNode;
        }

    }

    public static class ManhattanDistance implements Heuristic<Location> {

        @Override
        public float estimate(Location node, Location endNode) {
            return Vector2.dst(node.getX(), node.getY(), endNode.getX(), endNode.getY());
        }
    }
}
