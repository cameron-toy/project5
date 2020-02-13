import processing.core.PImage;

public interface Entity {

    PImage getCurrentImage();

    void nextImage();

    void moveEntity(
            WorldModel world,
            Point pos);

    Point getPosition();

    String getId();

    void setPosition(Point p);


}