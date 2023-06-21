/*
 * Copyright (C) 2016 Pedro Paulo de Amorim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.folioreader.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.folioreader.Font
import com.folioreader.R

class FontAdapter : RecyclerView.Adapter<FontAdapter.ViewHolder?>() {
    private var mFonts: ArrayList<Font>? = null
    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_font, parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.mName.text = mFonts!![i].name
    }

    override fun getItemCount(): Int {
        return if (mFonts != null) mFonts!!.size else 0
    }

    fun setFonts(mFonts: ArrayList<Font>?) {
        this.mFonts = mFonts
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val mName: TextView

        init {
            mName = v.findViewById<View>(R.id.name) as TextView
        }
    }
}