import processing.core.PImage;
import java.util.List;

public abstract class ActivityEntity extends Entity {

   protected int actionPeriod;

    public ActivityEntity(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod) {
        super(id, position, images);
        this.actionPeriod = actionPeriod;
    }

    public abstract void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler);

    public int getActionPeriod() { return this.actionPeriod; }

    public void scheduleActions(Action action, EventScheduler scheduler) {
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createActivityAction(this, action.getWorld(), action.getImageStore()), this.actionPeriod);
    }

}