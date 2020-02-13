
import processing.core.PImage;
import java.util.List;
import java.util.Optional;

public class OreBlob implements ActivityEntity, AnimationEntity {

    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int actionPeriod;
    private int animationPeriod;
    private static final String QUAKE_KEY = "quake";

    public OreBlob(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod
    ) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    public PImage getCurrentImage() {
        return this.images.get(this.imageIndex);
    }

    public void nextImage() {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }

    public void moveEntity(
            WorldModel world,
            Point pos) {
        Point oldPos = this.position;
        if (pos.withinBounds(world) && !pos.equals(oldPos)) {
            world.setOccupancyCell(oldPos, null);
            world.removeEntityAt(pos);
            world.setOccupancyCell(pos, this);
            this.position = pos;
        }
    }

    public Point getPosition() {
        return this.position;
    }

    public String getId() {
        return this.id;
    }

    public void setPosition(Point p) {
        this.position = p;
    }

    public int getActionPeriod() { return this.actionPeriod; }

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler) {
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
        if (this.position.adjacent(target.getPosition())) {
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);
            return true;
        }
        else {
            Point nextPos = this.nextPositionOreBlob(world, target.getPosition());

            if (!this.position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                this.moveEntity(world, nextPos);
            }
            return false;
        }
    }

    private Point nextPositionOreBlob(
            WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.getX() - this.position.getX());
        Point newPos = new Point(this.position.getX() + horiz, this.position.getY());

        Optional<Entity> occupant = world.getOccupant(newPos);

        if (horiz == 0 || (occupant.isPresent() && !(occupant.get().getClass()
                == Ore.class)))
        {
            int vert = Integer.signum(destPos.getY() - this.position.getY());
            newPos = new Point(this.position.getX(), this.position.getY() + vert);
            occupant = world.getOccupant(newPos);
            if (vert == 0 || (occupant.isPresent() && !(occupant.get().getClass()
                    == Ore.class)))
            {
                newPos = this.position;
            }
        }

        return newPos;
    }

    public int getAnimationPeriod() { return this.animationPeriod; }

    public void scheduleActions(Action action, EventScheduler scheduler) {
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createActivityAction(this, action.getWorld(), action.getImageStore()), this.actionPeriod);
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createAnimationAction(this, 0), this.animationPeriod);
    }
}