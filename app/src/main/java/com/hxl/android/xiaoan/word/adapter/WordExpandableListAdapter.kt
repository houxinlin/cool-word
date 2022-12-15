package com.hxl.android.xiaoan.word.adapter

import android.content.Context
import android.content.SharedPreferences
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.hxl.android.xiaoan.word.R
import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.bean.meanKey
import com.hxl.android.xiaoan.word.bean.wordKey
import com.hxl.android.xiaoan.word.utils.SharedPreferencesUtils


class WordExpandableListAdapter(
    private val context: Context,
    var expandableListTitle: List<String> = mutableListOf(),
    var expandableListDetail: MutableMap<String, MutableList<WordBean>> = mutableMapOf()
) : BaseExpandableListAdapter() {

    var meanVisibleState: () -> Boolean = { true }
    var wordVisibleState: () -> Boolean = { true }

    var playVoice: (WordBean) -> Unit = {}
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private val visibleWords = mutableMapOf<String, Boolean>()


    private var clickSync = sharedPreferences.getBoolean("click_sync", true)
    fun reverseVisible(key: String) {
        visibleWords[key] = !visibleWords.getOrDefault(key, false)
    }

    fun setData(titles: List<String>, details: MutableMap<String, MutableList<WordBean>>) {
        this.expandableListTitle = titles
        this.expandableListDetail = details

    }


    override fun getChild(listPosition: Int, expandedListPosition: Int): WordBean {
        return expandableListDetail[expandableListTitle[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int, expandedListPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup?
    ): View {
        var convertView = convertView
        val wordBean = getChild(listPosition, expandedListPosition)

        if (convertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_item, parent, false)
        }
        convertView!!.findViewById<ImageView>(R.id.iv_voice)
            .setOnClickListener { playVoice.invoke(wordBean) }

        convertView.findViewById<TextView>(R.id.tv_word).apply {
            text = wordBean.wordName
            isVisible = wordVisibleState.invoke()

            if (clickSync) {
                if (visibleWords.getOrDefault(wordBean.wordKey(), false)) {
                    isVisible = true
                }
            }
        }

        convertView.findViewById<TextView>(R.id.tv_mean).apply {
            text = wordBean.wordMean
            isVisible = meanVisibleState.invoke()
            if (clickSync) {
                if (visibleWords.getOrDefault(wordBean.meanKey(), false)) {
                    isVisible = true

                }
            }
        }
        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return expandableListDetail[expandableListTitle[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any? {
        return expandableListTitle[listPosition]
    }

    override fun getGroupCount(): Int {
        return expandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertViewCache: View?, parent: ViewGroup?
    ): View? {
        var convertView = convertViewCache
        val listTitle = getGroup(listPosition) as String?
        if (convertView == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_group, parent, false)
        }

        convertView!!.findViewById<TextView>(R.id.tv_name).text =
            "$listTitle (${expandableListDetail[listTitle]!!.size})"
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}