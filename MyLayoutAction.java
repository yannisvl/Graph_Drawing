import java.awt.event.ActionEvent;
import java.time.Duration;

import javax.swing.AbstractAction;

import com.yworks.yfiles.layout.ILayoutAlgorithm;
import com.yworks.yfiles.layout.circular.CircularLayout;
import com.yworks.yfiles.layout.hierarchic.HierarchicLayout;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.view.GraphComponent;

/**
 * The Class MyLayoutAction handles layout actions.
 * @author Dionysis, crisraft
 */
public class MyLayoutAction extends AbstractAction
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The graph component. */
	private GraphComponent graphComponent;
	
	/** The layout name. */
	private String layoutName;


	/**
	 * Gets the standard layout names.
	 *
	 * @return the standard layout names
	 */
	public static String[] getStandardLayoutNames()
	{
		return new String[]{"organic", "circular", "hierarchic", "random-layouter", "inorder", "rectangle-enclosing", "contour", "schnyder"};
	}
	
	/**
	 * Instantiates a new my layout action.
	 *
	 * @param graphComponent the graph component
	 * @param layoutName the layout name
	 */
	public MyLayoutAction(GraphComponent graphComponent,String layoutName) 
	{
		super(layoutName != null?layoutName:"default");
		this.graphComponent = graphComponent;
		if(layoutName!=null)
			this.layoutName = layoutName;
		else
			this.layoutName = "organic";
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) 
	{
		// disable the action/button once the layout starts
		setEnabled(false);
		if(this.layoutName == null)
			setEnabled(true);
		else
		{			
			ILayoutAlgorithm layouter=createLayouter();
			if(layouter==null)
			{
				setEnabled(true);
			}
			else
			{
				this.graphComponent.morphLayout(layouter, Duration.ofMillis(500), 
						// re-enable the action/button after everything has finished
						(source, args) -> setEnabled(true));
			}
		}
	}


	/**
	 * Creates the layouter.
	 *
	 * @return the layout algorithm
	 */
	private ILayoutAlgorithm createLayouter()
	{
		if(this.layoutName.equals("organic"))
		{
			OrganicLayout orgLayout = new OrganicLayout();
			orgLayout.setNodeSizeConsiderationEnabled(true);
			orgLayout.setMinimumNodeDistance(demo.editor.basic.GraphEditorConstants.GRID_SIZE);
			return orgLayout;
		}
		if(this.layoutName.equals("circular"))
		{
			CircularLayout circLayout = new CircularLayout();
			circLayout.setComponentLayoutEnabled(true);
			return circLayout;
		}
		if(this.layoutName.equals("hierarchic"))
		{
		HierarchicLayout hierLayout = new HierarchicLayout();
		hierLayout.setComponentLayoutEnabled(true);
		return hierLayout;
		}
		if(this.layoutName.equals("random-layouter"))
		{
			return new MyRandomLayouter();
		}
		if(this.layoutName.equals("inorder"))
		{
			return new Inorder();
		}
		if(this.layoutName.equals("rectangle-enclosing"))
		{
			return new RectangleEnclosing();
		}
		if(this.layoutName.equals("contour"))
		{
			return new Contour();
		}
		if(this.layoutName.equals("schnyder"))
		{
			return new Schnyder();
		}
		
		return null;
	}
}
