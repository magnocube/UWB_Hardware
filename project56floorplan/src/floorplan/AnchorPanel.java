package floorplan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by Rene Schouten on 12/1/2017.
 */
public class AnchorPanel {
    public final JCheckBox fakeCheckBox;
    public JTextField anchorNameField;
    public JCheckBox masterAnchorCheckBox;
    public JButton addAnchorButton;
    public JTextField anchorDistance;
    public int anchorEditingIndex = 0;
    public int anchorsLastListClickIndex = -1;
    public JTextField editAnchorName;
    public JTextField anchorY;
    public JTextField anchorX;
    public JTextField editAnchorDistance;
    public JPanel editAnchorPanel;
    
    private Floorplan floorplan;
    public JPanel anchorJPanel=new JPanel();
    public AnchorPanel(Floorplan floorplan) {
        this.floorplan=floorplan;

        anchorJPanel.setBounds(1050, 100, 750, 300);
        anchorJPanel.setLayout(null);

        floorplan.anchorListItems = new DefaultListModel<>();
        floorplan.anchorList = new JList<>(floorplan.anchorListItems);
        floorplan.anchorList.setBounds(10, 10, 200, 250);

        

        anchorJPanel.add(floorplan.anchorList);
        floorplan.anchorList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList list = (JList) e.getSource();
                int index = list.locationToIndex(e.getPoint());
                System.out.println(index + "  " + list.getSelectedIndex());
                if (index == anchorsLastListClickIndex) {
                    list.clearSelection();
                    anchorsLastListClickIndex = -1;
                    floorplan.anchorEditCancel();
                } else {
                    floorplan.anchorEditCancel();
                    anchorEditingIndex = index;
                    anchorsLastListClickIndex = index;
                    floorplan.openAnchorEdit(index);
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
        floorplan.anchorList.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_DELETE) {

                    int index = floorplan.anchorList.getSelectedIndex();
                    if(index!=-1) {
                        floorplan.anchorListItems.remove(index);
                        floorplan.uwbConfiguration.anchors.remove(index);
                        anchorJPanel.repaint();
                    }

                }
            }
        });

        JLabel anchorAdressLabel = new JLabel("adress");
        anchorAdressLabel.setBounds(250, 0, 200, 50);
        anchorJPanel.add(anchorAdressLabel);

        anchorNameField = new JTextField();
        anchorNameField.setBounds(250, 40, 150, 50);
        anchorJPanel.add(anchorNameField);

        JLabel anchorDistanceLabel = new JLabel("distance to master");
        anchorDistanceLabel.setBounds(430, 0, 200, 50);
        anchorJPanel.add(anchorDistanceLabel);

        anchorDistance = new JTextField("0");
        anchorDistance.setBounds(430, 40, 100, 50);

        anchorJPanel.add(anchorDistance);

        masterAnchorCheckBox = new JCheckBox("master");
        masterAnchorCheckBox.setBounds(540, 40, 80, 30);
        anchorJPanel.add(masterAnchorCheckBox);

        fakeCheckBox = new JCheckBox("fake");
        fakeCheckBox.setBounds(540,70, 80, 30);
        anchorJPanel.add(fakeCheckBox);

        addAnchorButton = new JButton("add anchor");
        addAnchorButton.setBounds(630, 50, 100, 30);
        addAnchorButton.addActionListener(floorplan);
        anchorJPanel.add(addAnchorButton);
        createAnchorEditPanel();
    }
    private void createAnchorEditPanel() {
        editAnchorPanel = new JPanel();
        editAnchorPanel.setBounds(230, 100, 500, 170);
        editAnchorPanel.setBackground(Color.LIGHT_GRAY);
        anchorJPanel.add(editAnchorPanel);

        JLabel editAnchorLabel = new JLabel("edit anchor");
        editAnchorLabel.setBounds(10, 5, 100, 20);
        editAnchorPanel.add(editAnchorLabel);
        JLabel xLabel = new JLabel("x:");
        xLabel.setBounds(10, 40, 100, 20);
        editAnchorPanel.add(xLabel);
        JLabel yLabel = new JLabel("y:");
        yLabel.setBounds(140, 40, 100, 20);
        editAnchorPanel.add(yLabel);
        JLabel nameLabel = new JLabel("adress:");
        nameLabel.setBounds(10, 110, 100, 20);
        editAnchorPanel.add(nameLabel);

        anchorX = new JTextField(5);
        anchorX.setBounds(30, 30, 100, 50);
        editAnchorPanel.add (anchorX);
        anchorY = new JTextField(5);
       anchorY.setBounds(160, 30, 100, 50);
        editAnchorPanel.add (anchorY);
        editAnchorName = new JTextField(10);
        editAnchorName.setBounds(50, 100, 100, 50);
        editAnchorPanel.add (editAnchorName);

        JLabel distanceLabel = new JLabel("distance to master");
        distanceLabel.setBounds(200, 100, 100, 20);
        editAnchorPanel.add(distanceLabel);

        editAnchorDistance = new JTextField(5);
        editAnchorDistance.setBounds(200, 100, 100, 50);
        editAnchorPanel.add (editAnchorDistance);

        JButton anchorEdit = new JButton("edit");
        anchorEdit.setBounds(380, 10, 100, 50);
        anchorEdit.addActionListener(e -> floorplan.anchorEdit());
        editAnchorPanel.add(anchorEdit);
        JButton anchorCancel = new JButton("cancel");
        anchorCancel.setBounds(380, 80, 100, 50);
        anchorCancel.addActionListener(e -> floorplan.anchorEditCancel());
        editAnchorPanel.add(anchorCancel);
        editAnchorPanel.setLayout(null);
        editAnchorPanel.setVisible(false);
        floorplan.addMouseMotionListener(floorplan);
        anchorJPanel.add(editAnchorPanel);

    }
}
