package vanetsim.scenario.positionverification;

public class PositioningHelper {

    private static boolean positionVerificationByRSUEnabled = false;

    private static boolean positionVerificationRSU_Trilateration = false;
    private static boolean positionVerificationRSU_PredictMovement = false;

    private static boolean positionVerificationByVehicleEnabled = false;
    private static boolean positionVerificationVehicle_TraceNeighbours = false;

    private static boolean positionVerificationVehilceSendRssiToRsu = false;
    private static boolean positionVerificationRsuSendRssiToRsu = false;
    private static boolean positionVerificationVehilceSendRssiToVehicle = false;

    private static int allowedError = 60; // [mm]
    private static int threshold = 80000; // [ms]

    public static int getAllowedError() {
        return allowedError;
    }

    public static int getThreshold() {
        return threshold;
    }

    public static void setAllowedError(int allowedError) {
        PositioningHelper.allowedError = allowedError;
    }

    public static void setThreshold(int usedTheshold) {
        threshold = usedTheshold;
    }

    public PositioningHelper() {
    }

    public static boolean isPositionVerificationRSU_Trilateration() {
        return positionVerificationRSU_Trilateration;
    }

    public static void setPositionVerificationRSU_Trilateration(boolean state) {
        PositioningHelper.positionVerificationRSU_Trilateration = state;
    }

    public static boolean isPositionVerificationVehicle_TraceNeighbours() {
        return positionVerificationVehicle_TraceNeighbours;
    }

    public static void setPositionVerificationVehicle_TraceNeighbours(boolean state) {
        positionVerificationVehicle_TraceNeighbours = state;
    }

    public static void setPositionVerificationByRSUEnabled(boolean positionVerificationByRSUEnabled) {
        PositioningHelper.positionVerificationByRSUEnabled = positionVerificationByRSUEnabled;
    }

    public static void setPositionVerificationByVehicleEnabled(boolean positionVerificationByVehicleEnabled) {
        PositioningHelper.positionVerificationByVehicleEnabled = positionVerificationByVehicleEnabled;
    }

    public static boolean isPositionVerificationVehilceSendRssiToVehicle() {
        return positionVerificationVehilceSendRssiToVehicle;
    }

    public static void setPositionVerificationVehilceSendRssiToVehicle(boolean state) {
        positionVerificationVehilceSendRssiToVehicle = state;
    }

    public static boolean isPositionVerificationByRSUEnabled() {
        return positionVerificationByRSUEnabled;
    }

    public static boolean isPositionVerificationByVehicleEnabled() {
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

    public static boolean isPositionVerificationRSU_PredictMovement() {
        return positionVerificationRSU_PredictMovement;
    }

    public static void setPositionVerificationRSU_PredictMovement(boolean state) {
        positionVerificationRSU_PredictMovement = state;
    }
}
