package interest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * 贷款计算面板
 * 支持等额本息、等额本金、先息后本三种还款方式，
 * 计算月供、总利息、总还款额，并展示完整的还款计划表。
 */
public class LoanPanel extends JPanel {

    private JTextField principalField;      // 贷款金额输入框
    private JTextField rateField;           // 年利率输入框
    private JTextField yearsField;          // 贷款期限输入框
    private JRadioButton equalInstallment;  // 等额本息单选按钮
    private JRadioButton equalPrincipal;    // 等额本金单选按钮
    private JRadioButton interestOnly;      // 先息后本单选按钮
    private JLabel resultLabel;             // 结果汇总标签
    private DefaultTableModel tableModel;   // 还款计划表数据模型

    public LoanPanel() {
        initUI();
    }

    /** 构建界面 */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---------- 顶部：输入区域 ----------
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder("贷款信息"));

        inputPanel.add(new JLabel("贷款金额（元）："));
        principalField = new JTextField();
        inputPanel.add(principalField);

        inputPanel.add(new JLabel("年利率（%）："));
        rateField = new JTextField();
        inputPanel.add(rateField);

        inputPanel.add(new JLabel("贷款期限（年）："));
        yearsField = new JTextField();
        inputPanel.add(yearsField);

        inputPanel.add(new JLabel("还款方式："));
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        equalInstallment = new JRadioButton("等额本息", true); // 默认选中等额本息
        equalPrincipal = new JRadioButton("等额本金");
        interestOnly = new JRadioButton("先息后本");
        ButtonGroup group = new ButtonGroup();
        group.add(equalInstallment);
        group.add(equalPrincipal);
        group.add(interestOnly);
        typePanel.add(equalInstallment);
        typePanel.add(equalPrincipal);
        typePanel.add(interestOnly);
        inputPanel.add(typePanel);

        // 按钮行
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton calcButton = new JButton("计算");
        JButton clearButton = new JButton("清空");
        calcButton.addActionListener(e -> calculate());
        clearButton.addActionListener(e -> clear());
        buttonPanel.add(calcButton);
        buttonPanel.add(clearButton);

        JPanel northPanel = new JPanel(new BorderLayout(0, 8));
        northPanel.add(inputPanel, BorderLayout.CENTER);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);

        // ---------- 中部：还款计划表 ----------
        tableModel = new DefaultTableModel(
                new Object[]{"期数", "月供（元）", "偿还本金（元）", "偿还利息（元）", "剩余本金（元）"}, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("还款计划"));

        // ---------- 底部：结果汇总 ----------
        resultLabel = new JLabel("请填写贷款信息后点击“计算”", SwingConstants.CENTER);
        resultLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createTitledBorder("计算结果"));
        southPanel.add(resultLabel, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    /** 点击"计算"按钮后的处理 */
    private void calculate() {
        try {
            double principal = Double.parseDouble(principalField.getText().trim());
            double annualRate = Double.parseDouble(rateField.getText().trim());
            double years = Double.parseDouble(yearsField.getText().trim());

            if (principal <= 0 || annualRate < 0 || years <= 0) {
                JOptionPane.showMessageDialog(this, "金额和期限必须大于 0，利率不能为负数。",
                        "输入有误", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int months = (int) Math.round(years * 12);      // 总还款月数
            double monthlyRate = annualRate / 100.0 / 12.0; // 月利率

            tableModel.setRowCount(0); // 清空上一次的还款计划

            if (equalInstallment.isSelected()) {
                calculateEqualInstallment(principal, monthlyRate, months);
            } else if (equalPrincipal.isSelected()) {
                calculateEqualPrincipal(principal, monthlyRate, months);
            } else {
                calculateInterestOnly(principal, monthlyRate, months);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字。", "输入有误", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** 等额本息：每月月供固定，前期利息多本金少，后期本金多利息少 */
    private void calculateEqualInstallment(double principal, double monthlyRate, int months) {
        // 月供公式：M = P × r × (1+r)^n ÷ ((1+r)^n - 1)
        double pow = Math.pow(1 + monthlyRate, months);
        double monthlyPayment = principal * monthlyRate * pow / (pow - 1);

        DecimalFormat df = new DecimalFormat("0.00");
        double balance = principal; // 剩余本金
        double totalInterest = 0;

        for (int i = 1; i <= months; i++) {
            double interest = balance * monthlyRate;     // 本期利息
            double principalPart = monthlyPayment - interest; // 本期本金
            balance -= principalPart;
            if (i == months) {
                // 最后一期把剩余本金清零，修正四舍五入产生的尾差
                principalPart += balance;
                balance = 0;
            }
            totalInterest += interest;
            tableModel.addRow(new Object[]{
                    i,
                    df.format(monthlyPayment),
                    df.format(principalPart),
                    df.format(interest),
                    df.format(balance)
            });
        }

        resultLabel.setText(String.format("月供：%s 元    总利息：%s 元    总还款额：%s 元",
                df.format(monthlyPayment), df.format(totalInterest), df.format(principal + totalInterest)));
    }

    /** 等额本金：每月偿还的本金固定，月供逐月递减 */
    private void calculateEqualPrincipal(double principal, double monthlyRate, int months) {
        DecimalFormat df = new DecimalFormat("0.00");
        double principalPerMonth = principal / months; // 每月固定本金
        double balance = principal;
        double totalInterest = 0;
        double firstPayment = 0;
        double lastPayment = 0;

        for (int i = 1; i <= months; i++) {
            double interest = balance * monthlyRate;         // 本期利息（随剩余本金递减）
            double payment = principalPerMonth + interest;   // 本期月供
            if (i == 1) {
                firstPayment = payment;
            }
            if (i == months) {
                lastPayment = payment;
            }
            balance -= principalPerMonth;
            totalInterest += interest;
            tableModel.addRow(new Object[]{
                    i,
                    df.format(payment),
                    df.format(principalPerMonth),
                    df.format(interest),
                    df.format(balance)
            });
        }

        resultLabel.setText(String.format("首月月供：%s 元    末月月供：%s 元    总利息：%s 元    总还款额：%s 元",
                df.format(firstPayment), df.format(lastPayment),
                df.format(totalInterest), df.format(principal + totalInterest)));
    }

    /** 先息后本：每月只还利息，本金不变，到期最后一期一次性还清本金 */
    private void calculateInterestOnly(double principal, double monthlyRate, int months) {
        DecimalFormat df = new DecimalFormat("0.00");
        double monthlyInterest = principal * monthlyRate; // 每月固定利息
        double totalInterest = monthlyInterest * months;  // 总利息

        for (int i = 1; i <= months; i++) {
            if (i == months) {
                // 最后一期：利息 + 全部本金
                tableModel.addRow(new Object[]{
                        i,
                        df.format(monthlyInterest + principal),
                        df.format(principal),
                        df.format(monthlyInterest),
                        "0.00"
                });
            } else {
                // 前 n-1 期：只还利息，剩余本金不变
                tableModel.addRow(new Object[]{
                        i,
                        df.format(monthlyInterest),
                        "0.00",
                        df.format(monthlyInterest),
                        df.format(principal)
                });
            }
        }

        resultLabel.setText(String.format("每月利息：%s 元    到期还本：%s 元    总利息：%s 元    总还款额：%s 元",
                df.format(monthlyInterest), df.format(principal),
                df.format(totalInterest), df.format(principal + totalInterest)));
    }

    /** 清空输入和结果 */
    private void clear() {
        principalField.setText("");
        rateField.setText("");
        yearsField.setText("");
        tableModel.setRowCount(0);
        resultLabel.setText("请填写贷款信息后点击“计算”");
    }
}
