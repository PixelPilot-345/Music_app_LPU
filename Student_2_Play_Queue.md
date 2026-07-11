# Student 2 Report: Play Queue using a FIFO Queue & Playback Automation

---

## 1. What This Part Accomplishes
This module manages the staging of upcoming tracks using a custom First-In, First-Out (FIFO) queue. It also contains the core playback automation logic: playing the next song automatically when a WAV track finishes streaming.

---

## 2. Playback Automation & Routing Rules
To deliver a smooth listening experience, the automation controller coordinates the playback queue and playlist using these priorities:
* **Rule 1: Queue Priority**: When a song finishes playing, the player polls the **FIFO Queue** first.
* **Rule 2: Transition from Queue to Playlist**:
  * If the Queue is **not empty**, the head song is dequeued (`dequeue()`) and played immediately.
  * If the Queue is **empty**, the player falls back to the **Playlist Doubly Linked List** and plays the next chronological track relative to the current playing node.
* **Rule 3: Continuous Loop**: If the end of the playlist is reached and the queue is empty, the player wraps around to the beginning (`head`) of the playlist.

---

## 3. UI/UX Component Integration
The play queue is fully integrated with the visual dashboard and player layout:
* **The "FIFO Queue" Tab**: Displays a JTable listing the queued songs in their exact play order. Columns include queue positions, song titles, and artist credits.
* **Interactive Controls**:
  * **Quick Add Selector**: A `JComboBox<Song>` dropdown at the bottom of the FIFO Queue page allows users to pick any catalog song and click "Add to Queue" to stage it.
  * **Dashboard Integrations**: Users can select a song on the main Dashboard list and click the "Add to Play Queue" button to instantly push it into the queue.
  * **Play Next in Queue Button**: Triggers the automation manually to play the first queued song, sliding all remaining queued songs up in position.
* **Dynamic Sidebar Badges**: The sidebar menu displays a real-time badge: `FIFO Queue (N)`, which updates automatically whenever a song is enqueued or dequeued.

---

## 4. How the Code Works

### Node Structure (`QueueNode`)
Wraps the `Song` item and points to the next queued item:
```java
class QueueNode {
    Song song;
    QueueNode next;
    public QueueNode(Song s) { song = s; }
}
```

### Queue Operations (`PlayQueue`)
* **`enqueue(Song s)`**: Pushes a new node to the tail. If the queue is empty, assigns both `head` and `tail` to it. Otherwise, points the current `tail.next` to the new node and moves `tail` to the new node.
* **`dequeue()`**: Extracts the node at the `head` of the queue, shifts `head` forward, and returns the song.

---

## 5. Code Snippet
```java
class PlayQueue {
    QueueNode head, tail;
    
    // Add to tail (O(1) time complexity)
    public void enqueue(Song s) {
        QueueNode n = new QueueNode(s);
        if (tail == null) head = tail = n;
        else { tail.next = n; tail = n; }
    }
    
    // Extract from head (O(1) time complexity)
    public Song dequeue() {
        if (head == null) return null;
        Song s = head.song;
        head = head.next;
        if (head == null) tail = null;
        return s;
    }
}
```

### Automation Integration (`playNext()`)
```java
private void playNext() {
    Song next = playQueue.dequeue();
    if (next != null) {
        currentPlayingNode = activePlaylist.findNode(next);
        play(next);
        refreshQueue();
    } else {
        if (activePlaylist.head == null) return;
        if (currentPlayingNode == null) {
            currentPlayingNode = activePlaylist.head;
        } else {
            currentPlayingNode = (currentPlayingNode.next != null) ? currentPlayingNode.next : activePlaylist.head;
        }
        if (currentPlayingNode != null) {
            play(currentPlayingNode.song);
        }
    }
}
```
