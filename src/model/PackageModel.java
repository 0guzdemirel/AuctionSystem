package model;

import java.io.Serializable;

public class PackageModel implements Serializable { //model for sending & receiving data via socket connection
    private boolean isAuction;
    private BidModel bidModel;
    private AuctionModel auctionModel;

    private boolean isError;

    public PackageModel(boolean isAuction, AuctionModel auctionModel) {
        this.isAuction = isAuction;
        this.auctionModel = auctionModel;
    }

    public PackageModel(boolean isAuction, BidModel bidModel, boolean isError) {
        this.isAuction = isAuction;
        this.bidModel = bidModel;
        this.isError = isError;
    }

    public boolean isAuction() {
        return isAuction;
    }

    public BidModel getBidModel() {
        return bidModel;
    }

    public AuctionModel getAuctionModel() {
        return auctionModel;
    }

    public boolean isError() {
        return isError;
    }
}
