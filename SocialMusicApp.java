import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;

// ==========================================
// 1. DATA MODELS & CUSTOM DATA STRUCTURES
// ==========================================

class Song {
    public static void main(String[] args) { SocialMusicApp.main(args); }
    int id; String title, artist, genre, filename;
    public Song(int id, String t, String a, String g, String f) {
        this.id = id; title = t; artist = a; genre = g; filename = f;
    }
    public String toString() { return title + " - " + artist; }
}

// Student 1: Doubly Linked List (DLL) for Playlist
class DLLNode {
    Song song; DLLNode prev, next;
    public DLLNode(Song s) { song = s; }
}
class PlaylistDLL {
    DLLNode head, tail;
    public void add(Song s) {
        DLLNode n = new DLLNode(s);
        if (head == null) head = tail = n;
        else { tail.next = n; n.prev = tail; tail = n; }
    }
    public void remove(Song s) {
        for (DLLNode c = head; c != null; c = c.next) {
            if (c.song.id == s.id) {
                if (c == head) head = head.next;
                if (c == tail) tail = tail.prev;
                if (c.prev != null) c.prev.next = c.next;
                if (c.next != null) c.next.prev = c.prev;
                return;
            }
        }
    }
    public DLLNode findNode(Song s) {
        if (s == null) return null;
        for (DLLNode c = head; c != null; c = c.next) {
            if (c.song.id == s.id) return c;
        }
        return null;
    }
    private void swap(DLLNode a, DLLNode b) { Song t = a.song; a.song = b.song; b.song = t; }
    public void moveUp(Song s) {
        for (DLLNode c = head; c != null; c = c.next) {
            if (c.song.id == s.id && c != head) { swap(c, c.prev); return; }
        }
    }
    public void moveDown(Song s) {
        for (DLLNode c = head; c != null; c = c.next) {
            if (c.song.id == s.id && c != tail) { swap(c, c.next); return; }
        }
    }
    public List<Song> toList() {
        List<Song> l = new ArrayList<>();
        for (DLLNode c = head; c != null; c = c.next) l.add(c.song);
        return l;
    }
}

// Student 2: Custom FIFO Queue for Play Queue
class QueueNode {
    Song song; QueueNode next;
    public QueueNode(Song s) { song = s; }
}
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
    public List<Song> toList() {
        List<Song> l = new ArrayList<>();
        for (QueueNode c = head; c != null; c = c.next) l.add(c.song);
        return l;
    }
}

// Student 3: Binary Search for Catalog
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

// Student 4: Recommendation Graph (Adjacency List Graph Structure)
class GraphNode {
    Song song;
    List<Song> neighbors = new ArrayList<>();
    public GraphNode(Song s) { song = s; }
}
class RecommendationGraph {
    private java.util.Map<Integer, GraphNode> nodes = new java.util.HashMap<>();
    public void addSong(Song s) { nodes.putIfAbsent(s.id, new GraphNode(s)); }
    public void addConnection(Song s1, Song s2) {
        addSong(s1); addSong(s2);
        if (!nodes.get(s1.id).neighbors.contains(s2)) nodes.get(s1.id).neighbors.add(s2);
        if (!nodes.get(s2.id).neighbors.contains(s1)) nodes.get(s2.id).neighbors.add(s1);
    }
    public List<Song> getRecommendations(Song s) {
        if (s == null || !nodes.containsKey(s.id)) return new ArrayList<>();
        return nodes.get(s.id).neighbors;
    }
}

// ==========================================
// 2. AUDIO PLAYBACK ENGINE (WAV)
// ==========================================

class SongWavPlayer {
    private Clip clip;
    private Song currentSong;
    private Timer timer;
    public SongWavPlayer(Runnable onFinish) {
        timer = new Timer(100, e -> {
            if (clip != null && !clip.isRunning() && clip.getFramePosition() >= clip.getFrameLength()) {
                stop(); onFinish.run();
            }
        });
    }
    public void play(Song s) {
        stop(); currentSong = s;
        try {
            File f = new File("songs/" + s.filename);
            if (!f.exists()) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            clip = AudioSystem.getClip(); clip.open(ais); clip.start();
            timer.start();
        } catch (Exception e) { e.printStackTrace(); }
    }
    public void stop() {
        timer.stop();
        if (clip != null) { clip.stop(); clip.close(); clip = null; }
        currentSong = null;
    }
    public Song getCurrentSong() { return currentSong; }
}

// ==========================================
// 3. UI CANVAS VISUALIZERS
// ==========================================

class AudioVisualizerPanel extends JPanel {
    private int[] heights = new int[6];
    public AudioVisualizerPanel(SongWavPlayer player) {
        setPreferredSize(new Dimension(100, 35));
        setBackground(new Color(24, 26, 34));
        new Timer(100, e -> {
            boolean active = player.getCurrentSong() != null;
            for (int i = 0; i < heights.length; i++) heights[i] = active ? (int)(Math.random()*22)+5 : 5;
            repaint();
        }).start();
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); g.setColor(new Color(124, 77, 255));
        for (int i=0; i<heights.length; i++) {
            g.fillRect(i*15 + 5, getHeight() - heights[i] - 5, 10, heights[i]);
        }
    }
}

// ==========================================
// 4. MAIN WINDOW APP GUI
// ==========================================

public class SocialMusicApp extends JFrame {
    private static final Color COLOR_BG = new Color(33, 36, 46);       // Decent dark theme bg
    private static final Color COLOR_SIDEBAR = new Color(24, 26, 34);  // Darker sidebar
    private static final Color COLOR_PANEL = new Color(40, 44, 56);     // Cards / panels
    private static final Color COLOR_TEXT = new Color(235, 235, 240);   // Off-white text
    private static final Color COLOR_MUTED = new Color(150, 155, 175);  // Nice muted text
    private static final Color COLOR_ACCENT = new Color(124, 77, 255);  // Premium clean purple accent

    private List<Song> catalog = new ArrayList<>();
    private PlaylistDLL activePlaylist = new PlaylistDLL();
    private PlayQueue playQueue = new PlayQueue();
    private RecommendationGraph recGraph = new RecommendationGraph();

    private SongWavPlayer player;
    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);
    private Song selectedSong;
    private DLLNode currentPlayingNode = null;

    private JLabel lblSelTitle, lblSelArtist, lblSelGenre;
    private JLabel lblTitle, lblSubtitle;
    private JButton btnPlayPause;
    private JButton btnSidebarDashboard, btnSidebarPlaylist, btnSidebarQueue, btnSidebarRec;
    private DefaultTableModel playlistModel, queueModel;
    private DefaultListModel<Song> dashboardListModel = new DefaultListModel<>();
    private DefaultListModel<Song> recListModel = new DefaultListModel<>();
    private JLabel lblRecFor;

    public SocialMusicApp() {
        setTitle("SocialMusic - Student Project");
        setSize(880, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initData();
        player = new SongWavPlayer(this::playNext);

        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

        contentPanel.add(createDashboard(), "DASHBOARD");
        contentPanel.add(createPlaylist(), "PLAYLIST");
        contentPanel.add(createQueuePanel(), "QUEUE");
        contentPanel.add(createRecPanel(), "REC");

        if (!catalog.isEmpty()) {
            selectSong(catalog.get(0));
        }
        updateSidebarBadges();
        setVisible(true);
    }

    private void styleBtn(JButton b) {
        b.setBackground(new Color(60, 64, 80));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    private void styleLabel(JLabel l, Font f, Color c) {
        l.setFont(f); l.setForeground(c);
    }

    private void initData() {
        String[][] dummyData = {
            {"Blinding Lights", "The Weeknd", "Pop", "sample-3s.wav"},
            {"Shape of You", "Ed Sheeran", "Pop", "sample-6s.wav"},
            {"Bohemian Rhapsody", "Queen", "Rock", "overdrive.wav"},
            {"Stay", "Kid LAROI & Justin Bieber", "Pop", "sample-9s.wav"},
            {"Hotel California", "Eagles", "Rock", "gc.wav"},
            {"Bad Guy", "Billie Eilish", "Alternative", "synth.wav"},
            {"Someone Like You", "Adele", "Pop", "voice.wav"},
            {"Stairway to Heaven", "Led Zeppelin", "Rock", "gc.wav"},
            {"Perfect", "Ed Sheeran", "Pop", "voice-note.wav"},
            {"Smells Like Teen Spirit", "Nirvana", "Rock", "overdrive.wav"},
            {"Rolling in the Deep", "Adele", "Pop", "voice.wav"},
            {"Blinding Synth", "Retro Beats", "Synthwave", "synth.wav"},
            {"Ocean Breeze", "Nature Sounds", "Ambient", "noise.wav"},
            {"City Traffic Jam", "Urban Effects", "Sound Effect", "car-horn.wav"},
            {"Sine Wave Tone", "Lab Signal", "Electronic", "sine.wav"},
            {"Acoustic Solo", "Gary Clark", "Acoustic", "gc.wav"}
        };

        File folder = new File("songs");
        List<String> foundFiles = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            if (files != null) {
                for (File f : files) foundFiles.add(f.getName());
            }
        }

        int id = 1;
        for (String[] songData : dummyData) {
            String title = songData[0];
            String artist = songData[1];
            String genre = songData[2];
            String filename = songData[3];
            if (foundFiles.contains(filename)) {
                catalog.add(new Song(id++, title, artist, genre, filename));
            }
        }

        for (String filename : foundFiles) {
            boolean loaded = false;
            for (Song s : catalog) {
                if (s.filename.equals(filename)) { loaded = true; break; }
            }
            if (!loaded) {
                String title = filename.substring(0, filename.lastIndexOf('.'));
                title = title.replace('-', ' ').replace('_', ' ');
                String[] words = title.split(" ");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
                }
                catalog.add(new Song(id++, sb.toString().trim(), "Local Artist", "Sound", filename));
            }
        }

        if (catalog.isEmpty()) {
            catalog.add(new Song(1, "Summer Beat Loop", "Synth Master", "Pop", "sample-3s.wav"));
            catalog.add(new Song(2, "Synthwave Rhythm", "Retro Wave", "Rock", "sample-6s.wav"));
            catalog.add(new Song(3, "Jazz Piano Loop", "Dave Brubeck", "Jazz", "sample-9s.wav"));
        }

        for (Song s : catalog) recGraph.addSong(s);
        for (int i = 0; i < catalog.size(); i++) {
            recGraph.addConnection(catalog.get(i), catalog.get((i + 1) % catalog.size()));
            if (catalog.size() > 3) {
                recGraph.addConnection(catalog.get(i), catalog.get((i + 3) % catalog.size()));
            }
        }

        for (Song s : catalog) {
            activePlaylist.add(s);
        }

        refreshDashboardList();
    }

    private void refreshDashboardList() {
        dashboardListModel.clear();
        for (Song s : catalog) dashboardListModel.addElement(s);
    }

    private void updateSidebarBadges() {
        if (btnSidebarPlaylist != null && btnSidebarQueue != null) {
            btnSidebarPlaylist.setText("DLL Playlist (" + activePlaylist.toList().size() + ")");
            btnSidebarQueue.setText("FIFO Queue (" + playQueue.toList().size() + ")");
        }
    }

    private void updateRecommendations(Song s) {
        recListModel.clear();
        if (s != null) {
            lblRecFor.setText("Recommendations for: " + s.title);
            for (Song r : recGraph.getRecommendations(s)) {
                recListModel.addElement(r);
            }
        } else {
            lblRecFor.setText("Select a song to get recommendations");
        }
    }

    private JPanel createSidebar() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COLOR_SIDEBAR); p.setPreferredSize(new Dimension(170, 500));
        
        // Brand/Logo Header Panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        logoPanel.setOpaque(false);

        // Try to load and scale logo.png
        ImageIcon logoIcon = null;
        try {
            File logoFile = new File("logo.png");
            if (logoFile.exists()) {
                ImageIcon rawIcon = new ImageIcon("logo.png");
                Image img = rawIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
                logoIcon = new ImageIcon(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (logoIcon != null) {
            logoPanel.add(new JLabel(logoIcon));
        } else {
            JLabel emoji = new JLabel("🎵"); emoji.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            logoPanel.add(emoji);
        }

        JLabel title = new JLabel("SocialMusic");
        styleLabel(title, new Font("Segoe UI", Font.BOLD, 15), COLOR_TEXT);
        logoPanel.add(title);
        p.add(logoPanel);

        btnSidebarDashboard = new JButton("Dashboard");
        btnSidebarPlaylist = new JButton("DLL Playlist (0)");
        btnSidebarQueue = new JButton("FIFO Queue (0)");
        btnSidebarRec = new JButton("Recommendations");

        JButton[] buttons = {btnSidebarDashboard, btnSidebarPlaylist, btnSidebarQueue, btnSidebarRec};
        String[] cards = {"DASHBOARD", "PLAYLIST", "QUEUE", "REC"};
        for (int i = 0; i < buttons.length; i++) {
            final String card = cards[i];
            JButton btn = buttons[i];
            btn.setBackground(COLOR_SIDEBAR); btn.setForeground(COLOR_MUTED);
            btn.setFocusPainted(false); btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            btn.setMaximumSize(new Dimension(170, 35));
            btn.addActionListener(e -> {
                cardLayout.show(contentPanel, card);
                if (card.equals("PLAYLIST")) refreshPlaylist();
                if (card.equals("QUEUE")) refreshQueue();
                if (card.equals("REC")) updateRecommendations(selectedSong);
            });
            p.add(btn); p.add(Box.createVerticalStrut(5));
        }
        return p;
    }

    private JPanel createDashboard() {
        JPanel p = new JPanel(new BorderLayout(15, 15)); p.setBackground(COLOR_BG);
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_PANEL); card.setPreferredSize(new Dimension(240, 300));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        lblSelTitle = new JLabel("None"); styleLabel(lblSelTitle, new Font("Segoe UI", Font.BOLD, 18), COLOR_TEXT);
        lblSelArtist = new JLabel("None"); styleLabel(lblSelArtist, new Font("Segoe UI", Font.PLAIN, 14), COLOR_MUTED);
        lblSelGenre = new JLabel("None"); styleLabel(lblSelGenre, new Font("Segoe UI", Font.ITALIC, 12), COLOR_ACCENT);
        JButton playBtn = new JButton("PLAY NOW"); playBtn.setBackground(COLOR_ACCENT); playBtn.setForeground(Color.WHITE);
        styleBtn(playBtn);
        playBtn.addActionListener(e -> { if (selectedSong != null) play(selectedSong); });
        
        JLabel lblHeader = new JLabel("Selected Song:"); styleLabel(lblHeader, new Font("Segoe UI", Font.BOLD, 12), COLOR_MUTED);
        card.add(lblHeader); card.add(Box.createVerticalStrut(10));
        card.add(lblSelTitle); card.add(Box.createVerticalStrut(5));
        card.add(lblSelArtist); card.add(Box.createVerticalStrut(5));
        card.add(lblSelGenre); card.add(Box.createVerticalStrut(20));
        card.add(playBtn);
        p.add(card, BorderLayout.WEST);

        JList<Song> list = new JList<>(dashboardListModel); list.setBackground(COLOR_PANEL); list.setForeground(COLOR_TEXT);
        list.setSelectionBackground(COLOR_ACCENT); list.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectSong(list.getSelectedValue());
                updateRecommendations(list.getSelectedValue());
            }
        });
        
        JPanel center = new JPanel(new BorderLayout(5, 5)); center.setOpaque(false);
        
        JPanel topPanel = new JPanel(new BorderLayout(5, 5)); topPanel.setOpaque(false);

        JPanel topSortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); topSortPanel.setOpaque(false);
        JLabel lblSort = new JLabel("Sort:"); styleLabel(lblSort, new Font("Segoe UI", Font.BOLD, 12), COLOR_TEXT);
        JButton btnSortTitle = new JButton("By Title"); styleBtn(btnSortTitle);
        JButton btnSortArtist = new JButton("By Artist"); styleBtn(btnSortArtist);
        
        btnSortTitle.addActionListener(e -> {
            catalog.sort((a, b) -> a.title.compareToIgnoreCase(b.title));
            refreshDashboardList();
        });
        btnSortArtist.addActionListener(e -> {
            catalog.sort((a, b) -> a.artist.compareToIgnoreCase(b.artist));
            refreshDashboardList();
        });
        
        topSortPanel.add(lblSort); topSortPanel.add(btnSortTitle); topSortPanel.add(btnSortArtist);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); searchPanel.setOpaque(false);
        JTextField txtSearch = new JTextField(8);
        JButton btnSearch = new JButton("Search"); styleBtn(btnSearch);
        btnSearch.addActionListener(e -> {
            String q = txtSearch.getText().trim();
            if (!q.isEmpty()) {
                catalog.sort((a, b) -> a.title.compareToIgnoreCase(b.title));
                refreshDashboardList();
                int idx = BinarySearch.searchByTitle(catalog, q);
                if (idx >= 0) {
                    list.setSelectedIndex(idx);
                    list.ensureIndexIsVisible(idx);
                    selectSong(catalog.get(idx));
                    JOptionPane.showMessageDialog(this, "Found: " + catalog.get(idx).title);
                } else {
                    JOptionPane.showMessageDialog(this, "Song not found!");
                }
            }
        });
        searchPanel.add(txtSearch); searchPanel.add(btnSearch);

        topPanel.add(topSortPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        center.add(topPanel, BorderLayout.NORTH); center.add(new JScrollPane(list), BorderLayout.CENTER);
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); bottom.setOpaque(false);
        JButton btnAddPlaylist = new JButton("Add to Playlist"); styleBtn(btnAddPlaylist);
        JButton btnAddQ = new JButton("Add to Play Queue"); styleBtn(btnAddQ);
        
        btnAddPlaylist.addActionListener(e -> {
            Song s = list.getSelectedValue();
            if (s != null) {
                boolean exists = false;
                for (Song existing : activePlaylist.toList()) {
                    if (existing.id == s.id) { exists = true; break; }
                }
                if (exists) {
                    JOptionPane.showMessageDialog(this, "Song is already in the playlist!");
                    return;
                }
                activePlaylist.add(s);
                updateSidebarBadges();
                JOptionPane.showMessageDialog(this, "Added to DLL Playlist!");
            }
        });
        
        btnAddQ.addActionListener(e -> {
            Song s = list.getSelectedValue();
            if (s != null) {
                playQueue.enqueue(s);
                updateSidebarBadges();
                JOptionPane.showMessageDialog(this, "Added to FIFO Play Queue!");
            }
        });
        
        bottom.add(btnAddPlaylist); bottom.add(btnAddQ);
        center.add(bottom, BorderLayout.SOUTH);
        
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private void styleTable(JTable t) {
        t.setBackground(COLOR_PANEL); t.setForeground(COLOR_TEXT);
        t.getTableHeader().setBackground(COLOR_SIDEBAR); t.getTableHeader().setForeground(COLOR_TEXT);
        t.setSelectionBackground(COLOR_ACCENT); t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private JPanel createPlaylist() {
        JPanel p = new JPanel(new BorderLayout(10, 10)); p.setBackground(COLOR_BG);
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        playlistModel = new DefaultTableModel(new Object[]{"ID", "Title", "Artist", "Genre", "File"}, 0);
        JTable table = new JTable(playlistModel); styleTable(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); controls.setOpaque(false);
        JButton playSel = new JButton("Play"); JButton up = new JButton("Move Up"); JButton down = new JButton("Move Down"); JButton rem = new JButton("Remove");
        for (JButton b : new JButton[]{playSel, up, down, rem}) {
            styleBtn(b);
            b.addActionListener(e -> {
                int r = table.getSelectedRow(); if (r == -1) return;
                Song s = activePlaylist.toList().get(r);
                if (e.getSource() == playSel) play(s);
                else if (e.getSource() == up) { activePlaylist.moveUp(s); refreshPlaylist(); table.setRowSelectionInterval(r-1, r-1); }
                else if (e.getSource() == down) { activePlaylist.moveDown(s); refreshPlaylist(); table.setRowSelectionInterval(r+1, r+1); }
                else if (e.getSource() == rem) { activePlaylist.remove(s); refreshPlaylist(); }
            });
            controls.add(b);
        }
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); inputPanel.setOpaque(false);
        JLabel lblSelect = new JLabel("Quick Add Song:"); styleLabel(lblSelect, new Font("Segoe UI", Font.PLAIN, 12), COLOR_TEXT);
        
        JComboBox<Song> cbPlaylistSongs = new JComboBox<>();
        for (Song s : catalog) cbPlaylistSongs.addItem(s);
        cbPlaylistSongs.setBackground(COLOR_PANEL); cbPlaylistSongs.setForeground(COLOR_TEXT);
        
        JButton addBtn = new JButton("Add to Playlist"); styleBtn(addBtn);
        addBtn.addActionListener(e -> {
            Song s = (Song) cbPlaylistSongs.getSelectedItem();
            if (s != null) {
                boolean exists = false;
                for (Song existing : activePlaylist.toList()) {
                    if (existing.id == s.id) { exists = true; break; }
                }
                if (exists) {
                    JOptionPane.showMessageDialog(this, "Song is already in the playlist!");
                    return;
                }
                activePlaylist.add(s);
                refreshPlaylist();
            }
        });
        
        inputPanel.add(lblSelect); inputPanel.add(cbPlaylistSongs); inputPanel.add(addBtn);

        JPanel south = new JPanel(new BorderLayout(5, 5)); south.setOpaque(false);
        south.add(controls, BorderLayout.NORTH); south.add(inputPanel, BorderLayout.SOUTH);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createQueuePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10)); p.setBackground(COLOR_BG);
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        queueModel = new DefaultTableModel(new Object[]{"Queue Position", "Title", "Artist"}, 0);
        JTable table = new JTable(queueModel); styleTable(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(5, 5)); south.setOpaque(false);
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); inputPanel.setOpaque(false);
        JLabel lblSelect = new JLabel("Quick Add Song:"); styleLabel(lblSelect, new Font("Segoe UI", Font.PLAIN, 12), COLOR_TEXT);
        
        JComboBox<Song> cbQueueSongs = new JComboBox<>();
        for (Song s : catalog) cbQueueSongs.addItem(s);
        cbQueueSongs.setBackground(COLOR_PANEL); cbQueueSongs.setForeground(COLOR_TEXT);
        
        JButton addBtn = new JButton("Add to Queue"); styleBtn(addBtn);
        addBtn.addActionListener(e -> {
            Song s = (Song) cbQueueSongs.getSelectedItem();
            if (s != null) {
                playQueue.enqueue(s);
                refreshQueue();
            }
        });
        inputPanel.add(lblSelect); inputPanel.add(cbQueueSongs); inputPanel.add(addBtn);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT)); controls.setOpaque(false);
        JButton dequeueBtn = new JButton("Play Next in Queue"); styleBtn(dequeueBtn);
        dequeueBtn.addActionListener(e -> playNext());
        controls.add(dequeueBtn);
        
        south.add(inputPanel, BorderLayout.WEST);
        south.add(controls, BorderLayout.EAST);
        
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createRecPanel() {
        JPanel p = new JPanel(new BorderLayout(15, 15)); p.setBackground(COLOR_BG);
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblRecFor = new JLabel("Select a song to get recommendations");
        styleLabel(lblRecFor, new Font("Segoe UI", Font.BOLD, 14), COLOR_TEXT);
        p.add(lblRecFor, BorderLayout.NORTH);

        JList<Song> rList = new JList<>(recListModel);
        rList.setBackground(COLOR_PANEL); rList.setForeground(COLOR_TEXT);
        rList.setSelectionBackground(COLOR_ACCENT); rList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(new JScrollPane(rList), BorderLayout.CENTER);

        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomControls.setOpaque(false);
        JButton playRec = new JButton("Play Selected Recommendation"); styleBtn(playRec);
        playRec.addActionListener(e -> {
            Song s = rList.getSelectedValue();
            if (s != null) { play(s); }
        });
        bottomControls.add(playRec);
        p.add(bottomControls, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createBottomBar() {
        JPanel p = new JPanel(new BorderLayout(15, 5)); p.setBackground(COLOR_SIDEBAR);
        p.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JPanel infoPanel = new JPanel(); infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        lblTitle = new JLabel("Not Playing"); styleLabel(lblTitle, new Font("Segoe UI", Font.BOLD, 13), COLOR_TEXT);
        lblSubtitle = new JLabel(""); styleLabel(lblSubtitle, new Font("Segoe UI", Font.PLAIN, 10), COLOR_MUTED);
        
        infoPanel.add(lblTitle); infoPanel.add(lblSubtitle);
        p.add(infoPanel, BorderLayout.WEST);

        JPanel centerControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); centerControls.setOpaque(false);
        JButton btnPrev = new JButton("⏮ Previous"); styleBtn(btnPrev);
        btnPlayPause = new JButton("Play/Pause"); styleBtn(btnPlayPause);
        btnPlayPause.setBackground(COLOR_ACCENT);
        JButton btnNext = new JButton("Next ⏭"); styleBtn(btnNext);

        btnPlayPause.addActionListener(e -> {
            Song cur = player.getCurrentSong();
            if (cur != null) {
                player.stop();
                lblTitle.setText("Stopped");
                lblSubtitle.setText("");
                btnPlayPause.setText("Play");
            } else if (selectedSong != null) {
                play(selectedSong);
            }
        });

        btnPrev.addActionListener(e -> playPrevious());
        btnNext.addActionListener(e -> playNext());

        centerControls.add(btnPrev);
        centerControls.add(btnPlayPause);
        centerControls.add(btnNext);
        
        p.add(centerControls, BorderLayout.CENTER);
        p.add(new AudioVisualizerPanel(player), BorderLayout.EAST);
        return p;
    }

    private void selectSong(Song s) {
        if (s == null) return;
        selectedSong = s;
        lblSelTitle.setText(s.title); lblSelArtist.setText(s.artist); lblSelGenre.setText(s.genre);
    }

    private void play(Song s) {
        if (s == null) return;
        player.play(s);
        currentPlayingNode = activePlaylist.findNode(s);
        lblTitle.setText(s.title);
        lblSubtitle.setText(s.artist + " • " + s.genre);
        btnPlayPause.setText("Pause");
    }

    private void playPrevious() {
        if (activePlaylist.head == null) return;
        if (currentPlayingNode == null) {
            currentPlayingNode = activePlaylist.tail;
        } else {
            currentPlayingNode = (currentPlayingNode.prev != null) ? currentPlayingNode.prev : activePlaylist.tail;
        }
        if (currentPlayingNode != null) {
            play(currentPlayingNode.song);
        }
    }

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

    private void refreshPlaylist() {
        playlistModel.setRowCount(0); List<Song> list = activePlaylist.toList();
        for (int i = 0; i < list.size(); i++) {
            Song s = list.get(i); playlistModel.addRow(new Object[]{i+1, s.title, s.artist, s.genre, s.filename});
        }
        updateSidebarBadges();
    }

    private void refreshQueue() {
        queueModel.setRowCount(0); List<Song> list = playQueue.toList();
        for (int i = 0; i < list.size(); i++) {
            Song s = list.get(i); queueModel.addRow(new Object[]{i+1, s.title, s.artist});
        }
        updateSidebarBadges();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(SocialMusicApp::new);
    }
}
