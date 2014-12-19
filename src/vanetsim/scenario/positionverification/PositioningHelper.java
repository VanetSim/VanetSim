package vanetsim.scenario.positionverification;

public class PositioningHelper {

    // TODO: add GUI setting
    private static boolean positionVerificationByRSUEnabled = true;
    private static boolean positionVerificationRSU_Triangulation = true;

    private static boolean positionVerificationByVehicleEnabled = false;

    private static boolean positionVerificationVehilceSendRssiToRsu = false;
    private static boolean positionVerificationRsuSendRssiToRsu = true;
    private static boolean positionVerificationVehilceSendRssiToVehicle = false;

    public static final int POSITIONVERIFICATION_TRIANGULATION = 1;

    public PositioningHelper() {
    }

    public static boolean isPositionVerificationRSU_Triangulation() {
        return positionVerificationRSU_Triangulation;
    }

    public static void setPositionVerificationRSU_Triangulation(boolean positionVerificationRSU_Triangulation) {
        PositioningHelper.positionVerificationRSU_Triangulation = positionVerificationRSU_Triangulation;
    }

    public static boolean isPositionVerificationVehilceSendRssiToVehicle() {
        return positionVerificationVehilceSendRssiToVehicle;
    }

    public static void setPositionVerificationVehilceSendRssiToVehicle(boolean state) {
        positionVerificationVehilceSendRssiToVehicle = state;
    }

    public static boolean getPositionVerificationByRSUEnabled() {
        return positionVerificationByRSUEnabled;
    }

    public static boolean getPositionVerificationByVehicleEnabled() {
        return positionVerificationByVehicleEnabled;
    }

    public static boolean isPositionVerificationVehilceSendRssiToRsu() {
        return positionVerificationVehilceSendRssiToRsu;
    }

    public static void setPositionVerificationVehilceSendRssiToRsu(boolean state) {
        positionVerificationVehilceSendRssiToRsu = state;
    }

    public static boolean isPositionVerificationRsuSendRssiToRsu() {
        return positionVerificationRsuSendRssiToRsu;
    }

    public static void setPositionVerificationRsuSendRssiToRsu(boolean state) {
        positionVerificationRsuSendRssiToRsu = state;
    }
}
