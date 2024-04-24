import model.AuctionModel;
import model.BidModel;
import model.PackageModel;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private Socket socket;
    private String name;
    private GuiForm guiForm;

    public Client(String name) {
        try {
            this.name = name;
            //create socket connection
            socket = new Socket("localhost", 9997);
            System.out.println(name + " initialized.");

            //init gui
            SwingUtilities.invokeLater(() -> {
                guiForm = new GuiForm(new FormCallBack() { //gui callback
                    @Override
                    public void onClickBidButton(BidModel bidModel) {
                        sendBid(socket, bidModel);
                    }

                    @Override
                    public void onClickStartAuction(AuctionModel auctionModel) {
                        startAuction(socket, auctionModel);
                    }
                });
                SocketHandler socketHandler = new SocketHandler(socket);
                socketHandler.start();
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Client client = new Client("client 1");
        Client client2 = new Client("client 2");
    }

    //send created bid to server
    private void sendBid(Socket socket, BidModel bidModel) {
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(new PackageModel(false, bidModel, false));
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //send created auction to server
    private void startAuction(Socket socket, AuctionModel auctionModel) {
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(new PackageModel(true, auctionModel));
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receive(Socket socket) {
        ObjectInputStream objectInputStream;
        PackageModel packageModel;
        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            packageModel = (PackageModel) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (packageModel.isAuction()) { //send auction to gui to update gui
            guiForm.onAuctionCreated(packageModel.getAuctionModel());
        } else {
            if (packageModel.isError())
                guiForm.onBidError(packageModel.getBidModel()); //send error to gui to show error pop-up when bid is invalid.
            else
                guiForm.onBidCreated(packageModel.getBidModel()); //send bid to gui to update gui
        }
    }

    //receive multiple objects for one socket connection
    public class SocketHandler extends Thread {
        private Socket socket;

        public SocketHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            while (!socket.isClosed()) {
                receive(socket);
            }
        }
    }
}
