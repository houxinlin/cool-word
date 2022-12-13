package com.hxl.android.xiaoan.word.ui.activity

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.hxl.android.xiaoan.word.R
import com.hxl.android.xiaoan.word.adapter.WordExampleViewPagerAdapter
import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.common.Application
import com.hxl.android.xiaoan.word.databinding.ActivityWordTestBinding
import com.hxl.android.xiaoan.word.utils.*
import com.hxl.android.xiaoan.word.word.FrdicExample
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.Semaphore


class WordTestActivity : AppCompatActivity() {
    private lateinit var wordTestBinding: ActivityWordTestBinding

    private lateinit var testWords: MutableMap<WordBean, Int>

    //不认识单词列表
    private val notKnowSet = mutableMapOf<WordBean, Int>()

    //模糊单词列表
    private val vagueSet = mutableMapOf<WordBean, Int>()

    //认识单词列表
    private val knowSet = mutableSetOf<WordBean>()

    //单词队列
    private var wordQueue: LinkedBlockingDeque<WordBean> = LinkedBlockingDeque()

    private val example = FrdicExample()

    private val examples = mutableListOf<Spanned>()

    private val nextWordSemaphore = Semaphore(1)
    private val removeAndNexWordStrategy: (WordBean) -> Unit = {
        wordQueue.pollFirst()
        knowSet.add(it)
        refreshProgress()
        SystemUtils.play(R.raw.success)
    }
    private val insertAndNexWordStrategy: (WordBean) -> Unit = {
        testWords[it] = testWords[it]!! - 1

        wordQueueShuffle(it) { list, word ->
            list.addRandom(word)
        }
        wordQueue.pollFirst()//弹出一个

    }

    private val txtPlayer = TxtPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wordTestBinding = DataBindingUtil.setContentView(this, R.layout.activity_word_test)

        setSupportActionBar(wordTestBinding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        initView()
        initEvent()

        intent.getIntExtra(INTENT_SIZE_ARG, 10).run {
            wordTestBinding.wordProgress.maxValue = this
            generatorTestWord(this)
        }

        nextWord()
    }

    private fun wordQueueShuffle(
        wordBean: WordBean,
        insertStrategy: (MutableList<WordBean>, WordBean) -> Unit
    ) {
        val list = wordQueue.toMutableList() //获取当前队列集合
        wordQueue.clear()  //队列清空
        insertStrategy.invoke(list, wordBean) //执行排序策略
        list.forEach(wordQueue::addLast) //直接排序策略后重新生成单词队列
    }

    private fun generatorTestWord(size: Int) {
        //生成测试的单词
        val tempWords = Application.generatorTestWord(size)

        testWords = mutableMapOf()
        tempWords.forEach {
            wordQueue.addFirst(it)
            //每个单词至少点击认识两次
            testWords[it] = DEFAULT_KNOW_COUNT
        }
    }


    /**
     * 以下将背诵单词队列重新生成，由于被点击不认识
     */
    private fun shuffleWord(
        target: MutableMap<WordBean, Int>,
        insertStrategy: (MutableList<WordBean>, WordBean) -> Unit
    ) {
        val currentWord = getCurrentWord()
        currentWord?.run {
            setContinueButtonVisible(true)
            //记录 《不认识》 和 《模糊》 的次数
            val count = target.getOrDefault(this, 0)
            target[this] = count + 1

            wordQueueShuffle(this, insertStrategy)

            refreshProgress()//刷新进度
            wordTestBinding.tvMean.text = this.wordMean

        }
    }

    private fun nextWordStrategy() {
        val currentWord = getCurrentWord()
        currentWord?.run {
            if (nextWordSemaphore.tryAcquire()) {
                wordTestBinding.tvMean.text = currentWord.wordMean
                if (wordQueue.size <= 1 || testWords[this]!! <= 0) removeAndNexWordStrategy(this)
                //如果还需要继续背诵,添加到随机位置
                if ((testWords[this]!! > 0)) insertAndNexWordStrategy(this)
                SystemUtils.delayRun(1000) {
                    nextWordSemaphore.release()
                    nextWord()
                }
            }
        }
    }


    private fun getCurrentWord(): WordBean? {
        return wordQueue.peekFirst()
    }

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


    private fun refreshProgress() {
        val notKnowCount = notKnowSet.keys.count { !knowSet.contains(it) }

        val vagueCount = vagueSet.keys.count { !knowSet.contains(it) }
        wordTestBinding.wordProgress.setState(notKnowCount, vagueCount, knowSet.size)
        wordTestBinding.wordProgress.currentValue = knowSet.size
    }

    private fun doFinish() {
        Toast.makeText(this, "完成", Toast.LENGTH_SHORT).show()
    }

    private fun nextWord(wordBean: WordBean?) {
        refreshProgress()
        if (wordBean == null) {
            doFinish()
            return
        }
        wordTestBinding.tvMean.text = ""
        wordTestBinding.tvWord.text = wordBean.wordName
        txtPlayer.player(wordBean.wordName)
        queryExample()
    }

    private fun queryExample() {
        example.listExample(getCurrentWord()!!.wordName).baseSubscribe({
            examples.clear()
            for (key in it.keys) {
                examples.add(Html.fromHtml(key + it[key], HtmlCompat.FROM_HTML_MODE_COMPACT))
            }
            ArrayAdapter(this, R.layout.item_example_text_view, R.id.tv_value, examples).run {
                wordTestBinding.exampleListview.adapter = this
            }

            Log.i(TAG, "queryExample: $it")
        }, {})
    }

    private fun nextWord() {
        wordTestBinding.wordViewgroup.close()
        nextWord(wordQueue.peekFirst())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
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

    /**
     * 初始化事件
     */
    private fun initEvent() {
        wordTestBinding.wordViewgroup.callback = {
            wordTestBinding.rlController.alpha = it
            wordTestBinding.btnBack.alpha = 1.0f - it
            wordTestBinding.btnBack.isVisible = 1.0f - it != 0f
        }
        wordTestBinding.btnBack.setOnClickListener {
            wordTestBinding.wordViewgroup.close()
        }
        wordTestBinding.btnNext.setOnClickListener {
            setContinueButtonVisible(false)
            wordQueue.pollFirst()//将当前背诵的弹出，执行排序策略后，第一个始终不会变
            nextWord()

        }
        wordTestBinding.btnIncognizance.setOnClickListener {
            shuffleWord(notKnowSet) { list, word ->
                testWords[word] = DEFAULT_KNOW_COUNT + 2
                list.addNearby(INSERT_NEARBY, word)
            }

        }
        wordTestBinding.btnVague.setOnClickListener {
            shuffleWord(vagueSet) { list, word ->
                testWords[word] = DEFAULT_KNOW_COUNT + 1
                list.addCenterNearby(INSERT_NEARBY, word)
            }
        }

        wordTestBinding.tvWord.setOnClickListener { txtPlayer.player(getCurrentWord()!!.wordName) }
        wordTestBinding.btnKnow.setOnClickListener { nextWordStrategy() }
        wordTestBinding.tvTip.setOnClickListener {
        }

    }

    override fun onBackPressed() {
        if (wordTestBinding.wordViewgroup.isOpen){
            wordTestBinding.wordViewgroup.close()
            return
        }
        super.onBackPressed()
    }

    companion object {
        const val INTENT_SIZE_ARG = "size"
        const val TAG = "TAG"
        const val DEFAULT_KNOW_COUNT = 1
        const val INSERT_NEARBY = 5
        fun getStartIntent(size: Int, context: Context): Intent {
            return Intent(context, WordTestActivity::class.java).apply {
                this.putExtra(INTENT_SIZE_ARG, size)
            }
        }
    }
}