import processing.core.PImage;
import java.util.List;

public abstract class Entity {

    protected String id;
    protected Point position;
    protected List<PImage> images;

    public Entity (
            String id,
            Point position,
            List<PImage> images) {
        this.id = id;
        this.position = position;
        this.images = images;
    }

    public PImage getCurrentImage() {
        return this.images.get(0);
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

    public Point getPosition() { return this.position; }

    public String getId() { return this.id; }

    public void setPosition(Point p) { this.position = p; }


}