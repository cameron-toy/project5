import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class MinerFull implements ActivityEntity, AnimationEntity {
    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int resourceLimit;
    private int actionPeriod;
    private int animationPeriod;

    public MinerFull(
            String id,
            Point position,
            List<PImage> images,
            int resourceLimit,
            int actionPeriod,
            int animationPeriod
    ) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.resourceLimit = resourceLimit;
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
        Optional<Entity> fullTarget =
                world.findNearest(this.getPosition(), Blacksmith.class);

        if (fullTarget.isPresent() && this.moveToFull(world,
                fullTarget.get(), scheduler))
        {
            this.transformFull(world, scheduler, imageStore);
        }
        else {
            scheduler.scheduleEvent(this,
                    Factory.createActivityAction(this, world, imageStore),
                    this.getActionPeriod());
        }
    }

    public boolean moveToFull(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (this.position.adjacent(target.getPosition())) {
            return true;
        }
        else {
            Point nextPos = this.nextPositionMiner(world, target.getPosition());

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

    private Point nextPositionMiner(
            WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.getX() - this.position.getX());
        Point newPos = new Point(this.position.getX() + horiz, this.position.getY());

        if (horiz == 0 || world.isOccupied(newPos)) {
            int vert = Integer.signum(destPos.getY() - this.position.getY());
            newPos = new Point(this.position.getX(), this.position.getY() + vert);

            if (vert == 0 || world.isOccupied(newPos)) {
                newPos = this.position;
            }
        }

        return newPos;
    }

    public void transformFull(
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        MinerNotFull miner = Factory.createMinerNotFull(this.id, this.resourceLimit,
                this.position, this.actionPeriod,
                this.animationPeriod,
                this.images);

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(miner);
        ActivityAction a = Factory.createActivityAction(miner, world, imageStore);
        miner.scheduleActions(a, scheduler);
    }

    public int getAnimationPeriod() { return this.animationPeriod; }

    public void scheduleActions(Action action, EventScheduler scheduler) {
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createActivityAction(this, action.getWorld(), action.getImageStore()), this.actionPeriod);
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createAnimationAction(this, 0), this.animationPeriod);
    }
}

