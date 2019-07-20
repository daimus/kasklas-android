package id.web.daimus.kasklas.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import id.web.daimus.kasklas.R;
import id.web.daimus.kasklas.model.CartListModel;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartListModel> items;
    private OnAddItemListener onAddItemListener = null;
    private OnRemoveItemListener onRemoveItemListener = null;

    public CartAdapter(){
        items = new ArrayList<>();
    }

    public void add(CartListModel cartListModel){
        items.add(cartListModel);
        notifyItemInserted(items.size());
    }

    public void addAll(List<CartListModel> cartListModels){
        for (CartListModel cartListModel : cartListModels){
            add(cartListModel);
        }
    }

    public void remove(CartListModel cartListModel){
        int position = items.indexOf(cartListModel);
        if (position > -1) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateItem(CartListModel cartListModel, int position){
        items.set(position, cartListModel);
        notifyItemChanged(position);
    }

    public List<CartListModel> getItems() {
        return items;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CartViewHolder holder, final int position) {
        Double totalAmount = items.get(position).getQty() * items.get(position).getAmount();
        holder.name_TV.setText(items.get(position).getName());

        DecimalFormat indonesianCurrency = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols indonesianFormat = new DecimalFormatSymbols();

        indonesianFormat.setCurrencySymbol("Rp");
        indonesianFormat.setMonetaryDecimalSeparator(',');
        indonesianFormat.setGroupingSeparator('.');
        indonesianCurrency.setDecimalFormatSymbols(indonesianFormat);

        holder.totalAmount_TV.setText(String.valueOf(indonesianCurrency.format(totalAmount)));

        holder.qty_TV.setText(String.valueOf(items.get(position).getQty()));

        holder.add_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onAddItemListener == null) return;
                onAddItemListener.onClick(items.get(position), position);
            }
        });
        holder.remove_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRemoveItemListener == null) return;
                onRemoveItemListener.onClick(items.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (items != null) ? items.size() : 0;
    }

    public class CartViewHolder extends RecyclerView.ViewHolder{
        private TextView name_TV, totalAmount_TV, qty_TV;
        private ImageButton add_IB, remove_IB;

        public CartViewHolder(View itemView) {
            super(itemView);
            name_TV = (TextView) itemView.findViewById(R.id.name_TV);
            totalAmount_TV = (TextView) itemView.findViewById(R.id.totalAmount_TV);
            qty_TV = (TextView) itemView.findViewById(R.id.qty_TV);
            add_IB = (ImageButton) itemView.findViewById(R.id.add_IB);
            remove_IB = (ImageButton) itemView.findViewById(R.id.remove_IB);
        }
    }

    public void setOnAddItemListener (OnAddItemListener onAddItemListener){
        this.onAddItemListener = onAddItemListener;
    }

    public void setOnRemoveItemListener (OnRemoveItemListener onRemoveItemListener){
        this.onRemoveItemListener = onRemoveItemListener;
    }

    public interface OnAddItemListener{
        void onClick(CartListModel obj, int position);
    }

    public interface OnRemoveItemListener{
        void onClick(CartListModel obj, int position);
    }
}
