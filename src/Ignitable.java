import processing.core.PImage;
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
}
