package id.web.daimus.kasklas.model;

public class PaymentListModel {

    Integer payment_id;
    String payment_no;
    Integer student_id;
    Double amount;
    String timestamp;
    String name;

    public boolean progress = false;

    public PaymentListModel(boolean progress) {
        this.progress = progress;
    }

    public Integer getPaymentId() {
        return payment_id;
    }

    public void setPaymentId(Integer payment_id) {
        this.payment_id = payment_id;
    }

    public String getPaymentNo() {
        return payment_no;
    }

    public void setPaymentNo(String payment_no) {
        this.payment_no = payment_no;
    }

    public Integer getStudentId() {
        return student_id;
    }

    public void setStudentId(Integer studentId) {
        student_id = studentId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
