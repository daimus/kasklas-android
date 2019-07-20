package id.web.daimus.kasklas.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BillModel extends DefaultResponseModel {
    @SerializedName("num_rows")
    Integer numRows;
    @SerializedName("total_rows")
    Integer totalRows;
    @SerializedName("data")
    List<BillListModel> data;

    public Integer getNumRows() {
        return numRows;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public List<BillListModel> getData() {
        return data;
    }
}
