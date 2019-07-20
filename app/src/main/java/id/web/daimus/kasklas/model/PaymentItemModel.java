package id.web.daimus.kasklas.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaymentItemModel extends DefaultResponseModel {
    @SerializedName("data")
    List<CartListModel> data;

    public List<CartListModel> getData() {
        return data;
    }
}
