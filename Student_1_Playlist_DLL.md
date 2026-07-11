# Student 1 Report: Playlist Management using a Doubly Linked List (DLL)

---

## 1. What This Part Accomplishes
This module handles active playlist management, facilitating insertion, deletion, reordering, and dual-directional navigation of songs. It powers the dynamic playback controls (Play/Pause, Next, and Previous) at the bottom control bar of the UI.

---

## 2. Why a Doubly Linked List (DLL) was Chosen
* **Bidirectional Traversal ($O(1)$)**: Unlike a singly linked list, which can only traverse forward, each node in a Doubly Linked List maintains references to both the previous (`prev`) and next (`next`) nodes. This is the optimal structure to back **Next** and **Previous** buttons: skipping in either direction is a constant time operation.
* **Efficient Reordering & Deletions ($O(1)$)**: Removing a selected song or rearranging its position (moving up or down in the playlist) only requires updating the neighbor node pointer references. Unlike arrays or lists where deleting an item triggers an $O(N)$ shift of all subsequent elements, DLL pointer relinking executes in $O(1)$ time.

---

## 3. UI/UX Component Integration
The Playlist Management module is fully integrated with the Swing user interface, presenting visual controls for all underlying operations:
* **The "DLL Playlist" Tab**: Features a dark-themed `JTable` mapping the songs currently loaded in the active playlist.
* **Control Buttons**:
  * **Play Button**: Triggers playback of the currently highlighted table row, updating the bottom control bar metadata and starting the soundwave visualizer.
  * **Move Up & Move Down Buttons**: Swaps the selected song node with its predecessor or successor. The `JTable` updates instantly to reflect the modified list order, providing direct visual feedback.
  * **Remove Button**: Unlinks the selected song from the list and dynamically refreshes the table rows.
* **Quick Add Dropdown**: Located at the bottom of the playlist view, a `JComboBox<Song>` lets users quickly select any song from the global catalog and append it to the DLL.
* **Dynamic Sidebar Badges**: The sidebar navigation button dynamically updates to display `DLL Playlist (N)` in real-time, showing the current track count.

---

## 4. How the Code Works

### Node Structure (`DLLNode`)
Stores the reference to the `Song` object along with pointers to the next and previous nodes in the sequence:
```java
class DLLNode {
    Song song;
    DLLNode prev, next;
    public DLLNode(Song s) { song = s; }
}
```

### Main List Operations (`PlaylistDLL`)
* **`add(Song s)`**: Creates a new `DLLNode`. If the list is empty, assigns both `head` and `tail` to it. Otherwise, links it to the current `tail` and updates the tail pointer.
* **`remove(Song s)`**: Iterates through the list, finds the node containing the target song ID, and rewires the `next` pointer of the previous node and the `prev` pointer of the next node to bypass and delete the target.
* **`moveUp(Song s)` / `moveDown(Song s)`**: Locates the node in the DLL and swaps its data values with its neighbor (`prev` or `next` respectively).

---

## 5. Code Snippet
```java
class PlaylistDLL {
    DLLNode head, tail;
    
    // Add to tail (O(1) complexity)
    public void add(Song s) {
        DLLNode n = new DLLNode(s);
        if (head == null) head = tail = n;
        else { tail.next = n; n.prev = tail; tail = n; }
    }
    
    // Search and unlink node (O(N) search, O(1) delete)
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
    
    // Move up in playlist (swap values)
    public void moveUp(Song s) {
        for (DLLNode c = head; c != null; c = c.next) {
            if (c.song.id == s.id && c != head) {
                Song t = c.song;
                c.song = c.prev.song;
                c.prev.song = t;
                return;
            }
        }
    }
}
```
