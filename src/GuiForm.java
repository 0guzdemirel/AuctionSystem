import model.AuctionModel;
import model.BidModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class GuiForm extends JFrame {

    private FormCallBack formCallBack;

    private JTextField createdAuctionNameTextField;
    private JButton createAuctionButton;
    private JList<String> bidderList;
    private JList<String> auctionList;

    private JTextField bidderNameTextField;
    private JTextField bidTextField;
    private JButton bidButton;
    private JTextArea auctionStatusTextArea;
    private JTextArea countdownTextArea;
    private JTextArea latestBidTextArea;
    private JPanel auctionPanel;
    private JLabel selectedAuctionTitleLabel;
    private JTextField initialPriceTextField;

    ArrayList<AuctionModel> auctionArrayList = new ArrayList<>();
    ArrayList<Thread> countdownThreadList = new ArrayList<>();

    public GuiForm(FormCallBack formCallBack) {
        this.formCallBack = formCallBack;
        setTitle("Auction System");
        setContentPane(auctionPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setVisible(true);
        validateNumberInputAreas();
        setListeners();
    }

    private void setListeners() {
        bidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isValidBid()) {
                    //notify client and send created bid by callback
                    formCallBack.onClickBidButton(
                            new BidModel(
                                    auctionArrayList.get(auctionList.getSelectedIndex()).getId(),
                                    bidderNameTextField.getText(),
                                    bidTextField.getText()
                            ));
                    //reset input areas
                    bidTextField.setText("");
                }
            }
        });

        createAuctionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isValidAuction()) {
                    //notify client and send created auction by callback
                    formCallBack.onClickStartAuction(
                            new AuctionModel(
                                    createdAuctionNameTextField.getText(),
                                    initialPriceTextField.getText()
                            ));
                    //reset input areas
                    createdAuctionNameTextField.setText("");
                    initialPriceTextField.setText("");
                }
            }
        });

        auctionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (auctionList.getSelectedIndex() != -1) {

                    //set selected auction for countdown textarea
                    for (int i = 0; i < auctionArrayList.size(); i++) {
                        auctionArrayList.get(i).setSelected(false);
                    }
                    auctionArrayList.get(auctionList.getSelectedIndex()).setSelected(true);

                    //set last bid textarea (bidder name and bid amount)
                    setLastBidView(auctionArrayList.get(auctionList.getSelectedIndex()).getTourRemainingSecond());
                    //set countdown text
                    countdownTextArea.setText(Utils.secondsToTime(auctionArrayList.get(auctionList.getSelectedIndex()).getTourRemainingSecond()));
                    //set auction title
                    selectedAuctionTitleLabel.setText(auctionArrayList.get(auctionList.getSelectedIndex()).getTitle());
                    //set bid list
                    bidderList.setListData(Utils.bidModelArrListToStrArr(auctionArrayList.get(auctionList.getSelectedIndex()).getBidList()));
                }
            }
        });
    }

    //starts countdown for auction
    private Thread getCountdown(int second, int index) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int totalSec = second;
                while (totalSec > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    totalSec = totalSec - 1;
                    onTick(totalSec, index);
                }
            }
        });
        thread.start();
        return thread;
    }

    //operations on tick of countdown
    private void onTick(int remainingSecond, int index) {
        //decrease auction remaining second by one on tick
        auctionArrayList.get(index).setTourRemainingSecond(remainingSecond);

        //set selected auction remaining second and latest bid on tick
        if (auctionArrayList.get(index).isSelected()) {
            countdownTextArea.setText(Utils.secondsToTime(auctionArrayList.get(auctionList.getSelectedIndex()).getTourRemainingSecond()));
            setLastBidView(remainingSecond);
        }
    }

    //sets auction status and current bid info textareas
    private void setLastBidView(int remainingSecond) {
        try {
            if (remainingSecond < 1) { //if auction is finished then set auction status as finished and winner bidder info.
                auctionStatusTextArea.setText("Auction Finished");
                latestBidTextArea.setText(
                        "WINNER\n" +
                                "Name: " + auctionArrayList.get(auctionList.getSelectedIndex()).getBidList().get(auctionArrayList.get(auctionList.getSelectedIndex()).getBidList().size() - 1).getBidderName()
                                + "\n" +
                                "Bid: $" + auctionArrayList.get(auctionList.getSelectedIndex()).getBidList().get(auctionArrayList.get(auctionList.getSelectedIndex()).getBidList().size() - 1).getBid()
                );
            } else { //if auction is in progress then set auction status as in progress and current bidder info.
                auctionStatusTextArea.setText("Auction In Progress");
                latestBidTextArea.setText(
                        "CURRENT BID\n" +
                                "Name: " + auctionArrayList.get(auctionList.getSelectedIndex()).getBidList().get(auctionArrayList.get(auctionList.getSelectedIndex()).getBidList().size() - 1).getBidderName()
                                + "\n" +
                                "Bid: $" + auctionArrayList.get(auctionList.getSelectedIndex()).getBidList().get(auctionArrayList.get(auctionList.getSelectedIndex()).getBidList().size() - 1).getBid()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onAuctionCreated(AuctionModel auctionModel) {
        //add auction to auction list and set JList data to update JList
        auctionArrayList.add(auctionModel);
        auctionList.setListData(Utils.auctionModelArrListToStrArr(auctionArrayList));
        //set auction selected when created
        auctionList.setSelectedIndex(auctionArrayList.size() - 1);
        //start countdown for created auction
        countdownThreadList.add(getCountdown(auctionModel.getTourRemainingSecond(), auctionArrayList.size() - 1));
    }

    public void onBidCreated(BidModel bidModel) {
        //check auctions to detect which auction that bid made for and add bid to bid list of that auction.
        for (int i = 0; i < auctionArrayList.size(); i++) {
            if (auctionArrayList.get(i).getId() == bidModel.getAuctionId()) {
                auctionArrayList.get(i).getBidList().add(bidModel);
            }
        }
        //update JList
        bidderList.setListData(Utils.bidModelArrListToStrArr(auctionArrayList.get(auctionList.getSelectedIndex()).getBidList()));
    }

    public void onBidError(BidModel bidModel) {
        //show error pop up when users make bid too fast for same auction in small time period.
        if (auctionArrayList.get(auctionList.getSelectedIndex()).getId() == bidModel.getAuctionId()) {
            showErrorPopUp(
                    "Error",
                    "Bid denied. You should wait at least 2 seconds after last bid to make another bid.\n" +
                            "Bidder Name: " + bidModel.getBidderName() + "\n" +
                            "Bid: $" + bidModel.getBid()
            );
        }
    }

    //checks auction whether is valid or not for creation
    private boolean isValidAuction() {
        if (createdAuctionNameTextField.getText().isEmpty() || initialPriceTextField.getText().isEmpty()) {
            showErrorPopUp(
                    "Error",
                    "Fill all required input areas to start auction."
            );
        } else {
            return true;
        }
        return false;
    }

    //checks bid whether is valid or not for creation
    private boolean isValidBid() {
        if (auctionArrayList.isEmpty()) {
            showErrorPopUp(
                    "Error",
                    "To bid you should select an auction first."
            );
        } else if (auctionArrayList.get(auctionList.getSelectedIndex()).getTourRemainingSecond() < 1) { // if auction is finished
            showErrorPopUp(
                    "Error",
                    "You can not bid for a finished auction."
            );
        } else if (bidderNameTextField.getText().isEmpty() || bidTextField.getText().isEmpty()) { // if input areas invalid
            showErrorPopUp(
                    "Error",
                    "Fill all required input areas to make bid."
            );
        } else if (Integer.parseInt(bidTextField.getText()) <= Integer.parseInt(auctionArrayList.get(auctionList.getSelectedIndex()).getBidList().get(auctionArrayList.get(auctionList.getSelectedIndex()).getBidList().size() - 1).getBid())) { // if bid amount less than latest bid.
            showErrorPopUp(
                    "Error",
                    "Bid amount must be higher than the latest bid."
            );
        } else {
            return true;
        }
        return false;
    }

    //only numeric inputs for auction price and bid amount input areas
    private void validateNumberInputAreas() {
        initialPriceTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!"0123456789".contains("" + e.getKeyChar()))
                    initialPriceTextField.setText("");
            }
        });

        bidTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!"0123456789".contains("" + e.getKeyChar()))
                    bidTextField.setText("");
            }
        });
    }

    private void showErrorPopUp(String title, String msg) {
        JOptionPane.showMessageDialog(
                new JFrame(),
                msg,
                title,
                JOptionPane.ERROR_MESSAGE);
    }
}
