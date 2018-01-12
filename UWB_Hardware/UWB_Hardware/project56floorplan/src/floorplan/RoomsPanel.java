package floorplan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Rene Schouten on 12/1/2017.
 */
public class RoomsPanel {
    public JList<String> roomsList;
    public int roomsLastListClickIndex=-1;
    public int roomsEditingIndex=0;
    public JTextField roomNameField;
    public JButton addRoomButton;
 

    public JPanel editRoomPanel;
    public JTextField editRoomName;
    public JTextField roomY;
    public JTextField roomX;
    
    public Floorplan floorplan;
    
    public JPanel roomsJPanel;
    
    public RoomsPanel(Floorplan floorplan) {
        this.floorplan=floorplan;

        roomsJPanel =new JPanel();
        roomsJPanel.setBounds(1050, 420, 800, 300);
        roomsJPanel.setLayout(null);

        floorplan.roomsListItems = new DefaultListModel<>();
        roomsList = new JList<>(floorplan.roomsListItems);
        roomsList.setBounds(10,10, 200, 300);
        roomsJPanel.add(roomsList);
        roomsList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList list = (JList) e.getSource();
                int index = list.locationToIndex(e.getPoint());
                System.out.println(index + "  " + list.getSelectedIndex());
                if (index == roomsLastListClickIndex) {
                    list.clearSelection();
                    roomsLastListClickIndex = -1;
                    floorplan.roomsEditCancel();
                } else {
                    floorplan.roomsEditCancel();
                    roomsEditingIndex = index;
                    roomsLastListClickIndex = index;
                    floorplan.openRoomEdit(index);
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
        });
        roomsList.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_DELETE) {
                    int index = roomsList.getSelectedIndex();
                    floorplan.roomsListItems.remove(index);
                    floorplan.uwbConfiguration.rooms.remove(index);
                    floorplan.repaint();
                }
            }
        });

        JLabel roomNameLabel = new JLabel("room name");
        roomNameLabel.setBounds(250, 0, 200, 50);
        roomsJPanel.add(roomNameLabel);

        roomNameField = new JTextField();
        roomNameField.setBounds(250, 40, 200, 50);
        roomsJPanel.add(roomNameField);


        addRoomButton = new JButton("add room");
        addRoomButton.setBounds(500, 40, 100, 30);
        addRoomButton.addActionListener(floorplan);
        roomsJPanel.add(addRoomButton);
        createRoomEditPanel();


    }
    public void createRoomEditPanel() {
        editRoomPanel = new JPanel();
        editRoomPanel.setBounds(1300, 550, 500, 170);
        editRoomPanel.setBackground(Color.LIGHT_GRAY);
        roomsJPanel.add(editRoomPanel);

        JLabel editRoomLabel = new JLabel("edit room");
        editRoomLabel.setBounds(10, 5, 100, 20);
        editRoomPanel.add(editRoomLabel);
        JLabel xLabel = new JLabel("x:");
        xLabel.setBounds(10, 40, 100, 20);
        editRoomPanel.add(xLabel);
        JLabel yLabel = new JLabel("y:");
        yLabel.setBounds(140, 40, 100, 20);
        editRoomPanel.add(yLabel);
        JLabel nameLabel = new JLabel("name:");
        nameLabel.setBounds(10, 110, 100, 20);
        editRoomPanel.add(nameLabel);

        roomX = new JTextField(5);
        roomX.setBounds(30, 30, 100, 50);
        editRoomPanel.add(roomX);
        roomY = new JTextField(5);
        roomY.setBounds(160, 30, 100, 50);
        editRoomPanel.add(roomY);
        editRoomName = new JTextField(10);
        editRoomName.setBounds(50, 100, 200, 50);
        editRoomPanel.add(editRoomName);
        JButton anchorEdit = new JButton("edit");
        anchorEdit.setBounds(280, 10, 100, 50);
        anchorEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //roomEdit();
            }
        });
        editRoomPanel.add(anchorEdit);
        JButton roomCancel = new JButton("cancel");
        roomCancel.setBounds(280, 80, 100, 50);
        roomCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //roomEditCancel();
            }
        });



        editRoomPanel.add(roomCancel);
        editRoomPanel.setLayout(null);
        editRoomPanel.setVisible(false);
        floorplan.addMouseMotionListener(floorplan);
    }
}
