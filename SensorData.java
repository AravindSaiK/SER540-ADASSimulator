public class SensorData {
    enum Sensor{
        STEERING_ANGLE,
        SPEED,
        YAW_RATE,
        LONGITUDINAL_ACCELERATION,
        LATERAL_ACCELERATION,
        GPS
    }

    private Double timeOffset;
    private String sensorData;
    private Sensor sensorType;

    public SensorData(double timeOffset, String sensorData, Sensor sensorType) {
        this.timeOffset = timeOffset;
        this.sensorData = sensorData;
        this.sensorType = sensorType;
    }

    public Double getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(Double timeOffset) {
        this.timeOffset = timeOffset;
    }

    public String getSensorData() {
        return sensorData;
    }

    public void setSensorData(String sensorData) {
        this.sensorData = sensorData;
    }

    public Sensor getSensorType() {
        return sensorType;
    }

    public void setSensorType(Sensor sensorType) {
        this.sensorType = sensorType;
    }
}
