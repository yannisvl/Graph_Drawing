# Graph drawing algorithms

Java implementations of graph-layout exercises integrated into a Swing editor using yFiles. The project connects discrete graph algorithms with interactive visual inspection of their resulting layouts.

## Algorithms and structure

| File | Role |
| --- | --- |
| `BasicGraphEditor.java` | Interactive editor and application entry point |
| `MyLayoutAction.java` | Layout selection and animated application |
| `Inorder.java` | Inorder tree layout |
| `RectangleEnclosing.java` | Rectangle-enclosing layout |
| `Contour.java` | Contour-based layout |
| `CanonicalOrdering.java`, `Schnyder.java` | Planar-drawing machinery |
| `Forest.java`, `MyNode.java` | Supporting structures |

The editor also exposes yFiles' organic, circular and hierarchic layouts for comparison.

```mermaid
flowchart LR
    A[Create or load a graph] --> B[Choose a layout]
    B --> C[Compute coordinates]
    C --> D[Animate and inspect drawing]
```

## Build requirements

This is a source-only course project. It requires a Java development environment, Swing and a compatible **yFiles for Java** distribution with the appropriate license. The yFiles dependency is not bundled, and no Maven or Gradle build is included.

Import the sources into a Java project, configure the yFiles libraries and run `BasicGraphEditor.main`. Some references retain the original demo package name (`demo.editor.basic`); align the package layout with your yFiles demo environment before compilation.

## Scope and attribution

Algorithms have structural input assumptions, such as tree or planar-graph requirements; an arbitrary graph is not a valid input for every layout. The repository does not include a correctness benchmark or automated test suite.

The editor scaffold includes original course/demo author credits in its source headers. Those credits remain part of the project; the full editor should not be attributed solely to this repository's owner.
