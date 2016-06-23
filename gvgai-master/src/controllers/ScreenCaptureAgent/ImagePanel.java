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
	if(image==null)
		return;
	
	int rate = getHeight()/image.getHeight();
	
	if(rate>0)
	{
			BufferedImage newImage = new BufferedImage(image.getWidth() * rate, image.getHeight() * rate,
					BufferedImage.TYPE_INT_RGB);

			for (int i = 0; i < image.getWidth(); i++)
				for (int j = 0; j < image.getHeight(); j++) {
					int startW = i * rate;
					int startH = j * rate;
					int endW = startW + rate;
					int endH = startH + rate;

					for (int m = startW; m < endW; m++)
						for (int n = startH; n < endH; n++) {
							newImage.setRGB(m, n, image.getRGB(i, j));
						}
				}

	this.image = newImage;
	}
	else
	{
//		int hblocksize = (int) Math.ceil((double)image.getHeight()/getHeight());
//		int wblocksize = (int) Math.ceil((double)image.getWidth()/getWidth());
//		
//		int blocksize = (hblocksize>wblocksize)? hblocksize:wblocksize;
//		
//		System.out.println(image.getHeight()+" "+getHeight());
//		System.out.println(blocksize);
//		double[][] shrinkedIm = preProcess(image,blocksize);
//		
//		BufferedImage im = new BufferedImage(shrinkedIm.length,shrinkedIm[0].length,BufferedImage.TYPE_INT_RGB);
//		
//		for(int i=0;i<im.getWidth();i++)
//			for(int j=0;j<im.getHeight();j++)
//				im.setRGB(i, j, (int) shrinkedIm[i][j]);
		int newH = image.getHeight()*getWidth()/image.getWidth();
		int newW = image.getWidth()*getHeight()/image.getHeight();
		BufferedImage dimg = null;
		Image tmp = null;
		if(newW>getWidth())
		{
			tmp = image.getScaledInstance(getWidth(), newH, Image.SCALE_SMOOTH);
		    dimg = new BufferedImage(getWidth(), newH, BufferedImage.TYPE_INT_ARGB);

		}
		
		else
		{
			tmp = image.getScaledInstance(newW, getHeight(), Image.SCALE_SMOOTH);
			dimg = new BufferedImage(newW, getHeight(), BufferedImage.TYPE_INT_ARGB);
		}
	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    
		this.image = dimg;
	}
}

private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

	if(image==null)
		return null;
/*	  int [] pix =  ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	  
	  ByteBuffer byteBuffer = ByteBuffer.allocate(pix.length * 4);        
      IntBuffer intBuffer = byteBuffer.asIntBuffer();
      intBuffer.put(pix);

      final byte[] pixels = byteBuffer.array();
     */
      final int width = image.getWidth();
      final int height = image.getHeight();
    //  System.out.println("2D "+width+" "+height);
      int[][] result = new int[width][height];
      
      for(int i=0;i<width;i++)
    	  for(int j=0;j<height;j++)
    	  {
    		 // System.out.println(width+" "+height+" "+i+" "+j);
    		  result[i][j] = image.getRGB(i, j);
    	  }
      
      
/*      final boolean hasAlphaChannel = image.getAlphaRaster() != null;

      int[][] result = new int[height][width];
      if (hasAlphaChannel) {
         final int pixelLength = 4;
         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
            int argb = 0;
            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
            argb += ((int) pixels[pixel + 1] & 0xff); // blue
            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
            result[row][col] = argb;
            col++;
            if (col == width) {
               col = 0;
               row++;
            }
         }
      } else {
         final int pixelLength = 3;
         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
            int argb = 0;
            argb += -16777216; // 255 alpha
            argb += ((int) pixels[pixel] & 0xff); // blue
            argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
            argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
            result[row][col] = argb;
            col++;
            if (col == width) {
               col = 0;
               row++;
            }
         }
      }
*/
      return result;
   }

public static double[][] preProcess(BufferedImage image, int block)
{
	int[][] im = convertTo2DWithoutUsingGetRGB(image);
	if(im == null)
		return null;
	//System.out.println(im.length+" "+im[0].length);
/*	BufferedImage nm = new BufferedImage(im.length,im[0].length,BufferedImage.TYPE_INT_RGB);
	for(int i=0;i<im.length;i++)
		for(int j=0;j<im[0].length;j++)
		{
			nm.setRGB(i,j,im[i][j]);
		}
	BufferedImage temp = new BufferedImage(im.length,im[0].length,BufferedImage.TYPE_INT_RGB);
	for(int i=0;i<im.length;i++)
		for(int j=0;j<im[0].length;j++)
			temp.setRGB(i,j,im[i][j]);
	File op = new File("pre.png");
    try {
		ImageIO.write(temp, "png", op);
	} catch (IOException e) {
		e.printStackTrace();
	}
	*/
	
	int width = image.getWidth();
	int height = image.getHeight();
	
	//int res = width*height;
	
	double[][] shrinkedIm = new double[width/block][height/block];
	
//	System.out.println(im.length+" "+im[0].length);
//	System.out.println(shrinkedIm.length+" "+shrinkedIm[0].length);
	
	
	for(int i=0;i<width/block;i++)
	{
		for(int j=0;j<height/block;j++)
		{
			//System.out.println(i+"\t"+j);
			int startW = i*block;
			int endW = startW+(block-1);
			int startH = j*block;
			int endH = startH+(block-1);
			
//			System.out.println(i+"\t"+j+"\t"+startW+"\t"+endW+"\t"+startH+"\t"+endH);
			
			int mean[] = new int[3];//Integer.MIN_VALUE;
			
			for(int k=startW;k<=endW;k++)
				for(int m=startH;m<=endH;m++)
				{
					Color c = new Color(im[k][m]);
					int red = c.getRed();
					int green = c.getGreen();
					int blue = c.getBlue();
						
					//if(im[k][m]>mean)
					//mean+=im[k][m];
					mean[0] += red;
					mean[1] += green;
					mean[2] += blue;
				}
			
			mean[0]/= block*block;
			mean[1]/= block*block;
			mean[2]/= block*block;
			
			shrinkedIm[i][j] = mean[0]*65536+mean[1]*256+mean[2];
		}
	}
	/*
	for(int i=0;i<shrinkedIm.length;i++)
	{
		for(int j=0;j<shrinkedIm[0].length;j++)
			System.out.print(shrinkedIm[i][j]+",");
		System.out.println();
	}
	*/
	/*
	BufferedImage newImage = new BufferedImage(shrinkedIm.length,shrinkedIm[0].length,BufferedImage.TYPE_INT_RGB);
	
	for(int i=0;i<shrinkedIm.length;i++)
		for(int j=0;j<shrinkedIm[0].length;j++)
		{
			newImage.setRGB(i,j,shrinkedIm[i][j]);
		}

	File outputfile = new File("nm_"+block+".png");
    try {
		ImageIO.write(newImage, "png", outputfile);
	} catch (IOException e) { 
		e.printStackTrace();
	}
    
    // get DataBufferBytes from Raster
    WritableRaster raster = newImage .getRaster();
    DataBufferInt data   = (DataBufferInt) raster.getDataBuffer();
    
    int[] pixels = data.getData();
    float[] pixs = new float[pixels.length];
    
    for(int i=0;i<pixs.length;i++)
    	pixs[i] = pixels[i];
	*/
	//System.out.println(shrinkedIm);
	return shrinkedIm;
	
}

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    //g.setColor(Color.WHITE);

	//System.out.println(image);
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