import processing.core.PImage;
import java.util.List;
import java.util.Optional;

public class Fire extends AnimationEntity {

    Optional<Entity> previousOccupant;
    private static final int FIRE_ANIMATION_REPEAT_COUNT = 31;

    public Fire(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod,
            WorldModel world,
            EventScheduler scheduler) {
        super(id, position, images, actionPeriod, animationPeriod);
        previousOccupant = world.getOccupant(position);
        previousOccupant.ifPresent(scheduler::unscheduleAllEvents);
        previousOccupant.ifPresent(world::removeEntity);
    }

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler) {
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);
        if (previousOccupant.isPresent()) {
            Entity ent = previousOccupant.get();

            if (ent instanceof ActivityEntity) {
                if (ent instanceof Ignitable) {
                    Ignitable burning = (Ignitable) ent;
                    burning.setOnFire();
                    world.tryAddEntity(burning);
                    ActivityAction a = Factory.createActivityAction(burning, world, imageStore);
                    burning.scheduleActions(a, scheduler);
                    scheduler.scheduleEvent(burning,
                            Factory.createActivityAction(burning, world, imageStore),
                            burning.getActionPeriod());
                    scheduler.unscheduleAllEvents(this);
                    world.removeEntity(this);
                    return;
                }
                ActivityEntity ae = (ActivityEntity) ent;
                world.tryAddEntity(ae);
                ActivityAction a = Factory.createActivityAction(ae, world, imageStore);
                ae.scheduleActions(a, scheduler);
                scheduler.scheduleEvent(ae,
                        Factory.createActivityAction(ae, world, imageStore),
                        ae.getActionPeriod());
                return;
            }
            world.tryAddEntity(ent);
        }
    }

    public void scheduleActions(Action action, EventScheduler scheduler) {
        super.scheduleActions(action, scheduler);
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createAnimationAction(this, FIRE_ANIMATION_REPEAT_COUNT), this.animationPeriod);
    }

}