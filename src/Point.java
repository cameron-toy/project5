import java.util.List;
import java.util.Optional;

public final class Point
{
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public boolean equals(Object other) {
        return other instanceof Point && ((Point)other).x == this.x
                && ((Point)other).y == this.y;
    }

    public int hashCode() {
        int result = 17;
        result = result * 31 + x;
        result = result * 31 + y;
        return result;
    }

    public boolean adjacent(Point other) {
        return (this.x == other.x && Math.abs(this.y - other.y) == 1) || (this.y == other.y
                && Math.abs(this.x - other.x) == 1);
    }

    public Optional<Entity> nearestEntity(
            List<Entity> entities)
    {
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        else {
            Entity nearest = entities.get(0);
            int nearestDistance = this.distanceSquared(nearest.getPosition());

            for (Entity other : entities) {
                int otherDistance = this.distanceSquared(other.getPosition());

                if (otherDistance < nearestDistance) {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }

    private int distanceSquared(Point other) {
        int deltaX = other.x - this.x;
        int deltaY = other.y - this.y;

        return deltaX * deltaX + deltaY * deltaY;
    }

    public boolean withinBounds(WorldModel world) {
        return this.y >= 0 && this.y < world.getNumRows() && this.x >= 0
                && this.x < world.getNumCols();
    }

    public int getX() { return this.x; }

    public int getY() { return this.y; }
}
