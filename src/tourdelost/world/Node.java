package tourdelost.world;

import java.util.List;

/** A point on the map and the level activity IDs available there. */
public record Node(String id, int x, int y, List<String> activityIds) {
    public Node {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Node ID must not be blank.");
        }
        activityIds = List.copyOf(activityIds);
    }
}
