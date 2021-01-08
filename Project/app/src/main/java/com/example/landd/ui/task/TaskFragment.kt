package com.example.landd.ui.task


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.landd.DownloadUtil
import com.example.landd.R
import com.example.landd.logic.model.Task
import com.example.landd.ui.host.HostViewModel
import com.kongzue.dialog.interfaces.OnInputDialogButtonClickListener
import com.kongzue.dialog.util.InputInfo
import com.kongzue.dialog.util.TextInfo
import com.kongzue.dialog.v3.InputDialog
import com.melnykov.fab.FloatingActionButton
import kotlin.concurrent.thread


class TaskFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel
    private val downLoadList: MutableList<Task> = ArrayList()
    private val finishList: MutableList<Task> = ArrayList()

    //set Header
    var adapter: DownLoadAdapter? = null
    var adapter2: FinishAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_task, container, false)
        val fab: FloatingActionButton = root.findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            InputDialog.show(activity as AppCompatActivity, "新建任务", "请输入网址url", "确定", "取消")
                .setOnOkButtonClickListener(OnInputDialogButtonClickListener { baseDialog, v, inputStr ->
                    thread {
                        DownloadUtil.newTask("https://www.baidu.com")
                        activity?.runOnUiThread {
                            taskViewModel.getDownLoad()
                            adapter?.notifyDataSetChanged()
                        }
                    }

                    return@OnInputDialogButtonClickListener false
                })
                .setOnCancelButtonClickListener(OnInputDialogButtonClickListener { baseDialog, v, inputStr ->
                    return@OnInputDialogButtonClickListener false
                })
                .setInputInfo(
                    InputInfo()
                        .setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                        .setTextInfo(
                            TextInfo()  //设置文字样式
                                .setFontColor(Color.DKGRAY)
                                .setFontSize(16)
                        )
                        .setMultipleLines(true)//支持多行输入
                ).backgroundColor = Color.TRANSPARENT
        }

        //下载页面
        Log.d("where", "after initContents()")
        initDownLoadContents()
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val download: RecyclerView = root.findViewById(R.id.DownLoad)
        download.layoutManager = linearLayoutManager
        adapter = DownLoadAdapter(downLoadList)
        download.adapter = adapter
        setHeader(download);

        //RecyclerView中没有item的监听事件，需要自己在适配器中写一个监听事件的接口。参数根据自定义
        adapter!!.setOnItemClickListener(object : DownLoadAdapter.OnItemClickListener {
            @SuppressLint("ShowToast")
            override fun OnItemClick(view: View?, data: Task?) {
                Toast.makeText(context, "点击了item", Toast.LENGTH_SHORT).show()
            }
        })

        //长按
        //item的长按事件
        adapter!!.setOnLongItemClickListener(object :
            DownLoadAdapter.OnRecyclerViewLongItemClickListener {
            override fun onLongItemClick(view: View?, position: Int) {
                if (downLoadList.size > 0) {
                    downLoadList.removeAt(position - 1);
                    adapter!!.notifyItemRemoved(position);
                    Toast.makeText(context, position.toString(), Toast.LENGTH_SHORT).show()
                    //删除后，为了防止position作乱调整位置,但是后面发现位置没有乱，保留此条是避免之后会遇到这种情况
                    adapter!!.notifyItemRangeChanged(position, downLoadList.size - position);
                    //Toast.makeText(context, "长按", Toast.LENGTH_SHORT).show()
                }
            }
        })

        //完成页面
        initFinishContents()
        val finish: RecyclerView = root.findViewById(R.id.Finish)
        val linearLayoutManager2 = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        finish.layoutManager = linearLayoutManager2
        adapter2 = FinishAdapter(finishList)
        finish.adapter = adapter2
        setHeader2(finish);

        //RecyclerView中没有item的监听事件，需要自己在适配器中写一个监听事件的接口。参数根据自定义
        adapter2!!.setOnItemClickListener(object : FinishAdapter.OnItemClickListener {
            @SuppressLint("ShowToast")
            override fun OnItemClick(view: View?, data: Task?) {
                Toast.makeText(context, "点击了item", Toast.LENGTH_SHORT).show()
            }
        })
        //长按
        //item的长按事件
        adapter2!!.setOnLongItemClickListener(object :
            FinishAdapter.OnRecyclerViewLongItemClickListener {
            override fun onLongItemClick(view: View?, position: Int) {
                if (finishList.size > 0) {
                    finishList.removeAt(position - 1);
                    adapter2!!.notifyItemRemoved(position);
                    Toast.makeText(context, position.toString(), Toast.LENGTH_SHORT).show()
                    //删除后，为了防止position作乱调整位置,但是后面发现位置没有乱，保留此条是避免之后会遇到这种情况
                    adapter2!!.notifyItemRangeChanged(position, finishList.size - position);
                    //Toast.makeText(context, "长按", Toast.LENGTH_SHORT).show()
                }
            }
        })
        return root
    }

    private fun initDownLoadContents() {
        for (i in 0..2) {
            val l1 = Task(
                "", "1.txt",
                1234, "txt", "2020-11-10 0:04", false
            )
            downLoadList.add(l1)
            val l2 = Task(
                "", "weixin.exe", 1234,
                "exe", "2020-12-10 0:04", false
            )
            downLoadList.add(l2)
        }
    }

    private fun initFinishContents() {
        for (i in 0..2) {
            val l1 = Task(
                "", "电影鉴赏.ppt", 1234,
                "ppt", "2020-11-10 0:04", true
            )
            finishList.add(l1)
            val l2 = Task(
                "", "软件测试.xlxs", 1234,
                "xlsx", "2020-11-20 12:00", true
            )
            finishList.add(l2)
        }
    }

    //set Header 下载
    private fun setHeader(view: RecyclerView) {
        val header: View = LayoutInflater.from(context).inflate(R.layout.header, view, false)
        adapter?.setHeaderView(header)
    }

    //set Header 完成
    private fun setHeader2(view: RecyclerView) {
        val header: View = LayoutInflater.from(context).inflate(R.layout.header2, view, false)
        adapter2?.setHeaderView(header)
    }
}

