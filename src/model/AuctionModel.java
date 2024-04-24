package model;

import java.io.Serializable;
import java.util.ArrayList;

public class AuctionModel implements Serializable {
    private int id;
    private String title;
    private ArrayList<BidModel> bidList;
    private int tourRemainingSecond = 60;
    private String initialPrice;

    private boolean isSelected;

    public AuctionModel(String title, String initialPrice) {
        this.title = title;
        this.initialPrice = initialPrice;
        bidList = new ArrayList<>();
        bidList.add(new BidModel(id, "Initial Price", "" + initialPrice));
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<BidModel> getBidList() {
        return bidList;
    }

    public int getTourRemainingSecond() {
        return tourRemainingSecond;
    }

    public void setTourRemainingSecond(int tourRemainingSecond) {
        this.tourRemainingSecond = tourRemainingSecond;
    }

    public String getInitialPrice() {
        return initialPrice;
    }

    @Override
    public String toString() {
        return "AuctionModel{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", bidList=" + bidList +
                ", tourRemainingSecond=" + tourRemainingSecond +
                ", initialPrice='" + initialPrice + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
