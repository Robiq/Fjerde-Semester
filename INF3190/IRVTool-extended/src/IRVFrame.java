/*
IRV-Tool
An Internet Routing Visualization Tool.

Copyright (C) 

This program is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software 
Foundation; either version 2 of the License, or (at your option) any later 
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this 
program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, 
Suite 330, Boston, MA 02111-1307 USA

author: Christian Sternagel
mail:   csac3692@uibk.ac.at

*/
package irvtool;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.regex.*;
import java.net.*;

public class IRVFrame extends JFrame implements ActionListener{
  protected Screen screen = new Screen(this, 0);
  protected IRVController controller = new IRVController(screen, this);
  private JMenu menu, menu2, opMenu, fileMenu, lookAndFeelMenu = null;
  private JScrollPane scroller;
  protected RemoteMenu remoteMenu;
  //protected boolean saved = false;
  private String filename = null;
  private JFileChooser fch = new JFileChooser();
  protected JMenuBar menuBar;
  private PaintMenu paintMenu;
  protected Terminal term;
  private javax.swing.filechooser.FileFilter irv = new IRVFilter();
  private javax.swing.filechooser.FileFilter jpg = new JPGFilter();
  private String title;


  public IRVFrame(String title, boolean decoration){
    /* super class constructor (JFRame) */
    super(title+" - unnamed");
    this.title = title;
    /* set icon */
    URL image = this.getClass().getResource("icons/programmIcon01.png");
    this.setIconImage(new ImageIcon(image).getImage());
    /* the programm exits when this window is closed */
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    /* set menubar */
    this.setJMenuBar(this.initJMenuBar(decoration));
    this.addKeyListener(controller);

    /* get the ContentPane of this IRVFrame */
    Container cp = this.getContentPane();
    cp.setLayout(new BorderLayout());
    paintMenu = new PaintMenu(controller);

    cp.add(paintMenu, BorderLayout.WEST);

    screen.addMouseListener(controller);
    screen.addMouseMotionListener(controller);
    screen.addKeyListener(controller);
    scroller = new JScrollPane(screen);
    cp.add(scroller, BorderLayout.CENTER);
    remoteMenu = new RemoteMenu(controller);
    controller.setClock(remoteMenu.clock);
    JPanel dummy = new JPanel();
    dummy.setLayout(new BoxLayout(dummy, BoxLayout.Y_AXIS));
    dummy.add(remoteMenu);

    term = new Terminal();

    fch.addChoosableFileFilter(irv);
    fch.addChoosableFileFilter(jpg);

    dummy.add(term);
    cp.add(dummy, BorderLayout.SOUTH);

    /* center IRVFrame on screen */
    /* this.setLocationRelativeTo(null); */
    /* set size and make visible */
    //this.pack();
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    if(System.getProperty("file.separator").equals("/"))
      this.setSize(dim); // MAXIMIZATION not supported for LINUX with j2sdk1.4.2
    else{
      this.setSize(800, 600);
      this.setExtendedState(this.getExtendedState() | MAXIMIZED_BOTH);
    }
    this.setLocation((dim.width-this.getSize().width)/2, (dim.height-this.getSize().height)/2);
    this.setVisible(true);
    term.disable();
  }

  public void disableMenu(){
    fileMenu.setEnabled(false);
    opMenu.setEnabled(false);
    if(lookAndFeelMenu != null)
      lookAndFeelMenu.setEnabled(false);
  }

  public void enableMenu(){
    fileMenu.setEnabled(true);
    opMenu.setEnabled(true);
    if(lookAndFeelMenu != null)
      lookAndFeelMenu.setEnabled(true);
  }

  private JMenuBar initJMenuBar(boolean dec){
    JMenuItem mItem;
    /* create MenuBar */
    menuBar = new JMenuBar();
    /* file menu START */
    fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    fileMenu.setToolTipText("This is the file menu");
    /* new */
    URL image = this.getClass().getResource("icons/new01.png");
    mItem = new JMenuItem("New", new ImageIcon(image));
    mItem.setMnemonic(KeyEvent.VK_N);
    mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
    mItem.setToolTipText("Creates a new (empty) network");
    mItem.addActionListener(controller);
    fileMenu.add(mItem);
    /* open */
    image = this.getClass().getResource("icons/open01.png");
    mItem = new JMenuItem("Open", new ImageIcon(image));
    mItem.setMnemonic(KeyEvent.VK_O);
    mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
    mItem.setToolTipText("Opens a network from a file");
    mItem.addActionListener(controller);
    fileMenu.add(mItem);
    /* close */
    image = this.getClass().getResource("icons/empty.png");
    mItem = new JMenuItem("Close" , new ImageIcon(image));
    mItem.setToolTipText("Closes the current network");
    mItem.addActionListener(controller);
    fileMenu.add(mItem);
    /* separator */
    fileMenu.addSeparator();
    /* save */
    image = this.getClass().getResource("icons/save01.png");
    mItem = new JMenuItem("Save", new ImageIcon(image));
    mItem.setMnemonic(KeyEvent.VK_S);
    mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    mItem.setToolTipText("Saves the network to the current file");
    mItem.addActionListener(controller);
    fileMenu.add(mItem);
    /* save as ...*/
    image = this.getClass().getResource("icons/empty.png");
    mItem = new JMenuItem("Save As ..." , new ImageIcon(image));
    mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0/* no modofiers */));
    mItem.setToolTipText("Saves the network with a new filename");
    mItem.addActionListener(controller);
    fileMenu.add(mItem);
    /* separator */
    fileMenu.addSeparator();
    /* save as ...*/
    image = this.getClass().getResource("icons/quit01.png");
    mItem = new JMenuItem("Quit", new ImageIcon(image));
    mItem.setMnemonic(KeyEvent.VK_Q);
    mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
    mItem.setToolTipText("Quits the application");
    mItem.addActionListener(controller);
    fileMenu.add(mItem);
    menuBar.add(fileMenu);
    /* file menu END */
    /* options menu START */
    opMenu = new JMenu("Options");
    opMenu.setMnemonic(KeyEvent.VK_O);
    opMenu.setToolTipText("Change between different routing protocolls");
    /* RIP */
    ButtonGroup protocols = new ButtonGroup();
    mItem = new JCheckBoxMenuItem("RIP", true);
    mItem.addActionListener(this);
    protocols.add(mItem);
    opMenu.add(mItem);
    menu = new JMenu("Settings ...");
    mItem = new JCheckBoxMenuItem("  Triggered updates", false);
    mItem.setMnemonic(KeyEvent.VK_T);
    mItem.setToolTipText("Every router broadcasts an update when its status changes");
    mItem.addActionListener(this);
    menu.add(mItem);
    //ButtonGroup bg = new ButtonGroup();
    mItem = new JCheckBoxMenuItem("  Split Horizon", false);
    mItem.setMnemonic(KeyEvent.VK_S);
    mItem.setToolTipText("Use split horizon");
    mItem.addActionListener(controller);
    //bg.add(mItem);
    menu.add(mItem);
    mItem = new JCheckBoxMenuItem("  Split Horizon with poisoned reverse", false);
    mItem.setMnemonic(KeyEvent.VK_P);
    mItem.setToolTipText("The fastest version of RIP");
    mItem.addActionListener(this);
    //bg.add(mItem);
    menu.add(mItem);
    mItem = new JMenuItem("  RIP Variables");
    mItem.setMnemonic(KeyEvent.VK_V);
    mItem.setToolTipText("Set global variables");
    mItem.addActionListener(this);
    menu.add(mItem);
    opMenu.add(menu);
    /* separator */
    opMenu.addSeparator();
    /* OSPF */
    mItem = new JCheckBoxMenuItem("OSPF");
    mItem.addActionListener(this);
    //mItem.setEnabled(false);
    opMenu.add(mItem);
    protocols.add(mItem);
    menu2 = new JMenu("Settings ...");
    mItem = new JCheckBoxMenuItem("  Multiple Path", false);
    mItem.setMnemonic(KeyEvent.VK_M);
    mItem.setToolTipText("If there are multiple equally long paths, the load is split between them");
    mItem.addActionListener(this);
    menu2.add(mItem);
    mItem = new JCheckBoxMenuItem("  Multiple Metrics", false);
    mItem.setMnemonic(KeyEvent.VK_U);
    mItem.setToolTipText("Use Multiple Metrics (delay, throughput, reliability and cost)");
    mItem.addActionListener(this);
    menu2.add(mItem);
    mItem = new JCheckBoxMenuItem("  Multiple Areas", false);
    mItem.setMnemonic(KeyEvent.VK_A);
    mItem.setToolTipText("Use Multiple Areas");
    mItem.addActionListener(this);
    menu2.add(mItem);
    mItem = new JMenuItem("  OSPF Variables");
    mItem.setMnemonic(KeyEvent.VK_V);
    mItem.setToolTipText("Set global variables");
    mItem.addActionListener(this);
    menu2.add(mItem);
    menu2.setEnabled(false);
    
    opMenu.add(menu2);

    menuBar.add(opMenu);
    /* options menu END */

    if(!dec){
      lookAndFeelMenu = new JMenu("Look & Feel");
      lookAndFeelMenu.setMnemonic(KeyEvent.VK_L);
      menuBar.add(lookAndFeelMenu);
      UIManager.LookAndFeelInfo info[] = UIManager.getInstalledLookAndFeels();
      ButtonGroup lookAndFeelGroup = new ButtonGroup();
      JCheckBoxMenuItem[] lookAndFeelMenuEntry = new JCheckBoxMenuItem[info.length];
      for (int i = 0; i < info.length; i++) {
        lookAndFeelMenuEntry[i] = new JCheckBoxMenuItem(info[i].getName());
        lookAndFeelMenuEntry[i].setActionCommand(info[i].getClassName());
        lookAndFeelMenuEntry[i].addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent ae){
            JMenuItem item = (JMenuItem) ae.getSource();
            try{
              UIManager.setLookAndFeel(item.getActionCommand());
              SwingUtilities.updateComponentTreeUI(IRVFrame.this);
              //IRVFrame.this.pack();
            } catch (Exception exception){
              exception.printStackTrace();
            }
          }
        });
        lookAndFeelMenu.add(lookAndFeelMenuEntry[i]);
        lookAndFeelGroup.add(lookAndFeelMenuEntry[i]);
        if (UIManager.getLookAndFeel().getName().equals(info[i].getName()))
          lookAndFeelMenuEntry[i].setSelected(true);
      }
    }
    
    JMenu license = new JMenu("License Information");
    license.setMnemonic(KeyEvent.VK_I);
    menuBar.add(license);
    mItem = new JMenuItem("Warranty Information");
    mItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        try{
          BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("license/gpl.txt")));
          StringBuffer file = new StringBuffer();
          String line = null;
          while((line = in.readLine()) != null)
            file.append(line).append("<NewLine>");
          in.close();
          Pattern p = Pattern.compile("(NO WARRANTY.*)END OF TERMS AND CONDITIONS");
          Matcher m = p.matcher(file);
          m.find();
          new InfoPane(IRVFrame.this, "Warranty Information", file.toString().substring(m.start(1), m.end(1)).replaceAll("<NewLine>", "\n")).show();
        } catch(Exception e){
          e.printStackTrace();
          System.err.println(e.toString());
        }
      }
    });
    license.add(mItem);
    mItem = new JMenuItem("Conditions for Redistribution");
    mItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        try{
          BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("license/gpl.txt")));
          StringBuffer file = new StringBuffer();
          String line = null;
          while((line = in.readLine()) != null)
            file.append(line).append("<NewLine>");
          in.close();
          Pattern p = Pattern.compile("(TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION.*)<NewLine>\\s*NO WARRANTY<NewLine>");
          Matcher m = p.matcher(file);
          m.find();
          new InfoPane(IRVFrame.this, "Conditions for Redistribution", file.toString().substring(m.start(1), m.end(1)).replaceAll("<NewLine>", "\n")).show();
        } catch(Exception e){
          e.printStackTrace();
          System.err.println(e.toString());
        }
      }
    });
    license.add(mItem);

    return menuBar;
  }
  
  protected void initRouters(){
    ArrayList help = this.controller.screen.getObjects();
    
    for(int i = 0; i < help.size(); ++i){
      Object tmp = help.get(i);
      if(tmp instanceof Router){
        Router r = (Router)tmp;
        int mode = 0;
        if(this.usingMultipleMetrics()) // if using MM, set mode of every Router to MM
        	mode |= Consts.OSPFMULTIPLEMETRICS;
        if(this.usingMultipleAreas())
        	mode |= Consts.OSPFMULTIPLEAREAS;
        
        r.setOspfMode(mode);
        r.setRoutingTable(controller.getRoutingTable(r));
        r.setStatus(this.controller.getStatus());
        
      }
    }
  }

  public void actionPerformed(ActionEvent ae){
    String cmd = ae.getActionCommand();
    //System.out.println(cmd);
    if(cmd.equals("RIP")){
      this.opMenu.getItem(1).setEnabled(true);
      this.menu2.getItem(0).setSelected(false);
      this.menu2.getItem(1).setSelected(false);
      this.opMenu.getItem(4).setEnabled(false);
      checkOSPFDisplay(); // sets the ospf display mode
      screen.repaint();
      this.initRouters();
    }
    else if(cmd.equals("OSPF")){
      this.opMenu.getItem(4).setEnabled(true);
      this.menu.getItem(0).setSelected(false);
      this.menu.getItem(1).setSelected(false);
      this.menu.getItem(2).setSelected(false);
      this.opMenu.getItem(1).setEnabled(false);
      checkOSPFDisplay(); // sets the ospf display mode
      screen.repaint();
      this.initRouters();
    }
    else if(cmd.equals("  Multiple Metrics")) {
    	System.out.println("test");
    	checkOSPFDisplay(); // sets the ospf display mode
    	screen.repaint();
    	this.initRouters();
    }
    else if(cmd.equals("  Multiple Areas")) {
    	 checkOSPFDisplay();
    	 screen.repaint();
    	 this.initRouters();
    }
    
    else if(cmd.equals("  OSPF Variables")){
      new IRVDialog(this, new OSPFVariableEditPane());
    }
    else if(cmd.equals("  RIP Variables")){
      new IRVDialog(this, new RIPVariableEditPane());
    }
  }

  public boolean rip(){
    //System.out.println("rip()");
    return this.opMenu.getItem(0).isSelected();
  }

  public boolean triggeredUpdates(){
    //System.out.println("tu()");
    return this.menu.getItem(0).isSelected();
  }

  public boolean splitHorizon(){
    //System.out.println("sh()");
    return this.menu.getItem(1).isSelected();
  }

  public boolean splitHorizonWithPoisonedReverse(){
    //System.out.println("shwpr()");
    return this.menu.getItem(2).isSelected();
  }

  public boolean ospf(){
    return !this.opMenu.getItem(1).isEnabled();
  }

  public boolean multiplePaths(){
    //System.out,println("multi()");
    return this.menu2.getItem(0).isSelected();
  }
  
  // returns true if MenuItem "Multiple Metrics" is checked, false otherwise
  public boolean usingMultipleMetrics() {
	  return this.menu2.getItem(1).isSelected();
  }
  
  public boolean usingMultipleAreas() {
	  return this.menu2.getItem(2).isSelected();
  }
  
  // if we are using Multiple Metrics, set the screen to display extended information
  public void checkOSPFDisplay() {
	  screen.extendedOSPFDisplay = 0;
	  if(usingMultipleMetrics())
		  screen.extendedOSPFDisplay |= Consts.OSPFMULTIPLEMETRICS;
	  if(usingMultipleAreas())
		  screen.extendedOSPFDisplay |= Consts.OSPFMULTIPLEAREAS;
  }

  /*
   Cursor cu = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("icons/irvtool1.png").getImage(), new Point(0,0), "myCursor");
   container.setCursor(cu);
  */

  protected void newNetwork(){
    save();
    this.screen.reset();
    Address.reset();
    this.remoteMenu.button3.doClick();
    this.controller.setChanged(true);
    this.filename = null;
    this.paintMenu.unselectAll();
    this.setTitle(this.title+" - unnamed");
  }

  protected void save(){
    if(this.controller.getChanged()){
      int status;
      URL image = this.getClass().getResource("icons/warning.png");
      status = JOptionPane.showConfirmDialog(this, "Do you want to save the changes?", "WARNING", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, new ImageIcon(image));
      if(status == JOptionPane.YES_OPTION){
        if(this.filename != null)
          this.saveNetwork();
        else
          this.saveNetworkAs();
      }
      this.controller.setChanged(false);
    }
  }

  protected void openNetwork(){
    save();
    this.fch.removeChoosableFileFilter(jpg);
    fch.setFileFilter(irv);
    int status = fch.showOpenDialog(this);
    if(status == JFileChooser.APPROVE_OPTION){
      this.filename = fch.getSelectedFile().toString();
      try{
        ObjectInputStream file = new ObjectInputStream(new FileInputStream(this.filename));
        
        if(file.readBoolean()){ // RIP
          this.opMenu.getItem(0).setSelected(true);
          this.opMenu.getItem(1).setEnabled(true);
        }
        else
          this.opMenu.getItem(0).setSelected(false);
        if(file.readBoolean()) // TU
          this.menu.getItem(0).setSelected(true);
        else
          this.menu.getItem(0).setSelected(false);
        if(file.readBoolean()) // SH
          this.menu.getItem(1).setSelected(true);
        else
          this.menu.getItem(1).setSelected(false);
        if(file.readBoolean()) // SHPR
          this.menu.getItem(2).setSelected(true);
        else
          this.menu.getItem(2).setSelected(false);
        if(file.readBoolean()){ // OSPF
          this.opMenu.getItem(3).setSelected(true);
          this.opMenu.getItem(1).setEnabled(false);
          this.opMenu.getItem(4).setEnabled(true);
        }
        else
          this.opMenu.getItem(3).setSelected(false);
        if(file.readBoolean()) // MultiPath
          this.menu2.getItem(0).setSelected(true);
        else
          this.menu2.getItem(0).setSelected(false);
        
        this.menu2.getItem(1).setSelected(file.readBoolean()); // Multiple Metris
        this.menu2.getItem(2).setSelected(file.readBoolean()); // Multiple Areas
    
        //this.controller.sim = (Simulator)file.readObject(); // load Simulator+Timeline
        this.controller.screen.reset();
        this.controller.screen.objects = ((ArrayList)file.readObject()); // load objects
        this.controller.screen.setObjects();
        this.controller.screen.unmarkAll();
        file.close();
        Address.setNextAddress(this.controller.screen.getMaxAddress()+1);
        this.paintMenu.unselectAll();
        this.controller.setChanged(false);
        this.setTitle(this.title+" - "+this.filename);
        checkOSPFDisplay();
   	 	screen.repaint();
   	 	this.initRouters();
   	 
      } catch(Exception e){
    	term.write("ERROR: File " + filename + " is not compatible with this version of the tool.");
        this.term.write(e.toString());
        e.printStackTrace();
        return;
      }
      return;
    }
  }

  protected void closeNetwork(){
    this.newNetwork();
    this.controller.setChanged(false);
    this.filename = null;
    this.paintMenu.unselectAll();
    this.setTitle(this.title);
  }

  protected void saveNetwork(){
    if(this.filename == null){
      this.saveNetworkAs();
      return;
    }
    if(this.filename == null)
      return;
    try{
      this.controller.sim = null;
      ObjectOutputStream file = new ObjectOutputStream(new FileOutputStream(this.filename, false /*do not append*/));
      //file.writeObject(this.controller.sim); // save Simulator+Timeline
      for(int i = 0; i < this.controller.screen.objects.size(); ++i){
        Object o = this.controller.screen.objects.get(i);
        if(o instanceof Router){
          if(((Router)o).rtable == null){
            ((Router)o).setRoutingTable(this.controller.getRoutingTable((Router)o));
            
            //((Router)o).rtable.init();
          }
        }
      }
      this.controller.sim = null;
      file.writeBoolean(this.rip()); // RIP
      file.writeBoolean(this.triggeredUpdates()); // Triggered-Updates
      file.writeBoolean(this.splitHorizon()); // Split-Horizon;
      file.writeBoolean(this.splitHorizonWithPoisonedReverse()); // SPlit-Horizon with Poisoned-Reverse
      file.writeBoolean(this.ospf()); // OSPF
      file.writeBoolean(this.multiplePaths()); // MULTI
      file.writeBoolean(this.usingMultipleMetrics()); // Multiple Metrics
      file.writeBoolean(this.usingMultipleAreas());
      file.writeObject(this.controller.screen.objects); // save objects
      //this.controller.screen.reset();
      file.close();
      this.setTitle(this.title+" - "+this.filename);
      this.controller.setChanged(false);
    } catch(IOException ioe){
      this.term.write(ioe.toString());
       ioe.printStackTrace();
      this.controller.setChanged(true);
      return;
    }
    this.controller.setChanged(true);
  }

  protected void saveNetworkAs(){
    fch.removeChoosableFileFilter(jpg);
    fch.addChoosableFileFilter(jpg);
    fch.setFileFilter(irv);
    int status = fch.showSaveDialog(this);
    javax.swing.filechooser.FileFilter fil = fch.getFileFilter();
    if(status == JFileChooser.APPROVE_OPTION){
      this.filename = fch.getSelectedFile().toString();
      if(this.filename == null)
        return;
      if(fil.equals(jpg)){
        if(this.filename.indexOf('.') == -1)
          this.filename += ".jpg";
        try{
          BufferedImage img = new BufferedImage(this.scroller.getViewport().getSize().width, this.scroller.getViewport().getSize().height, BufferedImage.TYPE_INT_RGB);
          Graphics g = img.getGraphics();
          this.screen.paintComponent(g);
          this.screen.paintObjects(g);
          ImageIO.write(img, "jpeg", new File(filename));
        } catch(IOException ioe){
          this.term.write("error while writing image");
        }
        return;
      }
      if(this.filename.indexOf('.') == -1)
        this.filename += ".irv";
      try{
        this.controller.sim = null;
        ObjectOutputStream file = new ObjectOutputStream(new FileOutputStream(this.filename, false /*do not append*/));
        //file.writeObject(this.controller.sim); // save Simulator+Timeline
        for(int i = 0; i < this.controller.screen.objects.size(); ++i){
          Object o = this.controller.screen.objects.get(i);
          if(o instanceof Router){
            if(((Router)o).rtable == null){
              ((Router)o).setRoutingTable(this.controller.getRoutingTable((Router)o));
              //((Router)o).rtable.init();
            }
          }
        }
        this.controller.sim = null;
        file.writeBoolean(this.rip()); // RIP
        file.writeBoolean(this.triggeredUpdates()); // Triggered-Updates
        file.writeBoolean(this.splitHorizon()); // Split-Horizon;
        file.writeBoolean(this.splitHorizonWithPoisonedReverse()); // SPlit-Horizon with Poisoned-Reverse
        file.writeBoolean(this.ospf()); // OSPF
        file.writeBoolean(this.multiplePaths()); // MULTI
        file.writeBoolean(this.usingMultipleMetrics()); // Multiple Metrics
        file.writeBoolean(this.usingMultipleAreas());
        file.writeObject(this.controller.screen.objects); // save objects
        //this.controller.screen.reset();
        file.close();
        this.setTitle(this.title+" - "+this.filename);
        this.controller.setChanged(false);
      } catch(IOException ioe){
        this.term.write(ioe.toString());
        ioe.printStackTrace();
        this.controller.setChanged(true);
        return;
      }
      return;
    }
    this.controller.setChanged(true);
  }

  protected void quit(){
    save();
    this.setVisible(false);
    System.exit(0);
  }

  public static void main(String[] argv){
    /* use Look&Feel and not Window Style */
    /* JFrame.setDefaultLookAndFeelDecorated(true); */
    boolean dec = false;
    if(argv.length > 0 && argv[0].equals("dec")){
      dec = true;
    }
    dec = true;
    Locale.setDefault(Locale.ENGLISH);

    JFrame.setDefaultLookAndFeelDecorated(dec);
    JDialog.setDefaultLookAndFeelDecorated(dec);
    IRVFrame irvf = new IRVFrame("IRV - Tool", dec);
    Splash sp = new Splash("icons/splash01.png", 
    "IRV-Tool version 1.0, Copyright (C) 2004 Christian Sternagel\n"+
    "IRV-Tool comes with ABSOLUTELY NO WARRANTY;\n"+
    "for details choose `License Information -> Warranty Information'.\n"+
    "This is free software, and you are welcome to redistribute it\n"+
    "under certain conditions;\n"+
    "choose `License Information -> Conditions of Redistribution' for details.");
    sp.setMillis(10000);
    sp.start();
  }
}
