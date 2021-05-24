package ru.konovalovk.subtitle_parser.habib

import android.app.Activity
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.DialogFragment
import com.ichi2.anki.api.AddContentApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.konovalovk.subtitle_parser.R
import ru.konovalovk.subtitle_parser.dictionary.Anki
import ru.konovalovk.subtitle_parser.dictionary.AnkiStoragePermissionError
import ru.konovalovk.subtitle_parser.dictionary.Dictionary
import ru.konovalovk.subtitle_parser.dictionary.model.Glosbe
import java.io.IOException
import java.util.regex.Pattern

/**
 * Created by habib on 4/20/17.
 */
class DictionaryDialog : DialogFragment(),
    OnItemSelectedListener, View.OnClickListener {
    private var mWordToTranslateEditText: EditText? = null
    private var mainScrollView: ScrollView? = null
    private var mCaptionTextView: TextView? = null
    private var mDictionary: Dictionary? = null
    private var mOfflineTranslationTextView: TextView? = null
    private var mOfflineDictionaryLoading: ProgressBar? = null
    private var mOnlineTranslationTextView: TextView? = null
    private var mOnlineDictionaryLoading: ProgressBar? = null
    private var mOfflineDictionaryLayout: LinearLayout? = null
    private var mSpeakButton: ImageView? = null
    private var mBookmark: ImageView? = null
    private var mFromLanguageValues: Array<String>? = null
    private var mToLanguageValues: Array<String>? = null
    private var mSettings: SharedPreferences? = null
    private var toLanguageInitialized = false
    private var fromLanguageInitialized = false
    private var mOnlineTranslated = false
    private var mOfflineTranslated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mSettings == null) {
            mSettings = PreferenceManager.getDefaultSharedPreferences(context)
            mLastFromLangugeSelectedItem =
                mSettings!!.getInt(
                    LAST_FROM_LANGUAGE_SELECTED,
                    25
                )
            mLastToLangugeSelectedItem =
                mSettings!!.getInt(
                    LAST_to_LANGUAGE_SELECTED,
                    84
                )
        }
        dialog!!.setCancelable(true)
        dialog!!.setCanceledOnTouchOutside(true)
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.dictionary, container)
    }

    private fun translate(word: String?) {
        mBookmark!!.visibility = View.INVISIBLE
        mWordToTranslateEditText!!.setText(word)
        mWordToTranslateEditText!!.setSelection(word!!.length)
        mTranslateTask = Translate()
        mTranslateTask!!.execute(*arrayOf<String?>(word))
        onlineTranslate(word)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val word = requireArguments()["word"] as String?
        val dialog = requireArguments()["dialog"] as String?
        val clickableCaption = makeWordsClickable(dialog)
        mWordToTranslateEditText = view.findViewById<View>(R.id.edit_text) as EditText
        mCaptionTextView = view.findViewById<View>(R.id.caption) as TextView
        mSpeakButton = view.findViewById<View>(R.id.speak) as ImageView
        mainScrollView = view.findViewById<View>(R.id.main_scroll_view) as ScrollView
        mBookmark = view.findViewById<View>(R.id.bookmark) as ImageView
        mOfflineTranslationTextView = view.findViewById<View>(R.id.offline_translation) as TextView
        mOfflineDictionaryLoading =
            view.findViewById<View>(R.id.offline_dictionary_loading) as ProgressBar
        mOnlineTranslationTextView = view.findViewById<View>(R.id.online_translation) as TextView
        mOnlineDictionaryLoading =
            view.findViewById<View>(R.id.online_dictionary_loading) as ProgressBar
        mOfflineDictionaryLayout =
            view.findViewById<View>(R.id.offline_dictionary_layout) as LinearLayout
        mBookmark!!.setOnClickListener(this)
        mWordToTranslateEditText!!.setText(word)
        mWordToTranslateEditText!!.setSelection(word!!.length)
        mCaptionTextView!!.text = clickableCaption
        mCaptionTextView!!.movementMethod = LinkTouchMovementMethod()
        mCaptionTextView!!.highlightColor = resources.getColor(R.color.orange500)
        mOfflineTranslationTextView!!.movementMethod = LinkTouchMovementMethod()
        mSpeakButton!!.setOnClickListener(this)
        mWordToTranslateEditText!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> // If the event is a key-down event on the "enter" button
            if (event.action == KeyEvent.ACTION_DOWN &&
                keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                // Perform action on key press
                translate(mWordToTranslateEditText!!.text.toString())
                return@OnKeyListener false
            }
            false
        })
        val fromLanguageSpinner = view.findViewById<View>(R.id.language_from_list) as Spinner
        val toLanguageSpinner = view.findViewById<View>(R.id.language_to_list) as Spinner

        //From language spinner
        fromLanguageSpinner.onItemSelectedListener = this
        val fromLangugeAdapter = ArrayAdapter.createFromResource(
            requireActivity(), R.array.online_from_language_entries, R.layout.language_list_spinner_item
        )
        mFromLanguageValues = resources.getStringArray(R.array.online_from_language_values)
        fromLangugeAdapter.setDropDownViewResource(R.layout.language_list_spinner_dropdown_item)
        fromLanguageSpinner.adapter = fromLangugeAdapter
        fromLanguageSpinner.setSelection(mLastFromLangugeSelectedItem)

        //To language spinner
        toLanguageSpinner.onItemSelectedListener = this
        val toLangugeAdapter = ArrayAdapter.createFromResource(
            requireActivity(), R.array.online_to_language_entries, R.layout.language_list_spinner_item
        )
        mToLanguageValues = resources.getStringArray(R.array.online_to_language_values)
        toLangugeAdapter.setDropDownViewResource(R.layout.language_list_spinner_dropdown_item)
        toLanguageSpinner.adapter = fromLangugeAdapter
        toLanguageSpinner.setSelection(mLastToLangugeSelectedItem)
    }

    override fun onItemSelected(parent: AdapterView<*>, v: View, position: Int, id: Long) {
        val editor = mSettings!!.edit()
        val parentId = parent.id
        if (parentId == R.id.language_from_list) {
            mLastFromLangugeSelectedItem = position
            editor.putInt(LAST_FROM_LANGUAGE_SELECTED, mLastFromLangugeSelectedItem).commit()
            fromLanguageInitialized = true
        } else if (parentId == R.id.language_to_list) {
            mLastToLangugeSelectedItem = position
            editor.putInt(LAST_to_LANGUAGE_SELECTED, mLastToLangugeSelectedItem).commit()
            toLanguageInitialized = true
        }
        val toLanguage = mToLanguageValues!![mLastToLangugeSelectedItem]
        val fromLanguge = mFromLanguageValues!![mLastFromLangugeSelectedItem]


        //prevent to translate two time
        if (toLanguageInitialized && fromLanguageInitialized) {
            mDictionaryLoadertask = DictionaryLoader()
            mDictionaryLoadertask!!.execute(
                *arrayOf(
                    fromLanguge,
                    toLanguage,
                    mWordToTranslateEditText!!.text.toString()
                )
            )
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    private fun getMeaning(from: String, dest: String, phrase: String) {}
    private fun onlineTranslate(word: String?) {
        mOnlineTranslated = false
        mOnlineTranslationTextView!!.visibility = View.GONE
        mOnlineDictionaryLoading!!.visibility = View.VISIBLE
        val fromLanguge = mFromLanguageValues!![mLastFromLangugeSelectedItem]
        val toLanguage = mToLanguageValues!![mLastToLangugeSelectedItem]
        val mGlosbeService = mDictionary!!.glosbeService
        if (mGlosbeService == null) {
            if (isAdded) {
                mOnlineDictionaryLoading!!.visibility = View.GONE
                mOnlineTranslationTextView!!.visibility = View.VISIBLE
                mOnlineTranslationTextView!!.text = getString(R.string.online_translation_error)
            }
            return
        }
        mGlosbeService.getMeaning(fromLanguge, toLanguage, word!!.toLowerCase())
            .enqueue(object : Callback<Glosbe> {
                override fun onResponse(call: Call<Glosbe>, response: Response<Glosbe>) {
                    mOnlineDictionaryLoading!!.visibility = View.GONE
                    mOnlineTranslationTextView!!.visibility = View.VISIBLE
                    var translation: String? = ""
                    if (response.isSuccessful) {
                        val tuc = response.body().tuc
                        if (tuc != null) {
                            val tucSize = tuc.size
                            var meaningCount = 0
                            for (i in 0 until tucSize) {
                                val phrase = tuc[i].phrase
                                if (phrase != null && phrase.language == toLanguage) {
                                    if (meaningCount++ > 0) {
                                        translation += if (toLanguage == "fa") "، " else if (toLanguage == "ja") "、 " else ", "
                                    }
                                    translation += phrase.text
                                }
                            }
                            if (!translation!!.isEmpty()) {
                                mOnlineTranslated = true
                                mBookmark!!.visibility = View.VISIBLE
                            }
                            if (translation.isEmpty()) translation = word
                            mOnlineTranslationTextView!!.text = translation
                        }
                    } else {
                        if (isAdded) {
                            mOnlineTranslationTextView!!.text =
                                getString(R.string.online_translation_error)
                        }
                    }
                }

                override fun onFailure(call: Call<Glosbe>, t: Throwable) {
                    mOnlineDictionaryLoading!!.visibility = View.GONE
                    mOnlineTranslationTextView!!.visibility = View.VISIBLE
                    if (t is NoConnectivityException) {
                        if (isAdded) {
                            val message =
                                "<font color='red' >" + "<b>" + getString(R.string.connection_error_title) + "</b>" + "</font>" +
                                        "<br>" + getString(R.string.connection_error_message)
                            val styledTranslation = Html.fromHtml(message) as SpannableStringBuilder
                            mOnlineTranslationTextView!!.text = styledTranslation
                        }
                    } else {
                        if (isAdded) {
                            mOnlineTranslationTextView!!.text = getString(R.string.unexpected_error)
                        }
                    }
                }
            })
    }

    private var mTranslateTask: Translate? = null
    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.speak) {
            if (mDictionary != null) {
                if (mDictionary!!.ttsForLanguageAvailable) mDictionary!!.speakOut(
                    mWordToTranslateEditText!!.text.toString()
                ) else Toast.makeText(
                    context,
                    getText(R.string.tts_not_available),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (id == R.id.bookmark) {
            if (!(mOfflineTranslated || mOnlineTranslated)) return
            val ankiTask = AnkiTask()
            if (AddContentApi.getAnkiDroidPackageName(context) == null) {
                Toast.makeText(
                    context,
                    requireContext().getString(R.string.Anki_not_installed),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (Permissions.shouldRequestAnkiPermission(
                        activity
                    )
                ) {
                    Permissions.requestAnkiPermission(requireActivity(), 1)
                } else {
                    var tranlsation = ""
                    if (mOfflineTranslated) {
                        tranlsation = mOfflineTranslationTextView!!.text.toString()
                    }
                    if (mOnlineTranslated) {
                        tranlsation += "<br>"
                        tranlsation += mOnlineTranslationTextView!!.text.toString()
                    }
                    ankiTask.execute(
                        *arrayOf(
                            mWordToTranslateEditText!!.text.toString(),
                            mCaptionTextView!!.text.toString(),
                            tranlsation
                        )
                    )
                }
            }
        }
    }

    private inner class AnkiTask : AsyncTask<String?, Void?, Int>() {
        protected override fun doInBackground(vararg params: String?): Int {
            val word = params[0]
            val caption = params[1]
            val translation = params[2]
            val noteIds = MediaDatabase.getAnkiNoteId(word)
            val anki = Anki(context) ?: return Companion.UNEXPECTED
            if (noteIds != null && !noteIds.isEmpty()) {
                for (noteId in noteIds) {
                    if (anki.ifExist(noteId)) {
                        return Companion.EXIST
                    }
                }
            }
            var noteId: Long? = null
            noteId = try {
                anki.addCardsToAnkiDroid("$word<br>$caption", translation, context)
            } catch (ankiStoragePermissionError: AnkiStoragePermissionError) {
                return Companion.ANKI_STORAGE_PERMISSION
            }
            if (noteId != null) {
                MediaDatabase.saveAnkiWord(word, noteId)
                return Companion.ADDED
            }
            return Companion.UNEXPECTED
        }

        override fun onPostExecute(code: Int) {
            when (code) {
                Companion.UNEXPECTED -> Toast.makeText(
                    context,
                    context!!.getString(R.string.Anki_unexpected_error),
                    Toast.LENGTH_SHORT
                ).show()
                Companion.ADDED -> Toast.makeText(
                    context,
                    context!!.getString(R.string.Anki_added),
                    Toast.LENGTH_SHORT
                ).show()
                Companion.EXIST -> Toast.makeText(
                    context,
                    context!!.getString(R.string.Anki_word_exist_already),
                    Toast.LENGTH_SHORT
                ).show()
                Companion.ANKI_STORAGE_PERMISSION -> Toast.makeText(
                    context,
                    context!!.getString(R.string.Anki_storage_permission),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onPreExecute() {}
    }

    private inner class Translate : AsyncTask<String?, Void?, String>() {
        protected override fun doInBackground(vararg params: String?): String {
            var translation = ""
            if (mDictionary != null) {
                translation = mDictionary!!.getTranslation(params[0]).translation
                mOfflineTranslated = mDictionary!!.getTranslation(params[0]).translated
            }
            return translation
        }

        override fun onPostExecute(translation: String) {
            val translationWithLinks = getTranslationWithLinks(translation)
            mOfflineTranslationTextView!!.text = translationWithLinks
            if (mOfflineTranslated) mBookmark!!.visibility = View.VISIBLE
            mOfflineDictionaryLoading!!.visibility = View.GONE
            mOfflineTranslationTextView!!.visibility = View.VISIBLE
            focusOnView(mainScrollView, mOfflineDictionaryLayout)
        }

        override fun onPreExecute() {
            mOfflineTranslationTextView!!.visibility = View.GONE
            mOfflineDictionaryLoading!!.visibility = View.VISIBLE
        }
    }

    private fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan, url: String) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        //prevent Error : not attached to Activity
        if (!isAdded) return
        val clickableLinkSpan: ClickableSpan = SubTouchSpan(
            url,
            true,
            Color.BLUE,
            Color.RED,
            resources.getColor(R.color.white),
            resources.getColor(R.color.white)
        )
        strBuilder.setSpan(clickableLinkSpan, start, end, flags)
        strBuilder.removeSpan(span)
    }

    private fun getTranslationWithLinks(html: String): SpannableStringBuilder {
        val styledTranslation = Html.fromHtml(html) as SpannableStringBuilder
        val strBuilder = SpannableStringBuilder(styledTranslation)
        val urls = strBuilder.getSpans(
            0, styledTranslation.length,
            URLSpan::class.java
        )
        for (span in urls) {
            makeLinkClickable(
                strBuilder,
                span,
                span.url.replace("bword:\\/\\/".toRegex(), "").replace("EP_", "")
                    .replace("_".toRegex(), " ")
            )
        }
        return strBuilder
    }

    private var mDictionaryLoadertask: DictionaryLoader? = null

    private inner class DictionaryLoader :
        AsyncTask<String?, String?, Dictionary?>() {
        private var word: String? = null
        protected override fun doInBackground(vararg params: String?): Dictionary? {
            val fromLanguage = params[0]
            val toLanguage = params[1]
            word = params[2]
            try {
                mDictionary = Dictionary.getInstance(
                    context, fromLanguage, toLanguage
                )
                return mDictionary
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(db: Dictionary?) {
            mDictionary = db
            translate(word)
        }

        override fun onPreExecute() {
            mOfflineTranslationTextView!!.visibility = View.GONE
            mOfflineDictionaryLoading!!.visibility = View.VISIBLE
        }

        protected override fun onProgressUpdate(vararg messages: String?) {
            Toast.makeText(context, messages[0], Toast.LENGTH_LONG).show()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity: Activity? = activity
        if (activity != null) {
            //Todo: unpause player //((VideoPlayerActivity) activity).onDictionaryDialogDismissed();
        }
    }

    override fun onResume() {
        super.onResume()
        val window = dialog!!.window
        val size = Point()
        val display = window!!.windowManager.defaultDisplay
        val width: Double
        val height: Double
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            width = display.width.toDouble()
            height = display.height.toDouble()
        } else {
            display.getSize(size)
            width = size.x.toDouble()
            height = size.y.toDouble()
        }
        val screenSize = resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            window.setLayout((width * 0.75).toInt(), (height * 0.7).toInt())
            window.setGravity(Gravity.CENTER)
        } else {
            mOfflineTranslationTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            mOnlineTranslationTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            mCaptionTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            window.setLayout(width.toInt(), (height * 0.85).toInt())
            window.setGravity(Gravity.TOP)
        }
    }

    private fun makeWordsClickable(text: String?): SpannableStringBuilder {
        val ss = SpannableStringBuilder("")
        val pattern = "[-!$%^&*()_+|~=`{}\\[\\]:\\\";'<>?,.\\/\\s+]"
        val r = Pattern.compile(pattern)
        val words =
            text!!.split("(?=[-!$%^&*()_+|~=`{}\\[\\]:\\\";'<>?,.\\/\\s+])|(?<=[-!$%^&*()_+|~=`{}\\[\\]:\\\";'<>?,.\\/\\s+])")
                .toTypedArray()
        var start: Int
        var end: Int
        end = 0
        start = end
        var index = 0
        for (word in words) {
            index++
            ss.append(word)
            end = start + word.length
            if (!r.matcher(word).find()) {
                val clickableSpan: ClickableSpan = SubTouchSpan(
                    word,
                    false,
                    Color.BLACK,
                    resources.getColor(R.color.orange500),
                    Color.parseColor("#ffdfae"),
                    Color.parseColor("#ffdfae")
                )
                ss.setSpan(clickableSpan, start, end, 0)
            }
            start = end
        }
        return ss
    }

    private inner class LinkTouchMovementMethod : LinkMovementMethod() {
        private var mPressedSpan: SubTouchSpan? = null
        override fun onTouchEvent(
            textView: TextView,
            spannable: Spannable,
            event: MotionEvent
        ): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                mPressedSpan = getPressedSpan(textView, spannable, event)
                if (mPressedSpan != null) {
                    mPressedSpan!!.setPressed(true)
                    Selection.setSelection(
                        spannable, spannable.getSpanStart(mPressedSpan),
                        spannable.getSpanEnd(mPressedSpan)
                    )
                }
            } else if (event.action == MotionEvent.ACTION_MOVE) {
                val touchedSpan = getPressedSpan(textView, spannable, event)
                if (mPressedSpan != null && touchedSpan !== mPressedSpan) {
                    mPressedSpan!!.setPressed(false)
                    mPressedSpan = null
                    Selection.removeSelection(spannable)
                }
            } else {
                if (mPressedSpan != null) {
                    mPressedSpan!!.setPressed(false)
                    super.onTouchEvent(textView, spannable, event)
                }
                mPressedSpan = null
                Selection.removeSelection(spannable)
            }
            return true
        }

        private fun getPressedSpan(
            textView: TextView,
            spannable: Spannable,
            event: MotionEvent
        ): SubTouchSpan? {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= textView.totalPaddingLeft
            y -= textView.totalPaddingTop
            x += textView.scrollX
            y += textView.scrollY
            val layout = textView.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val link = spannable.getSpans(off, off, SubTouchSpan::class.java)
            var touchedSpan: SubTouchSpan? = null
            if (link.size > 0) {
                touchedSpan = link[0]
            }
            return touchedSpan
        }
    }

    private inner class SubTouchSpan constructor(
        private val mText: String,
        private val underlineText: Boolean,
        private val mNormalTextColor: Int,
        private val mPressedTextColor: Int,
        private val bgColor: Int,
        private val mPressedBackgroundColor: Int
    ) :
        ClickableSpan() {
        private var mIsPressed = false
        fun setPressed(isSelected: Boolean) {
            mIsPressed = isSelected
        }

        override fun onClick(widget: View) {
            translate(mText)
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = if (mIsPressed) mPressedTextColor else mNormalTextColor
            ds.bgColor = if (mIsPressed) mPressedBackgroundColor else bgColor
            ds.isUnderlineText = underlineText
        }
    }

    private fun focusOnView(scrollView: ScrollView?, view: View?) {
        scrollView!!.post { scrollView.scrollTo(0, requireView().top) }
    }

    companion object {
        private const val UNEXPECTED = 0
        private const val EXIST = 1
        private const val ADDED = 2
        private const val ANKI_STORAGE_PERMISSION = 3

        //last selected Items position
        private var mLastFromLangugeSelectedItem = 25
        private var mLastToLangugeSelectedItem = 84
        const val TAG = "VLC/DictionaryDialog"
        const val LAST_FROM_LANGUAGE_SELECTED = "last_from_language_selected"
        const val LAST_to_LANGUAGE_SELECTED = "last_to_language_selected"
        fun newInstance(dialog: String?, wordToTranslate: String?): DictionaryDialog {
            val dictionaryFragment = DictionaryDialog()
            val args = Bundle()
            args.putString("word", wordToTranslate)
            args.putString("dialog", dialog)
            dictionaryFragment.arguments = args
            return dictionaryFragment
        }
    }
}