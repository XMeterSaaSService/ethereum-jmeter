package net.xmeter.ethereum.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class CustomHorizontalPanel extends JPanel {
	private static final long serialVersionUID = 240L;

	private final Box subPanel = Box.createVerticalBox();

	private final float horizontalAlign;

	private final int vgap;

	public CustomHorizontalPanel() {
		this(5, LEFT_ALIGNMENT);
	}

	public CustomHorizontalPanel(Color bkg) {
		this();
		subPanel.setBackground(bkg);
		this.setBackground(bkg);
	}

	public CustomHorizontalPanel(int vgap, float horizontalAlign) {
		super(new BorderLayout());
		add(subPanel, BorderLayout.NORTH);
		this.vgap = vgap;
		this.horizontalAlign = horizontalAlign;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component add(Component c) {
		// This won't work right if we remove components. But we don't, so I'm
		// not going to worry about it right now.
		if (vgap > 0 && subPanel.getComponentCount() > 0) {
			subPanel.add(Box.createVerticalStrut(vgap));
		}

		if (c instanceof JComponent) {
			((JComponent) c).setAlignmentX(horizontalAlign);
		}

		return subPanel.add(c);
	}
	
	@Override
	public void remove(Component comp) {
		subPanel.remove(comp);
	}
	
	public void removeSubs() {
		subPanel.removeAll();
	}
}
