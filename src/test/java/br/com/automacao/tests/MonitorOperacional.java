package br.com.automacao.tests;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MonitorOperacional extends JFrame {

    // Configurações de Conexão (Baseadas na sua imagem do DBeaver)
    private final String URL = "jdbc:oracle:thin:@10.4.8.189:1521/fepp"; 
    private final String USER = "990681";
    private final String PASS = "kK2THSxr39fMLVP"; // <--- COLOQUE SUA SENHA AQUI

    private JTabbedPane tabbedPane;
    private DefaultTableModel modelCanais, modelLogs, modelPix;
    private JTable tableCanais, tableLogs, tablePix;

    public MonitorOperacional() {
        setTitle("Monitoração Banco Produção BLK - QA Tool");
        setSize(1200, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        
        // Carregamento inicial
        atualizarTudo();
        
        // Timer para auto-refresh a cada 5 minutos
        new Timer(300000, e -> atualizarTudo()).start();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // Aba 1: Diferença de Canais (Faltam)
        modelCanais = new DefaultTableModel(new String[]{"SISTEMA", "MAX_CANAL", "RECEBIDO_BLK", "FALTAM_CNL"}, 0);
        tableCanais = new JTable(modelCanais);
        configurarAlertaVermelho(tableCanais, 3); // Coluna FALTAM_CNL
        tabbedPane.addTab("Canais (Diferença)", new JScrollPane(tableCanais));

        // Aba 2: Subidor e Processador Logs
        modelLogs = new DefaultTableModel(new String[]{"SISTEMA", "STATUS", "DATA REF", "NSU_ULTIMO"}, 0);
        tableLogs = new JTable(modelLogs);
        tabbedPane.addTab("Logs Processamento", new JScrollPane(tableLogs));

        // Aba 3: Alertas PIX, Baixas e SIGAF
        modelPix = new DefaultTableModel(new String[]{"MONITORAÇÃO", "VALOR / QTD"}, 0);
        tablePix = new JTable(modelPix);
        configurarAlertaVermelho(tablePix, 1); // Coluna QTD
        tabbedPane.addTab("Alertas & PIX", new JScrollPane(tablePix));

        // Painel Inferior
        JPanel painelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnUpdate = new JButton("Atualizar Agora");
        btnUpdate.addActionListener(e -> atualizarTudo());
        
        JButton btnCorrecao = new JButton("Corrigir Lançamentos Pendentes");
        btnCorrecao.setForeground(Color.RED);
        btnCorrecao.addActionListener(e -> executarUpdateLDC());

        painelInferior.add(btnCorrecao);
        painelInferior.add(btnUpdate);

        add(tabbedPane, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);
    }

    private void atualizarTudo() {
        new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                carregarCanais(conn);
                carregarLogs(conn);
                carregarAlertasGerais(conn);
                System.out.println("Monitoração atualizada em: " + new java.util.Date());
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, "Erro ao conectar no Oracle: " + e.getMessage()));
            }
        }).start();
    }

    private void carregarCanais(Connection conn) throws SQLException {
        modelCanais.setRowCount(0);
        // Array com as 4 queries de sistemas
        String[] queries = {
            "select 'IBK' as SISTEMA, (SELECT max(LOGNSU) FROM NTBASA.LOG WHERE LOGDATREF >= TRUNC(SYSDATE)) as MAX_CANAL, (SELECT max(LOGNSU) FROM BLBASA.LOG WHERE lognsugrl IN (select ltrultnsu from blbasa.ltr where ptacodori=995 and ltrdatref>=trunc(sysdate) and ltrtip=0)) as RECEBIDO_BLK, ((SELECT max(LOGNSU) FROM NTBASA.LOG WHERE LOGDATREF >= TRUNC(SYSDATE)) - (SELECT max(LOGNSU) FROM BLBASA.LOG WHERE lognsugrl IN (select ltrultnsu from blbasa.ltr where ptacodori=995 and ltrdatref>=trunc(sysdate) and ltrtip=0))) as FALTAM_CNL from dual",
            "select 'Mobile' as SISTEMA, (SELECT max(LOGNSU) FROM MOBPRDBSA.LOG WHERE LOGDATREF >= TRUNC(SYSDATE)) as MAX_CANAL,(SELECT max(LOGNSU) FROM BLBASA.LOG WHERE lognsugrl IN (select ltrultnsu from blbasa.ltr where ptacodori=989 and ltrdatref>=trunc(sysdate) and ltrtip=0)) as RECEBIDO_BLK, ((SELECT max(LOGNSU) FROM MOBPRDBSA.LOG WHERE LOGDATREF >= TRUNC(SYSDATE)) - (SELECT max(LOGNSU) FROM BLBASA.LOG WHERE lognsugrl IN (select ltrultnsu from blbasa.ltr where ptacodori=989 and ltrdatref>=trunc(sysdate) and ltrtip=0))) as FALTAM_CNL from dual",
            "select 'Extracash' as SISTEMA, (SELECT max(LOGNSU) FROM EXTRACASH.FTN_LOG WHERE LOGDATREF >= TRUNC(SYSDATE)) as MAX_CANAL,(SELECT max(LOGNSU) FROM BLBASA.LOG WHERE lognsugrl IN (select ltrultnsu from blbasa.ltr where ptacodori=8999 and ltrdatref>=trunc(sysdate) and ltrtip=0)) as RECEBIDO_BLK, ((SELECT max(LOGNSU) FROM EXTRACASH.FTN_LOG WHERE LOGDATREF >= TRUNC(SYSDATE)) - (SELECT max(LOGNSU) FROM BLBASA.LOG WHERE lognsugrl IN (select ltrultnsu from blbasa.ltr where ptacodori=8999 and ltrdatref>=trunc(sysdate) and ltrtip=0))) as FALTAM_CNL from dual",
            "select 'NextBank' as SISTEMA, (SELECT max(LOGNSU) FROM NEXTBANK.FTN_LOG WHERE LOGDATREF >= TRUNC(SYSDATE)) as MAX_CANAL, (SELECT max(LOGNSU) FROM BLBASA.LOG WHERE lognsugrl IN (select ltrultnsu from blbasa.ltr where ptacodori=999 and ltrdatref>=trunc(sysdate) and ltrtip=0)) as RECEBIDO_BLK, ((SELECT max(LOGNSU) FROM NEXTBANK.FTN_LOG WHERE LOGDATREF >= TRUNC(SYSDATE)) - (SELECT max(LOGNSU) FROM BLBASA.LOG WHERE lognsugrl IN (select ltrultnsu from blbasa.ltr where ptacodori=999 and ltrdatref>=trunc(sysdate) and ltrtip=0))) as FALTAM_CNL from dual"
        };

        for (String sql : queries) {
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    modelCanais.addRow(new Object[]{rs.getString(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)});
                }
            }
        }
    }

    private void carregarLogs(Connection conn) throws SQLException {
        modelLogs.setRowCount(0);
        String sql = "select CASE ptacodori WHEN 995 THEN 'IBK' WHEN 989 THEN 'Mobile' WHEN 999 THEN 'Nextbank' WHEN 8999 THEN 'Extracash' ELSE '' END AS Sistema, " +
                     "ltrdatref, ltrultnsu, CASE LTRTIP WHEN 0 THEN 'RECEBIDO' WHEN 1 THEN 'PROCESSADO' WHEN 3 THEN 'INFORMADO' WHEN 5 THEN 'POSICAO LOG' ELSE '' END AS Status " +
                     "from blbasa.ltr where ptacodori IN (995,989,999,8999) and ltrdatref>=trunc(sysdate) and ltrtip IN (0,1,3,5) ORDER BY ptacodori, LTRTIP";
        
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                modelLogs.addRow(new Object[]{rs.getString(1), rs.getString(4), rs.getDate(2), rs.getLong(3)});
            }
        }
    }

    private void carregarAlertasGerais(Connection conn) throws SQLException {
        modelPix.setRowCount(0);
        
        // 1. Pix Sem Débito
        String sqlPix = "select count(*) from blbasa.tfp INNER JOIN blbasa.aut ON (tfp.AUTNSUBLK = aut.AUTNSUBLK) where tfp.tfptip = 2 and tfp.TFPSTA = 2 and aut.autsta NOT IN (3,4) and ((sysdate - aut.autdathorfim) * 24 * 60 * 60) >= 120";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlPix)) {
            if (rs.next()) modelPix.addRow(new Object[]{"Pix Sem Débito (>120s)", rs.getInt(1)});
        }

        // 2. Baixa de Títulos
        String sqlTitulos = "SELECT count(*) FROM blbasa.cmc WHERE trunc(CMCDATHORCRI) = trunc(sysdate) AND CMCSTA = 0 AND cmctip = 2";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlTitulos)) {
            if (rs.next()) modelPix.addRow(new Object[]{"Baixa Títulos Pendentes", rs.getInt(1)});
        }

        // 3. SIGAF
        String sqlSigaf = "SELECT count(*) FROM BLBASA.LOG WHERE logdatref = trunc(sysdate) AND LOGTERCOD = 'SIGAF'";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlSigaf)) {
            if (rs.next()) modelPix.addRow(new Object[]{"Logs SIGAF (Hoje)", rs.getInt(1)});
        }
    }

    private void executarUpdateLDC() {
        int confirm = JOptionPane.showConfirmDialog(this, "Deseja executar o ajuste de LDC Pendente?", "QA Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sqlUpdate = "UPDATE BLBASA.LDC l SET l.LDCSTA = 2 WHERE l.LDCNSUORI IN (SELECT lresp.LDCNSUORI FROM BLBASA.LDC lresp WHERE lresp.LDCNSUORI IN (SELECT l.LDCNSUORI FROM BLBASA.LDC l WHERE l.LDCSTD = 1 AND l.LDCSTA = 1) AND lresp.LDCSTD = 2 AND lresp.LDCSTA = 5) AND l.LDCSTD = 1 AND l.LDCSTA = 1";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS); Statement st = conn.createStatement()) {
                int rows = st.executeUpdate(sqlUpdate);
                JOptionPane.showMessageDialog(this, rows + " registros ajustados!");
                atualizarTudo();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro no Update: " + e.getMessage());
            }
        }
    }

    private void configurarAlertaVermelho(JTable tabela, int colunaAlvo) {
        tabela.getColumnModel().getColumn(colunaAlvo).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Number && ((Number) value).intValue() > 0) {
                    c.setBackground(new Color(255, 210, 210)); // Vermelho bem suave
                    c.setForeground(Color.RED);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });
    }

    public static void main(String[] args) {
        // Bloco de Teste de Driver solicitado
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("Driver encontrado com sucesso!");
        } catch (ClassNotFoundException e) {
            System.out.println("Erro: Driver JDBC não encontrado no Classpath. Verifique o ojdbc8.jar.");
            JOptionPane.showMessageDialog(null, "Driver JDBC não encontrado! Adicione o ojdbc8.jar ao projeto.");
        }

        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new MonitorOperacional().setVisible(true);
        });
    }
}