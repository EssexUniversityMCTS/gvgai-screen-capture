package controllers.ScreenCaptureAgent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import ontology.Types;

public class MyFrame extends JFrame {

	public JPanel contentPane;
	public JTable table;
	public JTable qTable;
	public JTextField action;
	public JTextField reward;
	private JPanel panel;
	private JScrollPane scrollPane;
	final ImagePanel previous = new ImagePanel();
	final ImagePanel result = new ImagePanel();

	Experience[] experiencePool;
	ArrayList<Types.ACTIONS> actions;
	double[][] qv;
	private JTextField picSize;
	/**
	 * Launch the application.
	 */
	/*
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
				//	MyFrame frame = new MyFrame();
					
				//	frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
*/
	/**
	 * Create the frame.
	 */
	public MyFrame(Experience[] experiencePool, ArrayList<Types.ACTIONS> actions, double[][] qv) {
		this.actions = actions;
		this.qv = qv;
		setTitle("Experience Log");
		this.experiencePool = experiencePool;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 627, 420);
		setLocation((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()-getWidth()),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()-getHeight()));
		getContentPane().setLayout(null);
		
		//final ImagePanel previous = new ImagePanel();
		previous.setBackground(Color.BLACK);
		previous.setBounds(124, 5, 311, 151);
		getContentPane().add(previous);
		
		//final ImagePanel result = new ImagePanel();
		result.setBackground(Color.BLACK);
		result.setBounds(124, 223, 311, 151);
		getContentPane().add(result);
		DefaultTableModel tableModel = new DefaultTableModel(null,new String[]{"Experience"});
		
	//	tableModel.addRow(new String[]{"1"});
		
		action = new JTextField();
		action.setEditable(false);
		action.setFont(new Font("Traditional Arabic", Font.PLAIN, 15));
		action.setBounds(134, 177, 164, 35);
		getContentPane().add(action);
		action.setColumns(10);
		
		reward = new JTextField();
		reward.setEditable(false);
		reward.setFont(new Font("Traditional Arabic", Font.PLAIN, 22));
		reward.setColumns(10);
		reward.setBounds(336, 177, 109, 35);
		getContentPane().add(reward);
		
		panel = new JPanel();
		panel.setBounds(0, 0, 125, 374);
		getContentPane().add(panel);
		
		table = new JTable();
		DefaultTableModel tableModel2 = new DefaultTableModel(null,new String[]{"Action", "Value"});
		
		for(int i=0;i<actions.size();i++)
		{
			tableModel2.addRow(new String[]{});
		}
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	int row = table.getSelectedRow();
		        if (row >= 0) {
		        	try{
		        		fill(row);
		        	}
		        	catch(NullPointerException e){}
		        }
	        }
	    });
		
		table.setFont(new Font("Tahoma", Font.PLAIN, 11));
		table.setBackground(Color.GRAY);
		
		table.setModel(tableModel);
		scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(panel.getWidth(),panel.getHeight()));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane);
		
		
		JPanel qv_panel = new JPanel();
		qv_panel.setBounds(440, 0, 170, 137);
		getContentPane().add(qv_panel);
		qTable = new JTable();
		//tableModel.addRow(new String[]{"1"});
		qTable.setModel(tableModel2);
		qTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		
		JScrollPane scrollPane_1 = new JScrollPane(qTable);
		qv_panel.add(scrollPane_1);
		scrollPane_1.setPreferredSize(new Dimension(160, 120));
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		picSize = new JTextField();
		picSize.setEditable(false);
		picSize.setBounds(484, 148, 86, 20);
		getContentPane().add(picSize);
		picSize.setColumns(10);
		picSize.setText("Store "+QLearning.pool.size());
		
	//	contentPane = new JPanel();
	//	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	//	contentPane.setLayout(new GridLayout(1, 3));
	//	setContentPane(contentPane);
	}
	
	public void update()
	{
		picSize.setText("Store "+QLearning.pool.size());
		
	}
	
	public void fill(int row)
	{
		Experience experience = experiencePool[row];
    	reward.setText(experience.getReward()+"");
    	action.setText(experience.getAction()+"");
    	
    	double[][] pr = experience.getPrevious();
    	BufferedImage prev = new BufferedImage(pr.length,pr[0].length,BufferedImage.TYPE_INT_RGB);
   // 	System.out.println(pr+" "+pr.length+" "+pr[0].length);
    	for(int i=0;i<pr.length;i++)
    		for(int j=0;j<pr[0].length;j++)
    			prev.setRGB(i, j, (int)pr[i][j]);

    	previous.setImage(prev);
    	previous.repaint();
    	
    	double[][] re = experience.getResult();
    	BufferedImage res = new BufferedImage(re.length,re[0].length,BufferedImage.TYPE_INT_RGB);
   // 	System.out.println(pr+" "+pr.length+" "+pr[0].length);
    	for(int i=0;i<re.length;i++)
    		for(int j=0;j<re[0].length;j++)
    			res.setRGB(i, j, (int)re[i][j]);

    	result.setImage(res);
    	result.repaint();
    	/*
    	File outputfile = new File("nm.png");
	    try {
			ImageIO.write(prev, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	*/
    	DefaultTableModel qTableModel = (DefaultTableModel) qTable.getModel();
    	
    	int index = QLearning.findIndexFromImage(experience.getPrevious());
    	for(int i=0;i<actions.size();i++)
    	{
    		//String[] st = new String[]{actions.get(i)+"", qv[row][i]+""};
    		qTableModel.setValueAt(actions.get(i), i, 0);
    		qTableModel.setValueAt(qv[index][i], i, 1);
    	}
    	
    	repaint();
	}
}
