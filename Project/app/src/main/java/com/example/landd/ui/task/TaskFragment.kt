package com.example.landd.ui.task


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.landd.DownloadUtil
import com.example.landd.LANDDApplication
import com.example.landd.R
import com.example.landd.database.AppDataBase
import com.example.landd.logic.model.Task
import com.kongzue.dialog.interfaces.OnInputDialogButtonClickListener
import com.kongzue.dialog.util.InputInfo
import com.kongzue.dialog.util.TextInfo
import com.kongzue.dialog.v3.InputDialog
import com.melnykov.fab.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class TaskFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel

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

        //下载页面
        Log.d("where", "after initContents()")
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val download: RecyclerView = root.findViewById(R.id.DownLoad)
        download.layoutManager = linearLayoutManager
        adapter = DownLoadAdapter(this, taskViewModel.downloadingTaskList)
        download.adapter = adapter
        setHeader(download)
        taskViewModel.downloadingTaskList.observe(requireActivity()) {
            requireActivity().runOnUiThread {
                download.adapter!!.notifyDataSetChanged()
            }
        }

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
                taskViewModel.downloadingTaskList.value?.let { downLoadList ->
                    if (downLoadList.size > 0) {
                        val task = downLoadList.removeAt(position - 1);
                        GlobalScope.launch(Dispatchers.IO) {
                            AppDataBase.getDatabase().taskDao().delete(task)
                        }
                        adapter!!.notifyItemRemoved(position);
                        Toast.makeText(context, position.toString(), Toast.LENGTH_SHORT).show()
                        //删除后，为了防止position作乱调整位置,但是后面发现位置没有乱，保留此条是避免之后会遇到这种情况
                        adapter!!.notifyItemRangeChanged(position, downLoadList.size - position);
                        //Toast.makeText(context, "长按", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        //完成页面
        val finish: RecyclerView = root.findViewById(R.id.Finish)
        val linearLayoutManager2 = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        finish.layoutManager = linearLayoutManager2
        adapter2 = FinishAdapter(this, taskViewModel.finishedTaskList)
        finish.adapter = adapter2
        setHeader2(finish);
        taskViewModel.finishedTaskList.observe(requireActivity()) {
            requireActivity().runOnUiThread {
                finish.adapter!!.notifyDataSetChanged()
            }
        }

        taskViewModel.refreshTaskListInIOThread()

        //RecyclerView中没有item的监听事件，需要自己在适配器中写一个监听事件的接口。参数根据自定义
        adapter2!!.setOnItemClickListener(object : FinishAdapter.OnItemClickListener {
            @SuppressLint("ShowToast")
            override fun OnItemClick(view: View?, data: Task?) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.data = Uri.parse("/storage/emulated/0/Download")
                intent.type = "*/*"
                startActivity(intent)
            }
        })
        //长按
        //item的长按事件
        adapter2!!.setOnLongItemClickListener(object :
            FinishAdapter.OnRecyclerViewLongItemClickListener {
            override fun onLongItemClick(view: View?, position: Int) {
                taskViewModel.finishedTaskList.value?.let { finishList ->
                    if (finishList.size > 0) {
                        val task = finishList.removeAt(position - 1);
                        GlobalScope.launch(Dispatchers.IO) {
                            AppDataBase.getDatabase().taskDao().delete(task)
                        }
                        adapter2!!.notifyItemRemoved(position);
                        Toast.makeText(context, position.toString(), Toast.LENGTH_SHORT).show()
                        //删除后，为了防止position作乱调整位置,但是后面发现位置没有乱，保留此条是避免之后会遇到这种情况
                        adapter2!!.notifyItemRangeChanged(position, finishList.size - position);
                        //Toast.makeText(context, "长按", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        val fab: FloatingActionButton = root.findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            InputDialog.show(activity as AppCompatActivity, "新建任务", "请输入网址url", "确定", "取消")
                .setOnOkButtonClickListener(OnInputDialogButtonClickListener { baseDialog, v, inputStr ->
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            DownloadUtil.newTask(inputStr)
                            taskViewModel.refreshTaskList()
                        } catch (e: Exception) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "错误：${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
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

        return root
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

