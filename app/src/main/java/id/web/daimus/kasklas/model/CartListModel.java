package id.web.daimus.kasklas.model;

public class CartListModel {
    Integer id;
    String name;
    Double amount;
    Integer qty;

    public CartListModel(Integer id, String name, Double amount, Integer qty){
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.qty = qty;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
}
