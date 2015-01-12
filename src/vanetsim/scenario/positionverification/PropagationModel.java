package vanetsim.scenario.positionverification;

import java.util.Random;

import org.apache.commons.math3.distribution.GammaDistribution;

public class PropagationModel {

    private static final PropagationModel INSTANCE = new PropagationModel();

    /** Free Space (or Friis) deterministic Propagation Model **/
    public static final int PROPAGATION_MODEL_FREE_SPACE = 1;

    /** Log-Normal Shadowing probabilistic Propagation Model **/
    public static final int PROPAGATION_MODEL_SHADOWING = 2;

    /** Nakagami probabilistic Propagation Model **/
    public static final int PROPAGATION_MODEL_NAKAGAMI = 3;

    /**
     * The globally used Propagationmodel. This Setting is available in order to provide a mechanism to get a global config for all Vehicles.
     * <code>0</code> if no global Model is used.
     */
    private static int globalPropagationModel = 1;

    /** Random Number generator used for Gauss Distribution */
    private Random rand_ = new Random(System.currentTimeMillis());

    /** The standard deviation of the Gauss Distribution, used for e.g. Shadowing Propagation */
    static double sigma_ = 4;

    /** The mean of the Gauss Distribution, for Shadowing this is 0. */
    static double mean_ = 0;

    /** a reference Distance measured in meters */
    static double referenceDistance_ = 1;

    /**
     * Sending and receiving parameters set to 1, because they are assumed to be equal for every vehicle. Parameters exist in case we want to change
     * these.
     */
    static double sendingPower_, sendingGain_, receivingGain_ = 1;

    /** Passloss Factor for propagation modells. */
    static double passLossFactor_ = 2;

    /** The Wavelength of the used transmission system. */
    static double waveLength_ = 1;
    // the wavelength is currently without any effect, it just shifts the RSSI for all vehicles.

    /** received signal strength at <code> referenceDistance_ </code> **/
    static double Pr_0 = Double.NaN;

    /** Empty constructor in order to disable instancing. */
    private PropagationModel() {
        calculateReferenceSignalStrength();
    }

    /** sets the PassLoss Facot **/
    public static void setPassLossFactor(double passLossFactor) {
        passLossFactor_ = passLossFactor;
    }

    /** sets the Standard Deviation for the Gauss Distribution **/
    public void setGaussStandardDeviation(double sigma) {
        sigma_ = sigma;
    }

    /** calculates a reference Signalstrength at a <code> referenceDistance_ </code>. Used for Shadowing **/
    private void calculateReferenceSignalStrength() {
        if (Pr_0 != Double.NaN) {
            return;
        }
        // calculate the reference received strength at a reference distance
        Pr_0 = sendingPower_ + sendingGain_ + receivingGain_ - (passLossFactor_ * 10 * Math.log10(4 * Math.PI * referenceDistance_ / waveLength_));
    }

    /** returns the global used Propagationmodel **/
    public static int getGlobalPropagationModel() {
        return globalPropagationModel;
    }

    public static void setGlobalPropagationModel(int globalPropagationModel) {
        PropagationModel.globalPropagationModel = globalPropagationModel;
    }

    public static double getSigma() {
        return sigma_;
    }

    public static void setSigma(double sigma_) {
        PropagationModel.sigma_ = sigma_;
    }

    public static double getMean() {
        return mean_;
    }

    public static void setMean(double mean_) {
        PropagationModel.mean_ = mean_;
    }

    public static double getReferenceDistance() {
        return referenceDistance_;
    }

    public static void setReferenceDistance(double referenceDistance_) {
        PropagationModel.referenceDistance_ = referenceDistance_;
    }

    public static double getSendingPower() {
        return sendingPower_;
    }

    public static void setSendingPower(double sendingPower_) {
        PropagationModel.sendingPower_ = sendingPower_;
    }

    public static double getSendingGain() {
        return sendingGain_;
    }

    public static void setSendingGain(double sendingGain_) {
        PropagationModel.sendingGain_ = sendingGain_;
    }

    public static double getReceivingGain() {
        return receivingGain_;
    }

    public static void setReceivingGain(double receivingGain_) {
        PropagationModel.receivingGain_ = receivingGain_;
    }

    public static double getPassLossFactor() {
        return passLossFactor_;
    }

    public static double getWaveLength() {
        return waveLength_;
    }

    public static void setWaveLength_(double waveLength_) {
        PropagationModel.waveLength_ = waveLength_;
    }

    public static double getPr_0() {
        return Pr_0;
    }

    public static void setPr_0(double pr_0) {
        Pr_0 = pr_0;
    }

    /** returns an instance of the Propagationmodel **/
    public static PropagationModel getInstance() {
        return INSTANCE;
    }

    /**
     * Calculates the RSSI Value that would be received using a Propagation Model at the distance (dx,dy). <br>
     * <br>
     * Propagation Models:<br>
     * PropagationModel.PROPAGATION_MODEL_FREE_SPACE<br>
     * PropagationModel.PROPAGATION_MODEL_SHADOWING<br>
     * <br>
     * 
     * result will be the RSSI in [dB], uses dB because values are way better to interpret
     * 
     * @param propagationModel
     *            the propagation Model to be used.
     * @param dx
     * @param dy
     * @return the calculated RSSI value, or <code>NaN</code> if no RSSI could be calculated.
     */

    public double calculateRSSI(int propagationModel, long dx, long dy) {
        double d = Math.sqrt(dx * dx + dy * dy);// / 100; // convert [cm] to [m] to not mess up the formulas
        // this might help if GPS Traces are used together with RSSI Traces to keep Numbers at scale
        double result = calculateRSSI(propagationModel, d);
        return result;

    }

    public double calculateRSSI(int propagationModel, double in) {
        double result = Double.NaN;
        double distance = in / 100;

        switch (propagationModel) {
            case PROPAGATION_MODEL_FREE_SPACE:
                // calculate the Friis / Free Space RSSI value in [dB]
                result = sendingPower_ + sendingGain_ + receivingGain_ - (10 * passLossFactor_ * Math.log10((4 * Math.PI * distance) / waveLength_));
                break;

            case PROPAGATION_MODEL_SHADOWING:
                // get a random gauss distributed value
                double gaussNormal = rand_.nextGaussian() * sigma_ + mean_;

                // calculate the received signal strength in [dB]
                result = Pr_0 - (10 * passLossFactor_ * Math.log10(distance / referenceDistance_)) + gaussNormal;

                break;
            case PROPAGATION_MODEL_NAKAGAMI:
                // TODO: implement this!
                // Gamma Distributed Rx
                //  pseudo
                // double powerW = std::pow (10, (txPowerDbm - 30) / 10);
                // resultPowerW = m_gammaRandomVariable->GetValue (m, powerW / m);
                // double resultPowerDbm = 10 * std::log10 (resultPowerW) + 30;

                /*
                 * The Gamma distribution density function has the form
                 * 
                 * x^(alpha-1) * exp(-x/beta) p(x; alpha, beta) = ---------------------------- beta^alpha * Gamma(alpha)
                 * 
                 * for x > 0.
                 */

                break;
            default:
                break;
        }

        return result;
    }

    /**
     * calculates a distance to the rssi value.
     * 
     * @param propagationModel
     *            the propagationmodel to be used
     * @param rssi
     *            the rssi value to which the distance should be calculated
     * @return the distance
     */
    public double calculateDistance(int propagationModel, double rssi) {
        double result = Double.NaN;
        switch (propagationModel) {
            case PROPAGATION_MODEL_FREE_SPACE:
                // inversed Friis Formula
                result = waveLength_ / (4 * Math.PI)
                        * Math.pow(10, ((sendingPower_ + sendingGain_ + receivingGain_ - rssi) / (10 * passLossFactor_)));
                break;

            case PROPAGATION_MODEL_SHADOWING:
                // TODO: these parameters maybe should be changeable through the GUI so verifiers can try to increase precision
                double tmp_sigma = 4;
                double tmp_mean = 0;

                // get a random gaussian value
                // maybe this doesn't need to be used at all, further experiments will show if this actually impacts results
                double X = rand_.nextGaussian() * tmp_sigma + tmp_mean;

                // calculate a distance from the rssi value
                result = referenceDistance_ * Math.pow(10, (Pr_0 + X - rssi) / (10 * passLossFactor_));

                break;
            case PROPAGATION_MODEL_NAKAGAMI:
                // TODO: implement this!
                break;
            default:
                result = Double.NaN;
                break;
        }
        return result * 100;
    }

}
