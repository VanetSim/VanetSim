package vanetsim.gui.controlpanels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import vanetsim.localization.Messages;
import vanetsim.scenario.positionverification.PositioningHelper;

public class LocationTechniquePanel extends JPanel implements ActionListener {

    /** The necessary constant for serializing. */
    private static final long serialVersionUID = -8668788010484644736L;

    /** RadioButton to select RSU Trilateration */
    JRadioButton useRSUTrilateration;

    /** RadioButton to select RSU Predict Movement */
    JRadioButton useRSUPredictMovement;

    /** RadioButton to select Vehicle Trace Neighbours */
    JRadioButton useVehicleTraceNeighbours;

    /** RadioButton to select Manual Mode */
    JRadioButton useManualMode;
    
  //TODO: add RadioButton for RSSi Ratio

    /** CheckBox to choose RSU to RSU Information Exchange. */
    private final JCheckBox rsuExchange2RSU_;
    /** JLabel to describe rsuExchange2RSU_ checkbox */
    private final JLabel rsuExchange2RSULabel_;

    /** CheckBox to choose Vehicle to RSU Information Exchange. */
    private final JCheckBox vehicleExchange2RSU_;
    /** JLabel to describe vehicleExchange2RSU_ checkbox */
    private final JLabel vehicleExchange2RSULabel_;

    /** CheckBox to choose Vehicle to Vehicle Information Exchange. */
    private final JCheckBox vehicleExchange2Vehicle_;
    /** JLabel to describe vehicleExchange2Vehicle_ checkbox */
    private final JLabel vehicleExchange2VehicleLabel_;

    /** The input field for the allowed Error */
    private final JFormattedTextField allowedError_;
    /** JLabel to describe allowedError_ JFormattedTextField */
    private final JLabel allowedErrorLabel_;

    /** The input field for the allowed RSSI Error */
    private final JFormattedTextField allowedRSSIError_;
    /** JLabel to describe allowedRSSIError_ JFormattedTextField */
    private final JLabel allowedRSSIErrorLabel_;

    /** The input field for the Threshold time */
    private final JFormattedTextField threshold_;
    /** JLabel to describe threshold_ JFormattedTextField */
    private final JLabel thresholdLabel_;

    /** Checkbox to enable Trilateration in manual mode */
    private final JCheckBox rsuTrilateration_;
    private final JLabel rsuTrilaterationLabel_;
    
    /** CheckBox to enable RSU Traffic monitoring */
    private final JCheckBox rsuPredictMovement_;
    private final JLabel rsuPredictMovementLabel_;
    
    /** CheckBox to enable Vehicle Tracing other Vehicles */
    private final JCheckBox vehicleTraceNeighbours_;
    private final JLabel vehicleTraceNeighboursLabel_;
    
    //TODO: add CheckBox and Label for RSSi Ratio for manual Mode

    public LocationTechniquePanel() {
        setLayout(new GridBagLayout());

        // global layout settings
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.PAGE_START;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 2;

        // Radio buttons to select mode
        ButtonGroup group = new ButtonGroup();
        useRSUTrilateration = new JRadioButton(Messages.getString("LocationTechniquePanel.techRSUTrilateration")); //$NON-NLS-1$
        useRSUTrilateration.setActionCommand("techRSUTrilateration"); //$NON-NLS-1$
        useRSUTrilateration.addActionListener(this);
        useRSUTrilateration.setSelected(true);
        group.add(useRSUTrilateration);
        ++c.gridy;
        add(useRSUTrilateration, c);

        useRSUPredictMovement = new JRadioButton(Messages.getString("LocationTechniquePanel.techRSUPredictmove")); //$NON-NLS-1$
        useRSUPredictMovement.setActionCommand("techRSUPredictmove"); //$NON-NLS-1$
        useRSUPredictMovement.addActionListener(this);
        group.add(useRSUPredictMovement);
        ++c.gridy;
        add(useRSUPredictMovement, c);

        useVehicleTraceNeighbours = new JRadioButton(Messages.getString("LocationTechniquePanel.techVehicleTraceNeighbours")); //$NON-NLS-1$
        useVehicleTraceNeighbours.setActionCommand("techVehicleTraceNeighbours"); //$NON-NLS-1$
        useVehicleTraceNeighbours.addActionListener(this);
        group.add(useVehicleTraceNeighbours);
        ++c.gridy;
        add(useVehicleTraceNeighbours, c);

        //TODO: add GUI creationcode for RSSI Ratio here
        
        useManualMode = new JRadioButton(Messages.getString("LocationTechniquePanel.ManualMode")); //$NON-NLS-1$
        useManualMode.setActionCommand("techManualMode"); //$NON-NLS-1$
        useManualMode.addActionListener(this);
        group.add(useManualMode);
        ++c.gridy;
        add(useManualMode, c);

        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridwidth = 2;
        c.gridx = 0;
        ++c.gridy;
        c.gridwidth = 2;
        add(new JSeparator(SwingConstants.HORIZONTAL), c);
        c.gridwidth = 1;

        // CheckBox RSU Exchange to RSU
        c.gridx = 0;
        rsuExchange2RSULabel_ = new JLabel(Messages.getString("PropagationModelPanel.exchangeRSU2RSU")); //$NON-NLS-1$
        ++c.gridy;
        add(rsuExchange2RSULabel_, c);
        rsuExchange2RSU_ = new JCheckBox();
        rsuExchange2RSU_.setSelected(true);
        rsuExchange2RSU_.setEnabled(false);
        rsuExchange2RSU_.setActionCommand("exchangeRSU2RSU"); //$NON-NLS-1$
        c.gridx = 1;
        add(rsuExchange2RSU_, c);
        rsuExchange2RSU_.addActionListener(this);

        // CheckBox Vehicle Exchange to RSU
        c.gridx = 0;
        vehicleExchange2RSULabel_ = new JLabel(Messages.getString("PropagationModelPanel.exchangeVehicle2RSU")); //$NON-NLS-1$
        ++c.gridy;
        add(vehicleExchange2RSULabel_, c);
        vehicleExchange2RSULabel_.setVisible(false);
        vehicleExchange2RSU_ = new JCheckBox();
        vehicleExchange2RSU_.setSelected(false);
        vehicleExchange2RSU_.setVisible(false);
        vehicleExchange2RSU_.setActionCommand("exchangeVEHICLE2RSU"); //$NON-NLS-1$
        c.gridx = 1;
        add(vehicleExchange2RSU_, c);
        vehicleExchange2RSU_.addActionListener(this);

        // CheckBox Vehicle Exchange to Vehicle
        c.gridx = 0;
        vehicleExchange2VehicleLabel_ = new JLabel(Messages.getString("PropagationModelPanel.exchangeVehicle2Vehicle")); //$NON-NLS-1$
        ++c.gridy;
        add(vehicleExchange2VehicleLabel_, c);
        vehicleExchange2VehicleLabel_.setVisible(false);
        vehicleExchange2Vehicle_ = new JCheckBox();
        vehicleExchange2Vehicle_.setSelected(false);
        vehicleExchange2Vehicle_.setVisible(false);
        vehicleExchange2Vehicle_.setActionCommand("exchangeVEHICLE2VEHICLE"); //$NON-NLS-1$
        c.gridx = 1;
        add(vehicleExchange2Vehicle_, c);
        vehicleExchange2Vehicle_.addActionListener(this);

        // allowed Error entry
        c.gridx = 0;
        allowedErrorLabel_ = new JLabel(Messages.getString("LocationTechniquePanel.allowedError")); //$NON-NLS-1$
        ++c.gridy;
        add(allowedErrorLabel_, c);
        allowedError_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        allowedError_.setValue(60);
        allowedError_.setPreferredSize(new Dimension(60, 20));
        allowedError_.setEditable(true);
        c.gridx = 1;
        add(allowedError_, c);

        // allowed RSSI Error entry
        c.gridx = 0;
        allowedRSSIErrorLabel_ = new JLabel(Messages.getString("LocationTechniquePanel.allowedRSSIError")); //$NON-NLS-1$
        ++c.gridy;
        add(allowedRSSIErrorLabel_, c);
        allowedRSSIError_ = new JFormattedTextField(new DecimalFormat());
        allowedRSSIError_.setValue(new Double(0.35));
        allowedRSSIError_.setPreferredSize(new Dimension(60, 20));
        allowedRSSIError_.setEditable(true);
        allowedRSSIError_.setVisible(false);
        allowedRSSIErrorLabel_.setVisible(false);
        c.gridx = 1;
        add(allowedRSSIError_, c);

        // Threshold entry
        c.gridx = 0;
        thresholdLabel_ = new JLabel(Messages.getString("LocationTechniquePanel.threshold")); //$NON-NLS-1$
        ++c.gridy;
        add(thresholdLabel_, c);
        thresholdLabel_.setVisible(false);
        threshold_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        threshold_.setValue(80000);
        threshold_.setPreferredSize(new Dimension(60, 20));
        threshold_.setEditable(true);
        threshold_.setVisible(false);
        c.gridx = 1;
        add(threshold_, c);

        // CheckBox RSU Trilateration
        c.gridx = 0;
        rsuTrilaterationLabel_ = new JLabel(Messages.getString("PropagationModelPanel.rsuTrilatCB")); //$NON-NLS-1$
        ++c.gridy;
        add(rsuTrilaterationLabel_, c);
        rsuTrilaterationLabel_.setVisible(false);
        rsuTrilateration_ = new JCheckBox();
        rsuTrilateration_.setSelected(false);
        rsuTrilateration_.setVisible(false);
        rsuTrilateration_.setActionCommand("activateRSUTrilateration"); //$NON-NLS-1$
        c.gridx = 1;
        add(rsuTrilateration_, c);
        rsuTrilateration_.addActionListener(this);

        // CheckBox RSU Predit Movement
        c.gridx = 0;
        rsuPredictMovementLabel_ = new JLabel(Messages.getString("PropagationModelPanel.rsuPredictMovement")); //$NON-NLS-1$
        ++c.gridy;
        add(rsuPredictMovementLabel_, c);
        rsuPredictMovementLabel_.setVisible(false);
        rsuPredictMovement_ = new JCheckBox();
        rsuPredictMovement_.setSelected(false);
        rsuPredictMovement_.setVisible(false);
        rsuPredictMovement_.setActionCommand("activateRSUPredictMovement"); //$NON-NLS-1$
        c.gridx = 1;
        add(rsuPredictMovement_, c);
        rsuPredictMovement_.addActionListener(this);

        // CheckBox Vehicle Trace Neighbours
        c.gridx = 0;
        vehicleTraceNeighboursLabel_ = new JLabel(Messages.getString("PropagationModelPanel.VehicleTrace")); //$NON-NLS-1$
        ++c.gridy;
        add(vehicleTraceNeighboursLabel_, c);
        vehicleTraceNeighboursLabel_.setVisible(false);
        vehicleTraceNeighbours_ = new JCheckBox();
        vehicleTraceNeighbours_.setSelected(false);
        vehicleTraceNeighbours_.setVisible(false);
        vehicleTraceNeighbours_.setActionCommand("activateVehicleTrace"); //$NON-NLS-1$
        c.gridx = 1;
        add(vehicleTraceNeighbours_, c);
        vehicleTraceNeighbours_.addActionListener(this);

        // to consume the rest of the space
        c.weighty = 1.0;
        ++c.gridy;
        JPanel space = new JPanel();
        space.setOpaque(false);
        add(space, c);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if ("techRSUTrilateration".equals(command)) {
            rsuExchange2RSU_.setSelected(true);
            rsuExchange2RSU_.setVisible(true);
            rsuExchange2RSU_.setEnabled(false);
            rsuExchange2RSULabel_.setVisible(true);
            PositioningHelper.setPositionVerificationRsuSendRssiToRsu(rsuExchange2RSU_.isSelected());

            vehicleExchange2RSU_.setSelected(false);
            vehicleExchange2RSU_.setVisible(false);
            vehicleExchange2RSULabel_.setVisible(false);
            PositioningHelper.setPositionVerificationVehilceSendRssiToRsu(vehicleExchange2RSU_.isSelected());

            vehicleExchange2Vehicle_.setSelected(false);
            vehicleExchange2Vehicle_.setVisible(false);
            vehicleExchange2VehicleLabel_.setVisible(false);
            PositioningHelper.setPositionVerificationVehilceSendRssiToVehicle(vehicleExchange2Vehicle_.isSelected());

            allowedError_.setVisible(true);
            allowedError_.setEnabled(true);
            allowedErrorLabel_.setVisible(true);
            threshold_.setVisible(false);
            thresholdLabel_.setVisible(false);
            allowedRSSIError_.setVisible(false);
            allowedRSSIErrorLabel_.setVisible(false);
            rsuTrilateration_.setVisible(false);
            rsuTrilaterationLabel_.setVisible(false);
            rsuPredictMovement_.setVisible(false);
            rsuPredictMovementLabel_.setVisible(false);
            vehicleTraceNeighbours_.setVisible(false);
            vehicleTraceNeighboursLabel_.setVisible(false);

            PositioningHelper.setPositionVerificationByRSUEnabled(true);
            PositioningHelper.setPositionVerificationByVehicleEnabled(false);
            PositioningHelper.setPositionVerificationRSU_Trilateration(true);
            PositioningHelper.setPositionVerificationRSU_PredictMovement(false);
            PositioningHelper.setPositionVerificationVehicle_TraceNeighbours(false);
        } else if ("techRSUPredictmove".equals(command)) {
            rsuExchange2RSU_.setSelected(false);
            rsuExchange2RSU_.setVisible(false);
            rsuExchange2RSU_.setEnabled(false);
            rsuExchange2RSULabel_.setVisible(false);
            PositioningHelper.setPositionVerificationRsuSendRssiToRsu(rsuExchange2RSU_.isSelected());

            vehicleExchange2RSU_.setSelected(false);
            vehicleExchange2RSU_.setVisible(false);
            vehicleExchange2RSULabel_.setVisible(false);
            PositioningHelper.setPositionVerificationVehilceSendRssiToRsu(vehicleExchange2RSU_.isSelected());

            vehicleExchange2Vehicle_.setSelected(false);
            vehicleExchange2Vehicle_.setVisible(false);
            vehicleExchange2VehicleLabel_.setVisible(false);
            PositioningHelper.setPositionVerificationVehilceSendRssiToVehicle(vehicleExchange2Vehicle_.isSelected());

            allowedError_.setVisible(false);
            allowedError_.setEnabled(false);
            allowedErrorLabel_.setVisible(false);
            threshold_.setVisible(false);
            thresholdLabel_.setVisible(false);
            allowedRSSIError_.setVisible(true);
            allowedRSSIErrorLabel_.setVisible(true);
            rsuTrilateration_.setVisible(false);
            rsuTrilaterationLabel_.setVisible(false);
            rsuPredictMovement_.setVisible(false);
            rsuPredictMovementLabel_.setVisible(false);
            vehicleTraceNeighbours_.setVisible(false);
            vehicleTraceNeighboursLabel_.setVisible(false);

            PositioningHelper.setPositionVerificationByRSUEnabled(true);
            PositioningHelper.setPositionVerificationByVehicleEnabled(false);
            PositioningHelper.setPositionVerificationRSU_Trilateration(false);
            PositioningHelper.setPositionVerificationRSU_PredictMovement(true);
            PositioningHelper.setPositionVerificationVehicle_TraceNeighbours(false);

        } else if ("exchangeRSU2RSU".equals(command)) {
            PositioningHelper.setPositionVerificationRsuSendRssiToRsu(rsuExchange2RSU_.isSelected());
        } else if ("exchangeVEHICLE2RSU".equals(command)) {
            PositioningHelper.setPositionVerificationVehilceSendRssiToRsu(vehicleExchange2RSU_.isSelected());
        } else if ("exchangeVEHICLE2VEHICLE".equals(command)) {
            PositioningHelper.setPositionVerificationVehilceSendRssiToVehicle(vehicleExchange2Vehicle_.isSelected());
        } else if ("techVehicleTraceNeighbours".equals(command)) {
            rsuExchange2RSU_.setSelected(false);
            rsuExchange2RSU_.setVisible(false);
            rsuExchange2RSU_.setEnabled(false);
            rsuExchange2RSULabel_.setVisible(false);
            PositioningHelper.setPositionVerificationRsuSendRssiToRsu(rsuExchange2RSU_.isSelected());

            vehicleExchange2RSU_.setSelected(false);
            vehicleExchange2RSU_.setVisible(false);
            vehicleExchange2RSULabel_.setVisible(false);
            PositioningHelper.setPositionVerificationVehilceSendRssiToRsu(vehicleExchange2RSU_.isSelected());

            vehicleExchange2Vehicle_.setSelected(true);
            vehicleExchange2Vehicle_.setVisible(false);
            vehicleExchange2VehicleLabel_.setVisible(false);
            PositioningHelper.setPositionVerificationVehilceSendRssiToVehicle(vehicleExchange2Vehicle_.isSelected());

            allowedError_.setVisible(false);
            allowedError_.setEnabled(false);
            allowedErrorLabel_.setVisible(false);
            threshold_.setVisible(true);
            thresholdLabel_.setVisible(true);
            allowedRSSIError_.setVisible(false);
            allowedRSSIErrorLabel_.setVisible(false);
            rsuTrilateration_.setVisible(false);
            rsuTrilaterationLabel_.setVisible(false);
            rsuPredictMovement_.setVisible(false);
            rsuPredictMovementLabel_.setVisible(false);
            vehicleTraceNeighbours_.setVisible(false);
            vehicleTraceNeighboursLabel_.setVisible(false);

            PositioningHelper.setPositionVerificationByRSUEnabled(false);
            PositioningHelper.setPositionVerificationByVehicleEnabled(true);
            PositioningHelper.setPositionVerificationRSU_Trilateration(false);
            PositioningHelper.setPositionVerificationRSU_PredictMovement(false);
            PositioningHelper.setPositionVerificationVehicle_TraceNeighbours(true);
        } else if ("techManualMode".equals(command)) {
            rsuExchange2RSU_.setVisible(true);
            rsuExchange2RSU_.setEnabled(true);
            rsuExchange2RSU_.setSelected(false);
            rsuExchange2RSULabel_.setVisible(true);
            vehicleExchange2RSU_.setVisible(true);
            vehicleExchange2RSULabel_.setVisible(true);
            vehicleExchange2Vehicle_.setVisible(true);
            vehicleExchange2VehicleLabel_.setVisible(true);
            allowedError_.setVisible(true);
            allowedError_.setEnabled(true);
            allowedErrorLabel_.setVisible(true);
            threshold_.setVisible(true);
            thresholdLabel_.setVisible(true);
            allowedRSSIError_.setVisible(true);
            allowedRSSIErrorLabel_.setVisible(true);
            rsuTrilateration_.setVisible(true);
            rsuTrilaterationLabel_.setVisible(true);
            rsuPredictMovement_.setVisible(true);
            rsuPredictMovementLabel_.setVisible(true);
            vehicleTraceNeighbours_.setVisible(true);
            vehicleTraceNeighboursLabel_.setVisible(true);
            
            //TODO: add settings for RSSI Ratio

        } else if ("activateRSUTrilateration".equals(command)) {
            PositioningHelper.setPositionVerificationByRSUEnabled(true);
            PositioningHelper.setPositionVerificationByVehicleEnabled(false);
            PositioningHelper.setPositionVerificationRSU_Trilateration(true);
            PositioningHelper.setPositionVerificationRSU_PredictMovement(false);
            PositioningHelper.setPositionVerificationVehicle_TraceNeighbours(false);
            rsuPredictMovement_.setSelected(false);
            vehicleTraceNeighbours_.setSelected(false);
        } else if ("activateRSUPredictMovement".equals(command)) {
            PositioningHelper.setPositionVerificationByRSUEnabled(true);
            PositioningHelper.setPositionVerificationByVehicleEnabled(false);
            PositioningHelper.setPositionVerificationRSU_Trilateration(false);
            PositioningHelper.setPositionVerificationRSU_PredictMovement(true);
            PositioningHelper.setPositionVerificationVehicle_TraceNeighbours(false);
            rsuTrilateration_.setSelected(false);
            vehicleTraceNeighbours_.setSelected(false);
        } else if ("activateVehicleTrace".equals(command)) {
            PositioningHelper.setPositionVerificationByRSUEnabled(false);
            PositioningHelper.setPositionVerificationByVehicleEnabled(true);
            PositioningHelper.setPositionVerificationRSU_Trilateration(false);
            PositioningHelper.setPositionVerificationRSU_PredictMovement(false);
            PositioningHelper.setPositionVerificationVehicle_TraceNeighbours(true);
            rsuTrilateration_.setSelected(false);
            rsuPredictMovement_.setSelected(false);
        }
        //TODO: add action handling for RSSI Ratio
    }

    public void loadAttributes() {
        allowedError_.setValue(PositioningHelper.getAllowedError());
        threshold_.setValue(PositioningHelper.getThreshold());
        allowedRSSIError_.setValue(PositioningHelper.getAllowedRssError());
    }

    public void saveAttributes() {
        PositioningHelper.setAllowedError(((Number) allowedError_.getValue()).intValue());
        PositioningHelper.setThreshold(((Number) threshold_.getValue()).intValue());
        PositioningHelper.setAllowedRSSIError(((Number) allowedRSSIError_.getValue()).doubleValue());
    }

    public void setLocationRSUTrilateration(boolean state) {
        useRSUTrilateration.setSelected(state);
    }

    public void setLocationRSUPredictMovement(boolean state) {
        useRSUPredictMovement.setSelected(state);
    }

    public void setLocationVehicleTraceNeighbours(boolean state) {
        useVehicleTraceNeighbours.setSelected(state);
    }

    public void setLocationVerifyByVehicle(boolean tmpBoolean) {
    }

    public void setLocationVehicleSendToRSU(boolean tmpBoolean) {
        vehicleExchange2RSU_.setSelected(tmpBoolean);
    }

    public void setLocationRSUSendToRSU(boolean tmpBoolean) {
        rsuExchange2RSU_.setSelected(tmpBoolean);
    }

    public void setLocationVehicleSendToVehicle(boolean tmpBoolean) {
        vehicleExchange2Vehicle_.setSelected(tmpBoolean);
    }
    
    //TODO: add getter and setter for RSSI Ratio CheckBox
}
