package de.ebuchner.vocab.mobile.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import de.ebuchner.vocab.nui.common.I18NLocator;

public class MessageDialogs {

    public static void showMessageBox(Context context, String message) {
        showMessageBox(context, message, null);
    }

    public static void showMessageBox(Context context, String message, final MessageDialogListener listener) {
        AlertDialog ad = new AlertDialog.Builder(context).create();
        ad.setCancelable(false);
        ad.setMessage(message);
        ad.setButton(
                DialogInterface.BUTTON_POSITIVE,
                I18NLocator.locate().getString("nui.ok"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (listener != null)
                            listener.onMessageDialogClosed();
                    }
                });
        ad.show();
    }

    public static void showConfirmBox(Context context, String message, final ConfirmDialogListener listener) {
        AlertDialog ad = new AlertDialog.Builder(context).create();
        ad.setMessage(message);
        ad.setButton(
                DialogInterface.BUTTON_POSITIVE,
                I18NLocator.locate().getString("nui.ok"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listener.onConfirmAccepted();
                    }
                });
        ad.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                I18NLocator.locate().getString("nui.cancel"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listener.onConfirmCanceled();
                    }
                });
        ad.show();
    }

    public static void showInputTextDialog(Context context, String title, final InputDialogListener listener) {
        AlertDialog ad = new AlertDialog.Builder(context).create();
        ad.setTitle(title);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        ad.setView(input);

        ad.setButton(
                DialogInterface.BUTTON_POSITIVE,
                I18NLocator.locate().getString("nui.ok"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onInputOk(input.getText().toString());
                    }
                });

        ad.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                I18NLocator.locate().getString("nui.cancel"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onInputCanceled();
                    }
                });

        ad.show();
    }

    public interface InputDialogListener {
        void onInputOk(String input);

        void onInputCanceled();
    }

    public interface ConfirmDialogListener {
        void onConfirmAccepted();

        void onConfirmCanceled();
    }

    public interface MessageDialogListener {
        void onMessageDialogClosed();
    }
}
