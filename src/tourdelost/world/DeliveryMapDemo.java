package tourdelost.world;

import java.util.List;
import java.util.OptionalInt;

/** Shared medicine delivery map and console checks for graph movement. */
public final class DeliveryMapDemo {
    private DeliveryMapDemo() {
    }

    /** Creates the read-only map with start S and hospital G. */
    public static Graph createGraph() {
        return new Graph(
            List.of(
                new Node("S", 0, 1, List.of()),
                new Node("B", 2, 0, List.of()),
                new Node("C", 1, 2, List.of()),
                new Node("D", 3, 2, List.of()),
                new Node("G", 4, 1, List.of())
            ),
            List.of(
                road("road-sb", "S", "B", 4),
                road("road-bg", "B", "G", 4),
                road("road-sc", "S", "C", 1),
                road("road-cd", "C", "D", 1),
                road("road-dg", "D", "G", 1)
            )
        );
    }

    public static void main(String[] args) {
        Graph graph = createGraph();
        Player player = new Player(graph, "S");

        check(!graph.canMove("S", "G"), "The graph has no direct road from S to G.");
        checkRejectedMove(player, "S", "G", "road-sb");

        move(player, "S", "B", "road-sb");
        check(!graph.canMove("B", "S"), "The graph has no road from B back to S.");
        checkRejectedMove(player, "B", "S", "road-sb");
        move(player, "B", "G", "road-bg");
        checkRoute(player, "S -> B -> G", 8);

        resetPlayer(player);

        move(player, "S", "C", "road-sc");
        move(player, "C", "D", "road-cd");
        move(player, "D", "G", "road-dg");
        checkRoute(player, "S -> C -> D -> G", 3);

        resetPlayer(player);
    }

    private static Edge road(String id, String from, String to, int cost) {
        return new Edge(id, from, to, Edge.Direction.DIRECTED, Edge.Type.ROAD, cost,
            OptionalInt.empty());
    }

    private static void move(Player player, String from, String to, String edgeId) {
        check(player.confirmMove(from, to, edgeId).isPresent()
                && player.currentNodeId().equals(to),
            "Moved from " + from + " to " + to + " on " + edgeId + ".");
    }

    private static void checkRejectedMove(Player player, String from, String to, String edgeId) {
        String position = player.currentNodeId();
        List<MoveRecord> history = player.history();
        check(position.equals(from) && player.confirmMove(from, to, edgeId).isEmpty()
                && player.currentNodeId().equals(position) && player.history().equals(history),
            "Rejected " + from + " -> " + to + " with position and history unchanged.");
    }

    private static void checkRoute(Player player, String route, int expectedCost) {
        int totalCost = player.history().stream().mapToInt(MoveRecord::cost).sum();
        check(player.currentNodeId().equals("G") && totalCost == expectedCost,
            route + " reached hospital G with total cost " + totalCost
                + " (expected " + expectedCost + ").");
    }

    private static void resetPlayer(Player player) {
        player.reset();
        check(player.currentNodeId().equals("S") && player.history().isEmpty(),
            "Reset returned to S and cleared move history.");
    }

    private static void check(boolean passed, String description) {
        if (!passed) {
            throw new AssertionError(description);
        }
        System.out.println("PASS " + description);
    }
}
