import processing.core.PImage;

import java.util.*;

public final class WorldModel
{
    private int numRows;
    private int numCols;
    private Background background[][];
    private Entity occupancy[][];
    private Set<Entity> entities;
    private static final int ORE_REACH = 1;

    public WorldModel(int numRows, int numCols, Background defaultBackground) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.background = new Background[numRows][numCols];
        this.occupancy = new Entity[numRows][numCols];
        this.entities = new HashSet<>();

        for (int row = 0; row < numRows; row++) {
            Arrays.fill(this.background[row], defaultBackground);
        }
    }


    public Optional<Entity> getOccupant(Point pos) {
        if (this.isOccupied(pos)) {
            return Optional.of(this.getOccupancyCell(pos));
        }
        else {
            return Optional.empty();
        }
    }

    public boolean isOccupied(Point pos) {
        return pos.withinBounds(this) && this.getOccupancyCell(pos) != null;
    }

    private Entity getOccupancyCell(Point pos) {
        return this.occupancy[pos.getY()][pos.getX()];
    }

    public void setOccupancyCell(
            Point pos, Entity entity)
    {
        this.occupancy[pos.getY()][pos.getX()] = entity;
    }

    public void removeEntityAt(Point pos) {
        if (pos.withinBounds(this) && this.getOccupancyCell(pos) != null) {
            Entity entity = this.getOccupancyCell(pos);

            /* This moves the entity just outside of the grid for
             * debugging purposes. */
            entity.setPosition(new Point(-1, -1));
            this.entities.remove(entity);
            this.setOccupancyCell(pos, null);
        }
    }

    public Optional<Entity> findNearest(
            Point pos, Class clazz)
    {
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : this.entities) {
            if (clazz.isInstance(entity)) {
                ofType.add(entity);
            }
        }

        return pos.nearestEntity(ofType);
    }

    public void tryAddEntity(Entity entity) {
        if (this.isOccupied(entity.getPosition())) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

        this.addEntity(entity);
    }

    public void addEntity(Entity entity) {
        if (entity.getPosition().withinBounds(this)) {
            this.setOccupancyCell(entity.getPosition(), entity);
            this.entities.add(entity);
        }
    }

    public void removeEntity(Entity entity) {
        this.removeEntityAt(entity.getPosition());
    }

    public Optional<Point> findOpenAround(Point pos) {
        for (int dy = -ORE_REACH; dy <= ORE_REACH; dy++) {
            for (int dx = -ORE_REACH; dx <= ORE_REACH; dx++) {
                Point newPt = new Point(pos.getX() + dx, pos.getY() + dy);
                if (newPt.withinBounds(this) && !this.isOccupied(newPt)) {
                    return Optional.of(newPt);
                }
            }
        }

        return Optional.empty();
    }

    public Optional<PImage> getBackgroundImage(
            Point pos)
    {
        if (pos.withinBounds(this)) {
            return Optional.of(this.getBackgroundCell(pos).getCurrentImage());
        }
        else {
            return Optional.empty();
        }
    }

    private Background getBackgroundCell(Point pos) {
        return this.background[pos.getY()][pos.getX()];
    }

    private void setBackgroundCell(
            Point pos, Background background)
    {
        this.background[pos.getY()][pos.getX()] = background;
    }

    public void setBackground(
            Point pos, Background background)
    {
        if (pos.withinBounds(this)) {
            this.setBackgroundCell(pos, background);
        }
    }

    public int getNumCols() { return this.numCols; }

    public int getNumRows() { return this.numRows; }

    public Set<Entity> getEntities() { return this.entities; }






}
