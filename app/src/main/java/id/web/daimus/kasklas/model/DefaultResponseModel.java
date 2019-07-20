package id.web.daimus.kasklas.model;

import com.google.gson.annotations.SerializedName;

public class DefaultResponseModel {
    @SerializedName("success")
    Boolean success;
    @SerializedName("status")
    String status;
    @SerializedName("message")
    String message;

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getSuccess(){
        return success;
    }
}
