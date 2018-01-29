package net.xmeter.ethereum.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import net.xmeter.ethereum.sampler.GetTxReceipt;

public class GetTxReceiptUI extends AbstractSamplerGui implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JCheckBox useCorrelatedData = new JCheckBox("Use Correlated Data");
	
	private final JLabel txHashLabel = new JLabel("Transaction Hash:");
	private final JTextField txHashText = new JTextField("txHash");
	
	private JPanel mainPanel = new VerticalPanel();
	
	public GetTxReceiptUI() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		
		JPanel conCheckBox = new HorizontalPanel();
		conCheckBox.add(useCorrelatedData);
		useCorrelatedData.setSelected(false);
		useCorrelatedData.setToolTipText("Data from earlier DeployContract sampler!");
		useCorrelatedData.addActionListener(this);
		
		JPanel con = new VerticalPanel();
		con.add(conCheckBox);

		JPanel con1 = new HorizontalPanel(); 
		con1.add(txHashLabel);
		con1.add(txHashText);
		con.add(con1);
		
		con.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Get Transaction Receipt"));
		mainPanel.add(con);
	}
	
	@Override
	public String getLabelResource() {
		return "";
	}
	
	@Override
	public String getStaticLabel() {
		return "Ethereum - GetTxReceipt";
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		GetTxReceipt sampler = (GetTxReceipt) element;
		
		useCorrelatedData.setSelected(sampler.isUseCorrelatedData());  // init UI element with saved value
		txHashText.setText(sampler.getTxHash());
		
		if(useCorrelatedData.isSelected()) {
			txHashText.setEnabled(false);
		} else {
			txHashText.setEnabled(true);
		}
	}

	@Override
	public TestElement createTestElement() {
		GetTxReceipt sampler = new GetTxReceipt();
		this.setupSamplerProperties(sampler);
		return sampler;
	}
	
	@Override
	public void modifyTestElement(TestElement element) {
		GetTxReceipt sampler = (GetTxReceipt) element;
		this.setupSamplerProperties(sampler);
	}
	
	private void setupSamplerProperties(GetTxReceipt sampler) {
		this.configureTestElement(sampler);
		sampler.setUseCorrelatedData(useCorrelatedData.isSelected());
		sampler.setTxHash(txHashText.getText());
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == useCorrelatedData) {
			if(useCorrelatedData.isSelected()) {
				txHashText.setEnabled(false);
			} else {
				txHashText.setEnabled(true);
			}
		}
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
	}

}
