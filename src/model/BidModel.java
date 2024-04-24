package model;

import java.io.Serializable;

public class BidModel implements Serializable {
    private int auctionId;
    private String bidderName;
    private String bid;

    public BidModel(int auctionId, String bidderName, String bid) {
        this.auctionId = auctionId;
        this.bidderName = bidderName;
        this.bid = bid;
    }

    public String getBidderName() {
        return bidderName;
    }

    public String getBid() {
        return bid;
    }

    @Override
    public String toString() {
        return "BidModel{" +
                "auctionId=" + auctionId +
                ", bidderName='" + bidderName + '\'' +
                ", bid='" + bid + '\'' +
                '}';
    }

    public int getAuctionId() {
        return auctionId;
    }
}
