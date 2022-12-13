package com.hxl.android.xiaoan.word.adapter

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.hxl.android.xiaoan.word.R
import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.utils.SharedPreferencesUtils


class WordExpandableListAdapter(
    private val context: Context,
    var expandableListTitle: List<String> = mutableListOf(),
    var expandableListDetail: MutableMap<String, MutableList<WordBean>> = mutableMapOf()
) : BaseExpandableListAdapter() {

    var meanVisibleState: () -> Boolean = { true }
    var wordVisibleState: () -> Boolean = { true }

    var playVoice: (WordBean) -> Unit = {}

    val visibleWords = mutableMapOf<Int, Boolean>()
    fun setData(titles: List<String>, details: MutableMap<String, MutableList<WordBean>>) {
        this.expandableListTitle = titles
        this.expandableListDetail = details

    }


    override fun getChild(listPosition: Int, expandedListPosition: Int): Any? {
        return expandableListDetail[expandableListTitle[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    private fun bindVisible(view: View, fn: () -> Boolean) {
        view.isVisible = fn.invoke()
    }

    override fun getChildView(
        listPosition: Int, expandedListPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup?
    ): View {
        var convertView = convertView
        val expandedListText = getChild(listPosition, expandedListPosition) as WordBean?
        if (convertView == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_item, parent, false)
        }
        convertView!!.findViewById<ImageView>(R.id.iv_voice)
            .setOnClickListener { playVoice.invoke(expandedListText!!) }
        convertView.findViewById<TextView>(R.id.tv_word).text = expandedListText!!.wordName
        convertView.findViewById<TextView>(R.id.tv_mean).text = expandedListText.wordMean

        bindVisible(convertView.findViewById<TextView>(R.id.tv_word), wordVisibleState)
        //如没有开启单击显示，则将显示开关绑定到meanVisibleState
        if (!SharedPreferencesUtils.getBoolean("click_show_flag")) bindVisible(
            convertView.findViewById<TextView>(R.id.tv_mean), meanVisibleState
        ) else {
            //如果visibleWords显示状态是true
            val visible = visibleWords.getOrDefault(expandedListText.id, false)
            convertView.findViewById<TextView>(R.id.tv_mean).isVisible = visible
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