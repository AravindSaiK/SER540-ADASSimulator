public class CurveData {
    private Double avgSpeed, timeOffset;
    private String startGPS, endGPS, turnDirection, turnType;

    public CurveData(Double avgSpeed, String startGPS, String endGPS, String turnDirection, String turnType) {
        this.avgSpeed = avgSpeed;
        this.startGPS = startGPS;
        this.endGPS = endGPS;
        this.turnDirection = turnDirection;
        this.turnType = turnType;
    }

    public CurveData() {
    }

    public Double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(Double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public String getStartGPS() {
        return startGPS;
    }

    public void setStartGPS(String startGPS) {
        this.startGPS = startGPS;
    }

    public String getEndGPS() {
        return endGPS;
    }

    public void setEndGPS(String endGPS) {
        this.endGPS = endGPS;
    }

    public String getTurnDirection() {
        return turnDirection;
    }

    public void setTurnDirection(String turnDirection) {
        this.turnDirection = turnDirection;
    }

    public String getTurnType() {
        return turnType;
    }

    public void setTurnType(String turnType) {
        this.turnType = turnType;
    }

    public Double getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(Double timeOffset) {
        this.timeOffset = timeOffset;
    }

    @Override
    public String toString() {
        return "Curve:-\n" +
                "Avg Speed (km/h)= " + avgSpeed +
                ",\n Start GPS (Lat - Lon in Degrees)= " + startGPS + '\'' +
                ",\n End GPS (Lat - Lon in Degrees)= " + endGPS + '\'' +
                ",\n Curve Direction= " + turnDirection  +
                " Curve,\n Curve Type= " + turnType +
                " Speed Curve.";
    }
}
