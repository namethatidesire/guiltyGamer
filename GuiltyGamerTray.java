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
    /* Debug Opts */
    private final boolean verbose = false;

    /* General Attributes */
    private final boolean darkMode = false;
    private Image smile;
    private Image frown;

    private final Image smileDark = Toolkit.getDefaultToolkit().createImage("smile.png");
    private final Image smileLight = Toolkit.getDefaultToolkit().createImage("smile-light.png");
    private final Image frownDark = Toolkit.getDefaultToolkit().createImage("frown.png");
    private final Image frownLight = Toolkit.getDefaultToolkit().createImage("frown-light.png");

    private TrayIcon trayIcon;
    private boolean running = false;

    /* JFrame (GUI) */
    private final JFrame frame = new JFrame("Guilty Gamer");
    private final JLabel statsLabel = new JLabel("", JLabel.CENTER);

    /* Time */
    private LocalDateTime startTime;
    private Duration playtime = Duration.ZERO;
    private long hours, minutes, seconds;

    public static void main(String[] args) {
        if (!SystemTray.isSupported()) {
            System.out.println("This system does not support task tray functionality.");
            return;
        }
        GuiltyGamerTray ggtracker = new GuiltyGamerTray();
        ggtracker.startTrayIcon();
        ggtracker.startTask();
    }

    /* startTrayIcon()


     */
    public void startTrayIcon() {
        if (darkMode) {
            smile = smileDark;
            frown = frownDark;
        } else {
            smile = smileLight;
            frown = frownLight;
        }

        PopupMenu menu = new PopupMenu();
        MenuItem open = new Menu("Open");
        MenuItem exit = new Menu("Exit");

        //open.addActionListener();
        menu.add(open);

        exit.addActionListener(e -> System.exit(0));
        menu.add(exit);

        trayIcon = new TrayIcon(smile, "Guilty Gamer");
        trayIcon.setImageAutoSize(true);
        trayIcon.setPopupMenu(menu);
        trayIcon.addActionListener(e -> openGUI());

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
           e.printStackTrace();
        }
        if (verbose) System.out.println("Created tray icon.");
    }

    /* startTask()


     */
    public void startTask() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new BorderLayout());

        statsLabel.setText("Total playtime: " + hours + " hours " + minutes + " minutes " + seconds + " seconds");
        frame.add(statsLabel, BorderLayout.CENTER);
        if (verbose) System.out.println("Created frame & label.");

        Timer timer = new Timer(true);
        if (verbose) System.out.println("Created timer daemon.");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                peekGame();
                refreshGUI();
            }
        }, 0,5050);
    }

    /* openGUI()


     */
    public void openGUI() {
        if (frame.isActive()) {
            frame.toFront();
            if (verbose) System.out.println("Bring GUI window to front.");
            return;
        }

        // Show a simple GUI with the total playtime
        SwingUtilities.invokeLater(() -> {
            // Calculate hours, minutes, and seconds of total playtime
            refreshGUI();

            // Show the GUI
            frame.setVisible(true);
            if (verbose) System.out.println("Open new GUI window.");
        });
    }

    /* refreshGUI()


     */
    public void refreshGUI() {
        hours = playtime.toHours();
        minutes = playtime.toMinutesPart();
        seconds = playtime.toSecondsPart();

        statsLabel.setText("Total playtime: " + hours + " hours " + minutes + " minutes " + seconds + " seconds");
        if (verbose) System.out.println("Refreshed playtime.");
    }

    /* peekGame()


     */
    public void peekGame() {
        try {
            Process processList = Runtime.getRuntime().exec("tasklist");
            boolean nowRunning = false;

            BufferedReader buffRead = new BufferedReader(new InputStreamReader(processList.getInputStream()));
            String process;

            while (!nowRunning && (process = buffRead.readLine()) != null) {
                if (process.toLowerCase().contains("valorant.exe")) {
                    nowRunning = true;
                }
            }

            System.out.println("running: " + running + " nowRunning: " + nowRunning);

            // Game opened
            if (!running && nowRunning) {
                startTime = LocalDateTime.now();
                running = true;
                trayIcon.setImage(frown);
                trayIcon.setToolTip("VALORANT playtime is being recorded.");
                if (verbose) System.out.println("Game started running.");
                return;
            }

            // Game running
            if (running && nowRunning) {
                playtime = Duration.between(startTime, LocalDateTime.now());
                if (verbose) System.out.println("Game still running.");
                return;
            }

            // Game closed
            if (running && !nowRunning) {
                running = false;
                trayIcon.setImage(smile);
                trayIcon.setToolTip("VALORANT is not currently running.");
                if (verbose) System.out.println("Game closed.");
                return;
            }

            if (verbose) System.out.println("Game not running.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
