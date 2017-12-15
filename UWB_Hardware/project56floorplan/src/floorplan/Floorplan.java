package floorplan;

import gnu.io.CommPortIdentifier;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Floorplan extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener {


	private JButton loadConfigButton;
	private JButton configSaveButton;
	public JCheckBox sendSerial;
    public JCheckBox displaySerial;
    private JComboBox comList;

	public Triangulation triangulation;
    private JTextField comTextField;
	private JButton startSerialButton;
	private JButton simulatorButton;
    private Simulator simulator= new Simulator(this);
	private JTextArea jsonTextField;
	private JButton jsonExportButton;
	public JButton loadButton;
	JTextField path;
	JFrame frame;

	JList<String> anchorList;
	public DefaultListModel anchorListItems;
    private AnchorPanel anchorPanel;

	private String action = "none";
	private BufferedImage floorplan;


	public DefaultListModel roomsListItems;
	private RoomsPanel roomsPanel;


    private JFrame roomMaker = new JFrame("room configurator");
    private SerialThread serialThread;

	public UWBConfiguration uwbConfiguration = new UWBConfiguration();
	public String fileLocation="C:\\Users\\Rene Schouten\\Pictures\\project56\\";

	public Floorplan(JFrame frame) {
		this.frame = frame;




		path = new JTextField(fileLocation+"room1.png");
		path.setBounds(1100, 10, 500, 50);
		path.setPreferredSize(new Dimension(200, 24));

		loadButton = new JButton("load");
		loadButton.setBounds(1600, 10, 80, 30);
		loadButton.addActionListener(this);

		add(path);
		add(loadButton);

        JLabel anchorsLabel = new JLabel("uwbConfiguration.anchors");
        anchorsLabel.setBounds(1050, 40, 200, 100);
        add(anchorsLabel);


		anchorPanel = new AnchorPanel(this);
        add(anchorPanel.anchorJPanel);

        JLabel roomsLabel = new JLabel("rooms");
        roomsLabel.setBounds(1050, 360, 200, 100);
        add(roomsLabel);

		roomsPanel = new RoomsPanel(this);
        add(roomsPanel.roomsJPanel);



		jsonTextField = new JTextArea();
		jsonTextField.setWrapStyleWord(true);
		jsonTextField.setLineWrap(true);
		jsonTextField.setBounds(1050,800,800,200);
		add(jsonTextField);

		jsonExportButton = new JButton("export config");
		jsonExportButton.setBounds(1800, 20, 110, 30);
		jsonExportButton.addActionListener(this);
		add(jsonExportButton);

		configSaveButton = new JButton("save config");
		configSaveButton.setBounds(1800, 60, 110, 30);
		configSaveButton.addActionListener(this);
		add(configSaveButton);

		loadConfigButton = new JButton("load config");
		loadConfigButton.setBounds(1800, 100, 110, 30);
		loadConfigButton.addActionListener(this);
		add(loadConfigButton);



        simulatorButton = new JButton("simulate");
        simulatorButton.setBounds(1680,20, 110, 30);
        simulatorButton.addActionListener(this);
        add(simulatorButton);

		startSerialButton = new JButton("start serial");
		startSerialButton.addActionListener(this);
		startSerialButton.setBounds(1680,60,110,30);
		add(startSerialButton);

		comList = new JComboBox();
		comList.setBounds(1590,50,80,30);
		comList.addActionListener(this);
		findComPorts();
		add(comList);

        displaySerial = new JCheckBox("show");
        displaySerial.setBounds(1590,80,100,20);
        add(displaySerial);

        sendSerial = new JCheckBox("send");
        sendSerial.setBounds(1590,105,100,20);
        sendSerial.setSelected(true);
        add(sendSerial);

		setSize(new Dimension(1920, 1020));
        addKeyListener(this);
        requestFocusInWindow();
        setFocusable(true);
		addMouseListener(this);
		setLayout(null);
		setVisible(true);

        roomMaker.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        roomMaker.setSize(new Dimension(500,500));
        roomMaker.setVisible(false);


    }

	public void findComPorts()
	{
		comList.removeAll();
        comList.removeAllItems();
		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();

		while ( portEnum.hasMoreElements() )
		{
			CommPortIdentifier portIdentifier = portEnum.nextElement();
			if(portIdentifier.getPortType()==CommPortIdentifier.PORT_SERIAL) {
				comList.addItem(portIdentifier.getName());

			}
		}

	}


	public void anchorEditCancel() {
        anchorPanel.editAnchorPanel.setVisible(false);
		uwbConfiguration.anchors.get(anchorPanel.anchorEditingIndex).deselect();
		repaint();
	}
	public void roomsEditCancel() {
		roomsPanel.editRoomPanel.setVisible(false);
		uwbConfiguration.rooms.get(roomsPanel.roomsEditingIndex).deselect();
		repaint();
	}
	public void anchorEdit() {

		Anchor anchor = uwbConfiguration.anchors.get(anchorPanel.anchorEditingIndex);
		int x;
		int y;
		try {
			x = Integer.parseInt(anchorPanel.anchorX.getText());
			y = Integer.parseInt(anchorPanel.anchorY.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "invalidate values", "invalidate values",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if ((x < 0) || (y < 0) || (x > 1000) || (y > 1000)) {
			JOptionPane.showMessageDialog(null, "invalidate values", "invalidate values",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		anchor.setLocation(new Point(x, y));
        anchor.setDistance(anchorPanel.editAnchorDistance.getText());
		anchor.setName(anchorPanel.editAnchorName.getText());
		anchorListItems.set(anchorPanel.anchorEditingIndex, makeAnchorText(x, y, anchorPanel.editAnchorName.getText()));
        anchorPanel.editAnchorPanel.setVisible(false);

		anchor.deselect();
		repaint();
		anchorList.revalidate();
	}

	public void openAnchorEdit(int index) {
		Anchor anchor = uwbConfiguration.anchors.get(index);
        anchorPanel.anchorX.setText("" + (int) anchor.getLocation().getX());
        anchorPanel.anchorY.setText("" + (int) anchor.getLocation().getY());
        anchorPanel.editAnchorName.setText(anchor.getName());
        anchorPanel.editAnchorDistance.setText(anchor.getDistanceString());
        anchorPanel.editAnchorPanel.setVisible(true);

		anchor.setSelected();
		repaint();
	}
	public void openRoomEdit(int index) {
		Room room = uwbConfiguration.rooms.get(index);
		roomsPanel.roomX.setText("" + (int) room.getLocation1().getX());
		roomsPanel.roomY.setText("" + (int) room.getLocation1().getY());
		roomsPanel.editRoomName.setText(room.getName());

		roomsPanel.editRoomPanel.setVisible(true);

		room.setSelected();
		repaint();
	}

	public String makeAnchorText(int x, int y, String name) {
		return "(" + x + "," + y + ") " + name;
	}
	public String makeRoomsText(int x, int y, int x2, int y2, String name) {
		return "(" + x + "," + y + ") " +"(" + x2 + "," + y2 + ") "+ name;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
	    if(e.getSource()==comList)
        {
            findComPorts();
        }
		if (e.getSource() == loadButton) {
			loadImage(path.getText());
		}
		if(e.getSource() == jsonExportButton) {
			calculateScale();
			JsonCreator j = new JsonCreator(uwbConfiguration.anchors,uwbConfiguration.rooms, uwbConfiguration.scale,this);
			jsonTextField.setText(j.getJson());
			ConfigConnection con = new ConfigConnection();
			con.sendConfig(j.getJson());
			con.sendImage(path.getText().toString());

		}
		if(e.getSource()==configSaveButton)
		{
			calculateScale();
			JsonCreator j = new JsonCreator(uwbConfiguration.anchors,uwbConfiguration.rooms, uwbConfiguration.scale,this);
			jsonTextField.setText(j.getJson());
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter( new FileWriter(fileLocation+"config.txt"));
				writer.write(j.getJson());
			} catch ( IOException e2) {
				e2.printStackTrace();
			} finally {
				try {
					if ( writer != null)
						writer.close( );
				} catch ( IOException e3) {
					e3.printStackTrace();
				}
			}
		}
		if(e.getSource()==loadConfigButton)
		{
			uwbConfiguration.loadFromFile(fileLocation+"config.txt",this);
			anchorListItems.clear();

			for(int i=0;i<uwbConfiguration.anchors.size();i++){
				anchorListItems.addElement(makeAnchorText((int)uwbConfiguration.anchors.get(i).getLocation().getX(),(int)uwbConfiguration.anchors.get(i).getLocation().getY(), uwbConfiguration.anchors.get(i).getName()));
			}anchorList.revalidate();

			roomsListItems.clear();
			for(Room room:uwbConfiguration.rooms){
				roomsListItems.addElement(makeRoomsText((int)room.getLocation1().getX(),(int)room.getLocation1().getY(),(int)room.getLocation2().getX(),(int)room.getLocation2().getY(),room.getName()));
			}roomsPanel.roomsList.revalidate();
		}
		if (e.getSource() == anchorPanel.addAnchorButton) {
			if (checkAnchorName(anchorPanel.anchorNameField.getText())) {
				action = "placeAnchor";
				frame.setCursor(Cursor.CROSSHAIR_CURSOR);

				uwbConfiguration.anchors.add(new Anchor(anchorPanel.anchorNameField.getText(), new Point(0, 0), anchorPanel.masterAnchorCheckBox.isSelected(), anchorPanel.fakeCheckBox.isSelected(),
						true,anchorPanel.anchorDistance.getText()));
				anchorListItems.addElement(makeAnchorText(0, 0, anchorPanel.anchorNameField.getText()));
                anchorPanel.anchorEditingIndex = uwbConfiguration.anchors.size() - 1;
			}

		}

		if (e.getSource() == roomsPanel.addRoomButton) {
			if (checkRoomName(roomsPanel.roomNameField.getText())) {
				action = "placeRoom1";
				frame.setCursor(Cursor.CROSSHAIR_CURSOR);

				uwbConfiguration.rooms.add(new Room(roomsPanel.roomNameField.getText(), new Point(0, 0),new Point(0, 0),
						true,false));
				roomsListItems.addElement(makeRoomsText(0, 0,0,0, roomsPanel.roomNameField.getText()));
				roomsPanel.roomsEditingIndex = uwbConfiguration.rooms.size() - 1;
			}

		}
		if(e.getSource()==simulatorButton)
        {
            if(simulator.isOn()==false)
            {
                simulatorButton.setText("stop simulation");
                simulator.startSimulator();
            }
            else
            {
                simulatorButton.setText("simulate");
                simulator.stopSimulator();
            }
        }
		if(e.getSource()==startSerialButton)
		{
		    if(serialThread==null) {
		        startSerialButton.setText("stop serial");
                serialThread = new SerialThread(String.valueOf(comList.getSelectedItem()),this);
                serialThread.start();
            }
            else
            {
                serialThread.kill();
                serialThread=null;
                startSerialButton.setText("start serial");
            }
		}
	}

	public void loadImage(String imagePath) {
		String tempPath=  imagePath.replace("\\","/");
		System.out.println(tempPath);
		int lastSlash =  tempPath.lastIndexOf("/");
		uwbConfiguration.imageName=tempPath.substring(lastSlash+1,tempPath.length());
		System.out.println(uwbConfiguration.imageName);
		floorplan = null;
		try {
			floorplan = ImageIO.read(new File( imagePath));

			BufferedImage before = floorplan;
			int max = Math.max(before.getWidth(), before.getHeight());
			uwbConfiguration.imageScale = 1000.0 / max;

			int w = 1000;
			int h = 1000;
			BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			// at.scale(1000/before.getWidth(), 1000/before.getHeight());
			at.scale(uwbConfiguration.imageScale, uwbConfiguration.imageScale);
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			after = scaleOp.filter(before, after);
			floorplan = after;
		} catch (IOException e1) {
			e1.printStackTrace();
			consolePrint("image don't exist");
		}
		repaint();

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (action.equals("placeAnchor")) {
			if (e.getX() < 1000) {
				action = "none";
				frame.setCursor(Cursor.DEFAULT_CURSOR);
				System.out.println(anchorPanel.anchorEditingIndex);
				uwbConfiguration.anchors.get(anchorPanel.anchorEditingIndex).setIsMoving(false);
				repaint();
                anchorPanel.anchorNameField.setText("");
                anchorPanel.anchorDistance.setText("0");
                anchorPanel.masterAnchorCheckBox.setSelected(false);
                anchorPanel.fakeCheckBox.setSelected(false);
			}

		} else if (action.equals("placeRoom1")) {
			if (e.getX() < 1000) {
				action = "placeRoom2";
				System.out.println(roomsPanel.roomsEditingIndex);
				uwbConfiguration.rooms.get(roomsPanel.roomsEditingIndex).setIsMoving1(false);
				uwbConfiguration.rooms.get(roomsPanel.roomsEditingIndex).setIsMoving2(true);
				repaint();
			}

		}
		else if (action.equals("placeRoom2")) {
			if (e.getX() < 1000) {
				action = "none";
				frame.setCursor(Cursor.DEFAULT_CURSOR);
				System.out.println(roomsPanel.roomsEditingIndex);
				uwbConfiguration.rooms.get(roomsPanel.roomsEditingIndex).setIsMoving2(false);
				repaint();
			}

		}else {
			int counter = 0;
			for (Anchor anchor : uwbConfiguration.anchors) {
				Point loc = anchor.getLocation();
				if ((loc.getX() < e.getX() + 40) && (loc.getX() > e.getX() - 40)) {
					if ((loc.getY() < e.getY() + 40) && (loc.getY() > e.getY() - 40)) {
						if (e.getClickCount() == 1) {
							if(anchorPanel.editAnchorPanel.isVisible())
							{
								anchorEditCancel();
								if(anchorPanel.anchorEditingIndex!=counter)
								{
                                    anchorPanel.anchorEditingIndex=counter;
									openAnchorEdit(counter);
								}
							}
							else
							{
								System.out.println(counter);
                                anchorPanel.anchorEditingIndex=counter;
								openAnchorEdit(counter);
							}

						}
						if (e.getClickCount() == 2) {
                            anchorPanel.anchorEditingIndex = counter;
							anchorEditCancel();
							anchor.setIsMoving(true);
							action = "placeAnchor";
							frame.setCursor(Cursor.CROSSHAIR_CURSOR);
						}
					}
				}
				counter++;
			}
			for (Room room:uwbConfiguration.rooms) {
				counter=0;
				Point loc = room.getLocation1();
				Point loc2 = room.getLocation2();
				if ((loc.getX() < e.getX() + 15) && (loc.getX() > e.getX() - 15)) {
					if ((loc.getY() < e.getY() + 15) && (loc.getY() > e.getY() - 15)) {
						if (e.getClickCount() == 1) {
							if(roomsPanel.editRoomPanel.isVisible())
							{
								roomsEditCancel();
								if(roomsPanel.roomsEditingIndex!=counter)
								{
									roomsPanel.roomsEditingIndex=counter;
									openRoomEdit(counter);
								}
							}
							else
							{
								System.out.println(counter);
								roomsPanel.roomsEditingIndex=counter;
								openRoomEdit(counter);
							}

						}
						if (e.getClickCount() == 2) {
							roomsPanel.roomsEditingIndex = counter;
							roomsEditCancel();
							room.setIsMoving1(true);
							action = "placeRoom1";
							frame.setCursor(Cursor.CROSSHAIR_CURSOR);
						}
					}
					counter++;
				}
				if ((loc2.getX() < e.getX() + 15) && (loc2.getX() > e.getX() - 15)) {
					if ((loc2.getY() < e.getY() + 15) && (loc2.getY() > e.getY() - 15)) {
						if (e.getClickCount() == 1) {
							if(roomsPanel.editRoomPanel.isVisible())
							{
								roomsEditCancel();
								if(roomsPanel.roomsEditingIndex!=counter)
								{
									roomsPanel.roomsEditingIndex=counter;
									openRoomEdit(counter);
								}
							}
							else
							{
								System.out.println(counter);
								roomsPanel.roomsEditingIndex=counter;
								openRoomEdit(counter);
							}

						}
						if (e.getClickCount() == 2) {
							roomsPanel.roomsEditingIndex = counter;
							roomsEditCancel();
							room.setIsMoving2(true);
							action = "placeRoom2";
							frame.setCursor(Cursor.CROSSHAIR_CURSOR);

						}
					}
				}
				counter++;
			}
		}
		calculateScale();

	}

	public boolean checkAnchorName(String name) {
		if (name.length() == 0) {
			JOptionPane.showMessageDialog(null, "give the anchor a name", "no name", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		int masters = 0;
		for (Anchor anchor : uwbConfiguration.anchors) {
			if (anchor.getName().equals(name)) {
				JOptionPane.showMessageDialog(null, "this name is already used", "name already exist",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			if (anchor.isMaster())
				masters++;
		}
		if (masters > 0) {
			if (anchorPanel.masterAnchorCheckBox.isSelected()) {
				JOptionPane.showMessageDialog(null, "there can not more than 1 master", "multiple masters",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
		}
		if((anchorPanel.masterAnchorCheckBox.isSelected())&&(anchorPanel.fakeCheckBox.isSelected()))
        {
            JOptionPane.showMessageDialog(null, "can't both master and fake", "error",
                    JOptionPane.INFORMATION_MESSAGE);
        }
		return true;
	}
	public boolean checkRoomName(String name) {
		if (name.length() == 0) {
			JOptionPane.showMessageDialog(null, "give the room a name", "no name", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		for (Room room : uwbConfiguration.rooms) {
			if (room.getName().equals(name)) {
				JOptionPane.showMessageDialog(null, "this name is already used", "name already exist",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
		}
		return true;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (floorplan != null) {
			g.drawImage(floorplan, 0, 0, floorplan.getWidth(), floorplan.getHeight(), null);
		}
		for (Anchor anchor : uwbConfiguration.anchors) {
			int x = 0;
			int y = 0;
			int size = 40;
			if (anchor.isMoving()) {
				Point mousePos = getMousePosition();
				x = (int) mousePos.getX();
				y = (int) mousePos.getY();
				anchor.setLocation(new Point(x, y));
				anchorListItems.setElementAt(makeAnchorText(x, y, anchor.getName()), anchorPanel.anchorEditingIndex);
				anchorList.revalidate();
			} else {
				x = (int) anchor.getLocation().getX();
				y = (int) anchor.getLocation().getY();
			}
			Color color = null;

			if (anchor.isMaster()) {
				color = Color.blue;
			}else if(anchor.isFake())
            {
                color = Color.MAGENTA;
            }
			else {
				color = Color.red;
			}
			if (anchor.isSelected()) {
				color = Color.green;

			}
			g.setColor(color);
			size = 20;
			g.fillOval(x - size / 2, y - size / 2, size, size);
			size = 30;
			if (anchor.isMaster()) {
				color = Color.blue;
			} else {
				color = Color.red;
			}
			g.setColor(color);
			g.drawOval(x - size / 2, y - size / 2, size, size);
			size = 40;
			g.drawOval(x - size / 2, y - size / 2, size, size);
		}
		for (Room room:uwbConfiguration.rooms) {
			int x = 0;
			int y = 0;
			int x2 = 0;
			int y2 = 0;
			int size = 40;
			if (room.isMoving1()) {
				Point mousePos = getMousePosition();
				x = (int) mousePos.getX();
				y = (int) mousePos.getY();
				room.setLocation1(new Point(x, y));
			} else {
				x = (int) room.getLocation1().getX();
				y = (int) room.getLocation1().getY();
			}
			if (room.isMoving2()) {
				Point mousePos = getMousePosition();
				x2 = (int) mousePos.getX();
				y2 = (int) mousePos.getY();
				room.setLocation2(new Point(x2, y2));

			} else {
				x2 = (int) room.getLocation2().getX();
				y2 = (int) room.getLocation2().getY();
			}
			roomsListItems.setElementAt(makeRoomsText(x, y,x2, y2, room.getName()), roomsPanel.roomsEditingIndex);
			roomsPanel.roomsList.revalidate();
			Color color = null;
			color = Color.magenta;
			if (room.isSelected()) {
				color = Color.green;
			}
			g.setColor(color);
			size = 10;
			g.fillOval(x - size / 2, y - size / 2, size, size);
			size = 10;
			g.fillOval(x2 - size / 2, y2 - size / 2, size, size);
			
			g.drawRect(x, y, x2-x, y2-y);
			g.drawRect(x+1, y+1, x2-x-2, y2-y-2);
			g.setColor(Color.CYAN);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 25)); 
			g.drawString(room.getName(), x+5, y+20);
		}
		if(simulator.isOn())
        {
            g.setColor(Color.ORANGE);
            g.fillOval((int)simulator.getLocation().getX(),(int)simulator.getLocation().getY(),30, 30);
        }
        if(triangulation!=null)
		{
        if(triangulation.tags.size()>0) {
			if (triangulation.tags.get(0).getLocation() != null) {
				double x = triangulation.tags.get(0).getLocation().getX();
				double y = triangulation.tags.get(0).getLocation().getY();
				g.setColor(Color.cyan);
				g.fillOval((int) x, (int) y, 30, 30);
				for (int i = 0; i < triangulation.tags.get(0).potentialLocations.size(); i++) {
					g.setColor(Color.pink);
					x = triangulation.tags.get(0).potentialLocations.get(i).getLocation().getX();
					y = triangulation.tags.get(0).potentialLocations.get(i).getLocation().getY();
					g.fillOval((int) x, (int) y, 20, 20);
				}
			}
		}
        }


	}

	@Override
	public void mousePressed(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	@Override
	public void mouseExited(MouseEvent e) {
	}
	long lastPaintTime = 0;
	@Override
	public void mouseDragged(MouseEvent e) {
		updateGui();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		updateGui();
	}
	public void updateGui() {

		if (System.currentTimeMillis() - lastPaintTime > 30) {
			repaint();
			lastPaintTime = System.currentTimeMillis();
		}
	}

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {
        simulator.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        simulator.keyReleased(e);
    }

    public void calculateScale()
	{
		uwbConfiguration.scale=0;
		int masterIndex=-1;
		for (int i = 0; i < uwbConfiguration.anchors.size(); i++) {
			if (uwbConfiguration.anchors.get(i).isMaster()) {
				masterIndex=i;
			}
		}
		if(masterIndex==-1)
		{
			uwbConfiguration.scale=0;
			return;
		}



		for (int i = 0; i < uwbConfiguration.anchors.size(); i++) {
			if (!uwbConfiguration.anchors.get(i).isMaster()) {
				double distance= uwbConfiguration.anchors.get(i).getDistance();
				if(distance!=0)
				{
					double x1= uwbConfiguration.anchors.get(i).getLocation().getX();
					double x2= uwbConfiguration.anchors.get(masterIndex).getLocation().getX();
					double y1= uwbConfiguration.anchors.get(i).getLocation().getY();
					double y2= uwbConfiguration.anchors.get(masterIndex).getLocation().getY();
					double pixeldistance=Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
					uwbConfiguration.scale=pixeldistance/distance;
				}
			}
		}
		if(uwbConfiguration.scale<=0)
		{
			uwbConfiguration.scale=0;
		}
	}

	public double pixelsToMeters(double pixeldistance) {
		return pixeldistance/ uwbConfiguration.scale;
	}
	public double metersToPixels(double meters)
	{
		return meters* uwbConfiguration.scale;
	}
	public double scaledPixelsToRealPixels(double pixels)
	{
		return pixels/uwbConfiguration.imageScale;
	}
	public void consolePrint(String text)
	{
	    String originalText=jsonTextField.getText();
        if(originalText.length()>1000)
        {
            originalText= originalText.substring(0,1000);
        }
		jsonTextField.setText(text+"\n" +originalText);
	}
}
