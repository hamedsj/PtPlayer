package me.pitok.videoplayer.views

import android.app.Dialog
import android.content.Context
import android.os.Environment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.pitok.logger.Logger
import me.pitok.sdkextentions.toPx
import me.pitok.videoplayer.R
import me.pitok.videoplayer.entity.ListDialogItemEntity
import java.io.File
import java.lang.Exception
import java.lang.NullPointerException


class SubtitleListDialogView(
    context: Context,
    private val resultCallback: (String) -> Unit
) : Dialog(context) {

    private val dialogRecycler: RecyclerView

    private var items: MutableList<ListDialogItemEntity> = mutableListOf()

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_subtitle_list_dialog, null, false)
        dialogRecycler = view.findViewById(R.id.listDialogRecycler)
        dialogRecycler.layoutManager = LinearLayoutManager(context)
        dialogRecycler.adapter = SubtitleListDialogAdapter(items) {}
        setFolderFileListToAdapter(
            try{
            requireNotNull(
                requireNotNull(Environment.getExternalStorageDirectory().parentFile)
                    .parentFile
            )
            }catch (ignored: Exception){Environment.getExternalStorageDirectory()}
        )
        setContentView(view)
        window?.setLayout(500f.toPx(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
    }

    private fun setFolderFileListToAdapter(paramFolder: File){
        val folder = if (isExternalStorageParent(paramFolder)) paramFolder.parentFile
        else paramFolder
        val unsortedFiles = folder.listFiles()?:run{arrayOf<File>()}
        val sortedFiles = unsortedFiles.sortedWith(object: Comparator<File> {
            override fun compare(file1: File?, file2: File?): Int {
                if (file1 == null || file2 == null) return 0
                if (file1.isDirectory && !file2.isDirectory) return -1
                if (file2.isDirectory && !file1.isDirectory) return 1
                return when{
                    requireNotNull(file1.name) > requireNotNull(file2.name) -> 1
                    requireNotNull(file1.name) == requireNotNull(file2.name) -> 0
                    else -> -1
                }
            }

        })
        items.clear()
        if (canShowParentFolder(folder)) {
            items.add(
                ListDialogItemEntity(
                    itemUnique = folder.parentFile?.absolutePath,
                    itemIconResource = R.drawable.ic_folder,
                    itemTitleResource = "..",
                    itemOnClickListener = ::onItemClickListener
                )
            )
        }
        sortedFiles.forEach { file ->
            if (isReadableAndNotEmptyFolder(file)){
                items.add(
                    ListDialogItemEntity(
                        itemUnique =
                        if (!isExternalStorageParent(file))
                            file.absolutePath
                        else
                            Environment.getExternalStorageDirectory().absolutePath
                        ,
                        itemIconResource = R.drawable.ic_folder,
                        itemTitleResource = file.name,
                        itemOnClickListener = ::onItemClickListener
                    )
                )
            }else if (file.path.endsWith(".srt")){
                items.add(
                    ListDialogItemEntity(
                        itemUnique = file.absolutePath,
                        itemIconResource = R.drawable.ic_srt,
                        itemTitleResource = file.name,
                        itemOnClickListener = ::onItemClickListener
                    )
                )
            }
        }
        dialogRecycler.adapter?.notifyDataSetChanged()
    }

    private fun canShowParentFolder(folder: File): Boolean {
        if (folder.absolutePath ==
            try{
                requireNotNull(
                    requireNotNull(
                        Environment.getExternalStorageDirectory().parentFile
                    ).parentFile
                )
            }catch (ignored:Exception){
                Environment.getExternalStorageDirectory()
            }.absolutePath
        ) return false
        return true
    }

    private fun isExternalStorageParent(file: File): Boolean {
        return file.absolutePath == Environment.getExternalStorageDirectory().parentFile.absolutePath
    }

    private fun isReadableAndNotEmptyFolder(file: File): Boolean {
        if (isExternalStorageParent(file))
            return true
        if (file.isDirectory && file.listFiles() != null && file.listFiles().isNotEmpty())
            return true
        return false
    }

    private fun onItemClickListener(path: String?){
        Logger.d("onItemClickListener called : $path")
        if (path == null) return
        val file = File(path)
        if (file.isDirectory){
            setFolderFileListToAdapter(file)
            return
        }
        if (!file.path.endsWith(".srt")) return
        resultCallback.invoke(file.absolutePath)
        dismiss()
    }

}