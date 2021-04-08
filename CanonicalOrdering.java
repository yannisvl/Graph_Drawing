//Given an input planar layout graph, we compute a canonical order for its nodes.
//algorithm based on the notes of the course lectures

import java.util.*;

import com.yworks.yfiles.algorithms.*;
import com.yworks.yfiles.layout.LayoutGraph;
import com.yworks.yfiles.layout.MultiStageLayout;
import com.yworks.yfiles.layout.router.StraightLineEdgeRouter;

public class CanonicalOrdering extends MultiStageLayout {
    public CanonicalOrdering() {
        super();
        this.prependStage(new StraightLineEdgeRouter());
    }

    public void applyLayoutCore(LayoutGraph graph) {
        //get list of all nodes in canonical order
        List<Node> canonical = get_ordering(graph);
        //print their indexes
        System.out.println("THIS IS THE ORDER");
        print_ordering(canonical);
    }

    //void function to get the canonical ordering
    public List<Node> get_ordering(LayoutGraph g){
        //List of nodes for output
        List<Node> nodes_in_order=new ArrayList<>();

        //get the embedding of the planar graph
        PlanarEmbedding pe = new PlanarEmbedding(g);

        //get all the faces
        List<List<Dart>> faces = pe.getFaces();

        //get the outer face
        List<Dart> fout = pe.getOuterFace();

        //keep faces containing a particular node or edge
        Map<Node, List<List<Dart>>> Fv = new HashMap<>();
        Map<Edge, List<List<Dart>>> Fe = new HashMap<>();

        //for all faces
        for (List<Dart> f: faces){
            for (Dart d:f){
                //get the dart and then the associated edge
                Edge e = d.getAssociatedEdge();
                //if it is the first time to assign a value to the map, create new list of faces
                //anyway add the face to the Fe because this face contains the edge
                Fe.computeIfAbsent(e, k -> new ArrayList<>()).add(f);
                //with this code we get only a cyclic order of the nodes on the face
                Node n;
                if (d.isReversed()){
                    n = e.target();
                }
                else n = e.source();
                if (f!=fout) {
                    //if it is not the outer face add it to list of the current node and update map
                    //if list doesnt exist, create it
                    Fv.computeIfAbsent(n, k -> new ArrayList<>()).add(f);
                }
            }
        }

        //define the mappings
        //number of separation faces that contain a particular node
        Map<Node, Integer> sep = new HashMap<>();
        //number of edges of a face on the outer face
        Map<List<Dart>, Integer> outE = new HashMap<>();
        //number of nodes of a face on the outer face
        Map<List<Dart>, Integer> outV = new HashMap<>();

        //initialize all sep values to 0
        for (Node n: g.getNodes()){
            sep.put(n, 0);
        }
        //initialize the face mappings to 0
        for (List<Dart> f: faces){
            outE.put(f, 0);
            outV.put(f, 0);
        }
        //find values of outE, outV by adding 1 when finding an item that satisfies property
        // for all darts on outer face
        for (Dart d:fout){
            //get the associated edge
            Edge e = d.getAssociatedEdge();
            //for all faces of these edges, update their map as they have an outer edge
            for (List<Dart> f: Fe.get(e)){
                if (f != fout) {
                    outE.put(f, outE.get(f) + 1);
                }
            }
            //do the same for the nodes
            Node n;
            if (d.isReversed()){
                n = e.target();
            }
            else n = e.source();
            //for all the faces of the outer nodes
            for (List<Dart> f: Fv.get(n)){
                if (f != fout) {
                    //update their node map, as they contain a node on the outer face
                    outV.put(f, outV.get(f) + 1);
                }
            }
        }

        //find number of separation faces containing a node n
        for (Dart d:fout){
            //get the source of this dart
            Edge e = d.getAssociatedEdge();
            Node n;
            if (d.isReversed()){
                n = e.target();
            }
            else n = e.source();
            //for all the faces containing this node,
            for (List<Dart> f: Fv.get(n)){
                if (f != fout) {
                    //if the condition holds
                    if (outV.get(f) == 3 || (outV.get(f) == 2 && outE.get(f) == 0)) {
                        //update their separation map by adding one
                        sep.put(n, sep.get(n) + 1);
                    }
                }
            }
        }

        //get all nodes of outer face and put them on the outer list
        List<Node> outer = new ArrayList<>();
        for (Dart d:fout) {
            Edge e = d.getAssociatedEdge();
            Node n;
            if (d.isReversed()){
                n = e.target();
            }
            else n = e.source();
            outer.add(n);
        }

        //get nodes n1, n2 and remove them from outer list
        Node n1 = outer.get(0);
        Node n2 = outer.get(1);
        outer.remove(n1);
        outer.remove(n2);

        //nodes n1, n2 are the two nodes on the edge at the bottom
        //this is the number of loops left to do. 3 is substracted, as we exclude n1, n2 and the last node
        // which is simply taken as the last node in the order.
        int steps_left = g.getNodes().toList().size()-3;
        //this boolean variable tells us if the node we are willing to add, only has 2 neighbors on the outer face
        //or it was chosen because of the sep_faces = 0 condition
        boolean outdeg2 = false;
        List<Node> nodes_to_add = new ArrayList<>();
        for (int k=0; k<steps_left; k++){
            //vk is the node that is going to be removed
            //wp_1 will be its first neighbor on the outer face on the left
            //and wq_1 will be its last neighbor on the outer face on the right
            //initialize them somehow
            Node vk=n1, wp_1=n1, wq_1=n1;
            //find node from outer face that we can remove
            //iterate through candidates
            for (Node v:outer){
                //get the list of faces it belongs to
                List<List<Dart>> f = Fv.get(v);
                //and the first one of these
                List<Dart> f0 = f.get(0);
                //check if the condition holds (if it can be removed-chosen)
                if (sep.get(v)==0 || (f.size()==1 && outE.get(f0)==2 && outV.get(f0)==3)){
                    //update boolean variable
                    if (f.size()==1 && outE.get(f0)==2 && outV.get(f0)==3){
                        outdeg2=true;
                    }
                    else {outdeg2=false;}
                    vk = v;
                    //found which to remove
                    //we ve got vk, now find its neighbors on the outer path
                    //first of all, get its position on the outer list
                    int pos = outer.indexOf(vk);
                    int last_pos = outer.size()-1;
                    //now for the case that it is the first node on the outer face, then its left neighbor is n2. If
                    // it is the only node, then its neighbors are of course
                    //n1, and n2. Assign their value to wp_1, wq_1
                    // if it the last on the list, its right neighbor is n1.
                    if (pos==0) {
                        wp_1 = n2;
                        if (pos==last_pos){
                            wq_1 = n1;
                        }
                        else {
                            wq_1 = outer.get(pos + 1);
                        }
                    }

                    else if (pos==last_pos) {
                        wp_1 = outer.get(pos-1);
                        wq_1 = n1;
                    }
                    //else just get the one before and the one after
                    else {
                        wp_1 = outer.get(pos-1);
                        wq_1 = outer.get(pos+1);
                    }
                    //ok so far so good
                    //now cyclic next to darts
                    //nodes_to_add is going to be the list with all the nodes between wp_1, wq_1, including them
                    nodes_to_add.removeAll(nodes_to_add);
                    nodes_to_add.add(wp_1);

                    //new out members are all the darts coming out of vk
                    List<Dart> new_out_members = pe.getOutgoingDarts(vk);
                    Dart d_start=null, d=null;
                    //now iterate through them
                    for (Dart dart:new_out_members){
                        Edge e = dart.getAssociatedEdge();
                        Node n;
                        if (dart.isReversed()){
                            n = e.source();
                        }
                        else n = e.target();
                        //if you find wq_1, that is where you should start from
                        if (n==wq_1) d_start=dart;
                    }
                    //now iterate through them in a cyclid order until we find wp_1
                    d = pe.getCyclicNext(d_start);
                    boolean start=false, stop=false;
                    while (d != d_start) {
                        Edge e = d.getAssociatedEdge();
                        Node n;

                        if (d.isReversed()){
                            n = e.source();
                        }
                        else n = e.target();

                        //replace path on the outer graph
                        //if we find wp_1 we leave the while loop
                        if (n==wp_1){
                            break;
                        }
                        //if not we should add that node to the outer face,
                        //so include it to nodes_to_add and outer on the appropriate positions
                        nodes_to_add.add(1, n);
                        outer.add(pos, n);
                        //get next neighbor of vk
                        d = pe.getCyclicNext(d);
                    }

                    //vk has done its job
                    //remove it from the outer face
                    outer.remove(vk);
                    //add it to the canonical order
                    nodes_in_order.add(vk);
                    //if this point is reached we need to break the for loop, in order to get to the next kstep
                    break;
                }
            }
            //add last neighbor on the nodes to add
            nodes_to_add.add(wq_1);

            //remove triangular faces
            //iterate first on the nodes to add
            for (int j=0; j<nodes_to_add.size()-1; j++){
                Node w1=nodes_to_add.get(j);
                Node w2=nodes_to_add.get(j+1);
                //get the outer face edge of 2 successive nodes
                Edge e=w1.getEdge(w2);
                boolean correct_dir = true;
                //check if it is reversed compared to the dart of these 2 nodes, belonging to the face we want to remove!
                if (e.target()==w1) correct_dir=false;
                Dart[] ds  = pe.getDarts(e);
                Dart d;
                //get the correct dart
                //if edge points on the correct direction get the same dart else get the other one
                if (ds[0].isReversed() && !correct_dir || !ds[0].isReversed() && correct_dir){
                    d = ds[0];
                }
                else {
                    d = ds[1];
                }
                Edge tempe = d.getAssociatedEdge();
                //get face
                List<Dart> f = d.getFace();
                //remove it from both node maps
                Fv.get(w1).remove(f);
                Fv.get(w2).remove(f);
            }

            //update outV(f)
            //just add one for the in-between nodes of wp_1 and wq_1
            for (int j=1; j<nodes_to_add.size()-1; j++){
                for (List<Dart> f:Fv.get(nodes_to_add.get(j))) {
                    outV.put(f, outV.get(f) + 1);
                }
            }

            //update outE(f)
            //same thing as the nodes
            for (int j=1; j<nodes_to_add.size(); j++){
                Node w1=nodes_to_add.get(j-1);
                Node w2=nodes_to_add.get(j);
                Edge e = w1.getEdge(w2);
                for (List<Dart> f:Fe.get(e)){
                    outE.put(f, outE.get(f) + 1);
                }
            }

            //update sep(F)
            //ok here, we were check if the condition holds for every face that a certain node belongs to
            //the we update the number on faces satisfying the condition
            for (Node w:nodes_to_add){
                //do it for the node in the path
                int result=0;
                for (List<Dart> f:Fv.get(w)){
                    if (outV.get(f)==3 || (outV.get(f)==2 && outE.get(f)==0)) result++;
                }
                sep.put(w, result);
                //do it for each neighbors
                for (Node n:w.getNeighbors()){
                    result = 0;
                    for (List<Dart> f:Fv.get(n)){
                        if (outV.get(f)==3 || (outV.get(f)==2 && outE.get(f)==0)) result++;
                    }
                    sep.put(n, result);
                }

            }
        }

        //if you've reached this point, there is only one node left
        //it is the last one, add to the ordering
        nodes_in_order.add(outer.get(0));
        //now of course just add n2, n1
        nodes_in_order.add(n2);
        nodes_in_order.add(n1);

        List<Node> reversed_list = new ArrayList<>();
        //remember to reverse the list as we have it in the reversed order
        for (int i = nodes_in_order.size(); i-- > 0; ) {
            reversed_list.add(nodes_in_order.get(i));
        }
        //for some reason Collection.reverse didn't work
        return reversed_list;
    }

    //print indexes in canonical order
    public void print_ordering(List<Node> l){
        for (Node n:l){
            System.out.print(n.index());
            System.out.print(" ");
        }
        System.out.println();
    }
}