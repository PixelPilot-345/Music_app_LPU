# 🎵 SocialMusic - College Data Structures & Algorithms Project

A clean, modern, student-made Java music player application implementing **4 distinct custom data structures and algorithms** to showcase collaboration on a team project.

---

## 👥 The Team & DSA Contributions

This project simulates a 4-person development team. Each member designed and implemented a specific Data Structure or Algorithm:

### 1. Playlist Management (Doubly Linked List) — **Student 1**
* **Class**: `PlaylistDLL` / `DLLNode`
* **Purpose**: Manages the user's active playlist. Supports adding, removing, shifting track positions (moving tracks up and down), and traversing the playlist backwards and forwards.
* **Why DLL**: Allows bidirectional traversal for **Next** and **Previous** buttons in $O(1)$ time, and efficient unlinking of nodes without shifting array items.

### 2. Staged Play Queue (FIFO Queue) — **Student 2**
* **Class**: `PlayQueue` / `QueueNode`
* **Purpose**: Allows users to stage upcoming tracks in a queue. Supports enqueuing and dequeuing operations.
* **Why Queue**: Employs First-In, First-Out (FIFO) logic so songs are played in the exact order they were queued. Integrates prioritised playback automation (plays queued tracks before returning to the playlist).

### 3. Song Search (Binary Search) — **Student 3**
* **Class**: `BinarySearch`
* **Purpose**: Enables rapid searching of the song catalog by title.
* **Why Binary Search**: Operates in $O(\log N)$ logarithmic time. Since Binary Search requires a sorted collection, it works collaboratively with Student 4's sorting system.

### 4. Catalog Sorting (Bubble Sort) — **Student 4**
* **Class**: `CatalogSorter`
* **Purpose**: Sorts the catalog list alphabetically by **Title** or **Artist** on the Dashboard.
* **Why Bubble Sort**: Implements an in-place sorting routine that satisfies the sorting precondition required for Binary Search.

---

## 🚀 How to Run the App

### Prerequisites
* Java Development Kit (JDK) 21 or higher installed.
* A folder named `songs/` in the root directory containing `.wav` music files (e.g. `sample-3s.wav`, `gc.wav`, `synth.wav`, etc.).

### Command Line
Run the application directly using the single-file source launcher:
```powershell
& "C:\Users\HP\.vscode\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64\bin\java.exe" SocialMusicApp.java
```

---

## 🎨 Features & UI/UX
* **Flat Dark Aesthetics**: Styled with a customized dark palette (Blue-gray `#21242E` and deep Indigo accents `#7C4DFF`).
* **Dynamic Audio Engine**: Directly opens and streams standard uncompressed WAV audio clips using the Java Sound API.
* **Real-time Visualizer**: Draws bouncing soundwave bars dynamically to visualize music playback.
* **Collaborative Design**: Each member's tab is seamlessly integrated with the central navigation bar.
