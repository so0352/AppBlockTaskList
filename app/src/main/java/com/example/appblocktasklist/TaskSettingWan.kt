package com.example.appblocktasklist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.core.view.get
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.example.appblocktasklist.roomdb.TasksDB.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class TaskSettingWan : Fragment() {
    private val args: TaskSettingWanArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_setting_wan_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val titleEditText = view.findViewById<EditText>(R.id.title)
        val titleReasonEditText = view.findViewById<EditText>(R.id.title_reason)
        val reasonOfReasonEditText = view.findViewById<EditText>(R.id.reason_of_reason)
        val memoEditText = view.findViewById<EditText>(R.id.memo)
        val editpriority = view.findViewById<RadioGroup>(R.id.priorityRadioGroup)

        if (args.taskID == -1) {
            view.findViewById<Button>(R.id.button7).setOnClickListener{
                val title = titleEditText.text.toString()
                val titleReason = titleReasonEditText.text.toString()
                val reasonOfReason = reasonOfReasonEditText.text.toString()
                val memo = memoEditText.text.toString()
                val priorityRadioGroup = view.findViewById<RadioGroup>(R.id.priorityRadioGroup)

                val priority = when (priorityRadioGroup.checkedRadioButtonId) {
                    R.id.radioLowPriority -> 1 // 低い優先度を選択した場合
                    R.id.radioMediumPriority -> 5 // 中程度の優先度を選択した場合
                    R.id.radioHighPriority -> 10 // 高い優先度を選択した場合
                    else -> 1 // デフォルトは低い優先度
                }

                if (title != ""){
                    GlobalScope.launch {
                        MyApplication.database.tasksDao().insertAll(
                            Task(
                                taskName = title,
                                memo = memo,
                                reason = "$titleReason, $reasonOfReason",
                                priority = priority
                            )
                        )
                    }
                    val action = TaskSettingWanDirections.actionTaskSettingWanFragmentToSystemHomeFragment()
                    navController.navigate(action)
                }
            }
        } else {
            GlobalScope.launch {
                var task = MyApplication.database.tasksDao().getTask(args.taskID)

                val reasons = task.reason.split(",")


                launch(Dispatchers.Main) {
                    // ここにメインスレッドで実行したいコードを書く
                    titleEditText.setText(task.taskName)
                    titleReasonEditText.setText(reasons[0])
                    reasonOfReasonEditText.setText(reasons[1])
                    memoEditText.setText(task.memo)
                    when (task.priority) {
                    1 -> editpriority.check(R.id.radioLowPriority)// 低い優先度を選択した場合
                    5 -> editpriority.check(R.id.radioMediumPriority) // 中程度の優先度を選択した場合
                    10 ->editpriority.check(R.id.radioHighPriority)
                }

                view.findViewById<Button>(R.id.button7).setOnClickListener{
                    val title = titleEditText.text.toString()
                    val titleReason = titleReasonEditText.text.toString()
                    val reasonOfReason = reasonOfReasonEditText.text.toString()
                    val memo = memoEditText.text.toString()
                    val priority = when (editpriority.checkedRadioButtonId) {
                        R.id.radioLowPriority -> 1 // 低い優先度を選択した場合
                        R.id.radioMediumPriority -> 5 // 中程度の優先度を選択した場合
                        R.id.radioHighPriority -> 10 // 高い優先度を選択した場合
                        else -> 1 // デフォルトは低い優先度
                    }

                    if (title != ""){
                        GlobalScope.launch {
                            task.taskName = title
                            task.reason = "${titleReason}, ${reasonOfReason}"
                            task.memo = memo
                            task.priority = priority
                            MyApplication.database.tasksDao().update(
                                task
                            )
                        }
                        val action = TaskSettingWanDirections.actionTaskSettingWanFragmentToSystemHomeFragment()
                        navController.navigate(action)
                    }
                }
            }
        }
    }
}}