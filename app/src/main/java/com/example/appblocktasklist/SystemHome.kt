package com.example.appblocktasklist

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.appblocktasklist.roomdb.TasksDB.Task
import com.example.appblocktasklist.roomdb.locksettingDB.LockSetting
import com.example.appblocktasklist.worker.UsedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class SystemHome : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_system_home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        println("Work OK1")
        val workRequest = OneTimeWorkRequestBuilder<UsedApp>()
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(requireContext())
            .beginUniqueWork("uniqueWork", ExistingWorkPolicy.KEEP, workRequest)//Workerが複数起動することを防ぐ
            .enqueue()

        println("Work OK2")



        //NavHostの取得
        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        //navControllerの取得
        val navController = navHostFragment.navController

        GlobalScope.launch {
            val listView = view.findViewById<ListView>(R.id.SystemTaskList)
            val tasks: List<Task> = MyApplication.database.tasksDao().getAll()
            val taskNames: List<String> = tasks.map { it.taskName }

            val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, taskNames)
            listView.adapter = adapter
        }

        //GlobalScope.launch {
        //    val listView_lock = view.findViewById<ListView>(R.id.SystemLockList)
        //    val locks: List<LockSetting> = MyApplication.database.lockSettingDao().getAll()
//
        //    withContext(Dispatchers.Main) {
//
        //        val lockStrings: List<String> = locks.map { "${it.usableTime}, ${it.dayOfWeek}" }
//
        //        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, lockStrings)
        //        listView_lock.adapter = adapter
        //    }
        //}


        //GlobalScope.launch {
        //    val listView_lock = view.findViewById<ListView>(R.id.SystemLockList)
        //    val locks: List<LockSetting> = MyApplication.database.lockSettingDao().getAll()
//
        //    withContext(Dispatchers.Main) {
        //        val lockStrings: List<String> = locks.mapNotNull { lock ->
        //            val days = lock.dayOfWeek.entries.filter { it.value }.joinToString(", ") { dayOfWeekToJapanese(it.key) }
        //            val humanReadableTime = durationToString(lock.usableTime)
        //            val targetApps = lock.targetApp.joinToString(", ") // Join the target apps into a single string
        //            if (humanReadableTime != null) {
        //                "$humanReadableTime\n 制限する曜日:$days\n 制限アプリ: $targetApps" // Include target apps in the string
        //            } else {
        //                null
        //            }
        //        }
//
        //        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, lockStrings)
        //        listView_lock.adapter = adapter
        //    }
        //}

        GlobalScope.launch {
            val listView_lock = view.findViewById<ListView>(R.id.SystemLockList)
            val locks: List<LockSetting> = MyApplication.database.lockSettingDao().getAll()

            withContext(Dispatchers.Main) {
                val lockStrings: List<String> = locks.mapNotNull { lock ->
                    val days = lock.dayOfWeek.entries.filter { it.value }.joinToString(", ") { dayOfWeekToJapanese(it.key) }
                    val humanReadableTime = if (lock.usableTime != null) {
                        durationToString(lock.usableTime)
                    } else if (lock.beginTime != null && lock.endTime != null) {
                        "${localTimeToString(lock.beginTime)}～${localTimeToString(lock.endTime)}の間制限"
                    } else {
                        null
                    }
                    val pm: PackageManager = requireContext().packageManager
                    val labelNames = mutableListOf<String>()
                    for (packageName in lock.targetApp) {
                        try {
                            val packageInfo = pm.getPackageInfo(packageName, 0)
                            labelNames.add(pm.getApplicationLabel(packageInfo.applicationInfo).toString())
                        } catch (e: PackageManager.NameNotFoundException) {
                            continue
                        }
                    }

                    val targetApps = labelNames.joinToString(", ") // Join the target apps into a single string
                    if (humanReadableTime != null) {
                        "$humanReadableTime\n 制限する曜日:$days\n 制限アプリ: $targetApps" // Include target apps in the string
                    } else {
                        null
                    }
                }

                val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, lockStrings)
                listView_lock.adapter = adapter
            }
        }












        if (Settings.canDrawOverlays(requireContext())) {
            println("Work OK1")

            val workRequest = OneTimeWorkRequestBuilder<UsedApp>()
                .setInitialDelay(0, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(requireContext())
                .beginUniqueWork("uniqueWork", ExistingWorkPolicy.KEEP, workRequest)//Workerが複数起動することを防ぐ
                .enqueue()

            println("Work OK2")

        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + requireContext().packageName)
            )

            requireActivity().startActivity(intent)
            val intent2 = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            requireActivity().startActivity(intent2)
        }


        //ロック設定が押されたら
        view.findViewById<Button>(R.id.button2).setOnClickListener{
//            //ファイル名 +  Directionsが自動生成される
//            val action = SystemHomeDirections.actionSystemHomeFragmentToSystemLockmenuFragment()
//            navController.navigate(action)

            //val remaining = calcRemaining("com.google.android.youtube")
            //if (remaining != null) {
            //    setLockWorker(requireContext(), remaining)
            //}


            //通知送るとき
            //val timeRemaining = TimeRemaining()
            //val remaining = 60
            //timeRemaining.sendRemainingTimeNotification(requireContext(),remaining)
          
          
            println("Work OK1")
            val workRequest = OneTimeWorkRequestBuilder<UsedApp>()
                .setInitialDelay(0, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(requireContext())
                .beginUniqueWork("uniqueWork", ExistingWorkPolicy.KEEP, workRequest)//Workerが複数起動することを防ぐ
                .enqueue()

            println("Work OK2")



            //ファイル名 +  Directionsが自動生成される
            val action = SystemHomeDirections.actionSystemHomeFragmentToSystemLockmenuFragment()
            navController.navigate(action)
        }

        //タスク設定が押されたら
        view.findViewById<Button>(R.id.SystemHomeTaskBottun).setOnClickListener{

            println("Work OK1")
            val workRequest = OneTimeWorkRequestBuilder<UsedApp>()
                .setInitialDelay(0, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(requireContext())
                .beginUniqueWork("uniqueWork", ExistingWorkPolicy.KEEP, workRequest)//Workerが複数起動することを防ぐ
                .enqueue()

            println("Work OK2")



            val action = SystemHomeDirections.actionSystemHomeFragmentToSystemTaskmenuFragment()
            navController.navigate(action)
        }
    }


    fun dayOfWeekToJapanese(day: DayOfWeek): String {
        return when (day) {
            DayOfWeek.MONDAY -> "月"
            DayOfWeek.TUESDAY -> "火"
            DayOfWeek.WEDNESDAY -> "水"
            DayOfWeek.THURSDAY -> "木"
            DayOfWeek.FRIDAY -> "金"
            DayOfWeek.SATURDAY -> "土"
            DayOfWeek.SUNDAY -> "日"
        }
    }

    fun durationToString(duration: Duration?): String? {
        if (duration == null) {
            return null
        }

        val hours = duration.seconds / 3600
        val minutes = duration.seconds % 3600 / 60

        return "$hours"+"時間$minutes"+"分経過後にロック"
    }

    fun localTimeToString(localTime: LocalTime?): String? {
        if (localTime == null) {
            return null
        }
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return localTime.format(formatter).replace(":", "時").plus("分")
    }






}