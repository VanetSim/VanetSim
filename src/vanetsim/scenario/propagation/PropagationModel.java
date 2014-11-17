package vanetsim.scenario.propagation;

public class PropagationModel {

    private static final PropagationModel INSTANCE = new PropagationModel();

    /** Free Space (or Friis) deterministic Propagation Model */
    public static final int PROPAGATION_MODEL_FREE_SPACE = 1;

    /** Log-Normal Shadowing probabilistic Propagation Model */
    public static final int PROPAGATION_MODEL_SHADOWING = 2;

    /**
     * Empty constructor in order to disable instancing.
     */
    private PropagationModel() {
    }

    public static PropagationModel getInstance() {
        return INSTANCE;
    }

    public double calculateRSSI(int propagationModel, long dx, long dy) {
        double result = 0;
        switch (propagationModel) {
            case PROPAGATION_MODEL_FREE_SPACE:
                // TODO: implement this
                result = 10;
                break;
            case PROPAGATION_MODEL_SHADOWING:
                // TODO: implement this
                result = 20;
                break;
            default:
                break;
        }

        return result;
    }
    // // dummy implementation
    // // R=-10 * n *log10(d) + A
    // // n = Pathloss
    // // d = distance
    // // A = Signalstrength at 1m
    //
    // // sqrt is not needed, just use (dy * dy + dx * dx)
    // double d = Math.sqrt(dy * dy + dx * dx);
    // double n = 2;
    // // not needed because it is constant Addition
    // double A = -44.8;
    //
    // double res = -10 * n * Math.log10(d) + A;
}
