package com.example.landd.ui.task

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.landd.LANDDApplication.Companion.context
import com.example.landd.R
import com.example.landd.logic.model.Task
import kotlinx.android.synthetic.main.cell_download.view.*


class DownLoadAdapter(private var downloadList: MutableList<Task>) :
    RecyclerView.Adapter<DownLoadAdapter.DownLoadViewHolder>() {
    //需要外部访问，所以需要设置set方法，方便调用
    private var onItemClickListener: DownLoadAdapter.OnItemClickListener? = null

    //长按
    var mOnLongItemClickListener: DownLoadAdapter.OnRecyclerViewLongItemClickListener? = null

    //文件类型对应的资源文件
    private var imgMap = mapOf(
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
        "zip" to R.drawable.file_zip,
    )

    //set Header
    val TYPE_HEADER = 0
    val TYPE_NORMAL = 1
    private var mHeaderView: View? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    inner class DownLoadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var fileType: ImageView
        lateinit var pause: ImageView
        lateinit var fileName: TextView
        lateinit var downloadProcess: TextView
        lateinit var fileSpeed: TextView

        init {
            if (itemView != mHeaderView) {
                fileType = itemView.findViewById(R.id.fileType)
                pause = itemView.findViewById(R.id.pause)
                fileName = itemView.findViewById(R.id.fileName)
                downloadProcess = itemView.findViewById(R.id.doenLoadText)
                fileSpeed = itemView.findViewById(R.id.downloadSpeed)
                itemView.setOnClickListener { v -> //此处回传点击监听事件
                    onItemClickListener?.OnItemClick(v, downloadList.get(layoutPosition - 1))
                }
                //长按
                itemView.setOnLongClickListener { v ->
                    mOnLongItemClickListener?.onLongItemClick(v, adapterPosition)
                    true
                }
                itemView.pause.setOnClickListener {
                    if (pause.drawable.constantState?.equals(pause.resources.getDrawable(R.drawable.start).constantState) == true) {
                        pause.setImageResource(R.drawable.pause)
                    } else {
                        pause.setImageResource(R.drawable.start)
                    }
                }
            }
        }
    }

    /**
     * 设置item的监听事件的接口
     */
    interface OnItemClickListener {
        /**
         * 接口中的点击每一项的实现方法，参数自己定义
         */
        fun OnItemClick(view: View?, data: Task?)
    }


    fun setOnItemClickListener(onItemClickListener: DownLoadAdapter.OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    //长按
    fun setOnLongItemClickListener(listener: DownLoadAdapter.OnRecyclerViewLongItemClickListener?) {
        mOnLongItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownLoadViewHolder {
        //set Header
        if (mHeaderView != null && viewType == TYPE_HEADER)
            return DownLoadViewHolder(mHeaderView!!);

        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(R.layout.cell_download, parent, false)
        return DownLoadViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DownLoadViewHolder, position: Int) {
        //set Header
        if (getItemViewType(position) == TYPE_HEADER) return;
        val pos = getRealPosition(holder)

        val download: Task = downloadList[pos]   //position 换为pos
        val resid = imgMap.filter { download.file_type in it.key }
        if (resid.isEmpty()) {//没有匹配到文件
            holder.fileType.setImageResource(R.drawable.file_unknown)
        } else {
            holder.fileType.setImageResource(resid.values.toIntArray()[0])
        }
        holder.fileName.text = download.file_name
        holder.downloadProcess.text = download.file_size.toString()
        holder.fileSpeed.text = "100kb/s"
    }

    override fun getItemCount(): Int {
        //return downloadList.size
        return if (mHeaderView == null) downloadList.size else downloadList.size + 1
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

    fun getRealPosition(holder: DownLoadAdapter.DownLoadViewHolder): Int {
        val position = holder.layoutPosition
        return if (mHeaderView == null) position else position - 1
    }

    //长按删除
    open interface OnRecyclerViewLongItemClickListener {
        fun onLongItemClick(view: View?, position: Int)
    }
}