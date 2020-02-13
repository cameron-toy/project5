import java.util.List;
import java.util.Optional;

import processing.core.PImage;

public final class Entity
{
    private EntityKind kind;
    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int resourceLimit;
    private int resourceCount;
    private int actionPeriod;
    private int animationPeriod;

    public Entity(
            EntityKind kind,
            String id,
            Point position,
            List<PImage> images,
            int resourceLimit,
            int resourceCount,
            int actionPeriod,
            int animationPeriod)
    {
        this.kind = kind;
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }


    public PImage getCurrentImage() {
        return this.images.get(this.imageIndex);
    }

    public int getAnimationPeriod() {
        switch (this.kind) {
            case MINER_FULL:
            case MINER_NOT_FULL:
            case ORE_BLOB:
            case QUAKE:
                return this.animationPeriod;
            default:
                throw new UnsupportedOperationException(
                        String.format("getAnimationPeriod not supported for %s",
                                this.kind));
        }
    }

    public void nextImage() {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }


    public void transformFull(
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        Entity miner = Functions.createMinerNotFull(this.id, this.resourceLimit,
                this.position, this.actionPeriod,
                this.animationPeriod,
                this.images);

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(miner);
        Action a = Functions.createActivityAction(miner, world, imageStore);
        scheduler.scheduleActions(a);
    }

//    private Entity createMinerNotFull()
//    {
//        return new Entity(EntityKind.MINER_NOT_FULL, this.id, this.position, this.images,
//                this.resourceLimit, 0, this.actionPeriod,this. animationPeriod);
//    }

//    public void scheduleActions(
//
//            EventScheduler scheduler,
//            WorldModel world,
//            ImageStore imageStore)
//    {
//        switch (this.kind) {
//            case MINER_FULL:
//                scheduler.scheduleEvent(this,
//                        Functions.createActivityAction(this, world, imageStore),
//                        this.actionPeriod);
//                scheduler.scheduleEvent(this,
//                        Functions.createAnimationAction(this,0),
//                        this.getAnimationPeriod());
//                break;
//
//            case MINER_NOT_FULL:
//                scheduler.scheduleEvent(this,
//                        Functions.createActivityAction(this, world, imageStore),
//                        this.actionPeriod);
//                scheduler.scheduleEvent(this,
//                        Functions.createAnimationAction(this,0),
//                        this.getAnimationPeriod());
//                break;
//
//            case ORE:
//                scheduler.scheduleEvent(this,
//                        Functions.createActivityAction(this, world, imageStore),
//                        this.actionPeriod);
//                break;
//
//            case ORE_BLOB:
//                scheduler.scheduleEvent(this,
//                        Functions.createActivityAction(this, world, imageStore),
//                        this.actionPeriod);
//                scheduler.scheduleEvent(this,
//                        Functions.createAnimationAction(this, 0),
//                        this.getAnimationPeriod());
//                break;
//
//            case QUAKE:
//                scheduler.scheduleEvent(this,
//                        Functions.createActivityAction(this, world, imageStore),
//                        this.actionPeriod);
//                scheduler.scheduleEvent(this, Functions.createAnimationAction(this, QUAKE_ANIMATION_REPEAT_COUNT),
//                        this.getAnimationPeriod());
//                break;
//
//            case VEIN:
//                scheduler.scheduleEvent(this,
//                        Functions.createActivityAction(this, world, imageStore),
//                        this.actionPeriod);
//                break;
//
//            default:
//        }
//    }

//    public Action createAnimationAction(int repeatCount) {
//        return new Action(ActionKind.ANIMATION, this, null, null,
//                repeatCount);
//    }
//
//    public Action createActivityAction(
//            WorldModel world, ImageStore imageStore)
//    {
//        return new Action(ActionKind.ACTIVITY, this, world, imageStore, 0);
//    }


    public boolean moveToFull(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (this.position.adjacent(target.position)) {
            return true;
        }
        else {
            Point nextPos = this.nextPositionMiner(world, target.position);

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

    private void moveEntity(WorldModel world, Point pos) {
        Point oldPos = this.position;
        if (pos.withinBounds(world) && !pos.equals(oldPos)) {
            world.setOccupancyCell(oldPos, null);
            world.removeEntityAt(pos);
            world.setOccupancyCell(pos, this);
            this.position = pos;
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

    public boolean transformNotFull(
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        if (this.resourceCount >= this.resourceLimit) {
            Entity miner = Functions.createMinerFull(this.id, this.resourceLimit,
                    this.position, this.actionPeriod,
                    this.animationPeriod,
                    this.images);

            world.removeEntity(this);
            scheduler.unscheduleAllEvents(this);

            world.addEntity(miner);
            Action a = Functions.createActivityAction(miner, world, imageStore);
            scheduler.scheduleActions(a);

            return true;
        }

        return false;
    }

    public boolean moveToNotFull(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (this.position.adjacent(target.position)) {
            this.resourceCount += 1;
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);

            return true;
        }
        else {
            Point nextPos = this.nextPositionMiner(world, target.position);

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

    // blob specific
    public boolean moveToOreBlob(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (this.position.adjacent(target.position)) {
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);
            return true;
        }
        else {
            Point nextPos = this.nextPositionOreBlob(world, target.position);

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

        if (horiz == 0 || (occupant.isPresent() && !(occupant.get().kind
                == EntityKind.ORE)))
        {
            int vert = Integer.signum(destPos.getY() - this.position.getY());
            newPos = new Point(this.position.getX(), this.position.getY() + vert);
            occupant = world.getOccupant(newPos);
            if (vert == 0 || (occupant.isPresent() && !(occupant.get().kind
                    == EntityKind.ORE)))
            {
                newPos = this.position;
            }
        }

        return newPos;
    }

    public Point getPosition() { return this.position; }

    public EntityKind getKind() { return this.kind; }

    public String getId() { return this.id; }

    public int getActionPeriod() { return this.actionPeriod; }

    public void setPosition(Point p) { this.position = p; }






}
