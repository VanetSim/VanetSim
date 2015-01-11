package vanetsim.gui.controlpanels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private static final long serialVersionUID = 1294546530654701135L;

    /** RadioButton to select RSU Trilateration */
    JRadioButton useRSUTrilateration;

    /** RadioButton to select RSU Predict Movement */
    JRadioButton useRSUPredictMovement;

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
        vehicleExchange2RSU_ = new JCheckBox();
        vehicleExchange2RSU_.setSelected(false);
        vehicleExchange2RSU_.setActionCommand("exchangeVEHICLE2RSU"); //$NON-NLS-1$
        c.gridx = 1;
        add(vehicleExchange2RSU_, c);
        rsuExchange2RSU_.addActionListener(this);

        // CheckBox Vehicle Exchange to Vehicle
        c.gridx = 0;
        vehicleExchange2VehicleLabel_ = new JLabel(Messages.getString("PropagationModelPanel.exchangeVehicle2Vehicle")); //$NON-NLS-1$
        ++c.gridy;
        add(vehicleExchange2VehicleLabel_, c);
        vehicleExchange2Vehicle_ = new JCheckBox();
        vehicleExchange2Vehicle_.setSelected(false);
        vehicleExchange2Vehicle_.setActionCommand("exchangeVEHICLE2VEHICLE"); //$NON-NLS-1$
        c.gridx = 1;
        add(vehicleExchange2Vehicle_, c);
        rsuExchange2RSU_.addActionListener(this);

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
            allowedError_.setVisible(true);
            allowedError_.setEditable(true);
            allowedErrorLabel_.setVisible(true);
            PositioningHelper.setPositionVerificationByRSUEnabled(true);
            PositioningHelper.setPositionVerificationByVehicleEnabled(false);
            PositioningHelper.setPositionVerificationRSU_Trilateration(true);
            PositioningHelper.setPositionVerificationRSU_PredictMovement(false);
            rsuExchange2RSU_.setSelected(true);
            rsuExchange2RSU_.setEnabled(false);

        } else if ("techRSUPredictmove".equals(command)) {
            allowedError_.setVisible(false);
            allowedError_.setEditable(false);
            allowedErrorLabel_.setVisible(false);
            rsuExchange2RSU_.setEnabled(true);
            PositioningHelper.setPositionVerificationByRSUEnabled(true);
            PositioningHelper.setPositionVerificationByVehicleEnabled(false);
            PositioningHelper.setPositionVerificationRSU_Trilateration(false);
            PositioningHelper.setPositionVerificationRSU_PredictMovement(true);

        } else if ("exchangeRSU2RSU".equals(command)) {
            PositioningHelper.setPositionVerificationRsuSendRssiToRsu(rsuExchange2RSU_.isSelected());
        } else if ("exchangeVEHICLE2RSU".equals(command)) {
            PositioningHelper.setPositionVerificationVehilceSendRssiToRsu(vehicleExchange2RSU_.isSelected());
        } else if ("exchangeVEHICLE2VEHICLE".equals(command)) {
            PositioningHelper.setPositionVerificationVehilceSendRssiToVehicle(vehicleExchange2Vehicle_.isSelected());
        }

    }

    public void loadAttributes() {
        allowedError_.setValue(PositioningHelper.getAllowedError());
    }

    public void saveAttributes() {
        PositioningHelper.setAllowedError(((Number) allowedError_.getValue()).intValue());
    }

}
