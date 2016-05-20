package controllers.ScreenCaptureAgent;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel{

private BufferedImage image;

public ImagePanel() {
  /* try {                
      image = ImageIO.read(new File("image name and path"));
   } catch (IOException ex) {
        // handle exception...
   }*/
}

public void setImage(BufferedImage image)
{
	
	int rate = getHeight()/image.getHeight();
	BufferedImage newImage = new BufferedImage(image.getWidth()*rate,image.getHeight()*rate,BufferedImage.TYPE_INT_RGB);
	
	for(int i=0;i<image.getWidth();i++)
		for(int j=0;j<image.getHeight();j++)
		{
			int startW = i*rate;
			int startH = j*rate;
			int endW = startW+rate;
			int endH = startH+rate;
			
			for(int m=startW;m<endW;m++)
				for(int n=startH;n<endH;n++)
				{
					newImage.setRGB(m, n, image.getRGB(i, j));
				}
		}
	
	this.image = newImage;
}

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    //g.setColor(Color.WHITE);
    if(image!=null)
    {
    	g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters
    //	File outputfile = new File("image.png");
	 //   try {
	//		ImageIO.write(image, "png", outputfile);
	//	} catch (IOException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}
    	
    }
}

}