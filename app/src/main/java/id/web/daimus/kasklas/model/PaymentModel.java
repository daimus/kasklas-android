package id.web.daimus.kasklas.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaymentModel extends DefaultResponseModel {
    @SerializedName("total_rows")
    Integer totalRows;
    @SerializedName("num_rows")
    Integer numRows;
    @SerializedName("data")
    List<PaymentListModel> data;

    public Integer getTotalRows() {
        return totalRows;
    }

    public Integer getNumRows() {
        return numRows;
    }

    public List<PaymentListModel> getData() {
        return data;
    }
}
