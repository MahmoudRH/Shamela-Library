package com.folioreader.util

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.TextView
import com.folioreader.R

object ProgressDialog {
    fun show(ctx: Context?, text: String?): Dialog {
        val dialog = Dialog(ctx!!, R.style.full_screen_dialog)
        dialog.setContentView(R.layout.progress_dialog)
        (dialog.findViewById<View>(R.id.label_loading) as TextView).text = text
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}