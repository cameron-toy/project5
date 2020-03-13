import java.util.List;
import java.util.function.Predicate;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;


class AStarPathingStrategy
    implements PathingStrategy {
    public List<Point> computePath(Point start, Point end,
                                   Predicate<Point> canPassThrough,
                                   BiPredicate<Point, Point> withinReach,
                                   Function<Point, Stream<Point>> potentialNeighbors) {

        Comparator<AStarPoint> comp = (p1, p2) -> p1.getF() - p2.getF();

        Map<Point, AStarPoint> closed_map = new HashMap<>();
        Map<Point, AStarPoint> open_map = new HashMap<>();
        PriorityQueue<AStarPoint> open_q = new PriorityQueue<>(comp);

        AStarPoint startPoint = new AStarPoint(start, null, end);
        open_q.offer(startPoint);

        while (!open_q.isEmpty()) {
            AStarPoint current = open_q.remove();
            if (withinReach.test(current.getPoint(), end))
                return computePath(current);
            List<Point> points = potentialNeighbors.apply(current.getPoint())
                                        .filter(canPassThrough)
                                        .filter(p -> !p.equals(start) && !p.equals(end))
                                        .collect(Collectors.toList());
//                                        .map((Point p) -> new AStarPoint(p, current, end));

            for (Point p : points) {
                if (!closed_map.containsKey(p)) {
                    AStarPoint ap = new AStarPoint(p, current, end);
                    if (open_map.containsKey(p)) {
                        if (ap.getG() > open_map.get(p).getG()) {
                            open_q.remove(open_map.get(p));
                            open_q.add(ap);
                            open_map.replace(p, ap);
                        }
                    } else {
                        open_q.add(ap);
                        open_map.put(p, ap);
                    }
                }
            }

            closed_map.put(current.getPoint(), current);
        }
        return new ArrayList<>();
    }

    public List<Point> computePath(AStarPoint current) {
        List<Point> path = new ArrayList<>();
        while (current.getParent() != null) {
            path.add(0, current.getPoint());
            current = current.getParent();
        }
        return path;
    }
}