import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
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

// Student 3: Custom Graph for Recommendations
class GraphNode {
    Song song; List<Song> neighbors = new ArrayList<>();
    public GraphNode(Song s) { song = s; }
}
class RecommendationGraph {
    Map<Integer, GraphNode> nodes = new HashMap<>();
    public void addSong(Song s) { nodes.putIfAbsent(s.id, new GraphNode(s)); }
    public void addConnection(Song s1, Song s2) {
        addSong(s1); addSong(s2);
        if (!nodes.get(s1.id).neighbors.contains(s2)) nodes.get(s1.id).neighbors.add(s2);
        if (!nodes.get(s2.id).neighbors.contains(s1)) nodes.get(s2.id).neighbors.add(s1);
    }
    public List<Song> getRecommendations(Song s) {
        return nodes.containsKey(s.id) ? nodes.get(s.id).neighbors : new ArrayList<>();
    }
    public List<Song> getAllSongs() {
        List<Song> all = new ArrayList<>();
        for (GraphNode n : nodes.values()) all.add(n.song);
        return all;
    }
}

// Student 4: Catalog Sorting using Bubble Sort
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
    public static void sortByArtist(List<Song> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).artist.compareToIgnoreCase(list.get(j + 1).artist) > 0) {
                    Song temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
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

class GraphVisualizerPanel extends JPanel {
    private RecommendationGraph graph;
    private java.util.function.Consumer<Song> onClick;
    private Song sel, play;
    private Map<Song, Point> pos = new HashMap<>();
    public GraphVisualizerPanel(RecommendationGraph g, java.util.function.Consumer<Song> click) {
        graph = g; onClick = click; setBackground(new Color(40, 44, 56));
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                for (Map.Entry<Song, Point> entry : pos.entrySet()) {
                    if (e.getPoint().distance(entry.getValue()) < 15) {
                        sel = entry.getKey(); onClick.accept(sel); repaint(); break;
                    }
                }
            }
        });
    }
    public void update(Song s, Song p) { sel = s; play = p; repaint(); }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        List<Song> songs = graph.getAllSongs(); if (songs.isEmpty()) return;
        int cx = getWidth()/2, cy = getHeight()/2;
        pos.clear();
        for (int i = 0; i < songs.size(); i++) {
            double a = (2.0 * Math.PI * i) / songs.size();
            pos.put(songs.get(i), new Point(cx + (int)(95*Math.cos(a)), cy + (int)(95*Math.sin(a))));
        }
        g2.setColor(new Color(90, 95, 115));
        for (Song s : songs) {
            Point p1 = pos.get(s);
            for (Song n : graph.getRecommendations(s)) {
                Point p2 = pos.get(n);
                if (p2 != null) g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
        for (Song s : songs) {
            Point p = pos.get(s);
            g2.setColor(s == play ? new Color(46, 204, 113) : (s == sel ? new Color(52, 152, 219) : new Color(124, 77, 255)));
            g2.fillOval(p.x - 10, p.y - 10, 20, 20);
            g2.setColor(Color.WHITE); g2.drawOval(p.x - 10, p.y - 10, 20, 20);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(s.title, p.x + 12, p.y + 5);
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
    private Song selectedSong, selectedGraphSong;

    private JLabel lblSelTitle, lblSelArtist, lblSelGenre;
    private DefaultTableModel playlistModel, queueModel;
    private GraphVisualizerPanel graphPanel;
    private DefaultListModel<Song> recListModel = new DefaultListModel<>();
    private DefaultListModel<Song> dashboardListModel = new DefaultListModel<>();
    private JComboBox<String> fF;

    public SocialMusicApp() {
        setTitle("SocialMusic - Student Project");
        setSize(880, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initData();
        player = new SongWavPlayer(this::playNextFromQueue);

        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

        contentPanel.add(createDashboard(), "DASHBOARD");
        contentPanel.add(createPlaylist(), "PLAYLIST");
        contentPanel.add(createQueuePanel(), "QUEUE");
        contentPanel.add(createRecView(), "REC");

        if (!catalog.isEmpty()) {
            selectSong(catalog.get(0));
            selectedGraphSong = catalog.get(0);
        }
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
        File folder = new File("songs");
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        int id = 1;
        
        if (files != null) {
            for (File f : files) {
                String name = f.getName();
                String title = name.substring(0, name.lastIndexOf('.'));
                title = title.replace('-', ' ').replace('_', ' ');
                
                String[] words = title.split(" ");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
                }
                title = sb.toString().trim();
                
                String artist = "Local Artist";
                String genre = "Sound";
                if (name.contains("sample")) { artist = "Sample Lab"; genre = "Electronic"; }
                else if (name.equals("gc.wav")) { artist = "Gary Clark"; genre = "Country"; }
                else if (name.equals("synth.wav")) { artist = "Stellar Noise"; genre = "Synth"; }
                else if (name.equals("voice.wav") || name.equals("voice-note.wav")) { artist = "Vocal Choir"; genre = "Vocal"; }
                else if (name.equals("noise.wav")) { artist = "Relaxing Frequency"; genre = "Ambient"; }
                else if (name.equals("sine.wav")) { artist = "Lab Oscillator"; genre = "Tone"; }
                else if (name.equals("overdrive.wav")) { artist = "Guitar Hero"; genre = "Rock"; }
                else if (name.equals("car-horn.wav")) { artist = "Traffic Jam"; genre = "Urban"; }

                catalog.add(new Song(id++, title, artist, genre, name));
            }
        }
        
        if (catalog.isEmpty()) {
            catalog.add(new Song(1, "Summer Beat Loop", "Synth Master", "Pop", "sample-3s.wav"));
            catalog.add(new Song(2, "Synthwave Rhythm", "Retro Wave", "Rock", "sample-6s.wav"));
            catalog.add(new Song(3, "Jazz Piano Loop", "Dave Brubeck", "Jazz", "sample-9s.wav"));
        }

        for (Song s : catalog) recGraph.addSong(s);
        for (int i = 0; i < catalog.size(); i++) {
            recGraph.addConnection(catalog.get(i), catalog.get((i+1)%catalog.size()));
            if (catalog.size() > 3) {
                recGraph.addConnection(catalog.get(i), catalog.get((i+3)%catalog.size()));
            }
        }
        
        for (int i = 0; i < Math.min(3, catalog.size()); i++) {
            activePlaylist.add(catalog.get(i));
        }

        refreshDashboardList();
    }

    private void refreshDashboardList() {
        dashboardListModel.clear();
        for (Song s : catalog) dashboardListModel.addElement(s);
    }

    private JPanel createSidebar() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COLOR_SIDEBAR); p.setPreferredSize(new Dimension(170, 500));
        
        JLabel title = new JLabel("🎵 SocialMusic");
        styleLabel(title, new Font("Segoe UI", Font.BOLD, 16), COLOR_TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(20,15,20,15));
        p.add(title);

        String[] items = {"Dashboard", "DLL Playlist", "FIFO Queue", "Recommendations"};
        String[] cards = {"DASHBOARD", "PLAYLIST", "QUEUE", "REC"};
        for (int i = 0; i < items.length; i++) {
            final String card = cards[i];
            JButton btn = new JButton(items[i]);
            btn.setBackground(COLOR_SIDEBAR); btn.setForeground(COLOR_MUTED);
            btn.setFocusPainted(false); btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            btn.setMaximumSize(new Dimension(170, 35));
            btn.addActionListener(e -> {
                cardLayout.show(contentPanel, card);
                if (card.equals("PLAYLIST")) refreshPlaylist();
                if (card.equals("QUEUE")) refreshQueue();
                if (card.equals("REC")) graphPanel.update(selectedGraphSong, player.getCurrentSong());
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
        list.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) selectSong(list.getSelectedValue()); });
        
        JPanel center = new JPanel(new BorderLayout(5, 5)); center.setOpaque(false);
        
        JPanel topSortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); topSortPanel.setOpaque(false);
        JLabel lblSort = new JLabel("Sort Catalog:"); styleLabel(lblSort, new Font("Segoe UI", Font.BOLD, 12), COLOR_TEXT);
        JButton btnSortTitle = new JButton("By Title"); styleBtn(btnSortTitle);
        JButton btnSortArtist = new JButton("By Artist"); styleBtn(btnSortArtist);
        
        btnSortTitle.addActionListener(e -> {
            CatalogSorter.sortByTitle(catalog);
            refreshDashboardList();
        });
        btnSortArtist.addActionListener(e -> {
            CatalogSorter.sortByArtist(catalog);
            refreshDashboardList();
        });
        
        topSortPanel.add(lblSort); topSortPanel.add(btnSortTitle); topSortPanel.add(btnSortArtist);
        
        center.add(topSortPanel, BorderLayout.NORTH); center.add(new JScrollPane(list), BorderLayout.CENTER);
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5)); bottom.setOpaque(false);
        JButton btnAddQ = new JButton("Add to Play Queue"); styleBtn(btnAddQ);
        btnAddQ.addActionListener(e -> {
            if (list.getSelectedValue() != null) {
                playQueue.enqueue(list.getSelectedValue());
                JOptionPane.showMessageDialog(this, "Added to Play Queue!");
            }
        });
        bottom.add(btnAddQ);
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
        JTextField fT = new JTextField(8); JTextField fA = new JTextField(8); JTextField fG = new JTextField(6);
        fF = new JComboBox<>();
        for (Song s : catalog) fF.addItem(s.filename);
        JButton addBtn = new JButton("Add Song"); styleBtn(addBtn);
        addBtn.addActionListener(e -> {
            if (fT.getText().isEmpty() || fA.getText().isEmpty()) return;
            Song s = new Song(catalog.size()+1, fT.getText(), fA.getText(), fG.getText(), (String)fF.getSelectedItem());
            catalog.add(s); recGraph.addSong(s); activePlaylist.add(s); refreshPlaylist();
            refreshDashboardList();
            fT.setText(""); fA.setText(""); fG.setText("");
        });
        
        JLabel lT = new JLabel("Title:"); styleLabel(lT, new Font("Segoe UI", Font.PLAIN, 12), COLOR_TEXT);
        JLabel lA = new JLabel("Artist:"); styleLabel(lA, new Font("Segoe UI", Font.PLAIN, 12), COLOR_TEXT);
        JLabel lG = new JLabel("Genre:"); styleLabel(lG, new Font("Segoe UI", Font.PLAIN, 12), COLOR_TEXT);
        
        inputPanel.add(lT); inputPanel.add(fT);
        inputPanel.add(lA); inputPanel.add(fA);
        inputPanel.add(lG); inputPanel.add(fG);
        inputPanel.add(fF); inputPanel.add(addBtn);

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

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT)); controls.setOpaque(false);
        JButton dequeueBtn = new JButton("Play Next in Queue"); styleBtn(dequeueBtn);
        dequeueBtn.addActionListener(e -> playNextFromQueue());
        controls.add(dequeueBtn);
        p.add(controls, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createRecView() {
        JPanel p = new JPanel(new BorderLayout(10, 10)); p.setBackground(COLOR_BG);
        graphPanel = new GraphVisualizerPanel(recGraph, s -> {
            selectedGraphSong = s; recListModel.clear();
            for (Song r : recGraph.getRecommendations(s)) recListModel.addElement(r);
        });
        p.add(graphPanel, BorderLayout.CENTER);

        JPanel side = new JPanel(new BorderLayout(5, 5)); side.setBackground(COLOR_PANEL); side.setPreferredSize(new Dimension(180, 400));
        side.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JList<Song> rl = new JList<>(recListModel); rl.setBackground(COLOR_PANEL); rl.setForeground(COLOR_TEXT);
        rl.setSelectionBackground(COLOR_ACCENT);
        JButton playRec = new JButton("Play Recommended"); styleBtn(playRec);
        playRec.addActionListener(e -> { if (rl.getSelectedValue()!=null) play(rl.getSelectedValue()); });
        
        JLabel lRec = new JLabel("Recommendations:"); styleLabel(lRec, new Font("Segoe UI", Font.BOLD, 12), COLOR_TEXT);
        side.add(lRec, BorderLayout.NORTH); side.add(new JScrollPane(rl), BorderLayout.CENTER); side.add(playRec, BorderLayout.SOUTH);
        p.add(side, BorderLayout.EAST);
        return p;
    }

    private JPanel createBottomBar() {
        JPanel p = new JPanel(new BorderLayout(10, 5)); p.setBackground(COLOR_SIDEBAR);
        p.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel lblTitle = new JLabel("Not Playing"); styleLabel(lblTitle, new Font("Segoe UI", Font.BOLD, 13), COLOR_TEXT);
        p.add(lblTitle, BorderLayout.WEST);

        JButton playBtn = new JButton("Play/Pause"); styleBtn(playBtn);
        playBtn.setBackground(COLOR_ACCENT);
        playBtn.addActionListener(e -> {
            if (player.getCurrentSong() != null) { player.stop(); lblTitle.setText("Stopped"); }
            else if (selectedSong != null) play(selectedSong);
        });
        p.add(playBtn, BorderLayout.CENTER);
        p.add(new AudioVisualizerPanel(player), BorderLayout.EAST);
        return p;
    }

    private void selectSong(Song s) {
        if (s == null) return;
        selectedSong = s;
        lblSelTitle.setText(s.title); lblSelArtist.setText(s.artist); lblSelGenre.setText(s.genre);
    }

    private void play(Song s) {
        player.play(s);
        graphPanel.update(selectedGraphSong, s);
    }

    private void playNextFromQueue() {
        Song next = playQueue.dequeue();
        if (next == null) {
            List<Song> list = activePlaylist.toList(); if (list.isEmpty()) return;
            int nextIdx = 0; Song cur = player.getCurrentSong();
            if (cur != null) {
                for (int i=0; i<list.size(); i++) {
                    if (list.get(i).id == cur.id) { nextIdx = (i+1)%list.size(); break; }
                }
            }
            next = list.get(nextIdx);
        }
        play(next); refreshQueue();
    }

    private void refreshPlaylist() {
        playlistModel.setRowCount(0); List<Song> list = activePlaylist.toList();
        for (int i = 0; i < list.size(); i++) {
            Song s = list.get(i); playlistModel.addRow(new Object[]{i+1, s.title, s.artist, s.genre, s.filename});
        }
    }

    private void refreshQueue() {
        queueModel.setRowCount(0); List<Song> list = playQueue.toList();
        for (int i = 0; i < list.size(); i++) {
            Song s = list.get(i); queueModel.addRow(new Object[]{i+1, s.title, s.artist});
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(SocialMusicApp::new);
    }
}
