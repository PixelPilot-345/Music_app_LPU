# Student 3 Report: Song Catalog Search using Binary Search

---

## 1. What This Part Accomplishes
This module provides high-speed song lookup within the global song catalog. It allows users to quickly search the library by song title, locating the exact match and highlighting it visually on the dashboard interface.

---

## 2. The Mechanics of Binary Search
Binary Search is a classic divide-and-conquer algorithm designed for sorted lists. Its operations follow these stages:
1. **Define Boundaries**: Initialize `low` at 0 and `high` at `list.size() - 1`.
2. **Find Middle Point**: Calculate `mid = (low + high) / 2`.
3. **Compare Keys**:
   * If the song at the `mid` index has a title matching the search query (ignoring case), the algorithm terminates and returns `mid`.
   * If the search query is lexicographically larger than the middle title, the search moves to the right half: set `low = mid + 1`.
   * If the query is smaller, the search moves to the left half: set `high = mid - 1`.
4. **Repeat**: Continue steps 2-3 while `low <= high`. If the boundaries cross without a match, return `-1` (indicating not found).

---

## 3. Prerequisite Sorting & Collaboration
* **Sorted Search Space**: Binary Search relies on the sorting precondition. It *only* works if the list is already sorted lexicographically.
* **Coordination**: Before executing the search, the program invokes an inline comparison sort on the `catalog` list to sort it alphabetically by title:
  ```java
  catalog.sort((a, b) -> a.title.compareToIgnoreCase(b.title));
  ```
  This ensures that the list complies with the binary search precondition, guaranteeing correct and reliable results.

---

## 4. UI/UX Interaction & Selection Routing
The search module is connected directly to the user interface on the **Dashboard** panel:
1. **Search Input**: The user types a song title into the text field `txtSearch` and clicks the "Search" button.
2. **Pre-processing**: The event handler automatically sorts the `catalog` and updates the Dashboard `JList` view (`refreshDashboardList()`) to match the sorted order.
3. **Search Execution**: The binary search executes instantly in logarithmic $O(\log N)$ steps.
4. **Visual Selection & Scrolling**: If a match is found:
   * **Highlight Selection**: The JList selection is updated to highlight the matching row: `list.setSelectedIndex(idx)`.
   * **Auto-Scroll to View**: The list is scrolled to ensure the selected song is visible to the user: `list.ensureIndexIsVisible(idx)`.
   * **Update Details Panel**: The selected song details card (Title, Artist, Genre) updates immediately.
   * **Success Dialogue**: A dialog box pops up confirming: `Found: [Song Title]`.
5. **Immediate Action**: Once highlighted, the user can click:
   * **PLAY NOW**: Plays the song immediately in the player.
   * **Add to Playlist / Add to Play Queue**: Inserts the song into the corresponding DLL playlist or FIFO queue.

---

## 5. Algorithmic Source Code
```java
class BinarySearch {
    public static int searchByTitle(List<Song> list, String query) {
        int low = 0;
        int high = list.size() - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = list.get(mid).title.compareToIgnoreCase(query);
            
            if (cmp == 0) {
                return mid; // Match found at index mid
            } else if (cmp < 0) {
                low = mid + 1; // Discard left half
            } else {
                high = mid - 1; // Discard right half
            }
        }
        return -1; // Match not found
    }
}
```

### Event Handler in GUI
```java
btnSearch.addActionListener(e -> {
    String q = txtSearch.getText().trim();
    if (!q.isEmpty()) {
        // Step 1: Pre-sort the catalog (precondition check)
        catalog.sort((a, b) -> a.title.compareToIgnoreCase(b.title));
        refreshDashboardList();
        
        // Step 2: Perform O(log N) binary search
        int idx = BinarySearch.searchByTitle(catalog, q);
        
        if (idx >= 0) {
            // Step 3: Highlight selection in UI
            list.setSelectedIndex(idx);
            
            // Step 4: Scroll list view to make selection visible
            list.ensureIndexIsVisible(idx);
            
            // Step 5: Update preview details card
            selectSong(catalog.get(idx));
            
            // Step 6: Notify the user
            JOptionPane.showMessageDialog(this, "Found: " + catalog.get(idx).title);
        } else {
            JOptionPane.showMessageDialog(this, "Song not found!");
        }
    }
});
```
