import model.AuctionModel;
import model.BidModel;
import model.PackageModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class Server {

    private ArrayList<AuctionModel> auctions;
    private ArrayList<Socket> clients;
    ServerSocket serverSocket;

    private final int MIN_TIME_PERIOD_BETWEEN_TWO_BID = 5000;

    public Server() {
        try {
            //create server socket
            serverSocket = new ServerSocket(9997);
            System.out.println("Server initialized. Waiting for clients.");
            auctions = new ArrayList<>();
            clients = new ArrayList<>();
            while (true) {
                //accept connection with clients
                Socket socket = serverSocket.accept();
                System.out.println("Connection established.");
                clients.add(socket);
                SocketHandler socketHandler = new SocketHandler(socket);
                socketHandler.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
    }


    private long receivedTime;
    private int latestReceivedBidAuctionId;

    private void receive(Socket socket) {
        ObjectInputStream objectInputStream;
        PackageModel packageModel;
        try { // receive package object using ObjectInputStream
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            packageModel = (PackageModel) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (packageModel.isAuction()) { // set received auction id, add auction to the list and notify clients
            packageModel.getAuctionModel().setId(getUniqueAuctionId());
            auctions.add(packageModel.getAuctionModel());
            notifyClient(packageModel.getAuctionModel());
        } else { // check minimum time period between two bid and add bid to bidList of its auction if it is valid then notify clients
            if ((new Date()).getTime() - receivedTime < MIN_TIME_PERIOD_BETWEEN_TWO_BID && latestReceivedBidAuctionId == packageModel.getBidModel().getAuctionId()) {
                notifyClient(packageModel.getBidModel(), true);
            } else {
                for (int i = 0; i < auctions.size(); i++) {
                    if (auctions.get(i).getId() == packageModel.getBidModel().getAuctionId()) {
                        latestReceivedBidAuctionId = packageModel.getBidModel().getAuctionId();
                        auctions.get(i).getBidList().add(packageModel.getBidModel());
                        notifyClient(packageModel.getBidModel(), false);
                    }
                }
            }
            receivedTime = (new Date()).getTime();
        }
    }

    //sends received auctions to clients
    private void notifyClient(AuctionModel auctionModel) {
        ObjectOutputStream objectOutputStream;
        try {
            for (int i = 0; i < clients.size(); i++) {
                objectOutputStream = new ObjectOutputStream(clients.get(i).getOutputStream());
                objectOutputStream.writeObject(new PackageModel(true, auctionModel));
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //sends received bids to clients
    private void notifyClient(BidModel bidModel, boolean isError) {
        ObjectOutputStream objectOutputStream = null;
        try {
            for (int i = 0; i < clients.size(); i++) {
                objectOutputStream = new ObjectOutputStream(clients.get(i).getOutputStream());
                objectOutputStream.writeObject(new PackageModel(false, bidModel, isError));
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    //generates unique id for auction and returns it
    private int getUniqueAuctionId() {
        boolean isDuplicate = true;
        int id = 0;

        while (isDuplicate) {
            id = (int) (Math.random() * 10000);
            isDuplicate = false;
            for (int i = 0; i < auctions.size(); i++) {
                if (auctions.get(i).getId() == id) {
                    isDuplicate = true;
                    break;
                }
            }
        }
        return id;
    }
}
