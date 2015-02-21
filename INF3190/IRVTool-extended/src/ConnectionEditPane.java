package irvtool;


import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ConnectionEditPane extends EditPane{
  private Connection con;
  private static final String[] columns = {"from", "to"};
  private ArrayList c;
  private JTable table;
  private JTextField addr = new JTextField(7);
  private JTextField from = new JTextField(7);
  private JTextField to = new JTextField(7);
  private JTextField cost = new JTextField(7);
  private JButton addentry = null;

  public ConnectionEditPane(Connection con){
    super();
    this.con = con;
    this.title = "Connection: "+this.con.getAddress();
    this.c = con.getConstraints();
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.init();
  }

  public void init(){
    this.table = new JTable(new Model());
    table.setPreferredScrollableViewportSize(new Dimension(100, 50));
    JScrollPane scrollPane = new JScrollPane(table);
    this.setValues();
    addentry = new JButton("add");
    JButton delete = new JButton("delete");
    JButton clear = new JButton("clear");
    addentry.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        if(!ConnectionEditPane.this.validateInput())
          return;
        ConnectionEditPane.this.con.add(new Integer(from.getText()).intValue(), new Integer(to.getText()).intValue());
        ConnectionEditPane.this.from.setText("");
        ConnectionEditPane.this.to.setText("");
        ConnectionEditPane.this.setValues();
      }
    });
    delete.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        TableModel model = ConnectionEditPane.this.table.getModel();
        int row = ConnectionEditPane.this.table.getSelectedRow();
        ConnectionEditPane.this.con.remove(((Integer)model.getValueAt(row, 0)).intValue(), ((Integer)model.getValueAt(row, 1)).intValue());
        ConnectionEditPane.this.setValues();
      }
    });
    clear.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        ConnectionEditPane.this.c.clear();
        ConnectionEditPane.this.setValues();
      }
    });

    JPanel pane = new JPanel();
    pane.add(new JLabel("addr ", JLabel.RIGHT));
    pane.add(addr);
    this.add(pane);
    pane = new JPanel();
    pane.add(new JLabel("cost ", JLabel.RIGHT));
    pane.add(cost);
    this.add(pane);
    pane = new JPanel();
    pane.add(new JLabel("Failure times:", JLabel.CENTER));
    this.add(pane);
    
    this.add(scrollPane);
    pane = new JPanel();
    pane.add(from);
    pane.add(to);
    this.add(pane);
    pane = new JPanel();
    pane.add(addentry);
    pane.add(delete);
    pane.add(clear);
    this.add(pane);
    addr.setText(""+this.con.getAddress());
    cost.setText(""+this.con.getCost());
  }

  public boolean save(){
    if(!validateAddressAndCost())
      return false;
    if(!from.getText().equals("") && !to.getText().equals("")){
      if(JOptionPane.showConfirmDialog(this, "Do you want to add the link-failure?", "IRV-Tool", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        this.addentry.doClick();
    }
    this.con.setAddress(new Integer(addr.getText()).intValue());
    this.con.setCost(new Integer(cost.getText()).intValue());
    return true;
  }

  private boolean validateAddressAndCost(){
    try{
      int addr = new Integer(this.addr.getText()).intValue();
      if((this.con.scr.getAddressExists(addr) && (this.con.getAddress() != addr)) || addr < 1)
        return false;
      if((new Integer(this.cost.getText()).intValue()) < 0) {
    	  return false;
      }
    } catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }

  private boolean validateInput(){
    try{
    	if((new Integer(from.getText())) < 0)
      	  return false;
        if((new Integer(to.getText())) < 0)
      	  return false;
    } catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }

  private void setValues(){
    if(this.con.getConstraints() != null)
      this.c = this.con.getConstraints();
    else{
      this.c = new ArrayList(0);
    }
    this.table.revalidate();
    this.table.repaint();
  }

  class Model extends AbstractTableModel{
    public Model(){
    }

    public int getColumnCount(){
      return columns.length;
    }

    public int getRowCount() {
      return c.size();
    }

    public String getColumnName(int col) {
      return columns[col];
    }

    public Object getValueAt(int row, int col) {
      if(col == 0)
        return new Integer(((Point)c.get(row)).x);
      else
        return new Integer(((Point)c.get(row)).y);
    }

    public Class getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }
  }

}