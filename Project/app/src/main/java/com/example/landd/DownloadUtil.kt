package com.example.landd

import android.content.Context
import android.webkit.URLUtil
import com.example.landd.database.AppDataBase
import com.example.landd.logic.model.SubTask
import com.example.landd.logic.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.ProxyAuthenticator
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Proxy

data class Authentication(val username: String, val password: String)

object DownloadUtil {
    private val clientMap = HashMap<String, OkHttpClient>()
    private var ps: HttpProxyServer? = null
    private var authentication: Authentication? = null

    // 最小的需要划分成多个子任务的文件大小，如果文件大小小于minFileSizeToSplit，那么不划分多个子任务
    var minFileSizeToSplit = 1024 * 1024

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
        val task = Task(url, fileName, fileSize, "", "", false)
        val subTaskList = mutableListOf<SubTask>()
        if (fileSize != -1 && fileSize < minFileSizeToSplit) {
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

        val db = AppDataBase.getDatabase()
        db.runInTransaction {
            GlobalScope.launch(Dispatchers.Main) {
                db.taskDao().insert(task)
                for (subTask in subTaskList) {
                    db.subTaskDao().insert(subTask)
                }
            }
        }
    }

    public fun download(inetSocketAddress: InetSocketAddress, task: Task, subTask: SubTask) {
        val client = getClient(inetSocketAddress)
        val fos = LANDDApplication.context.openFileOutput("${subTask.id}", Context.MODE_PRIVATE)
        client.newCall(range(task.url, subTask.start, subTask.end)).execute().body()?.byteStream()
            ?.use {
                val b = ByteArray(1024)
                while (true) {
                    when (val len = it.read(b)) {
                        -1, 0 -> break
                        else -> fos.write(b, 0, len)
                    }
                }
            }
        fos.close()
    }

    public fun download(hostname: String, port: Int, task: Task, subTask: SubTask) {
        download(InetSocketAddress(hostname, port), task, subTask)
    }

    public fun download(task: Task, subTask: SubTask) {
        ps?.let { download(it.listenAddress, task, subTask) } ?: throw NoProxyServerException()
    }

    open class UtilException : Exception()
    open class ProxyException : UtilException()
    class NoProxyServerException : ProxyException()
}