# Student 4 Report: Recommendation System using a Custom Graph Structure

## 1. What This Part Accomplishes
This module manages recommendations based on a custom Graph data structure. It connects related songs to form a social music network, suggesting similar music whenever the user selects a track.

## 2. Why a Graph (Adjacency List) was Chosen
* **Non-linear Relationships**: Music connections are complex and non-linear. A song can be connected to multiple other songs. A Graph is the natural and optimal data structure to model these relationships.
* **Adjacency List Efficiency**: Storing neighbors in an Adjacency List (rather than a 2D matrix) optimizes memory efficiency ($O(V + E)$) and makes fetching recommendations simple and fast.

## 3. How the Code Works

### Graph Node (`GraphNode`)
Represents a vertex in the Graph, wrapping the `Song` data and its list of neighboring songs (edges):
```java
class GraphNode {
    Song song;
    List<Song> neighbors = new ArrayList<>();
    public GraphNode(Song s) { song = s; }
}
```

### Recommendation Graph Class (`RecommendationGraph`)
* **`addSong(Song s)`**: Adds a new vertex to the Graph.
* **`addConnection(Song s1, Song s2)`**: Creates an undirected edge (connection) between two song nodes.
* **`getRecommendations(Song s)`**: Returns the list of neighboring song objects connected to the given song in $O(1)$ lookup time.

## 4. Code Snippet
```java
class RecommendationGraph {
    private java.util.Map<Integer, GraphNode> nodes = new java.util.HashMap<>();
    
    public void addSong(Song s) { nodes.putIfAbsent(s.id, new GraphNode(s)); }
    
    public void addConnection(Song s1, Song s2) {
        addSong(s1); addSong(s2);
        if (!nodes.get(s1.id).neighbors.contains(s2)) nodes.get(s1.id).neighbors.add(s2);
        if (!nodes.get(s2.id).neighbors.contains(s1)) nodes.get(s2.id).neighbors.add(s1);
    }
    
    public List<Song> getRecommendations(Song s) {
        if (s == null || !nodes.containsKey(s.id)) return new ArrayList<>();
        return nodes.get(s.id).neighbors;
    }
}
```
