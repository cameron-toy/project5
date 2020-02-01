import java.util.Optional;
import java.util.Random;

public final class Action
{
    private ActionKind kind;
    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;
    private static final String BLOB_KEY = "blob";
    private static final String BLOB_ID_SUFFIX = " -- blob";
    private static final int BLOB_PERIOD_SCALE = 4;
    private static final int BLOB_ANIMATION_MIN = 50;
    private static final int BLOB_ANIMATION_MAX = 150;
    private static final int ORE_REACH = 1;
    private static final String ORE_ID_PREFIX = "ore -- ";
    private static final int ORE_CORRUPT_MIN = 20000;
    private static final int ORE_CORRUPT_MAX = 30000;
    private static final String ORE_KEY = "ore";
    private static final String QUAKE_KEY = "quake";
    private static final Random rand = new Random();


    public Action(
            ActionKind kind,
            Entity entity,
            WorldModel world,
            ImageStore imageStore,
            int repeatCount)
    {
        this.kind = kind;
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
        this.repeatCount = repeatCount;
    }

    public void executeAction(EventScheduler scheduler) {
        switch (this.kind) {
            case ACTIVITY:
                this.executeActivityAction(scheduler);
                break;

            case ANIMATION:
                this.executeAnimationAction(scheduler);
                break;
        }
    }

    private void executeActivityAction(
            EventScheduler scheduler)
    {
        switch (this.entity.getKind()) {
            case MINER_FULL:
                this.executeMinerFullActivity(scheduler);
                break;

            case MINER_NOT_FULL:
                this.executeMinerNotFullActivity(scheduler);
                break;

            case ORE:
                executeOreActivity(scheduler);
                break;

            case ORE_BLOB:
                executeOreBlobActivity(scheduler);
                break;

            case QUAKE:
                executeQuakeActivity(scheduler);
                break;

            case VEIN:
                this.executeVeinActivity(scheduler);
                break;

            default:
                throw new UnsupportedOperationException(String.format(
                        "executeActivityAction not supported for %s",
                        this.entity.getKind()));
        }
    }

    private void executeAnimationAction(
            EventScheduler scheduler)
    {
        this.entity.nextImage();

        if (this.repeatCount != 1) {
            scheduler.scheduleEvent(this.entity,
                    Functions.createAnimationAction(this.entity, Math.max(this.repeatCount - 1, 0)),
                    this.entity.getAnimationPeriod());
        }
    }

    private void executeOreActivity(
            EventScheduler scheduler)
    {
        Point pos = this.entity.getPosition();

        this.world.removeEntity(this.entity);
        scheduler.unscheduleAllEvents(this.entity);

        Entity blob = Functions.createOreBlob(this.entity.getId() + BLOB_ID_SUFFIX, pos,
                this.entity.getActionPeriod() / BLOB_PERIOD_SCALE,
                BLOB_ANIMATION_MIN + this.rand.nextInt(
                        BLOB_ANIMATION_MAX
                                - BLOB_ANIMATION_MIN),
                this.imageStore.getImageList(BLOB_KEY));

        this.world.addEntity(blob);
        Action a = Functions.createActivityAction(blob, this.world, this.imageStore);
        scheduler.scheduleActions(a);
    }

    private void executeMinerNotFullActivity(
            EventScheduler scheduler)
    {
        Optional<Entity> notFullTarget =
                this.world.findNearest(this.entity.getPosition(), EntityKind.ORE);

        if (!notFullTarget.isPresent() || !this.entity.moveToNotFull(this.world,
                notFullTarget.get(),
                scheduler)
                || !this.entity.transformNotFull(this.world, scheduler, this.imageStore))
        {
            scheduler.scheduleEvent(entity,
                    Functions.createActivityAction(this.entity, this.world, this.imageStore),
                    this.entity.getActionPeriod());
        }
    }


    private void executeMinerFullActivity(
            EventScheduler scheduler)
    {
        Optional<Entity> fullTarget =
                this.world.findNearest(this.entity.getPosition(), EntityKind.BLACKSMITH);

        if (fullTarget.isPresent() && entity.moveToFull(this.world,
                fullTarget.get(), scheduler))
        {
            this.entity.transformFull(this.world, scheduler, this.imageStore);
        }
        else {
            scheduler.scheduleEvent(this.entity,
                    Functions.createActivityAction(this.entity, this.world, this.imageStore),
                    this.entity.getActionPeriod());
        }
    }

    private void executeVeinActivity(
            EventScheduler scheduler)
    {
        Optional<Point> openPt = this.world.findOpenAround(this.entity.getPosition());

        if (openPt.isPresent()) {
            Entity ore = Functions.createOre(ORE_ID_PREFIX + this.entity.getId(), openPt.get(),
                    ORE_CORRUPT_MIN + this.rand.nextInt(
                            ORE_CORRUPT_MAX - ORE_CORRUPT_MIN),
                    this.imageStore.getImageList(ORE_KEY));
            this.world.addEntity(ore);
            Action a = Functions.createActivityAction(ore, this.world, this.imageStore);
            scheduler.scheduleActions(a);
        }

        scheduler.scheduleEvent(this.entity,
                Functions.createActivityAction(this.entity, this.world, this.imageStore),
                this.entity.getActionPeriod());
    }

    private void executeOreBlobActivity(
            EventScheduler scheduler)
    {
        Optional<Entity> blobTarget =
                this.world.findNearest(this.entity.getPosition(), EntityKind.VEIN);
        long nextPeriod = this.entity.getActionPeriod();

        if (blobTarget.isPresent()) {
            Point tgtPos = blobTarget.get().getPosition();

            if (this.entity.moveToOreBlob(this.world, blobTarget.get(), scheduler)) {
                Entity quake = Functions.createQuake(tgtPos,
                        this.imageStore.getImageList(QUAKE_KEY));

                this.world.addEntity(quake);
                nextPeriod += this.entity.getActionPeriod();
                Action a = Functions.createActivityAction(quake, this.world, this.imageStore);
                scheduler.scheduleActions(a);
            }
        }

        scheduler.scheduleEvent(this.entity,
                Functions.createActivityAction(this.entity, this.world, this.imageStore),
                nextPeriod);
    }

    private void executeQuakeActivity(
            EventScheduler scheduler)
    {
        scheduler.unscheduleAllEvents(this.entity);
        this.world.removeEntity(this.entity);
    }

    public Entity getEntity () { return this.entity; }

    public WorldModel getWorld() { return this.world; }

    public ImageStore getImageStore() { return this.imageStore; }





}
