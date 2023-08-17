package es.charles.holeesheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import es.charles.holeesheet.databinding.RowItemHoLeeMenuBinding

class HoLeeSheetMenuAdapter(
    private val onItemClickListener: ((HoLeeMenuItem) -> Unit)? = null
) : RecyclerView.Adapter<HoLeeSheetMenuViewHolder>() {

    private val differ: AsyncListDiffer<HoLeeMenuItem> = AsyncListDiffer(this, HoLeeSheetMenuDiffUtil())
    fun submitList(list: List<HoLeeMenuItem>) = differ.submitList(list)
    fun currentList() = differ.currentList

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoLeeSheetMenuViewHolder {
        val layaoutInflater = LayoutInflater.from(parent.context)
        val binding = RowItemHoLeeMenuBinding.inflate(layaoutInflater, parent, false)
        return HoLeeSheetMenuViewHolder(binding, onItemClickListener)
    }

    override fun getItemCount(): Int = differ.currentList.size
    override fun getItemId(position: Int): Long = differ.currentList[position].id

    override fun onBindViewHolder(holder: HoLeeSheetMenuViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.bind(item)
    }

    private class HoLeeSheetMenuDiffUtil : DiffUtil.ItemCallback<HoLeeMenuItem>() {
        override fun areItemsTheSame(oldItem: HoLeeMenuItem, newItem: HoLeeMenuItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HoLeeMenuItem, newItem: HoLeeMenuItem): Boolean {
            return oldItem == newItem
        }
    }
}

class HoLeeSheetMenuViewHolder (
    private val binding: RowItemHoLeeMenuBinding,
    private val onItemClickListener: ((HoLeeMenuItem) -> Unit)?
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HoLeeMenuItem) {
            binding.apply {
                tvTituloHoLeeSheetMenu.text = item.title
//                item.subtitle?.let { tvSubtituloHoLeeSheetMenu.text = it }
//                tvSubtituloHoLeeSheetMenu.show(item.subtitle != null)
                hoLeeSheetMenuIcon.setImageResource(item.icon)
                root.setOnClickListener {
                    onItemClickListener?.invoke(item)
                }
            }
        }
}
