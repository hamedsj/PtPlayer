package me.pitok.subtitle.components

import android.graphics.*
import android.text.style.LineBackgroundSpan
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign


class SubtitleBackgroundColorSpan(
    private val backgroundColor: Int,
    private val padding: Float,
    private val radius: Float
) : LineBackgroundSpan {

    private val rect = RectF()
    private val paint = Paint()
    private val paintStroke = Paint()
    private val path= Path()

    private var prevWidth = -1f
    private var prevLeft = -1f
    private var prevRight = -1f
    private var prevBottom = -1f
    private var prevTop = -1f

    init {
        paint.color = backgroundColor
        paintStroke.color = backgroundColor
    }

    override fun drawBackground(
        c: Canvas,
        p: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lnum: Int
    ) {

        val width = p.measureText(text, start, end) + 2f * padding
        val shift = (right - width) / 2f

        rect.set(shift, top.toFloat(), right - shift, bottom.toFloat())

        if (lnum == 0) {
            c.drawRoundRect(rect, radius, radius, paint);
        } else {
            path.reset();
            val dr = width - prevWidth;
            val diff = -sign(dr) * min(2f * radius, abs(dr / 2f)) /2f;
            path.moveTo(
                prevLeft, prevBottom - radius
            );

            path.cubicTo(
                prevLeft, prevBottom - radius,
                prevLeft, rect.top,
                prevLeft + diff, rect.top
            );
            path.lineTo(
                rect.left - diff, rect.top
            );
            path.cubicTo(
                rect.left - diff, rect.top,
                rect.left, rect.top,
                rect.left, rect.top + radius
            );
            path.lineTo(
                rect.left, rect.bottom - radius
            );
            path.cubicTo(
                rect.left, rect.bottom - radius,
                rect.left, rect.bottom,
                rect.left + radius, rect.bottom
            );
            path.lineTo(
                rect.right - radius, rect.bottom
            );
            path.cubicTo(
                rect.right - radius, rect.bottom,
                rect.right, rect.bottom,
                rect.right, rect.bottom - radius
            );
            path.lineTo(
                rect.right, rect.top + radius
            );
            path.cubicTo(
                rect.right, rect.top + radius,
                rect.right, rect.top,
                rect.right + diff, rect.top
            );
            path.lineTo(
                prevRight - diff, rect.top
            );
            path.cubicTo(
                prevRight - diff, rect.top,
                prevRight, rect.top,
                prevRight, prevBottom - radius
            );
            path.cubicTo(
                prevRight, prevBottom - radius,
                prevRight, prevBottom,
                prevRight - radius, prevBottom

            );
            path.lineTo(
                prevLeft + radius, prevBottom
            );
            path.cubicTo(
                prevLeft + radius, prevBottom,
                prevLeft, prevBottom,
                prevLeft, rect.top - radius
            );
            c.drawPath(path, paintStroke);
        }

        prevWidth = width;
        prevLeft = rect.left;
        prevRight = rect.right;
        prevBottom = rect.bottom;
        prevTop = rect.top;
    }
}