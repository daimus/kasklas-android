package id.web.daimus.kasklas.model;

import java.util.ArrayList;

public class BillListModel {
    Integer id;
    String student_id;
    String name;
    String gender;
    String address;
    String phone;
    Double total_bill;
    ArrayList<DetailBillModel> bills;
    public boolean progress = false;


    public BillListModel(boolean progress) {
        this.progress = progress;
    }

    public BillListModel(Integer id, String name, String gender, String address, String phone, boolean progress){
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
        this.progress = progress;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStudentId() {
        return student_id;
    }

    public void setStudentId(String student_id) {
        this.student_id = student_id;
    }

    public Double getTotalBill() {
        return total_bill;
    }

    public void setTotalBill(Double totalBill) {
        this.total_bill = total_bill;
    }

    public ArrayList<DetailBillModel> getBills() {
        return bills;
    }

    public void setBills(ArrayList<DetailBillModel> bills) {
        this.bills = bills;
    }
}
