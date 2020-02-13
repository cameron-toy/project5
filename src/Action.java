
public interface ActionInterface {

    //public Entity getEntity();

    public WorldModel getWorld();

    public ImageStore getImageStore();

    public void executeAction(EventScheduler scheduler);
}