package com.example.landd.ui.host

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.landd.R
import com.example.landd.database.AppDataBase
import com.example.landd.logic.model.Host
import com.example.landd.logic.model.State
import com.kongzue.dialog.interfaces.OnInputDialogButtonClickListener
import com.kongzue.dialog.util.InputInfo
import com.kongzue.dialog.util.TextInfo
import com.kongzue.dialog.v3.InputDialog
import com.melnykov.fab.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.NumberFormatException

class HostFragment : Fragment() {

    private lateinit var hostViewModel: HostViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hostViewModel = ViewModelProvider(this).get(HostViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_host, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.hostRecyclerView)

        val layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = HostAdapter(this, hostViewModel.hostList)
        recyclerView.layoutManager = layoutManager
        hostViewModel.hostList.observe(requireActivity()) {
            recyclerView.adapter!!.notifyDataSetChanged()
        }
        hostViewModel.refreshHostListInIOThread()


        val hostFab = root.findViewById<FloatingActionButton>(R.id.hostFab)
        hostFab.setOnClickListener {
            InputDialog.show(
                activity as AppCompatActivity,
                "连接新主机",
                "输入主机Socket",
                "确定",
                "取消"
            )
                .setOnOkButtonClickListener(OnInputDialogButtonClickListener { _, _, inputStr ->
                    val result = inputStr.split(":")
                    if (result.size != 2) {
                        Toast.makeText(context, "输入应该为 ip:post 的形式", Toast.LENGTH_SHORT).show()
                        return@OnInputDialogButtonClickListener false
                    }
                    val ip = result[0]
                    var port = 0
                    try {
                        port = result[1].toInt()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "不正确的端口", Toast.LENGTH_SHORT).show()
                        return@OnInputDialogButtonClickListener false
                    }
                    val host = Host(ip, port, "", "", State.CONNECTING)
                    GlobalScope.launch(Dispatchers.IO) {
                        AppDataBase.getDatabase().hostDao().insert(host)
                        hostViewModel.refreshHostList()
                    }
                    return@OnInputDialogButtonClickListener false
                })
                .setOnCancelButtonClickListener(OnInputDialogButtonClickListener { _, _, _ ->
                    return@OnInputDialogButtonClickListener false
                })
                .setInputInfo(
                    InputInfo().setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                        .setTextInfo(TextInfo().setFontColor(Color.DKGRAY).setFontSize(16))
                        .setMultipleLines(true)
                ).backgroundColor = Color.TRANSPARENT
        }
        return root
    }
}