package id.web.daimus.kasklas.model;

public class StudentListModel {
    Integer id;
    String student_id;
    String name;
    String gender;
    String address;
    String phone;
    public boolean progress = false;

    public StudentListModel(boolean progress) {
        this.progress = progress;
    }

    public StudentListModel(Integer id, String name, String gender, String address, String phone, boolean progress){
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
}
