# Student 2 Report: Play Queue using a FIFO Queue & Playback Automation

## 1. What This Part Accomplishes
This module implements a dynamic queue for staging upcoming tracks. It also coordinates the playback automation logic: playing the next song automatically when the current track ends.

## 2. Playback Automation Rule
* **Priority Playback**: When the music player finishes playing a song (or when the user skips next), the app first checks if there are any songs staged in the **FIFO Queue**.
* **Queue First, Then Playlist**:
  * If the Queue is **not empty**, the top song is dequeued (`dequeue()`) and played.
  * If the Queue is **empty**, the player automatically falls back to the **Playlist Doubly Linked List** and plays the next chronological track relative to the current position.

## 3. How the Code Works

### Main Queue Operations (`PlayQueue`)
* **`enqueue(Song s)`**: Adds a new song to the tail of the queue.
* **`dequeue()`**: Retrieves and removes the song at the head of the queue.

### Automation Controller (`playNext()`)
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

## 4. Code Snippet
```java
class PlayQueue {
    QueueNode head, tail;
    
    public void enqueue(Song s) {
        QueueNode n = new QueueNode(s);
        if (tail == null) head = tail = n;
        else { tail.next = n; tail = n; }
    }
    
    public Song dequeue() {
        if (head == null) return null;
        Song s = head.song; head = head.next;
        if (head == null) tail = null;
        return s;
    }
}
```
