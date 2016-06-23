package myStuffs;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import controllers.ScreenCaptureAgent.ImagePanel;
import core.ArcadeMachine;

import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameSelectionFrame extends JFrame {

	/**
	 * Launch the application.
	 */
	
	String currentGame = "";
	String currentLevel = "";
	String path = "examples/gridphysics/";
	String recordActionsFile = null; //where to record the actions executed. null if not to save.
    int seed = new Random().nextInt();
	
	public static final ArrayList<String> gameName = new ArrayList<String>();
    public static final HashMap<String,Integer> levelMap = new HashMap<String, Integer>();
    
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GameSelectionFrame frame = new GameSelectionFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GameSelectionFrame() {
		getContentPane().setFont(new Font("Angsana New", Font.BOLD, 22));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 562, 430);
		getContentPane().setLayout(null);
		
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 150, 380);
		getContentPane().add(panel);
		
		
		final JTable table = new JTable();
		table.setFont(new Font("Angsana New", Font.PLAIN, 22));
		
		table.setBackground(Color.WHITE);
		table.getTableHeader().setFont(new Font("Angsana New", Font.BOLD, 26));
		
		DefaultTableModel tableModel = new DefaultTableModel(null,new String[]{"GAMES"});
		
		JFileChooser chooser = new JFileChooser();
		
		chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Game directory");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    //
	    // disable the "All files" option.
	    //
	    chooser.setAcceptAllFileFilterUsed(false);
	    //
	    
	    final JTable table2 = new JTable();
	    
	    table2.setShowHorizontalLines(false);
	    table2.setFont(new Font("Angsana New", Font.BOLD, 22));
	    table2.setColumnSelectionAllowed(true);
	    table2.setCellSelectionEnabled(true);
	    table2.setRowSelectionAllowed(false);
	    
	  //  if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 
	    {
			chooser.setCurrentDirectory(new File("examples/gridphysics"));
			File[] filesInDirectory = chooser.getCurrentDirectory().listFiles();
			//System.out.println(chooser.getCurrentDirectory());
			
			for (File file : filesInDirectory) 
			{
				String name = file.getName().split("(\\.)|(_)")[0];
			//	System.out.println(name);
				if(gameName.contains(name))
				{
					levelMap.replace(name, levelMap.get(name)+1);
				}
				else
				{
					gameName.add(name);
					levelMap.put(name, 0);
					tableModel.addRow(new String[]{name});
				}
			}

		} 
//	    else 
//	    {
//			System.out.println("No Selection ");
//		}

		table.setRowHeight(20);
		table.setModel(tableModel);
		

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(panel.getBounds().width, panel.getBounds().height - 5));
		panel.add(scrollPane);
		
		final ImagePanel imagePanel = new ImagePanel();
		imagePanel.setBackground(Color.BLACK);
		imagePanel.setBounds(160, 11, 365, 256);
		getContentPane().add(imagePanel);
		
	
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	int row = table.getSelectedRow();
		        int numLevel = levelMap.get(gameName.get(row));
		        DefaultTableModel tableModel2 = new DefaultTableModel(null,new String[numLevel]);
		        
		        String[] strNumLevel = new String[numLevel];
		        for(int i=0;i<numLevel;i++)
		        {
		        	strNumLevel[i] = i+"";
		        }
		        
		        tableModel2.addRow(strNumLevel);
		        table2.setModel(tableModel2);
		        
		        table2.repaint();
		        BufferedImage image = null;
				try 
				{
					image = ImageIO.read(new File("screenshots/"+gameName.get(row)+"_lvl0.txt.png"));
				//	System.out.println(image.getWidth()+" "+image.getHeight());
				} catch (IOException e) 
				{
					image = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
					image.setRGB(0, 0, 0);
					System.out.println("problem "+gameName.get(row));//e.printStackTrace();
				}
		        imagePanel.setImage(image);
		        imagePanel.repaint();
	        }
	    });
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(178, 304, 347, 35);
		getContentPane().add(panel_1);
		
		
		//JScrollPane scrollPane_1 = new JScrollPane(table2);
		//scrollPane_1.setPreferredSize(new Dimension(panel_1.getBounds().width, panel_1.getBounds().height - 5));
		
		
		
		table2.setPreferredSize(new Dimension(panel_1.getBounds().width-5, panel_1.getBounds().height - 5));
		table2.setRowHeight(panel_1.getBounds().height);
		table2.setRowMargin(20);
		table2.getColumnModel().setColumnMargin(20);
		
		table2.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	
	        //	System.out.println("selected");
	        	int row = table.getSelectedRow();
		        
		        int level = table2.getSelectedColumn();
		        
		        if(level==-1)
		        	level = 0;
		        BufferedImage image = null;
				try 
				{
					image = ImageIO.read(new File("screenshots/"+gameName.get(row)+"_lvl"+level+".txt.png"));
				//	System.out.println(image.getWidth()+" "+image.getHeight());
				} catch (IOException e) 
				{
					System.out.println("problem "+gameName.get(row)+"lvl_"+level);//e.printStackTrace();
				}
				currentGame = gameName.get(row)+".txt";
				currentLevel = gameName.get(row)+"_lvl"+level+".txt";
		        imagePanel.setImage(image);
		        imagePanel.repaint();
	        }
	    });
		
		table2.repaint();
		panel_1.add(table2);
		
		
		JButton btnPlay = new JButton("PLAY!");
		btnPlay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				MyThread thread = new MyThread(path+currentGame,path+currentLevel);
				thread.run();
			}
		});
		
		btnPlay.setFont(new Font("Angsana New", Font.BOLD, 18));
		btnPlay.setBounds(315, 349, 89, 29);
		getContentPane().add(btnPlay);
		
		JLabel lblLevels = new JLabel("LEVELS");
		lblLevels.setFont(new Font("Angsana New", Font.BOLD, 18));
		lblLevels.setBounds(328, 278, 59, 14);
		getContentPane().add(lblLevels);
		
//		for(int i=0;i<gameName.size();i++)
//		{
//			System.out.println(gameName.get(i)+" "+levelMap.get(gameName.get(i)));
//		}
	}
}
