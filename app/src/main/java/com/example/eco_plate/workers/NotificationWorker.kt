package com.example.eco_plate.workers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.eco_plate.data.repository.OrderRepository
import com.example.eco_plate.notifications.NotificationNotifier
import com.example.eco_plate.utils.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val orderRepository: OrderRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!canPostNotifications()) {
            return Result.success()
        }

        val res = orderRepository.getMyOrders().first()

        if (res is Resource.Success && res.data != null) {
            val today = getTodayIsoDate()

            val arrivingToday = res.data.filter { order ->
                order.pickupTime?.startsWith(today) == true && order.status != com.example.eco_plate.data.models.OrderStatus.CANCELLED
            }

            arrivingToday.forEach { order ->
                @SuppressLint("MissingPermission")
                NotificationNotifier.showArrivingToday(applicationContext, order.orderNumber)
            }

            return Result.success()
        }

        return Result.retry()
    }

    private fun canPostNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun getTodayIsoDate(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA)
        return sdf.format(cal.time)
    }
}