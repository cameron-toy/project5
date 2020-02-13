import java.util.*;

public final class EventScheduler
{
    private PriorityQueue<Event> eventQueue;
    private Map<Entity, List<Event>> pendingEvents;
    private double timeScale;

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

//    public void scheduleActions(
//            Action action
//    )
//    {
//        ActivityEntity acE = (action.getEntity() instanceof ActivityEntity) ? (ActivityEntity) action.getEntity() : null;
//        AnimationEntity anE = (action.getEntity() instanceof AnimationEntity) ? (AnimationEntity) action.getEntity() : null;
//        switch (action.getEntity().getClass()) {
//            case MinerFull.class:
//                this.scheduleEvent(action.getEntity(),
//                        Factory.createActivityAction(acE, action.getWorld(), action.getImageStore()), acE.getActionPeriod());
//                this.scheduleEvent(action.getEntity(),
//                        Factory.createAnimationAction(anE,0), anE.getAnimationPeriod());
//                break;
//
//            case MinerNotFull.class:
//                this.scheduleEvent(action.getEntity(),
//                        Factory.createActivityAction(acE, action.getWorld(), action.getImageStore()), acE.getActionPeriod());
//                this.scheduleEvent(action.getEntity(),
//                        Factory.createAnimationAction(anE,0), anE.getAnimationPeriod());
//                break;
//
//            case Ore.class:
//                this.scheduleEvent(action.getEntity(),
//                        Factory.createActivityAction(acE, action.getWorld(), action.getImageStore()), acE.getActionPeriod());
//                break;
//
//            case OreBlob.class:
//                this.scheduleEvent(action.getEntity(),
//                        Factory.createActivityAction(acE, action.getWorld(), action.getImageStore()), acE.getActionPeriod());
//                this.scheduleEvent(action.getEntity(),
//                        Factory.createAnimationAction(anE, 0), anE.getAnimationPeriod());
//                break;
//
//            case Quake.class:
//                this.scheduleEvent(action.getEntity(),
//                        Factory.createActivityAction(acE, action.getWorld(), action.getImageStore()), acE.getActionPeriod());
//                this.scheduleEvent(action.getEntity(), Factory.createAnimationAction(anE, QUAKE_ANIMATION_REPEAT_COUNT), anE.getAnimationPeriod());
//                break;
//
//            case Vein.class:
//                this.scheduleEvent(action.getEntity(),
//                        Factory.createActivityAction(acE, action.getWorld(), action.getImageStore()), acE.getActionPeriod());
//                break;
//
//            default:
//        }
//    }

}
