package interest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * 存款计算面板
 * 支持单利和复利两种计息方式，复利可选择按年/按六个月/按三个月/按月计息，
 * 计算到期本息合计、利息总额，并列出逐期账单明细。
 */
public class DepositPanel extends JPanel {

    private JTextField principalField;    // 本金输入框
    private JTextField rateField;         // 年利率输入框
    private JTextField yearsField;        // 存款期限输入框
    private JRadioButton simpleInterest;  // 单利单选按钮
    private JRadioButton compoundInterest;// 复利单选按钮
    private JComboBox<String> frequencyBox; // 复利频率下拉框（按年/按六个月/按三个月/按月）
    private JLabel resultLabel;           // 结果标签
    private DefaultTableModel tableModel; // 账单明细表数据模型

    public DepositPanel() {
        initUI();
    }

    /** 构建界面 */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---------- 顶部：输入区域 ----------
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder("存款信息"));

        inputPanel.add(new JLabel("本金（元）："));
        principalField = new JTextField();
        inputPanel.add(principalField);

        inputPanel.add(new JLabel("年利率（%）："));
        rateField = new JTextField();
        inputPanel.add(rateField);

        inputPanel.add(new JLabel("存款期限（年）："));
        yearsField = new JTextField();
        inputPanel.add(yearsField);

        inputPanel.add(new JLabel("计息方式："));
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        simpleInterest = new JRadioButton("单利", true); // 默认单利
        compoundInterest = new JRadioButton("复利");
        ButtonGroup group = new ButtonGroup();
        group.add(simpleInterest);
        group.add(compoundInterest);
        typePanel.add(simpleInterest);
        typePanel.add(compoundInterest);
        inputPanel.add(typePanel);

        inputPanel.add(new JLabel("复利频率："));
        frequencyBox = new JComboBox<>(new String[]{"按年", "按六个月", "按三个月", "按月"});
        // 选单利时禁用频率选择，选复利时启用
        compoundInterest.addActionListener(e -> frequencyBox.setEnabled(true));
        simpleInterest.addActionListener(e -> frequencyBox.setEnabled(false));
        frequencyBox.setEnabled(false); // 初始为单利，禁用
        inputPanel.add(frequencyBox);

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

        // ---------- 中部：结果展示 + 账单明细表 ----------
        resultLabel = new JLabel("请填写存款信息后点击“计算”", SwingConstants.CENTER);
        resultLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("计算结果"));
        resultPanel.add(resultLabel, BorderLayout.CENTER);

        // 账单明细表：期数、期初余额、本期利息、期末余额、累计利息
        tableModel = new DefaultTableModel(
                new Object[]{"期数", "期初余额（元）", "本期利息（元）", "期末余额（元）", "累计利息（元）"}, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("账单明细"));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.add(resultPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    /** 点击"计算"按钮后的处理 */
    private void calculate() {
        try {
            double principal = Double.parseDouble(principalField.getText().trim());
            double annualRate = Double.parseDouble(rateField.getText().trim());
            double years = Double.parseDouble(yearsField.getText().trim());

            if (principal <= 0 || annualRate < 0 || years <= 0) {
                JOptionPane.showMessageDialog(this, "本金和期限必须大于 0，利率不能为负数。",
                        "输入有误", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double rate = annualRate / 100.0; // 年利率（小数）
            tableModel.setRowCount(0); // 清空上一次的账单

            if (simpleInterest.isSelected()) {
                // 单利：本金不变，利息按年滚动，期末一次性结算
                resultLabel.setText(calculateSimpleBill(principal, rate, years));
            } else {
                // 复利：利息按所选频率滚入本金
                resultLabel.setText(calculateCompoundBill(principal, rate, years, getPeriodsPerYear()));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字。", "输入有误", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** 根据所选频率返回每年计息次数：按年 1 次、按六个月 2 次、按三个月 4 次、按月 12 次 */
    private int getPeriodsPerYear() {
        String freq = (String) frequencyBox.getSelectedItem();
        switch (freq) {
            case "按年":
                return 1;
            case "按六个月":
                return 2;
            case "按三个月":
                return 4;
            default:
                return 12; // 按月
        }
    }

    /**
     * 单利账单：每年一行，本金不滚存；
     * 若期限含小数年，最后一行按实际时长计息。
     */
    private String calculateSimpleBill(double principal, double rate, double years) {
        DecimalFormat df = new DecimalFormat("0.00");
        int fullYears = (int) Math.floor(years);
        double fraction = years - fullYears; // 不足一年的部分

        double interestPerYear = principal * rate; // 每年利息（本金不变，每年相同）
        double totalInterest = 0;

        for (int i = 1; i <= fullYears; i++) {
            totalInterest += interestPerYear;
            addBillRow(df, i, principal, interestPerYear, principal + totalInterest, totalInterest);
        }
        if (fraction > 0.001) {
            // 尾期：按实际时长计息
            double lastInterest = principal * rate * fraction;
            totalInterest += lastInterest;
            addBillRow(df, fullYears + 1, principal, lastInterest, principal + totalInterest, totalInterest);
        }

        return String.format("到期本息合计：%s 元    利息总额：%s 元",
                df.format(principal + totalInterest), df.format(totalInterest));
    }

    /**
     * 复利账单：按所选频率逐期计息，每期利息滚入本金；
     * 若总期数含小数（如 1.5 年按年复利），最后一行按实际时长计息。
     */
    private String calculateCompoundBill(double principal, double rate, double years, int periodsPerYear) {
        DecimalFormat df = new DecimalFormat("0.00");
        double periodRate = rate / periodsPerYear;     // 每期利率
        double totalPeriods = years * periodsPerYear;  // 总期数（可能含小数）
        int fullPeriods = (int) Math.floor(totalPeriods);
        double fraction = totalPeriods - fullPeriods;  // 不足一期的部分

        double balance = principal;
        double totalInterest = 0;

        for (int i = 1; i <= fullPeriods; i++) {
            double interest = balance * periodRate;
            totalInterest += interest;
            balance += interest;
            addBillRow(df, i, balance - interest, interest, balance, totalInterest);
        }
        if (fraction > 0.001) {
            // 尾期：按实际时长计息（不足一期按比例折算）
            double lastInterest = balance * rate * fraction / periodsPerYear;
            totalInterest += lastInterest;
            balance += lastInterest;
            addBillRow(df, fullPeriods + 1, balance - lastInterest, lastInterest, balance, totalInterest);
        }

        return String.format("到期本息合计：%s 元    利息总额：%s 元",
                df.format(balance), df.format(totalInterest));
    }

    /** 向账单明细表添加一行 */
    private void addBillRow(DecimalFormat df, int period, double beginBalance,
                            double interest, double endBalance, double totalInterest) {
        tableModel.addRow(new Object[]{
                period,
                df.format(beginBalance),
                df.format(interest),
                df.format(endBalance),
                df.format(totalInterest)
        });
    }

    /** 清空输入和结果 */
    private void clear() {
        principalField.setText("");
        rateField.setText("");
        yearsField.setText("");
        simpleInterest.setSelected(true);
        frequencyBox.setEnabled(false);
        tableModel.setRowCount(0);
        resultLabel.setText("请填写存款信息后点击“计算”");
    }
}
