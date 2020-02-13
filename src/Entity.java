import processing.core.PImage;

public interface EntityInterface {

    public PImage getCurrentImage();

    public void nextImage();

    public void moveEntity(
            WorldModel world,
            Point pos);

    public Point getPosition();

    public String getId();

    public int getActionPeriod();

    public void setPosition(Point p);

    public boolean moveToOreBlob(
            WorldModel world,
            Entity target,
            EventScheduler scheduler);

    public boolean moveToNotFull(
            WorldModel world,
            Entity target,
            EventScheduler scheduler);



}