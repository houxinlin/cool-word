package com.hxl.android.xiaoan.word.ui.activity

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.OvershootInterpolator
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.utils.MDUtil.inflate
import com.hxl.android.xiaoan.word.BaseActivity
import com.hxl.android.xiaoan.word.R
import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.common.Application
import com.hxl.android.xiaoan.word.databinding.ActivityWordTestBinding
import com.hxl.android.xiaoan.word.ui.widget.base.CircularProgressBar
import com.hxl.android.xiaoan.word.ui.widget.base.Option
import com.hxl.android.xiaoan.word.utils.*
import com.hxl.android.xiaoan.word.word.FrdicExample
import com.hxl.android.xiaoan.word.word.IWordRecord
import com.hxl.android.xiaoan.word.word.WordTestQueue
import java.util.concurrent.Semaphore
import kotlin.math.ceil


class WordTestActivity : BaseActivity() {
    private lateinit var wordTestBinding: ActivityWordTestBinding
    private var testWordsCount: MutableMap<WordBean, Int> = mutableMapOf()

    private val notKnowRecord = NotKnowWordRecord()
    private val vagueRecord = VagueWordRecord()

    //认识单词列表
    private val knowSet = mutableSetOf<WordBean>()

    //单词队列
    private var wordQueue: WordTestQueue = WordTestQueue()
    private val example = FrdicExample()
    private val examples = mutableListOf<Spanned>()
    private val nextWordSemaphore = Semaphore(1)

    private val removeAndNexWordStrategy: (WordBean) -> Unit = {
        knowSet.add(it)
        SystemUtils.play(R.raw.success)
    }
    private val insertAndNexWordStrategy: (WordBean) -> Unit = {
        testWordsCount[it] = testWordsCount[it]!! - 1
        wordQueue.wordQueueSort(it) { list, word ->
            list.addRandom(word)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        wordTestBinding = DataBindingUtil.setContentView(this, R.layout.activity_word_test)

        setSupportActionBar(wordTestBinding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        initView()
        initEvent()

        val init: (List<WordBean>) -> Unit = { wordBeans ->
            wordTestBinding.wordProgress.maxValue = wordBeans.size
            wordBeans.forEach {
                testWordsCount[it] = DEFAULT_KNOW_COUNT
            }
            if (wordBeans.isNotEmpty()) setNextWord(wordQueue.getCurrentWord()!!)
        }
        val testSize = intent.getIntExtra(INTENT_SIZE_ARG, 10)
        wordQueue.initTestWord(testSize) { init.invoke(it) }
    }

    private fun recordCountAndSortWord(
        target: IWordRecord,
        insertStrategy: (MutableList<WordBean>, WordBean) -> Unit
    ) {
        val currentWord = wordQueue.getCurrentWord()
        currentWord?.run {
            setContinueButtonVisible(true)
            //记录 《不认识》 和 《模糊》 的次数
            target.inc(this)
            wordQueue.wordQueueSort(this, insertStrategy)
            refreshProgress()//刷新进度
            wordTestBinding.tvMean.text = this.wordMean

        }
    }

    private fun nextWordStrategy() {
        val currentWord = wordQueue.getCurrentWord()
        currentWord?.run {
            wordTestBinding.tvMean.text = currentWord.wordMean
            if (wordQueue.size <= 1 || testWordsCount[this]!! <= 0) removeAndNexWordStrategy(
                this
            )
            //如果还需要继续背诵,添加到随机位置
            if ((testWordsCount[this]!! > 0)) insertAndNexWordStrategy(this)

            delayAndNextWord()

        }
    }

    private fun delayAndNextWord() {
        if (nextWordSemaphore.tryAcquire()) {
            SystemUtils.delayRun(1000) {
                nextWordSemaphore.release()
                doNextWord()
            }
        }
    }


    /**
     * 设置继续按钮可见性
     */
    private fun setContinueButtonVisible(visible: Boolean = true) {
        val value = if (visible) wordTestBinding.btnVague.x else 0
        ObjectAnimator.ofFloat(wordTestBinding.btnIncognizance, "translationX", value.toFloat())
            .apply {
                duration = 200
                start()
            }
        ObjectAnimator.ofFloat(wordTestBinding.btnKnow, "translationX", -value.toFloat()).apply {
            duration = 200
            start()
        }
        wordTestBinding.btnNext.isVisible = visible
        SystemUtils.delayRun(if (visible) 80 else 0) {
            wordTestBinding.btnKnow.isVisible = !visible
            wordTestBinding.btnVague.isVisible = !visible
            wordTestBinding.btnIncognizance.isVisible = !visible
        }
    }


    /**
     * 刷新进度
     */
    private fun refreshProgress() {
        val notKnowCount = notKnowRecord.count { !knowSet.contains(it) }

        val vagueCount = vagueRecord.count { !knowSet.contains(it) }
        wordTestBinding.wordProgress.setState(notKnowCount, vagueCount, knowSet.size)
        wordTestBinding.wordProgress.currentValue = knowSet.size
    }

    /**
     * 完成测试
     */

    private fun doFinish() {
        val result = mutableListOf<WordScore>()
        for (key in testWordsCount.keys) {
            val score =
                ceil(notKnowRecord.getOrDefault(key, 0) * 2.5) + vagueRecord.getOrDefault(key, 0)
            result.add(WordScore(key, score.toInt()))
        }
        result.sortBy { it.score }
        MaterialDialog(this).show {
            title(text = "统计")
            cancelOnTouchOutside(false)
            positiveButton(text = "继续加油") {
                cancel()
                finish()
            }
            onCancel {
                finish()
            }
            listCustomItems(result, object : CustomList {
                override fun bindView(view: View, position: Int) {
                    view.findViewById<TextView>(R.id.tv_word).text =
                        result[position].wordBean.wordName
                    view.findViewById<TextView>(R.id.tv_mean).text =
                        result[position].wordBean.wordMean
                    val progress = 100f - (100f * (result[position].score / 15f))
                    view.findViewById<CircularProgressBar>(R.id.progress).progress = progress
                }

                override fun createView(parent: ViewGroup, context: Context): View {
                    return parent.inflate(context, R.layout.item_dialog_result_statistics)
                }
            })
        }
        Toast.makeText(this, "完成", Toast.LENGTH_SHORT).show()
    }

    /**
     * 下一个单词
     */
    private fun setNextWord(wordBean: WordBean) {
        refreshProgress()
        wordTestBinding.tvMean.text = ""
        wordTestBinding.tvWord.text = wordBean.wordName
        wordTestBinding.btnPhontype.text = wordBean.phontype.addPrefix("美")
        wordTestBinding.btnPhonitic.text = wordBean.phonitic.addPrefix("英")

        if (isAutoPlayTTS()) ttsAuto(wordBean.wordName)

        if (autoShowOption()) {
            showOptions()
        } else {
            wordTestBinding.optionView.reset()
            wordTestBinding.tvTip.isVisible = true
        }
        queryExample()
    }

    /**
     * 查询例句
     */
    private fun queryExample() {
        example.listExample(wordQueue.getCurrentWord()!!.wordName).baseSubscribe({
            examples.clear()
            for (key in it.keys) {
                examples.add(Html.fromHtml(key + it[key], HtmlCompat.FROM_HTML_MODE_COMPACT))
            }
            ArrayAdapter(this, R.layout.item_example_text_view, R.id.tv_value, examples).run {
                wordTestBinding.exampleListview.adapter = this
            }
        }, {})
    }

    /**
     * 下一个单词
     */
    private fun doNextWord() {
        wordQueue.pollFirst()//弹出一个
        wordTestBinding.wordViewgroup.close()
        setContinueButtonVisible(false)
        refreshProgress()
        if (wordQueue.peekFirst() == null) {
            doFinish()
            return
        }
        setNextWord(wordQueue.peekFirst()!!)
    }


    /**
     * 初始化视图
     */
    private fun initView() {
        val viewAnimator: (View, Int, Int) -> Unit = { view: View, from: Int, to: Int ->
            ObjectAnimator.ofFloat(view, "translationY", from.toFloat(), to.toFloat()).apply {
                this.interpolator = OvershootInterpolator()
                duration = 400
                start()
            }
        }
        val start: (Int) -> Unit = {
            wordTestBinding.btnIncognizance.translationY = it.toFloat()
            wordTestBinding.btnVague.translationY = it.toFloat()
            wordTestBinding.btnKnow.translationY = it.toFloat()

            viewAnimator.invoke(wordTestBinding.btnIncognizance, it, 0)
            SystemUtils.delayRun(50) { viewAnimator.invoke(wordTestBinding.btnVague, it, 0) }
            SystemUtils.delayRun(100) { viewAnimator.invoke(wordTestBinding.btnKnow, it, 0) }
        }
        wordTestBinding.btnVague.viewTreeObserver.addOnGlobalLayoutListener(
            object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    start.invoke(wordTestBinding.btnVague.y.toInt() * 2)
                    wordTestBinding.btnVague.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
    }


    private fun doRecordCountAndSortWord(iWordRecord: IWordRecord) {
        recordCountAndSortWord(iWordRecord) { list, word ->
            testWordsCount[word] = DEFAULT_KNOW_COUNT + iWordRecord.getDefaultCount()
            list.addNearby(INSERT_NEARBY, word)
        }
        if (waitUserContinue()) delayAndNextWord()
    }

    private fun doNotKnowRecordCountAndSortWord() {
        doRecordCountAndSortWord(notKnowRecord)
    }

    private fun doVagueRecordCountAndSortWord() {
        doRecordCountAndSortWord(vagueRecord)
    }

    private fun doContinueButtonClick() {
        setContinueButtonVisible(false)
        doNextWord()
    }

    private fun doOptionClick(option: Option) {
        if (option.id == wordQueue.getCurrentWord()?.id) {
            nextWordStrategy()
            return
        }
        SystemUtils.play(R.raw.error)
        doNotKnowRecordCountAndSortWord()
    }


    private fun playCurrentWord(playLan: PlayLan) {
        wordQueue.getCurrentWord()?.wordName.run {
            this?.tts(playLan)
        }
    }

    /**
     * 初始化事件
     */
    private fun initEvent() {

        wordTestBinding.btnPhontype.setOnClickListener { playCurrentWord(PlayLan.EN) }
        wordTestBinding.btnPhontype.setOnClickListener { playCurrentWord(PlayLan.UK) }

        wordTestBinding.wordViewgroup.callback = {
            wordTestBinding.rlController.alpha = it
            wordTestBinding.btnBack.alpha = 1.0f - it
            wordTestBinding.btnBack.isVisible = 1.0f - it != 0f
        }

        wordTestBinding.btnBack.setOnClickListener { wordTestBinding.wordViewgroup.close() }

        wordTestBinding.btnNext.setOnClickListener { doContinueButtonClick() }
        wordTestBinding.btnIncognizance.setOnClickListener { doNotKnowRecordCountAndSortWord() }
        wordTestBinding.btnVague.setOnClickListener { doVagueRecordCountAndSortWord() }

        wordTestBinding.optionView.setClickOption { doOptionClick(it) }

        wordTestBinding.btnKnow.setOnClickListener { nextWordStrategy() }
        wordTestBinding.tvTip.setOnClickListener { showOptions() }

    }

    /**
     * 显示选项
     */
    private fun showOptions() {
        wordQueue.getCurrentWord()?.run {
            val options = Application.generatorOptions(this.id)
            options.add(Option(this.id, this.wordMean))
            wordTestBinding.optionView.setOptions(this.id, options.shuffled())
            wordTestBinding.tvTip.isVisible = false
        }

    }

    override fun onBackPressed() {
        if (wordTestBinding.wordViewgroup.isOpen) {
            wordTestBinding.wordViewgroup.close()
            return
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_test, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val INTENT_SIZE_ARG = "size"
        const val DEFAULT_KNOW_COUNT = 1
        const val INSERT_NEARBY = 5
        fun getStartIntent(size: Int, context: Context): Intent {
            return Intent(context, WordTestActivity::class.java).apply {
                this.putExtra(INTENT_SIZE_ARG, size)
            }
        }
    }
}

data class WordScore(val wordBean: WordBean, val score: Int)

class NotKnowWordRecord : IWordRecord() {
    override fun getDefaultCount(): Int {
        return 2
    }
}

class VagueWordRecord : IWordRecord() {
    override fun getDefaultCount(): Int {
        return 1
    }
}