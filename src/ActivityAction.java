
public class ActivityAction extends Action {

    public ActivityAction(ActivityEntity entity, WorldModel world, ImageStore imageStore) {
        super(entity, world, imageStore);
    }

    public void executeAction(EventScheduler scheduler) {
        ActivityEntity ent = (ActivityEntity) this.entity;
        ent.executeActivity(this.world, this.imageStore, scheduler);
    }

}