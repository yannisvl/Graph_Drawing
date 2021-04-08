import java.util.Random;

import com.yworks.yfiles.algorithms.INodeCursor;
import com.yworks.yfiles.algorithms.Node;
import com.yworks.yfiles.layout.LayoutGraph;
import com.yworks.yfiles.layout.MultiStageLayout;
import com.yworks.yfiles.layout.router.StraightLineEdgeRouter;

/**
 * The Class MyRandomLayouter assigns random coordinates to all nodes.
 * 
 * @author Dionysis, crisraft
 */
public class MyRandomLayouter extends MultiStageLayout {

	/**
	 * Instantiates a new random layouter.
	 * Edges are drawn as straight lines.
	 */
	public MyRandomLayouter() {
		super();
		this.prependStage(new StraightLineEdgeRouter());
	}

	/** The random generator. */
	private static Random RAN_GEN = new Random(0);

	/* (non-Javadoc)
	 * @see com.yworks.yfiles.layout.MultiStageLayout#applyLayoutCore(com.yworks.yfiles.layout.LayoutGraph)
	 */
	@Override
	public void applyLayoutCore(LayoutGraph graph) {
		INodeCursor nc = graph.getNodeCursor();
		while (nc.ok()) {
			Node n = nc.node();
			graph.setCenter(n, RAN_GEN.nextInt(700), RAN_GEN.nextInt(500));
			nc.next();
		}
	}

}
