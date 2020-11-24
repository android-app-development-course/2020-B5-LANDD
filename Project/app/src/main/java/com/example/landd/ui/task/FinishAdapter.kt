package com.example.landd.ui.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.landd.LANDDApplication.Companion.context
import com.example.landd.R
import kotlinx.android.synthetic.main.cell_finish.view.*

class FinishAdapter(private var finishList: MutableList<FinishEntity>):
    RecyclerView.Adapter<FinishAdapter.FinishViewHolder>() {
    //需要外部访问，所以需要设置set方法，方便调用
    private var onItemClickListener: FinishAdapter.OnItemClickListener? = null
    //长按
    var mOnLongItemClickListener: FinishAdapter.OnRecyclerViewLongItemClickListener? = null

    //文件类型对应的资源文件
    private var imgMap=mapOf(
        "chm" to R.drawable.file_chm,
        "code" to R.drawable.file_code, "css" to R.drawable.file_css,
        "csv" to R.drawable.file_csv, "dmg" to R.drawable.file_dmg,
        "xlsx" to R.drawable.file_excel_office, "exe" to R.drawable.file_exe,
        "installtion" to R.drawable.file_installation_pa, "music" to R.drawable.file_music,
        "oa" to R.drawable.file_oa, "open" to R.drawable.file_open,
        "pdf" to R.drawable.file_pdf, "pic" to R.drawable.file_pic,
        "ppt" to R.drawable.file_ppt_office, "rtf" to R.drawable.file_rtf,
        "txt" to R.drawable.file_txt, "unknown" to R.drawable.file_unknown,
        "video" to R.drawable.file_video, "doc" to R.drawable.file_word_office,
        "zip" to R.drawable.file_zip
    )

    //set Header
    val TYPE_HEADER = 0
    val TYPE_NORMAL = 1
    private var mHeaderView: View? = null

    inner class FinishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var fileType: ImageView
        lateinit var del:ImageView
        lateinit var fileName: TextView
        lateinit var fileSize: TextView
        lateinit var finishTime: TextView

        init {
            if(itemView != mHeaderView)
            {
                fileType  = itemView.findViewById(R.id.Finish_fileType)
                del = itemView.findViewById(R.id.deleteView)
                fileName  = itemView.findViewById(R.id.file_name_finish)
                fileSize  = itemView.findViewById(R.id.file_size_finish)
                finishTime  = itemView.findViewById(R.id.finish_time)
                itemView.setOnClickListener { v -> //此处回传点击监听事件
                    onItemClickListener?.OnItemClick(v, finishList.get(layoutPosition))
                }
                //长按
                itemView.setOnLongClickListener { v ->
                    mOnLongItemClickListener?.onLongItemClick(v, adapterPosition)
                    true
                }
                itemView.deleteView.setOnClickListener{
                    if(finishList.size>0) {
                        finishList.removeAt(position-1);
                        notifyItemRemoved(position);
                        //删除后，为了防止position作乱调整位置,但是后面发现位置没有乱，保留此条是避免之后会遇到这种情况
                        notifyItemRangeChanged(position, finishList.size- position);
                        //Toast.makeText(context, "长按", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinishViewHolder {
        //set Header
        if(mHeaderView != null && viewType == TYPE_HEADER)
            return  FinishViewHolder(mHeaderView!!);

        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(R.layout.cell_finish, parent, false)
        return FinishViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FinishViewHolder, position: Int) {
        //set Header
        if(getItemViewType(position) == TYPE_HEADER) return;
        val pos = getRealPosition(holder)

        val finish: FinishEntity = finishList[pos]   //position 换为pos
        val resid =imgMap.filter { finish.getFileType().toString() in it.key }
        holder.fileType.setImageResource(resid.values.toIntArray()[0])
        holder.fileName.text = finish.getFileName()
        holder.fileSize.text = finish.getFileSize()
        holder.finishTime.text = finish.getFinishTime()
    }

    override fun getItemCount(): Int {
        return if (mHeaderView == null) finishList.size else finishList.size+ 1
    }

    /**
     * 设置item的监听事件的接口
     */
    interface OnItemClickListener {
        /**
         * 接口中的点击每一项的实现方法，参数自己定义
         */
        fun OnItemClick(view: View?, data: FinishEntity?)
    }

    fun setOnItemClickListener(onItemClickListener: FinishAdapter.OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    //长按
    fun setOnLongItemClickListener(listener: FinishAdapter.OnRecyclerViewLongItemClickListener?) {
        mOnLongItemClickListener = listener
    }

    //set Hearder
    open fun setHeaderView(headerView: View) {
        mHeaderView = headerView
        notifyItemInserted(0)
    }

    fun getHeaderView(): View? {
        return mHeaderView
    }

    override fun getItemViewType(position: Int): Int {
        if (mHeaderView == null) return TYPE_NORMAL
        return if (position == 0) TYPE_HEADER else TYPE_NORMAL
    }

    fun getRealPosition(holder:FinishAdapter.FinishViewHolder): Int {
        val position = holder.layoutPosition
        return if (mHeaderView == null) position else position - 1
    }

    //长按删除
    open interface OnRecyclerViewLongItemClickListener {
        fun onLongItemClick(view: View?, position: Int)
    }
}