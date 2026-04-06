import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tech.young.R
import com.tech.young.data.InvestmentItem
import com.tech.young.databinding.ItemInvestmentBinding

class InvestmentAdapter(
    private val list: List<InvestmentItem>
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
            tvName.text = item.name
            tvType.text = item.type
            tvValue.text = item.value

            if (item.isUp) {
                ivStatus.setImageResource(R.drawable.iv_up_arrow)
            } else {
                ivStatus.setImageResource(R.drawable.iv_down_arrow)
            }
        }
    }
}