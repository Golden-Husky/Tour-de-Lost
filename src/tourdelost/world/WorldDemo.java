package tourdelost.world;

import java.util.List;
import java.util.OptionalInt;

/** Shared example graph and console checks for the world API. */
public final class WorldDemo {
    private WorldDemo() {
    }

    public static Graph createGraph() {
        return new Graph(
            List.of(
                new Node("A", 0, 0, List.of()),
                new Node("B", 1, 0, List.of("refuel")),
                new Node("C", 2, 0, List.of()),
                new Node("D", 2, 1, List.of("water-station"))
            ),
            List.of(
                new Edge("road-ab", "A", "B", Edge.Direction.DIRECTED, Edge.Type.ROAD, 3,
                    OptionalInt.empty()),
                new Edge("road-bc", "B", "C", Edge.Direction.BIDIRECTIONAL, Edge.Type.ROAD, 2,
                    OptionalInt.empty()),
                new Edge("power-bd", "B", "D", Edge.Direction.BIDIRECTIONAL, Edge.Type.POWER_LINE, 1,
                    OptionalInt.empty()),
                new Edge("pipe-cd", "C", "D", Edge.Direction.DIRECTED, Edge.Type.PIPE, 1,
                    OptionalInt.of(8))
            )
        );
    }

    public static void main(String[] args) {
        Graph graph = createGraph();
        check(graph.canMove("A", "B"), "A player can follow a direct road.");
        check(!graph.canMove("B", "A"), "A player cannot go against a one-way road.");
        check(!graph.canMove("A", "C"), "A player cannot skip an intermediate node.");
        check(graph.canMove("B", "C") && graph.canMove("C", "B"),
            "A two-way road works in both directions.");
        check(!graph.canMove("B", "D") && !graph.canMove("C", "D"),
            "A power line or pipe is not a walking route.");
        check(graph.findNode("B").orElseThrow().activityIds().equals(List.of("refuel"))
                && graph.findEdge("pipe-cd").orElseThrow().capacity().orElseThrow() == 8
                && graph.outgoingEdges("C").stream().map(Edge::id).toList()
                    .equals(List.of("road-bc", "pipe-cd")),
            "The shared graph exposes activities, pipe capacity, and outgoing edges.");

        Player player = new Player(graph, "A");
        Edge road = graph.roadEdgesBetween("A", "B").getFirst();
        MoveRecord completed = player.confirmMove("A", "B", road.id()).orElseThrow();
        check(player.currentNodeId().equals("B")
                && player.history().equals(List.of(new MoveRecord("A", "B", "road-ab", 3))),
            "A confirmed move updates position and records the road and its cost once.");
        System.out.println(completed);

        check(player.confirmMove("B", "A", "road-ab").isEmpty()
                && player.confirmMove("B", "D", "power-bd").isEmpty()
                && player.confirmMove("A", "B", "road-ab").isEmpty()
                && player.currentNodeId().equals("B") && player.history().equals(List.of(completed)),
            "Rejected and repeated confirmations leave position and history alone.");

        player.reset();
        check(player.currentNodeId().equals("A") && player.history().isEmpty(),
            "Reset returns the player to the start with no move history.");
    }

    private static void check(boolean passed, String description) {
        if (!passed) {
            throw new AssertionError(description);
        }
        System.out.println("PASS " + description);
    }
}
