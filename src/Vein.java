import processing.core.PImage;
import java.util.List;
import java.util.Optional;

public class Vein implements ActivityEntity {

    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int actionPeriod;
    private static final String ORE_ID_PREFIX = "ore -- ";
    private static final int ORE_CORRUPT_MIN = 20000;
    private static final int ORE_CORRUPT_MAX = 30000;
    private static final String ORE_KEY = "ore";

    public Vein(
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
        Optional<Point> openPt = world.findOpenAround(this.getPosition());

        if (openPt.isPresent()) {
            Ore ore = Factory.createOre(ORE_ID_PREFIX + this.getId(), openPt.get(),
                    ORE_CORRUPT_MIN + VirtualWorld.getRand().nextInt(
                            ORE_CORRUPT_MAX - ORE_CORRUPT_MIN),
                    imageStore.getImageList(ORE_KEY));
            world.addEntity(ore);
            ActivityAction a = Factory.createActivityAction(ore, world, imageStore);
            ore.scheduleActions(a, scheduler);
        }

        scheduler.scheduleEvent(this,
                Factory.createActivityAction(this, world, imageStore),
                this.getActionPeriod());
    }

    public void scheduleActions(Action action, EventScheduler scheduler) {
        scheduler.scheduleEvent(action.getEntity(),
                Factory.createActivityAction(this, action.getWorld(), action.getImageStore()), this.actionPeriod);
    }
}