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
            WorldModel world) {
        super(id, position, images, actionPeriod, animationPeriod);
        previousOccupant = world.getOccupant(position);
        world.removeEntityAt(position);
    }

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler) {
        if (previousOccupant.isPresent()) {
            Entity ent = previousOccupant.get();
            if (ent instanceof ActivityEntity) {
                if (ent instanceof Ignitable) {
                    Ignitable burning = (Ignitable) ent;
                    burning.setOnFire();
                    world.tryAddEntity(burning);
                    ActivityAction a = Factory.createActivityAction(burning, world, imageStore);
                    burning.scheduleActions(a, scheduler);
                    return;
                }
                ActivityEntity ae = (ActivityEntity) ent;
                world.tryAddEntity(ae);
                ActivityAction a = Factory.createActivityAction(ae, world, imageStore);
                ae.scheduleActions(a, scheduler);
                return;
            }
            world.tryAddEntity(ent);
        }
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);
    }

    public void scheduleActions(Action action, EventScheduler scheduler) {
        super.scheduleActions(action, scheduler);
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createAnimationAction(this, FIRE_ANIMATION_REPEAT_COUNT), this.animationPeriod);
    }

}