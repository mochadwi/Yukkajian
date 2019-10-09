package io.mochadwi.yukmengaji.Adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotifyingLinearLayoutManager(context: Context) :
    LinearLayoutManager(context, RecyclerView.VERTICAL, false) {

    private var listener: OnLayoutCompleteListener? = null

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        if (state?.itemCount!! > 0) {
            listener?.onLayoutComplete()
        }
    }

    fun isLastItemCompletelyVisible() = findLastCompletelyVisibleItemPosition() == itemCount - 1

    /**
     * Set a listener that will be notified when a layout is complete.
     *
     * @param listener The listener to notify
     */
    fun setOnLayoutCompleteListener(listener: OnLayoutCompleteListener) {
        this.listener = listener
    }

    interface OnLayoutCompleteListener {
        fun onLayoutComplete()
    }
}