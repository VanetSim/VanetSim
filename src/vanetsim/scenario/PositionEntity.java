package vanetsim.scenario;

/**
 * Simple Helper class to handle information in Positionverification process
 */
public class PositionEntity {
    private long receiverID_;
    private int receiverX_;
    private int receiverY_;
    private long senderID_;
    private int senderX_;
    private int senderY_;
    private double rssi_;
    private KnownVehicle knownVehicle_ = null;
    private boolean sourceIsRSU_ = false;
    private int firstContactTime_ = -1;

    public int getFirstContactTime() {
        return firstContactTime_;
    }

    public void setFirstContactTime(int Time) {
        this.firstContactTime_ = Time;
    }

    public KnownVehicle getKnownVehicle() {
        return knownVehicle_;
    }

    public void setSenderID(long ID) {
        this.senderID_ = ID;
    }

    private double calculatedDistance_ = Double.NaN;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PositionEntity) {
            PositionEntity tmp = (PositionEntity) obj;
            if (tmp.getSenderID() == senderID_)
                return true;
        }
        return false;
    }

    public PositionEntity(long receiverID, int receiverX, int receiverY, KnownVehicle knownVehicle, boolean sourceIsRSU) {
        receiverID_ = receiverID;
        receiverX_ = receiverX;
        receiverY_ = receiverY;
        senderID_ = knownVehicle.getID();
        senderX_ = knownVehicle.getX();
        senderY_ = knownVehicle.getY();
        rssi_ = knownVehicle.getRssi();
        knownVehicle_ = knownVehicle;
        sourceIsRSU_ = sourceIsRSU;
    }

    public double getCalculatedDistance() {
        return calculatedDistance_;
    }

    public void setCalculatedDistance(double distance) {
        this.calculatedDistance_ = distance;
    }

    public long getReceiverID() {
        return receiverID_;
    }

    public int getReceiverX() {
        return receiverX_;
    }

    public int getReceiverY() {
        return receiverY_;
    }

    public double getRSSI() {
        return rssi_;
    }

    public int getSenderX() {
        return senderX_;
    }

    public int getSenderY() {
        return senderY_;
    }

    public long getSenderID() {
        return senderID_;
    }

    public boolean isSourceIsRSU_() {
        return sourceIsRSU_;
    }

    public void setSourceIsRSU_(boolean sourceIsRSU) {
        this.sourceIsRSU_ = sourceIsRSU;
    }

}
