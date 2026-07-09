# Student 1 Report: Playlist Management using a Doubly Linked List (DLL)

## 1. What This Part Accomplishes
This module manages the active user playlist, allowing songs to be added, removed, reordered, and traversed. It powers the playback navigation (Play/Pause, Next, and Previous controls) at the bottom of the UI.

## 2. Why a Doubly Linked List (DLL) was Chosen
* **Bidirectional Traversal**: In a Doubly Linked List, each node maintains references to both the previous (`prev`) and next (`next`) nodes. This is the optimal data structure for implements **Next** and **Previous** track skipping, as we can step forward or backward in constant $O(1)$ time.
* **Efficient Reordering**: Reordering elements (move up or down) is done by simply swapping reference pointers or node values in $O(1)$ time, without shifting subsequent elements like in an array list.

## 3. How the Code Works

### Node Structure (`DLLNode`)
Stores the `Song` data and bidirectional pointers:
```java
class DLLNode {
    Song song;
    DLLNode prev, next;
    public DLLNode(Song s) { song = s; }
}
```

### Main List Operations (`PlaylistDLL`)
* **`add(Song s)`**: Appends a song to the end. Updates the `tail` pointer and links the new node to the old tail.
* **`remove(Song s)`**: Finds the song and adjusts its neighbors' pointers to unlink it.
* **`findNode(Song s)`**: Iterates through the list to return the matching `DLLNode` pointer.
* **`moveUp(Song s)` / `moveDown(Song s)`**: Swaps adjacent song data values to change their layout positions.

## 4. Code Snippet
```java
class PlaylistDLL {
    DLLNode head, tail;
    
    public void add(Song s) {
        DLLNode n = new DLLNode(s);
        if (head == null) head = tail = n;
        else { tail.next = n; n.prev = tail; tail = n; }
    }
    
    public DLLNode findNode(Song s) {
        if (s == null) return null;
        for (DLLNode c = head; c != null; c = c.next) {
            if (c.song.id == s.id) return c;
        }
        return null;
    }
}
```
