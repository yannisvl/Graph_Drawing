import java.util.ArrayList;
import java.util.List;

public class Forest {
    // 0 for red, 1 for green, 2 for blue
    //this method simply connects source to target with this particular color
    //updating of course the next and previous nodes of the participants
    public void connectNodes(MyNode source, MyNode target, int color){
        if (color==0){
            source.next_red = target;
            if (!target.prevs_red.contains(source)) {
                target.prevs_red.add(source);
            }
        }
        else if (color==1){
            source.next_green = target;
            if (!target.prevs_green.contains(source)) {
                target.prevs_green.add(source);
            }
        }
        else if (color==2){
            source.next_blue = target;
            if (!target.prevs_blue.contains(source)) {
                target.prevs_blue.add(source);
            }
        }
    }

    //this is a preorder traversal starting from the 3 outer nodes, one for each color
    public void calcDesc(MyNode n0, int color){
        if (color==0){
            //if it is the outer node it has one descendant including itself
            if (n0.next_red==null){
                n0.numDescR = 1;
            }
            //else it has one more than its child
            else {
                n0.numDescR = n0.next_red.numDescR+1;
            }
            //now call the same function for its children
            for (MyNode n:n0.prevs_red) {
                calcDesc(n, color);
            }
        }
        else if (color==1){
            if (n0.next_green==null){
                n0.numDescG = 1;
            }
            else {
                n0.numDescG = n0.next_green.numDescG+1;
            }
            for (MyNode n:n0.prevs_green) {
                calcDesc(n, color);
            }
        }
        else if (color==2){
            if (n0.next_blue==null){
                n0.numDescB = 1;
            }
            else {
                n0.numDescB = n0.next_blue.numDescB+1;
            }
            for (MyNode n:n0.prevs_blue) {
                calcDesc(n, color);
            }
        }
    }

    //this is done by postorder and not preorder
    public int calcAnc(MyNode n0, int color){
        int total=0;
        List<MyNode> l = new ArrayList<>();
        if (color==0){
            l = n0.prevs_red;
            //if it has not ancestors then its value is 1 including itself
            if (l.isEmpty()){
                n0.numAncR=1;
            }
            //else sum over all its direct parents
            else{
                for (MyNode n:l){
                    total += calcAnc(n, color);
                }
                n0.numAncR=total+1;
            }
            return n0.numAncR;
        }
        else if (color==1){
            l = n0.prevs_green;
            if (l.isEmpty()){
                n0.numAncG=1;
            }
            else{
                for (MyNode n:l){
                    total += calcAnc(n, color);
                }
                n0.numAncG=total+1;
            }
            return n0.numAncG;
        }
        else if (color==2){
            l = n0.prevs_blue;
            if (l.isEmpty()){
                n0.numAncB=1;
            }
            else{
                for (MyNode n:l){
                    total += calcAnc(n, color);
                }
                n0.numAncB=total+1;
            }
            return n0.numAncB;
        }
        return 0;
    }

    //sums are computed with a simple postorder
    public void computeSums(MyNode n0, int color) {
        if (color == 0) {
            if (n0.next_red == null) {
                n0.sumG_overR = n0.numAncG;
                n0.sumB_overR = n0.numAncB;
            }
            else {
                n0.sumG_overR = n0.next_red.sumG_overR + n0.numAncG;
                n0.sumB_overR = n0.next_red.sumB_overR + n0.numAncB;
            }
            for (MyNode n : n0.prevs_red) {
                computeSums(n, color);
            }
        }
        else if (color == 1) {
            if (n0.next_green == null) {
                n0.sumR_overG = n0.numAncR;
                n0.sumB_overG = n0.numAncB;
            }
            else {
                n0.sumR_overG = n0.next_green.sumR_overG + n0.numAncR;
                n0.sumB_overG = n0.next_green.sumB_overG + n0.numAncB;
            }
            for (MyNode n : n0.prevs_green) {
                computeSums(n, color);
            }
        }
        else if (color == 2) {
            if (n0.next_blue == null) {
                n0.sumR_overB = n0.numAncR;
                n0.sumG_overB = n0.numAncG;
            }
            else {
                n0.sumR_overB = n0.next_blue.sumR_overB + n0.numAncR;
                n0.sumG_overB = n0.next_blue.sumG_overB + n0.numAncG;
            }
            for (MyNode n : n0.prevs_blue) {
                computeSums(n, color);
            }
        }
    }
}