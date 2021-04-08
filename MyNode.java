import com.yworks.yfiles.algorithms.Node;

import java.util.ArrayList;
import java.util.List;

//names are clear themselves, but lets take a look
public class MyNode {
    //this is the node corresponding to this MyNode instance
    public Node myself = null;

    //this variable is the SINGLE node that is next on the tree
    public MyNode next_red = null, next_green = null, next_blue = null;

    //we also keep the previous ones
    public List<MyNode> prevs_red=new ArrayList<>(), prevs_green=new ArrayList<>(), prevs_blue=new ArrayList<>();

    //now some numerical values in order to compute coordinates
    //these are the nubmer of descendants on each path
    public int numDescR=0, numDescG=0, numDescB=0;

    //same for number of ancestors
    public int numAncR=0, numAncG=0, numAncB=0;

    //these are the sums of ancestors of a particular color but
    //, calculated over a path of another color
    //these values are calculates in the forest class
    public int sumR_overG, sumR_overB, sumG_overR, sumG_overB, sumB_overR, sumB_overG;

    //and of course the coordinates
    public int coordx, coordy, coordz;
}
