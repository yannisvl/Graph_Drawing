/*
Για κάθε υποδένδρο βρίσκουμε αναλυτικά τα δεξιά και αριστερά contour.
Όταν είναι να ενώσουμε 2 subtrees στον κοινό τους πατέρα, τα φέρνουμε όσο πιο κοντά γίνεται,
ώστε να μην ακουμπήσουν και κάνουμε update όλα τους κόμβους των υποδένδρων ως προς την
θέση τους
 */

import java.util.*;

import com.yworks.yfiles.algorithms.INodeCursor;
import com.yworks.yfiles.algorithms.INodeMap;
import com.yworks.yfiles.algorithms.Node;
import com.yworks.yfiles.layout.LayoutGraph;
import com.yworks.yfiles.layout.MultiStageLayout;
import com.yworks.yfiles.layout.router.StraightLineEdgeRouter;
import com.yworks.yfiles.utils.IEnumerable;

public class Contour extends MultiStageLayout {
    //το βάθος του κόμβου που εξετάζεται (y_συντεταγμένη)
    private int bathos=0;

    public Contour() {
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
        //αυτό το mapping έχει για κάθε κόμβο την απόστασή του από τον y'y άξονα οπότε με βάση
        //αυτόν θα ζωγραφίσουμε τους κόμβους
        INodeMap dists = graph.createNodeMap();
        //υπολογισμός των contours και ταυτόχρονα εύρεση του παραπάνω mapping
        List<List<Node>> left_and_right_contours = eval_contours(root, graph, dists, left_child);
        //ζωγράφισε κόμβους
        draw_nodes(root, graph, dists, left_child);
    }

    //επιστρέφει μία λίστα από 2 λίστες.
    //η 1η λίστα είναι το left contour ενώ η 2η το right contour
    //οι λίστες ξεκινούν από κάτω προς τα πάνω, δηλαδή από το φύλλο προς τη ρίζα κατά ύψος.
    public List<List<Node>> eval_contours(Node n, LayoutGraph g, INodeMap ds, INodeMap lc){
        //αρχικοποιούμε τη λίστα
        List<List<Node>> pair = new ArrayList<List<Node>>();
        //βρίσκουμε τα παιδιά του κόμβου
        List<Node> children = getChildren(n);
        Node l, r;
        //πλθος παιδιών
        int s = children.size();
        //παίρνουμε περιπτώσεις βάσει του πλήθους
        if (s==0){
            //αν δεν έχει παιδιά είναι φύλλο και
            // απλά το κάθε contour θα περιέχει μόνο τον ίδιο τον κόμβο
            List<Node> left = new ArrayList<Node>();
            List<Node> right = new ArrayList<Node>();
            left.add(n);
            right.add(n);
            pair.add(left);
            pair.add(right);
            //ως απόσταση βάζουμε 0 αρχικά
            ds.setDouble(n, (double) 0);
            return pair;
        }
        //αν έχει ένα μόνο παιδί
        else if (s==1){
            //βρες αναδρομικά τα contour για το παιδί
            List<List<Node>> con = eval_contours(children.get(0), g, ds, lc);
            //και απλά πρόσθεσε σε αυτά τα contour τον εν λόγω κόμβο
            con.get(0).add(n);
            con.get(1).add(n);
            //βάλε απόσταση 0, γιατί ουσιαστικά είναι η ρίζα του υποδένδρου που εξετάζεται
            ds.setDouble(n,(double) 0);
            //κάνε update όλα τα dists του υποδένδρου κατά 1, δεξιά ή αριστερά
            //δες up1 για λεπτομέρειες στο update
            up1(n, 2, ds, lc);
            return con;
        }
        //αν έχεις 2 παιδιά
        else if(s==2){
            //δες ποιο είναι ποιο
            if (lc.getBool(children.get(0))==true){
                l = children.get(0);
                r = children.get(1);
            }
            else{
                r = children.get(0);
                l = children.get(1);
            }
            //αναδρομικά υπολόγισε πάλι τα contour για αριστερό και δεξί παιδί (postorder)
            List<List<Node>> conl = eval_contours(l, g, ds, lc);
            List<Node> ll = conl.get(0);
            List<Node> lr = conl.get(1);
            List<List<Node>> conr = eval_contours(r, g, ds, lc);
            List<Node> rl = conr.get(0);
            List<Node> rr = conr.get(1);
            //εδώ είναι το σημαντικό μέρος που συνενώνονται τα υποδένδρα
            //αρχικά βρίσκουμε την απόσταση μέσω του δεξιού contour του αριστερού υποδένδρου και
            //του αριστερού contour του δεξιού υποδένδρου
            double between = distance(lr, rl, ds);
            //βάλε στον κόμβο ρίζα (μέχρι τώρα) την απόσταση 0
            ds.setDouble(n,(double) 0);
            //και κάνε update όλους τους κόμβους του υποδένδρου
            up1(n, between, ds, lc);
            //Απο δω και κάτω κάνουμε update τα contour
            //ύψη υποδένδρων
            int s1 = ll.size(), s2 = rr.size();
            //περιπτώσεις
            //αν έχουν ίδιο ύψος τα υποδένδρα τότε απλά επίστρεψε το αριστερό contour του αριστερού
            //υποδένδρου και το δεξιό contour του δεξιού υποδένδρου
            if (s1==s2){
                pair.add(ll);
                pair.add(rr);
            }
            //αν είναι το αριστερό πιο ψηλό
            else if (s1>s2){
                //πάρε το αριστερό contour του αριστερού subtree όπως είναι
                pair.add(ll);
                //για το δεξύ, πάρε από το δεξύ contour του αριστερού υποδένδρου τους κόμβους μέχρι
                //να φτάσεις τη βάση του δεξιού υποδένδρου και μετά πάρε το δεξύ contour του δεξιού
                //υποδένδρου
                for (int i=s1-s2-1; i>=0; i--){
                    //ουσιαστικά εδώ παίρνουμε το δεξύ δεξύ contour και βάζουμε στις πρώτες θέσεις
                    //τους υπόλοιπου κόμβους από το δεξύ contour του αριστερού υποδένδρου
                    rr.add(0, lr.get(i));
                }
                pair.add(rr);
            }
            else{
                //αλλιώς τα ίδια αλλά με το δεξύ subtree πιο ψηλό
                //για το αριστερό, πάρε από το αριστερό contour του δεξιού υποδένδρου τους κόμβους μέχρι
                //να φτάσεις τη βάση του αριστερού υποδένδρου και μετά πάρε το αριστερό contour του αριστερού
                //υποδένδρου
                for (int i=s2-s1-1; i>=0; i--){
                    //ουσιαστικά εδώ παίρνουμε το αριστερό αριστερό contour και βάζουμε στις πρώτες θέσεις
                    //τους υπόλοιπου κόμβους από το αριστερό contour του δεξιού υποδένδρου
                    ll.add(0, rl.get(i));
                }
                pair.add(ll);
                //πάρε το δεξί contour του δεξιού subtree όπως είναι
                pair.add(rr);
            }
            ll.add(n);
            rr.add(n);
            return pair;
        }
        return pair;
    }

    //με αυτή την συνάρτηση υπολογίζουμε την απόσταση μεταξύ 2 υποδένδρων.
    public double distance(List<Node> l1, List<Node> l2, INodeMap ds){
        double trees_distance, horizontal, maxdist=0;
        Node ui, vi;
        int s1 = l1.size(), s2 = l2.size();
        //ουσιασικά για τους κόμβους που έχουν το ίδιο βάθος
        //παίρνουμε τα αντίστοιχα ζεύγη για το δεξιό contour του αριστερού υποδένδρου l1
        //και του αριστερού contour για το δεξιό υποδένδρο l1.
        //αυτοί οι κόμβοι έχουν τις μέχρι τώρα σχετικές αποστάσεις από τις ρίζες των υποδένδρων
        //έτσι πχ ένας κόμβος της l1 μπορεί να έχει απόσταση +4 από τη ρίζα του και ένας κόμβος της
        //l2 απόσταση -3 από την ρίζα της. Αυτοί οι 2 θα έχουν συνολικά απόσταση 4-(-3)=7
        for (int i=0; i<Math.min(s1,s2); i++){
            ui = l1.get(s1-i-1);
            vi = l2.get(s2-i-1);
            horizontal = ds.getDouble(ui) - ds.getDouble(vi);
            //Με βάση όλα τα ζεύγη αποστάσεων βρσίκουμε το μεγαλύτερο
            if (horizontal > maxdist) maxdist=horizontal;
        }
        if (maxdist%2==0){
            trees_distance=2;
        }
        else{
            trees_distance=1;
        }
        //αν είναι περιττό πρόσθεσε 1 αλλιώς 2
        //αυτό ώστε όταν κάνουμε διά 2 και μετά υπολογίσουμε τις αποστάσεις απο κάθε παιδί
        //να βγαίνει ακέραιος.
        return trees_distance+maxdist;
    }

    //εδώ κάνουμε update όλα τα dists των κόμβων ώστε να υπάρχει απόσταση d
    //ο κόμβος ρίζα δεν θέλουμε να μετατοπιστεί, γιατί θέλουμε να μείνει με απόσταση 0
    //προς το παρόν. Για αυτό καλούμε αναδρομικά την up2
    public void up1(Node n, double d, INodeMap ds, INodeMap lc){
        //άρα η μετατόπιση θα είναι d/2 ώστε ο πατέρας των παιδιών να είναι από πάνω και στη μέση
        double metatopish = d/2;
        //βρες τα παιδιά
        List<Node> children = getChildren(n);
        Node l, r;
        //πλήθος παιδιών
        int s = children.size();
        //περιπτώσεις
        if (s == 0) {
            //αν είσαι φύλλο μην κανεις τιποτα
            return;
        }
        //αν εχεις ένα παιδί, τσέκαρε αν είναι δεξιό ή αριστερό
        else if (s == 1) {
            boolean kidisleft = lc.getBool(children.get(0));
            boolean kidisright = !kidisleft;
            Node m = children.get(0);
            //αν είναι αριστερό μετατόπισε το κατά αριστερά
            if (kidisleft){
                up2(m, metatopish, ds, false, lc);
            }
            //αλλιώς κατά δεξιά μετατόπιση
            if (kidisright){
                up2(m, metatopish, ds, true, lc);
            }
        }
        //αν έχεις 2 παιδιά , βρες ποιο είναι ποιο
        else if (s == 2) {
            if (lc.getBool(children.get(0))==true){
                l = children.get(0);
                r = children.get(1);
            }
            else{
                r = children.get(0);
                l = children.get(1);
            }
            //και απλά όλο το αριστερό υποδένδρο μετατόπιση αριστερα
            up2(l, metatopish, ds, false, lc);
            //ενώ το δεξύ, όλο μετατόπιση δεξιά κατά metatopish
            up2(r, metatopish, ds, true, lc);
        }
    }

    //η up2 είναι βοηθητική και έχει την bool add η οποία μας λεει αν πάμε δεξιά
    //και πρεπει να προσθέσουμε ή πάμε αριστερά οπότε αφαιρούμε
    public void up2(Node n, double d, INodeMap ds, boolean add, INodeMap lc){
        //έτσι για τα υποδένδρα κάνουμε απλά μια postorder
        List<Node> children = getChildren(n);
        Node l, r;
        int s = children.size();
        if (s == 0) {
            //αν δεν έχει παιδιά κάνε update τον εαυτό του
            up3(n, d, ds, add);
        } else if (s == 1) {
            //αν έχει ένα παιδί κάνε update, μετά αναδρομική κλήση
            Node m = children.get(0);
            up3(n, d, ds, add);
            up2(m, d, ds, add, lc);
        } else if (s == 2) {
            //αν έχεις 2 παιδιά, βρες ποιο είναι ποιο και μετά
            if (lc.getBool(children.get(0))==true){
                l = children.get(0);
                r = children.get(1);
            }
            else{
                r = children.get(0);
                l = children.get(1);
            }
            //κάνε αναδρομική κλήση για το ένα
            up2(l, d, ds, add, lc);
            //update τον εαυτό
            up3(n, d, ds, add);
            //αναδρομική κλήση για το άλλο
            up2(r, d, ds, add, lc);
        }
    }

    //η up3 αντικαθιστά την τιμή απόστασης στο Map dists ανάλογα με το
    //αν μετακινήθηκε δεξιά ή αριστερά
    public void up3(Node n, double m, INodeMap ds, boolean add){
        double old = ds.getDouble(n);
        if (add) {
            ds.setDouble(n, old+m);
        }
        else {
            ds.setDouble(n, old-m);
        }
    }

    //τέλος έχουμε όλες τις πληροφορίες που χρειαζόμαστε
    //έχουμε τις x-συντεταγμένες στο ds και το βάθος απλά προσαφαιρούμε προσεκτικά
    //ανάλογα με το inorder που κάνουμε
    public void draw_nodes(Node node, LayoutGraph graph, INodeMap ds, INodeMap lc) {
        List<Node> children = getChildren(node);
        Node left, right;
        int s = children.size();
        if (s == 0) {
            //βρίσκουμε χ-συντεταγμένη από το ds.
            graph.setCenter(node, ds.getDouble(node) * 50, bathos * 50);
            return;
        } else if (s == 1) {
            boolean isleft = lc.getBool(children.get(0));
            boolean isright = !isleft;
            if (isright) {
                graph.setCenter(node, ds.getDouble(node) * 50, bathos * 50);
            }
            bathos++;
            draw_nodes(children.get(0), graph, ds, lc);
            bathos--;
            if (isleft) {
                graph.setCenter(node, ds.getDouble(node) * 50, bathos * 50);
            }
        } else if (s == 2) {
            if (lc.getBool(children.get(0))==true){
                left = children.get(0);
                right = children.get(1);
            }
            else{
                right = children.get(0);
                left = children.get(1);
            }
            bathos++;
            draw_nodes(left, graph, ds, lc);
            bathos--;
            graph.setCenter(node, ds.getDouble(node) * 50, bathos * 50);
            bathos++;
            draw_nodes(right, graph, ds, lc);
            bathos--;
        }
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
    public List<Node> getChildren(Node node){
        IEnumerable<Node> kids = node.getSuccessors();
        return kids.toList();
    }
}