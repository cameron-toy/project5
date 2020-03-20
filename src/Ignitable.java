import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class Ignitable extends AnimationEntity {

    protected boolean onFire;
    protected int lifeSpan;

    public Ignitable(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod) {
        super(id, position, images, actionPeriod, animationPeriod);
        this.onFire = false;
        this.lifeSpan = 0;
    }

    public void setOnFire() { this.onFire = true; }
    public void setLifeSpan() {
        int min = 5;
        int max = 20;

        Random rand = new Random();
        this.lifeSpan = rand.nextInt((max - min) + 1) + min;
    }

    public boolean moveToOnFire(WorldModel world,
                                EventScheduler scheduler) {
        List<Point> potential = new ArrayList<>();

        potential.add(this.position.shift(0, 1));
        potential.add(this.position.shift(1, 0));
        potential.add(this.position.shift(0, -1));
        potential.add(this.position.shift(-1, 0));

        potential.removeIf(p -> !(!(world.isOccupied(p)) && p.withinBounds(world)));
        Random rand = new Random();
        Point nextPos = potential.get(rand.nextInt(potential.size()));
        if (!this.position.equals(nextPos)) {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            occupant.ifPresent(scheduler::unscheduleAllEvents);
            this.moveEntity(world, nextPos);
        }
        return false;
    }
}
