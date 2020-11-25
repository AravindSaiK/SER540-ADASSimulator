import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.List;
import java.util.*;

public class ADASSimulator extends JFrame{
    private String trcFileName;
    private String htmFileName = "/Users/aravind/Documents/SER540/19 GPS Track.htm";

    private JPanel mainPanel;
    private JPanel simulatorPanel;
    private JButton simulate;
    private JTextField timeField;
    private JTextField angleField;
    private JTextField speedField;
    private JTextField yawField;
    private JTextField lonAccField;
    private JTextField latAccField;
    private JTextField latField;
    private JTextField lonField;
    private JLabel timeLabel;
    private JLabel angleLabel;
    private JLabel speedLabel;
    private JLabel yawLabel;
    private JLabel lonAccLabel;
    private JLabel latAccLabel;
    private JLabel latLabel;
    private JLabel lonLabel;
    private JLabel simulateLabel;
    private JLabel curveLabel;
    private JTextArea curveArea;
    private JLabel warningLabel;
    private JTextField warningField;
    private Timer t;
    private int i = 0;
    private boolean curveFlag = false;
    private CurveData curve;
    private int curveCount = 0;
    private int sensorCount = 0;
    private boolean firstRun = true;
    private int warningCount = 0;
    String off = "-", ang = "-", vel = "-", yaw = "-", loac = "-", laac = "-", gla = "-", glo = "-";

    private HashMap<Double, SensorData> sensorData = new HashMap<Double, SensorData>();
    private ArrayList<CurveData> curveData = new ArrayList<CurveData>();
    private boolean warningFlag = false;

    //Constructor to set properties for the GUI.
    public ADASSimulator(){
        super("ADAS Simulator");
    }

    //Sets the GUI visible.
    public void activate(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(900, 800));
        this.setContentPane(this.mainPanel);
        this.pack();
        this.setLocationRelativeTo(null);

        activateListeners();
        this.setVisible(true);
    }

    //Setting the Listener for the button.
    private void activateListeners() {
        simulate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                simulate.setEnabled(false);
                startSimulation();
            }
        });
    }

    //Simulation Starts.
    private void startSimulation() {
        if(t != null){
            t.stop();
            firstRun = false;
            resetValues();
        }else{
            //Read and Store GPS data.
            readGPSData();
            // Read the TRC data.
            startReadingSensors();
            final List<Double> keySet = new ArrayList<Double>(sensorData.keySet());
            System.out.println("Time Offset(ms)"+ "\t" +"Steering Angle(Deg)" + "\t" + "Velocity(km/h)"+ "\t" + "Yaw Rate(m/Deg)" + "\t" + "Longitudinal Acceleration(m/s^2)"+ "\t" + "Lateral Acceleration(m/s^2)" + "\t" + "Latitude(Deg)" + "\t" + "Longitude(Deg)" + "\n");
            System.out.print(off+"\t\t\t\t"+ang+"\t\t\t\t\t"+vel+"\t\t\t\t"+yaw+"\t\t\t\t"+loac+"\t\t\t\t\t\t\t\t\t"+laac+"\t\t\t\t\t\t\t"+gla+"\t\t\t\t"+glo);
            Collections.sort(keySet);
            //Timer to simulate real world time amd update GUI.
            t = new Timer(3, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (i < keySet.size()) {
                        if(sensorData.get(keySet.get(i)).getSensorType() == SensorData.Sensor.STEERING_ANGLE){
                            detectCurve(sensorData.get(keySet.get(i)));
                        }
                        checkForCurve(sensorData.get(keySet.get(i)));
                        updateData(sensorData.get(keySet.get(i)));
                        i++;
                    }
                    if (i >= keySet.size()) {
                        if(curveFlag){
                            SensorData sensor = sensorData.get(keySet.get(i - 1));
                            sensor.setSensorData("0");
                            detectCurve(sensor);
                        }
                        i = 0;
                        simulate.setEnabled(true);
                        t.stop();
                    }
                }
            });
        }
        t.start();
    }

    //Method to warn on Curves.
    private void checkForCurve(SensorData sensorData) {
        //Does nothing on first run.
        if(!firstRun){
            Double curOffset = sensorData.getTimeOffset();
            if(warningCount < curveData.size()) {
                Double nextCurveOffset = curveData.get(warningCount).getTimeOffset();
                //This is the case when the warning has been given.
                if(warningFlag){
                    //Checks if the curve has started and turns off the warning.
                    if(curOffset >= nextCurveOffset){
                        warningFlag = false;
                        warningField.setText("-");
                        warningCount++;
                    }
                }else{
                    //Takes the difference between current time and next curve time.
                    Double diff = nextCurveOffset - curOffset;
                    //Gives warning when there is curve coming in next 3 seconds.
                    if(diff <= 3000.0){
                        warningFlag = true;
                        String text = "Warning - "+curveData.get(warningCount).getTurnDirection() +" Curve Ahead," +
                                "Curve Type - " + curveData.get(warningCount).getTurnType() + " Speed Curve";
                        warningField.setText(text);
                    }
                }
            }
        }
    }

    //Method to detect curve.
    private void detectCurve(SensorData sensorData) {
        Double curSteeringAngle = 0.0;
        //Get current steering angle.
        if(!angleField.getText().equals("-")){
            curSteeringAngle = Math.abs(Double.parseDouble(angleField.getText()));
        }
        Double speed = 0.0;
        if(!speedField.getText().equals("-")){
            speed = Double.parseDouble(speedField.getText());
        }
        //Get next Steering angle.
        Double nextSteeringAngle = Math.abs(Double.parseDouble(sensorData.getSensorData()));
        //This is the case when currently driving in curve.
        if(curveFlag){
            speed +=  curve.getAvgSpeed();
            curve.setAvgSpeed(speed);
            //When the steering wheel straightens the curve ends.
            if(nextSteeringAngle < 5 && nextSteeringAngle > -5 ){
                curveFlag = false;
                curve.setAvgSpeed(curve.getAvgSpeed()/sensorCount);
                //End GPS data saved.
                curve.setEndGPS(latField.getText() + " - " + lonField.getText());
                curve.setTurnType("LOW");
                // If avg speed is above 30 determined as high speed curve.
                if(curve.getAvgSpeed() > 30){
                    curve.setTurnType("HIGH");
                }
                //Displaying Curve data in GUI.
                String text = curveCount + ") " + curve.toString();
                if(!curveArea.getText().equals("-")){
                    text = curveArea.getText() + "\n" + text;
                }
                curveArea.setText(text);
                //On the first run saving the curve data to warn in future simulations.
                if(firstRun){
                    curveData.add(curve);
                }
                curve = null;
            }else{
                sensorCount++;
            }
            //Detecting curve.
        }else{
            //If the steering angle goes beyond +/- 20 curve is detected.
            if(curSteeringAngle < -20 || curSteeringAngle > 20){
                curveCount++;
                sensorCount = 1;
                curveFlag = true;
                curve = new CurveData();
                curve.setTimeOffset(Double.parseDouble(timeField.getText()));
                //Start GPS data saved.
                curve.setStartGPS(latField.getText() + " - " + lonField.getText());
                curve.setAvgSpeed(speed);
                curve.setTurnDirection("RIGHT");
                //If the steering angle is < 0 the turn direction is determined as left else right.
                if(Double.parseDouble(sensorData.getSensorData()) < 0){
                    curve.setTurnDirection("LEFT");
                }
            }
        }
    }

    //Resets the values after a simulation.
    private void resetValues() {
        timeField.setText("-");
        angleField.setText("-");
        speedField.setText("-");
        yawField.setText("-");
        lonAccField.setText("-");
        latAccField.setText("-");
        latField.setText("-");
        lonField.setText("-");
        warningField.setText("-");
        curveArea.setText("-");
        sensorCount = 0;
        curveFlag = false;
        curveCount = 0;
        sensorCount = 0;
        warningFlag = false;
        warningCount = 0;
        off = "-";
        ang = "-";
        vel = "-";
        yaw = "-";
        loac = "-";
        laac = "-";
        gla = "-";
        glo = "-";
    }

    //Start reading TRC data and displaying in the GUI.
    private void startReadingSensors() {
        File trcFile = new File(trcFileName);
        try {
            Scanner sc = new Scanner(trcFile);
            while(sc.hasNextLine()) {
                String s = sc.nextLine();
                //Read only lines that does not start with ";" character.
                if (!s.startsWith(";")) {
                    //Convert String " 75521)     40066.6  Rx         019F  8  01 00 59 10 29 07 F0 E4 " ->
                    // Array {"75521)", "40066.6", "Rx", "019F", "8", "01", "00", "59", "10", "29", "07", "F0", "E4"}
                    s = s.trim();
                    String[] st = s.split("\\s+");
                    //Extract the Offset time.
                    Double timeOffSet = Double.parseDouble(st[1]);
                    //Sending the Object to GUI for Display
                    if(st[3].equals("0003")){
                        SensorData data = new SensorData(timeOffSet, this.extractSteeringAngle(st[5],st[6]), SensorData.Sensor.STEERING_ANGLE);
                        sensorData.put(timeOffSet, data);
                    }else if(st[3].equals("019F")){
                        SensorData data = new SensorData(timeOffSet, this.extractDisplayedSpeed(st[5],st[6]), SensorData.Sensor.SPEED);
                        sensorData.put(timeOffSet, data);
                    }else if(st[3].equals("0245")){
                        SensorData data = new SensorData(timeOffSet, this.extractYawRate(st[5],st[6]), SensorData.Sensor.YAW_RATE);
                        sensorData.put(timeOffSet, data);
                        timeOffSet++;
                        data = new SensorData(timeOffSet - 1, this.extractLongitudinalOrLateralAcceleration(st[9]), SensorData.Sensor.LONGITUDINAL_ACCELERATION);
                        sensorData.put(timeOffSet, data);
                        timeOffSet++;
                        data = new SensorData(timeOffSet - 2, this.extractLongitudinalOrLateralAcceleration(st[10]), SensorData.Sensor.LATERAL_ACCELERATION);
                        sensorData.put(timeOffSet, data);
                    }else{
                        continue;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    //Takes in the Sensor data as input and displays them in their respective GUI fields.
    private void updateData(SensorData sensorData) {
        off = sensorData.getTimeOffset().toString();
        timeField.setText(off);
        if(sensorData.getSensorType() == SensorData.Sensor.GPS){
            String[] s = sensorData.getSensorData().split(",");
            gla = s[0];
            glo = s[1];
            latField.setText(gla);
            lonField.setText(glo);
        }else if(sensorData.getSensorType() == SensorData.Sensor.SPEED){
            vel = sensorData.getSensorData();
            speedField.setText(vel);
        }else if(sensorData.getSensorType() == SensorData.Sensor.YAW_RATE){
            yaw = sensorData.getSensorData();
            yawField.setText(yaw);
        }else if(sensorData.getSensorType() == SensorData.Sensor.LATERAL_ACCELERATION){
            laac = sensorData.getSensorData();
            latAccField.setText(laac);
        }else if(sensorData.getSensorType() == SensorData.Sensor.LONGITUDINAL_ACCELERATION){
            loac = sensorData.getSensorData();
            lonAccField.setText(loac);
        }else if(sensorData.getSensorType() == SensorData.Sensor.STEERING_ANGLE){
            ang = sensorData.getSensorData();
            angleField.setText(ang);
        }else{
            return;
        }
        if(ang.length() > 6)
            ang = ang.substring(0,5);
        if(vel.length() > 6)
            vel = vel.substring(0,5);
        if(yaw.length() > 6)
            yaw = yaw.substring(0,5);
        //Displaying in the console.
        System.out.print(off+"\t\t\t"+ang+"\t\t\t\t"+vel+"\t\t\t"+yaw+"\t\t\t"+loac+"\t\t\t\t\t"+laac+"\t\t\t"+gla+"\t\t"+glo + "\r");
    }

    //Reading and storing GPS data.
    private void readGPSData() {
        //Creating file to read from.
        File htmFile = new File(htmFileName);
        try {
            Scanner sc = new Scanner(htmFile);
            double time = 0;
            while(sc.hasNextLine()) {
                String s = sc.nextLine();
                //Read only lines that contains the sequence "new GLatLng("
                if (s.contains("new GLatLng(")) {
                    //Convert "new GLatLng( 52.721103, 13.223500)," -> "new GLatLng( 52.721103 13.223500)"
                    s = s.replace(",", "");
                    //Convert "new GLatLng( 52.721103 13.223500)" -> "new GLatLng 52.721103 13.223500)"
                    s = s.replace("(", "");
                    //Convert "new GLatLng 52.721103 13.223500)" -> "new GLatLng 52.721103 13.223500"
                    s = s.replace(")", "");
                    //Remove any trailing whitespaces.
                    s = s.trim();
                    // Convert the String "new GLatLng 52.721103 13.223500" -> Array {"new", "GLatLng", "52.721103", "13.223500"}
                    String[] st = s.split(" ");
                    int x = 2, y = 3;
                    double lat = 0, lon = 0;
                    //Extract the Latitude and Longitude values from the array based on different scenarios.
                    if (st[0].equals("[")) {
                        lat = Double.parseDouble(st[x + 2]);
                        lon = Double.parseDouble(st[y + 2]);
                    } else if (!st[0].equals("new")) {
                        continue;
                    } else {
                        lat = Double.parseDouble(st[x]);
                        lon = Double.parseDouble(st[y]);
                    }
                    sensorData.put(time, new SensorData(time, lat + "," + lon, SensorData.Sensor.GPS));
                    time += 1000;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String extractSteeringAngle(String b1, String b2){
        //Convert HexaDecimal to Binary.
        String byte1 = new BigInteger(b1, 16).toString(2);
        String byte2 = new BigInteger(b2, 16).toString(2);
        //Append 0 to ensure 8 bits size.
        //Ex: 02 Hex -> 10 binary, to make 10 -> 00000010
        for(int i = byte1.length(); i < 8; i++){
            byte1 = "0" + byte1;
        }
        //Append 0 to ensure 8 bits size.
        //Ex: 02 Hex -> 10 binary, to make 10 -> 00000010
        for(int i = byte2.length(); i < 8; i++){
            byte2 = "0" + byte2;
        }
        //Extract necessary bits from 2 bytes.
        byte1 = byte1.substring(2);
        byte1 = byte1 + byte2;
        //Convert Binary to decimal and Calculate the Steering Angle.
        Double angle = new BigInteger(byte1, 2).doubleValue();
        angle *= 0.5;
        angle += -2048;
        return angle.toString();
    }

    private String extractDisplayedSpeed(String b1, String b2){
        //Convert HexaDecimal to Binary.
        String byte1 = new BigInteger(b1, 16).toString(2);
        String byte2 = new BigInteger(b2, 16).toString(2);
        //Append 0 to ensure 8 bits size.
        //Ex: 02 Hex -> 10 binary, to make 10 -> 00000010
        for(int i = byte1.length(); i < 8; i++){
            byte1 = "0" + byte1;
        }
        //Append 0 to ensure 8 bits size.
        //Ex: 02 Hex -> 10 binary, to make 10 -> 00000010
        for(int i = byte2.length(); i < 8; i++){
            byte2 = "0" + byte2;
        }
        //Extract necessary bits from 2 bytes.
        byte1 = byte1.substring(4);
        byte1 = byte1 + byte2;
        //Convert Binary to decimal and Calculate the Display Speed.
        Double speed = new BigInteger(byte1, 2).doubleValue();
        speed *= 0.1;
        return speed.toString();
    }

    private String extractYawRate(String b1, String b2){
        //Convert HexaDecimal to Binary.
        String byte1 = new BigInteger(b1, 16).toString(2);
        String byte2 = new BigInteger(b2, 16).toString(2);
        //Append 0 to ensure 8 bits size.
        //Ex: 02 Hex -> 10 binary, to make 10 -> 00000010
        for(int i = byte1.length(); i < 8; i++){
            byte1 = "0" + byte1;
        }
        //Append 0 to ensure 8 bits size.
        //Ex: 02 Hex -> 10 binary, to make 10 -> 00000010
        for(int i = byte2.length(); i < 8; i++){
            byte2 = "0" + byte2;
        }
        //Extract necessary bits from 2 bytes.
        byte1 = byte1 + byte2;
        //Convert Binary to decimal and Calculate the Yaw Rate.
        Double yawRate = new BigInteger(byte1, 2).doubleValue();
        yawRate *= 0.01;
        yawRate += -327.68;
        return yawRate.toString();
    }

    private String extractLongitudinalOrLateralAcceleration(String b){
        //Convert HexaDecimal to Binary.
        String byte1 = new BigInteger(b, 16).toString(2);
        //Convert Binary to decimal and Calculate the Yaw Rate.
        Double acceleration = new BigInteger(byte1, 2).doubleValue();
        acceleration *= 0.08;
        acceleration += -10.24;
        return acceleration.toString();
    }

    public static void main(String a[]){
        System.out.println("File Path Linux instance : /Users/aravind/Documents/SER540/19 CANmessages.trc");
        System.out.println("File Path Windows instance : C:/Users/aravind/Documents/SER540/19 GPS Track.htm");
        System.out.println("Please Enter CAN Dump file path: ");
        ADASSimulator simulator = new ADASSimulator();
        //Taking file names as input.
        Scanner sc = new Scanner(System.in);
        simulator.trcFileName = sc.nextLine();
        System.out.println("Please Enter GPS file path: ");
        simulator.htmFileName = sc.nextLine();
        sc.close();
        simulator.activate();
    }
}
