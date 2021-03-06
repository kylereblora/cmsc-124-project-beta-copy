
	/*****************************************************************************************
	*  File Description:                                                                     *
	*  This file creates the lexeme table panel for the UI.                                  *
	*****************************************************************************************/

//  --------------------------------------------------  [PACKAGE DECLARATION]
	package gui;

//	--------------------------------------------------	[JAVA IMPORTS]
	import java.awt.*;
	import javax.swing.*;
	import javax.swing.table.*;

//	--------------------------------------------------	[CLASS SPECIFICATION]
public class LexemeTable extends JPanel {

	/**************
	*  Constants  *
	**************/
	private static final int MAXIMUM_PANEL_WIDTH = Interpreter.getPanelWidth()/2;
	private static final int MAXIMUM_PANEL_HEIGHT = Interpreter.getPanelHeight();
	
	/***************
	*  Attributes  *
	***************/

	private DefaultTableModel model;

	// Panel - Dividers
		private JLabel titlebar;
		private JPanel titlePanel;
		private JPanel tablePanel;
		private JTable table;
		private JScrollPane tableScroll;

	/***************************
	*    Panel Constructors    *
	***************************/
	public LexemeTable() {
		super();
		this.setComponents();
	}

	/****************
	*    Methods    *
	****************/
	private void setComponents() {

		this.setPreferredSize(new Dimension(LexemeTable.MAXIMUM_PANEL_WIDTH, LexemeTable.MAXIMUM_PANEL_HEIGHT));
		this.setLayout(new BorderLayout());

		// Initialization of Panels
		this.titlebar = new JLabel("Lexemes");
		this.titlePanel = new JPanel();
		this.tablePanel = new JPanel();

		this.titlePanel.setPreferredSize(new Dimension(LexemeTable.MAXIMUM_PANEL_WIDTH, (LexemeTable.MAXIMUM_PANEL_HEIGHT/2)-130));
		this.tablePanel.setPreferredSize(new Dimension(LexemeTable.MAXIMUM_PANEL_WIDTH, (LexemeTable.MAXIMUM_PANEL_HEIGHT/2)+130));

		this.titlePanel.add(this.titlebar, BorderLayout.CENTER);

		// Table Creation
		this.model = new DefaultTableModel(){
			@Override
			public boolean isCellEditable(int row, int column) {
      			return false;
    		}
		};
		
		this.table = new JTable(this.model);
		this.model.addColumn("Lexeme");
		this.model.addColumn("Classification");

		this.tableScroll = new JScrollPane(this.table);
		this.tableScroll.setPreferredSize(new Dimension(LexemeTable.MAXIMUM_PANEL_WIDTH,250));
		this.tablePanel.add(this.tableScroll, BorderLayout.CENTER);

		this.add(this.titlePanel, BorderLayout.NORTH);
		this.add(this.tablePanel, BorderLayout.CENTER);
	}

	/****************
	*    Getters    *
	****************/
	public DefaultTableModel getModel(){return this.model;}
	public static int getPanelWidth(){return LexemeTable.MAXIMUM_PANEL_WIDTH;}
	public static int getPanelHeight(){return LexemeTable.MAXIMUM_PANEL_HEIGHT;}

	/****************
	*    Setters    *
	****************/
	public void resetModel(){this.model = new DefaultTableModel();}
}
