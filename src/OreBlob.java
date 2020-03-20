
import processing.core.PImage;
import java.util.List;
import java.util.Optional;

public class OreBlob extends Ignitable {

    private static final String QUAKE_KEY = "quake";

    public OreBlob(
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
        if (onFire) {
            if (lifeSpan-- <= 0) {
                scheduler.unscheduleAllEvents(this);
                world.removeEntity(this);
            }
        }
        Optional<Entity> blobTarget =
                world.findNearest(this.getPosition(), Vein.class);
        long nextPeriod = this.getActionPeriod();

        if (blobTarget.isPresent()) {
            Point tgtPos = blobTarget.get().getPosition();

            if (this.moveToOreBlob(world, blobTarget.get(), scheduler)) {
                Quake quake = Factory.createQuake(tgtPos,
                        imageStore.getImageList(QUAKE_KEY));

                world.addEntity(quake);
                nextPeriod += this.getActionPeriod();
                ActivityAction a = Factory.createActivityAction(quake, world, imageStore);
                quake.scheduleActions(a, scheduler);
            }
        }

        scheduler.scheduleEvent(this,
                Factory.createActivityAction(this, world, imageStore),
                nextPeriod);
        }

    public boolean moveToOreBlob(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (!(this.onFire)){
            if (this.position.adjacent(target.getPosition())) {
                world.removeEntity(target);
                scheduler.unscheduleAllEvents(target);
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
        return this.moveToOnFire(world, scheduler);
    }

    public void setOnFire(ImageStore imStore) {
        super.setOnFire(imStore);
        this.images = imStore.getImageList("blob-fire");
    }

}