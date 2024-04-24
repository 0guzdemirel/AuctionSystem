import model.AuctionModel;
import model.BidModel;

public interface FormCallBack {
    void onClickBidButton(BidModel bidModel);
    void onClickStartAuction(AuctionModel auctionModel);
}
