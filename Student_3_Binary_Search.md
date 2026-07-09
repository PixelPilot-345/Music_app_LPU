# Student 3 Report: Song Catalog Search using Binary Search

## 1. What This Part Accomplishes
This module handles search functionality for the song catalog on the Dashboard. It allows users to quickly find and select any song by title using a search text field.

## 2. Why Binary Search was Chosen
* **Logarithmic Time Complexity**: Binary Search runs in $O(\log N)$ time, making search lookups extremely fast even as the catalog grows.
* **Prerequisite Coordination**: Binary Search requires the underlying list to be sorted. This creates a natural collaborative link with **Student 4**'s sorting algorithm: before performing the search, the list is sorted by title.

## 3. How the Code Works

### Binary Search Algorithm (`BinarySearch`)
* Compares the query string with the title of the song at the midpoint (`mid`) of the current search boundary.
* If the midpoint title matches the query (ignoring case), it returns the index.
* If the midpoint title is lexicographically smaller than the query, the left boundary is moved to `mid + 1`.
* If it is greater, the right boundary is moved to `mid - 1`.

### UI Integration
When the user clicks "Search" on the Dashboard:
1. Calls `CatalogSorter.sortByTitle(catalog)` to sort the library.
2. Calls `BinarySearch.searchByTitle(catalog, query)`.
3. If found (index $\ge 0$), selects the song in the dashboard list and centers the view.

## 4. Code Snippet
```java
class BinarySearch {
    public static int searchByTitle(List<Song> list, String query) {
        int low = 0;
        int high = list.size() - 1;
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
