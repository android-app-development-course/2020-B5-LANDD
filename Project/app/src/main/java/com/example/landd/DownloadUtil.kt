package com.example.landd

import android.content.Context
import android.icu.text.DateFormat
import android.os.Environment
import android.util.Log
import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.SortedList
import com.example.landd.database.AppDataBase
import com.example.landd.logic.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.ProxyAuthenticator
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.atomic.AtomicInteger

data class Authentication(val username: String, val password: String)

object DownloadUtil {
    private val clientMap = HashMap<String, OkHttpClient>()
    private var ps: HttpProxyServer? = null
    private var authentication: Authentication? = null
    private const val TAG = "DownloadUtil"

    // 最小的需要划分成多个子任务的文件大小，如果文件大小小于minFileSizeToSplit，那么不划分多个子任务
    var minFileSizeToSplit = 10 * 1024 * 1024

    public fun startProxyServer(port: Int, authentication: Authentication? = null) {
        stopProxyServer()
        this.authentication = authentication
        ps = DefaultHttpProxyServer.bootstrap().apply {
            withAllowLocalOnly(false)
            withPort(port)
            if (authentication != null) {
                withProxyAuthenticator(object : ProxyAuthenticator {
                    override fun authenticate(userName: String?, password: String?): Boolean {
                        TODO("Not yet implemented")
                    }

                    override fun getRealm(): String {
                        TODO("Not yet implemented")
                    }
                })
            }
        }.start()
    }

    public fun stopProxyServer() {
        ps?.stop()
        ps = null
    }

    public fun proxyServerIsRunning(): Boolean {
        return ps != null
    }

    private fun getClient(inetSocketAddress: InetSocketAddress): OkHttpClient {
        val hostname = inetSocketAddress.hostName
        val port = inetSocketAddress.port
        val clientKey = "$hostname:$port"
        return clientMap.getOrPut(clientKey) {
            OkHttpClient.Builder().apply {
                proxy(Proxy(Proxy.Type.HTTP, inetSocketAddress))
            }.build()
        }
    }

    private fun range(url: String, start: Int, end: Int): Request {
        return Request.Builder().apply {
            url(url)
            if (end >= 0) {
                header("Range", "bytes=$start-$end")
            } else {
                header("Range", "bytes=$start-")
            }
        }.build()
    }

    @Suppress("NAME_SHADOWING")
    public fun testProxyServer(inetSocketAddress: InetSocketAddress): Boolean {
        return try {
            val inetSocketAddress =
                inetSocketAddress ?: ps?.listenAddress ?: throw NoProxyServerException()
            val client = getClient(inetSocketAddress)
            client.newCall(range("https://www.baidu.com", 0, 0)).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    public fun testProxyServer(hostname: String, port: Int): Boolean {
        return testProxyServer(InetSocketAddress(hostname, port))
    }

    @Suppress("NAME_SHADOWING")
    public fun newTask(url: String, inetSocketAddress: InetSocketAddress? = null) {
        val inetSocketAddress =
            inetSocketAddress ?: ps?.listenAddress ?: throw NoProxyServerException()
        val client = getClient(inetSocketAddress)

        val testResp = client.newCall(
            Request.Builder().apply {
                url(url)
                head() // 只获取头部
            }.build()
        ).execute()

        // 获取文件大小
        var fileSize = testResp.header("Content-Length")?.toInt() ?: -1

        val disposition = testResp.header("Content-Disposition")

        // 获取文件类型
        val fileType = testResp.header("Content-Type")

        // 测试是否能断点续传
        val canPartial = client.newCall(range(url, 0, 0)).execute().isSuccessful

        // 如果文件大小未知，那么尝试手动获取
        if (fileSize == -1 && canPartial) {
            // 二分法求文件大小
            var currentSize = 0
            var step = minFileSizeToSplit
            while (step != 0) {
                val newSize = currentSize + step
                val test = client.newCall(range(url, newSize, newSize)).execute().isSuccessful
                if (test) {
                    currentSize = newSize
                    step *= 2
                } else {
                    step /= 2
                }
            }
            fileSize = currentSize
        }

        // 生成任务
        val fileName = URLUtil.guessFileName(url, disposition, fileType)
        val db = AppDataBase.getDatabase()
        val task = Task(url, fileName, fileSize, "", "", false)
        task.id = db.taskDao().insert(task)
        val subTaskList = mutableListOf<SubTask>()
        if (fileSize != -1 && fileSize > minFileSizeToSplit) {
            val partSize = fileSize / minFileSizeToSplit
            for (i in 0..partSize) {
                val start = minFileSizeToSplit * i
                val end = start + minFileSizeToSplit - 1
                val subTask = SubTask(task.id, start, end, false)
                subTaskList.add(subTask)
            }
            subTaskList.last().end = -1
        } else {
            subTaskList.add(SubTask(task.id, 0, -1, false))
        }
        for (subTask in subTaskList) {
            db.subTaskDao().insert(subTask)
        }
    }

    public fun download(
        inetSocketAddress: InetSocketAddress,
        task: Task,
        subTask: SubTask,
        speedRecorder: SpeedRecorder
    ) {
        val client = getClient(inetSocketAddress)
        val fos = LANDDApplication.context.openFileOutput("${subTask.id}", Context.MODE_PRIVATE)
        client.newCall(range(task.url, subTask.start, subTask.end)).execute().body()?.byteStream()
            ?.use {
                val b = ByteArray(1024)
                while (true) {
                    when (val len = it.read(b)) {
                        -1, 0 -> break
                        else -> {
                            speedRecorder.addAndGet(len)
                            fos.write(b, 0, len)
                        }
                    }
                }
            }
        fos.close()
    }

    public fun download(
        hostname: String,
        port: Int,
        task: Task,
        subTask: SubTask,
        speedRecorder: SpeedRecorder
    ) {
        download(InetSocketAddress(hostname, port), task, subTask, speedRecorder)
    }

    public fun download(
        taskListLiveData: MutableLiveData<MutableList<Task>>,
        position: Int,
        speedRecorder: SpeedRecorder
    ) {
        val taskList = taskListLiveData.value!!
        val task = taskList[position]
        val db = AppDataBase.getDatabase()

        // 任务已完成则返回
        if (task.has_finish) {
            return
        }

        // 找出未完成的子任务
        val unfinishedSubTaskList = db.subTaskDao().findUnFinishedAll(task.id)
        if (unfinishedSubTaskList.isNotEmpty()) {
            // 如果有未完成的子任务，那么开始下载
            // 找到可用的服务器
            val validHostList = db.hostDao().getAllValid()
            if (validHostList.isEmpty()) {
                throw Exception("无可用的服务器")
            }

            // 分配任务给各个服务器
            val dispatcher = MutableList(validHostList.size) { mutableListOf<Int>() }
            for (i in unfinishedSubTaskList.indices) {
                dispatcher[i % validHostList.size].add(i)
            }

            for (i in dispatcher.indices) {
                if (dispatcher[i].size > 0) {
                    // 该服务器有下载任务
                    val host = validHostList[i]
                    GlobalScope.launch(Dispatchers.IO) {
                        for (j in dispatcher[i]) {
                            val subTask = unfinishedSubTaskList[j]
                            try {
                                download(host.ip, host.port, task, subTask, speedRecorder)
                                subTask.hasFinish = true
                                db.subTaskDao().update(subTask)
                            } catch (e: Exception) {
                                Log.d(TAG, "download: ${host.ip}:${host.port}不可用")
                            }
                        }

                        // 完成任务，更新
                        taskListLiveData.postValue(taskList)
//                        try {
//                            mergeTempFiles(taskListLiveData, position)
//                        } catch (e: Exception) {
//                        }
                    }
                }
            }
        }
//        else {
//            mergeTempFiles(taskListLiveData, position)
//        }
    }

    private fun fileOutputStream(name: String): FileOutputStream {
        try {
            if (File(name).exists()) {
                throw Exception()
            }
            return FileOutputStream(name)
        } catch (e: Exception) {
            var i = 1
            while (true) {
                try {
                    if (File("${name}_$i").exists()) {
                        throw Exception()
                    }
                    return FileOutputStream("${name}_$i")
                } catch (e: Exception) {
                    ++i
                }
            }
        }
    }

    public fun mergeTempFiles(
        taskListLiveData: MutableLiveData<MutableList<Task>>,
        position: Int
    ): Boolean {
        val taskList = taskListLiveData.value!!
        val task = taskList[position]
        val db = AppDataBase.getDatabase()
        val unfinishedSubTaskList = db.subTaskDao().findUnFinishedAll(task.id)
        if (unfinishedSubTaskList.isNotEmpty()) {
            return false
        }
        val finishedSubTaskList = db.subTaskDao().findFinishedAll(task.id)
//        val fos = LANDDApplication.context.openFileOutput(task.file_name, Context.MODE_PRIVATE)
        val baseName = "/storage/emulated/0/Download/${task.file_name}"
        val fos: FileOutputStream = fileOutputStream(baseName)
        for (subTask in finishedSubTaskList) {
            try {
                LANDDApplication.context.openFileInput(subTask.id.toString()).use {
                    fos.write(it.readBytes())
                }
            } catch (e: FileNotFoundException) {
                Log.d(TAG, "mergeTempFiles: NoSuchSubTaskFile")
            }
        }
        fos.close()
        task.has_finish = true
        db.taskDao().update(task)
        return true
    }

    open class UtilException : Exception()
    open class ProxyException : UtilException()
    class NoProxyServerException : ProxyException()
}