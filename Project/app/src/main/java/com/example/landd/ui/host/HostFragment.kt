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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.landd.R
import com.example.landd.database.host.HostDataBases
import com.example.landd.database.host.HostRepository
import com.example.landd.logic.model.Host
import com.example.landd.logic.model.State
import com.kongzue.dialog.interfaces.OnInputDialogButtonClickListener
import com.kongzue.dialog.util.InputInfo
import com.kongzue.dialog.util.TextInfo
import com.kongzue.dialog.v3.InputDialog
import com.melnykov.fab.FloatingActionButton
import java.lang.NumberFormatException

class HostFragment : Fragment() {

    private lateinit var hostViewModel: HostViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dao = context?.let { HostDataBases.getInstance(it).hostDao }
        val repository = dao?.let { HostRepository(it) }
        hostViewModel = ViewModelProviders.of(requireActivity(),
            repository?.let { HostViewModelFactory(it) }).
        get(HostViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_host, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.hostRecyclerView)
        hostViewModel.hostList.addAll(
            arrayListOf(
                Host(1,"192.168.123.1", 1234, "", State.CONNECTED),
                Host(2,"192.168.123.2", 1234, "", State.UNUSED),
                Host(3,"192.168.123.3", 1234, "", State.UNAUTHORIZED),
                Host(4,"192.168.123.4", 1234, "", State.DISCONNECTED)
            )
        )

        val layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = HostAdapter(this, hostViewModel.hostList)
        recyclerView.layoutManager = layoutManager

        val hostFab = root.findViewById<FloatingActionButton>(R.id.hostFab)
        hostFab.setOnClickListener {
            InputDialog.show(
                activity as AppCompatActivity,
                "连接新主机",
                "输入主机Socket",
                "确定",
                "取消"
            )
                .setOnOkButtonClickListener(OnInputDialogButtonClickListener { baseDialog, v, inputStr ->
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
                    }
                    hostViewModel.hostList.add(Host(1,ip, port, "", State.CONNECTING))
                    recyclerView.adapter!!.notifyDataSetChanged()
                    return@OnInputDialogButtonClickListener false
                })
                .setOnCancelButtonClickListener(OnInputDialogButtonClickListener { _, _, _ ->
                    return@OnInputDialogButtonClickListener false
                })
                .setInputInfo(
                    InputInfo().setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                        .setTextInfo(TextInfo().setFontColor(Color.DKGRAY).setFontSize(16))
                        .setMultipleLines(true)
                )
                .setBackgroundColor(Color.TRANSPARENT)
        }
        return root
    }
}