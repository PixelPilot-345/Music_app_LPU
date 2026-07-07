# Student 1 Report: Playlist Management using a Doubly Linked List (DLL)

## 1. What This Part Accomplishes
This module manages the active user playlist, allowing songs to be added, removed, and reordered (moved up or down).

## 2. Why a Doubly Linked List (DLL) was Chosen
* **Efficient Reordering**: In a Doubly Linked List, each node maintains pointers to both its predecessor (`prev`) and successor (`next`). This allows us to reorder elements (move up or down) by simply swapping reference pointers or node values in $O(1)$ time, without shifting subsequent elements like in an array or ArrayList.
* **Efficient Deletion**: Unlinking a node from anywhere in the playlist is a simple pointer-reassignment operation, making insertion and deletion highly efficient.

## 3. How the Code Works

### Node Structure (`DLLNode`)
Stores the `Song` data and two pointers:
```java
class DLLNode {
    Song song;
    DLLNode prev, next;
    public DLLNode(Song s) { song = s; }
}
```

### Main List Operations (`PlaylistDLL`)
* **`add(Song s)`**: Appends a song to the end. If the list is empty, both `head` and `tail` point to the new node. Otherwise, the current tail's `next` is set to the new node, the new node's `prev` points to the old tail, and `tail` is updated.
* **`remove(Song s)`**: Traverses the list to find the matching song. Once found, it adjusts the `next` pointer of the predecessor and the `prev` pointer of the successor, unlinking the node.
* **`moveUp(Song s)` / `moveDown(Song s)`**: Finds the song node and swaps its `Song` value with its predecessor (for up) or successor (for down), shifting the track's position in the playlist table.

## 4. Code Snippet
```java
class PlaylistDLL {
    DLLNode head, tail;
    
    public void add(Song s) {
        DLLNode n = new DLLNode(s);
        if (head == null) head = tail = n;
        else { tail.next = n; n.prev = tail; tail = n; }
    }
    
    public void remove(Song s) {
        for (DLLNode c = head; c != null; c = c.next) {
            if (c.song.id == s.id) {
                if (c == head) head = head.next;
                if (c == tail) tail = tail.prev;
                if (c.prev != null) c.prev.next = c.next;
                if (c.next != null) c.next.prev = c.prev;
                return;
            }
        }
    }
}
```
