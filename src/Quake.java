
import processing.core.PImage;
import java.util.List;
import java.util.Optional;

public class Quake implements ActivityEntity, AnimationEntity {

    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int actionPeriod;
    private int animationPeriod;
    private static final int QUAKE_ANIMATION_REPEAT_COUNT = 10;

    public Quake(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod
    ) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    public PImage getCurrentImage() {
        return this.images.get(this.imageIndex);
    }

    public void nextImage() {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }

    public void moveEntity(
            WorldModel world,
            Point pos) {
        Point oldPos = this.position;
        if (pos.withinBounds(world) && !pos.equals(oldPos)) {
            world.setOccupancyCell(oldPos, null);
            world.removeEntityAt(pos);
            world.setOccupancyCell(pos, this);
            this.position = pos;
        }
    }

    public Point getPosition() {
        return this.position;
    }

    public String getId() {
        return this.id;
    }

    public void setPosition(Point p) {
        this.position = p;
    }

    public int getActionPeriod() { return this.actionPeriod; }

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler) {
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);
    }

    public int getAnimationPeriod() { return this.animationPeriod; }

    public void scheduleActions(Action action, EventScheduler scheduler) {
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createActivityAction(this, action.getWorld(), action.getImageStore()), this.actionPeriod);
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createAnimationAction(this, QUAKE_ANIMATION_REPEAT_COUNT), this.animationPeriod);
    }
}