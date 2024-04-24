import model.AuctionModel;
import model.BidModel;

import java.util.ArrayList;

public class Utils {
    public static String[] auctionModelArrListToStrArr(ArrayList<AuctionModel> arrayList) {
        String[] arr = new String[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            arr[i] = arrayList.get(i).getTitle();
        }
        return arr;
    }

    public static String[] bidModelArrListToStrArr(ArrayList<BidModel> arrayList) {
        String[] arr = new String[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            arr[i] = arrayList.get(i).getBidderName() + ": $" + arrayList.get(i).getBid();
        }
        return arr;
    }

    public static String secondsToTime(int totalSecond) {
        int minute = (totalSecond / 60);
        int seconds = (totalSecond % 60);

        String minAsStr, secAsStr;

        if (minute < 10) {
            minAsStr = "0" + minute;
        } else {
            minAsStr = "" + minute;
        }

        if (seconds < 10) {
            secAsStr = "0" + seconds;
        } else {
            secAsStr = "" + seconds;
        }

        return minAsStr + ":" + secAsStr;
    }
}
