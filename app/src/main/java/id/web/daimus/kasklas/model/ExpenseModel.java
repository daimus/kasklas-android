package id.web.daimus.kasklas.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ExpenseModel extends DefaultResponseModel {

    @SerializedName("num_rows")
    Integer numRows;
    @SerializedName("total_rows")
    Integer totalRows;
    @SerializedName("data")
    List<ExpenseListModel> data;

    public Integer getNumRows() {
        return numRows;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public List<ExpenseListModel> getData() {
        return data;
    }
}
