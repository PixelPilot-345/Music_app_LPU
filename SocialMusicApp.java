import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import javax.sound.sampled.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

// ==========================================
// 1. DATA MODELS & CUSTOM DATA STRUCTURES
// ==========================================

class Song {
    public static void main(String[] args) {
        SocialMusicApp.main(args);
    }

    int id;
    String title;
    String artist;
    String genre;
    String filename; // e.g. "sample-3s.wav"
    String duration; // e.g. "0:03"
    int plays;
    int likes;

    public Song(int id, String title, String artist, String genre, String filename, String duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.filename = filename;
        this.duration = duration;
        this.plays = (int)(Math.random() * 400) + 100;
        this.likes = (int)(plays * (0.4 + Math.random() * 0.4));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Song)) return false;
        Song other = (Song) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return title + " - " + artist + " [" + genre + "]";
    }
}

class SongNode {
    Song song;
    SongNode prev;
    SongNode next;

    public SongNode(Song song) {
        this.song = song;
    }
}

class PlaylistDLL {
    private SongNode head;
    private SongNode tail;
    private SongNode current;
    private int size = 0;

    public synchronized void add(Song song) {
        SongNode node = new SongNode(song);
        if (head == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
    }

    public synchronized void remove(Song song) {
        SongNode temp = head;
        while (temp != null) {
            if (temp.song.equals(song)) {
                if (temp.prev != null) {
                    temp.prev.next = temp.next;
                } else {
                    head = temp.next;
                }
                
                if (temp.next != null) {
                    temp.next.prev = temp.prev;
                } else {
                    tail = temp.prev;
                }

                if (current == temp) {
                    current = (temp.next != null) ? temp.next : temp.prev;
                }
                size--;
                return;
            }
            temp = temp.next;
        }
    }

    public synchronized void moveUp(Song song) {
        SongNode node = findNode(song);
        if (node == null || node.prev == null) return;

        SongNode p = node.prev;
        SongNode n = node.next;
        SongNode pp = p.prev;

        if (pp != null) pp.next = node;
        else head = node;

        node.prev = pp;
        node.next = p;
        p.prev = node;
        p.next = n;

        if (n != null) n.prev = p;
        else tail = p;
    }

    public synchronized void moveDown(Song song) {
        SongNode node = findNode(song);
        if (node == null || node.next == null) return;

        SongNode n = node.next;
        SongNode p = node.prev;
        SongNode nn = n.next;

        if (p != null) p.next = n;
        else head = n;

        n.prev = p;
        n.next = node;
        node.prev = n;
        node.next = nn;

        if (nn != null) nn.prev = node;
        else tail = node;
    }

    private SongNode findNode(Song song) {
        SongNode temp = head;
        while (temp != null) {
            if (temp.song.equals(song)) return temp;
            temp = temp.next;
        }
        return null;
    }

    public SongNode getHead() { return head; }
    public SongNode getTail() { return tail; }
    public SongNode getCurrent() { return current; }
    public void setCurrent(SongNode node) { this.current = node; }
    public int size() { return size; }

    public List<Song> toList() {
        List<Song> list = new ArrayList<>();
        SongNode temp = head;
        while (temp != null) {
            list.add(temp.song);
            temp = temp.next;
        }
        return list;
    }

    public SongNode setCurrentBySong(Song song) {
        SongNode temp = head;
        while (temp != null) {
            if (temp.song.equals(song)) {
                this.current = temp;
                return temp;
            }
            temp = temp.next;
        }
        return null;
    }
}

class HashEntry<K, V> {
    K key;
    V value;
    HashEntry<K, V> next;

    public HashEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }
}

class CustomHashMap<K, V> {
    private HashEntry<K, V>[] buckets;
    private int capacity = 32;
    private int size = 0;

    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        buckets = new HashEntry[capacity];
    }

    private int getBucketIndex(K key) {
        return Math.abs(key.hashCode() % capacity);
    }

    public synchronized void put(K key, V value) {
        int idx = getBucketIndex(key);
        HashEntry<K, V> head = buckets[idx];
        while (head != null) {
            if (head.key.equals(key)) {
                head.value = value;
                return;
            }
            head = head.next;
        }
        size++;
        head = buckets[idx];
        HashEntry<K, V> newNode = new HashEntry<>(key, value);
        newNode.next = head;
        buckets[idx] = newNode;
    }

    public synchronized V get(K key) {
        int idx = getBucketIndex(key);
        HashEntry<K, V> head = buckets[idx];
        while (head != null) {
            if (head.key.equals(key)) return head.value;
            head = head.next;
        }
        return null;
    }

    public int getCapacity() { return capacity; }
    public HashEntry<K, V>[] getBuckets() { return buckets; }
}

class SongSearchMap {
    private CustomHashMap<String, List<Song>> titleIndex = new CustomHashMap<>();
    private CustomHashMap<String, List<Song>> artistIndex = new CustomHashMap<>();
    private CustomHashMap<String, List<Song>> genreIndex = new CustomHashMap<>();

    public void indexSong(Song song) {
        indexTerm(titleIndex, song.title.toLowerCase().trim(), song);
        indexTerm(artistIndex, song.artist.toLowerCase().trim(), song);
        indexTerm(genreIndex, song.genre.toLowerCase().trim(), song);
    }

    private void indexTerm(CustomHashMap<String, List<Song>> map, String term, Song song) {
        List<Song> list = map.get(term);
        if (list == null) {
            list = new ArrayList<>();
            map.put(term, list);
        }
        if (!list.contains(song)) list.add(song);
    }

    public List<Song> search(String query, String filterType) {
        List<Song> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return results;
        query = query.toLowerCase().trim();

        if (filterType.equalsIgnoreCase("title")) {
            return prefixLookup(titleIndex, query);
        } else if (filterType.equalsIgnoreCase("artist")) {
            return prefixLookup(artistIndex, query);
        } else if (filterType.equalsIgnoreCase("genre")) {
            return prefixLookup(genreIndex, query);
        }
        return results;
    }

    private List<Song> prefixLookup(CustomHashMap<String, List<Song>> map, String query) {
        List<Song> results = new ArrayList<>();
        List<Song> exact = map.get(query);
        if (exact != null) results.addAll(exact);

        for (int i = 0; i < map.getCapacity(); i++) {
            HashEntry<String, List<Song>> entry = map.getBuckets()[i];
            while (entry != null) {
                if (entry.key.contains(query) && !entry.key.equals(query)) {
                    for (Song s : entry.value) {
                        if (!results.contains(s)) results.add(s);
                    }
                }
                entry = entry.next;
            }
        }
        return results;
    }
}

class SongRecommendationGraph {
    private CustomHashMap<Song, List<Song>> adjList = new CustomHashMap<>();
    private List<Song> allSongs = new ArrayList<>();

    public void addSong(Song song) {
        if (adjList.get(song) == null) {
            adjList.put(song, new ArrayList<>());
            allSongs.add(song);
        }
    }

    public void addConnection(Song s1, Song s2) {
        addSong(s1);
        addSong(s2);
        List<Song> neighbors1 = adjList.get(s1);
        if (!neighbors1.contains(s2)) neighbors1.add(s2);
        List<Song> neighbors2 = adjList.get(s2);
        if (!neighbors2.contains(s1)) neighbors2.add(s1);
    }

    public List<Song> getConnections(Song song) {
        List<Song> connections = adjList.get(song);
        return connections != null ? connections : new ArrayList<>();
    }

    public List<Song> getAllSongs() { return allSongs; }

    public List<Song> recommend(Song source, int maxRecommendations) {
        List<Song> recommendations = new ArrayList<>();
        if (source == null) return recommendations;

        Queue<Song> queue = new LinkedList<>();
        Set<Song> visited = new HashSet<>();

        queue.add(source);
        visited.add(source);

        while (!queue.isEmpty() && recommendations.size() < maxRecommendations) {
            Song current = queue.poll();
            List<Song> neighbors = getConnections(current);
            for (Song neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    recommendations.add(neighbor);
                    if (recommendations.size() >= maxRecommendations) break;
                }
            }
        }

        if (recommendations.size() < maxRecommendations) {
            for (Song s : allSongs) {
                if (!visited.contains(s) && s.genre.equalsIgnoreCase(source.genre)) {
                    visited.add(s);
                    recommendations.add(s);
                    if (recommendations.size() >= maxRecommendations) break;
                }
            }
        }

        if (recommendations.size() < maxRecommendations) {
            for (Song s : allSongs) {
                if (!visited.contains(s)) {
                    visited.add(s);
                    recommendations.add(s);
                    if (recommendations.size() >= maxRecommendations) break;
                }
            }
        }

        return recommendations;
    }
}

class StackNode {
    Song song;
    StackNode next;
    public StackNode(Song song) { this.song = song; }
}

class RecentlyPlayedStack {
    private StackNode top;
    private int size = 0;

    public synchronized void push(Song song) {
        remove(song);
        StackNode node = new StackNode(song);
        node.next = top;
        top = node;
        size++;
    }

    public synchronized Song pop() {
        if (top == null) return null;
        Song song = top.song;
        top = top.next;
        size--;
        return song;
    }

    private void remove(Song song) {
        if (top == null) return;
        if (top.song.equals(song)) {
            top = top.next;
            size--;
            return;
        }
        StackNode curr = top;
        while (curr.next != null) {
            if (curr.next.song.equals(song)) {
                curr.next = curr.next.next;
                size--;
                return;
            }
            curr = curr.next;
        }
    }

    public synchronized List<Song> getHistoryList() {
        List<Song> history = new ArrayList<>();
        StackNode temp = top;
        while (temp != null) {
            history.add(temp.song);
            temp = temp.next;
        }
        return history;
    }

    public synchronized void clear() {
        top = null;
        size = 0;
    }
}

// ==========================================
// 2. REAL AUDIO WAV FILE PLAYER ENGINE
// ==========================================

class SongWavPlayer {
    private Clip clip;
    private Thread playThread;
    private Song currentPlayingSong;
    private PlaybackListener listener;
    private javax.swing.Timer progressTimer;
    private boolean isPlaying = false;

    public interface PlaybackListener {
        void onProgress(int elapsedMs);
        void onSongFinished();
    }

    public SongWavPlayer(PlaybackListener listener) {
        this.listener = listener;
        progressTimer = new javax.swing.Timer(80, e -> {
            if (isPlaying && clip != null) {
                try {
                    int elapsed = (int) (clip.getMicrosecondPosition() / 1000);
                    if (listener != null) {
                        listener.onProgress(elapsed);
                    }
                    if (!clip.isRunning() && clip.getFramePosition() >= clip.getFrameLength()) {
                        stop();
                        if (listener != null) {
                            listener.onSongFinished();
                        }
                    }
                } catch (Exception ex) {}
            }
        });
    }

    public synchronized void play(Song song) {
        stop();
        currentPlayingSong = song;
        isPlaying = true;

        playThread = new Thread(() -> {
            try {
                File file = new File("songs/" + song.filename);
                if (!file.exists()) {
                    System.err.println("Wav file not found: " + file.getAbsolutePath());
                    return;
                }
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                
                SwingUtilities.invokeLater(() -> progressTimer.start());
            } catch (Exception e) {
                System.err.println("Error playing WAV: " + e.getMessage());
            }
        });
        playThread.setDaemon(true);
        playThread.start();
    }

    public synchronized void stop() {
        isPlaying = false;
        progressTimer.stop();
        if (clip != null) {
            try {
                clip.stop();
                clip.close();
            } catch (Exception e) {}
            clip = null;
        }
        if (playThread != null) {
            playThread.interrupt();
            playThread = null;
        }
        currentPlayingSong = null;
    }

    public boolean isPlaying() {
        return isPlaying && clip != null && clip.isRunning();
    }

    public Song getCurrentSong() {
        return currentPlayingSong;
    }

    public void setPositionPct(double pct) {
        if (clip != null) {
            long total = clip.getMicrosecondLength();
            long position = (long) (total * pct);
            clip.setMicrosecondPosition(position);
            if (listener != null) {
                listener.onProgress((int)(position / 1000));
            }
        }
    }
}

// ==========================================
// 3. UI COMPONENTS & GENERAL STYLING
// ==========================================

class CustomButton extends JButton {
    private Color hoverColor;
    private Color normalColor;
    private Color activeColor;

    public CustomButton(String text, Color baseColor) {
        super(text);
        this.normalColor = baseColor;
        this.hoverColor = adjustColor(baseColor, 20);
        this.activeColor = adjustColor(baseColor, -20);
        
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { setBackground(normalColor); }
            @Override
            public void mousePressed(MouseEvent e) { setBackground(activeColor); }
            @Override
            public void mouseReleased(MouseEvent e) { setBackground(hoverColor); }
        });
        setBackground(normalColor);
    }

    private Color adjustColor(Color c, int amount) {
        int r = Math.max(0, Math.min(255, c.getRed() + amount));
        int g = Math.max(0, Math.min(255, c.getGreen() + amount));
        int b = Math.max(0, Math.min(255, c.getBlue() + amount));
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
        
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        
        g2d.setColor(getForeground());
        g2d.drawString(getText(), x, y);
        g2d.dispose();
    }
}

class AudioVisualizerPanel extends JPanel {
    private float[] currentHeights = new float[16];
    private float[] targetHeights = new float[16];
    private boolean isPlaying = false;
    private Color neonColor1 = new Color(168, 85, 247);
    private Color neonColor2 = new Color(6, 182, 212);
    private javax.swing.Timer timer;

    public AudioVisualizerPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(150, 60));
        
        timer = new javax.swing.Timer(30, e -> {
            for (int i = 0; i < 16; i++) {
                if (isPlaying) {
                    float maxH;
                    if (i < 4) {
                        maxH = 40f;
                    } else if (i < 10) {
                        maxH = 30f;
                    } else {
                        maxH = 18f;
                    }
                    targetHeights[i] = (float)(Math.random() * maxH) + 4f;
                } else {
                    targetHeights[i] = 0f;
                }
                
                float diff = targetHeights[i] - currentHeights[i];
                if (diff < 0) {
                    currentHeights[i] += diff * 0.15f;
                } else {
                    currentHeights[i] += diff * 0.35f;
                }
            }
            repaint();
        });
        timer.start();
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        int barWidth = 6;
        int spacing = 3;
        int totalWidth = 16 * barWidth + 15 * spacing;
        int startX = (w - totalWidth) / 2;

        for (int i = 0; i < 16; i++) {
            int x = startX + i * (barWidth + spacing);
            float barH = currentHeights[i];
            float y = h - barH - 8;
            
            GradientPaint gp = new GradientPaint(x, y, neonColor1, x, y + barH, neonColor2);
            g2d.setPaint(gp);
            g2d.fill(new RoundRectangle2D.Float(x, y, barWidth, barH, 3, 3));
        }
        g2d.dispose();
    }
}

// ==========================================
// 4. MAIN INTERACTIVE GRAPH PANEL
// ==========================================

class GraphVisualizerPanel extends JPanel {
    private SongRecommendationGraph graph;
    private Map<Song, Point2D.Double> positions = new HashMap<>();
    private Song selectedSong;
    private Song currentPlayingSong;
    private Song draggedSong;
    private SocialMusicApp parent;
    
    private Color nodeBg = new Color(30, 30, 30);
    private Color nodeBorder = new Color(80, 80, 80);
    private Color borderPlaying = new Color(16, 185, 129);
    private Color borderSelected = new Color(6, 182, 212);
    
    private double animationTime = 0.0;
    private javax.swing.Timer animationTimer;
    
    public GraphVisualizerPanel(SongRecommendationGraph graph, SocialMusicApp parent) {
        this.graph = graph;
        this.parent = parent;
        setBackground(new Color(18, 18, 18));
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                initializeCoordinates();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                draggedSong = null;
                Point p = e.getPoint();
                for (Map.Entry<Song, Point2D.Double> entry : positions.entrySet()) {
                    Point2D.Double nodePos = entry.getValue();
                    if (p.distance(nodePos.x, nodePos.y) < 28) {
                        draggedSong = entry.getKey();
                        selectedSong = draggedSong;
                        parent.updateRecommendationSelection(selectedSong);
                        repaint();
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedSong = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedSong != null) {
                    Point2D.Double pos = positions.get(draggedSong);
                    if (pos != null) {
                        pos.x = Math.max(30, Math.min(getWidth() - 30, e.getX()));
                        pos.y = Math.max(30, Math.min(getHeight() - 30, e.getY()));
                        repaint();
                    }
                }
            }
        });
        
        animationTimer = new javax.swing.Timer(30, e -> {
            animationTime += 0.008;
            if (animationTime > 1.0) {
                animationTime = 0.0;
            }
            repaint();
        });
        animationTimer.start();
    }

    public synchronized void initializeCoordinates() {
        positions.clear();
        List<Song> songs = graph.getAllSongs();
        if (songs.isEmpty()) return;

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        if (cx == 0 || cy == 0) {
            cx = 300; cy = 250;
        }

        int count = songs.size();
        double r = Math.max(160.0, count * 12.0);
        for (int i = 0; i < count; i++) {
            Song song = songs.get(i);
            double angle = (2.0 * Math.PI * i) / count;
            double px = cx + r * Math.cos(angle);
            double py = cy + r * Math.sin(angle);
            positions.put(song, new Point2D.Double(px, py));
        }
        repaint();
    }

    public void updateGraphState(Song selected, Song playing) {
        this.selectedSong = selected;
        this.currentPlayingSong = playing;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Draw connections (lines with glow effects)
        Set<String> drawnEdges = new HashSet<>();
        
        for (Song song : graph.getAllSongs()) {
            Point2D.Double p1 = positions.get(song);
            if (p1 == null) continue;

            for (Song neighbor : graph.getConnections(song)) {
                Point2D.Double p2 = positions.get(neighbor);
                if (p2 == null) continue;

                String edgeKey = song.id < neighbor.id ? song.id + "-" + neighbor.id : neighbor.id + "-" + song.id;
                if (drawnEdges.contains(edgeKey)) continue;
                drawnEdges.add(edgeKey);

                // Glow Underlay
                g2d.setStroke(new BasicStroke(6.0f));
                if (song.equals(currentPlayingSong) || neighbor.equals(currentPlayingSong)) {
                    g2d.setColor(new Color(16, 185, 129, 45));
                } else if (song.equals(selectedSong) || neighbor.equals(selectedSong)) {
                    g2d.setColor(new Color(6, 182, 212, 45));
                } else {
                    g2d.setColor(new Color(138, 43, 226, 20));
                }
                g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));

                // Solid Line Overlay
                g2d.setStroke(new BasicStroke(2.0f));
                if (song.equals(currentPlayingSong) || neighbor.equals(currentPlayingSong)) {
                    g2d.setColor(new Color(16, 185, 129, 220));
                } else if (song.equals(selectedSong) || neighbor.equals(selectedSong)) {
                    g2d.setColor(new Color(6, 182, 212, 220));
                } else {
                    g2d.setColor(new Color(138, 43, 226, 100));
                }
                g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));

                // Flowing particles along edges
                double t = (animationTime + (song.id * 0.17)) % 1.0;
                double px = p1.x * (1.0 - t) + p2.x * t;
                double py = p1.y * (1.0 - t) + p2.y * t;
                
                if (song.equals(currentPlayingSong) || neighbor.equals(currentPlayingSong)) {
                    g2d.setColor(new Color(52, 211, 153));
                    g2d.fill(new Ellipse2D.Double(px - 3.5, py - 3.5, 7, 7));
                } else if (song.equals(selectedSong) || neighbor.equals(selectedSong)) {
                    g2d.setColor(new Color(34, 211, 238));
                    g2d.fill(new Ellipse2D.Double(px - 3, py - 3, 6, 6));
                } else {
                    g2d.setColor(new Color(192, 132, 252));
                    g2d.fill(new Ellipse2D.Double(px - 2.5, py - 2.5, 5, 5));
                }
            }
        }

        // 2. Draw vertices (circles with pulsing ripple waves)
        int r = 26;
        for (Song song : graph.getAllSongs()) {
            Point2D.Double pos = positions.get(song);
            if (pos == null) continue;

            if (song.equals(currentPlayingSong)) {
                // Outer Ripple 1
                double pulse1 = (animationTime * 40.0);
                float alpha1 = 1.0f - (float)(pulse1 / 40.0);
                g2d.setColor(new Color(16, 185, 129, (int)(alpha1 * 130)));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.draw(new Ellipse2D.Double(pos.x - r - pulse1, pos.y - r - pulse1, (r + pulse1)*2, (r + pulse1)*2));
                
                // Outer Ripple 2
                double pulse2 = ((animationTime + 0.5) % 1.0) * 40.0;
                float alpha2 = 1.0f - (float)(pulse2 / 40.0);
                g2d.setColor(new Color(16, 185, 129, (int)(alpha2 * 130)));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new Ellipse2D.Double(pos.x - r - pulse2, pos.y - r - pulse2, (r + pulse2)*2, (r + pulse2)*2));

                RadialGradientPaint rp = new RadialGradientPaint((float)pos.x, (float)pos.y, 50f, new float[]{0.0f, 1.0f}, new Color[]{new Color(16, 185, 129, 70), new Color(16, 185, 129, 0)});
                g2d.setPaint(rp);
                g2d.fill(new Ellipse2D.Double(pos.x - 50, pos.y - 50, 100, 100));
            } else if (song.equals(selectedSong)) {
                double pulse = (animationTime * 30.0);
                float alpha = 1.0f - (float)(pulse / 30.0);
                g2d.setColor(new Color(6, 182, 212, (int)(alpha * 120)));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new Ellipse2D.Double(pos.x - r - pulse, pos.y - r - pulse, (r + pulse)*2, (r + pulse)*2));

                RadialGradientPaint rp = new RadialGradientPaint((float)pos.x, (float)pos.y, 45f, new float[]{0.0f, 1.0f}, new Color[]{new Color(6, 182, 212, 70), new Color(6, 182, 212, 0)});
                g2d.setPaint(rp);
                g2d.fill(new Ellipse2D.Double(pos.x - 45, pos.y - 45, 90, 90));
            }

            g2d.setColor(nodeBg);
            g2d.fill(new Ellipse2D.Double(pos.x - r, pos.y - r, r * 2, r * 2));

            if (song.equals(currentPlayingSong)) {
                g2d.setColor(borderPlaying);
                g2d.setStroke(new BasicStroke(3.5f));
            } else if (song.equals(selectedSong)) {
                g2d.setColor(borderSelected);
                g2d.setStroke(new BasicStroke(3f));
            } else {
                g2d.setColor(nodeBorder);
                g2d.setStroke(new BasicStroke(2f));
            }
            g2d.draw(new Ellipse2D.Double(pos.x - r, pos.y - r, r * 2, r * 2));

            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (song.equals(currentPlayingSong)) g2d.setColor(borderPlaying);
            else if (song.equals(selectedSong)) g2d.setColor(borderSelected);
            else g2d.setColor(Color.LIGHT_GRAY);

            String initial = song.genre.substring(0, Math.min(2, song.genre.length())).toUpperCase();
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(initial, (float)(pos.x - fm.stringWidth(initial)/2.0), (float)(pos.y + fm.getAscent()/2.0 - 2));

            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2d.setColor(Color.WHITE);
            String titleText = song.title;
            int labelW = g2d.getFontMetrics().stringWidth(titleText);
            g2d.drawString(titleText, (float)(pos.x - labelW/2.0), (float)(pos.y + r + 15));
            
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2d.setColor(Color.GRAY);
            String artistText = song.artist;
            int artW = g2d.getFontMetrics().stringWidth(artistText);
            g2d.drawString(artistText, (float)(pos.x - artW/2.0), (float)(pos.y + r + 25));
        }

        g2d.dispose();
    }
}

class ModernScrollBarUI extends BasicScrollBarUI {
    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        // Track is transparent
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(80, 80, 80, 120));
        g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
        g2.dispose();
    }
}

// ==========================================
// 5. MAIN SWING WINDOW APPLICATION
// ==========================================

public class SocialMusicApp extends JFrame implements SongWavPlayer.PlaybackListener {
    private final Color COLOR_BG = new Color(18, 18, 18);
    private final Color COLOR_PANEL = new Color(24, 24, 24);
    private final Color COLOR_SIDEBAR = new Color(12, 12, 12);
    private final Color COLOR_TEXT_MAIN = Color.WHITE;
    private final Color COLOR_TEXT_MUTED = new Color(167, 167, 167);
    
    private final Color COLOR_ACCENT = new Color(168, 85, 247);
    private final Color COLOR_ACCENT_CYAN = new Color(6, 182, 212);
    private final Color COLOR_PLAYING = new Color(16, 185, 129);

    private void styleScrollPane(JScrollPane sp) {
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getHorizontalScrollBar().setOpaque(false);
        sp.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));
    }

    private List<Song> catalog = new ArrayList<>();

    // Custom Data Structures
    private PlaylistDLL activePlaylist = new PlaylistDLL();
    private SongSearchMap searchEngine = new SongSearchMap();
    private SongRecommendationGraph recGraph = new SongRecommendationGraph();
    private RecentlyPlayedStack playStack = new RecentlyPlayedStack();

    // WAV Audio engine
    private SongWavPlayer player;
    private boolean isTimelineAdjusting = false;

    // GUI Components
    private CardLayout mainCardLayout;
    private JPanel mainContentPane;
    private JLabel currentSongTitleLabel, currentSongArtistLabel;
    private JSlider songProgressSlider;
    private JLabel timeElapsedLabel, timeRemainingLabel;
    private JButton playPauseButton;
    private AudioVisualizerPanel visualizerPanel;

    // View panels
    private JLabel dashboardGreeting;
    private JLabel dashboardSelectTitle, dashboardSelectArtist, dashboardSelectGenre, dashboardSelectPlays, dashboardSelectLikes;
    private Song selectedDashboardSong;
    private JButton dashboardPlayBtn;

    private JTable playlistTable;
    private DefaultTableModel playlistTableModel;

    private JTextField searchBar;
    private JRadioButton radioTitle, radioArtist, radioGenre;
    private DefaultListModel<Song> searchResultsModel;
    private JList<Song> searchResultsList;

    private GraphVisualizerPanel graphCanvas;
    private JLabel recSelTitle, recSelArtist, recSelGenre, recSelLikes;
    private Song selectedGraphSong;
    private JPanel recResultsPanel;

    private DefaultTableModel historyTableModel;
    private JTable historyTable;

    public SocialMusicApp() {
        super("Social Music Playlist & Recommendation System");
        setSize(1150, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(COLOR_BG);

        // Load procedural database catalogs & indices
        initCatalogData();

        // Load custom application icon
        try {
            setIconImage(createMusicIcon());
        } catch (Exception e) {}

        // Startup Audio Player (WAV)
        player = new SongWavPlayer(this);

        // Setup Main Frame UI layout
        JPanel masterPanel = new JPanel(new BorderLayout());
        masterPanel.setBackground(COLOR_BG);
        setContentPane(masterPanel);

        // Sidebar Setup
        JPanel sidebar = createSidebarPanel();
        masterPanel.add(sidebar, BorderLayout.WEST);

        // Main Panel
        mainCardLayout = new CardLayout();
        mainContentPane = new JPanel(mainCardLayout);
        mainContentPane.setBackground(COLOR_BG);
        masterPanel.add(mainContentPane, BorderLayout.CENTER);

        // Load Sub views
        mainContentPane.add(createDashboardPanel(), "DASHBOARD");
        mainContentPane.add(createPlaylistPanel(), "PLAYLIST");
        mainContentPane.add(createSearchPanel(), "SEARCH");
        mainContentPane.add(createRecommendationsPanel(), "RECOMMENDATIONS");
        mainContentPane.add(createHistoryPanel(), "HISTORY");

        // Player Controls Bar (Bottom)
        JPanel bottomBar = createBottomPlayerBar();
        masterPanel.add(bottomBar, BorderLayout.SOUTH);

        // Selection default song
        if (catalog.size() > 0) {
            selectDashboardSong(catalog.get(0));
            selectedGraphSong = catalog.get(0);
            updateRecommendationSelection(selectedGraphSong);
        }

        setVisible(true);
    }

    private void initCatalogData() {
        // Define 13 tracks mapped to 13 completely different physical WAV files
        catalog.add(new Song(1, "Summer Beat Loop", "Synth Master", "Pop", "sample-3s.wav", "0:03"));
        catalog.add(new Song(2, "Synthwave Rhythm", "Retro Wave", "Rock", "sample-6s.wav", "0:06"));
        catalog.add(new Song(3, "Jazz Piano Loop", "Dave Brubeck", "Jazz", "sample-9s.wav", "0:09"));
        catalog.add(new Song(4, "Classical Strings", "Pachelbel", "Classical", "sample-12s.wav", "0:12"));
        catalog.add(new Song(5, "Lofi Chill Beats", "Lofi Girl", "Lofi", "sample-15s.wav", "0:15"));
        catalog.add(new Song(6, "Acoustic Guitar", "Gary Clark", "Country", "gc.wav", "0:28"));
        catalog.add(new Song(7, "Synth Bass Arp", "Stellar Noise", "Electronic", "synth.wav", "0:11"));
        catalog.add(new Song(8, "Vocal Acapella", "Voice Choir", "Vocal", "voice.wav", "0:05"));
        catalog.add(new Song(9, "Whisper Message", "Ambient Echo", "Ambient", "voice-note.wav", "0:01"));
        catalog.add(new Song(10, "White Noise Generator", "Relaxing Frequency", "Ambient", "noise.wav", "0:32"));
        catalog.add(new Song(11, "Sine Wave Tone", "Lab Oscillator", "Electronic", "sine.wav", "0:01"));
        catalog.add(new Song(12, "Overdrive Solo", "Guitar Hero", "Rock", "overdrive.wav", "0:01"));
        catalog.add(new Song(13, "Car Horn Sound", "Traffic Jam", "Urban", "car-horn.wav", "0:06"));

        // Index in Custom Hash Map search structure
        for (Song s : catalog) {
            searchEngine.indexSong(s);
        }

        // Add to Graph recommendations engine
        for (Song s : catalog) {
            recGraph.addSong(s);
        }

        // Connect the 15 songs in an interconnected ring with crossover connections
        for (int i = 0; i < catalog.size(); i++) {
            Song current = catalog.get(i);
            // Connect to next song (ring structure)
            Song next = catalog.get((i + 1) % catalog.size());
            recGraph.addConnection(current, next);
            
            // Crossover: Connect to the song 3 steps ahead
            Song step3 = catalog.get((i + 3) % catalog.size());
            recGraph.addConnection(current, step3);
            
            // Crossover: Connect to the first song of the same genre
            for (int j = i + 1; j < catalog.size(); j++) {
                Song other = catalog.get(j);
                if (current.genre.equals(other.genre)) {
                    recGraph.addConnection(current, other);
                    break;
                }
            }
        }

        // Prepopulate active playlist DLL with default tracks
        activePlaylist.add(catalog.get(0));
        activePlaylist.add(catalog.get(2));
        activePlaylist.add(catalog.get(4));
        activePlaylist.add(catalog.get(6));
        activePlaylist.add(catalog.get(8));
    }

    private Image createMusicIcon() {
        int size = 64;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(COLOR_ACCENT);
        g2d.fillOval(15, 35, 18, 15);
        g2d.fillOval(38, 30, 18, 15);
        g2d.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(30, 42, 30, 15);
        g2d.drawLine(53, 37, 53, 10);
        g2d.drawLine(30, 15, 53, 10);
        g2d.dispose();
        return img;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(35, 35, 35)));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 25));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("🎵  SocialMusic");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        titlePanel.add(titleLabel);
        sidebar.add(titlePanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        Border activeBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, COLOR_ACCENT),
            BorderFactory.createEmptyBorder(12, 10, 12, 15)
        );
        Border inactiveBorder = BorderFactory.createEmptyBorder(12, 15, 12, 15);

        String[] navItems = {"Dashboard", "My Playlist", "Search Library", "Recommendation Graph", "Played History"};
        String[] codes = {"DASHBOARD", "PLAYLIST", "SEARCH", "RECOMMENDATIONS", "HISTORY"};
        
        JButton[] buttons = new JButton[navItems.length];

        for (int i = 0; i < navItems.length; i++) {
            final String targetCode = codes[i];
            final int index = i;
            buttons[i] = new JButton(navItems[i]);
            buttons[i].setFont(new Font("Segoe UI", Font.BOLD, 14));
            buttons[i].setForeground(COLOR_TEXT_MUTED);
            buttons[i].setBackground(COLOR_SIDEBAR);
            buttons[i].setContentAreaFilled(false);
            buttons[i].setFocusPainted(false);
            buttons[i].setBorder(inactiveBorder);
            buttons[i].setHorizontalAlignment(SwingConstants.LEFT);
            buttons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            buttons[i].setMaximumSize(new Dimension(210, 45));
            buttons[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            buttons[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (buttons[index].getForeground() != COLOR_ACCENT) {
                        buttons[index].setForeground(Color.WHITE);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (buttons[index].getForeground() != COLOR_ACCENT) {
                        buttons[index].setForeground(COLOR_TEXT_MUTED);
                    }
                }
            });

            buttons[i].addActionListener(e -> {
                for (JButton btn : buttons) {
                    btn.setForeground(COLOR_TEXT_MUTED);
                    btn.setBorder(inactiveBorder);
                }
                buttons[index].setForeground(COLOR_ACCENT);
                buttons[index].setBorder(activeBorder);
                mainCardLayout.show(mainContentPane, targetCode);
                
                if (targetCode.equals("PLAYLIST")) {
                    refreshPlaylistTable();
                } else if (targetCode.equals("HISTORY")) {
                    refreshHistoryTable();
                } else if (targetCode.equals("RECOMMENDATIONS")) {
                    graphCanvas.initializeCoordinates();
                    if (player.getCurrentSong() != null) {
                        graphCanvas.updateGraphState(selectedGraphSong, player.getCurrentSong());
                    }
                }
            });

            menuPanel.add(buttons[i]);
            menuPanel.add(Box.createVerticalStrut(10));
        }

        buttons[0].setForeground(COLOR_ACCENT);
        buttons[0].setBorder(activeBorder);
        sidebar.add(menuPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        JLabel footerLabel = new JLabel("© 2026 SocialMusic App");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(90, 90, 90));
        footerPanel.add(footerLabel);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String salutation = "Good Evening";
        if (hour < 12) salutation = "Good Morning";
        else if (hour < 17) salutation = "Good Afternoon";
        
        dashboardGreeting = new JLabel(salutation + ", Explorer 🎵");
        dashboardGreeting.setFont(new Font("Segoe UI", Font.BOLD, 28));
        dashboardGreeting.setForeground(COLOR_TEXT_MAIN);
        headerPanel.add(dashboardGreeting);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 25, 0));
        centerGrid.setOpaque(false);
        centerGrid.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        panel.add(centerGrid, BorderLayout.CENTER);

        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setOpaque(false);
        
        JPanel songCard = new JPanel();
        songCard.setPreferredSize(new Dimension(380, 420));
        songCard.setBackground(COLOR_PANEL);
        songCard.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true));
        songCard.setLayout(new BorderLayout());

        JPanel cardArt = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_ACCENT, getWidth(), getHeight(), COLOR_ACCENT_CYAN);
                g2d.setPaint(gp);
                g2d.fill(new RoundRectangle2D.Float(15, 15, getWidth()-30, getHeight()-30, 15, 15));
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 54));
                String initial = (selectedDashboardSong != null) ? selectedDashboardSong.title.substring(0,1) : "🎵";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initial)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(initial, x, y);
                g2d.dispose();
            }
        };
        cardArt.setPreferredSize(new Dimension(380, 220));
        cardArt.setOpaque(false);
        songCard.add(cardArt, BorderLayout.NORTH);

        JPanel cardDetails = new JPanel();
        cardDetails.setBackground(COLOR_PANEL);
        cardDetails.setLayout(new BoxLayout(cardDetails, BoxLayout.Y_AXIS));
        cardDetails.setBorder(BorderFactory.createEmptyBorder(15, 25, 20, 25));

        dashboardSelectTitle = new JLabel("Song Title");
        dashboardSelectTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        dashboardSelectTitle.setForeground(COLOR_TEXT_MAIN);
        dashboardSelectTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        dashboardSelectArtist = new JLabel("Artist Name");
        dashboardSelectArtist.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        dashboardSelectArtist.setForeground(COLOR_TEXT_MUTED);
        dashboardSelectArtist.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        metaRow.setOpaque(false);
        
        dashboardSelectGenre = new JLabel("Genre");
        dashboardSelectGenre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dashboardSelectGenre.setForeground(COLOR_ACCENT_CYAN);
        
        dashboardSelectPlays = new JLabel("Plays: 0");
        dashboardSelectPlays.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dashboardSelectPlays.setForeground(COLOR_TEXT_MUTED);

        dashboardSelectLikes = new JLabel("Likes: 0");
        dashboardSelectLikes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dashboardSelectLikes.setForeground(COLOR_TEXT_MUTED);

        metaRow.add(dashboardSelectGenre);
        metaRow.add(new JLabel("•")).setForeground(Color.DARK_GRAY);
        metaRow.add(dashboardSelectPlays);
        metaRow.add(new JLabel("•")).setForeground(Color.DARK_GRAY);
        metaRow.add(dashboardSelectLikes);
        metaRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        dashboardPlayBtn = new CustomButton("PLAY NOW", COLOR_ACCENT);
        dashboardPlayBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        dashboardPlayBtn.setPreferredSize(new Dimension(160, 40));
        dashboardPlayBtn.setMaximumSize(new Dimension(160, 40));
        dashboardPlayBtn.addActionListener(e -> {
            if (selectedDashboardSong != null) {
                playDirectSong(selectedDashboardSong);
            }
        });

        cardDetails.add(dashboardSelectTitle);
        cardDetails.add(Box.createVerticalStrut(5));
        cardDetails.add(dashboardSelectArtist);
        cardDetails.add(Box.createVerticalStrut(12));
        cardDetails.add(metaRow);
        cardDetails.add(Box.createVerticalStrut(18));
        cardDetails.add(dashboardPlayBtn);

        songCard.add(cardDetails, BorderLayout.CENTER);
        cardWrapper.add(songCard);
        centerGrid.add(cardWrapper);

        JPanel catalogListPanel = new JPanel(new BorderLayout());
        catalogListPanel.setOpaque(false);

        JLabel catLabel = new JLabel("Music Library Catalog");
        catLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        catLabel.setForeground(COLOR_TEXT_MAIN);
        catLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        catalogListPanel.add(catLabel, BorderLayout.NORTH);

        DefaultListModel<Song> catModel = new DefaultListModel<>();
        for (Song s : catalog) catModel.addElement(s);
        
        JList<Song> catList = new JList<>(catModel);
        catList.setBackground(COLOR_PANEL);
        catList.setForeground(COLOR_TEXT_MAIN);
        catList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        catList.setSelectionBackground(new Color(60, 60, 60));
        catList.setSelectionForeground(COLOR_TEXT_MAIN);
        catList.setFixedCellHeight(40);
        catList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        catList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && catList.getSelectedValue() != null) {
                selectDashboardSong(catList.getSelectedValue());
                cardArt.repaint();
            }
        });

        JScrollPane scrollPane = new JScrollPane(catList);
        styleScrollPane(scrollPane);
        catalogListPanel.add(scrollPane, BorderLayout.CENTER);

        JButton quickAddBtn = new CustomButton("ADD TO PLAYLIST", new Color(40, 40, 40));
        quickAddBtn.addActionListener(e -> {
            Song selected = catList.getSelectedValue();
            if (selected != null) {
                activePlaylist.add(selected);
                JOptionPane.showMessageDialog(this, "\"" + selected.title + "\" added to active playlist!", "Added", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        actionRow.setOpaque(false);
        actionRow.add(quickAddBtn);
        catalogListPanel.add(actionRow, BorderLayout.SOUTH);

        centerGrid.add(catalogListPanel);

        return panel;
    }

    private void selectDashboardSong(Song s) {
        selectedDashboardSong = s;
        dashboardSelectTitle.setText(s.title);
        dashboardSelectArtist.setText(s.artist);
        dashboardSelectGenre.setText(s.genre);
        dashboardSelectPlays.setText("Plays: " + s.plays);
        dashboardSelectLikes.setText("Likes: " + s.likes);
    }

    private JPanel createPlaylistPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Current Playlist");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        topRow.add(titleLabel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JButton playBtn = new CustomButton("Play Selected", COLOR_PLAYING);
        JButton moveUpBtn = new CustomButton("▲ Move Up", COLOR_ACCENT);
        JButton moveDownBtn = new CustomButton("▼ Move Down", COLOR_ACCENT);
        JButton removeBtn = new CustomButton("Remove", new Color(220, 53, 69));

        controls.add(playBtn);
        controls.add(moveUpBtn);
        controls.add(moveDownBtn);
        controls.add(removeBtn);
        topRow.add(controls, BorderLayout.EAST);
        panel.add(topRow, BorderLayout.NORTH);

        playlistTableModel = new DefaultTableModel(new Object[]{"#", "Title", "Artist", "Genre", "Duration"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        playlistTable = new JTable(playlistTableModel);
        styleDarkTable(playlistTable);

        JScrollPane scrollPane = new JScrollPane(playlistTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel addTrackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        addTrackPanel.setOpaque(false);
        addTrackPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)), "Link and Insert Local WAV Track", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), COLOR_TEXT_MUTED));
        
        JTextField fTitle = new JTextField(12);
        JTextField fArtist = new JTextField(12);
        JTextField fGenre = new JTextField(8);
        JComboBox<String> fFile = new JComboBox<>(new String[]{
            "sample-3s.wav", "sample-6s.wav", "sample-9s.wav", "sample-12s.wav", "sample-15s.wav",
            "gc.wav", "synth.wav", "voice.wav", "voice-note.wav", "noise.wav", "sine.wav",
            "overdrive.wav", "car-horn.wav"
        });

        styleInputField(fTitle);
        styleInputField(fArtist);
        styleInputField(fGenre);
        styleComboBox(fFile);

        addTrackPanel.add(new JLabel("Title:")).setForeground(COLOR_TEXT_MAIN);
        addTrackPanel.add(fTitle);
        addTrackPanel.add(new JLabel("Artist:")).setForeground(COLOR_TEXT_MAIN);
        addTrackPanel.add(fArtist);
        addTrackPanel.add(new JLabel("Genre:")).setForeground(COLOR_TEXT_MAIN);
        addTrackPanel.add(fGenre);
        addTrackPanel.add(new JLabel("WAV Filename:")).setForeground(COLOR_TEXT_MAIN);
        addTrackPanel.add(fFile);

        JButton addBtn = new CustomButton("Add Song", COLOR_ACCENT);
        addTrackPanel.add(addBtn);
        panel.add(addTrackPanel, BorderLayout.SOUTH);

        playBtn.addActionListener(e -> {
            int row = playlistTable.getSelectedRow();
            if (row != -1) {
                Song song = activePlaylist.toList().get(row);
                playPlaylistSong(song);
            }
        });

        moveUpBtn.addActionListener(e -> {
            int row = playlistTable.getSelectedRow();
            if (row > 0) {
                Song song = activePlaylist.toList().get(row);
                activePlaylist.moveUp(song);
                refreshPlaylistTable();
                playlistTable.setRowSelectionInterval(row - 1, row - 1);
            }
        });

        moveDownBtn.addActionListener(e -> {
            int row = playlistTable.getSelectedRow();
            if (row != -1 && row < activePlaylist.size() - 1) {
                Song song = activePlaylist.toList().get(row);
                activePlaylist.moveDown(song);
                refreshPlaylistTable();
                playlistTable.setRowSelectionInterval(row + 1, row + 1);
            }
        });

        removeBtn.addActionListener(e -> {
            int row = playlistTable.getSelectedRow();
            if (row != -1) {
                Song song = activePlaylist.toList().get(row);
                activePlaylist.remove(song);
                refreshPlaylistTable();
                if (activePlaylist.size() > 0) {
                    int nextSel = Math.min(row, activePlaylist.size() - 1);
                    playlistTable.setRowSelectionInterval(nextSel, nextSel);
                }
            }
        });

        addBtn.addActionListener(e -> {
            String title = fTitle.getText().trim();
            String artist = fArtist.getText().trim();
            String genre = fGenre.getText().trim();
            String filename = (String) fFile.getSelectedItem();

            if (title.isEmpty() || artist.isEmpty() || genre.isEmpty() || filename == null || filename.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please populate all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File fileCheck = new File("songs/" + filename);
            if (!fileCheck.exists()) {
                JOptionPane.showMessageDialog(this, "The file 'songs/" + filename + "' does not exist. Please place the file in the songs folder.", "WAV File Not Found", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Detect duration programmatically or set default
            String duration = "0:05";
            try {
                AudioInputStream stream = AudioSystem.getAudioInputStream(fileCheck);
                AudioFormat format = stream.getFormat();
                long frames = stream.getFrameLength();
                double durationInSeconds = (double) frames / format.getFrameRate();
                int min = (int) (durationInSeconds / 60);
                int sec = (int) (durationInSeconds % 60);
                duration = String.format("%d:%02d", min, sec);
                stream.close();
            } catch (Exception ex) {}

            Song newSong = new Song(catalog.size() + 1, title, artist, genre, filename, duration);
            
            catalog.add(newSong);
            searchEngine.indexSong(newSong);
            recGraph.addSong(newSong);

            // Connect to other matching genres
            for (Song s : catalog) {
                if (s.genre.equalsIgnoreCase(newSong.genre) && s.id != newSong.id) {
                    recGraph.addConnection(s, newSong);
                }
            }

            activePlaylist.add(newSong);
            
            fTitle.setText("");
            fArtist.setText("");
            fGenre.setText("");
            fFile.setSelectedIndex(0);
            refreshPlaylistTable();
            
            int newRow = activePlaylist.size() - 1;
            playlistTable.setRowSelectionInterval(newRow, newRow);
        });

        return panel;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(COLOR_PANEL);
        combo.setForeground(COLOR_TEXT_MAIN);
        combo.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private void styleInputField(JTextField f) {
        f.setBackground(COLOR_PANEL);
        f.setForeground(COLOR_TEXT_MAIN);
        f.setCaretColor(COLOR_TEXT_MAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void refreshPlaylistTable() {
        playlistTableModel.setRowCount(0);
        List<Song> songs = activePlaylist.toList();
        Song current = (player.getCurrentSong() != null) ? player.getCurrentSong() : null;

        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            String indexStr = (current != null && s.equals(current)) ? "▶  " + (i + 1) : String.valueOf(i + 1);
            playlistTableModel.addRow(new Object[]{indexStr, s.title, s.artist, s.genre, s.duration});
        }
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Lookup Song Database");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchBarPanel = new JPanel(new BorderLayout(15, 0));
        searchBarPanel.setOpaque(false);
        searchBarPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        searchBar = new JTextField();
        searchBar.setBackground(COLOR_PANEL);
        searchBar.setForeground(COLOR_TEXT_MAIN);
        searchBar.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchBar.setCaretColor(COLOR_TEXT_MAIN);
        searchBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        searchBarPanel.add(searchBar, BorderLayout.CENTER);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        radioPanel.setOpaque(false);

        radioTitle = new JRadioButton("Search Title", true);
        radioArtist = new JRadioButton("Search Artist");
        radioGenre = new JRadioButton("Search Genre");

        ButtonGroup group = new ButtonGroup();
        group.add(radioTitle);
        group.add(radioArtist);
        group.add(radioGenre);

        styleRadioButton(radioTitle);
        styleRadioButton(radioArtist);
        styleRadioButton(radioGenre);

        radioPanel.add(radioTitle);
        radioPanel.add(radioArtist);
        radioPanel.add(radioGenre);
        searchBarPanel.add(radioPanel, BorderLayout.SOUTH);

        topPanel.add(searchBarPanel, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);

        searchResultsModel = new DefaultListModel<>();
        searchResultsList = new JList<>(searchResultsModel);
        searchResultsList.setBackground(COLOR_PANEL);
        searchResultsList.setForeground(COLOR_TEXT_MAIN);
        searchResultsList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchResultsList.setSelectionBackground(new Color(60, 60, 60));
        searchResultsList.setSelectionForeground(COLOR_TEXT_MAIN);
        searchResultsList.setFixedCellHeight(45);
        searchResultsList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(searchResultsList);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomActionPanel.setOpaque(false);
        
        JButton btnPlay = new CustomButton("Play Track", COLOR_PLAYING);
        JButton btnAdd = new CustomButton("Add to Playlist", COLOR_ACCENT);

        bottomActionPanel.add(btnPlay);
        bottomActionPanel.add(btnAdd);
        panel.add(bottomActionPanel, BorderLayout.SOUTH);

        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                runHashMapSearch();
            }
        });

        ActionListener filterTrigger = e -> runHashMapSearch();
        radioTitle.addActionListener(filterTrigger);
        radioArtist.addActionListener(filterTrigger);
        radioGenre.addActionListener(filterTrigger);

        btnPlay.addActionListener(e -> {
            Song selected = searchResultsList.getSelectedValue();
            if (selected != null) {
                playDirectSong(selected);
            }
        });

        btnAdd.addActionListener(e -> {
            Song selected = searchResultsList.getSelectedValue();
            if (selected != null) {
                activePlaylist.add(selected);
                JOptionPane.showMessageDialog(this, "\"" + selected.title + "\" added to active playlist!", "Added", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Run default load
        runHashMapSearch();

        return panel;
    }

    private void styleRadioButton(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setForeground(COLOR_TEXT_MUTED);
        rb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rb.setFocusPainted(false);
    }

    private void runHashMapSearch() {
        String query = searchBar.getText().trim();
        searchResultsModel.clear();

        if (query.isEmpty()) {
            for (Song s : catalog) searchResultsModel.addElement(s);
            return;
        }

        String filterType = "title";
        if (radioArtist.isSelected()) filterType = "artist";
        else if (radioGenre.isSelected()) filterType = "genre";

        List<Song> results = searchEngine.search(query, filterType);
        for (Song s : results) {
            searchResultsModel.addElement(s);
        }
    }

    private JPanel createRecommendationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);

        graphCanvas = new GraphVisualizerPanel(recGraph, this);
        panel.add(graphCanvas, BorderLayout.CENTER);

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(300, getHeight()));
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(45, 45, 45)));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        JLabel sideTitle = new JLabel("Song Relationship Card");
        sideTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sideTitle.setForeground(COLOR_TEXT_MAIN);
        sideTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sideTitle);
        sidebar.add(Box.createVerticalStrut(20));

        recSelTitle = new JLabel("No Selection");
        recSelTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        recSelTitle.setForeground(COLOR_TEXT_MAIN);
        recSelTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        recSelArtist = new JLabel("Artist Name");
        recSelArtist.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recSelArtist.setForeground(COLOR_TEXT_MUTED);
        recSelArtist.setAlignmentX(Component.LEFT_ALIGNMENT);

        recSelGenre = new JLabel("Genre");
        recSelGenre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        recSelGenre.setForeground(COLOR_ACCENT_CYAN);
        recSelGenre.setAlignmentX(Component.LEFT_ALIGNMENT);

        recSelLikes = new JLabel("Likes: 0");
        recSelLikes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recSelLikes.setForeground(COLOR_TEXT_MUTED);
        recSelLikes.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(recSelTitle);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(recSelArtist);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(recSelGenre);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(recSelLikes);
        
        sidebar.add(Box.createVerticalStrut(25));

        JButton playNow = new CustomButton("Play Selected Node", COLOR_PLAYING);
        playNow.setAlignmentX(Component.LEFT_ALIGNMENT);
        playNow.setMaximumSize(new Dimension(260, 35));
        
        JButton addPlay = new CustomButton("Add Node to Playlist", COLOR_ACCENT);
        addPlay.setAlignmentX(Component.LEFT_ALIGNMENT);
        addPlay.setMaximumSize(new Dimension(260, 35));

        JButton runBFS = new CustomButton("⚡ Run BFS Recommendations", COLOR_ACCENT_CYAN);
        runBFS.setAlignmentX(Component.LEFT_ALIGNMENT);
        runBFS.setMaximumSize(new Dimension(260, 35));

        sidebar.add(playNow);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(addPlay);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(runBFS);
        sidebar.add(Box.createVerticalStrut(25));

        JLabel resultHeader = new JLabel("Recommended Connections (BFS)");
        resultHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultHeader.setForeground(COLOR_TEXT_MAIN);
        resultHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(resultHeader);
        sidebar.add(Box.createVerticalStrut(10));

        recResultsPanel = new JPanel();
        recResultsPanel.setLayout(new BoxLayout(recResultsPanel, BoxLayout.Y_AXIS));
        recResultsPanel.setOpaque(false);
        recResultsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane resultScroll = new JScrollPane(recResultsPanel);
        styleScrollPane(resultScroll);
        resultScroll.setBorder(null);
        resultScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(resultScroll);

        panel.add(sidebar, BorderLayout.EAST);

        playNow.addActionListener(e -> {
            if (selectedGraphSong != null) {
                playDirectSong(selectedGraphSong);
                graphCanvas.updateGraphState(selectedGraphSong, selectedGraphSong);
            }
        });

        addPlay.addActionListener(e -> {
            if (selectedGraphSong != null) {
                activePlaylist.add(selectedGraphSong);
                JOptionPane.showMessageDialog(this, "\"" + selectedGraphSong.title + "\" added to active playlist!", "Added", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        runBFS.addActionListener(e -> {
            if (selectedGraphSong != null) {
                triggerBFS(selectedGraphSong);
            }
        });

        return panel;
    }

    public void updateRecommendationSelection(Song song) {
        this.selectedGraphSong = song;
        recSelTitle.setText(song.title);
        recSelArtist.setText(song.artist);
        recSelGenre.setText(song.genre);
        recSelLikes.setText("Likes: " + song.likes + "  |  Plays: " + song.plays);

        recResultsPanel.removeAll();
        List<Song> directNeighbors = recGraph.getConnections(song);
        if (directNeighbors.isEmpty()) {
            JLabel empty = new JLabel("No direct connections.");
            empty.setForeground(COLOR_TEXT_MUTED);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            recResultsPanel.add(empty);
        } else {
            for (Song n : directNeighbors) {
                JPanel item = new JPanel(new BorderLayout());
                item.setOpaque(false);
                item.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
                
                JLabel lbl = new JLabel("• " + n.title + " (" + n.genre + ")");
                lbl.setForeground(COLOR_TEXT_MUTED);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                item.add(lbl, BorderLayout.CENTER);

                JButton quickPlay = new JButton("▶");
                quickPlay.setForeground(COLOR_PLAYING);
                quickPlay.setContentAreaFilled(false);
                quickPlay.setBorder(null);
                quickPlay.setFocusPainted(false);
                quickPlay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                quickPlay.addActionListener(ev -> {
                    playDirectSong(n);
                    updateRecommendationSelection(n);
                    graphCanvas.updateGraphState(n, n);
                });
                item.add(quickPlay, BorderLayout.EAST);

                recResultsPanel.add(item);
            }
        }
        recResultsPanel.revalidate();
        recResultsPanel.repaint();
    }

    private void triggerBFS(Song start) {
        recResultsPanel.removeAll();
        List<Song> recommendations = recGraph.recommend(start, 4);
        
        for (Song r : recommendations) {
            JPanel item = new JPanel(new BorderLayout());
            item.setOpaque(false);
            item.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

            JLabel lbl = new JLabel("✨ " + r.title + " - " + r.artist);
            lbl.setForeground(COLOR_ACCENT_CYAN);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            item.add(lbl, BorderLayout.CENTER);

            JPanel subActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            subActions.setOpaque(false);

            JButton add = new JButton("➕");
            add.setForeground(COLOR_ACCENT);
            add.setContentAreaFilled(false);
            add.setBorder(null);
            add.setFocusPainted(false);
            add.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            add.addActionListener(ev -> {
                activePlaylist.add(r);
                JOptionPane.showMessageDialog(this, "\"" + r.title + "\" added to active playlist!", "Added", JOptionPane.INFORMATION_MESSAGE);
            });

            JButton play = new JButton("▶");
            play.setForeground(COLOR_PLAYING);
            play.setContentAreaFilled(false);
            play.setBorder(null);
            play.setFocusPainted(false);
            play.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            play.addActionListener(ev -> {
                playDirectSong(r);
                updateRecommendationSelection(r);
                graphCanvas.updateGraphState(r, r);
            });

            subActions.add(add);
            subActions.add(play);
            item.add(subActions, BorderLayout.EAST);

            recResultsPanel.add(item);
        }
        
        recResultsPanel.revalidate();
        recResultsPanel.repaint();
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Playback History (Stack - LIFO)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        topRow.add(titleLabel, BorderLayout.WEST);

        JButton clearBtn = new CustomButton("Clear History Stack", new Color(220, 53, 69));
        topRow.add(clearBtn, BorderLayout.EAST);
        panel.add(topRow, BorderLayout.NORTH);

        historyTableModel = new DefaultTableModel(new Object[]{"Stack Order", "Title", "Artist", "Genre", "Duration"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        styleDarkTable(historyTable);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        clearBtn.addActionListener(e -> {
            playStack.clear();
            refreshHistoryTable();
        });

        return panel;
    }

    private void refreshHistoryTable() {
        historyTableModel.setRowCount(0);
        List<Song> history = playStack.getHistoryList();
        for (int i = 0; i < history.size(); i++) {
            Song s = history.get(i);
            String position = (i == 0) ? "TOP (Most Recent)" : String.valueOf(i + 1);
            historyTableModel.addRow(new Object[]{position, s.title, s.artist, s.genre, s.duration});
        }
    }

    private void styleDarkTable(JTable table) {
        table.setBackground(COLOR_PANEL);
        table.setForeground(COLOR_TEXT_MAIN);
        table.setGridColor(new Color(40, 40, 40));
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(60, 60, 60));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setShowVerticalLines(false);
        table.setBorder(null);

        JTableHeader header = table.getTableHeader();
        header.setBackground(COLOR_SIDEBAR);
        header.setForeground(COLOR_TEXT_MAIN);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 45, 45)));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

    private JPanel createBottomPlayerBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setPreferredSize(new Dimension(getWidth(), 95));
        bar.setBackground(COLOR_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(45, 45, 45)));
        bar.setBorder(BorderFactory.createCompoundBorder(
            bar.getBorder(),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JPanel trackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        trackPanel.setOpaque(false);
        trackPanel.setPreferredSize(new Dimension(280, 75));

        JPanel miniArt = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_ACCENT, getWidth(), getHeight(), COLOR_ACCENT_CYAN);
                g2d.setPaint(gp);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2d.dispose();
            }
        };
        miniArt.setPreferredSize(new Dimension(50, 50));
        trackPanel.add(miniArt);

        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(false);

        currentSongTitleLabel = new JLabel("Choose a Song");
        currentSongTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        currentSongTitleLabel.setForeground(COLOR_TEXT_MAIN);
        
        currentSongArtistLabel = new JLabel("Play your favorite music");
        currentSongArtistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        currentSongArtistLabel.setForeground(COLOR_TEXT_MUTED);

        details.add(currentSongTitleLabel);
        details.add(Box.createVerticalStrut(3));
        details.add(currentSongArtistLabel);
        trackPanel.add(details);
        bar.add(trackPanel, BorderLayout.WEST);

        JPanel controlsWrapper = new JPanel();
        controlsWrapper.setLayout(new BoxLayout(controlsWrapper, BoxLayout.Y_AXIS));
        controlsWrapper.setOpaque(false);

        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonsRow.setOpaque(false);

        JButton prevBtn = createControlBtn("⏮");
        playPauseButton = createControlBtn("▶");
        JButton nextBtn = createControlBtn("⏭");

        buttonsRow.add(prevBtn);
        buttonsRow.add(playPauseButton);
        buttonsRow.add(nextBtn);
        controlsWrapper.add(buttonsRow);
        controlsWrapper.add(Box.createVerticalStrut(8));

        JPanel sliderRow = new JPanel(new BorderLayout(8, 0));
        sliderRow.setOpaque(false);
        sliderRow.setPreferredSize(new Dimension(500, 20));

        timeElapsedLabel = new JLabel("0:00");
        timeElapsedLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        timeElapsedLabel.setForeground(COLOR_TEXT_MUTED);

        timeRemainingLabel = new JLabel("0:00");
        timeRemainingLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        timeRemainingLabel.setForeground(COLOR_TEXT_MUTED);

        songProgressSlider = new JSlider(0, 100, 0);
        songProgressSlider.setOpaque(false);
        songProgressSlider.setFocusable(false);
        songProgressSlider.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        songProgressSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { isTimelineAdjusting = true; }
            @Override
            public void mouseReleased(MouseEvent e) {
                isTimelineAdjusting = false;
                if (player.getCurrentSong() != null) {
                    player.setPositionPct(songProgressSlider.getValue() / 100.0);
                }
            }
        });

        sliderRow.add(timeElapsedLabel, BorderLayout.WEST);
        sliderRow.add(songProgressSlider, BorderLayout.CENTER);
        sliderRow.add(timeRemainingLabel, BorderLayout.EAST);
        controlsWrapper.add(sliderRow);
        
        JPanel midContainer = new JPanel(new GridBagLayout());
        midContainer.setOpaque(false);
        midContainer.add(controlsWrapper);
        bar.add(midContainer, BorderLayout.CENTER);

        visualizerPanel = new AudioVisualizerPanel();
        bar.add(visualizerPanel, BorderLayout.EAST);

        playPauseButton.addActionListener(e -> {
            Song current = player.getCurrentSong();
            if (current == null) {
                SongNode head = activePlaylist.getHead();
                if (head != null) playPlaylistSong(head.song);
                else if (catalog.size() > 0) playDirectSong(catalog.get(0));
            } else {
                if (player.isPlaying()) {
                    player.stop();
                    playPauseButton.setText("▶");
                    visualizerPanel.setPlaying(false);
                } else {
                    player.play(current);
                    playPauseButton.setText("⏸");
                    visualizerPanel.setPlaying(true);
                }
            }
        });

        nextBtn.addActionListener(e -> triggerNextSong());
        prevBtn.addActionListener(e -> triggerPrevSong());

        return bar;
    }

    private JButton createControlBtn(String txt) {
        JButton btn = new JButton(txt);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.setForeground(COLOR_TEXT_MAIN);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setForeground(COLOR_ACCENT); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setForeground(COLOR_TEXT_MAIN); }
        });
        return btn;
    }

    private void playDirectSong(Song song) {
        SongNode node = activePlaylist.setCurrentBySong(song);
        if (node == null) {
            activePlaylist.add(song);
            activePlaylist.setCurrentBySong(song);
        }
        
        player.play(song);
        playPauseButton.setText("⏸");
        currentSongTitleLabel.setText(song.title);
        currentSongArtistLabel.setText(song.artist);
        visualizerPanel.setPlaying(true);

        playStack.push(song);
        refreshHistoryTable();

        song.plays++;
        graphCanvas.updateGraphState(selectedGraphSong, song);
    }

    private void playPlaylistSong(Song song) {
        activePlaylist.setCurrentBySong(song);
        player.play(song);
        playPauseButton.setText("⏸");
        currentSongTitleLabel.setText(song.title);
        currentSongArtistLabel.setText(song.artist);
        visualizerPanel.setPlaying(true);

        playStack.push(song);
        refreshHistoryTable();
        
        song.plays++;
        graphCanvas.updateGraphState(selectedGraphSong, song);
        refreshPlaylistTable();
    }

    private void triggerNextSong() {
        SongNode curr = activePlaylist.getCurrent();
        if (curr != null && curr.next != null) {
            playPlaylistSong(curr.next.song);
        } else {
            SongNode head = activePlaylist.getHead();
            if (head != null) playPlaylistSong(head.song);
        }
    }

    private void triggerPrevSong() {
        SongNode curr = activePlaylist.getCurrent();
        if (curr != null && curr.prev != null) {
            playPlaylistSong(curr.prev.song);
        } else {
            SongNode tail = activePlaylist.getTail();
            if (tail != null) playPlaylistSong(tail.song);
        }
    }

    // ==========================================
    // REAL AUDIO PLAYBACK TIMELINE UPDATES
    // ==========================================

    @Override
    public void onProgress(int elapsedMs) {
        if (isTimelineAdjusting) return;
        
        SwingUtilities.invokeLater(() -> {
            Song s = player.getCurrentSong();
            if (s == null) return;
            
            int totalMs = parseDurationToMs(s.duration);
            double pct = (double) elapsedMs / totalMs;
            songProgressSlider.setValue((int)(pct * 100.0));

            timeElapsedLabel.setText(formatMs(elapsedMs));
            int remaining = Math.max(0, totalMs - elapsedMs);
            timeRemainingLabel.setText("-" + formatMs(remaining));
        });
    }

    @Override
    public void onSongFinished() {
        triggerNextSong();
    }

    private int parseDurationToMs(String duration) {
        try {
            String[] parts = duration.split(":");
            int min = Integer.parseInt(parts[0]);
            int sec = Integer.parseInt(parts[1]);
            return (min * 60 + sec) * 1000;
        } catch (Exception e) {
            return 5 * 1000;
        }
    }

    private String formatMs(int ms) {
        int totalSec = ms / 1000;
        int min = totalSec / 60;
        int sec = totalSec % 60;
        return String.format("%d:%02d", min, sec);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            new SocialMusicApp();
        });
    }
}
