import java.util.Comparator;

public class Point implements Comparable<Point> {

    protected final double x;
    protected final double y;
    protected final double z;
    public double distance;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }

    public double getD() {return distance;}
    public void setD(double distance) {this.distance = distance;}

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Point))
            return false;

        Point xyzPoint = (Point) obj;
        if (Double.compare(this.x, xyzPoint.x)!=0)
            return false;
        if (Double.compare(this.y, xyzPoint.y)!=0)
            return false;
        if (Double.compare(this.z, xyzPoint.z)!=0)
            return false;
        return true;
    }

    @Override
    public int compareTo(Point o) {
        int xComp = X_COMPARATOR.compare(this, o);
        if (xComp != 0)
            return xComp;
        int yComp = Y_COMPARATOR.compare(this, o);
        if (yComp != 0)
            return yComp;
        int zComp = Z_COMPARATOR.compare(this, o);
        return zComp;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(x).append(", ");
        builder.append(y).append(", ");
        builder.append(z);
        builder.append(")");
        return builder.toString();
    }

    public String toString2() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(x).append(", ");
        builder.append(y).append(", ");
        builder.append(z).append(", ");
        builder.append(distance);
        builder.append(")");
        return builder.toString();
    }


    public static final Comparator<Point> X_COMPARATOR = new Comparator<Point>() {

        @Override
        public int compare(Point o1, Point o2) {
            if (o1.x < o2.x)
                return -1;
            if (o1.x > o2.x)
                return 1;
            return 0;
        }
    };

    public static final Comparator<Point> Y_COMPARATOR = new Comparator<Point>() {

        @Override
        public int compare(Point o1, Point o2) {
            if (o1.y < o2.y)
                return -1;
            if (o1.y > o2.y)
                return 1;
            return 0;
        }
    };

    public static final Comparator<Point> Z_COMPARATOR = new Comparator<Point>() {

        @Override
        public int compare(Point o1, Point o2) {
            if (o1.z < o2.z)
                return -1;
            if (o1.z > o2.z)
                return 1;
            return 0;
        }
    };
    public double calcDistance(Point o1) {
        return calculateDistance(o1, this);
    }

    private static final double calculateDistance(Point o1, Point o2) {
        return Math.sqrt(Math.pow((o1.x - o2.x), 2) + Math.pow((o1.y - o2.y), 2) + Math.pow((o1.z - o2.z), 2));
    }
}