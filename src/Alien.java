import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Alien extends AnimationEntity {

    public Alien(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod) {
        super(id, position, images, actionPeriod, animationPeriod);
    }

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler) {
        Optional<Entity> alienTarget =
                world.findNearest(this.getPosition(), Miner.class);
        long nextPeriod = this.getActionPeriod();

        if (alienTarget.isPresent()) {
            if (!(this.moveToMiner(world, alienTarget.get(), scheduler))) {
                nextPeriod += this.getActionPeriod();
                scheduler.scheduleEvent(this,
                        Factory.createActivityAction(this, world, imageStore),
                        nextPeriod);
            }
        }
    }

    public boolean moveToMiner(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (this.position.adjacent(target.getPosition())) {
            world.removeEntity(target);
            world.removeEntity(this);
            scheduler.unscheduleAllEvents(target);
            scheduler.unscheduleAllEvents(this);
            return true;
        }
        else {
            PathingStrategy strategy = new AStarPathingStrategy();
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
            return false;
        }
    }

}
