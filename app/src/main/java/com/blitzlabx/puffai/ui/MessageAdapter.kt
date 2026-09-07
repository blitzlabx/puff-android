package com.blitzlabx.puffai.ui

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blitzlabx.puffai.R
import com.blitzlabx.puffai.data.Message

class MessageAdapter(
    private val items: List<Message>,
    private val onThoughtClick: (String) -> Unit
) : RecyclerView.Adapter<MessageAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val bubble: LinearLayout = view.findViewById(R.id.bubble)
        val content: TextView = view.findViewById(R.id.content)
        val thoughtRow: View = view.findViewById(R.id.thoughtRow)
        val thoughtArrow: TextView = view.findViewById(R.id.thoughtArrow)
        val thoughtLabel: TextView = view.findViewById(R.id.thoughtLabel)
        val thoughtBody: TextView = view.findViewById(R.id.thoughtBody)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = items[position]
        val isUser = msg.role == "user"

        val params = holder.bubble.layoutParams as LinearLayout.LayoutParams
        params.gravity = if (isUser) Gravity.END else Gravity.START
        holder.bubble.layoutParams = params
        holder.bubble.setBackgroundResource(
            if (isUser) R.drawable.bubble_user else R.drawable.bubble_assistant
        )

        holder.content.text = msg.content

        if (!isUser && !msg.thought.isNullOrBlank()) {
            holder.thoughtRow.isVisible = true
            val secs = ((msg.thoughtMs ?: 0) / 1000.0)
            holder.thoughtLabel.text = "Thought for ${"%.1f".format(secs)}s"
            holder.thoughtArrow.text = if (msg.expanded) "↓" else "→"
            holder.thoughtBody.isVisible = msg.expanded
            holder.thoughtBody.text = msg.thought
            holder.thoughtRow.setOnClickListener { onThoughtClick(msg.id) }
        } else {
            holder.thoughtRow.isVisible = false
            holder.thoughtBody.isVisible = false
        }

        // Subtle entrance animation
        holder.itemView.alpha = 0f
        holder.itemView.translationY = 24f
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    override fun getItemCount() = items.size
}
