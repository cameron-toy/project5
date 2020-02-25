import processing.core.PImage;

public class AnimationAction extends Action {

    private int repeatCount;

    public AnimationAction(
            AnimationEntity entity,
            WorldModel world,
            ImageStore imageStore,
            int repeatCount) {
        super(entity, world, imageStore);
        this.repeatCount = repeatCount;
    }



    public void executeAction(EventScheduler scheduler) {
        AnimationEntity ent = (AnimationEntity) this.entity;
        ent.nextImage();

        if (this.repeatCount != 1) {
            scheduler.scheduleEvent(ent,
                    Factory.createAnimationAction(ent, Math.max(this.repeatCount - 1, 0)),
                    ent.getAnimationPeriod());
        }
    }
}