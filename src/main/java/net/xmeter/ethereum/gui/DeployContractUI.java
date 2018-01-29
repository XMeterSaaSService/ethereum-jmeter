package net.xmeter.ethereum.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import net.xmeter.ethereum.sampler.DeployContract;

public class DeployContractUI extends AbstractSamplerGui implements ActionListener {
	private static final long serialVersionUID = 1L;
	//private static final Logger logger = Logger.getLogger(DeployContractUI.class.getCanonicalName());
	
	private final JLabel codeLabel = new JLabel("Bin Code:");
	private final JTextArea codeText = new JTextArea("bin code content", 6, 60);
	private final JScrollPane scroll = new JScrollPane (codeText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
	private final JLabel gasLabel = new JLabel("Gas Amount:");
	private final JTextField gasText = new JTextField("gas");
	
	private final JLabel fromUserLabel = new JLabel("User Account:");
	private final JTextField fromUserText = new JTextField("<UserAccount>");
	
	private JCheckBox useCorrelatedData = new JCheckBox("Use Correlated Data");
	
	private JPanel mainPanel = new VerticalPanel();
	
	public DeployContractUI() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		
		JPanel conCheckBox = new HorizontalPanel();
		conCheckBox.add(useCorrelatedData);
		useCorrelatedData.setSelected(false);
		useCorrelatedData.setToolTipText("Data from earlier CompileContract & EstimateGas sampler!");
		useCorrelatedData.addActionListener(this);
		
		JPanel con = new VerticalPanel();
		con.add(conCheckBox);
		con.add(codeLabel);
		codeText.setLineWrap(true);
		con.add(scroll);
		
		JPanel con1 = new HorizontalPanel(); 
		con1.add(gasLabel);
		con1.add(gasText);
		con1.add(fromUserLabel);
		con1.add(fromUserText);
		con.add(con1);
		
		con.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Deploy Contract"));
		mainPanel.add(con);
	}
	
	

	@Override
	public String getLabelResource() {
		return "";
	}
	
	@Override
	public String getStaticLabel() {
		return "Ethereum - DeployContract";
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		DeployContract sampler = (DeployContract) element;
		
		useCorrelatedData.setSelected(sampler.isUseCorrelatedData());  // init UI element with saved value
		codeText.setText(sampler.getBinStr());
		gasText.setText(sampler.getGas());
		fromUserText.setText(sampler.getFromUser());
		
		if(useCorrelatedData.isSelected()) {
			codeText.setEnabled(false);
			gasText.setEnabled(false);
		} else {
			codeText.setEnabled(true);
			gasText.setEnabled(true);
		}
	}

	@Override
	public TestElement createTestElement() {
		DeployContract sampler = new DeployContract();
		this.setupSamplerProperties(sampler);
		return sampler;
	}
	
	@Override
	public void modifyTestElement(TestElement element) {
		DeployContract sampler = (DeployContract) element;
		this.setupSamplerProperties(sampler);
	}
	
	private void setupSamplerProperties(DeployContract sampler) {
		this.configureTestElement(sampler);
		sampler.setUseCorrelatedData(useCorrelatedData.isSelected());
		sampler.setBinStr(codeText.getText());
		sampler.setGas(gasText.getText());
		sampler.setFromUser(fromUserText.getText());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == useCorrelatedData) {
			if(useCorrelatedData.isSelected()) {
				codeText.setEnabled(false);
				gasText.setEnabled(false);
			} else {
				codeText.setEnabled(true);
				gasText.setEnabled(true);
			}
		}
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
	}

}
