package me.pitok.videoplayer.views

import android.app.Dialog
import android.content.Context
import android.os.Environment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.pitok.sdkextentions.toPx
import me.pitok.videoplayer.R
import me.pitok.videoplayer.entity.ListDialogItemEntity
import java.io.File


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
        setFolderFileListToAdapter(Environment.getExternalStorageDirectory(), false)
        setContentView(view)
        val window = window
        window?.setLayout(500f.toPx(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
    }

    private fun setFolderFileListToAdapter(folder: File, showParentFolder: Boolean = true){
        val unsortedFiles = requireNotNull(folder.listFiles())
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
        if (showParentFolder) {
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
            if (file.isDirectory && requireNotNull(file.listFiles()).isNotEmpty()){
                items.add(
                    ListDialogItemEntity(
                        itemUnique = file.absolutePath,
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

    private fun onItemClickListener(path: String?){
        if (path == null) return
        val file = File(path)
        if (file.isDirectory){
            setFolderFileListToAdapter(
                file,
                file.absolutePath != Environment.getExternalStorageDirectory().absolutePath
            )
            return
        }
        if (!file.path.endsWith(".srt")) return
        resultCallback.invoke(file.absolutePath)
        dismiss()
    }

}