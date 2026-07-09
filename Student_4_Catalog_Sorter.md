# Student 4 Report: Catalog Sorting using a Custom Bubble Sort Algorithm

## 1. What This Part Accomplishes
This module manages sorting options for the song catalog on the main Dashboard. It allows users to sort the catalog list alphabetically by **Title** or **Artist**, and acts as a direct helper for the **Binary Search** system.

## 2. Why Bubble Sort was Chosen
* **In-Place Sorting**: Bubble Sort compares adjacent items and swaps them in place. It requires $O(1)$ auxiliary memory space, editing the catalog list directly.
* **Coordination with Search**: To search using Binary Search (Student 3's part), the collection must be sorted. The Bubble Sort implementation guarantees the sorting prerequisite is satisfied.

## 3. How the Code Works

### Main Sorting Class (`CatalogSorter`)
* **`sortByTitle(List<Song>)`**:
  * Traverses the list and compares the `title` field of adjacent elements using `compareToIgnoreCase()`.
  * If the left element is alphabetically greater than the right element, they are swapped.
* **`sortByArtist(List<Song>)`**:
  * Traverses the list and compares the `artist` field of adjacent elements, swapping them if they are in the wrong order.

## 4. Code Snippet
```java
class CatalogSorter {
    public static void sortByTitle(List<Song> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).title.compareToIgnoreCase(list.get(j + 1).title) > 0) {
                    Song temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }
}
```
