import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import processing.core.*;

public final class VirtualWorld extends PApplet
{
    private static final int TIMER_ACTION_PERIOD = 100;

    private static final int ALIEN_ANIMATION_PERIOD = 100;
    private static final int ALIEN_ACTION_PERIOD = 1000;

    private static int vein_count = 0;
    private static final int VIEW_WIDTH = 640;
    private static final int VIEW_HEIGHT = 480;
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = 32;
    private static final int WORLD_WIDTH_SCALE = 2;
    private static final int WORLD_HEIGHT_SCALE = 2;

    private static final int VIEW_COLS = VIEW_WIDTH / TILE_WIDTH;
    private static final int VIEW_ROWS = VIEW_HEIGHT / TILE_HEIGHT;
    private static final int WORLD_COLS = VIEW_COLS * WORLD_WIDTH_SCALE;
    private static final int WORLD_ROWS = VIEW_ROWS * WORLD_HEIGHT_SCALE;

    private static final String IMAGE_LIST_FILE_NAME = "imagelist";
    private static final String DEFAULT_IMAGE_NAME = "background_default";
    private static final int DEFAULT_IMAGE_COLOR = 0x808080;

    private static final String LOAD_FILE_NAME = "world.sav";

    private static final String FAST_FLAG = "-fast";
    private static final String FASTER_FLAG = "-faster";
    private static final String FASTEST_FLAG = "-fastest";
    private static final double FAST_SCALE = 0.5;
    private static final double FASTER_SCALE = 0.25;
    private static final double FASTEST_SCALE = 0.10;

    private static double timeScale = 1.0;

    private static final Random rand = new Random();

    private static final Point ignoreArray[] = new Point[]{
            new Point(0, 0),
            new Point(-4, -4),
            new Point(-4, -3),
            new Point(-4, 3),
            new Point(-4, 4),
            new Point(4, -4),
            new Point(4, -3),
            new Point(4, 4),
            new Point(4, 3),
            new Point(3, 4),
            new Point(3, -4),
            new Point(-3, 4),
            new Point(-3, -4)
    };

    private static final Set ignoreSet = new HashSet<>(Arrays.asList(ignoreArray));

    private ImageStore imageStore;
    private WorldModel world;
    private WorldView view;
    private EventScheduler scheduler;

    private long nextTime;

    public void settings() {
        size(VIEW_WIDTH, VIEW_HEIGHT);
    }

    /*
       Processing entry point for "sketch" setup.
    */
    public void setup() {
        this.imageStore = new ImageStore(
                createImageColored(TILE_WIDTH, TILE_HEIGHT,
                        DEFAULT_IMAGE_COLOR));
        this.world = new WorldModel(WORLD_ROWS, WORLD_COLS,
                createDefaultBackground(imageStore));
        this.view = new WorldView(VIEW_ROWS, VIEW_COLS, this, world, TILE_WIDTH,
                TILE_HEIGHT);
        this.scheduler = new EventScheduler(timeScale);

        loadImages(IMAGE_LIST_FILE_NAME, imageStore, this);
        loadWorld(world, LOAD_FILE_NAME, imageStore);

        scheduleActions(world, scheduler, imageStore);

        nextTime = System.currentTimeMillis() + TIMER_ACTION_PERIOD;
    }

    public void draw() {
        long time = System.currentTimeMillis();
        if (time >= nextTime) {
            this.scheduler.updateOnTime(time);
            nextTime = time + TIMER_ACTION_PERIOD;
        }

        view.drawViewport();
    }

    public void keyPressed() {
        if (key == CODED) {
            int dx = 0;
            int dy = 0;

            switch (keyCode) {
                case UP:
                    dy = -1;
                    break;
                case DOWN:
                    dy = 1;
                    break;
                case LEFT:
                    dx = -1;
                    break;
                case RIGHT:
                    dx = 1;
                    break;
            }
            view.shiftView(dx, dy);
        }
    }

    public void makeNewOre(Point p) {
        String id = String.format("CLICK_VEIN_%d", vein_count++);
        Vein v = Factory.createVein(
                id,
                p,
                22000,
                imageStore.getImageList("vein"));

        Optional<Entity> previousOccupant = world.getOccupant(p);
        previousOccupant.ifPresent(scheduler::unscheduleAllEvents);
        previousOccupant.ifPresent(world::removeEntity);

        world.addEntity(v);
        ActivityAction a = Factory.createActivityAction(v, world, imageStore);
        v.scheduleActions(a, scheduler);
        scheduler.scheduleEvent(v,
                Factory.createActivityAction(v, world, imageStore),
                v.getActionPeriod());
    }

    public void makeNewFire(Point p) {
        String id = "fire";
        Fire f =  Factory.createFire(
                id,
                p,
                imageStore.getImageList("fire"),
                world,
                scheduler);
        world.addEntity(f);
        ActivityAction a = Factory.createActivityAction(f, world, imageStore);
        f.scheduleActions(a, scheduler);
        scheduler.scheduleEvent(f,
                Factory.createActivityAction(f, world, imageStore),
                f.getActionPeriod());
    }

    public void makeNewAlien(Point p) {
        String id = "alien";
        Alien a = Factory.createAlien(
                id,
                p,
                ALIEN_ACTION_PERIOD,
                ALIEN_ANIMATION_PERIOD,
                imageStore.getImageList("alien"));
        world.addEntity(a);
        ActivityAction l = Factory.createActivityAction(a, world, imageStore);
        a.scheduleActions(l, scheduler);
        scheduler.scheduleEvent(a,
                Factory.createActivityAction(a, world, imageStore),
                a.getActionPeriod());
    }


    public void mouseClicked() {
        int cellX = (int) Math.floor(mouseX / TILE_WIDTH) + view.getViewport().getCol();
        int cellY = (int) Math.floor(mouseY / TILE_HEIGHT) + view.getViewport().getRow();
        Point p = new Point(cellX, cellY);
        makeNewOre(p);
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                Point d = new Point(dx, dy);
                if (!ignoreSet.contains(d)) {
                    do {
                        makeNewFire(p.translate(d));
                    } while (rand.nextBoolean());
                }
            }
        }

    }

    private static Background createDefaultBackground(ImageStore imageStore) {
        return new Background(DEFAULT_IMAGE_NAME,
                imageStore.getImageList(DEFAULT_IMAGE_NAME));
    }

    private static PImage createImageColored(int width, int height, int color) {
        PImage img = new PImage(width, height, RGB);
        img.loadPixels();
        for (int i = 0; i < img.pixels.length; i++) {
            img.pixels[i] = color;
        }
        img.updatePixels();
        return img;
    }

    private static void loadImages(
            String filename, ImageStore imageStore, PApplet screen)
    {
        try {
            Scanner in = new Scanner(new File(filename));
            Functions.loadImages(in, imageStore, screen);
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void loadWorld(
            WorldModel world, String filename, ImageStore imageStore)
    {
        try {
            Scanner in = new Scanner(new File(filename));
            Functions.load(in, world, imageStore);
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void scheduleActions(
            WorldModel world, EventScheduler scheduler, ImageStore imageStore)
    {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof ActivityEntity) {
                ActivityEntity ae = (ActivityEntity) entity;
                ActivityAction a = Factory.createActivityAction(ae, world, imageStore);
                ae.scheduleActions(a, scheduler);
            }
        }
    }

    private static void parseCommandLine(String[] args) {
        for (String arg : args) {
            switch (arg) {
                case FAST_FLAG:
                    timeScale = Math.min(FAST_SCALE, timeScale);
                    break;
                case FASTER_FLAG:
                    timeScale = Math.min(FASTER_SCALE, timeScale);
                    break;
                case FASTEST_FLAG:
                    timeScale = Math.min(FASTEST_SCALE, timeScale);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        parseCommandLine(args);
        PApplet.main(VirtualWorld.class);
    }

    public static Random getRand() { return rand; }
}
