package tourdelost.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Position and completed moves for one player on a graph. */
public final class Player {
    private final Graph graph;
    private final String startNodeId;
    private final List<MoveRecord> history = new ArrayList<>();

    public Player(Graph graph, String startNodeId) {
        this.graph = Objects.requireNonNull(graph, "graph");
        if (graph.findNode(startNodeId).isEmpty()) {
            throw new IllegalArgumentException("Unknown start node: " + startNodeId);
        }
        this.startNodeId = startNodeId;
    }

    public String startNodeId() {
        return startNodeId;
    }

    public synchronized String currentNodeId() {
        return history.isEmpty() ? startNodeId : history.getLast().toNodeId();
    }

    public synchronized List<MoveRecord> history() {
        return List.copyOf(history);
    }

    /**
     * Records an approved move if the player is still at the given origin and the
     * selected edge is a valid road to the destination. The game session owns
     * resource checks and the lifetime of pending moves.
     */
    public synchronized Optional<MoveRecord> confirmMove(String fromNodeId, String toNodeId, String edgeId) {
        if (!currentNodeId().equals(fromNodeId)) {
            return Optional.empty();
        }
        for (Edge road : graph.roadEdgesBetween(fromNodeId, toNodeId)) {
            if (road.id().equals(edgeId)) {
                MoveRecord move = new MoveRecord(fromNodeId, toNodeId, road.id(), road.cost());
                history.add(move);
                return Optional.of(move);
            }
        }
        return Optional.empty();
    }

    public synchronized void reset() {
        history.clear();
    }
}
