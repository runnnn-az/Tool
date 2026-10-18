package interest;

import javax.swing.*;

/**
 * 图形化利率计算器主程序
 * 包含两个功能页签：贷款计算、存款计算
 */
public class InterestCalculatorApp {

    public static void main(String[] args) {
        // Swing 组件必须在事件调度线程（EDT）中创建和更新，避免线程安全问题
        SwingUtilities.invokeLater(() -> {
            try {
                // 使用系统外观，让界面更接近 Windows 原生风格
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 外观设置失败不影响程序运行，忽略即可
            }

            JFrame frame = new JFrame("利率计算器");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 点关闭按钮退出程序

            // 页签容器：贷款计算 | 存款计算
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("贷款计算", new LoanPanel());
            tabbedPane.addTab("存款计算", new DepositPanel());

            frame.setContentPane(tabbedPane);
            frame.setSize(780, 640);
            frame.setLocationRelativeTo(null); // 窗口居中显示
            frame.setVisible(true);
        });
    }
}
