package co.stone.sdk.sample.entries

import co.stone.sdk.R
import co.stone.sdk.sample.ActionTag
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_actionable.view.*
import com.xwray.groupie.kotlinandroidextensions.Item as Entry

class ActionEntry(
    private val iconRes: Int,
    private val labelRes: Int,
    private val actionTag: ActionTag,
    private val onClick: (ActionTag) -> Unit
) : Entry() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        with(viewHolder.itemView) {
            titleCallToAction.setText(labelRes)
            iconCallToAction.setImageResource(iconRes)
            setOnClickListener { onClick(actionTag) }
        }
    }

    override fun getLayout(): Int = R.layout.item_actionable
}