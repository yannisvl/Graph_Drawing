import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.styles.ShapeNodeShape;
import com.yworks.yfiles.graph.styles.ShapeNodeStyle;
import com.yworks.yfiles.graphml.GraphMLIOHandler;
import com.yworks.yfiles.view.*;
import com.yworks.yfiles.view.input.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Graph-drawing course: editor for user-interaction with y-files.
 * 
 * @author Dionysis, crisraft
 * 
 */
public class BasicGraphEditor extends JFrame {
	private static final long serialVersionUID = 1L;

	private GraphComponent graphComponent;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> new BasicGraphEditor());
	}

	/**
	 * Build and populate the the frame of the Graph Editor
	 */
	public BasicGraphEditor() {
		super("Graph Drawing Course Project");
		// setup of graph component
		this.initializeGraphComponent();
		// setup of frame
		super.setSize(1300, 700);
		super.setLocationRelativeTo(null);
		super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		super.add(this.graphComponent, BorderLayout.CENTER);
		super.add(createToolBar(), BorderLayout.NORTH);
		super.setJMenuBar(this.createMenuBar());
		super.setVisible(true);
	}

	/**
	 * Initialize graph component.
	 */
	private void initializeGraphComponent() {
		this.graphComponent = new GraphComponent();
		this.createIOhandling();
		this.graphComponent.fitGraphBounds();
		this.graphComponent.setInputMode(new GraphEditorInputMode());
		this.createDefaultStyling();
		this.createGrid();
		this.enableGrouping(true);
		this.enableUndo(true);
	}

	/**
	 * setup the Input-Output handling.
	 */
	private void createIOhandling() {
		// input-output
		GraphMLIOHandler ioh = new GraphMLIOHandler();
		try {
			this.graphComponent.setGraphMLIOHandler(ioh);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		this.graphComponent.setFileIOEnabled(true);
	}

	/**
	 * Creates the default styling for nodes. Adds label to newly created nodes
	 */
	private void createDefaultStyling() {
		ShapeNodeStyle style = new ShapeNodeStyle();
		style.setShape(ShapeNodeShape.ROUND_RECTANGLE);
		style.setPaint(Colors.ORANGE);
		Pen pen = new Pen(Colors.DARK_ORANGE, 2.0);
		style.setPen(pen);
		this.graphComponent.getGraph().getNodeDefaults().setStyle(style);
	}

	/**
	 * Set the undo engine enabled or not, to the IGraph.
	 * 
	 * @param flag
	 *            True to enable, else False.
	 */
	private void enableUndo(boolean flag) {
		this.graphComponent.getGraph().setUndoEngineEnabled(flag);
	}

	/**
	 * Enables/Disables the ability of grouping nodes.
	 * 
	 * @param flag
	 *            True to enable, False to disable.
	 */
	private void enableGrouping(Boolean flag) {
		((GraphEditorInputMode) this.graphComponent.getInputMode()).setGroupingOperationsAllowed(flag);
	}

	/**
	 * Create the grid and allow snapping to the grid. Show the grid at the
	 * background.
	 */
	private void createGrid() {
		GridInfo gridInfo = new GridInfo();
		gridInfo.setHorizontalSpacing(demo.editor.basic.GraphEditorConstants.GRID_SIZE);
		gridInfo.setVerticalSpacing(demo.editor.basic.GraphEditorConstants.GRID_SIZE);
		gridInfo.setOrigin(new PointD(0.0, 0.0));
		GraphSnapContext snapContext = new GraphSnapContext();
		snapContext.setNodeGridConstraintProvider(new GridConstraintProvider<>(gridInfo));
		snapContext.setBendGridConstraintProvider(new GridConstraintProvider<>(gridInfo));
		snapContext.setPortGridConstraintProvider(new GridConstraintProvider<>(gridInfo));
		((GraphEditorInputMode) this.graphComponent.getInputMode()).setSnapContext(new GraphSnapContext());
		this.graphComponent.getBackgroundGroup().addChild(new GridVisualCreator(gridInfo),
				ICanvasObjectDescriptor.ALWAYS_DIRTY_INSTANCE);
	}
	
	/**
	 * Builds the tool bar of the frame.
	 * 
	 * @return The tool bar.
	 */
	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(this.createCommandButtonAction("plus2-16.png", ICommand.INCREASE_ZOOM, null, this.graphComponent));
		toolBar.add(this.createCommandButtonAction("zoom-original2-16.png", ICommand.ZOOM, 1, this.graphComponent));
		toolBar.add(this.createCommandButtonAction("minus2-16.png", ICommand.DECREASE_ZOOM, null, this.graphComponent));
		toolBar.add(this.createCommandButtonAction("fit2-16.png", ICommand.FIT_GRAPH_BOUNDS, null, this.graphComponent));

		toolBar.addSeparator();
		toolBar.add(this.createCommandButtonAction("cut-16.png", ICommand.CUT, null, this.graphComponent));
		toolBar.add(this.createCommandButtonAction("copy-16.png", ICommand.COPY, null, this.graphComponent));
		toolBar.add(this.createCommandButtonAction("paste-16.png", ICommand.PASTE, null, this.graphComponent));
		toolBar.add(this.createCommandButtonAction("delete3-16.png", ICommand.DELETE, null, this.graphComponent));
		toolBar.addSeparator();
		toolBar.add(this.createCommandButtonAction("undo-16.png", ICommand.UNDO, null, this.graphComponent));
		toolBar.add(this.createCommandButtonAction("redo-16.png", ICommand.REDO, null, this.graphComponent));

		return toolBar;
	}

	/**
	 * Create a new Command Action for a command button.
	 * 
	 * @param icon
	 *            The icon of the button.
	 * @param command
	 *            The to execute on action events. May not be null.
	 * @param parameter
	 *            The initial parameter that is passed when executing the
	 *            command. May be null.
	 * @param target
	 *            The initial component that is passed when executing the
	 *            command. May be null.
	 * @return The Command Action.
	 */
	private Action createCommandButtonAction(String icon, ICommand command, Object parameter, JComponent target) {
		Action action = new CommandAction(command, parameter, target);
		action.putValue(Action.SHORT_DESCRIPTION, command.getName());
		return action;
	}
	/**
	 * Create and populate the menu bar.
	 * 
	 * @return The menu bar.
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(this.createFileMenu());
		menuBar.add(this.createLayoutMenu());
		return menuBar;
	}

	/**
	 * Creates the file menu: --new--open/save/save as --export --exit
	 *
	 * @return the file menu
	 */
	private JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("File");
		JMenuItem newItem = new JMenuItem("New");
		newItem.setAction(new AbstractAction("New") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				BasicGraphEditor.this.graphComponent.getGraph().clear();
				BasicGraphEditor.this.graphComponent.getGraph().getUndoEngine().clear();
			}
		});
		fileMenu.add(newItem);
		fileMenu.addSeparator();
		fileMenu.add(this.createCommandMenuItemAction(ICommand.OPEN, null, this.graphComponent));
		fileMenu.add(this.createCommandMenuItemAction(ICommand.SAVE, null, this.graphComponent));
		fileMenu.add(this.createCommandMenuItemAction(ICommand.SAVE_AS, null, this.graphComponent));
		fileMenu.addSeparator();
		fileMenu.add(this.createExitAction());
		return fileMenu;
	}

	/**
	 * Create a new Command Action for a menu item.
	 * 
	 * @param command
	 *            The to execute on action events. May not be null.
	 * @param parameter
	 *            The initial parameter that is passed when executing the
	 *            command. May be null.
	 * @param target
	 *            The initial component that is passed when executing the
	 *            command. May be null.
	 * @return The Command Action.
	 */
	private Action createCommandMenuItemAction(ICommand command, Object parameter, JComponent target) {
		return new CommandAction(command, parameter, target);
	}
	
	/**
	 * Create an Action to close the program.
	 * 
	 * @return
	 */
	private Action createExitAction() {
		return new AbstractAction("Exit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
	}
	
	/**
	 * Creates the layout menu. --organic/circular/hierarchic 
	 *
	 * @return the JMenu
	 */
	private JMenu createLayoutMenu() {
		JMenu layoutMenu = new JMenu("Layouts");
		for (String s : MyLayoutAction.getStandardLayoutNames())
			layoutMenu.add(this.createLayoutAction(this.graphComponent, s));
		return layoutMenu;
	}

	/**
	 * Create an Action to apply the given layout on the graph.
	 * 
	 * @param graphComponent
	 * @param layoutName
	 * @return The Action
	 */
	private Action createLayoutAction(GraphComponent graphComponent, String layoutName) {
		Action action = new MyLayoutAction(this.graphComponent, layoutName);
		action.putValue(Action.SHORT_DESCRIPTION, layoutName);
		return action;
	}
}
