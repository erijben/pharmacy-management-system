package Pharmacy_client;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PharmacistHomePanel extends JPanel {

    public PharmacistHomePanel(String username) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // === Panel contenant la barre de recherche + titre ===
        JPanel headerSection = new JPanel();
        headerSection.setLayout(new BoxLayout(headerSection, BoxLayout.Y_AXIS));
        headerSection.setBackground(Color.WHITE);

        JTextField searchBar = new JTextField(" Search...");
        searchBar.setMaximumSize(new Dimension(500, 32));
        searchBar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchBar.setForeground(Color.GRAY);
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        headerSection.add(searchBar);

        headerSection.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("💊 Welcome back, pharmacist!");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        headerSection.add(title);

        add(headerSection, BorderLayout.NORTH);

        // === Données dynamiques depuis serveur ===
        String[] counts = fetchMedicineCounts();
        if (counts.length < 3) counts = new String[]{"0", "0", "0"};

        // === Grille principale
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);

        // === Cartes Statistiques ===
        JPanel statCards = new JPanel(new GridLayout(1, 3, 20, 0));
        statCards.setBackground(Color.WHITE);
        statCards.add(createStatCard("Total Medicines", counts[0], "/images/pill.png", new Color(220, 237, 200)));
        statCards.add(createStatCard("Total Bills", counts[1], "/images/bill.png", new Color(197, 225, 250)));
        statCards.add(createStatCard("Stock", counts[2], "/images/stock.png", new Color(255, 236, 179)));

        // === Graphiques
        JPanel chartPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        chartPanel.add(createImageLabel("/images/chart_medicines_by_company_resized.png", "💼 Medicines by Company"));
        chartPanel.add(createImageLabel("/images/chart_monthly_sales_resized.png", "📈 Monthly Sales"));

        // === Informations
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(250, 250, 250));
        infoPanel.setBorder(BorderFactory.createTitledBorder("📊 Session Info"));

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm a");

        infoPanel.add(createInfoLine("/images/calendar.png", "Today: " + now.format(dateFormat)));
        infoPanel.add(createInfoLine("/images/clock.png", "Time: " + now.format(timeFormat)));
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(createInfoLine("/images/quote.png", "“A healthy customer is the best prescription for success.”"));

        mainPanel.add(statCards);
        mainPanel.add(chartPanel);
        mainPanel.add(infoPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private String[] fetchMedicineCounts() {
        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            pw.println("countMedicines");
            String response = br.readLine();
            if (response != null && !response.equals("fail")) {
                return response.split("::"); // total medicines, total bills, total stock
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{"0", "0", "0"};
    }

    private JPanel createStatCard(String title, String value, String iconPath, Color bgColor) {
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(150, 100));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
        Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaled));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel numberLabel = new JLabel(value);
        numberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        textPanel.add(titleLabel);
        textPanel.add(numberLabel);

        card.add(iconLabel);
        card.add(textPanel);

        return card;
    }

    private JLabel createImageLabel(String path, String title) {
        ImageIcon rawIcon = new ImageIcon(getClass().getResource(path));
        Image scaled = rawIcon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(scaled));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createTitledBorder(title));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }

    private JPanel createInfoLine(String iconPath, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
        Image scaled = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaled));

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        panel.add(iconLabel);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(textLabel);

        return panel;
    }
}
