import processing.core.PImage;
import java.util.List;

public class Ore implements ActivityEntity {

    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int actionPeriod;
    private static final String BLOB_KEY = "blob";
    private static final String BLOB_ID_SUFFIX = " -- blob";
    private static final int BLOB_PERIOD_SCALE = 4;
    private static final int BLOB_ANIMATION_MIN = 50;
    private static final int BLOB_ANIMATION_MAX = 150;

    public Ore(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod
    ) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.actionPeriod = actionPeriod;
    }

    public PImage getCurrentImage() { return this.images.get(this.imageIndex); }

    public void nextImage() { this.imageIndex = (this.imageIndex + 1) % this.images.size(); }

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

    public Point getPosition() { return this.position; }

    public String getId() { return this.id; }

    public void setPosition(Point p) { this.position = p; }

    public int getActionPeriod() { return this.actionPeriod; }

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler
    ) {
        Point pos = this.getPosition();

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        OreBlob blob = Factory.createOreBlob(this.getId() + BLOB_ID_SUFFIX, pos,
                this.getActionPeriod() / BLOB_PERIOD_SCALE,
                BLOB_ANIMATION_MIN + VirtualWorld.getRand().nextInt(
                        BLOB_ANIMATION_MAX
                                - BLOB_ANIMATION_MIN),
                imageStore.getImageList(BLOB_KEY));

        world.addEntity(blob);
        ActivityAction a = Factory.createActivityAction(blob, world, imageStore);
        blob.scheduleActions(a, scheduler);
    }

    public void scheduleActions(Action action, EventScheduler scheduler) {
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createActivityAction(this, action.getWorld(), action.getImageStore()), this.actionPeriod);
    }
}