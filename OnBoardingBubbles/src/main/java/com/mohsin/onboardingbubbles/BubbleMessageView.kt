package com.mohsin.onboardingbubbles

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference
import java.util.ArrayList
import kotlin.math.roundToInt

class BubbleMessageView : ConstraintLayout {

    companion object {
        private const val WIDTH_ARROW = 20
    }

    private var itemView: View? = null

    private var imageViewIcon: ImageView? = null
    private var textViewTitle: TextView? = null
    private var textViewSubtitle: TextView? = null
    private var imageViewClose: ImageView? = null
    private var showCaseMessageViewLayout: ConstraintLayout? = null
    private var nextButton: Button? = null
    private var labelButton: TextView? = null

    private var targetViewScreenLocation: RectF? = null
    private var mBackgroundColor: Int = ContextCompat.getColor(context, R.color.blue_default)
    private var arrowPositionList = ArrayList<BubbleShowCase.ArrowPosition>()

    private var paint: Paint? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, builder: Builder) : super(context) {
        initView()
        setAttributes(builder)
        setBubbleListener(builder)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    private fun initView() {
        setWillNotDraw(false)

        inflateXML()
        bindViews()
    }

    private fun inflateXML() {
        itemView = inflate(context, R.layout.view_bubble_message, this)
    }

    private fun bindViews() {
        imageViewIcon = findViewById(R.id.imageViewShowCase)
        imageViewClose = findViewById(R.id.imageViewShowCaseClose)
        textViewTitle = findViewById(R.id.textViewShowCaseTitle)
        textViewSubtitle = findViewById(R.id.textViewShowCaseText)
        showCaseMessageViewLayout = findViewById(R.id.showCaseMessageViewLayout)
        nextButton = findViewById(R.id.nextButton)
        labelButton = findViewById(R.id.labelButton)
    }

    private fun setAttributes(builder: Builder) {
        if (builder.mImage != null) {
            imageViewIcon?.visibility = View.VISIBLE
            imageViewIcon?.setImageDrawable(builder.mImage!!)
        }
        if (builder.mCloseAction != null) {
            imageViewClose?.visibility = View.VISIBLE
            imageViewClose?.setImageDrawable(builder.mCloseAction!!)
        }

        if (builder.mDisableCloseAction != null && builder.mDisableCloseAction!!) {
            imageViewClose?.visibility = View.INVISIBLE
        }

        builder.mTitle?.let {
            textViewTitle?.visibility = View.VISIBLE
            textViewTitle?.text = builder.mTitle
        }
        builder.mSubtitle?.let {
            textViewSubtitle?.visibility = View.VISIBLE
            textViewSubtitle?.text = builder.mSubtitle
        }
        builder.mTextColor?.let {
            textViewTitle?.setTextColor(builder.mTextColor!!)
            textViewSubtitle?.setTextColor(builder.mTextColor!!)
        }
        builder.mTitleTextSize?.let {
            textViewTitle?.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                builder.mTitleTextSize!!.toFloat()
            )
        }
        builder.mSubtitleTextSize?.let {
            textViewSubtitle?.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                builder.mSubtitleTextSize!!.toFloat()
            )
        }
        builder.mBackgroundColor?.let { mBackgroundColor = builder.mBackgroundColor!! }
        arrowPositionList = builder.mArrowPosition
        targetViewScreenLocation = builder.mTargetViewScreenLocation

        builder.mNextButtonText?.let { text ->
            nextButton?.visibility = View.VISIBLE
            nextButton?.text = text
        }

        builder.mLabelButtonText?.let { text ->
            labelButton?.visibility = View.VISIBLE
            labelButton?.text = text
        }
    }

    private fun setBubbleListener(builder: Builder) {
        imageViewClose?.setOnClickListener { builder.mListener?.onCloseActionImageClick() }
        itemView?.setOnClickListener { builder.mListener?.onBubbleClick() }
        nextButton?.setOnClickListener { builder.mListener?.onNextButtonClick() }
        labelButton?.setOnClickListener { builder.mListener?.onLabelButtonClick() }
    }

    private fun getViewWidth(): Int = width

    private fun getMargin(): Int = ScreenUtils.dpToPx(20)

    private fun getSecurityArrowMargin(): Int {
        return getMargin() + ScreenUtils.dpToPx(2 * WIDTH_ARROW / 3)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        prepareToDraw()
        drawRectangle(canvas)

        for (arrowPosition in arrowPositionList) {
            drawArrow(canvas, arrowPosition, targetViewScreenLocation)
        }
    }

    private fun prepareToDraw() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.color = mBackgroundColor
        paint!!.style = Paint.Style.FILL
        paint!!.strokeWidth = 4.0f
    }

    private fun drawRectangle(canvas: Canvas) {
        val rect = RectF(
            getMargin().toFloat(),
            getMargin().toFloat(),
            getViewWidth() - getMargin().toFloat(),
            height - getMargin().toFloat()
        )
        canvas.drawRoundRect(rect, 10f, 10f, paint!!)
    }

    private fun drawArrow(
        canvas: Canvas,
        arrowPosition: BubbleShowCase.ArrowPosition,
        targetViewLocationOnScreen: RectF?
    ) {
        val xPosition: Int
        val yPosition: Int

        when (arrowPosition) {
            BubbleShowCase.ArrowPosition.LEFT -> {
                xPosition = getMargin()
                yPosition =
                    if (targetViewLocationOnScreen != null) getArrowVerticalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else height / 2
            }

            BubbleShowCase.ArrowPosition.RIGHT -> {
                xPosition = getViewWidth() - getMargin()
                yPosition =
                    if (targetViewLocationOnScreen != null) getArrowVerticalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else height / 2
            }

            BubbleShowCase.ArrowPosition.TOP -> {
                xPosition =
                    if (targetViewLocationOnScreen != null) getArrowHorizontalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else width / 2
                yPosition = getMargin()
            }

            BubbleShowCase.ArrowPosition.BOTTOM -> {
                xPosition =
                    if (targetViewLocationOnScreen != null) getArrowHorizontalPositionDependingOnTarget(
                        targetViewLocationOnScreen
                    ) else width / 2
                yPosition = height - getMargin()
            }
        }

        drawRhombus(canvas, paint, xPosition, yPosition, ScreenUtils.dpToPx(WIDTH_ARROW))
    }

    private fun getArrowHorizontalPositionDependingOnTarget(targetViewLocationOnScreen: RectF?): Int {
        val xPosition: Int = when {
            isOutOfRightBound(targetViewLocationOnScreen) -> width - getSecurityArrowMargin()
            isOutOfLeftBound(targetViewLocationOnScreen) -> getSecurityArrowMargin()
            else -> (targetViewLocationOnScreen!!.centerX() - ScreenUtils.getAxisXPositionOfViewOnScreen(
                this
            )).roundToInt()
        }
        return xPosition
    }

    private fun getArrowVerticalPositionDependingOnTarget(targetViewLocationOnScreen: RectF?): Int {
        val yPosition: Int = when {
            isOutOfBottomBound(targetViewLocationOnScreen) -> height - getSecurityArrowMargin()
            isOutOfTopBound(targetViewLocationOnScreen) -> getSecurityArrowMargin()
            else -> (targetViewLocationOnScreen!!.centerY() + ScreenUtils.getStatusBarHeight(context) - ScreenUtils.getAxisYPositionOfViewOnScreen(
                this
            )).roundToInt()
        }
        return yPosition
    }

    private fun isOutOfRightBound(targetViewLocationOnScreen: RectF?): Boolean {
        return targetViewLocationOnScreen!!.centerX() > ScreenUtils.getAxisXPositionOfViewOnScreen(
            this
        ) + width - getSecurityArrowMargin()
    }

    private fun isOutOfLeftBound(targetViewLocationOnScreen: RectF?): Boolean {
        return targetViewLocationOnScreen!!.centerX() < ScreenUtils.getAxisXPositionOfViewOnScreen(
            this
        ) + getSecurityArrowMargin()
    }

    private fun isOutOfBottomBound(targetViewLocationOnScreen: RectF?): Boolean {
        return targetViewLocationOnScreen!!.centerY() > ScreenUtils.getAxisYPositionOfViewOnScreen(
            this
        ) + height - getSecurityArrowMargin() - ScreenUtils.getStatusBarHeight(context)
    }

    private fun isOutOfTopBound(targetViewLocationOnScreen: RectF?): Boolean {
        return targetViewLocationOnScreen!!.centerY() < ScreenUtils.getAxisYPositionOfViewOnScreen(
            this
        ) + getSecurityArrowMargin() - ScreenUtils.getStatusBarHeight(context)
    }

    private fun drawRhombus(canvas: Canvas, paint: Paint?, x: Int, y: Int, width: Int) {
        val halfRhombusWidth = width / 2

        val path = Path()
        path.moveTo(x.toFloat(), (y + halfRhombusWidth).toFloat()) // Top
        path.lineTo((x - halfRhombusWidth).toFloat(), y.toFloat()) // Left
        path.lineTo(x.toFloat(), (y - halfRhombusWidth).toFloat()) // Bottom
        path.lineTo((x + halfRhombusWidth).toFloat(), y.toFloat()) // Right
        path.lineTo(x.toFloat(), (y + halfRhombusWidth).toFloat()) // Back to Top
        path.close()

        canvas.drawPath(path, paint!!)
    }


    //END REGION

    /**
     * Builder for BubbleMessageView class
     */
    class Builder {
        private lateinit var mContext: WeakReference<Context>
        var mTargetViewScreenLocation: RectF? = null
        var mImage: Drawable? = null
        var mDisableCloseAction: Boolean? = null
        var mTitle: String? = null
        var mSubtitle: String? = null
        var mCloseAction: Drawable? = null
        var mBackgroundColor: Int? = null
        var mTextColor: Int? = null
        var mTitleTextSize: Int? = null
        var mSubtitleTextSize: Int? = null
        var mArrowPosition = ArrayList<BubbleShowCase.ArrowPosition>()
        var mListener: OnBubbleMessageViewListener? = null
        var mNextButtonText: String? = null
        var mLabelButtonText: String? = null

        fun from(context: Context): Builder {
            mContext = WeakReference(context)
            return this
        }

        fun title(title: String?): Builder {
            mTitle = title
            return this
        }

        fun subtitle(subtitle: String?): Builder {
            mSubtitle = subtitle
            return this
        }

        fun image(image: Drawable?): Builder {
            mImage = image
            return this
        }

        fun closeActionImage(image: Drawable?): Builder {
            mCloseAction = image
            return this
        }

        fun disableCloseAction(isDisabled: Boolean): Builder {
            mDisableCloseAction = isDisabled
            return this
        }

        fun targetViewScreenLocation(targetViewLocationOnScreen: RectF): Builder {
            mTargetViewScreenLocation = targetViewLocationOnScreen
            return this
        }

        fun backgroundColor(backgroundColor: Int?): Builder {
            mBackgroundColor = backgroundColor
            return this
        }

        fun textColor(textColor: Int?): Builder {
            mTextColor = textColor
            return this
        }

        fun titleTextSize(textSize: Int?): Builder {
            mTitleTextSize = textSize
            return this
        }

        fun subtitleTextSize(textSize: Int?): Builder {
            mSubtitleTextSize = textSize
            return this
        }

        fun arrowPosition(arrowPosition: List<BubbleShowCase.ArrowPosition>): Builder {
            mArrowPosition.clear()
            mArrowPosition.addAll(arrowPosition)
            return this
        }

        fun nextButtonText(text: String?): Builder {
            mNextButtonText = text
            return this
        }

        fun labelButtonText(text: String?): Builder {
            mLabelButtonText = text
            return this
        }

        fun listener(listener: OnBubbleMessageViewListener?): Builder {
            mListener = listener
            return this
        }

        fun build(): BubbleMessageView {
            return BubbleMessageView(mContext.get()!!, this)
        }
    }
}