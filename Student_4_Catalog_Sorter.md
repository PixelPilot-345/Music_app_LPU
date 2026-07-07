# Student 4 Report: Catalog Sorting using a Custom Bubble Sort Algorithm

## 1. What This Part Accomplishes
This module handles sorting and organizing the song catalog on the main Dashboard. It allows users to dynamically sort the library of available songs by **Title** or **Artist** on demand.

## 2. Why Bubble Sort was Chosen
* **In-Place Sorting**: Bubble Sort works by comparing adjacent elements and swapping them if they are in the wrong order. It requires $O(1)$ extra space, sorting the catalog directly inside the existing song list.
* **Educational & Easy to Explain**: As one of the foundational sorting algorithms taught in computer science curricula, it is highly appropriate for college student presentations and demonstrates clear, manual algorithmic iteration logic.

## 3. How the Code Works

### Main Sorting Class (`CatalogSorter`)
* **`sortByTitle(List<Song>)`**:
  * Traverses the list and compares the `title` field of adjacent elements using `compareToIgnoreCase()`.
  * If the left element is alphabetically greater than the right element, they are swapped.
  * Repeats this process until the entire list is fully sorted.
* **`sortByArtist(List<Song>)`**:
  * Employs the identical sorting loop structure, but compares the `artist` field of the song objects instead.

### Dashboard UI Integration
* Adds two control buttons to the Dashboard panel: **By Title** and **By Artist**.
* Invokes `CatalogSorter.sortByTitle(catalog)` or `CatalogSorter.sortByArtist(catalog)` upon action triggers, then calls `refreshDashboardList()` to refresh the dashboard list visual elements.

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
