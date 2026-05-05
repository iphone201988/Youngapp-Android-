package com.tech.young.ui.share_screen


import androidx.media3.common.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tech.young.data.model.MenuItem
import com.tech.young.databinding.ItemLayoutTopicsBinding
import kotlin.math.log

class ExpandableMenuAdapter(
    private val list: MutableList<MenuItem>,
    private val onSelectionChanged: (List<MenuItem>) -> Unit
) : RecyclerView.Adapter<ExpandableMenuAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLayoutTopicsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutTopicsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.tvTitle.text = item.title

        // 🔹 CHILD / HEADER UI
        // Checking both type and isChild to ensure indentation works even if only one is set
        if (item.type == MenuItem.CHILD || item.isChild) {
            holder.binding.ivFolder.visibility = View.GONE
            holder.binding.rootLayout.setPadding(60, 12, 16, 12)
        } else {
            holder.binding.ivFolder.visibility =
                if (item.children.isNotEmpty()) View.VISIBLE else View.GONE
            holder.binding.rootLayout.setPadding(16, 12, 16, 12)
        }

        // 🔹 TICK ONLY FOR CHILD
        // Robust check for child type and selection state
        if ((item.type == MenuItem.CHILD || item.isChild) && item.isSelected) {
            holder.binding.ivTick.visibility = View.VISIBLE
        } else {
            holder.binding.ivTick.visibility = View.GONE
        }

        // 🔹 CLICK
        holder.itemView.setOnClickListener {
            // Use bindingAdapterPosition to avoid stale index issues in expandable lists
            val currentPos = holder.bindingAdapterPosition
            if (currentPos == RecyclerView.NO_POSITION) return@setOnClickListener

            val currentItem = list[currentPos]

            if (currentItem.type == MenuItem.HEADER) {
                val index = list.indexOf(currentItem)
                if (index == -1) return@setOnClickListener

                if (currentItem.isExpanded) {
                    list.removeAll(currentItem.children)
                    currentItem.isExpanded = false
                } else {
                    list.addAll(index + 1, currentItem.children)
                    currentItem.isExpanded = true
                }
                notifyDataSetChanged()
            } else {
                // ✅ TOGGLE SELECTION
                currentItem.isSelected = !currentItem.isSelected

                if (currentItem.isSelected) {
                    if (currentItem.title.equals("Other", ignoreCase = true)) {
                        // If "Other" is selected, deselect all other items
                        list.forEach { item ->
                            if (item != currentItem) {
                                item.isSelected = false
                            }
                            item.children.forEach { child ->
                                if (child != currentItem) {
                                    child.isSelected = false
                                }
                            }
                        }
                        notifyDataSetChanged()
                    } else {
                        // If a non-"Other" item is selected, deselect "Other"
                        var otherDeselected = false
                        list.forEach { item ->
                            if (item.title.equals("Other", ignoreCase = true) && item.isSelected) {
                                item.isSelected = false
                                otherDeselected = true
                            }
                            item.children.forEach { child ->
                                if (child.title.equals("Other", ignoreCase = true) && child.isSelected) {
                                    child.isSelected = false
                                    otherDeselected = true
                                }
                            }
                        }
                        if (otherDeselected) {
                            notifyDataSetChanged()
                        } else {
                            notifyItemChanged(currentPos)
                        }
                    }
                } else {
                    notifyItemChanged(currentPos)
                }

                // ✅ SEND SELECTED LIST
                onSelectionChanged(getSelectedItems())
            }
        }
    }

    override fun getItemCount(): Int = list.size

    // 🔹 SELECTED ITEMS
    private fun getSelectedItems(): List<MenuItem> {
        val selected = mutableListOf<MenuItem>()

        list.forEach { item ->
            if ((item.type == MenuItem.CHILD || item.isChild) && item.isSelected) {
                selected.add(item)
            }

            item.children.forEach { child ->
                if (child.isSelected) {
                    selected.add(child)
                }
            }
        }

        return selected.distinct()
    }
}
