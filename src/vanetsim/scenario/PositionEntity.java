package vanetsim.scenario;

/**
 * Simple Helper class to handle information in Positionverification process
 */
public class PositionEntity {
    private long receiverID_;
    private int receiverX_;
    private int receiverY_;
    private KnownVehicle[] knownVehicleArray_;

    public PositionEntity(long receiverID, int receiverX, int receiverY, KnownVehicle[] knownVehicleArray) {
        // TODO: remove system.out
        System.out.println("receiver of signal: " + receiverID);
        receiverID_ = receiverID;
        receiverX_ = receiverX;
        receiverY_ = receiverY;
        knownVehicleArray_ = knownVehicleArray;
    }

    public long getReceiverID_() {
        return receiverID_;
    }

    public int getReceiverX_() {
        return receiverX_;
    }

    public int getReceiverY_() {
        return receiverY_;
    }

    public KnownVehicle[] getKnownVehicleArray_() {
        return knownVehicleArray_;
    }

}
