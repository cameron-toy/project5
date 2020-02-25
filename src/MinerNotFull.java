import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class MinerNotFull extends Miner {

    private int resourceCount;

    public MinerNotFull(
            String id,
            Point position,
            List<PImage> images,
            int resourceLimit,
            int resourceCount,
            int actionPeriod,
            int animationPeriod
    ) {
        super(id, position, images, resourceLimit, actionPeriod, animationPeriod);
        this.resourceCount = resourceCount;
    }

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler) {
        Optional<Entity> notFullTarget =
                world.findNearest(this.getPosition(), Ore.class);

        if (!notFullTarget.isPresent() || !this.moveTo(world,
                notFullTarget.get(),
                scheduler)
                || !this.transform(world, scheduler, imageStore))
        {
            scheduler.scheduleEvent(this,
                    Factory.createActivityAction(this, world, imageStore),
                    this.getActionPeriod());
        }
    }

    public boolean moveTo(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (this.position.adjacent(target.getPosition())) {
            this.resourceCount += 1;
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);

            return true;
        }
        else {
            return super.moveTo(world, target, scheduler);
        }
    }

    public boolean transform(
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        if (this.resourceCount >= this.resourceLimit) {
            ActivityEntity miner = Factory.createMinerFull(this.id, this.resourceLimit,
                    this.position, this.actionPeriod,
                    this.animationPeriod,
                    this.images);

            world.removeEntity(this);
            scheduler.unscheduleAllEvents(this);

            world.addEntity(miner);
            ActivityAction a = Factory.createActivityAction(miner, world, imageStore);
            miner.scheduleActions(a, scheduler);

            return true;
        }

        return false;
    }
}
