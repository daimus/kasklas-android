package id.web.daimus.kasklas.api;

import java.util.ArrayList;
import java.util.HashMap;

import id.web.daimus.kasklas.model.BillModel;
import id.web.daimus.kasklas.model.DashboardModel;
import id.web.daimus.kasklas.model.DefaultResponseModel;
import id.web.daimus.kasklas.model.ExpenseModel;
import id.web.daimus.kasklas.model.LoginModel;
import id.web.daimus.kasklas.model.OptionModel;
import id.web.daimus.kasklas.model.PaymentItemModel;
import id.web.daimus.kasklas.model.PaymentModel;
import id.web.daimus.kasklas.model.ProductModel;
import id.web.daimus.kasklas.model.StudentModel;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {
    @FormUrlEncoded
    @POST("auth/validateSession")
    Call<DefaultResponseModel> validateSession(
            @Field("hash") String hash,
            @Field("version") Integer version
    );

    @FormUrlEncoded
    @POST("auth/signin")
    Call<LoginModel> signin(
            @Field("username") String username,
            @Field("password") String password
    );

    @GET("api/getOption")
    Call<OptionModel> getOption();

    @FormUrlEncoded
    @POST("api/saveOption")
    Call<DefaultResponseModel> saveOption(
      @Field("code") Integer code,
      @Field("value") String value
    );

    @GET("api/dashboard")
    Call<DashboardModel> dashboard();

    @FormUrlEncoded
    @POST("api/fetchBill")
    Call<BillModel> fetchBill(
            @Field("start") Integer start,
            @Field("length") Integer length,
            @Field("filter_search_query") String filterSearchQuery
    );

    @FormUrlEncoded
    @POST("api/fetchStudent")
    Call<StudentModel> fetchStudent(
        @Field("start") Integer start,
        @Field("length") Integer length,
        @Field("filter_search_query") String filterSearchQuery
    );

    @FormUrlEncoded
    @POST("api/addStudent")
    Call<DefaultResponseModel> addStudent(
        @Field("student_id") String studentId,
        @Field("name") String name,
        @Field("gender") String gender,
        @Field("address") String address,
        @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("api/editStudent")
    Call<DefaultResponseModel> editStudent(
            @Field("id") Integer id,
            @Field("student_id") String studentId,
            @Field("name") String name,
            @Field("gender") String gender,
            @Field("address") String address,
            @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("api/deleteStudent")
    Call<DefaultResponseModel> deleteStudent(
            @Field("id") Integer id
    );

    // PAYMENT API
    @FormUrlEncoded
    @POST("api/fetchPayment")
    Call<PaymentModel> fetchPayment(
            @Field("start") Integer start,
            @Field("length") Integer length,
            @Field("filter_student_id") Integer filterStudentId
    );

    @FormUrlEncoded
    @POST("api/addPayment")
    Call<DefaultResponseModel> addPayment(
            @Field("student_id") Integer studentId,
            @Field("product_id[]") ArrayList<Integer> productId,
            @Field("qty[]") ArrayList<Integer> qty
    );

    @FormUrlEncoded
    @POST("api/editPayment")
    Call<DefaultResponseModel> editPayment(
            @Field("student_id") Integer studentId,
            @Field("payment_id") Integer id,
            @Field("product_id[]") ArrayList<Integer> productId,
            @Field("qty[]") ArrayList<Integer> qty
            );

    @FormUrlEncoded
    @POST("api/deletePayment")
    Call<DefaultResponseModel> deletePayment(
            @Field("payment_id") Integer id
    );

    @FormUrlEncoded
    @POST("api/fetchPaymentItem")
    Call<PaymentItemModel> loadPaymentItem(
            @Field("payment_id") Integer id
    );

    // PRODUCT API
    @FormUrlEncoded
    @POST("api/fetchProduct")
    Call<ProductModel> fetchProduct(
            @Field("start") Integer start,
            @Field("length") Integer length,
            @Field("filter_search_query") String filterSearchQuery,
            @Field("filter_removable") Integer removable
    );

    @FormUrlEncoded
    @POST("api/addProduct")
    Call<DefaultResponseModel> addProduct(
            @Field("name") String name,
            @Field("period") String period,
            @Field("amount") Double amount
    );

    @FormUrlEncoded
    @POST("api/editProduct")
    Call<DefaultResponseModel> editProduct(
            @Field("id") Integer id,
            @Field("name") String name,
            @Field("period") String period,
            @Field("amount") Double amount
    );

    @FormUrlEncoded
    @POST("api/deleteProduct")
    Call<DefaultResponseModel> deleteProduct(
            @Field("id") Integer id
    );

    // EXPENSE API
    @FormUrlEncoded
    @POST("api/fetchExpense")
    Call<ExpenseModel> fetchExpense(
            @Field("start") Integer start,
            @Field("length") Integer length,
            @Field("filter_search_query") String filterSearchQuery
    );

    @FormUrlEncoded
    @POST("api/addExpense")
    Call<DefaultResponseModel> addExpense(
            @Field("name") String name,
            @Field("description") String description,
            @Field("amount") Double amount
    );

    @FormUrlEncoded
    @POST("api/editExpense")
    Call<DefaultResponseModel> editExpense(
            @Field("id") Integer id,
            @Field("name") String name,
            @Field("description") String description,
            @Field("amount") Double amount
    );

    @FormUrlEncoded
    @POST("api/deleteExpense")
    Call<DefaultResponseModel> deleteExpense(
            @Field("id") Integer id
    );

}
