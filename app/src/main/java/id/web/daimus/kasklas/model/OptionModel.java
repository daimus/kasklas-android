package id.web.daimus.kasklas.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OptionModel extends DefaultResponseModel {
    @SerializedName("data")
    ArrayList<OptionListModel> data;

    public ArrayList<OptionListModel> getData() {
        return data;
    }
}
