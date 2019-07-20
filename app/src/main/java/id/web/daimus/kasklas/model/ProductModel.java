package id.web.daimus.kasklas.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductModel extends DefaultResponseModel {
    @SerializedName("total_rows")
    Integer totalRows;
    @SerializedName("num_rows")
    Integer numRows;
    @SerializedName("data")
    List<ProductListModel> data;

    public Integer getTotalRows() {
        return totalRows;
    }

    public Integer getNumRows() {
        return numRows;
    }

    public List<ProductListModel> getData() {
        return data;
    }
}
