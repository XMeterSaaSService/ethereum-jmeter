package net.xmeter.ethereum.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import net.xmeter.ethereum.sampler.CompileContract;

public class ComplieContractUI extends AbstractSamplerGui implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private final JLabel codeLabel = new JLabel("Code:");
	private final JTextArea codeText = new JTextArea("put source code content here ..\nsupport multi lines", 6, 60);
	private final JScrollPane scroll = new JScrollPane (codeText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
	private JPanel mainPanel = new VerticalPanel();
	
	public ComplieContractUI() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		
		JPanel con = new HorizontalPanel();
		con.add(codeLabel);
		con.add(scroll);
		con.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Compile Solidity"));
		mainPanel.add(con);
	}
	
	
	@Override
	public TestElement createTestElement() {
		CompileContract sampler = new CompileContract();
		this.setupSamplerProperties(sampler);
		return sampler;
	}

	@Override
	public String getLabelResource() {
		return "";
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		CompileContract sampler = (CompileContract) element;
		codeText.setText(sampler.getCode()); // init UI element with saved value
	}
	
	@Override
	public String getStaticLabel() {
		return "Ethereum - CompileContract";
	}

	@Override
	public void modifyTestElement(TestElement element) {
		CompileContract sampler = (CompileContract) element;
		setupSamplerProperties(sampler);
	}
	
	private void setupSamplerProperties(CompileContract sampler) {
		this.configureTestElement(sampler);
		sampler.setCode(codeText.getText());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
	
	/*private void readFromFile(String fileName) {
		if(fileName == null || ("".equals(fileName.trim()))) {
			return;
		}
		
		File file = new File(fileName);

		if(!file.exists()) {
			logger.info("The file " + fileName +" is not existed!");
			return;
		}
		
		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			StringBuffer contents = new StringBuffer();
			while((line = reader.readLine()) != null) {
				contents.append(line);
			}
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		} 
	}*/
	
	@Override
	public void clearGui() {
		super.clearGui();
	}
}
