package com.example.landd.ui.host

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.landd.DownloadUtil
import com.example.landd.LANDDApplication
import com.example.landd.R
import com.example.landd.database.AppDataBase
import com.example.landd.logic.model.Host
import com.example.landd.logic.model.State
import com.google.android.material.switchmaterial.SwitchMaterial
import com.kongzue.dialog.interfaces.OnInputDialogButtonClickListener
import com.kongzue.dialog.util.InputInfo
import com.kongzue.dialog.util.TextInfo
import com.kongzue.dialog.v3.InputDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HostAdapter(private val fragment: HostFragment, private val hostList: LiveData<List<Host>>) :
    RecyclerView.Adapter<HostAdapter.ViewHolder>() {
    inner class ViewHolder(view: View, val refreshAnimation: Animation) :
        RecyclerView.ViewHolder(view) {
        val hostStatusColor: View = view.findViewById(R.id.hostStatusColor)
        val hostRefresh: ImageView = view.findViewById(R.id.hostRefresh)
        val hostUse: SwitchMaterial = view.findViewById(R.id.hostUse)
        val hostWarning: ImageView = view.findViewById(R.id.hostWarning)
        val hostSocket: TextView = view.findViewById(R.id.hostSocket)
        val hostStatusText: TextView = view.findViewById(R.id.hostStatusText)
    }

    private fun syncToDB(host: Host) {
        GlobalScope.launch(Dispatchers.IO) {
            AppDataBase.getDatabase().hostDao().update(host)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_host, parent, false)
        val refreshAnimation = AnimationUtils.loadAnimation(parent.context, R.anim.refresh)
        refreshAnimation.repeatCount = Animation.INFINITE
        refreshAnimation.repeatMode = Animation.RESTART
        return ViewHolder(view, refreshAnimation)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val host = hostList.value!![position]
        holder.hostSocket.text = "${host.ip}:${host.port}"
        when (host.state) {
            State.CONNECTED -> {
                holder.hostStatusColor.setBackgroundColor(
                    LANDDApplication.context.resources.getColor(
                        R.color.green
                    )
                )
                holder.hostRefresh.visibility = View.GONE
                holder.hostWarning.visibility = View.GONE
                holder.hostStatusText.text = "Online"
                holder.hostUse.isChecked = true
            }
            State.CONNECTING -> {
                holder.hostStatusColor.setBackgroundColor(
                    LANDDApplication.context.resources.getColor(
                        R.color.yellow
                    )
                )
                holder.hostRefresh.visibility = View.VISIBLE
                holder.hostWarning.visibility = View.GONE
                holder.hostRefresh.startAnimation(holder.refreshAnimation)
                holder.hostStatusText.text = "Connecting"
                holder.hostUse.isChecked = true
            }
            State.UNAUTHORIZED -> {
                holder.hostStatusColor.setBackgroundColor(
                    LANDDApplication.context.resources.getColor(
                        R.color.orange
                    )
                )
                holder.hostRefresh.visibility = View.GONE
                holder.hostWarning.visibility = View.VISIBLE
                holder.hostStatusText.text = "Unauthorized"
                holder.hostUse.isChecked = true
            }
            State.DISCONNECTED -> {
                holder.hostStatusColor.setBackgroundColor(
                    LANDDApplication.context.resources.getColor(
                        R.color.red
                    )
                )
                holder.hostRefresh.visibility = View.GONE
                holder.hostWarning.visibility = View.VISIBLE
                holder.hostSocket.text = "${host.ip}:${host.port}"
                holder.hostStatusText.text = "DisConnected"
                holder.hostUse.isChecked = true
            }
            State.UNUSED -> {
                holder.hostStatusColor.setBackgroundColor(
                    LANDDApplication.context.resources.getColor(
                        R.color.deep_gray
                    )
                )
                holder.hostRefresh.visibility = View.GONE
                holder.hostWarning.visibility = View.GONE
                holder.hostSocket.text = "${host.ip}:${host.port}"
                holder.hostStatusText.text = "Unused"
                holder.hostUse.isChecked = false
            }
        }
        if (host.state == State.CONNECTING) {
            GlobalScope.launch(Dispatchers.IO) {
                if (DownloadUtil.testProxyServer(host.ip, host.port)) {
                    host.state = State.CONNECTED
                } else {
                    host.state = State.DISCONNECTED
                }
                fragment.requireActivity().runOnUiThread {
                    notifyDataSetChanged()
                }
            }
        }
        holder.hostUse.setOnCheckedChangeListener { _, isChecked ->
            host.state = if (isChecked) State.CONNECTING else State.UNUSED
            syncToDB(host)
            notifyDataSetChanged()
        }
        holder.hostWarning.setOnClickListener { _ ->
            if (host.state == State.UNAUTHORIZED) {
                fragment.context?.let { _ ->
                    InputDialog.show(
                        fragment.activity as AppCompatActivity,
                        "验证",
                        "请输入Token",
                        "确定",
                        "取消"
                    )
                        .setOnOkButtonClickListener(OnInputDialogButtonClickListener { _, _, _ ->
                            host.state = State.CONNECTING
                            notifyDataSetChanged()
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
                host.state = State.DISCONNECTED
            } else {
                host.state = State.CONNECTING
                notifyDataSetChanged()
            }
            syncToDB(host)
        }
    }

    override fun getItemCount() = hostList.value?.size ?: 0
}