package tourdelost.world;

/** One completed move, recorded in the direction the player actually traveled. */
public record MoveRecord(String fromNodeId, String toNodeId, String edgeId, int cost) {}
