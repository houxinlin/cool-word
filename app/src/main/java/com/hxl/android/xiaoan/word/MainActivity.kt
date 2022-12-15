package com.hxl.android.xiaoan.word

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.*
import com.hxl.android.xiaoan.word.adapter.WordExpandableListAdapter
import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.bean.meanKey
import com.hxl.android.xiaoan.word.bean.wordKey
import com.hxl.android.xiaoan.word.common.Application
import com.hxl.android.xiaoan.word.databinding.ActivityMainBinding
import com.hxl.android.xiaoan.word.ui.activity.SettingsActivity
import com.hxl.android.xiaoan.word.ui.activity.WordTestActivity
import com.hxl.android.xiaoan.word.utils.*
import com.hxl.android.xiaoan.word.utils.SystemUtils.createMaterialDialog
import com.hxl.android.xiaoan.word.word.TextAutoComplete
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : BaseActivity() {
    private val TAG = "TAG"
    private lateinit var binding: ActivityMainBinding
    private lateinit var wordExpandableListAdapter: WordExpandableListAdapter
    private var wordVisible: Boolean = false
    private var meanVisible: Boolean = false
    private lateinit var exceptionHandler: ExceptionHandler
    private val textAutCompat: TextAutoComplete = TextAutoComplete()
    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        exceptionHandler = ExceptionHandler(this)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        wordExpandableListAdapter = WordExpandableListAdapter(this)
        initView()
        loadData()
        applySetting()
    }

    private fun setLoadLocalWords(list: List<WordBean>) {
        Application.word = list
        val adapterData = mutableMapOf<String, MutableList<WordBean>>()
        supportActionBar?.title = "已背 ${list.size} 个"
        list.forEach {
            val result = adapterData.getOrPut(it.insertDate) { mutableListOf() }
            result.add(it)
        }

        val titles = adapterData.keys.toList()
            .sortedBy { LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
        wordExpandableListAdapter.setData(titles.reversed(), adapterData)
        wordExpandableListAdapter.notifyDataSetChanged()
    }

    private fun initView() {
        binding.fab.setOnClickListener { showAddWordDialog() }
        binding.listview.setAdapter(wordExpandableListAdapter)

        binding.listview.setOnChildClickListener { _, _, groupIndex, childIndex, _ ->
            configPreferences.run {
                val wordBean = wordExpandableListAdapter.getChild(groupIndex, childIndex)
                if (getBoolean("click_sync", true)) {
                    if (getBoolean("show_word", true) && !getBoolean("show_mean", false)) {
                        wordExpandableListAdapter.reverseVisible(wordBean.meanKey())
                    }
                    if (!getBoolean("show_word", false) && getBoolean("show_mean", true)) {
                        wordExpandableListAdapter.reverseVisible(wordBean.wordKey())
                    }
                    wordExpandableListAdapter.notifyDataSetChanged()
                }
            }
            true
        }

        wordExpandableListAdapter.playVoice = {
            ttsAuto(it.wordName)
        }
        setGroupIndicatorToRight()
    }

    private fun applySetting() {
        wordVisible = configPreferences.getBoolean("show_word", true)
        meanVisible = configPreferences.getBoolean("show_mean", true)

        wordExpandableListAdapter.wordVisibleState = { wordVisible }
        wordExpandableListAdapter.meanVisibleState = { meanVisible }
        wordExpandableListAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        applySetting()
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
     * 显示添加单词Dialog
     */
    private fun showAddWordDialog() {
        MaterialDialog(this).show {
            title(R.string.add_word)
            positiveButton(R.string.ok) {
                val word = it.findViewById<EditText>(R.id.tv_word).text.toString()
                val mean = it.findViewById<EditText>(R.id.tv_mean).text.toString()
                //插入数据
                viewModel.importWord(mutableListOf(WordBean().apply {
                    this.wordMean = mean
                    this.wordName = word
                })).subscribe {
                    loadData()
                    Toast.makeText(this@MainActivity, "导入成功", Toast.LENGTH_SHORT).show()
                }

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

    private fun showTestNumberDialog() {
        MaterialDialog(this).show {
            title(text = "选择测试数量")
            customView(R.layout.dialog_test_number_picker)
            val seekBar = findViewById<SeekBar>(R.id.seek_bar).apply {
                setOnSeekBarChangeListener(object : ProgressListener() {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        this@show.findViewById<TextView>(R.id.tv_progress)?.text = "$p1"
                    }
                })
            }
            negativeButton(text = "确定") {
                seekBar.progress.run {
                    startActivity(WordTestActivity.getStartIntent(this, this@MainActivity))
                }
            }
        }
    }

    private fun doImport(words: List<WordBean>, indexs: IntArray) {
        val result = mutableListOf<WordBean>()
        val date = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        for (includeIndex in indexs) result.add(words[includeIndex].apply {
            this.insertDate = date
        })
        viewModel.importWord(result).baseSubscribe({
            Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show()
            loadData()
        }, {
            Toast.makeText(this, "导入失败", Toast.LENGTH_SHORT).show()
        })
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun showImportWordDialog() {
        var importWordsIndex = intArrayOf()
        val converter: (List<WordBean>) -> List<String> = { wordBeans ->
            wordBeans.map { "${it.wordName}\n${it.wordMean}" }.toCollection(mutableListOf())
        }
        val showDialog: (List<WordBean>) -> Unit = { allWord ->
            createMaterialDialog(this) {
                title(text = "选择单词 （${allWord.size}）个")
                listItemsMultiChoice(
                    items = converter(allWord),
                    waitForPositiveButton = false
                ) { _, indices, text ->
                    getPositiveButton().run {
                        this.text = "导入(${text.size})"
                        this.isEnabled = indices.isNotEmpty()
                    }
                    importWordsIndex = indices
                }
                customView(R.layout.dialog_import_word_header)

                positiveButton(text = "导入(0)") {
                    doImport(allWord, importWordsIndex)
                }
                negativeButton(text = "取消") {}
                var seekBar = findViewById<SeekBar>(R.id.seek_bar).apply {
                    setOnSeekBarChangeListener(object : ProgressListener() {
                        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                            this@createMaterialDialog.findViewById<Button>(R.id.btn_rand).text =
                                Editable.Factory.getInstance().newEditable("随机($p1)")
                        }
                    })
                }
                findViewById<Button>(R.id.btn_rand).setOnClickListener {
                    var max = seekBar.progress
                    if (max > allWord.size) max = allWord.size
                    uncheckAllItems()
                    allWord.size.generatorRandom(max).toIntArray().run {
                        checkItems(this)
                        importWordsIndex = this
                    }
                    getPositiveButton().run {
                        this.text = "导入($max)"

                        this.isEnabled = true
                    }
                }
            }
        }

        val ids = Application.word.map { it.id }.toCollection(mutableListOf())
        viewModel.loadNetworkWords(ids).baseSubscribe({ showDialog(it) }, exceptionHandler)

    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            getResult.launch(intent)
        }

        if (item.itemId == R.id.action_test) showTestNumberDialog()

        if (item.itemId == R.id.action_import) showImportWordDialog()
        return super.onOptionsItemSelected(item)
    }

    open class ProgressListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
        }
    }
}