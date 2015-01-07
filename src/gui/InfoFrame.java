package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

@SuppressWarnings("serial")
final public class InfoFrame<E> extends JFrame{
	
	private JPanel infoPanel;
	private JLabel infoLabelGeneral;
	private JLabel infoLabelSpecific;
	private JList<E> list;
	private DefaultListModel<E> listModel;
	private JScrollPane listScroll;
	private JTextArea log;
	private JScrollPane logScroll;
	private JTextArea debug;
	private JScrollPane debugScroll;
	private JTextArea info;
	private JScrollPane exceptionsScroll;
	private JTextArea exceptions;
	private JScrollPane infoScroll;
	private JTabbedPane textTabs;
	
	private JTextField textField;

	public InfoFrame(ListCellRenderer<E> cellRenderer){
		this();
		list.setCellRenderer(cellRenderer);
	}
	
	public InfoFrame(){
		super();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		
		Border blackLine = BorderFactory.createLineBorder(Color.black);
		
		infoLabelGeneral = new JLabel("General info");
		infoLabelSpecific = new JLabel("Specific info");
		
		listModel = new DefaultListModel<E>();
		list = new JList<E>(listModel);
		list.setBorder(blackLine);
		listScroll = new JScrollPane(list);
		
		log = new JTextArea();
		log.setText("Commands ^.^");
		log.setEditable(false);
		logScroll = new JScrollPane(log);
		
		debug = new JTextArea();
		debug.setText("Debug ^.^");
		debug.setEditable(false);
		debugScroll = new JScrollPane(debug);
		
		info = new JTextArea();
		info.setText("Info ^.^");
		info.setEditable(false);
		infoScroll = new JScrollPane(info);
		
		exceptions = new JTextArea();
		exceptions.setText("Exceptions ^.^");
		exceptions.setEditable(false);
		exceptionsScroll = new JScrollPane(exceptions);
		
		textTabs = new JTabbedPane();
		textTabs.setPreferredSize(new Dimension(0,250));
		textTabs.addTab("Log", logScroll);
		textTabs.addTab("MinLog", debugScroll);
		textTabs.addTab("Info", infoScroll);
		textTabs.addTab("Exceptions", exceptionsScroll);
		
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		
		c.weighty = 0.02;
		gridBag.setConstraints(infoLabelGeneral, c);
		gridBag.setConstraints(infoLabelSpecific, c);
		c.weighty = 1;
		gridBag.setConstraints(listScroll, c);
		gridBag.setConstraints(textTabs, c);
		
		infoPanel = new JPanel(gridBag);
		infoPanel.add(infoLabelGeneral);
		infoPanel.add(infoLabelSpecific);
		infoPanel.add(listScroll);
		infoPanel.add(textTabs);
		
		
		textField = new JTextField();
		
		add(infoPanel, BorderLayout.CENTER);
		add(textField, BorderLayout.SOUTH);
		
		//Make textField get the focus whenever frame is activated.
		addWindowFocusListener(new WindowAdapter() {
		    public void windowGainedFocus(WindowEvent e) {
		        textField.requestFocusInWindow();
		    }
		});
	}
	
	public void clearList(){
		listModel.clear();
	}

	public void setListItems(final Vector<E> listItems){
		
		listModel.clear();
		for(E listItem : listItems){
			listModel.addElement(listItem);
		}
		list.ensureIndexIsVisible(listModel.size() -1);

	}
	
	public void setGeneralInfoText(String info){
		infoLabelGeneral.setText(info);
	}
	
	public void setSpecificInfoText(String info){
		infoLabelSpecific.setText(info);
	}
	
	public void addListItem(E listItem){
		listModel.addElement(listItem);
		list.ensureIndexIsVisible(listModel.size() -1);
	}
	public void setListCellRenderer(ListCellRenderer<E> cellRenderer){
		list.setCellRenderer(cellRenderer);
	}
		
	public String getCommand(){
		return textField.getText();
	}
	
	public void addLogLine(String line){
		log.append("\n" + line);
		log.setCaretPosition(log.getDocument().getLength());
		logScroll.getHorizontalScrollBar().setValue(logScroll.getHorizontalScrollBar().getMinimum());
	}
	
	public void addDebugLine(String line){
		debug.append("\n" + line);
		debug.setCaretPosition(debug.getDocument().getLength());
		debugScroll.getHorizontalScrollBar().setValue(debugScroll.getHorizontalScrollBar().getMinimum());
	}
	
	public void addInfoLine(String line){
		info.append("\n" + line);
		info.setCaretPosition(info.getDocument().getLength());
		infoScroll.getHorizontalScrollBar().setValue(infoScroll.getHorizontalScrollBar().getMinimum());
	}
	
	public void addExceptionLine(String line){
		exceptions.append("\n" + line);
		exceptions.setCaretPosition(exceptions.getDocument().getLength());
		exceptionsScroll.getHorizontalScrollBar().setValue(exceptionsScroll.getHorizontalScrollBar().getMinimum());
	}
	
	public void setCommandListener(final Runnable listener){
		textField.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				listener.run();
				textField.setText("");			
			}
		});

	}
}
