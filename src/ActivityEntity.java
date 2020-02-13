
public interface ActivityEntity extends Entity {

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler);

    public int getActionPeriod();

    void scheduleActions(Action action, EventScheduler scheduler);

}