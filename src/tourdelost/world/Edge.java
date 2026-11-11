package tourdelost.world;

import java.util.Objects;
import java.util.OptionalInt;

/** A connection with level-defined cost and optional capacity. */
public record Edge(
    String id,
    String fromNodeId,
    String toNodeId,
    Direction direction,
    Type type,
    int cost,
    OptionalInt capacity
) {
    public enum Direction { DIRECTED, BIDIRECTIONAL }
    public enum Type { ROAD, POWER_LINE, PIPE }

    public Edge {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Edge ID must not be blank.");
        }
        Objects.requireNonNull(fromNodeId, "fromNodeId");
        Objects.requireNonNull(toNodeId, "toNodeId");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(capacity, "capacity");
        if (cost < 0 || capacity.orElse(0) < 0) {
            throw new IllegalArgumentException("Edge cost and capacity must be nonnegative.");
        }
    }

    public boolean allowsTraversal(String fromId, String toId) {
        return (fromNodeId.equals(fromId) && toNodeId.equals(toId))
            || (direction == Direction.BIDIRECTIONAL
                && toNodeId.equals(fromId) && fromNodeId.equals(toId));
    }
}
