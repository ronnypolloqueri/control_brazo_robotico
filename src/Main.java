import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        for(UIManager.LookAndFeelInfo laf:UIManager.getInstalledLookAndFeels()){
            if("Nimbus".equals(laf.getName()))
                try {
                    UIManager.setLookAndFeel(laf.getClassName());
                } catch (Exception ex) {
                }
        }
        new ControlServoForm("/dev/ttyACM0");
    }
}
