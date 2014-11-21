package vanetsim.scenario.propagation;

import java.util.Random;

public class PropagationModel {

    private static final PropagationModel INSTANCE = new PropagationModel();

    /** Free Space (or Friis) deterministic Propagation Model */
    public static final int PROPAGATION_MODEL_FREE_SPACE = 1;

    /** Log-Normal Shadowing probabilistic Propagation Model */
    public static final int PROPAGATION_MODEL_SHADOWING = 2;

    /**
     * The globally used Propagationmodel. This Setting is available in order to provide a mechanism to get a global config for all Vehicles.
     * <code>0</code> if no global Model is used.
     */
    //TODO: set the default to 0 when GUI sets this
    public int globalPropagationModel = 1;

    /** Random Number generator used for Gauss Distribution */
    private Random rand_ = new Random(System.currentTimeMillis());

    /** The standard deviation of the Gauss Distribution, used for e.g. Shadowing Propagation */
    double sigma_ = 4;

    /** The mean of the Gauss Distribution, for Shadowing this is 0. */
    double mean_ = 0;

    /** a reference Distance measured in meters */
    double referenceDistance_ = 1;

    /**
     * Sending and receiving parameters set to 1, because they are assumed to be equal for every vehicle. Parameters exist in case we want to change
     * these.
     */
    double sendingPower_, sendingGain_, receivingGain_ = 1;

    /** Passloss Factor for propagation modells. Attention! not used in Freespace, wherein this is fixed to 2. */
    double passLossFactor_ = 2;
    
    /** The Wavelength of the used transmission system. */
    double waveLength_ = 1;
    // the wavelength is currently without any effect, it just shifts the RSSI for all vehicles.
    
    /** received signal strength at <code> referenceDistance_ </code> **/
    double Pr_0 = Double.NaN;

    /** Empty constructor in order to disable instancing. */
    private PropagationModel() {
        calculateReferenceSignalStrength();
    }

    /** sets the PassLoss Facot **/
    public void setPassLossFactor(double passLossFactor){
        passLossFactor_= passLossFactor;
    }
    
    /** sets the Standard Deviation for the Gauss Distribution **/
    public void setGaussStandardDeviation(double sigma){
        sigma_ = sigma;
    }
    
    /** calculates a reference Signalstrength at a <code> referenceDistance_ </code>. Used for Shadowing **/ 
    private void calculateReferenceSignalStrength() {
        // calculate the reference received strength at a reference distance
        Pr_0 = sendingPower_ + sendingGain_ + receivingGain_ - (passLossFactor_ * 10 * Math.log10(4 * Math.PI * referenceDistance_ / waveLength_));
    }

    /** returns the global used Propagationmodel **/
    public int getGlobalPropagationModel() {
        return globalPropagationModel;
    }

    /** sets the global used Propagationmodel **/
    public void setGlobalPropagatinModel(int propagationModel) {
        globalPropagationModel = propagationModel;
    }

    /** returns an instance of the Propagationmodel **/
    public static PropagationModel getInstance() {
        return INSTANCE;
    }

    /**
     * Calculates the RSSI Value that would be received using a Propagation Model at the distance (dx,dy). <br><br>
     * Propagation Models:<br>
     * PropagationModel.PROPAGATION_MODEL_FREE_SPACE<br>
     * PropagationModel.PROPAGATION_MODEL_SHADOWING<br><br>
     * 
     * result will be the RSSI in [dB], uses dB because values are way better to interpret
     * @param propagationModel
     *            the propagation Model to be used.
     * @param dx
     * @param dy
     * @return the calculated RSSI value, or <code>NaN</code> if no RSSI could be calculated.
     */
    public double calculateRSSI(int propagationModel, long dx, long dy) {
        double result = Double.NaN;
        
        double d = Math.sqrt(dx * dx + dy * dy) / 10000; // convert [mm] to [m] to not mess up the formulas

        switch (propagationModel) {
            case PROPAGATION_MODEL_FREE_SPACE:
                // calculate the Friis / Free Space RSSI value in [dB]
                result = sendingPower_ + sendingGain_ + receivingGain_ - (10 * passLossFactor_ * Math.log10((4 * Math.PI * d) / waveLength_));
                break;
            
            case PROPAGATION_MODEL_SHADOWING:
                // get a random gauss distributed value
                double gaussNormal = rand_.nextGaussian() * sigma_ + mean_;

                // calculate the received signal strength in [dB]
                result = Pr_0 - (10 * passLossFactor_ * Math.log10(d / referenceDistance_)) + gaussNormal;

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
     * @return the distance in [m]
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
                // TODO: these parameters should be changeable through the GUI so verifiers can try to increase precision
                double tmp_sigma = 4;
                double tmp_mean = 0;

                // get a random gaussian value
                // maybe this doesn't need to be used at all, further experiments will show if this acually impacts results
                double X = rand_.nextGaussian() * tmp_sigma + tmp_mean;

                // calculate a distance from the rssi value
                result = referenceDistance_ * Math.pow(10, (Pr_0 + X - rssi) / (10 * passLossFactor_));

                break;
            default:
                result = Double.NaN;
                break;
        }
        return result;
    }
}
