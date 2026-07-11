# Student 4 Report: Recommendation System using a Custom Graph Structure

---

## 1. What This Part Accomplishes
This module manages song recommendations based on a custom Graph data structure. It builds connections (edges) between related songs (vertices), forming a musical social network that suggests similar music whenever a user interacts with a song.

---

## 2. Why a Graph (Adjacency List) was Chosen
* **Non-linear Modeling**: Music recommendations are naturally non-linear. A song can link to multiple other tracks based on genre, tempo, or artist relations. A Graph is the most natural data structure to represent these connections.
* **Memory & Lookup Efficiency**: Instead of an $O(V^2)$ adjacency matrix, which wastes memory on empty connections, this module implements an **Adjacency List** representation. Storing neighbor lists in a Hash Map optimizes memory efficiency to $O(V + E)$ and enables O(1) retrieval of recommendations.

---

## 3. UI/UX Component Integration
The Recommendation Graph is dynamically wired to the primary application panels:
* **Dynamic Selection Sync**: Whenever the user selects a song from the Dashboard catalog list, or whenever a new song starts playing in the player, the app triggers an update hook:
  ```java
  updateRecommendations(selectedSong);
  ```
* **The "Recommendations" Tab**: Displays the dynamic list of suggestions for the active song. It lists all adjacent songs in the Graph.
* **Instant Playback Control**: Features a "Play Selected Recommendation" button at the bottom of the recommendations tab. Selecting a song and clicking it starts the track instantly and re-calculates recommendations for the new song, allowing continuous exploration.

---

## 4. How the Code Works

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
* **`addSong(Song s)`**: Adds a new vertex to the Graph structure.
* **`addConnection(Song s1, Song s2)`**: Connects two song nodes, building an undirected edge in the adjacency list.
* **`getRecommendations(Song s)`**: Retrieves the list of neighbor song objects for O(1) recommendation lookups.

---

## 5. Code Snippet
```java
class RecommendationGraph {
    private java.util.Map<Integer, GraphNode> nodes = new java.util.HashMap<>();
    
    // Add vertex
    public void addSong(Song s) { nodes.putIfAbsent(s.id, new GraphNode(s)); }
    
    // Add undirected edge (connection)
    public void addConnection(Song s1, Song s2) {
        addSong(s1); addSong(s2);
        if (!nodes.get(s1.id).neighbors.contains(s2)) nodes.get(s1.id).neighbors.add(s2);
        if (!nodes.get(s2.id).neighbors.contains(s1)) nodes.get(s2.id).neighbors.add(s1);
    }
    
    // Fetch neighbors (O(1) lookup)
    public List<Song> getRecommendations(Song s) {
        if (s == null || !nodes.containsKey(s.id)) return new ArrayList<>();
        return nodes.get(s.id).neighbors;
    }
}
```
