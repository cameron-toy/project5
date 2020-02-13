
public class AnimationAction implements Action {

    private AnimationEntity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

    public AnimationAction(AnimationEntity entity, WorldModel world, ImageStore imageStore, int repeatCount) {
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
        this.repeatCount = repeatCount;
    }

    public AnimationEntity getEntity () { return this.entity; }

    public WorldModel getWorld() { return this.world; }

    public ImageStore getImageStore() { return this.imageStore; }

    public void executeAction(EventScheduler scheduler) {
        this.entity.nextImage();

        if (this.repeatCount != 1) {
            scheduler.scheduleEvent(this.entity,
                    Factory.createAnimationAction(this.entity, Math.max(this.repeatCount - 1, 0)),
                    this.entity.getAnimationPeriod());
        }
    }
}