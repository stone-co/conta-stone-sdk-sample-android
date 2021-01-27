package co.stone.sdk.sample.entries

import co.stone.sdk.R
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item as Entry

class LoadingEntry : Entry() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) = Unit

    override fun getLayout(): Int = R.layout.item_loading

    override fun getSpanSize(spanCount: Int, position: Int): Int  = 2
}