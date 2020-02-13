
public class ActivityAction implements Action {

    private ActivityEntity entity;
    private WorldModel world;
    private ImageStore imageStore;

    public ActivityAction(ActivityEntity entity, WorldModel world, ImageStore imageStore) {
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
    }

    public ActivityEntity getEntity () { return this.entity; }

    public WorldModel getWorld() { return this.world; }

    public ImageStore getImageStore() { return this.imageStore; }

    public void executeAction(EventScheduler scheduler)
    {
        this.entity.executeActivity(this.world, this.imageStore, scheduler);
    }

}