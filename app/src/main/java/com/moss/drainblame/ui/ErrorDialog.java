package com.moss.drainblame.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.moss.drainblame.R;

/*
 *  Error popup for unsupported Android versions
 */

public class ErrorDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.error)
                .setMessage(R.string.read_permissions_error)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                //TODO: Quit the app? That seems a bit rude...
            }
        });

        return builder.create();
    }
}