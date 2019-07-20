package id.web.daimus.kasklas.model;

import com.google.gson.annotations.SerializedName;

public class DashboardModel extends DefaultResponseModel {
    @SerializedName("income")
    Double income;
    @SerializedName("expense")
    Double expense;

    public Double getIncome() {
        return income;
    }

    public Double getExpense() {
        return expense;
    }
}
