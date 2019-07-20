package id.web.daimus.kasklas.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatRatingBar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import id.web.daimus.kasklas.R;

public class DialogUtils {
    private Context context;
    private AlertDialog.Builder builder;
    private ProgressDialog progressDialog;

    private OnClickListener onClickListener = null;
    private OnPositiveButtonClickListener onPositiveButtonClickListener = null;
    private OnNegativeButtonClickListener onNegativeButtonClickListener = null;

    private String resultText = null;

    public DialogUtils(Context context){
        this.context = context;
        initComponent();
    }

    private void initComponent(){
       builder = new AlertDialog.Builder(context);
       progressDialog = new ProgressDialog(context);
    }

    public void showNotificationDialog(String title) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (onPositiveButtonClickListener == null) return;
                onPositiveButtonClickListener.onClick();
            }
        });
        builder.show();
    }

    public void showMessageDialog(String title, String message){
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (onPositiveButtonClickListener == null) return;
                onPositiveButtonClickListener.onClick();
            }
        });
        builder.show();
    }

    public void showConfirmDialog(String title, String message) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (onPositiveButtonClickListener == null) return;
                onPositiveButtonClickListener.onClick();
            }
        });
        builder.setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onNegativeButtonClickListener == null) return;
                onNegativeButtonClickListener.onClick();
            }
        });
        builder.show();
    }

    public void showNoConnectionDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_no_connection);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        ((AppCompatButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (onClickListener == null) return;
                onClickListener.onClick();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public void showTextInputDialog(String title, String text) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_text_input);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        final TextView title_TV = (TextView) dialog.findViewById(R.id.title_TV);
        title_TV.setText(title);
        final EditText et_post = (EditText) dialog.findViewById(R.id.et_post);
        et_post.setText(text);

        ((AppCompatButton) dialog.findViewById(R.id.bt_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ((AppCompatButton) dialog.findViewById(R.id.bt_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                resultText = et_post.getText().toString().trim();
                onClickListener.onClick();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public void showProgressDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        if (!progressDialog.isShowing()){
            progressDialog.show();
        }

    }

    public void hideprogressDialog() {
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    public String getResultText() {
        return resultText;
    }

    // Interface event handler

    public void setOnClickListener (OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick();
    }

    public void setOnPositiveButtonClickListener (OnPositiveButtonClickListener onPositiveButtonClickListener){
        this.onPositiveButtonClickListener = onPositiveButtonClickListener;
    }

    public interface OnPositiveButtonClickListener {
        void onClick();
    }

    public void setOnNegativeButtonClickListener (OnNegativeButtonClickListener onNegativeButtonClickListener){
        this.onNegativeButtonClickListener = onNegativeButtonClickListener;
    }

    public interface OnNegativeButtonClickListener {
        void onClick();
    }
}
