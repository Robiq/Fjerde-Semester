package irvtool;


import javax.swing.*;

import java.awt.*;

public class HostEditPane extends EditPane{
	
  private Host host;
  private JTextField addr = new JTextField(7);
  private JTextField dest = new JTextField(7);
  private JTextField mess = new JTextField(7);
  private JTextField star = new JTextField(7);
  private JTextField cycl = new JTextField(7);
  private JTextField conn = new JTextField(7);

  public HostEditPane(Host host){
    super();
    this.host = host;
    this.conn.setEditable(false);
    this.title = "Host: "+this.host.getAddress();
    this.setLayout(new GridLayout(7,2));
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
    this.init();
  }

  public void init(){
    addr.setText(""+host.getAddress());
    dest.setText(""+host.getDestination());
    mess.setText(""+host.getMessage());
    star.setText(""+host.getStart());
    cycl.setText(""+host.getCycle());
    conn.setText(""+host.getCon());
  }

  private boolean validateInput(){
    try{    	
      int addr = new Integer(this.addr.getText()).intValue();
      if((this.host.scr.getAddressExists(addr) && (this.host.getAddress() != addr)) || addr < 1)
        return false;
      if(((new Integer(dest.getText()).intValue()) < 0)
				|| ((new Integer(star.getText()).intValue()) < 0)
				|| ((new Integer(cycl.getText()).intValue()) < 1))
			return false;
    } catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }


  public boolean save(){
    if(!validateInput())
      return false;
    host.setAddress(new Integer(addr.getText()).intValue());
    host.setDestination(new Integer(dest.getText()).intValue());
    host.setMessage(mess.getText());
    host.setStart(new Integer(star.getText()).intValue());
    host.setCycle(new Integer(cycl.getText()).intValue());
    return true;
  }
}
