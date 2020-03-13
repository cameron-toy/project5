import processing.core.PImage;
import java.util.List;
import java.util.Optional;


public abstract class Miner extends AnimationEntity{

    protected int resourceLimit;

    public Miner(
            String id,
            Point position,
            List<PImage> images,
            int resourceLimit,
            int actionPeriod,
            int animationPeriod) {
        super(id, position, images, actionPeriod, animationPeriod);
        this.resourceLimit = resourceLimit;
    }


    public boolean moveTo(
            WorldModel world,
            Entity target,
            EventScheduler scheduler) {

        PathingStrategy strategy = new AStarPathingStrategy();

        if (!this.position.adjacent(target.getPosition())) {
            List<Point> path = strategy.computePath(
                    this.position,
                    target.getPosition(),
                    point -> !(world.isOccupied(point)) && point.withinBounds(world),
                    Point::adjacent,
                    PathingStrategy.CARDINAL_NEIGHBORS
            );
            if (path.isEmpty())
                return false;
            Point nextPos = path.get(0);
            if (!this.position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                occupant.ifPresent(scheduler::unscheduleAllEvents);
                this.moveEntity(world, nextPos);
            }
        }
        return false;
    }

    public abstract boolean transform(
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore);

}