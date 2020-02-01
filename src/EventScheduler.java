import java.util.*;

public final class EventScheduler
{
    private PriorityQueue<Event> eventQueue;
    private Map<Entity, List<Event>> pendingEvents;
    private double timeScale;
    private static final int QUAKE_ANIMATION_REPEAT_COUNT = 10;

    public EventScheduler(double timeScale) {
        this.eventQueue = new PriorityQueue<>(new EventComparator());
        this.pendingEvents = new HashMap<>();
        this.timeScale = timeScale;
    }

    public void unscheduleAllEvents(
            Entity entity)
    {
        List<Event> pending = this.pendingEvents.remove(entity);

        if (pending != null) {
            for (Event event : pending) {
                this.eventQueue.remove(event);
            }
        }
    }

    public void scheduleEvent(
            Entity entity,
            Action action,
            long afterPeriod)
    {
        long time = System.currentTimeMillis() + (long)(afterPeriod
                * this.timeScale);
        Event event = new Event(action, time, entity);

        this.eventQueue.add(event);

        // update list of pending events for the given entity
        List<Event> pending = this.pendingEvents.getOrDefault(entity,
                new LinkedList<>());
        pending.add(event);
        this.pendingEvents.put(entity, pending);
    }

    public void updateOnTime(long time) {
        while (!this.eventQueue.isEmpty()
                && this.eventQueue.peek().getTime() < time) {
            Event next = this.eventQueue.poll();

            this.removePendingEvent(next);

            next.getAction().executeAction(this);
        }
    }

    private void removePendingEvent(
            Event event)
    {
        List<Event> pending = this.pendingEvents.get(event.getEntity());

        if (pending != null) {
            pending.remove(event);
        }
    }

    public void scheduleActions(
            Action action
    )
    {
        switch (action.getEntity().getKind()) {
            case MINER_FULL:
                this.scheduleEvent(action.getEntity(),
                        Functions.createActivityAction(action.getEntity(), action.getWorld(), action.getImageStore()),
                        action.getEntity().getActionPeriod());
                this.scheduleEvent(action.getEntity(),
                        Functions.createAnimationAction(action.getEntity(),0),
                        action.getEntity().getAnimationPeriod());
                break;

            case MINER_NOT_FULL:
                this.scheduleEvent(action.getEntity(),
                        Functions.createActivityAction(action.getEntity(), action.getWorld(), action.getImageStore()),
                        action.getEntity().getActionPeriod());
                this.scheduleEvent(action.getEntity(),
                        Functions.createAnimationAction(action.getEntity(),0),
                        action.getEntity().getAnimationPeriod());
                break;

            case ORE:
                this.scheduleEvent(action.getEntity(),
                        Functions.createActivityAction(action.getEntity(), action.getWorld(), action.getImageStore()),
                        action.getEntity().getActionPeriod());
                break;

            case ORE_BLOB:
                this.scheduleEvent(action.getEntity(),
                        Functions.createActivityAction(action.getEntity(), action.getWorld(), action.getImageStore()),
                        action.getEntity().getActionPeriod());
                this.scheduleEvent(action.getEntity(),
                        Functions.createAnimationAction(action.getEntity(), 0),
                        action.getEntity().getAnimationPeriod());
                break;

            case QUAKE:
                this.scheduleEvent(action.getEntity(),
                        Functions.createActivityAction(action.getEntity(), action.getWorld(), action.getImageStore()),
                        action.getEntity().getActionPeriod());
                this.scheduleEvent(action.getEntity(), Functions.createAnimationAction(action.getEntity(), QUAKE_ANIMATION_REPEAT_COUNT),
                        action.getEntity().getAnimationPeriod());
                break;

            case VEIN:
                this.scheduleEvent(action.getEntity(),
                        Functions.createActivityAction(action.getEntity(), action.getWorld(), action.getImageStore()),
                        action.getEntity().getActionPeriod());
                break;

            default:
        }
    }

}
