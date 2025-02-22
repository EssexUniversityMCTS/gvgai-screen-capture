package tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;

import core.competition.CompetitionParameters;

/**
 * Frame for the graphics.
 * PTSP-Competition
 * Created by Diego Perez, University of Essex.
 * Date: 20/12/11
 */
public class JEasyFrame extends JFrame {

    /**
     * Main component of the frame.
     */
    public Component comp;
    public Point location;
    public Dimension dimension;

    /**
     * Constructor
     * @param comp Main component of the frame.
     * @param title Title of the window.
     */
    public JEasyFrame(Component comp, String title) {
        super(title);
        this.comp = comp;
        getContentPane().add(BorderLayout.CENTER, comp);
        pack();
        this.setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        if(CompetitionParameters.closeAppOnClosingWindow){
        	setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
        
        location = this.getLocationOnScreen();
        dimension = this.getContentPane().getSize();
        repaint();
    }

    /**
     * Closes this component.
     */
    public void quit()
    {
        System.exit(0);
    }
}

