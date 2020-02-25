import processing.core.PImage;

import java.util.List;

public abstract class AnimationEntity extends ActivityEntity {

    protected int animationPeriod;
    protected int imageIndex;

    public AnimationEntity(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod) {
        super(id, position, images, actionPeriod);
        this.animationPeriod = animationPeriod;
    }

    public int getAnimationPeriod() { return this.animationPeriod; }

    public void scheduleActions(Action action, EventScheduler scheduler) {
        super.scheduleActions(action, scheduler);
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createAnimationAction(this, 0), this.animationPeriod);
    }

    public void nextImage() {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }

    public PImage getCurrentImage() {
        return this.images.get(this.imageIndex);
    }

}