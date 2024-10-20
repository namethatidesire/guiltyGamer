import java.awt.*;
import javax.swing.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;

public class GuiltyGamerTray {
    private TrayIcon trayIcon;
    private Timer timer;
    private LocalDateTime startTime;
    private Duration playtime = Duration.ZERO;
    private boolean running = false;

    public static void main(String[] args) {
        if (!SystemTray.isSupported()) {
            System.out.println("This system does not support task tray functionality.");
            return;
        }
        GuiltyGamerTray ggtracker = new GuiltyGamerTray();
        ggtracker.startTrayIcon();
        ggtracker.start();
    }

    public void startTrayIcon() {
        PopupMenu menu = new PopupMenu();
        MenuItem open = new Menu("Open");
        MenuItem exit = new Menu("Exit");

        //open.addActionListener();
        menu.add(open);

        exit.addActionListener(e -> System.exit(0));
        menu.add(exit);

        trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage("placeholder.png"), "Guilty Gamer");
        trayIcon.setImageAutoSize(true);
        trayIcon.setPopupMenu(menu);
        trayIcon.addActionListener(e -> viewStats());

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
           e.printStackTrace();
        }
    }

    public void start() {
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                peekGame();
            }
        }, 5000);
    }

    public void peekGame() {
        try {
            Process processList = Runtime.getRuntime().exec("tasklist");
            boolean nowRunning = false;

            BufferedReader buffRead = new BufferedReader(new InputStreamReader(processList.getInputStream()));
            String process;

            while (!nowRunning && (process = buffRead.readLine()) != null) {
                if (process.contains("VALORANT-Win64-Shipping.exe")) {
                    nowRunning = true;
                }
            }

            if (!running && nowRunning) {
                startTime = LocalDateTime.now();
                running = true;
                trayIcon.setToolTip("VALORANT playtime is being recorded.");
                return;
            }

            if (running && !nowRunning) {
                Duration sessionTime = Duration.between(startTime, LocalDateTime.now());
                running = false;
                playtime = playtime.plus(sessionTime);
                trayIcon.setToolTip("VALORANT is not currently running.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void viewStats() {
        // Show a simple GUI with the total playtime
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Game Playtime Statistics");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(300, 200);
            frame.setLayout(new BorderLayout());

            // Calculate hours, minutes, and seconds of total playtime
            long hours = playtime.toHours();
            long minutes = playtime.toMinutesPart();
            long seconds = playtime.toSecondsPart();

            JLabel statsLabel = new JLabel("Total playtime: " + hours + " hours " + minutes + " minutes " + seconds + " seconds", JLabel.CENTER);
            frame.add(statsLabel, BorderLayout.CENTER);

            // Show the GUI
            frame.setVisible(true);
        });
    }
}
