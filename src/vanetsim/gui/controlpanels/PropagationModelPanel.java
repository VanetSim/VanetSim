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
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import vanetsim.localization.Messages;
import vanetsim.scenario.Vehicle;
import vanetsim.scenario.positionverification.PropagationModel;

public class PropagationModelPanel extends JPanel implements ActionListener {
    /** The necessary constant for serializing. */
    private static final long serialVersionUID = -2615968824101592112L;

    /** CheckBox to choose . */
    private final JCheckBox activateSendRSSI_;

    /** JLabel to describe activateSendRSSI_ checkbox */
    private final JLabel activateSendRSSILabel_;

    /** JCombobox to select choice */
    private final JComboBox<String> propagationModelChoice_;

    /** JLabel to describe propagationModelChoice JComboBox */
    private final JLabel propagationModelChoiceLabel_;

    /** The input field for Sigma, used in Gauss Distributions */
    private final JFormattedTextField gaussSigma_;

    /** JLabel to describe gaussSigma_ JFormattedTextField */
    private final JLabel gaussSigmaLabel_;

    /** The input field for Mean, used in Gauss Distributions */
    private final JFormattedTextField gaussMean_;

    /** JLabel to describe gaussMean_ JFormattedTextField */
    private final JLabel gaussMeanLabel_;

    /** RadioButton to select Calculation of Reference RSSI */
    JRadioButton calcPr0_;

    /** RadioButton to select entering of Reference RSSI */
    JRadioButton enterPr0_;

    /** The input field for the Referencedistance */
    private final JFormattedTextField referenceDistnace_;

    /** JLabel to describe referenceDistnace_ JFormattedTextField */
    private final JLabel referenceDistnaceLabel_;

    /** The input field for Pr_0 */
    private final JFormattedTextField pr_0_;
    /** JLabel to describe pr_0_ JFormattedTextField */
    private final JLabel pr_0Label_;

    /** The input field for the sending Power */
    private final JFormattedTextField pSend_;
    /** JLabel to describe pSend_ JFormattedTextField */
    private final JLabel pSendLabel_;

    /** The input field for the sending Gain */
    private final JFormattedTextField gainSend_;
    /** JLabel to describe gainSend_ JFormattedTextField */
    private final JLabel gainSendLabel_;

    /** The input field for receiving Gain */
    private final JFormattedTextField gainReceive_;
    /** JLabel to describe gainReceive_ JFormattedTextField */
    private final JLabel gainReceiveLabel_;

    /** The input field for the PathLoss Factor */
    private final JFormattedTextField pathLossFactor_;
    /** JLabel to describe pathLossFactor_ JFormattedTextField */
    private final JLabel pathLossFactorLabel_;

    /** The input field for the Wavelength */
    private final JFormattedTextField lambda_;
    /** JLabel to describe lambda_ JFormattedTextField */
    private final JLabel lambdaLabel_;

    /** An array with all Propagation models */
    private static final String[] PRESET_TYPES = { Messages.getString("PropagationModelPanel.freeSpace"),
            Messages.getString("PropagationModelPanel.shadowing"), Messages.getString("PropagationModelPanel.nakagami") };

    /**
     * Constructor, creating GUI items.
     */
    public PropagationModelPanel() {
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

        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 5, 5);

        // CheckBox to enable sending RSSI
        c.gridx = 0;
        activateSendRSSILabel_ = new JLabel(Messages.getString("PropagationModelPanel.activateSendRSSI")); //$NON-NLS-1$
        ++c.gridy;
        add(activateSendRSSILabel_, c);
        activateSendRSSI_ = new JCheckBox();
        activateSendRSSI_.setSelected(false);
        activateSendRSSI_.setActionCommand("activateSendRSSI"); //$NON-NLS-1$
        c.gridx = 1;
        add(activateSendRSSI_, c);
        activateSendRSSI_.addActionListener(this);

        // ComboBox Model Choice
        c.gridwidth = 1;
        c.gridx = 0;
        propagationModelChoiceLabel_ = new JLabel(Messages.getString("PropagationModelPanel.propagationModelChoice")); //$NON-NLS-1$
        ++c.gridy;
        add(propagationModelChoiceLabel_, c);
        c.gridx = 1;
        propagationModelChoice_ = new JComboBox<String>(PRESET_TYPES);
        propagationModelChoice_.setSelectedIndex(0);
        add(propagationModelChoice_, c);

        // Sigma entry
        c.gridx = 0;
        gaussSigmaLabel_ = new JLabel(Messages.getString("PropagationModelPanel.sigma")); //$NON-NLS-1$
        ++c.gridy;
        add(gaussSigmaLabel_, c);
        gaussSigma_ = new JFormattedTextField(NumberFormat.getNumberInstance());
        gaussSigma_.setValue(new Double(4));
        gaussSigma_.setPreferredSize(new Dimension(60, 20));
        c.gridx = 1;
        add(gaussSigma_, c);

        // Mean entry
        c.gridx = 0;
        gaussMeanLabel_ = new JLabel(Messages.getString("PropagationModelPanel.mean")); //$NON-NLS-1$
        ++c.gridy;
        add(gaussMeanLabel_, c);
        gaussMean_ = new JFormattedTextField(NumberFormat.getNumberInstance());
        gaussMean_.setValue(new Double(0));
        gaussMean_.setPreferredSize(new Dimension(60, 20));
        c.gridx = 1;
        add(gaussMean_, c);

        c.gridwidth = 2;
        c.gridx = 0;
        ++c.gridy;
        add(new JSeparator(SwingConstants.HORIZONTAL), c);
        ++c.gridy;

        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.gridwidth = 2;
        // Radio buttns to select Reference Distance/Rx entry
        ButtonGroup group = new ButtonGroup();
        calcPr0_ = new JRadioButton(Messages.getString("PropagationModelPanel.calcPr0")); //$NON-NLS-1$
        calcPr0_.setActionCommand("calcPr0"); //$NON-NLS-1$
        calcPr0_.addActionListener(this);
        calcPr0_.setSelected(true);
        group.add(calcPr0_);
        ++c.gridy;
        add(calcPr0_, c);

        enterPr0_ = new JRadioButton(Messages.getString("PropagationModelPanel.enterPr0")); //$NON-NLS-1$
        enterPr0_.setActionCommand("enterPr0"); //$NON-NLS-1$
        enterPr0_.addActionListener(this);
        group.add(enterPr0_);
        ++c.gridy;
        add(enterPr0_, c);

        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 5, 5);

        // ref Distance entry
        c.gridx = 0;
        referenceDistnaceLabel_ = new JLabel(Messages.getString("PropagationModelPanel.refDist")); //$NON-NLS-1$
        ++c.gridy;
        add(referenceDistnaceLabel_, c);
        referenceDistnace_ = new JFormattedTextField(NumberFormat.getNumberInstance());
        referenceDistnace_.setValue(new Double(1));
        referenceDistnace_.setPreferredSize(new Dimension(60, 20));
        referenceDistnace_.setEditable(false);
        c.gridx = 1;
        add(referenceDistnace_, c);

        // ref Pr_0 entry
        c.gridx = 0;
        pr_0Label_ = new JLabel(Messages.getString("PropagationModelPanel.refPr0")); //$NON-NLS-1$
        ++c.gridy;
        add(pr_0Label_, c);
        pr_0_ = new JFormattedTextField(NumberFormat.getNumberInstance());
        pr_0_.setValue(new Double(Double.NaN));
        pr_0_.setPreferredSize(new Dimension(60, 20));
        pr_0_.setEditable(false);
        c.gridx = 1;
        add(pr_0_, c);
        c.gridwidth = 2;
        c.gridx = 0;
        ++c.gridy;
        add(new JSeparator(SwingConstants.HORIZONTAL), c);

        // Sending Power entry
        c.gridx = 0;
        pSendLabel_ = new JLabel(Messages.getString("PropagationModelPanel.psend")); //$NON-NLS-1$
        ++c.gridy;
        add(pSendLabel_, c);
        pSend_ = new JFormattedTextField(NumberFormat.getNumberInstance());
        pSend_.setValue(new Double(1));
        pSend_.setPreferredSize(new Dimension(60, 20));
        c.gridx = 1;
        add(pSend_, c);

        // Sending Gain entry
        c.gridx = 0;
        gainSendLabel_ = new JLabel(Messages.getString("PropagationModelPanel.sendgain")); //$NON-NLS-1$
        ++c.gridy;
        add(gainSendLabel_, c);
        gainSend_ = new JFormattedTextField(NumberFormat.getNumberInstance());
        gainSend_.setValue(new Double(1));
        gainSend_.setPreferredSize(new Dimension(60, 20));
        c.gridx = 1;
        add(gainSend_, c);

        // Sending Gain entry
        c.gridx = 0;
        gainReceiveLabel_ = new JLabel(Messages.getString("PropagationModelPanel.receivegain")); //$NON-NLS-1$
        ++c.gridy;
        add(gainReceiveLabel_, c);
        gainReceive_ = new JFormattedTextField(NumberFormat.getNumberInstance());
        gainReceive_.setValue(new Double(1));
        gainReceive_.setPreferredSize(new Dimension(60, 20));
        c.gridx = 1;
        add(gainReceive_, c);

        // PathLoss Factor entry
        c.gridx = 0;
        pathLossFactorLabel_ = new JLabel(Messages.getString("PropagationModelPanel.pathloass")); //$NON-NLS-1$
        ++c.gridy;
        add(pathLossFactorLabel_, c);
        pathLossFactor_ = new JFormattedTextField(NumberFormat.getNumberInstance());
        pathLossFactor_.setValue(new Double(1));
        pathLossFactor_.setPreferredSize(new Dimension(60, 20));
        c.gridx = 1;
        add(pathLossFactor_, c);

        // Wavelength entry
        c.gridx = 0;
        lambdaLabel_ = new JLabel(Messages.getString("PropagationModelPanel.lambda")); //$NON-NLS-1$
        ++c.gridy;
        add(lambdaLabel_, c);
        lambda_ = new JFormattedTextField(NumberFormat.getNumberInstance());
        lambda_.setValue(new Double(1));
        lambda_.setPreferredSize(new Dimension(60, 20));
        c.gridx = 1;
        add(lambda_, c);

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

        if ("activateSendRSSI".equals(command)) {
            Vehicle.setSendRssiEnabled(activateSendRSSI_.isSelected());
        } else if ("calcPr0".equals(command)) {
            referenceDistnace_.setEditable(false);
            referenceDistnace_.setValue(new Double(Double.NaN));
            pr_0_.setEditable(false);
            pr_0_.setValue(Double.NaN);
        } else if ("enterPr0".equals(command)) {
            referenceDistnace_.setEditable(true);
            referenceDistnace_.setValue(new Double(1));
            pr_0_.setEditable(true);
            pr_0_.setValue(new Double(-1));
        }
    }

    public void saveAttributes() {
        PropagationModel.setSigma(((Number) gaussSigma_.getValue()).doubleValue());

        PropagationModel.setMean(((Number) gaussMean_.getValue()).doubleValue());

        PropagationModel.setReferenceDistance(((Number) referenceDistnace_.getValue()).doubleValue());

        double tmp = ((Number) pr_0_.getValue()).doubleValue();
        PropagationModel.setPr_0(tmp);

        PropagationModel.setSendingPower(((Number) pSend_.getValue()).doubleValue());

        PropagationModel.setSendingGain(((Number) gainSend_.getValue()).doubleValue());

        PropagationModel.setReceivingGain(((Number) gainReceive_.getValue()).doubleValue());

        PropagationModel.setPassLossFactor(((Number) pathLossFactor_.getValue()).doubleValue());

        PropagationModel.setWaveLength_(((Number) lambda_.getValue()).doubleValue());

        String tmpSelected = propagationModelChoice_.getSelectedItem().toString();
        if (tmpSelected.equals(Messages.getString("PropagationModelPanel.freeSpace"))) {
            PropagationModel.setGlobalPropagationModel(PropagationModel.PROPAGATION_MODEL_FREE_SPACE);
        } else if (tmpSelected.equals(Messages.getString("PropagationModelPanel.shadowing"))) {
            PropagationModel.setGlobalPropagationModel(PropagationModel.PROPAGATION_MODEL_SHADOWING);
        } else if (tmpSelected.equals(Messages.getString("PropagationModelPanel.nakagami"))) {
            PropagationModel.setGlobalPropagationModel(PropagationModel.PROPAGATION_MODEL_NAKAGAMI);
        }

    }

    public void loadAttributes() {

        gaussSigma_.setValue(PropagationModel.getSigma());

        gaussMean_.setValue(PropagationModel.getMean());

        referenceDistnace_.setValue(PropagationModel.getReferenceDistance());

        pr_0_.setValue(PropagationModel.getPr_0());

        pSend_.setValue(PropagationModel.getSendingPower());

        gainSend_.setValue(PropagationModel.getSendingGain());

        gainReceive_.setValue(PropagationModel.getReceivingGain());

        gainReceive_.setValue(PropagationModel.getPassLossFactor());

        gainReceive_.setValue(PropagationModel.getWaveLength());

    }

}
