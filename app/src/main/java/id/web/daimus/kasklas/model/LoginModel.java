package id.web.daimus.kasklas.model;

import com.google.gson.annotations.SerializedName;

public class LoginModel extends DefaultResponseModel {
    @SerializedName("hash")
    String hash;
    @SerializedName("user")
    String user;

    public String getHash() {
        return hash;
    }

    public String getUser() {
        return user;
    }
}
