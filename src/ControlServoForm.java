import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

public class ControlServoForm extends JFrame{
    private JButton btDefault, btAddCommand, btRemoveCommand, btPlayCommands, btAgregarComandos;
    private JTextArea taLog, taCommands;
    PanamaHitek_Arduino ino = new PanamaHitek_Arduino();
    List<String> comandos = new ArrayList<String>();
    private String currentCommand;
    private boolean comandoCompletado = false;

    private void agregarComandoActual(){
        System.out.println("agregarComando");
        comandos.add(currentCommand);
        refrescarListaDeComandos();
    }

    private void agregarComandos(){
        String strComandos = taCommands.getText();
        for(String comando : strComandos.split("\n")){
            if(comando.trim().contains("."))
                comandos.add(comando);
        }
        refrescarListaDeComandos();
        taLog.append("¡Comandos agregados exitosamente!" + "\n");
    }
    private void removercomando(){
        comandos.remove(comandos.size()-1);
        refrescarListaDeComandos();
    }

    private void ejecutarComandos() {

        Thread hilo = new Thread() {

            public void run(){
                taLog.append("Ejecutando comandos...\n");
                for(int i = 0; i<comandos.size();i++)
                {
                    comandoCompletado = false;
                    System.out.println("Comandos " + (i+1) + "/" + comandos.size());
                    taLog.append("Comando " + (i + 1) + "..." + "\n");

                    sendData(comandos.get(i));
                    while (!comandoCompletado) {
                        taLog.append("Esperando respuesta exitosa de Arduino...\n");
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                        }
                    }
                    System.out.println("Comandos " + (i+1) + "/" + comandos.size()+ " Terminado");
                }
                taLog.append("Ejecutar comandos...Terminado\n");
            }
        };
        hilo.start();

    }

    private void refrescarListaDeComandos(){
        System.out.println("RefrescarLista");
        taCommands.setText("");
        taCommands.append("\n");
        for(String comando: comandos){
            taCommands.append(comando + "\n");
        }
    }
    private SerialPortEventListener listener = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {
                if (ino.isMessageAvailable()) {
                    String response = ino.printMessage();
                    if(response.matches(".*Completado.*")){
                        System.out.println("Comando Completado...................");
                        comandoCompletado = true;
                    }
                    taLog.append("(Arduino):<" + response + ">\n" ); // respuesta de arduino
                    System.out.println("(Arduino):<" + response + ">\n" );
                }
            } catch (SerialPortException | ArduinoException ex) {
                Logger.getLogger(ControlServoForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    public ControlServoForm(String port){
        this.setTitle("Control de brazo robótico para 6 servos");
        this.setSize(800,600);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        btDefault = new JButton("A Posición inicial");
        btAddCommand = new JButton("Agregar Commando");
        btRemoveCommand = new JButton("Remover Commando");
        btPlayCommands = new JButton("Ejecutar comandos");
        btAgregarComandos = new JButton("Agregar comandos");

        taLog = new JTextArea(10,100);
        JScrollPane panelLog = new JScrollPane(taLog);
        panelLog.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        panelLog.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });

        taCommands = new JTextArea(10,40);

        JScrollPane panelComandos = new JScrollPane(taCommands);
        panelComandos.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        panelComandos.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });

        JSliderServo[] servoSliders = {
                new JSliderServo(1, "Base giratoria", 0     ,180,20),
                new JSliderServo(2, "Articulación brazo" ,30,180,90),
                new JSliderServo(3, "Articulación codo"  ,30,180,60),
                new JSliderServo(4, "Articulación muñeca",30,180,60),
                new JSliderServo(5, "Giro Muñeca"        ,30,180,90),
                new JSliderServo(6, "Pinza"              ,70,180,120),
        };


        this.setLayout(new BorderLayout());
        JPanel panelCenter = new JPanel();
        panelCenter.setLayout(new GridLayout(0,2));
        panelCenter.setBorder(BorderFactory.createEmptyBorder(10,30,10,30));

        JLabel lb_titulo = new JLabel("BRAZO ROBÓTICO - 5TO ESIS (UNJBG) 2017");
        lb_titulo.setFont(new Font("Serif", Font.PLAIN, 40));
        lb_titulo.setHorizontalAlignment(JLabel.CENTER);
        this.add(lb_titulo, BorderLayout.NORTH);

        JPanel panelSouth = new JPanel();
        panelSouth.setBorder(BorderFactory.createEmptyBorder(10,30,10,30));
        panelSouth.setLayout(new BorderLayout(0,2));
        panelSouth.add(new JLabel("Log:"), BorderLayout.WEST);

        JPanel panelTexAreas = new JPanel();;
        panelTexAreas.setLayout(new BoxLayout(panelTexAreas, BoxLayout.X_AXIS));
        panelTexAreas.setBounds(0, 0, 300, 140);
        panelTexAreas.add(panelLog);
        panelTexAreas.add(panelComandos);

        panelSouth.add(panelTexAreas, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();

        panelBotones.setBounds(61, 11, 100, 140);
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));

        panelBotones.add(btAddCommand);
        panelBotones.add(btPlayCommands);
        panelBotones.add(btDefault);
        panelBotones.add(btRemoveCommand);
        panelBotones.add(btAgregarComandos);
        panelSouth.add(panelBotones, BorderLayout.EAST);

        for(int i = servoSliders.length - 1; i > -1; i--  ){
            servoSliders[i].setMain(this);
            JLabel label = new JLabel("Servo "  + servoSliders[i].getId() + "(" +
                                                       servoSliders[i].getName()+ ")");
            label.setFont(new Font("Serif", Font.PLAIN, 14));
            label.setHorizontalAlignment(JLabel.CENTER);
            panelCenter.add(label);
            panelCenter.add(servoSliders[i]);
        }
        this.add(panelCenter,BorderLayout.CENTER);
        this.add(panelSouth, BorderLayout.SOUTH);

        try {
            //Se inicia la comunicación con el Puerto Serie
            ino.arduinoRXTX(port, 9600, listener) ;
        } catch (ArduinoException ex) {
            Logger.getLogger(ControlServoForm.class.getName()).log(Level.SEVERE, null, ex);
            taLog.append(ex.getMessage() + "\n" );
        }


        btDefault.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Se hace el envío del String "0"
                try {
                    taLog.append("Seteando servos a posición inicial."  + "\n" );
                    ino.sendData(7 + ".0");
                    taLog.append("Terminado"  + "\n" );
                } catch (ArduinoException | SerialPortException ex) {
                    Logger.getLogger(ControlServoForm.class.getName()).log(Level.SEVERE, null, ex);
                    taLog.append(ex.getMessage()  + "\n" );
                }
            }
        });

        btAddCommand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarComandoActual();
            }
        });

        btRemoveCommand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removercomando();
            }
        });

        btAgregarComandos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarComandos();
            }
        });

        btPlayCommands.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarComandos();
            }
        });

        this.setVisible(true);
        this.pack();
    }

    public void sendData(String data){
        try {
            ino.sendData(data);
            currentCommand = data;
        } catch (ArduinoException | SerialPortException ex) {
            Logger.getLogger(ControlServoForm.class.getName()).log(Level.SEVERE, null, ex);
            taLog.append(ex.getMessage() + "\n" );
        }
    }
}

class PanelImagen extends javax.swing.JPanel {

    public PanelImagen() {
        this.setSize(300, 400); //se selecciona el tamaño del panel
    }

//Se crea un método cuyo parámetro debe ser un objeto Graphics

    public void paint(Graphics grafico) {
        Dimension height = getSize();

//Se selecciona la imagen que tenemos en el paquete de la //ruta del programa

        ImageIcon Img = new ImageIcon(getClass().getResource("/imgs/brazo_robotico.jpg"));

    //se dibuja la imagen que tenemos en el paquete Images //dentro de un panel

        grafico.drawImage(Img.getImage(), 0, 0, height.width, height.height, null);

        setOpaque(false);
        super.paintComponent(grafico);
    }
}