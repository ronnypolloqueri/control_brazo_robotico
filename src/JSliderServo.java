import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JSliderServo extends JSlider {

    int idServo;
    String name;
    ControlServoForm main;

    JSliderServo(int idServo){
        this(idServo, "No name", 0,180, 90);
    }

    JSliderServo(int idServo, String name, int min, int max, int value){
        super(JSlider.HORIZONTAL, min, max, value);
        this.idServo = idServo;
        this.name = name;

        this.setMajorTickSpacing(10);
        this.setMinorTickSpacing(1);
        this.setPaintTicks(true);
        this.setPaintLabels(true);
        this.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int angulo = getValue();
                System.out.println("value: " + angulo);
                main.sendData(idServo+ "." + angulo + ".0");
            }
        });
    }

    public void setMain(ControlServoForm main){
        this.main = main;
    }

    public String getId() {
        return "" + idServo;
    }

    public String getName() {
        return "" + name;
    }

}
