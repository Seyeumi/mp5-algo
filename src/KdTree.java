import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class KdTree<T extends Point> implements Iterable<T> {

    private int k = 3;
    private KdNode root = null;

    protected static final int X_AXIS = 0;
    protected static final int Y_AXIS = 1;
    protected static final int Z_AXIS = 2;

    public KdTree() { }

    public KdTree(List<Point> list) {
        super();
        root = createNode(list, k, 0);
    }

    public KdTree(List<Point> list, int k) {
        super();
        root = createNode(list, k, 0);
    }

    private static KdNode createNode(List<Point> list, int k, int depth) {
        if (list == null || list.size() == 0)
            return null;

        int axis = depth % k;
        if (axis == X_AXIS)
            Collections.sort(list, Point.X_COMPARATOR);
        else if (axis == Y_AXIS)
            Collections.sort(list, Point.Y_COMPARATOR);
        else
            Collections.sort(list, Point.Z_COMPARATOR);

        KdNode node = null;
        List<Point> less = new ArrayList<Point>(list.size());
        List<Point> more = new ArrayList<Point>(list.size());
        if (list.size() > 0) {
            int medianIndex = list.size()/2;
            node = new KdNode(list.get(medianIndex), k, depth);
            //Process list to see where each non-median point lies
            for (int i = 0; i < list.size(); i++) {
                if (i == medianIndex)
                    continue;
                Point p = list.get(i);
                //Cannot assume points before the median are less since they could be equal
                if (KdNode.compareTo(depth, k, p, node.id) <= 0) {
                    less.add(p);
                } else {
                    more.add(p);
                }
            }

            if ((medianIndex-1 >= 0) && less.size() > 0) {
                node.lesser = createNode(less, k, depth + 1);
                node.lesser.parent = node;
            }

            if ((medianIndex <= list.size()-1) && more.size() > 0) {
                node.greater = createNode(more, k, depth + 1);
                node.greater.parent = node;
            }
        }

        return node;
    }

    public boolean add(T value) {
        if (value == null)
            return false;

        if (root == null) {
            root = new KdNode(value);
            return true;
        }

        KdNode node = root;
        while (true) {
            if (KdNode.compareTo(node.depth, node.k, value, node.id) <= 0) {
                //Lesser
                if (node.lesser == null) {
                    KdNode newNode = new KdNode(value, k, node.depth + 1);
                    newNode.parent = node;
                    node.lesser = newNode;
                    break;
                }
                node = node.lesser;
            } else {
                //Greater
                if (node.greater == null) {
                    KdNode newNode = new KdNode(value, k, node.depth + 1);
                    newNode.parent = node;
                    node.greater = newNode;
                    break;
                }
                node = node.greater;
            }
        }

        return true;
    }

    public boolean contains(T value) {
        if (value == null || root == null)
            return false;

        KdNode node = getNode(this, value);
        return (node != null);
    }

    private static final <T extends Point> KdNode getNode(KdTree<T> tree, T value) {
        if (tree == null || tree.root == null || value == null)
            return null;

        KdNode node = tree.root;
        while (true) {
            if (node.id.equals(value)) {
                return node;
            } else if (KdNode.compareTo(node.depth, node.k, value, node.id) <= 0) {
                //Lesser
                if (node.lesser == null) {
                    return null;
                }
                node = node.lesser;
            } else {
                //Greater
                if (node.greater == null) {
                    return null;
                }
                node = node.greater;
            }
        }
    }

    public boolean remove(T value) {
        if (value == null || root == null)
            return false;

        KdNode node = getNode(this, value);
        if (node == null)
            return false;

        KdNode parent = node.parent;
        if (parent != null) {
            if (parent.lesser != null && node.equals(parent.lesser)) {
                List<Point> nodes = getTree(node);
                if (nodes.size() > 0) {
                    parent.lesser = createNode(nodes, node.k, node.depth);
                    if (parent.lesser != null) {
                        parent.lesser.parent = parent;
                    }
                } else {
                    parent.lesser = null;
                }
            } else {
                List<Point> nodes = getTree(node);
                if (nodes.size() > 0) {
                    parent.greater = createNode(nodes, node.k, node.depth);
                    if (parent.greater != null) {
                        parent.greater.parent = parent;
                    }
                } else {
                    parent.greater = null;
                }
            }
        } else {
            //root
            List<Point> nodes = getTree(node);
            if (nodes.size() > 0)
                root = createNode(nodes, node.k, node.depth);
            else
                root = null;
        }

        return true;
    }

    private static final List<Point> getTree(KdNode root) {
        List<Point> list = new ArrayList<Point>();
        if (root == null)
            return list;

        if (root.lesser != null) {
            list.add(root.lesser.id);
            list.addAll(getTree(root.lesser));
        }
        if (root.greater != null) {
            list.add(root.greater.id);
            list.addAll(getTree(root.greater));
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    public Collection<T> nearestNeighbourSearch(int K, T value) {
        if (value == null || root == null)
            return Collections.EMPTY_LIST;

        //Map used for results
        TreeSet<KdNode> results = new TreeSet<KdNode>(new distanceComparator(value));

        //Find the closest leaf node
        KdNode prev = null;
        KdNode node = root;
        while (node != null) {
            if (KdNode.compareTo(node.depth, node.k, value, node.id) <= 0) {
                //Lesser
                prev = node;
                node = node.lesser;
            } else {
                //Greater
                prev = node;
                node = node.greater;
            }
        }
        KdNode leaf = prev;

        if (leaf != null) {

            //Save which nodes we have looked at already
            Set<KdNode> examined = new HashSet<KdNode>();

            node = leaf;
            while (node != null) {
                //Search node
                searchNode(value, node, K, results, examined);
                node = node.parent;
            }
        }

        //Load up the collection of the results
        Collection<T> collection = new ArrayList<T>(K);
        for (KdNode kdNode : results)
            collection.add((T) kdNode.id);
        return collection;
    }

    private static final <T extends Point> void searchNode(T value, KdNode node, int K, TreeSet<KdNode> results, Set<KdNode> examined) {
        examined.add(node);

        //Search node
        KdNode lastNode = null;
        Double lastDistance = Double.MAX_VALUE;
        if (results.size() > 0) {
            lastNode = results.last();
            lastDistance = lastNode.id.calcDistance(value);
        }
        Double nodeDistance = node.id.calcDistance(value);
        node.id.distance = nodeDistance;
        if (nodeDistance.compareTo(lastDistance) < 0) {
            //If Last distance is greater than node distance
            if (results.size() == K && lastNode != null) {
                System.out.println("yes");
                results.remove(lastNode);
            }
            results.add(node);
        } else if (nodeDistance.equals(lastDistance)) {
            results.add(node);
        } else if (results.size() < K) {
            results.add(node);
        }
        lastNode = results.last();
        lastDistance = lastNode.id.calcDistance(value);

        int axis = node.depth % node.k;
        KdNode lesser = node.lesser;
        KdNode greater = node.greater;

        //Search children branches, if axis aligned distance is less than
        //current distance
        if (lesser != null && !examined.contains(lesser)) {
            examined.add(lesser);

            double nodePoint = Double.MIN_VALUE;
            double valuePlusDistance = Double.MIN_VALUE;
            if (axis == X_AXIS) {
                nodePoint = node.id.x;
                valuePlusDistance = value.x - lastDistance;
            } else if (axis == Y_AXIS) {
                nodePoint = node.id.y;
                valuePlusDistance = value.y - lastDistance;
            } else {
                nodePoint = node.id.z;
                valuePlusDistance = value.z - lastDistance;
            }
            boolean lineIntersectsCube = ((valuePlusDistance <= nodePoint) ? true : false);

            //Continue down lesser branch
            if (lineIntersectsCube)
                searchNode(value, lesser, K, results, examined);
        }
        if (greater != null && !examined.contains(greater)) {
            examined.add(greater);

            double nodePoint = Double.MIN_VALUE;
            double valuePlusDistance = Double.MIN_VALUE;
            if (axis == X_AXIS) {
                nodePoint = node.id.x;
                valuePlusDistance = value.x + lastDistance;
            } else if (axis == Y_AXIS) {
                nodePoint = node.id.y;
                valuePlusDistance = value.y + lastDistance;
            } else {
                nodePoint = node.id.z;
                valuePlusDistance = value.z + lastDistance;
            }
            boolean lineIntersectsCube = ((valuePlusDistance >= nodePoint) ? true : false);

            //Continue down greater branch
            if (lineIntersectsCube)
                searchNode(value, greater, K, results, examined);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Point> void search(final KdNode node, final Deque<T> results) {
        if (node != null) {
            results.add((T) node.id);
            search(node.greater, results);
            search(node.lesser, results);
        }
    }

    @Override
    public String toString() {
        return TreePrinter.getString(this);
    }

    protected static class distanceComparator implements Comparator<KdNode> {

        private final Point point;

        public distanceComparator(Point point) {
            this.point = point;
        }

        @Override
        public int compare(KdNode o1, KdNode o2) {
            Double d1 = point.calcDistance(o1.id);
            Double d2 = point.calcDistance(o2.id);
            if (d1.compareTo(d2) < 0)
                return -1;
            else if (d2.compareTo(d1) < 0)
                return 1;
            return o1.id.compareTo(o2.id);
        }
    }

    public Iterator<T> iterator() {
        final Deque<T> results = new ArrayDeque<T>();
        search(root, results);
        return results.iterator();
    }

    public Iterator<T> reverse_iterator() {
        final Deque<T> results = new ArrayDeque<T>();
        search(root, results);
        return results.descendingIterator();
    }

    public static class KdNode implements Comparable<KdNode> {

        private final Point id;
        private final int k;
        private final int depth;

        private KdNode parent = null;
        private KdNode lesser = null;
        private KdNode greater = null;

        public KdNode(Point id) {
            this.id = id;
            this.k = 3;
            this.depth = 0;
        }

        public KdNode(Point id, int k, int depth) {
            this.id = id;
            this.k = k;
            this.depth = depth;
        }

        public static int compareTo(int depth, int k, Point o1, Point o2) {
            int axis = depth % k;
            if (axis == X_AXIS)
                return Point.X_COMPARATOR.compare(o1, o2);
            if (axis == Y_AXIS)
                return Point.Y_COMPARATOR.compare(o1, o2);
            return Point.Z_COMPARATOR.compare(o1, o2);
        }

        @Override
        public int hashCode() {
            return 31 * (this.k + this.depth + this.id.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof KdNode))
                return false;

            KdNode kdNode = (KdNode) obj;
            if (this.compareTo(kdNode) == 0)
                return true;
            return false;
        }

        @Override
        public int compareTo(KdNode o) {
            return compareTo(depth, k, this.id, o.id);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("k=").append(k);
            builder.append(" depth=").append(depth);
            builder.append(" id=").append(id.toString());
            return builder.toString();
        }
    }

    protected static class TreePrinter {

        public static <T extends Point> String getString(KdTree<T> tree) {
            if (tree.root == null)
                return "Tree has no nodes.";
            return getString(tree.root, "", true);
        }

        private static String getString(KdNode node, String prefix, boolean isTail) {
            StringBuilder builder = new StringBuilder();

            if (node.parent != null) {
                String side = "left";
                if (node.parent.greater != null && node.id.equals(node.parent.greater.id))
                    side = "right";
                builder.append(prefix + (isTail ? "└── " : "├── ") + "[" + side + "] " + "depth=" + node.depth + " id="
                        + node.id + "\n");
            } else {
                builder.append(prefix + (isTail ? "└── " : "├── ") + "depth=" + node.depth + " id=" + node.id + "\n");
            }
            List<KdNode> children = null;
            if (node.lesser != null || node.greater != null) {
                children = new ArrayList<KdNode>(2);
                if (node.lesser != null)
                    children.add(node.lesser);
                if (node.greater != null)
                    children.add(node.greater);
            }
            if (children != null) {
                for (int i = 0; i < children.size() - 1; i++) {
                    builder.append(getString(children.get(i), prefix + (isTail ? "    " : "│   "), false));
                }
                if (children.size() >= 1) {
                    builder.append(getString(children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "),
                            true));
                }
            }

            return builder.toString();
        }
    }
}