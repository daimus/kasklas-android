package id.web.daimus.kasklas.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import id.web.daimus.kasklas.R;
import id.web.daimus.kasklas.model.ExpenseListModel;

public class ExpenseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;

    private int item_per_display = 0;
    private int current_selected_idx = -1;
    private List<ExpenseListModel> items = new ArrayList<>();
    boolean loading;
    private OnLoadMoreListener onLoadMoreListener = null;
    private OnClickListener onClickListener = null;

    private Context ctx;
    private SparseBooleanArray selectedItems;

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    public ExpenseAdapter(Context context, int item_per_display) {
        this.items = items;
        this.item_per_display = item_per_display;
        ctx = context;
        selectedItems = new SparseBooleanArray();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        ImageView icon_IV;
        TextView name_TV, description_TV, timestamp_TV, amount_TV;
        View parent_LL;
        public OriginalViewHolder(View v) {
            super(v);
            icon_IV = (ImageView) v.findViewById(R.id.icon_IV);
            name_TV = (TextView) v.findViewById(R.id.name_TV);
            description_TV = (TextView) v.findViewById(R.id.description_TV);
            timestamp_TV = (TextView) v.findViewById(R.id.timestamp_TV);
            amount_TV = (TextView) v.findViewById(R.id.amount_TV);
            parent_LL = (View) v.findViewById(R.id.parent_LL);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progress_bar;

        public ProgressViewHolder(View v) {
            super(v);
            progress_bar = (ProgressBar) v.findViewById(R.id.progress_bar);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
            vh = new OriginalViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ExpenseListModel item = items.get(position);
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;
            view.name_TV.setText(item.getName());
            view.description_TV.setText(item.getDescription());
            view.timestamp_TV.setText(item.getTimestamp());

            DecimalFormat indonesianCurrency = (DecimalFormat) DecimalFormat.getCurrencyInstance();
            DecimalFormatSymbols indonesianFormat = new DecimalFormatSymbols();

            indonesianFormat.setCurrencySymbol("Rp");
            indonesianFormat.setMonetaryDecimalSeparator(',');
            indonesianFormat.setGroupingSeparator('.');
            indonesianCurrency.setDecimalFormatSymbols(indonesianFormat);

            view.amount_TV.setText(String.valueOf(indonesianCurrency.format(item.getAmount())));

            view.parent_LL.setActivated(selectedItems.get(position, false));

            view.parent_LL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener == null) return;
                    onClickListener.onItemClick(v, item, position);
                }
            });

            view.parent_LL.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onClickListener == null) return false;
                    onClickListener.onItemLongClick(v, item, position);
                    return true;
                }
            });
        } else {
            ((ProgressViewHolder) holder).progress_bar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.items.get(position).progress ? VIEW_PROGRESS : VIEW_ITEM;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        lastItemViewDetector(recyclerView);
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insertData(List<ExpenseListModel> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i).progress) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {

            this.items.add(new ExpenseListModel(true));
            notifyItemInserted(getItemCount() - 1);

        }
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void toggleSelection(int pos) {
        current_selected_idx = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public SparseBooleanArray getSelectedItem(){
        return selectedItems;
    }

    public ExpenseListModel getItem(int pos){
        return items.get(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }


    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    if (!loading) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0) {
                            loading = true;
                            int current_page = getItemCount() / item_per_display;
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    public interface OnClickListener {
        void onItemClick(View view, ExpenseListModel obj, int pos);

        void onItemLongClick(View view, ExpenseListModel obj, int pos);
    }
}