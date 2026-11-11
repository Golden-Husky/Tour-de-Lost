package tourdelost.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Shared, read-only map data for the game, UI, and algorithms. */
public final class Graph {
    private final Map<String, Node> nodesById;
    private final Map<String, Edge> edgesById;
    private final Map<String, List<Edge>> outgoingByNodeId;

    public Graph(Collection<Node> nodes, Collection<Edge> edges) {
        Map<String, Node> nodeIndex = new LinkedHashMap<>();
        Map<String, List<Edge>> outgoing = new HashMap<>();
        for (Node node : nodes) {
            if (nodeIndex.putIfAbsent(node.id(), node) != null) {
                throw new IllegalArgumentException("Duplicate node ID: " + node.id());
            }
            outgoing.put(node.id(), new ArrayList<>());
        }

        Map<String, Edge> edgeIndex = new LinkedHashMap<>();
        for (Edge edge : edges) {
            List<Edge> from = outgoing.get(edge.fromNodeId());
            List<Edge> to = outgoing.get(edge.toNodeId());
            if (from == null || to == null) {
                throw new IllegalArgumentException("Unknown endpoint for edge: " + edge.id());
            }
            if (edgeIndex.putIfAbsent(edge.id(), edge) != null) {
                throw new IllegalArgumentException("Duplicate edge ID: " + edge.id());
            }
            from.add(edge);
            if (edge.direction() == Edge.Direction.BIDIRECTIONAL
                && !edge.fromNodeId().equals(edge.toNodeId())) {
                to.add(edge);
            }
        }

        outgoing.replaceAll((id, outgoingEdges) -> List.copyOf(outgoingEdges));
        outgoingByNodeId = Collections.unmodifiableMap(outgoing);
        nodesById = Collections.unmodifiableMap(nodeIndex);
        edgesById = Collections.unmodifiableMap(edgeIndex);
    }

    public Optional<Node> findNode(String nodeId) {
        return Optional.ofNullable(nodesById.get(nodeId));
    }

    public Optional<Edge> findEdge(String edgeId) {
        return Optional.ofNullable(edgesById.get(edgeId));
    }

    public Collection<Node> nodes() {
        return nodesById.values();
    }

    public Collection<Edge> edges() {
        return edgesById.values();
    }

    /** Returns connections that can be traversed from the node, of every edge type. */
    public List<Edge> outgoingEdges(String nodeId) {
        return outgoingByNodeId.getOrDefault(nodeId, List.of());
    }

    /** Returns the direct roads the player may use in the requested direction. */
    public List<Edge> roadEdgesBetween(String fromNodeId, String toNodeId) {
        if (fromNodeId == null || fromNodeId.equals(toNodeId)) {
            return List.of();
        }
        return outgoingEdges(fromNodeId).stream()
            .filter(edge -> edge.type() == Edge.Type.ROAD)
            .filter(edge -> edge.allowsTraversal(fromNodeId, toNodeId))
            .toList();
    }

    public boolean canMove(String fromNodeId, String toNodeId) {
        return !roadEdgesBetween(fromNodeId, toNodeId).isEmpty();
    }
}
