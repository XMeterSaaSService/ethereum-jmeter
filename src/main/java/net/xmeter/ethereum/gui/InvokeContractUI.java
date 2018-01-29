package net.xmeter.ethereum.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.xmeter.ethereum.javasampler.GetBalance;
import net.xmeter.ethereum.sampler.InvokeContract;

public class InvokeContractUI extends AbstractSamplerGui implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(GetBalance.class.getCanonicalName());
	
	private JCheckBox useCorrelatedData = new JCheckBox("Use Correlated Data");
	private final JLabel contractAddrLabel = new JLabel("Contract address:");
	private final JTextField contractAddrText = new JTextField();
	private final JLabel accountLabel = new JLabel("Account:");
	private final JTextField accountText = new JTextField();
	private final JLabel invokeDataLabel = new JLabel("Invoke Data:");
	private final JTextField invokeDataText = new JTextField();
	
	private final JLabel abiFileLabel = new JLabel("ABI content:");
	private final JTextArea abiFileText = new JTextArea("<abi content>", 6, 60);
	
	private JButton browse1;
	private final String BROWSE1 = "browse1";
	private final String FUNCTION = "FunctionList";
	
	private final JLabel functionsLabel = new JLabel("Functions:");
	private final JComboBox<String> funcList = new JComboBox<>(new String[0]);
	
	private JPanel mainPanel = new VerticalPanel();
	
	private CustomHorizontalPanel parametersContainerPanel = new CustomHorizontalPanel();
	
	private String[] funcNames = new String[0];
	private Hashtable<String, FunctionParameters> paras = new Hashtable<>();
	
	private JTextField[] parametersTypeFields = new JTextField[0];
	private JTextField[] parametersValueFields = new JTextField[0];
	
	public InvokeContractUI() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);

		JPanel conCheckBox = new HorizontalPanel();
		conCheckBox.add(useCorrelatedData);
		useCorrelatedData.setSelected(false);
		useCorrelatedData.setToolTipText("Data from earlier GetTxReceipt sampler!");
		useCorrelatedData.addActionListener(this);
		
		JPanel con = new VerticalPanel();
		con.add(conCheckBox);
		
		JPanel con1 = new HorizontalPanel(); 
		con1.add(accountLabel);
		con1.add(accountText);
		con.add(con1);
		
		JPanel con2 = new HorizontalPanel();
		con2.add(contractAddrLabel);
		con2.add(contractAddrText);
		con.add(con2);
		
		JPanel con3 = new HorizontalPanel();
		con3.add(invokeDataLabel);
		con3.add(invokeDataText);
		con.add(con3);
		
		con.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Invoke Transaction"));
		mainPanel.add(con);
		
		JPanel conHelper = new VerticalPanel();
		fillSmartContractPanel(conHelper);
		conHelper.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Smart Contract"));
		mainPanel.add(conHelper);
		
		mainPanel.add(parametersContainerPanel);
	}
	
	private void fillSmartContractPanel(JPanel funcPanel) {
		JScrollPane scroll = new JScrollPane (abiFileText, 
		   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		abiFileText.setLineWrap(true);
		funcPanel.add(abiFileLabel);
		funcPanel.add(scroll);
		
		browse1 = new JButton(JMeterUtils.getResString("browse"));
		browse1.setActionCommand(BROWSE1);
		browse1.addActionListener(this);
		browse1.setVisible(true);
		funcPanel.add(browse1);
		
		funcPanel.add(functionsLabel);
		funcPanel.add(funcList);
		
		funcList.setActionCommand(FUNCTION);
		funcList.addActionListener(this);
	}
	
	private JPanel createParameterPanel(String parameterName, int index, FunctionParameter functionParameter, String paraVal) {
		JPanel parameterPanel = new VerticalPanel();
		parameterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), functionParameter.getName()));
		
		JPanel panel = new HorizontalPanel();
		
		JLabel label = new JLabel("Parameter type:");
		panel.add(label);
		parametersTypeFields[index] = new JTextField();
		parametersTypeFields[index].setEditable(false);
		parametersTypeFields[index].setText(functionParameter.getType());
		panel.add(parametersTypeFields[index]);
		
		JLabel label1 = new JLabel("Parameter value:");
		panel.add(label1);
		parametersValueFields[index] = new JTextField();
		parametersValueFields[index].setText(paraVal);
		panel.add(parametersValueFields[index]);
		
		parameterPanel.add(panel);
		return parameterPanel;
	}

	private void setupUI(InvokeContract sampler) {
		useCorrelatedData.setSelected(sampler.isUseCorrelatedData());  // init UI element with saved value
		accountText.setText(sampler.getAccount());
		contractAddrText.setText(sampler.getContractAddress());
		invokeDataText.setText(sampler.getInvokeData());
		if(useCorrelatedData.isSelected()) {
			contractAddrText.setEnabled(false);
		} else {
			contractAddrText.setEnabled(true);
		}
		abiFileText.setText(sampler.getAbiFile());
		
		String abiContent = abiFileText.getText();
		if(abiContent != null && (!abiContent.trim().equals(""))) {
			refreshFunctionList(abiContent);
			
			String funcName = sampler.getFunctionName();
			if((funcName != null) && this.paras.containsKey(funcName)) {
				this.funcList.setSelectedItem(funcName);
				int index = 0;
				
				parametersContainerPanel.removeSubs();
				parametersContainerPanel.revalidate();
				
				FunctionParameters tempParas = this.paras.get(funcName);
				int size = tempParas.getParameters().size();
				parametersTypeFields = new JTextField[size];
				parametersValueFields = new JTextField[size];
				
				String[] paraValArr = new String[size];
				String paraVals = sampler.getFunctionParaVals();
				String[] tmp = paraVals.split(",");
				if(paraValArr.length >= tmp.length) {
					System.arraycopy(tmp, 0, paraValArr, 0, tmp.length);					
				}
				
				for(FunctionParameter para : this.paras.get(funcName).getParameters()) {
					JPanel panel = createParameterPanel(funcName, index, para, paraValArr[index]);
					index++;
					parametersContainerPanel.add(panel);
				}
			} else {
				logger.info(MessageFormat.format("Cannot find the function name {0}, skip setting the value of control.", funcName));
			}
		} else {
			this.abiFileText.setText("");
			this.funcList.removeAllItems();
			parametersContainerPanel.removeSubs();
			parametersContainerPanel.revalidate();
			this.paras.clear();
			parametersTypeFields = new JTextField[0];
			parametersValueFields = new JTextField[0];
		}
	}
	
	@Override
	public String getLabelResource() {
		return "";
	}
	
	@Override
	public String getStaticLabel() {
		return "Ethereum - InvokeContract";
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		InvokeContract sampler = (InvokeContract) element;
		setupUI(sampler);
	}
	
	@Override
	public TestElement createTestElement() {
		InvokeContract sampler = new InvokeContract();
		this.setupSamplerProperties(sampler);
		return sampler;
	}

	@Override
	public void modifyTestElement(TestElement element) {
		InvokeContract sampler = (InvokeContract) element;
		this.setupSamplerProperties(sampler);
	}
	
	private void setupSamplerProperties(InvokeContract sampler) {
		this.configureTestElement(sampler);
		sampler.setUseCorrelatedData(useCorrelatedData.isSelected());
		sampler.setAccount(accountText.getText());
		sampler.setContractAddress(contractAddrText.getText());
		sampler.setInvokeData(invokeDataText.getText());
		
		String abiStr = abiFileText.getText();
		sampler.setAbiFile(abiStr);
		String funcName = (String)this.funcList.getSelectedItem();
		sampler.setFunctionName(funcName);
		
		if(funcName != null) {
			FunctionParameters funcParas = paras.get(funcName);
			List<FunctionParameter> funcParaList = funcParas.getParameters();
			
			int index = 0;
			String types = "";
			String vals = "";
			for(FunctionParameter functionParameter : funcParaList) {
				vals += parametersValueFields[index].getText() + ",";
				index++;
				types += functionParameter.getType() + ",";
			}
			
			if(index > 0) {
				types = types.substring(0, types.length() - 1);
				vals = vals.substring(0, vals.length() - 1);
			}
			
			sampler.setFunctionParaTypes(types);
			sampler.setFunctionParaVals(vals);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == useCorrelatedData) {
			if(useCorrelatedData.isSelected()) {
				contractAddrText.setEnabled(false);
			} else {
				contractAddrText.setEnabled(true);
			}
		}
		
		String action = e.getActionCommand();
		if(BROWSE1.equals(action)) {
			String path = browseAndGetFilePath();
			//abiFileText.setText(FileUtil.handleUIFileName(FileServer.getFileServer().getBaseDir(), path));

			if(path != null || (!"".equals(path))) {
				readFromFile(path);
				refreshFunctionList(abiFileText.getText());
			}
		} else if(FUNCTION.equals(action)) {
			parametersContainerPanel.removeSubs();
			
			String selectedFunction = (String) this.funcList.getSelectedItem();
			if(selectedFunction == null) {
				return;
			}
			FunctionParameters tempParas = paras.get(selectedFunction);
			parametersTypeFields = new JTextField[tempParas.getParameters().size()];
			parametersValueFields = new JTextField[tempParas.getParameters().size()];
			
			int index = 0;
			for(FunctionParameter para : tempParas.getParameters()) {
				JPanel panel = createParameterPanel(selectedFunction, index++, para, "");
				parametersContainerPanel.add(panel);
			}
			parametersContainerPanel.revalidate();
			parametersContainerPanel.repaint();
		}
	}
	
	private String browseAndGetFilePath() {
		String path = "";
		JFileChooser chooser = FileDialoger.promptToOpenFile();
		if (chooser != null) {
			File file = chooser.getSelectedFile();
			if (file != null) {
				path = file.getPath();
			}
		}
		return path;
	}

	private void readFromFile(String fileName) {
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
			String fileContent = contents.toString();
			abiFileText.setText(fileContent);
			abiFileText.invalidate();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} 
	}
	
	private void refreshFunctionList(String abiContent) {
		JSONArray jsonArr = JSONArray.fromObject(abiContent);
		int size = jsonArr.size();
		paras.clear();
		
		this.funcNames = new String[size];
		funcList.removeAllItems();
		
		for(int i = 0; i < size; i++) {
			JSONObject json = jsonArr.getJSONObject(i);
			String type = json.getString("type");
			if(type != null && "constructor".equals(type)) {
				continue;
			}
			String funcName = json.getString("name");
			this.funcNames[i] = funcName;
			
			JSONArray inputs = json.getJSONArray("inputs");
			FunctionParameters params = new FunctionParameters();
			for(int j = 0; j < inputs.size(); j++) {
				FunctionParameter parameter = new FunctionParameter();
				parameter.setName(inputs.getJSONObject(j).getString("name"));
				parameter.setType(inputs.getJSONObject(j).getString("type"));
				params.getParameters().add(parameter);
			}
			this.paras.put(funcName, params);
			funcList.addItem(funcName);
		}
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		this.abiFileText.setText("");
		funcList.removeAllItems();
		parametersContainerPanel.removeSubs();
		parametersContainerPanel.revalidate();
		parametersContainerPanel.repaint();
	}
}
