
public class AStarPoint {

    private Point p;
    private int h;
    private int g;
    private int f;
    private AStarPoint parent;

    public AStarPoint(Point p, AStarPoint parent, Point end) {
        this.p = p;
        this.parent = parent;
        if (parent == null)
            return;
        this.g = parent.getG() + 1;
        this.h = Math.abs(end.getX()-p.getX()) + Math.abs(end.getY()-p.getY());
        this.f =  g + h;
    }

    public Point getPoint() { return p; }

    public AStarPoint getParent() { return parent; }

    public int getG() { return g; }

    public int getF() { return f; }

//    public boolean equals(Object o) {
//        if (o == this)
//            return true;
//        else if (o == null)
//            return false;
//        else {
//            AStarPoint ap = (AStarPoint) o;
//            return ap.getPoint().equals(p) && ap.getG() == g;
//        }
//    }


}