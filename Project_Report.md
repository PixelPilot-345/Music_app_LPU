# 🎵 SocialMusic - Comprehensive Project Report

---

## 📄 Cover Page

* **Title of the Project**: SocialMusic - Interactive Music Player with Custom DSA
* **Submitted by**: 
  1. `[Student 1 Name]` (Roll No: `[Roll 1]`, Reg No: `[Reg 1]`) - Doubly Linked List Module
  2. `[Student 2 Name]` (Roll No: `[Roll 2]`, Reg No: `[Reg 2]`) - Play Queue & Automation Module
  3. `[Student 3 Name]` (Roll No: `[Roll 3]`, Reg No: `[Reg 3]`) - Binary Search Module
  4. `[Student 4 Name]` (Roll No: `[Roll 4]`, Reg No: `[Reg 4]`) - Recommendation Graph Module
* **Course/Program**: Bachelor of Technology (B.Tech) in Computer Science & Engineering
* **Department**: Department of Computer Science & Engineering
* **Institution Name**: Lovely Professional University (LPU)
* **Academic Session**: 2026-2027
* **Guide/Mentor Name**: `[Mentor/Professor Name]`

---

## 🗂 Table of Contents
1. [Introduction](#1-introduction)
2. [Problem Statement](#2-problem-statement)
3. [Objectives](#3-objectives)
4. [Project Modules & Team Roles](#4-project-modules--team-roles)
5. [Technologies Used](#5-technologies-used)
6. [Module Description (Member-wise)](#6-module-description-member-wise)
7. [Implementation (Source Code)](#7-implementation-source-code)
8. [Data Structures & Algorithms Used](#8-data-structures--algorithms-used)
9. [Screenshots of the Project](#9-screenshots-of-the-project)
10. [Future Scope of the Project](#10-future-scope-of-the-project)
11. [Conclusion](#11-conclusion)
12. [References](#12-references)

---

## 1. Introduction
SocialMusic is a Java Swing-based desktop music player engineered to showcase custom-built data structures and algorithmic coordination in a collaborative team project. The application reads local `.wav` files, dynamically plays back audio, and renders a real-time sound visualizer, all managed through custom collections.

---

## 2. Problem Statement
Commercial audio players rely on complex libraries and default collection APIs. In an academic framework, building a music player with standard collections masks the underlying complexity of memory allocation and pointer manipulations. The challenge is to construct a fully functional player with custom-written data structures (DLL, FIFO Queue, Adjacency List Graph) that function cooperatively, ensuring clean memory management and thread-safe audio streaming.

---

## 3. Objectives
* Design and implement a custom **Doubly Linked List** to handle track sequence, order shifts, and bidirectional traversal.
* Implement a **First-In, First-Out (FIFO) Play Queue** with automatic priority playback routing.
* Integrate a logarithmic **Binary Search** query lookup system to search the song catalog.
* Construct an **Adjacency List Recommendation Graph** to suggest connected music tracks.
* Build a beautiful dark-themed GUI and real-time visualizer to provide a modern desktop user experience.

---

## 4. Project Modules & Team Roles
* **Module 1: Doubly Linked List Playlist Management (Student 1)**: Responsible for the playlist structures, element insertion/deletion, position adjustments, and track skipping hooks.
* **Module 2: FIFO Queue & Playback Automation (Student 2)**: Responsible for play queue structures and coordinate the auto-play progression thread when a track ends.
* **Module 3: Song Search (Student 3)**: Responsible for the sorted catalog search, JList highlights, auto-scrolling viewport, and layout matching.
* **Module 4: Recommendation Engine (Student 4)**: Responsible for establishing similarity links between tracks in the Graph database and populating recommendations.

---

## 5. Technologies Used
* **Language**: Java JDK 21
* **Graphic Framework**: Java Swing & AWT (Abstract Window Toolkit)
* **Audio Library**: javax.sound.sampled (Java Sound API)
* **Development Environment**: VS Code / Windows PowerShell
* **Version Control**: Git & GitHub

---

## 6. Module Description (Member-wise)

### Module 1: Playlist Management (Doubly Linked List) — Student 1
This module controls the active playlist table. By utilizing prev and next nodes, it handles track skips and reordering.
* **UI/UX Integration**:
  * **The DLL Playlist View**: Renders the active playlist in a custom-styled dark-themed `JTable`.
  * **Track Reordering**: Provides "Move Up" and "Move Down" buttons that swap adjacent node values in the DLL. The JTable reflects the updated order instantly, letting users easily reorganize their tracks.
  * **Interactive Playlist Controls**:
    * **Play Button**: Triggers the audio stream for the selected song in the playlist.
    * **Remove Button**: Unlinks the selected song from the list and refreshes the table row layout.
    * **Quick Add Selector**: A `JComboBox<Song>` allows users to select any song from the global catalog and append it to the DLL.
    * **Sidebar Notification Badge**: Dynamically displays the size of the playlist as `DLL Playlist (N)`.

### Module 2: Play Queue & Automation — Student 2
Stages pending tracks in a queue. Integrates automatic transition logic:
* **UI/UX Integration**:
  * **The FIFO Queue View**: Shows upcoming tracks in their exact play order.
  * **Staging Controls**: Includes an "Add to Queue" button on the Dashboard and a "Quick Add" dropdown selector inside the Queue panel to push tracks onto the tail of the play queue.
  * **Playback Automation**: Automatically polls the queue when a track ends. If populated, it dequeues the head song and plays it immediately. If empty, it falls back to the active Playlist DLL to continue playing.
  * **Sidebar Badge**: Real-time counter badge `FIFO Queue (N)` updates whenever a song is enqueued or dequeued.

### Module 3: Song Search (Binary Search) — Student 3
Performs binary search lookup on the catalog list.
* **Detailed Algorithmic Mechanics**:
  * Binary Search uses a divide-and-conquer strategy to locate a target item in logarithmic $O(\log N)$ time.
  * **The Sorting Precondition**: Because Binary Search splits the search space in half by comparing with a midpoint, it requires the search space to be sorted.
  * **The Search Flow**:
    1. When the user types a search query in `txtSearch` and clicks the "Search" button, the event handler first sorts the `catalog` list alphabetically by title:
       `catalog.sort((a, b) -> a.title.compareToIgnoreCase(b.title));`
    2. The Dashboard `JList` updates to reflect this sorted order.
    3. The binary search starts lookups. It computes the midpoint `mid = (low + high) / 2`.
    4. If the song at the `mid` index matches the query (case-insensitive), it returns `mid`.
    5. If the target query is lexicographically larger than the midpoint song title, `low` is moved to `mid + 1`. Otherwise, `high` is moved to `mid - 1`.
    6. If no match is found, it returns `-1`.
  * **UI/UX Selection & Scrolling**:
    * If a match is found (index $\ge 0$), the GUI updates the Dashboard list selection: `list.setSelectedIndex(idx)`.
    * It automatically scrolls the list viewport to make the highlighted selection visible: `list.ensureIndexIsVisible(idx)`.
    * The song preview card updates immediately.
    * A success message box pops up confirming: `Found: [Song Title]`.
    * The user can then click the "PLAY NOW" button or add the track to the playlist/queue.

### Module 4: Recommendations (Adjacency List Graph) — Student 4
Builds an undirected Graph mapping song connections.
* **UI/UX Integration**:
  * **The Recommendations Tab**: Displays dynamic recommendations based on connections in the graph.
  * **Dynamic Synchronization**: Selecting a song on the Dashboard or playing a track triggers `updateRecommendations(...)` to load all adjacent neighbor nodes of that track.
  * **Immediate Exploration**: Clicking "Play Selected Recommendation" starts playback of the suggested track and recalculates recommendations for the new song on the fly.

---

## 7. Implementation (Source Code)
Refer to the complete, self-contained implementation file: [SocialMusicApp.java](file:///c:/Users/HP/Desktop/July%20project/SocialMusicApp.java).

---

## 8. Data Structures & Algorithms Used

### Doubly Linked List (DLL)
```java
class DLLNode {
    Song song;
    DLLNode prev, next;
}
```

### FIFO Queue
```java
class QueueNode {
    Song song;
    QueueNode next;
}
```

### Binary Search
```java
class BinarySearch {
    public static int searchByTitle(List<Song> list, String query) {
        int low = 0, high = list.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = list.get(mid).title.compareToIgnoreCase(query);
            if (cmp == 0) return mid;
            else if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return -1;
    }
}
```

### Recommendation Graph (Adjacency List)
```java
class GraphNode {
    Song song;
    List<Song> neighbors = new ArrayList<>();
}
```

---

## 9. Screenshots of the Project
`[Insert Screenshots here: /logo.png is loaded in the top left sidebar of the application]`

---

## 10. Future Scope of the Project
* **SQL Database Integration**: Swap local mock data collections with a SQLite or MySQL database for persistent playlists.
* **Network Playback**: Synchronize playback with peers using Socket connections.
* **Audio Equalizer**: Add digital signal processing filters to adjust treble and bass parameters.

---

## 11. Conclusion
The SocialMusic project successfully demonstrates the implementation and interaction of key data structures (Doubly Linked Lists, Queues, and Graphs) with real-world algorithms in a graphical Java desktop program. The collaboration highlights how distinct student modules integrate seamlessly into a single production-ready codebase.

---

## 12. References
1. *Introduction to Algorithms* (CLRS), Cormen, Leiserson, Rivest, and Stein.
2. Oracle Java Sound API Documentation: [https://docs.oracle.com/javase/tutorial/sound/](https://docs.oracle.com/javase/tutorial/sound/)
3. GeeksforGeeks Data Structures: [https://www.geeksforgeeks.org/data-structures/](https://www.geeksforgeeks.org/data-structures/)
