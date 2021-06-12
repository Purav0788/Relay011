package com.example.relay

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View


abstract class SimpleTouchListener : View.OnTouchListener {
    /**
     * Flag determining whether the down touch has stayed with the bounds of the view.
     */
    private var touchStayedWithinViewBounds = false
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStayedWithinViewBounds = true
                onDownTouchAction()
                true
            }
            MotionEvent.ACTION_UP -> {
                if (touchStayedWithinViewBounds) {
                    onUpTouchAction()
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchStayedWithinViewBounds
                    && !isMotionEventInsideView(view, event)
                ) {
                    onCancelTouchAction()
                    touchStayedWithinViewBounds = false
                }
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                onCancelTouchAction()
                true
            }
            else -> false
        }
    }

    /**
     * Method which is called when the [View] is touched down.
     */
    abstract fun onDownTouchAction()

    /**
     * Method which is called when the down touch is released on the [View].
     */
    abstract fun onUpTouchAction()

    /**
     * Method which is called when the down touch is canceled,
     * e.g. because the down touch moved outside the bounds of the [View].
     */
    abstract fun onCancelTouchAction()

    /**
     * Determines whether the provided [MotionEvent] represents a touch event
     * that occurred within the bounds of the provided [View].
     *
     * @param view  the [View] to which the [MotionEvent] has been dispatched.
     * @param event the [MotionEvent] of interest.
     * @return true iff the provided [MotionEvent] represents a touch event
     * that occurred within the bounds of the provided [View].
     */
    private fun isMotionEventInsideView(view: View, event: MotionEvent): Boolean {
        val viewRect = Rect(
            view.getLeft(),
            view.getTop(),
            view.getRight(),
            view.getBottom()
        )
        return viewRect.contains(
            view.getLeft() + event.x.toInt(),
            view.getTop() + event.y.toInt()
        )
    }
}