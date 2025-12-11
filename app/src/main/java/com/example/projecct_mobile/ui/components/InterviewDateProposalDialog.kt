package com.example.projecct_mobile.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.projecct_mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialogue pour proposer 3 dates d'interview
 */
@Composable
fun InterviewDateProposalDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<Pair<String, String?>>) -> Unit, // List<Pair<date, time?>>
    isSubmitting: Boolean = false
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // État pour les 3 dates
    var date1 by remember { mutableStateOf<String?>(null) }
    var time1 by remember { mutableStateOf<String?>(null) }
    var date2 by remember { mutableStateOf<String?>(null) }
    var time2 by remember { mutableStateOf<String?>(null) }
    var date3 by remember { mutableStateOf<String?>(null) }
    var time3 by remember { mutableStateOf<String?>(null) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // Fonction pour ouvrir le date picker
    fun openDatePicker(
        onDateSelected: (String) -> Unit,
        minDate: Long? = null
    ) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                val formattedDate = dateFormat.format(selectedCalendar.time)
                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        )
        
        // Définir la date minimum (aujourd'hui)
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        datePickerDialog.datePicker.minDate = (minDate ?: today.timeInMillis) - 1000
        
        datePickerDialog.show()
    }
    
    // Fonction pour ouvrir le time picker
    fun openTimePicker(
        onTimeSelected: (String) -> Unit
    ) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val timePickerDialog = android.app.TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedCalendar.set(Calendar.MINUTE, selectedMinute)
                val formattedTime = timeFormat.format(selectedCalendar.time)
                onTimeSelected(formattedTime)
            },
            hour,
            minute,
            true // 24h format
        )
        
        timePickerDialog.show()
    }
    
    // Fonction de validation
    fun validateDates(): Boolean {
        errorMessage = null
        
        // Vérifier que les 3 dates sont remplies
        if (date1 == null || date2 == null || date3 == null) {
            errorMessage = "Veuillez sélectionner 3 dates"
            return false
        }
        
        // Vérifier que les 3 heures sont remplies (OBLIGATOIRE)
        if (time1 == null || time2 == null || time3 == null) {
            errorMessage = "Veuillez sélectionner une heure pour chaque date"
            return false
        }
        
        // Vérifier l'unicité des dates
        val dates = listOf(date1!!, date2!!, date3!!)
        if (dates.distinct().size != 3) {
            errorMessage = "Les 3 dates doivent être différentes"
            return false
        }
        
        // Vérifier que les dates+heures sont dans le futur
        val now = Calendar.getInstance()
        
        dates.forEachIndexed { index, dateStr ->
            val timeStr = when (index) {
                0 -> time1
                1 -> time2
                2 -> time3
                else -> null
            }
            
            try {
                val date = dateFormat.parse(dateStr)
                if (date != null && timeStr != null) {
                    val timeParts = timeStr.split(":")
                    val dateTime = Calendar.getInstance().apply {
                        time = date
                        set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                        set(Calendar.MINUTE, timeParts[1].toInt())
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    
                    if (dateTime.timeInMillis <= now.timeInMillis) {
                        errorMessage = "Toutes les dates et heures doivent être dans le futur"
                        return false
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Format de date ou heure invalide"
                return false
            }
        }
        
        return true
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Proposer 3 dates d'interview",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = GrayBorder
                        )
                    }
                }
                
                Text(
                    text = "Sélectionnez 3 dates différentes pour l'interview. L'acteur choisira parmi ces dates.",
                    fontSize = 14.sp,
                    color = GrayBorder,
                    lineHeight = 20.sp
                )
                
                // Date 1
                DateTimePickerRow(
                    label = "Date 1",
                    date = date1,
                    time = time1,
                    onDateClick = { openDatePicker(onDateSelected = { selectedDate -> date1 = selectedDate }) },
                    onTimeClick = { 
                        if (date1 != null) {
                            openTimePicker(onTimeSelected = { selectedTime -> time1 = selectedTime })
                        }
                    }
                )
                
                // Date 2
                DateTimePickerRow(
                    label = "Date 2",
                    date = date2,
                    time = time2,
                    onDateClick = { openDatePicker(onDateSelected = { selectedDate -> date2 = selectedDate }) },
                    onTimeClick = { 
                        if (date2 != null) {
                            openTimePicker(onTimeSelected = { selectedTime -> time2 = selectedTime })
                        }
                    }
                )
                
                // Date 3
                DateTimePickerRow(
                    label = "Date 3",
                    date = date3,
                    time = time3,
                    onDateClick = { openDatePicker(onDateSelected = { selectedDate -> date3 = selectedDate }) },
                    onTimeClick = { 
                        if (date3 != null) {
                            openTimePicker(onTimeSelected = { selectedTime -> time3 = selectedTime })
                        }
                    }
                )
                
                // Message d'erreur
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        color = Color(0xFFFF0000),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Boutons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    ) {
                        Text("Annuler", color = DarkBlue)
                    }
                    
                    Button(
                        onClick = {
                            if (validateDates()) {
                                onConfirm(
                                    listOf(
                                        Pair(date1!!, time1),
                                        Pair(date2!!, time2),
                                        Pair(date3!!, time3)
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Confirmer", color = White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateTimePickerRow(
    label: String,
    date: String?,
    time: String?,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = DarkBlue
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date picker
            OutlinedButton(
                onClick = onDateClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = date ?: "Sélectionner date",
                    fontSize = 14.sp
                )
            }
            
            // Time picker (OBLIGATOIRE)
            OutlinedButton(
                onClick = onTimeClick,
                modifier = Modifier.weight(1f),
                enabled = date != null,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkBlue
                )
            ) {
                Text(
                    text = time ?: "Heure *",
                    fontSize = 14.sp,
                    color = if (time == null) Color(0xFFFF0000) else DarkBlue
                )
            }
        }
    }
}

