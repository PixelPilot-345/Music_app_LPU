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

    private SongWavPlayer player;
    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);
    private Song selectedSong;
    private DLLNode currentPlayingNode = null;

    private JLabel lblSelTitle, lblSelArtist, lblSelGenre;
    private JLabel lblTitle;
    private JButton btnPlayPause;
    private DefaultTableModel playlistModel, queueModel;
    private DefaultListModel<Song> dashboardListModel = new DefaultListModel<>();
    private JComboBox<String> fF;

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

        if (!catalog.isEmpty()) {
            selectSong(catalog.get(0));
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

        String[] items = {"Dashboard", "DLL Playlist", "FIFO Queue"};
        String[] cards = {"DASHBOARD", "PLAYLIST", "QUEUE"};
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
        
        JPanel topPanel = new JPanel(new BorderLayout(5, 5)); topPanel.setOpaque(false);

        JPanel topSortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); topSortPanel.setOpaque(false);
        JLabel lblSort = new JLabel("Sort:"); styleLabel(lblSort, new Font("Segoe UI", Font.BOLD, 12), COLOR_TEXT);
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
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); searchPanel.setOpaque(false);
        JTextField txtSearch = new JTextField(8);
        JButton btnSearch = new JButton("Search"); styleBtn(btnSearch);
        btnSearch.addActionListener(e -> {
            String q = txtSearch.getText().trim();
            if (!q.isEmpty()) {
                CatalogSorter.sortByTitle(catalog);
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
                activePlaylist.add(s);
                JOptionPane.showMessageDialog(this, "Added to DLL Playlist!");
            }
        });
        
        btnAddQ.addActionListener(e -> {
            Song s = list.getSelectedValue();
            if (s != null) {
                playQueue.enqueue(s);
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
        JTextField fT = new JTextField(8); JTextField fA = new JTextField(8); JTextField fG = new JTextField(6);
        fF = new JComboBox<>();
        for (Song s : catalog) fF.addItem(s.filename);
        JButton addBtn = new JButton("Add Song"); styleBtn(addBtn);
        addBtn.addActionListener(e -> {
            if (fT.getText().isEmpty() || fA.getText().isEmpty()) return;
            Song s = new Song(catalog.size()+1, fT.getText(), fA.getText(), fG.getText(), (String)fF.getSelectedItem());
            catalog.add(s); activePlaylist.add(s); refreshPlaylist();
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
        dequeueBtn.addActionListener(e -> playNext());
        controls.add(dequeueBtn);
        p.add(controls, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createBottomBar() {
        JPanel p = new JPanel(new BorderLayout(15, 5)); p.setBackground(COLOR_SIDEBAR);
        p.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        lblTitle = new JLabel("Not Playing"); styleLabel(lblTitle, new Font("Segoe UI", Font.BOLD, 13), COLOR_TEXT);
        p.add(lblTitle, BorderLayout.WEST);

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
        lblTitle.setText("Playing: " + s.title);
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
