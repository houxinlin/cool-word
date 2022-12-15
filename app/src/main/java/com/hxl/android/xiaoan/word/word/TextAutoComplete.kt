package com.hxl.android.xiaoan.word.word

import android.text.Editable
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import com.hxl.android.xiaoan.word.R
import com.hxl.android.xiaoan.word.bean.BaiduSuggest
import com.hxl.android.xiaoan.word.net.Net
import com.hxl.android.xiaoan.word.utils.RxJavaUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TextAutoComplete() {
    private val wordComplete: MutableMap<String, String> = mutableMapOf()
    private lateinit var autoAdapter: ArrayAdapter<String>
    private lateinit var textView: AutoCompleteTextView

    fun bind(textView: AutoCompleteTextView, itemClick: String.() -> Unit) {
        this.textView = textView
        textView.apply {
            doAfterTextChanged {
                query(it.toString())
            }


            setOnItemClickListener { _, _, i, _ ->
                val k = wordComplete.keys.toList().sorted()[i]
                itemClick.invoke(wordComplete[k].toString())
                textView.text= Editable.Factory.getInstance().newEditable(k)
                textView.setSelection(k.length)
            }
        }

    }

    private fun query(text: String) {
        RxJavaUtils.createObservable<BaiduSuggest> {
            Net.getBaiduRetrofitApi().suggest(text).enqueue(object : Callback<BaiduSuggest> {
                override fun onResponse(
                    call: Call<BaiduSuggest>,
                    response: Response<BaiduSuggest>
                ) {
                    response.body()?.run { onNext(this);return }
                }

                override fun onFailure(call: Call<BaiduSuggest>, t: Throwable) {
                    onError(t)
                }
            })
        }.subscribe { wordItem ->
            wordComplete.clear()
            wordItem.data?.run {
                this.forEach { item -> wordComplete[item.k] = item.v }
            }
            val sortedResult = wordComplete.keys
                .toList()
                .sorted()
                .map { "$it  ${wordComplete[it]}" }
                .toCollection(mutableListOf())
            autoAdapter = ArrayAdapter<String>(
                textView.context,
                R.layout.item_auto_complete_text_view,
                R.id.autoCompleteItem,
                sortedResult
            )
            textView.setAdapter(autoAdapter)
            autoAdapter.notifyDataSetChanged()
        }
    }
}