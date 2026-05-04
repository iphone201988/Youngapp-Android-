import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tech.young.R
import com.tech.young.data.model.GetPortfolioData
import com.tech.young.databinding.ItemInvestmentBinding

class InvestmentAdapter(
    private var list: List<GetPortfolioData>,
    private val onItemClick: (GetPortfolioData) -> Unit
) : RecyclerView.Adapter<InvestmentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemInvestmentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvestmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        with(holder.binding) {
            tvSymbol.text = item.symbol
            tvName.text = if (item.name.isNullOrEmpty()) item.symbol else item.name
            tvType.text = item.type.replace("_", " ").capitalize()
            tvValue.text = "$${item.currentValue}"

            // Check if performing well to show up or down arrow
            if (item.investmentPerformingWell == "outperforming" || item.investmentPerformingWell == "meeting_expectations") {
                ivStatus.setImageResource(R.drawable.iv_up_arrow)
            } else {
                ivStatus.setImageResource(R.drawable.iv_down_arrow)
            }

            root.setOnClickListener {
                onItemClick(item)
            }

        }
    }

    fun updateList(newList: List<GetPortfolioData>) {
        list = newList
        notifyDataSetChanged()
    }
}
