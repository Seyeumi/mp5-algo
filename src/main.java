import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class main {
    public static void main(String[] args) {
        Point myPoints = new Point(1,5,3);
        Point myPoints1 = new Point(2,5,6);
        Point myPoints2 = new Point(4,4,3);
        Point lefybaby = new Point(2,4,3);
        Point rightbaby = new Point(589,4,5);
        Point leftleftbaby = new Point(589,400,55);


        List<Point> myList = new ArrayList<Point>(){{
            add(myPoints);
            add(myPoints1);
            add(myPoints2);
            add(lefybaby);
            add(rightbaby);
            add(leftleftbaby);
        }};
        KdTree myTree = new KdTree(myList);

        KdTree.TreePrinter myTreePrinter = new KdTree.TreePrinter();

        System.out.println(myTree.contains(new Point(2,4,3)));
        //K has to be 1 +
        Collection<Point> myNeighbours = myTree.nearestNeighbourSearch(0,new Point(3,1,4));

        for(Point p : myNeighbours){
            System.out.println("x: "+p.x +", "+ "y: "+p.y+ ", "+"z: "+p.z+", "+"distance: "+p.distance);
        }


        System.out.println(myTreePrinter.getString(myTree));
    }
}
