package irvtool;

import java.awt.*;
import javax.swing.*;

public class IPPackage extends Package{
	
  private static Image image = new ImageIcon(IPPackage.class.getResource("icons/ippack.png")).getImage();

  public IPPackage(int from, int to, int metric, String text){
    super(from, to, T_IP, metric, text);
  }
  
  // just for development
  public IPPackage(int from, int to, String text){
	    super(from, to, T_IP, M_UNUSED, text);
	  }

  public IPPackage(int from, int to){
    this(from, to, M_UNUSED, null);
  }

  public Image getImage(){
    return image;
  }
}
