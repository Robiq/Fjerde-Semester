package irvtool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HostEditPaneOSPF extends EditPane implements ActionListener {
	private Host host;
	private JTextField addr = new JTextField(7);
	private JTextField dest = new JTextField(7);
	private JTextField mess = new JTextField(7);
	private JTextField star = new JTextField(7);
	private JTextField cycl = new JTextField(7);
	private JTextField conn = new JTextField(7);
	private JRadioButton m_delay = new JRadioButton();
	private JRadioButton m_throughput = new JRadioButton();
	private JRadioButton m_reliability = new JRadioButton();
	private JRadioButton m_cost = new JRadioButton();
	boolean useMultipleMetrics;
	boolean useMultipleAreas;
	
	public HostEditPaneOSPF(Host host) {
		super();
		this.host = host;
		this.conn.setEditable(false);
		this.title = "Host: " + this.host.getAddress();
		useMultipleMetrics = ((this.host.scr.extendedOSPFDisplay&Consts.OSPFMULTIPLEMETRICS) != 0);
		useMultipleAreas = ((this.host.scr.extendedOSPFDisplay&Consts.OSPFMULTIPLEAREAS) != 0);
		if(useMultipleMetrics)
			this.setLayout(new GridLayout(12, 2));
		else
			this.setLayout(new GridLayout(7, 2));
		this.add(new JLabel("address ", JLabel.RIGHT));
		this.add(addr);
		this.add(new JLabel("Packet ", JLabel.RIGHT));
		this.add(new JLabel("Properties: ", JLabel.LEFT));
		this.add(new JLabel("destination ", JLabel.RIGHT));
		this.add(dest);
		this.add(new JLabel("message ", JLabel.RIGHT));
		this.add(mess);
		this.add(new JLabel("start ", JLabel.RIGHT));
		this.add(star);
		this.add(new JLabel("cycle ", JLabel.RIGHT));
		this.add(cycl);
		this.add(new JLabel("connection ", JLabel.RIGHT));
		this.add(conn);
		if(useMultipleMetrics) {
			this.add(new JLabel("metric to", JLabel.RIGHT));
			this.add(new JLabel(" be used:", JLabel.LEFT));
			this.add(new JLabel("delay ", JLabel.RIGHT));
			m_delay.addActionListener(this);
			this.add(m_delay);
			this.add(new JLabel("throughput ", JLabel.RIGHT));
			m_throughput.addActionListener(this);
			this.add(m_throughput);
			this.add(new JLabel("reliability ", JLabel.RIGHT));
			m_reliability.addActionListener(this);
			this.add(m_reliability);
			this.add(new JLabel("cost ", JLabel.RIGHT));
			m_cost.addActionListener(this);
			this.add(m_cost);
		}		
		this.init();
	}

	public void init() {
		addr.setText("" + host.getAddress());
		if(useMultipleAreas) {
	    	dest.setText(""+host.getDestArea() + "." + host.getDestination());
	    } else
	    	dest.setText(""+host.getDestination());
		mess.setText(host.getMessage());
		star.setText("" + host.getStart());
		cycl.setText("" + host.getCycle());
		conn.setText("" + host.getCon());
		if(useMultipleMetrics) {
			switch (host.getOSPFMetric()) {
			case Package.M_THROUGHPUT:
				m_delay.setSelected(false);
				m_throughput.setSelected(true);
				m_reliability.setSelected(false);
				m_cost.setSelected(false);
				break;
			case Package.M_RELIABILITY:
				m_delay.setSelected(false);
				m_throughput.setSelected(false);
				m_reliability.setSelected(true);
				m_cost.setSelected(false);
				break;
			case Package.M_COST:
				m_delay.setSelected(false);
				m_throughput.setSelected(false);
				m_reliability.setSelected(false);
				m_cost.setSelected(true);
				break;
			default:
				m_delay.setSelected(true);
				m_throughput.setSelected(false);
				m_reliability.setSelected(false);
				m_cost.setSelected(false);
			}
		}
	}
	
	private int[] parseDestination() throws NumberFormatException {
		  int[] array = new int[2];
		  
		  if(useMultipleAreas) {
		    	String destString = this.dest.getText();
		    	int index = destString.indexOf("."); 
		    	if(index > 0) {
		    		array[0] = (new Integer(destString.substring(0,index)).intValue());
		    		array[1] = (new Integer(destString.substring(index+1,destString.length())).intValue());
		    		if(array[0] >= 0 && array[1] >= 0)
		    			return array;
		    		else
		    			return null;
		    	} else
		    		return null;
	  	} else {
	  		array[0] =(new Integer(dest.getText()).intValue());
	  		array[1] = array[0];
	  		if(array[0] >= 0)
	  			return array;
	  	}
		return null;
	  }

	private boolean validateInput() {
		try {
			if(parseDestination() == null)
	    		return false;
			int addr = new Integer(this.addr.getText()).intValue();
		      if((this.host.scr.getAddressExists(addr) && (this.host.getAddress() != addr)) || addr < 1)
		        return false;
			if(((new Integer(star.getText()).intValue()) < 0)
					|| ((new Integer(cycl.getText()).intValue()) < 1))
				return false;
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public boolean save() {
		if (!validateInput())
			return false;
		host.setAddress(new Integer(addr.getText()).intValue());
		int [] destArray = parseDestination();
	    host.setDestination(destArray[1]);
	    if(useMultipleAreas) {
	    	host.setDestArea(destArray[0]);
	    }
		host.setMessage(mess.getText());
		host.setStart(new Integer(star.getText()).intValue());
		host.setCycle(new Integer(cycl.getText()).intValue());
		if(useMultipleMetrics) {
			if (m_delay.getSelectedObjects() != null) {
				host.setOSPFMetric(Package.M_DELAY);
			} else if (m_throughput.getSelectedObjects() != null) {
				host.setOSPFMetric(Package.M_THROUGHPUT);
			} else if (m_reliability.getSelectedObjects() != null) {
				host.setOSPFMetric(Package.M_RELIABILITY);
			} else if (m_cost.getSelectedObjects() != null) {
				host.setOSPFMetric(Package.M_COST);
			} else {
				host.setOSPFMetric(Package.M_UNUSED);
			}
		}
		return true;
	}

	// only used for multiple metrics
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(m_delay)) {
			m_delay.setSelected(true);
			m_throughput.setSelected(false);
			m_reliability.setSelected(false);
			m_cost.setSelected(false);
		} else if (ae.getSource().equals(m_throughput)) {
			m_delay.setSelected(false);
			m_throughput.setSelected(true);
			m_reliability.setSelected(false);
			m_cost.setSelected(false);
		} else if (ae.getSource().equals(m_reliability)) {
			m_delay.setSelected(false);
			m_throughput.setSelected(false);
			m_reliability.setSelected(true);
			m_cost.setSelected(false);
		} else if (ae.getSource().equals(m_cost)) {
			m_delay.setSelected(false);
			m_throughput.setSelected(false);
			m_reliability.setSelected(false);
			m_cost.setSelected(true);
		}
	}
}