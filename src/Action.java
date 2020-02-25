
public abstract class Action {

    protected Entity entity;
    protected WorldModel world;
    protected ImageStore imageStore;

    public Action(
            Entity entity,
            WorldModel world,
            ImageStore imageStore) {
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
    }


    public Entity getEntity () { return this.entity; }

    public WorldModel getWorld() { return this.world; }

    public ImageStore getImageStore() { return this.imageStore; }

    public abstract void executeAction(EventScheduler scheduler);
}