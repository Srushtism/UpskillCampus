package musicplayer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class MusicPlayerApp extends JFrame {

    JTable songTable, playlistTable;

    DefaultTableModel songModel, playlistModel;

    JComboBox<String> playlistBox;

    Clip clip;

    String currentPath = "";

    public MusicPlayerApp() {

        setTitle("Music Player");

        setSize(850, 550);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        DBConnection.createTables();

        songModel = new DefaultTableModel(
                new String[]{"ID", "Title", "Artist", "Path"}, 0);

        songTable = new JTable(songModel);

        playlistModel = new DefaultTableModel(
                new String[]{"ID", "Title", "Artist", "Path"}, 0);

        playlistTable = new JTable(playlistModel);

        playlistBox = new JComboBox<>();

        JButton addSongBtn = new JButton("Import Song");

        JButton createPlaylistBtn =
                new JButton("Create Playlist");

        JButton addToPlaylistBtn =
                new JButton("Add To Playlist");

        JButton removeBtn =
                new JButton("Remove Song");

        JButton playBtn = new JButton("Play");

        JButton pauseBtn = new JButton("Pause");

        JButton stopBtn = new JButton("Stop");

        JPanel topPanel = new JPanel();

        topPanel.add(addSongBtn);

        topPanel.add(createPlaylistBtn);

        topPanel.add(new JLabel("Playlist"));

        topPanel.add(playlistBox);

        topPanel.add(addToPlaylistBtn);

        topPanel.add(removeBtn);

        JPanel bottomPanel = new JPanel();

        bottomPanel.add(playBtn);

        bottomPanel.add(pauseBtn);

        bottomPanel.add(stopBtn);

        JSplitPane splitPane = new JSplitPane(

                JSplitPane.VERTICAL_SPLIT,

                new JScrollPane(songTable),

                new JScrollPane(playlistTable)
        );

        splitPane.setDividerLocation(230);

        add(topPanel, BorderLayout.NORTH);

        add(splitPane, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        loadSongs();

        loadPlaylists();

        addSongBtn.addActionListener(e -> importSong());

        createPlaylistBtn.addActionListener(
                e -> createPlaylist());

        addToPlaylistBtn.addActionListener(
                e -> addSongToPlaylist());

        playlistBox.addActionListener(
                e -> loadPlaylistSongs());

        removeBtn.addActionListener(
                e -> removeSongFromPlaylist());

        playBtn.addActionListener(
                e -> playSelectedSong());

        pauseBtn.addActionListener(
                e -> pauseSong());

        stopBtn.addActionListener(
                e -> stopSong());

        setVisible(true);
    }

    void loadSongs() {

        songModel.setRowCount(0);

        try (

                Connection con =
                        DBConnection.getConnection();

                Statement st =
                        con.createStatement();

                ResultSet rs =
                        st.executeQuery(
                                "SELECT * FROM songs")
        ) {

            while (rs.next()) {

                songModel.addRow(new Object[]{

                        rs.getInt("id"),

                        rs.getString("title"),

                        rs.getString("artist"),

                        rs.getString("path")
                });
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading songs");
        }
    }

    void importSong() {

        JFileChooser fc = new JFileChooser();

        int result = fc.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            File file = fc.getSelectedFile();

            String title = file.getName();

            String artist =
                    JOptionPane.showInputDialog(
                            "Enter Artist Name");

            try (

                    Connection con =
                            DBConnection.getConnection();

                    PreparedStatement ps =
                            con.prepareStatement(

                                    "INSERT INTO songs(title,artist,path)"
                                            + " VALUES(?,?,?)")
            ) {

                ps.setString(1, title);

                ps.setString(2, artist);

                ps.setString(3,
                        file.getAbsolutePath());

                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        this,
                        "Song Imported");

                loadSongs();

            } catch (Exception e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Error Importing Song");
            }
        }
    }

    void createPlaylist() {

        String name =
                JOptionPane.showInputDialog(
                        "Enter Playlist Name");

        if (name != null && !name.isEmpty()) {

            try (

                    Connection con =
                            DBConnection.getConnection();

                    PreparedStatement ps =
                            con.prepareStatement(

                                    "INSERT INTO playlists(name)"
                                            + " VALUES(?)")
            ) {

                ps.setString(1, name);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        this,
                        "Playlist Saved");

                loadPlaylists();

            } catch (Exception e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Playlist Exists");
            }
        }
    }

    void loadPlaylists() {

        playlistBox.removeAllItems();

        try (

                Connection con =
                        DBConnection.getConnection();

                Statement st =
                        con.createStatement();

                ResultSet rs =
                        st.executeQuery(
                                "SELECT name FROM playlists")
        ) {

            while (rs.next()) {

                playlistBox.addItem(
                        rs.getString("name"));
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error Loading Playlist");
        }
    }

    int getPlaylistId(String name)
            throws Exception {

        try (

                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(

                                "SELECT id FROM playlists "
                                        + "WHERE name=?")
        ) {

            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return rs.getInt("id");
            }
        }

        return -1;
    }

    void addSongToPlaylist() {

        int row = songTable.getSelectedRow();

        if (row == -1 ||
                playlistBox.getSelectedItem() == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Select Song and Playlist");

            return;
        }

        int songId =
                (int) songModel.getValueAt(row, 0);

        String playlistName =
                playlistBox.getSelectedItem().toString();

        try {

            int playlistId =
                    getPlaylistId(playlistName);

            try (

                    Connection con =
                            DBConnection.getConnection();

                    PreparedStatement ps =
                            con.prepareStatement(

                                    "INSERT INTO playlist_songs "
                                            + "VALUES(?,?)")
            ) {

                ps.setInt(1, playlistId);

                ps.setInt(2, songId);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        this,
                        "Song Added");

                loadPlaylistSongs();
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error Adding Song");
        }
    }

    void loadPlaylistSongs() {

        playlistModel.setRowCount(0);

        if (playlistBox.getSelectedItem() == null)
            return;

        try {

            int playlistId =
                    getPlaylistId(
                            playlistBox.getSelectedItem()
                                    .toString());

            String sql =
                    "SELECT s.id,s.title,s.artist,s.path "
                            + "FROM songs s "
                            + "JOIN playlist_songs ps "
                            + "ON s.id=ps.song_id "
                            + "WHERE ps.playlist_id=?";

            try (

                    Connection con =
                            DBConnection.getConnection();

                    PreparedStatement ps =
                            con.prepareStatement(sql)
            ) {

                ps.setInt(1, playlistId);

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {

                    playlistModel.addRow(
                            new Object[]{

                                    rs.getInt("id"),

                                    rs.getString("title"),

                                    rs.getString("artist"),

                                    rs.getString("path")
                            });
                }
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error Loading Songs");
        }
    }

    void removeSongFromPlaylist() {

        int row =
                playlistTable.getSelectedRow();

        if (row == -1 ||
                playlistBox.getSelectedItem() == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Select Playlist Song");

            return;
        }

        int songId =
                (int) playlistModel.getValueAt(row, 0);

        try {

            int playlistId =
                    getPlaylistId(
                            playlistBox.getSelectedItem()
                                    .toString());

            try (

                    Connection con =
                            DBConnection.getConnection();

                    PreparedStatement ps =
                            con.prepareStatement(

                                    "DELETE FROM playlist_songs "
                                            + "WHERE playlist_id=? "
                                            + "AND song_id=?")
            ) {

                ps.setInt(1, playlistId);

                ps.setInt(2, songId);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        this,
                        "Song Removed");

                loadPlaylistSongs();
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error Removing Song");
        }
    }

    void playSelectedSong() {

        int row =
                playlistTable.getSelectedRow();

        if (row != -1) {

            currentPath =
                    playlistModel.getValueAt(row, 3)
                            .toString();

        } else {

            row = songTable.getSelectedRow();

            if (row != -1) {

                currentPath =
                        songModel.getValueAt(row, 3)
                                .toString();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Select Song");

                return;
            }
        }

        try {

            stopSong();

            AudioInputStream audio =
                    AudioSystem.getAudioInputStream(
                            new File(currentPath));

            clip = AudioSystem.getClip();

            clip.open(audio);

            clip.start();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Only WAV Files Supported");
        }
    }

    void pauseSong() {

        if (clip != null &&
                clip.isRunning()) {

            clip.stop();
        }
    }

    void stopSong() {

        if (clip != null) {

            clip.stop();

            clip.close();
        }
    }

    public static void main(String[] args) {

        new MusicPlayerApp();
    }
}