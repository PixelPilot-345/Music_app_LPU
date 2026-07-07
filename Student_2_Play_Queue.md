# Student 2 Report: Play Queue using a FIFO Queue

## 1. What This Part Accomplishes
This module implements a dynamic playback queue, allowing users to stage multiple songs to be played sequentially in the future (a "play next" queue).

## 2. Why a Queue (FIFO) was Chosen
* **First-In, First-Out (FIFO) Order**: A queue is the natural data structure for playback scheduling. The first song added to the queue should be the first song to play when the current track finishes.
* **Separation of Concerns**: Staging upcoming songs in a dedicated queue keeps the primary playlist intact and allows on-the-fly custom ordering.

## 3. How the Code Works

### Node Structure (`QueueNode`)
A simple singly linked list node containing the `Song` data and a reference pointer to the `next` node:
```java
class QueueNode {
    Song song;
    QueueNode next;
    public QueueNode(Song s) { song = s; }
}
```

### Main Queue Operations (`PlayQueue`)
* **`enqueue(Song s)`**: Adds a new song to the tail of the queue. If the queue is empty, both `head` and `tail` reference the new node. Otherwise, the old tail's `next` pointer is updated to the new node, and the `tail` pointer is shifted.
* **`dequeue()`**: Removes and returns the song at the head of the queue. The `head` pointer is moved to `head.next`. If the queue becomes empty, the `tail` pointer is reset to null.

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
        Song s = head.song; 
        head = head.next;
        if (head == null) tail = null;
        return s;
    }
}
```
