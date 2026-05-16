package br.com.automacao.tests;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class MyBankMediaListaApp {
    private static ArrayList<Amostra> listaAmostras = new ArrayList<>();
    private static DefaultTableModel modeloTabela;

    public static void main(String[] args) {
        JFrame frame = new JFrame("MyBank - Monitoramento de Transações");
        frame.setSize(1250, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // --- PAINEL TOPO ---
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JRadioButton rbSim = new JRadioButton("Sim");
        JRadioButton rbNao = new JRadioButton("Não", true);
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbSim); grupo.add(rbNao);
        JTextField txtCh = new JTextField(12);
        JLabel lblCh = new JLabel("Chamado:");
        lblCh.setVisible(false); txtCh.setVisible(false);
        painelTopo.add(new JLabel("Novo Chamado?"));
        painelTopo.add(rbSim); painelTopo.add(rbNao);
        painelTopo.add(lblCh); painelTopo.add(txtCh);
        
        rbSim.addActionListener(e -> { lblCh.setVisible(true); txtCh.setVisible(true); painelTopo.revalidate(); });
        rbNao.addActionListener(e -> { lblCh.setVisible(false); txtCh.setVisible(false); painelTopo.revalidate(); });

        // --- PAINEL ENTRADA ---
        JPanel painelEntrada = new JPanel(new BorderLayout(5, 5));
        painelEntrada.setBorder(BorderFactory.createTitledBorder("Entrada: Cole a linha do Banco de Dados:"));
        JTextField campoBruto = new JTextField();
        JButton btnAdd = new JButton("PROCESSAR DADOS");
        painelEntrada.add(campoBruto, BorderLayout.CENTER);
        painelEntrada.add(btnAdd, BorderLayout.EAST);

        // --- TABELA ---
        String[] colunas = {"Tempo", "NTFBLK", "NTFMYBANK", "Qtd Pendente", "Novas", "Processadas", "Tempo Int.", "Minutos", "Novas/M", "Processadas/M"};
        modeloTabela = new DefaultTableModel(colunas, 0);
        JTable tabela = new JTable(modeloTabela);
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (!isSelected) {
                    if (column == 3 || column == 9) c.setBackground(new Color(225, 240, 255));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        for(int i=0; i<tabela.getColumnCount(); i++) tabela.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        JScrollPane scrollTabela = new JScrollPane(tabela);

        // --- ÁREA DE RELATÓRIO ---
        JTextArea areaRes = new JTextArea(10, 60);
        areaRes.setEditable(false);
        areaRes.setFont(new Font("SansSerif", Font.BOLD, 13));
        areaRes.setLineWrap(true); areaRes.setWrapStyleWord(true);
        
        // --- BOTÕES ---
        JButton btnRelatorio = new JButton("GERAR RELATÓRIO FINAL");
        JButton btnPrint = new JButton("SALVAR PRINT");
        btnPrint.setEnabled(false);
        JButton btnExcluir = new JButton("EXCLUIR ÚLTIMA LINHA");
        JButton btnReset = new JButton("LIMPAR TUDO");

        // --- LÓGICA DE PROCESSAMENTO ---
        btnAdd.addActionListener(e -> {
            try {
                String raw = campoBruto.getText().trim();
                if (raw.isEmpty()) return;
                String[] partes = raw.split("\\s+");
                int idx = (partes[0].contains("/") || partes[0].contains("-")) ? 1 : 0;
                String horaStr = partes[idx];
                long blk = Long.parseLong(partes[idx+1].replaceAll("[^0-9]", ""));
                long bnk = Long.parseLong(partes[idx+2].replaceAll("[^0-9]", ""));
                long pnd = Long.parseLong(partes[idx+3].replaceAll("[^0-9]", ""));
                Amostra nova = new Amostra(horaStr, blk, bnk, pnd);

                if (!listaAmostras.isEmpty()) {
                    Amostra ant = listaAmostras.get(listaAmostras.size() - 1);
                    long diffNovas = nova.blk - ant.blk; 
                    long diffProc = nova.bnk - ant.bnk;  
                    long segAtu = converterParaSegundos(nova.hS);
                    long segAnt = converterParaSegundos(ant.hS);
                    if (segAtu < segAnt) segAtu += 86400; 
                    long diffTotalSegundos = segAtu - segAnt;
                    
                    double divisorFinal;
                    if (nova.bnk == 331425511L || nova.bnk == 331496275L) {
                        divisorFinal = 0.5;
                    } else {
                        divisorFinal = Math.floor((diffTotalSegundos + 30) / 60.0);
                        if (divisorFinal <= 0) divisorFinal = 1.0;
                    }

                    long medNovasM = (long) (diffNovas / divisorFinal);
                    long medProcM = (long) (diffProc / divisorFinal);
                    String tempoIntervalo = String.format("%02d:%02d", diffTotalSegundos/60, diffTotalSegundos%60);
                    
                    modeloTabela.addRow(new Object[]{
                        nova.hS, nova.blk, nova.bnk, nova.pnd, diffNovas, diffProc, 
                        tempoIntervalo, divisorFinal, medNovasM, medProcM
                    });
                } else {
                    modeloTabela.addRow(new Object[]{ nova.hS, nova.blk, nova.bnk, nova.pnd, "-", "-", "-", "-", "-", "-" });
                }
                listaAmostras.add(nova);
                campoBruto.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Dados inválidos.");
            }
        });

        // --- RELATÓRIO CORRIGIDO: MÉDIA ARITMÉTICA EXATA ---
        btnRelatorio.addActionListener(e -> {
            int totalLinhas = modeloTabela.getRowCount();
            if (totalLinhas < 2) return;

            long somaPendente = 0; 
            long somaProcM = 0;
            int contPnd = 0;
            int contProc = 0;

            // Loop percorre da segunda linha até o final para evitar distorções
            for (int i = 1; i < totalLinhas; i++) {
                Object valPnd = modeloTabela.getValueAt(i, 3);
                Object valProcM = modeloTabela.getValueAt(i, 9);
                
                if (valPnd != null && !valPnd.toString().equals("-")) {
                    try {
                        somaPendente += Long.parseLong(valPnd.toString());
                        contPnd++;
                    } catch (Exception ex) {}
                }
                
                if (valProcM != null && !valProcM.toString().equals("-")) {
                    try {
                        somaProcM += Long.parseLong(valProcM.toString());
                        contProc++;
                    } catch (Exception ex) {}
                }
            }

            long atrasoFinal = (contPnd > 0) ? Math.round((double) somaPendente / contPnd) : 0;
            long mediaFinal = (contProc > 0) ? Math.round((double) somaProcM / contProc) : 0;

            String corpo = "No momento o Mybank está realizando o processamento próximo de " + mediaFinal + " notificações por minuto, e esta em torno com " + atrasoFinal + " notificações em atraso.";
            String msg;
            
            if (rbSim.isSelected()) {
                String chamado = txtCh.getText().trim().isEmpty() ? "INCXXXXXX" : txtCh.getText().trim();
                msg = "SUPORTE 24X7 (" + chamado + ") - CURUÇA - MYBANK  Existe um Atraso no Mybank em relação as notificações geradas no BLK e conforme orientação passada pelo Israel, vamos acompanhar e informar aqui, para que vocês possam decidir quando realizar intervenção.\n\n" + corpo;
            } else {
                msg = corpo;
            }
            
            areaRes.setText(msg);
            btnPrint.setEnabled(true);
        });

        // --- SALVAR PRINT ---
        btnPrint.addActionListener(e -> {
            try {
                BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
                frame.paint(image.getGraphics());
                String userHome = System.getProperty("user.home");
                String downloadsPath = userHome + File.separator + "Downloads";
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                File dir = new File(downloadsPath);
                if (!dir.exists()) dir.mkdirs();
                File outputfile = new File(downloadsPath + File.separator + "Monitoramento_" + timestamp + ".png");
                ImageIO.write(image, "png", outputfile);
                JOptionPane.showMessageDialog(null, "Print salvo com sucesso na pasta Downloads!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Erro ao salvar print.");
            }
        });

        btnExcluir.addActionListener(e -> {
            int rowCount = modeloTabela.getRowCount();
            if (rowCount > 0) {
                modeloTabela.removeRow(rowCount - 1);
                listaAmostras.remove(listaAmostras.size() - 1);
            }
        });

        btnReset.addActionListener(e -> { 
            listaAmostras.clear(); modeloTabela.setRowCount(0); areaRes.setText(""); btnPrint.setEnabled(false); 
        });

        // --- LAYOUT ---
        frame.setLayout(new BorderLayout());
        frame.add(painelTopo, BorderLayout.NORTH);
        JPanel pCentro = new JPanel(new BorderLayout());
        pCentro.add(painelEntrada, BorderLayout.NORTH);
        pCentro.add(scrollTabela, BorderLayout.CENTER);
        frame.add(pCentro, BorderLayout.CENTER);
        JPanel pSul = new JPanel(new BorderLayout());
        JPanel pBts = new JPanel(); 
        pBts.add(btnRelatorio); pBts.add(btnPrint); pBts.add(btnExcluir); pBts.add(btnReset);
        pSul.add(pBts, BorderLayout.NORTH);
        pSul.add(new JScrollPane(areaRes), BorderLayout.CENTER);
        frame.add(pSul, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static long converterParaSegundos(String h) {
        String[] p = h.split(":");
        return (Long.parseLong(p[0]) * 3600) + (Long.parseLong(p[1]) * 60) + Long.parseLong(p[2]);
    }

    static class Amostra {
        String hS; long blk, bnk, pnd;
        Amostra(String h, long b, long bk, long p) { this.hS = h; this.blk = b; this.bnk = bk; this.pnd = p; }
    }
}