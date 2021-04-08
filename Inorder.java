/*
Εκτελούμε inorder διάσχιση και κάθε φορά που επισκεπτόμαστε έναν νέο κόμβο τον
τυπώνουμε μία θέση δεξιότερα από ό,τι τον πρηγούμενο, και στο βάθος που του αντιστοιχεί.
 */
import java.util.List;

import com.yworks.yfiles.algorithms.*;
import com.yworks.yfiles.layout.LayoutGraph;
import com.yworks.yfiles.layout.MultiStageLayout;
import com.yworks.yfiles.layout.router.StraightLineEdgeRouter;
import com.yworks.yfiles.utils.IEnumerable;


public class Inorder extends MultiStageLayout {
    //πρόκειται για την σειρά εκτύπωσης των κόμβων
    //ξεκινάμε από 1 και μεγαλώνει κατά 1 καθώς διασχίζουμε το δέντρο.
    private int seira=1;
    //πρόκειται για το βάθος στο οποίο βρίσκεται ο κόμβος που διασχίζουμε
    //αρχικά είναι 0 για το επίπεδο της ρίζας.
    private int bathos=0;

    public Inorder() {
        super();
        this.prependStage(new StraightLineEdgeRouter());
    }

    @Override
    public void applyLayoutCore(LayoutGraph graph) {
        //το mapping αυτό μας λέει αν ένας κόμβος είναι αριστερό παιδί ή όχι (δεξιό παιδί)
        //του πατέρα του. Η ρίζα δεν περιλαμβάνεται καθώς δεν έχει πατέρα.
        //λεπτομέρειες στην αντίστοιχη συνάρτηση
        INodeMap left_child = compare_parent(graph);
        //βρσίκουμε τη ρίζα, λεπτομέρειες στον ορισμό της
        Node root = getRoot(graph);
        //αναδρομικά κάνουμε inorder διάσχιση ξεκινώντας από την ρίζα
        traverse(root, graph, left_child);
    }

    //η ρίζα βρίσκεται ψάχνοντας τον κόμβο που έχει έσω βαθμό ίσο με 0
    public Node getRoot(LayoutGraph graph){
        INodeCursor nc = graph.getNodeCursor();
        Node n = nc.node();
        //επαναληπτικά ψάχνουμε όλους τους κόμβους για την ζητούμενη ιδιότητα
        while (nc.ok()) {
            n = nc.node();
            //εδώ εξετάζουμε τον έσω βαθμό
            if (n.inDegree()==0){
                //αν τον βρούμε βγαίνουμε από την επανάληψη
                break;
            }
            nc.next();
        }
        //επιστρέφουμε τον κόμβο που έχει την ζητούμενη ιδιότητα
        return n;
    }

    //η compare_parent επιστρέφει ένα mapping το οποίο δείχνει αν ένας κόμβος είναι αριστερό
    //παιδί ή όχι. Το mapping αυτό εξαρτάται από τις αρχικές θέσεις των κόμβων εισόδου, όπως
    // αυτές δίνονται στον editor ή είναι στο αρχείο εισόδου. Αν ο κόμβος που εξετάζεται είναι
    // lone child τότε συγκρίνουμε την x-συνιστώσα του με του πατέρα του. Αν είναι πιο δεξιά,
    // τότε είναι δεξιό παιδί, αλλιώς αριστερό. Αν ο κόμβος όμως έχει
    //"αδερφό" τότε συγκρίνουμε αντίστοιχα τις x-συντεταγμένες των αδερφών.
    public INodeMap compare_parent(LayoutGraph graph){
        //αρχικοποίηση χ-συντεταγμένων, k:εν λόγω κόμβος, p: πατέρας, b:αδερφός.
        double k, p, b=0;
        //λογική μεταβλητή για το αν ο κόμβος έχει "αδερφό"
        boolean hasbrother;
        INodeCursor nc = graph.getNodeCursor();
        Node n = nc.node();
        //το mapping που θα επιστρέψουμε
        INodeMap m = graph.createNodeMap();
        //Διασχίζουμε όλον τον γράφο
        while (nc.ok()) {
            //false μέχρι απόδειξης του εναντίου
            hasbrother=false;
            n = nc.node();
            //βρίσκουμε την χ-συντ/γμένη
            k = graph.getCenterX(n);
            //πατέρας του κόμβου
            List<Node> parent = n.getPredecessors().toList();
            //αν έχει πατέρα τοτε δεν είναι ρίζα, μπες στο if
            if (parent.size()==1) {
                Node par = parent.get(0);
                //συντ/μένη πατέρα
                p = graph.getCenterX(par);
                //βρες τα παιδιά του πατέρα
                List<Node> brothers = par.getSuccessors().toList();
                //ψάξε τα (έως 2) παιδιά
                for (Node br: brothers){
                    //βρεις παιδί που να μην είναι ο αρχικός κόμβος που εξετάζεται τότε
                    //αυτός είναι ο αδερφός
                    if (br.index()!=n.index()){
                        //παρε την συντεταγμένη του και τσέκαρε την λογική μεταβλητή
                        b = graph.getCenterX(br);
                        hasbrother=true;
                    }
                }
            }
            //αν δεν έχει πατέρα πήγαινε σε επόμενο κόμβο
            else{
                nc.next();
                continue;
            }
            //αν έχει αδερφό σύγκρινε αυτούς τους 2
            if (hasbrother) {
                if (k >= b) {
                    m.setBool(n, false);
                } else {
                    m.setBool(n, true);
                }
            }
            //αλλιώς είναι μοναχοπαίδι, σύγκρινε με πατέρα.
            else{
                if (k >= p) {
                    m.setBool(n, false);
                } else {
                    m.setBool(n, true);
                }
            }
            nc.next();
        }
        //επίστρεψε το mapping
        return m;
    }

    //συνάρτηση που επιστρέφει σε μία λίστα τα (έως 2) παιδιά ενός κόμβου
    public List<Node> getChildren(Node node, LayoutGraph graph){
        IEnumerable<Node> kids = node.getSuccessors();
        return kids.toList();
    }

    //inorder διάσχιση
    public void traverse(Node node, LayoutGraph graph, INodeMap left_child){
        //βρες τα παιδιά του κόμβου
        List<Node> children = getChildren(node, graph);
        Node left, right;
        //και πόσα είναι. Πάρε περιπτώσεις
        int s = children.size();
        //αν δεν έχει παιδί, τότε είναι φύλλο.
        if (s==0){
            //έχει έρθει η σειρά του, ζωγράφισε το
            graph.setCenter(node, seira*50, bathos*50);
            //αύξησε την σειρά κατά 1, για την χ-συν/γμένη του επόμενου κόμβου
            seira++;
            return;
        }
        //αν έχει ένα παιδί
        else if (s==1){
            //τσέκαρε αν είναι αριστερό ή δεξύ
            boolean isleft = left_child.getBool(children.get(0));
            boolean isright = !isleft;
            //αν είναι δεξύ ζωγράφισε το και αύξησε την σειρά, αλλιώς
            if (isright) {
                graph.setCenter(node, seira*50, bathos*50);
                seira++;
            }
            //συνέχισε το traversal και θα το ζωγραφίσεις μετά
            //αύξησε το βάθος, γιατί διασχίζοντας πας ένα επίπεδο κάτω
            bathos++;
            traverse(children.get(0), graph, left_child);
            //ξανάμείωσε το, γιατί επέστρεψες
            bathos--;
            //αν είναι αριστερό παιδί ζωγράφισε το μετά το traversal του υποδένδρου
            if (isleft){
                graph.setCenter(node, seira*50, bathos*50);
                seira++;
            }
        }
        //αν τώρα έχουμε 2 παιδιά
        else if (s==2){
            //βρες αρχικά ποιο είναι δεξύ, ποιο αριστερό
            if (left_child.getBool(children.get(0))==true){
                left = children.get(0);
                right = children.get(1);
            }
            else{
                right = children.get(0);
                left = children.get(1);
            }
            //κάνε traversal στο αριστερό
            bathos++;
            traverse(left, graph, left_child);
            bathos--;
            //ζωγράφισε τον πατέρα
            graph.setCenter(node, seira*50, bathos*50);
            seira++;
            bathos++;
            //και μετά κάνε traverse αναδρομικά το δεξύ παιδί.
            traverse(right, graph, left_child);
            bathos--;
        }
    }
}