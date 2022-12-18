package com.hxl.android.xiaoan.word.ui.widget.base

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.hxl.android.xiaoan.word.R

class BaseProgressDialog(val context: Context) {
    private var dialog: CustomDialog
    fun start() {
        dialog.show()
        val size =context.resources.getDimensionPixelSize(R.dimen.loading_size)
        dialog.window?.setLayout(size,size)
    }

    fun stop() {
        dialog.dismiss()
    }

    init {
        val inflater = (context as Activity).layoutInflater
        val view = inflater.inflate(R.layout.dialog_loading, null)

        dialog = CustomDialog(context)
        dialog.setContentView(view)
    }
    class CustomDialog(context: Context) : Dialog(context, R.style.CustomDialogTheme) {
        init {

        }
    }
}