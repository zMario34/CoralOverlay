package tech.zmario.coraloverlay.windows;

import tech.zmario.coraloverlay.CoralOverlay;
import tech.zmario.coraloverlay.enums.RankColor;
import tech.zmario.coraloverlay.manager.UserManager;
import tech.zmario.coraloverlay.threads.LogsReaderRunnable;
import tech.zmario.coraloverlay.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class OverlayFrame extends JFrame {

    private final UserManager userManager;
    private final CoralOverlay coralOverlay;

    private final JPanel playersPanel = new JPanel();
    private final JPanel tagPanel = new JPanel();
    private final JPanel streakPanel = new JPanel();
    private final JPanel fkdrPanel = new JPanel();
    private final JPanel wlrPanel = new JPanel();
    private final JPanel finalsPanel = new JPanel();
    private final JPanel winsPanel = new JPanel();

    private int maxPlayers = 0;
    private double xOffset = 0;
    private double yOffset = 0;

    private OverlayFrame(CoralOverlay coralOverlay) {
        this.coralOverlay = coralOverlay;
        this.userManager = coralOverlay.getUserManager();

        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);

        setLocation(20, 20);
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 20));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                setLocation(event.getXOnScreen() - (int) xOffset, event.getYOnScreen() - (int) yOffset);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                xOffset = event.getX();
                yOffset = event.getY();
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == 3) setExtendedState(Frame.ICONIFIED);
                else if (event.getButton() == 2) System.exit(0);
            }
        });
    }

    public static OverlayFrame create(CoralOverlay coralOverlay) {
        return new OverlayFrame(coralOverlay);
    }

    public void start() {
        ImageIcon icon = TextUtils.LOGO_RESIZED;
        JLabel iconLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(110, 60, Image.SCALE_SMOOTH)));

        JLabel playersLabel = TextUtils.label("<b>GIOCATORI</b>", 0, 0, 21, Color.WHITE);
        JLabel tagLabel = TextUtils.label("<b>LIVELLO</b>", 0, 20, 21, Color.WHITE);
        JLabel streakLabel = TextUtils.label("<b>STRK</b>", 0, 0, 21, Color.WHITE);
        JLabel fkdrLabel = TextUtils.label("<b>FKDR</b>", 0, 0, 21, Color.WHITE);
        JLabel wlrLabel = TextUtils.label("<b>WLR</b>", 0, 0, 21, Color.WHITE);
        JLabel finals = TextUtils.label("<b>FNLS</b>", 0, 0, 21, Color.WHITE);
        JLabel winsLabel = TextUtils.label("<b>WINS</b>", 0, 0, 21, Color.WHITE);

        playersPanel.add(playersLabel);
        tagPanel.add(tagLabel);
        streakPanel.add(streakLabel);
        fkdrPanel.add(fkdrLabel);
        wlrPanel.add(wlrLabel);
        finalsPanel.add(finals);
        winsPanel.add(winsLabel);

        add(iconLabel, playersPanel, tagPanel, streakPanel, fkdrPanel, wlrPanel, finalsPanel, winsPanel);
        applyLayout(playersPanel, tagPanel, streakPanel, fkdrPanel, wlrPanel, finalsPanel, winsPanel);

        setIconImage(icon.getImage());

        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
        setSize(900, 700);
        update();

        if (false) loadDefaultPlayers(); // debug
    }

    private void loadDefaultPlayers() {
        addPlayer("UltimateITA");
        addPlayer("Ytnoos");
        addPlayer("zMario34");
        addPlayer("DavideTW");
        addPlayer("marchitobebee");
        addPlayer("DrElu");
        addPlayer("MelaOp");
        addPlayer("asynchunk");
        addPlayer("playerindisguise");
        addPlayer("Jamaicah");
    }

    public void update() {
        for (String playerName : userManager.getPlayerNames()) addPlayer(playerName);

        new LogsReaderRunnable(coralOverlay);
    }

    private void applyLayout(JPanel... panels) {
        for (JPanel panel : panels) {
            panel.setSize(30, 30);
            panel.setAlignmentY(Component.TOP_ALIGNMENT);
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(0, 0, 0, 0));
            panel.getComponent(0).setName("title-label");
        }
    }

    public void add(Component... components) {
        for (Component component : components) super.add(component);
    }

    public void addPlayer(String playerName) {
        if (playerName.isEmpty() || playerName.isBlank() || userManager.getPlayerNames().contains(playerName) ||
                playerName.equalsIgnoreCase(coralOverlay.getPlayerName())) return;

        try {
            Thread.sleep(60L);
        } catch (InterruptedException e) {
        }

        userManager.getPlayerNames().add(playerName);
        userManager.getUser(playerName).thenAccept(bedWarsUser -> userManager.getPrefix(playerName).thenAccept(prefix -> {
            double fkdr = Math.round((bedWarsUser.getFinalKills() * 100.0 / bedWarsUser.getFinalDeaths())) / 100.0;
            double wlr = Math.round((double) bedWarsUser.getWins() / (double) bedWarsUser.getFinalDeaths() * 100.0) / 100.0;
            RankColor rankColor = TextUtils.colorizeRank(prefix);
            Color levelColor = TextUtils.colorizeLevel(bedWarsUser.getLevel());

            String name = rankColor == RankColor.USER ?
                    playerName : "<b>" + rankColor.name() + "</b> " + playerName;

            Color textColor = RankColor.USER.getColor();

            playersPanel.add(TextUtils.label(name, 0, 0, 21, rankColor.getColor()));
            tagPanel.add(TextUtils.label("[" + bedWarsUser.getLevel() + "]", 0, 0, 21, levelColor));
            streakPanel.add(TextUtils.label(bedWarsUser.getWinStreak() + "", 0, 0, 21, textColor));
            fkdrPanel.add(TextUtils.label(fkdr + "", 0, 0, 21, TextUtils.getFKDRColor(fkdr)));
            wlrPanel.add(TextUtils.label(wlr + "", 0, 0, 21, TextUtils.getWLRColor(wlr)));
            finalsPanel.add(TextUtils.label(bedWarsUser.getFinalKills() + "", 0, 0, 21, textColor));
            winsPanel.add(TextUtils.label(bedWarsUser.getWins() + "", 0, 0, 21, textColor));
        }));
    }

    public void removePlayer(String playerName) {
        if (!userManager.getPlayerNames().remove(playerName)) return;

        for (Component component : playersPanel.getComponents()) {
            if (((JLabel) component).getText().contains(playerName)) {
                int index = playersPanel.getComponentZOrder(component);

                playersPanel.remove(index);
                tagPanel.remove(index);
                streakPanel.remove(index);
                fkdrPanel.remove(index);
                wlrPanel.remove(index);
                finalsPanel.remove(index);
                winsPanel.remove(index);
            }
        }

        updatePanel(playersPanel, tagPanel, streakPanel, fkdrPanel, wlrPanel, finalsPanel, winsPanel);
    }

    public void clearAll() {
        clearPanel(playersPanel, tagPanel, streakPanel, fkdrPanel, wlrPanel, finalsPanel, winsPanel);

        updatePanel(playersPanel, tagPanel, streakPanel, fkdrPanel, wlrPanel, finalsPanel, winsPanel);

        userManager.getPlayerNames().clear();
    }

    private void clearPanel(JPanel... panels) {
        for (JPanel panel : panels) {
            for (Component component : panel.getComponents()) {
                if (component.getName() != null && component.getName().equals("title-label")) continue;

                panel.remove(component);
            }
        }
    }

    private void updatePanel(JPanel... panels) {
        for (JPanel panel : panels) {
            panel.revalidate();
            panel.repaint();
        }
    }

    public void setMaxPlayers(int newPlayers) {
        if (maxPlayers > 0 && newPlayers != maxPlayers) clearAll();

        maxPlayers = newPlayers;
    }

    public void setDisguisedName(String disguisedName) {
        coralOverlay.setDisguisedName(disguisedName);
    }
}
