//this class gets the canonical order of the maximal planar input graph
//and computes the schnyder layout of the graph based on the lecture notes

import com.yworks.yfiles.algorithms.*;
import com.yworks.yfiles.layout.LayoutGraph;
import com.yworks.yfiles.layout.MultiStageLayout;
import com.yworks.yfiles.layout.router.StraightLineEdgeRouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Schnyder extends MultiStageLayout {

    public Schnyder() {
        super();
        this.prependStage(new StraightLineEdgeRouter());
    }

    @Override
    public void applyLayoutCore(LayoutGraph graph) {
        //first of all, get the ordering
        CanonicalOrdering co = new CanonicalOrdering();
        PlanarEmbedding pe = new PlanarEmbedding(graph);
        List<Node> ordering = new ArrayList<>();
        ordering = co.get_ordering(graph);
        //everything is inside solve method, check it out
        solve(ordering, pe, graph);
    }

    public void solve(List<Node> ordering, PlanarEmbedding pe, LayoutGraph initial){
        //this class (MyNode) is an extended form of Node adaptated on our needs
        //every Node is instance has a MyNode is instance and that is what this map is for
        //to get quickly from the Node instance to the MyNode one.
        Map<Node, MyNode> kombos = new HashMap<>();

        for (Node n:initial.getNodes()){
            MyNode x = new MyNode();
            x.myself = n;
            kombos.put(n, x);
        }

        //the forest structure is going to contain the 3 color trees that the schnyder realizer consists of
        Forest forest = new Forest();
        //of course, get the 3 outer nodes
        Node n1 = ordering.get(0), n2 = ordering.get(1), n_last = ordering.get(ordering.size()-1);

        //remove n1, n2
        ordering.remove(0);
        ordering.remove(0);

        //but add them on the outer face, we are going to need that
        List<Node> outer = new ArrayList<>();
        outer.add(n1); outer.add(n2);

        //we have the canonical order so just iterate through them
        for (Node n:ordering){
            //find first ending and starting node as you traverse neighbors on outer face
            //this looks a lot alike the procedure on the canonical order class
            boolean foundstart=false;
            //first we iterate once on the outer face to get the two neighbors on the outer face that are more left and right
            Node start=null, finish=null;
            for (Node x:outer){
                if (initial.containsEdge(n, x) || initial.containsEdge(x, n)){
                    if (!foundstart){
                        foundstart=true;
                        start = x;
                    }
                    else{
                        finish = x;
                    }
                }
            }
            //next we iterate a second time,
            //we assign first edge blue forest, between edges to red forest and last edge to green

            //colors are these: red-0, green-1, blue-2
            for (Node x:outer){
                //check if there is a connection between n and this x node on the outer face
                if (initial.containsEdge(n, x) || initial.containsEdge(x, n)){
                    if (x==start){
                        //if it is the starting node assign blue color and create a connection between them
                        //exactly like the lecture notes
                        forest.connectNodes(kombos.get(n), kombos.get(x), 2);
                    }
                    else if (x==finish){
                        //in a similar way, do the same for the last node,
                        //there is going to be a green connection
                        forest.connectNodes(kombos.get(n), kombos.get(x), 1);
                    }
                    else {
                        //for the between nodes assign connection with red color
                        forest.connectNodes(kombos.get(x), kombos.get(n), 0);
                    }
                }
            }
            //you have to update the outer face!
            int start_index, end_index;
            start_index = outer.indexOf(start);
            end_index = outer.indexOf(finish);
            //remove all of its neighbors from the outer face
            for (int x = end_index-1; x > start_index; x--) {
                outer.remove(x);
            }
            //and replace them with the node n itself
            outer.add(start_index+1, n);
        }
        //we have all the forests!!!
        //now we have to calculate number of descendants and number of ancestors
        //this is done with ready methods in the Forest class, check them out
        forest.calcDesc(kombos.get(n_last),0);
        forest.calcDesc(kombos.get(n2),1);
        forest.calcDesc(kombos.get(n1), 2);
        forest.calcAnc(kombos.get(n_last), 0);
        forest.calcAnc(kombos.get(n2), 1);
        forest.calcAnc(kombos.get(n1), 2);

        //some detail, assign these values to zero for the upper node, as we don't want to
        //take into account the outer edges
        kombos.get(n_last).numAncG = 0;
        kombos.get(n_last).numAncB = 0;
        //plus, we need to get summing values from each node to each one of the 3 triangle corners
        //over the path that connects them
        forest.computeSums(kombos.get(n_last),0);
        forest.computeSums(kombos.get(n2),1);
        forest.computeSums(kombos.get(n1), 2);

        //compute final coordinates
        //this is the best point! as it implements the weak barycentric representation
        //and more specifically the value of |V(R(v)) - P(v)|

        //that is the number of nodes on the 3 color faces with the previous path of that color excluded
        //this is done by getting the number of ancestors of a particular color as we iterate through
        //the paths that lead to the the other two color nodes
        //Thus we take the number of the internal nodes of this particular face.
        //this is the part mn.sumB_overR + mn.sumB_overG

        //the - mn.numAncB part is subtracted as it is counted twice on the result
        //- mn.numDescG this part is subtracted as they are the nodes on P(v)
        //the +2 part is just a detail caused by the special cases of the outer nodes
        for (MyNode mn:kombos.values()){
            //mn.coordx = mn.sumB_overR + mn.sumB_overG - mn.numAncB - mn.numDescG + 2;
            //mn.coordy = mn.sumG_overR + mn.sumG_overB - mn.numAncG - mn.numDescR + 2;
            //mn.coordz = mn.sumR_overG + mn.sumR_overB - mn.numAncR - mn.numDescB + 2;
            mn.coordx = mn.sumB_overR + mn.sumB_overG - mn.numAncB - mn.numDescG +2;
            mn.coordy = mn.sumG_overR + mn.sumG_overB - mn.numAncG - mn.numDescR +2;
            mn.coordz = mn.sumR_overG + mn.sumR_overB - mn.numAncR - mn.numDescB +2;
        }

        //the outer nodes are special cases
        int number_of_all_nodes = initial.getNodes().toList().size();

        kombos.get(n1).coordx = number_of_all_nodes - 1;
        kombos.get(n1).coordy = 0;

        kombos.get(n2).coordx = 0;
        kombos.get(n2).coordy = number_of_all_nodes - 1;

        kombos.get(n_last).coordx = 0;
        kombos.get(n_last).coordy = 0;

        //finally plot all points
        for (Node n:initial.getNodes()){
            initial.setCenter(n, kombos.get(n).coordx*50, kombos.get(n).coordy*50);
        }
    }
}