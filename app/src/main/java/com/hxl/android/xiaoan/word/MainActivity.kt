package com.hxl.android.xiaoan.word

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.internal.button.DialogActionButton
import com.afollestad.materialdialogs.list.*
import com.hxl.android.xiaoan.word.adapter.WordExpandableListAdapter
import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.common.Application
import com.hxl.android.xiaoan.word.databinding.ActivityMainBinding
import com.hxl.android.xiaoan.word.net.Net
import com.hxl.android.xiaoan.word.ui.activity.WordTestActivity
import com.hxl.android.xiaoan.word.ui.widget.numberpicker.NumberPicker
import com.hxl.android.xiaoan.word.utils.*
import com.hxl.android.xiaoan.word.utils.RxJavaUtils.createObservable
import com.hxl.android.xiaoan.word.utils.orm.AppDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Arrays
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var wordExpandableListAdapter = WordExpandableListAdapter(this)
    private val textAutCompat: TextAutoComplete = TextAutoComplete()

    private val TAG = "TAG"
    private lateinit var exceptionHandler: ExceptionHandler

    private val loadDataFunction: () -> Unit = { loadData() }

    private val txtPlayer = TxtPlayer()


    private val viewModel: MainViewModel by viewModels()

    private fun textToSpeak(text: String) {
        thread {
            try {
                txtPlayer.player(text)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun setLoadLocalWords(list: List<WordBean>) {
        Application.word = list
        val adapterData = mutableMapOf<String, MutableList<WordBean>>()
        supportActionBar?.title = "已背 ${list.size} 个"
        list.forEach {
            val result = adapterData.getOrPut(it.insertDate) { mutableListOf() }
            result.add(it)
        }

        Log.i(TAG, "setLoadLocalWords: $adapterData")
        val titles = adapterData.keys.toList()
            .sortedBy { LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
        wordExpandableListAdapter.setData(titles.reversed(), adapterData)
        wordExpandableListAdapter.notifyDataSetChanged()
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

    /**
     * 加载数据从本地
     */
    private fun loadData() {
        viewModel.getLocalWords().subscribe {
            setLoadLocalWords(it)
        }
    }

    /**
     * 添加单词
     */

    private fun addWord(word: String, mean: String) {
        createObservable<String> {
            Net.getAppRetrofit().addWord(word, mean).execute().body()?.run { onNext(this) }
        }.baseSubscribe({ loadDataFunction.invoke() }, exceptionHandler)

    }

    /**
     * 显示添加单词Dialog
     */
    private fun showAddWordDialog() {
        startActivity(WordTestActivity.getStartIntent(10, this@MainActivity))
//        MaterialDialog(this).show {
//            title(R.string.add_word)
//            positiveButton(R.string.ok) {
//                val word = it.findViewById<EditText>(R.id.tv_word).text.toString()
//                val mean = it.findViewById<EditText>(R.id.tv_mean).text.toString()
//                addWord(word, mean)
//
//            }
//            customView(R.layout.dialog_add_word)
//            //自动推荐单词单击后所对因的意思
//            val itemClick: (String) -> Unit = {
//                findViewById<EditText>(R.id.tv_mean).text =
//                    Editable.Factory.getInstance().newEditable(it)
//            }
//            this.findViewById<AutoCompleteTextView>(R.id.tv_word).apply {
//                this.threshold = 1
//                textAutCompat.bind(this, itemClick)
//            }
//
//        }
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

    private fun doImport(words: List<WordBean>, includeIndex: IntArray) {
        viewModel.importWord(words, includeIndex).baseSubscribe({
            Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show()
            loadData()
        }, {
            Toast.makeText(this, "导入失败", Toast.LENGTH_SHORT).show()
        })
    }

    @SuppressLint("CheckResult")
    private fun showImportWordDialog() {
        var importWordsIndex = intArrayOf()
        val converter: (List<WordBean>) -> List<String> = { wordBeans ->
            wordBeans.map { "${it.wordName} ${it.wordMean}" }.toCollection(mutableListOf())
        }
        val showDialog: (List<WordBean>) -> Unit = {allWord->
            createMaterialDialog {
                title(text = "选择单词 （${allWord.size}）个")
                listItemsMultiChoice(items = converter(allWord), waitForPositiveButton = false) { _, indices, text ->
                    getPositiveButton().run {
                        this.text = "导入(${text.size})"
                        this.isEnabled = indices.isNotEmpty()
                    }
                    importWordsIndex = indices
                }
                customView(R.layout.dialog_import_word_header)

                positiveButton(text = "导入") {
                    doImport(allWord, importWordsIndex)
                }
                negativeButton(text = "取消") {}

                findViewById<Button>(R.id.btn_rand).setOnClickListener {
                    var max =findViewById<NumberPicker>(R.id.number_picker).progress
                    if (max>allWord.size) max=allWord.size
                    Log.i(TAG, "showImportWordDialog: $max")
                    uncheckAllItems()
                    allWord.size.generatorRandom(max).toIntArray().run {
                        checkItems(this)
                        importWordsIndex =this
                    }
                    getPositiveButton().run {
                        this.text = "导入(${max})"

                        this.isEnabled = true
                    }
                }
            }
        }

        val ids = Application.word.map { it.id }.toCollection(mutableListOf())
        viewModel.loadNetworkWords(ids).baseSubscribe({ showDialog(it) }, exceptionHandler)

    }

    private fun createMaterialDialog(config: MaterialDialog.() -> Unit): MaterialDialog {
        return MaterialDialog(this).show {
            config.invoke(this)
        }
    }

    private fun MaterialDialog.getPositiveButton(): DialogActionButton {
        return view.buttonsLayout?.actionButtons?.get(WhichButton.POSITIVE.index)!!
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) showSettingDialog()

        if (item.itemId == R.id.action_test) showTestNumberDialog()

        if (item.itemId == R.id.action_import) showImportWordDialog()
        return super.onOptionsItemSelected(item)
    }
}