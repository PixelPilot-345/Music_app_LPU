# Student 3 Report: Recommendation System using an Undirected Graph

## 1. What This Part Accomplishes
This module structures song recommendation relationships as a network map and draws it as an interactive visual node map where users can click on nodes to discover recommendations.

## 2. Why a Graph was Chosen
* **Non-linear Connections**: Music similarities are multi-dimensional (a song can be linked to multiple different tracks across genres). A graph structure naturally represents these network relationships, where songs are vertices/nodes and mutual recommendations are edges.
* **Adjacency List Mapping**: Using an adjacency list maps each song to a dynamic neighbor list of recommended songs, making neighbor lookups efficient.

## 3. How the Code Works

### Node Structure (`GraphNode`)
Stores a `Song` vertex along with a list of other `Song` references representing its undirected edges:
```java
class GraphNode {
    Song song;
    List<Song> neighbors = new ArrayList<>();
    public GraphNode(Song s) { song = s; }
}
```

### Main Graph Operations (`RecommendationGraph`)
* **`addSong(Song s)`**: Adds a song vertex to the graph node map.
* **`addConnection(Song s1, Song s2)`**: Establishes an undirected edge between `s1` and `s2` by inserting each into the other's adjacency neighbor list.
* **`getRecommendations(Song s)`**: Retrieves the list of neighbor song objects.

### Visualizer Canvas (`GraphVisualizerPanel`)
* Coordinates nodes symmetrically in a circle using trigonometric positions (`cos` and `sin`).
* Renders lines between adjacent nodes using `g2.drawLine()`.
* Listens to mouse clicks, matching the click coordinates with the node centers using Euclidean distance math (`e.getPoint().distance(nodeCenter) < 15`).

## 4. Code Snippet
```java
class RecommendationGraph {
    Map<Integer, GraphNode> nodes = new HashMap<>();
    
    public void addSong(Song s) { 
        nodes.putIfAbsent(s.id, new GraphNode(s)); 
    }
    
    public void addConnection(Song s1, Song s2) {
        addSong(s1); addSong(s2);
        if (!nodes.get(s1.id).neighbors.contains(s2)) nodes.get(s1.id).neighbors.add(s2);
        if (!nodes.get(s2.id).neighbors.contains(s1)) nodes.get(s2.id).neighbors.add(s1);
    }
}
```
