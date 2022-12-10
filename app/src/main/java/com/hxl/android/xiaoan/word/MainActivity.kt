package com.hxl.android.xiaoan.word

import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.hxl.android.xiaoan.word.adapter.WordExpandableListAdapter
import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.databinding.ActivityMainBinding
import com.hxl.android.xiaoan.word.net.Net
import com.hxl.android.xiaoan.word.ui.activity.WordTestActivity
import com.hxl.android.xiaoan.word.ui.widget.numberpicker.NumberPicker
import com.hxl.android.xiaoan.word.utils.*
import com.hxl.android.xiaoan.word.utils.RxJavaUtils.createObservable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var wordExpandableListAdapter = WordExpandableListAdapter(this)
    private val textAutCompat: TextAutoComplete = TextAutoComplete()

    private val TAG = "TAG"
    private lateinit var exceptionHandler: ExceptionHandler

    private val parseDataFunction: (List<WordBean>) -> Unit = { parse(it) }
    private val loadDataFunction: () -> Unit = { loadData() }

    private val txtPlayer  =TxtPlayer()


    private fun textToSpeak(text: String) {
        thread {
            try {
                txtPlayer.player(text)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        exceptionHandler = ExceptionHandler(this)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        initView()
        loadData()
        applySetting()
    }

    private fun initView() {


        binding.fab.setOnClickListener { showAddWordDialog() }
        binding.listview.setAdapter(wordExpandableListAdapter)

        binding.listview.setOnChildClickListener { expandableListView, view, groupIndex, i2, l ->
            if (SharedPreferencesUtils.getBoolean("click_show_flag", false)) {
                val text =
                    wordExpandableListAdapter.expandableListDetail[wordExpandableListAdapter.expandableListTitle[groupIndex]]!![i2]
                val current = wordExpandableListAdapter.visibleWords.getOrDefault(text.id, false)
                wordExpandableListAdapter.visibleWords[text.id] = !current
                wordExpandableListAdapter.notifyDataSetChanged()
            }
            true
        }

        wordExpandableListAdapter.playVoice = {
            textToSpeak(it.wordName)
        }
        setGroupIndicatorToRight()
    }

    private fun applySetting() {
        val word = SharedPreferencesUtils.getBoolean("show_word")
        val mean = SharedPreferencesUtils.getBoolean("show_mean")

        wordExpandableListAdapter.wordVisibleState = { word }
        wordExpandableListAdapter.meanVisibleState = { mean }
    }

    private fun parse(list: List<WordBean>) {
        val data = mutableMapOf<String, MutableList<WordBean>>()
        supportActionBar?.title = "已背 ${list.size} 个"
        list.forEach {
            val result = data.getOrPut(it.insertDate) { mutableListOf() }
            result.add(it)
        }

        val titles = data.keys.toList()
            .sortedBy { LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
        wordExpandableListAdapter.setData(titles.reversed(), data)
        wordExpandableListAdapter.notifyDataSetChanged()
    }

    /**
     * 加载数据从服务器
     */
    private fun loadData() {
        createObservable<List<WordBean>> {
            Net.getAppRetrofit().getWordList().execute().body()?.run { onNext(this) }
        }.baseSubscrib(parseDataFunction, exceptionHandler)
    }

    /**
     * 添加单词
     */

    private fun addWord(word: String, mean: String) {
        createObservable<String> {
            Net.getAppRetrofit().addWord(word, mean).execute().body()?.run { onNext(this) }
        }.baseSubscrib({ loadDataFunction.invoke() }, exceptionHandler)

    }

    /**
     * 显示添加单词Dialog
     */
    private fun showAddWordDialog() {
        MaterialDialog(this).show {
            title(R.string.add_word)
            positiveButton(R.string.ok) {
                val word = it.findViewById<EditText>(R.id.tv_word).text.toString()
                val mean = it.findViewById<EditText>(R.id.tv_mean).text.toString()
                addWord(word, mean)

            }
            customView(R.layout.dialog_add_word)
            //自动推荐单词单击后所对因的意思
            val itemClick: (String) -> Unit = {
                findViewById<EditText>(R.id.tv_mean).text =
                    Editable.Factory.getInstance().newEditable(it)
            }
            this.findViewById<AutoCompleteTextView>(R.id.tv_word).apply {
                this.threshold = 1
                textAutCompat.bind(this, itemClick)
            }

        }
    }


    private fun setGroupIndicatorToRight() {
        val width = resources.displayMetrics.widthPixels
        binding.listview.setIndicatorBounds(width - 100, 0)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun showSettingDialog() {
        MaterialDialog(this, BottomSheet()).show {
            customView(R.layout.dialog_setting)
            val meanSwitch = findViewById<SwitchCompat>(R.id.switch_show_mean)
            val wordSwitch = findViewById<SwitchCompat>(R.id.switch_show_word)
            val clickSwitch = findViewById<SwitchCompat>(R.id.switch_click_show)
            meanSwitch.isChecked = SharedPreferencesUtils.getBoolean("show_mean")
            wordSwitch.isChecked = SharedPreferencesUtils.getBoolean("show_word")
            clickSwitch.isChecked = SharedPreferencesUtils.getBoolean("click_show_flag")

            meanSwitch.setOnCheckedChangeListener { _, b ->
                if (b) clickSwitch.isChecked = false
                changeWordState("show_mean", b)
            }
            wordSwitch.setOnCheckedChangeListener { _, b -> changeWordState("show_word", b) }
            clickSwitch.setOnCheckedChangeListener { _, b ->
                if (b) meanSwitch.isChecked = false
                changeWordState("click_show_flag", b)
            }
        }
    }

    private fun changeWordState(key: String, value: Boolean) {
        SharedPreferencesUtils.setBoolean(key, value)
        applySetting()
        wordExpandableListAdapter.notifyDataSetChanged()
    }


    private fun showTestNumberDialog() {
        MaterialDialog(this).show {
            title(text = "选择测试数量")
            customView(R.layout.dialog_test_number_picker)
            negativeButton(text = "确定") {
                findViewById<NumberPicker>(R.id.number_picker).progress.run {
                    startActivity(WordTestActivity.getStartIntent(this, this@MainActivity))
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) showSettingDialog()

        if (item.itemId == R.id.action_test) showTestNumberDialog()
        return super.onOptionsItemSelected(item)
    }
}