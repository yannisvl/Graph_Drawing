/*
Ακολουθούμε τους κανόνες των διαφανειών για να κάνουμε surround τα 2 υποδένδρα ενός κόμβου,
και αναδρομικά τα χωρίζουμε μεταξύ τους ως μη επικαλυπτόμενα ορθογώνια
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.yworks.yfiles.algorithms.INodeCursor;
import com.yworks.yfiles.algorithms.INodeMap;
import com.yworks.yfiles.algorithms.Node;
import com.yworks.yfiles.layout.LayoutGraph;
import com.yworks.yfiles.layout.MultiStageLayout;
import com.yworks.yfiles.layout.router.StraightLineEdgeRouter;
import com.yworks.yfiles.utils.IEnumerable;

public class RectangleEnclosing extends MultiStageLayout {
    //παρομοίως με την inorder κρατάμε το βάθος στο οποίο βρισκόμαστε για
    //την y-συντεταγμένη
    private int bathos=0;

    public RectangleEnclosing() {
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
        //αυτό το mapping είναι από Node σε array 5 τιμών όπως αυτές είναι στις διαφάνειες
        //(width, left_width, rigth_width, απόσταση από αριστερό παιδί, απόσταση από δεξί παιδί)
        Map<Node, int[]> tuples = get_tuples(root, graph, left_child);
        //έχοντας τις tuples αυτές, ζωγραφίζουμε αναδρομικά τον γράφο,
        //με βάση την πληροφορία αυτή
        // το 0 είναι η χ_θέση της ρίζας
        draw_graph(tuples, root, 0, graph, left_child);
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

    //για να βρούμε τις τούπλες όλων των κόμβων, εκτελούμε ένα postorder traversal
    public Map<Node, int[]> get_tuples(Node r, LayoutGraph g, INodeMap left_child){
        Map<Node, int[]> ts = new HashMap<Node, int[]>();
        postorder(ts, r, g, left_child);
        return ts;
    }

    //αναδρομικός υπολογισμών για τις τούπλες με postorder
    //ακολουθούμε 2 κανόνες
    //1. αν έχουμε 2 παιδιά τότε βάζουμε τον πατέρα τους στη μέση και πάνω αφήνοντας κενό
    //ενδιάμεσα από τα παιδιά.
    //2. αν είναι μοναχικό παιδί τότε βάζουμε τον πατέρα κατά 1 θέση αριστερά ή δεξιά,
    //αν το παιδί είναι δεξί ή αριστερό αντίστοιχα
    public int[] postorder(Map<Node, int[]> m, Node n, LayoutGraph g, INodeMap left_child){
        //βρίσκουμε τα παιδιά του κόμβου
        List<Node> children = getChildren(n, g);
        Node l,r;
        //αριθμός παιδιών
        int s = children.size();
        //ορισμός της τούπλας που θα επιστρέψει ο κόμβος
        int arr_out[] = new int[5];
        //αν δεν έχει κανένα παιδί
        if (s==0){
            //τότε ο κόμβος είναι φύλλο και έχει μηδενικές τιμές
            //στις θέσεις 4,5 βάζουμε -1 για να συμβολίσουμε το κενό αφού δεν έχει παιδιά
            int arr[] = {0,0,0,-1,-1};
            m.put(n, arr);
            return arr;
        }
        //αν έχει μόνο ένα παιδί
        else if (s==1){
            //τσεκάρουμε αν αυτό είναι αριστερό ή δεξί
            boolean kidisleft=left_child.getBool(children.get(0));
            boolean kidisright = !kidisleft;
            //αναδρομικά καλούμε την postorder για το παιδί του, και μας επιστρέφεται η τούπλα
            //του παιδιού
            int[] kid = postorder(m, children.get(0), g, left_child);
            //αρχικοποιούμε την τούπλα του κόμβου με του παιδιού και παίρνουμε περιπτώσεις
            int arr[] = {0,0,0,-1,-1};
            arr[0]=kid[0];
            //αν το παιδί είναι αριστερό
            if (kidisleft){
                //αν το υποδένδρο δεν έχει δεξί παίδι τότε το συνολικό width είναι ίδιο με του παιδιού
                //και το αφήνουμε όπως είναι, αλλιώς το αυξάνουμε κατά 1 λόγω του πατέρα.
                if (kid[2]<=0){
                    arr[0]++;
                }
                //αύξησε το αριστεροό width κατά ένα μια και έχουμε +1 κόμβο που μπαίνει πάνω δεξιά
                //από το παιδί
                arr[1]=kid[1]+1;
                //για το δεξιά width, θα είναι αυτό του παιδιού -1
                //προσοχή μην πέσουμε σε αρνητική τιμή
                arr[2]=Math.max(0, kid[2]-1);
                //απόσταση από αριστερό παιδί είναι προφανώς 1
                arr[3]=1;
                //από δεξί παιδί "κενό" γιατί δεν έχει
                arr[4]=-1;
            }
            //αν το παιδί είναι δεξιό κάνουμε τα ίδια αλλά κατα αντιστοιχία δεξιά
            else if (kidisright){
                //αυξανουμε αν χρειάζεται το width του κόμβου κατά 1
                if (kid[1]<=0){
                    arr[0]++;
                }
                //σημειώνουμε τα νεα width δεξιά και αριστερά
                arr[1]=Math.max(0, kid[1]-1);
                arr[2]=kid[2]+1;
                //σημειώνουμε τις αποστάσεις από τα παιδία
                arr[3]=-1;
                arr[4]=1;
            }
            m.put(n, arr);
            return arr;
        }
        //αν έχουμε 2 παιδιά τότε
        else if(s==2){
            //τσέκαρε αρχικά ποιο είναι δεξί ποιο αριστερό
            if (left_child.getBool(children.get(0))==true){
                l = children.get(0);
                r = children.get(1);
            }
            else{
                r = children.get(0);
                l = children.get(1);
            }
            int gap;
            //κάλεσε για το καθένα αναδρομικά την postorder για να βρεις τις 2 τούπλες
            int [] left = postorder(m, l, g, left_child);
            int [] right = postorder(m, r, g, left_child);
            //αρχικοποίησε τη νέα τούπλα
            int arr[] = {0,0,0,-1,-1};
            //η απόσταση τους είναι το δεξιό width του αριστερού παιδιού +
            //το αριστερό width του δεξιού παιδιού
            int dist = left[2]+right[1];
            //αν αυτή η απόσταση είναι αρτιος βάλε κενό 2 ανάμεσα
            if (dist%2==0) gap=2;
            //αλλιώς βάλε 1, ώστε δια 2 να βγαίνει ακέραια απόσταση από κάθε παιδί.
            else gap=1;
            dist += gap;
            dist /= 2;
            //άρα τελικά dist είναι η απόσταση από κάθε παιδί
            //το width θα είναι συνολικά τα 2 widths + το κενό (1 ή 2)
            //αφού τα ορθογώνια είναι μη επικαλυπτόμενα
            arr[0]=left[0]+right[0]+gap;
            //ίσες αποστάσεις από τα 2 παιδιά
            arr[3] = dist;
            arr[4] = dist;
            //προφανώς το αριστερό width είναι το αριστερό width του αριστερόυ παιδιού +
            // την απόσταση από το αριστερό παιδί
            arr[1] = dist + left[1];
            //αντίστοιχα το δεξιό width είναι το δεξιό width του δεξιού παιδιού +
            // την απόσταση από το δεξιό παιδί
            arr[2] = dist + right[2];
            m.put(n,arr);
            return arr;
        }
        return arr_out;
    }

    //η συνάρτηση αυτή ζωγραφίζει τους κόμβους με preorder αναδρομική κλήση
    public void draw_graph(Map<Node, int[]> t, Node n, int x, LayoutGraph g, INodeMap left_child){
        //βρες τα παιδιά του κόμβου
        List<Node> children = getChildren(n, g);
        Node left, right;
        //πλήθος παιδιών
        int s = children.size();
        //πάρε την τούπλα του κόμβου
        int arr[] = t.get(n);
        //αν δεν έχει παιδιά ο κόμβος απλά ζωγράφισε τον
        if (s==0){
            g.setCenter(n, x*50, bathos*50);
        }
        //αν έχει μόνο ένα παιδί κλασικά δες αν είναι δεξί ή αριστερό
        else if (s==1){
            boolean kidisleft=left_child.getBool(children.get(0));
            boolean kidisright=!kidisleft;
            //ζωγράφισε πρώτα τον κόμβο (preorder)
            g.setCenter(n, x*50, bathos*50);
            //και μετά κάλεσε αναδρομικά για το παιδί
            //αυξάνοντας κατά 1 το βάθος γιατί πάμε προς τα κάτω ένα επίπεδο
            //αν είναι δεξιό παιδί ως χ_θέση δίνουμε την χ_θέση του κόμβου + την απόσταση από
            //το δεξί υποδένδρο, αν είναι αριστερό δίνουμε ως χ_θέση την θέση του πατέρα -
            // την απόσταση από τον πατέρα.
            bathos++;
            if (kidisleft){
                draw_graph(t, children.get(0), x-arr[3], g, left_child);
            }
            if (kidisright) {
                draw_graph(t, children.get(0), x+arr[4], g, left_child);
            }
            bathos--;
        }
        //αν έχει 2 παιδιά
        else if (s==2){
            //βρες ποιο είναι ποιο
            if (left_child.getBool(children.get(0))==true){
                left = children.get(0);
                right = children.get(1);
            }
            else{
                right = children.get(0);
                left = children.get(1);
            }
            //ζωγράφισε πατέρα (preorder)
            g.setCenter(n, x*50, bathos*50);
            //κάλεσε αναδρομικά πρώτα για το αριστερό παιδί έπειτα για το δεξί
            //με κατάλληλες χ_θέσεις
            bathos++;
            draw_graph(t, left, x-arr[3], g, left_child);
            draw_graph(t, right, x+arr[4], g, left_child);
            bathos--;
        }
    }
}