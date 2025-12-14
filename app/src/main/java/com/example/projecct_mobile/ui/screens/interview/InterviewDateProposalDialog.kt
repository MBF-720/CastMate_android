package com.example.projecct_mobile.ui.screens.interview

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.projecct_mobile.data.model.InterviewDateOption
import com.example.projecct_mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialogue pour proposer EXACTEMENT 3 dates d'interview avec heures
 */
@Composable
fun InterviewDateProposalDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<InterviewDateOption>) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // États pour les 3 dates
    var date1 by remember { mutableStateOf<String?>(null) }
    var time1 by remember { mutableStateOf<String?>(null) }
    var date2 by remember { mutableStateOf<String?>(null) }
    var time2 by remember { mutableStateOf<String?>(null) }
    var date3 by remember { mutableStateOf<String?>(null) }
    var time3 by remember { mutableStateOf<String?>(null) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Format d'affichage pour les dates
    val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    /**
     * Validation des dates
     */
    fun validateDates(): Boolean {
        // 1. Vérifier que les 3 dates sont remplies
        if (date1 == null || date2 == null || date3 == null) {
            errorMessage = "Veuillez sélectionner 3 dates"
            return false
        }
        
        // 2. Vérifier que les 3 heures sont remplies (OBLIGATOIRE)
        if (time1 == null || time2 == null || time3 == null) {
            errorMessage = "Veuillez sélectionner une heure pour chaque date"
            return false
        }
        
        // 3. Vérifier l'unicité des dates
        val dates = listOf(date1!!, date2!!, date3!!)
        if (dates.distinct().size != 3) {
            errorMessage = "Les 3 dates doivent être différentes"
            return false
        }
        
        // 4. Vérifier que les dates+heures sont dans le futur
        val now = Calendar.getInstance()
        val dateTimePairs = listOf(
            Pair(date1!!, time1!!),
            Pair(date2!!, time2!!),
            Pair(date3!!, time3!!)
        )
        
        for ((dateStr, timeStr) in dateTimePairs) {
            try {
                val date = dateFormat.parse(dateStr)
                val timeParts = timeStr.split(":")
                if (date != null && timeParts.size == 2) {
                    val dateTime = Calendar.getInstance()
                    dateTime.time = date
                    dateTime.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    dateTime.set(Calendar.MINUTE, timeParts[1].toInt())
                    dateTime.set(Calendar.SECOND, 0)
                    dateTime.set(Calendar.MILLISECOND, 0)
                    
                    if (dateTime.timeInMillis <= now.timeInMillis) {
                        errorMessage = "Toutes les dates et heures doivent être dans le futur"
                        return false
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Erreur de validation des dates"
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
                // Titre
                Text(
                    text = "Proposer 3 dates d'interview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                
                Text(
                    text = "Vous devez proposer exactement 3 dates différentes avec des heures",
                    fontSize = 14.sp,
                    color = GrayBorder
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date 1
                DateTimeSelector(
                    label = "Date 1",
                    date = date1,
                    time = time1,
                    onDateClick = {
                        val calendar = Calendar.getInstance()
                        if (date1 != null) {
                            try {
                                val parsedDate = dateFormat.parse(date1!!)
                                if (parsedDate != null) {
                                    calendar.time = parsedDate
                                }
                            } catch (e: Exception) {
                                // Ignorer
                            }
                        }
                        
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(year, month, dayOfMonth)
                                date1 = dateFormat.format(selectedCalendar.time)
                                errorMessage = null
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = System.currentTimeMillis() - 1000
                        }.show()
                    },
                    onTimeClick = {
                        val calendar = Calendar.getInstance()
                        if (time1 != null) {
                            try {
                                val timeParts = time1!!.split(":")
                                if (timeParts.size == 2) {
                                    calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                                    calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                                }
                            } catch (e: Exception) {
                                // Ignorer
                            }
                        }
                        
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                selectedCalendar.set(Calendar.MINUTE, minute)
                                time1 = timeFormat.format(selectedCalendar.time)
                                errorMessage = null
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    displayDateFormat = displayDateFormat
                )
                
                // Date 2
                DateTimeSelector(
                    label = "Date 2",
                    date = date2,
                    time = time2,
                    onDateClick = {
                        val calendar = Calendar.getInstance()
                        if (date2 != null) {
                            try {
                                val parsedDate = dateFormat.parse(date2!!)
                                if (parsedDate != null) {
                                    calendar.time = parsedDate
                                }
                            } catch (e: Exception) {
                                // Ignorer
                            }
                        }
                        
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(year, month, dayOfMonth)
                                date2 = dateFormat.format(selectedCalendar.time)
                                errorMessage = null
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = System.currentTimeMillis() - 1000
                        }.show()
                    },
                    onTimeClick = {
                        val calendar = Calendar.getInstance()
                        if (time2 != null) {
                            try {
                                val timeParts = time2!!.split(":")
                                if (timeParts.size == 2) {
                                    calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                                    calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                                }
                            } catch (e: Exception) {
                                // Ignorer
                            }
                        }
                        
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                selectedCalendar.set(Calendar.MINUTE, minute)
                                time2 = timeFormat.format(selectedCalendar.time)
                                errorMessage = null
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    displayDateFormat = displayDateFormat
                )
                
                // Date 3
                DateTimeSelector(
                    label = "Date 3",
                    date = date3,
                    time = time3,
                    onDateClick = {
                        val calendar = Calendar.getInstance()
                        if (date3 != null) {
                            try {
                                val parsedDate = dateFormat.parse(date3!!)
                                if (parsedDate != null) {
                                    calendar.time = parsedDate
                                }
                            } catch (e: Exception) {
                                // Ignorer
                            }
                        }
                        
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(year, month, dayOfMonth)
                                date3 = dateFormat.format(selectedCalendar.time)
                                errorMessage = null
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = System.currentTimeMillis() - 1000
                        }.show()
                    },
                    onTimeClick = {
                        val calendar = Calendar.getInstance()
                        if (time3 != null) {
                            try {
                                val timeParts = time3!!.split(":")
                                if (timeParts.size == 2) {
                                    calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                                    calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                                }
                            } catch (e: Exception) {
                                // Ignorer
                            }
                        }
                        
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                selectedCalendar.set(Calendar.MINUTE, minute)
                                time3 = timeFormat.format(selectedCalendar.time)
                                errorMessage = null
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    displayDateFormat = displayDateFormat
                )
                
                // Message d'erreur
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFFF0000),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Boutons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler", color = DarkBlue)
                    }
                    
                    Button(
                        onClick = {
                            if (validateDates()) {
                                val dateOptions = listOf(
                                    InterviewDateOption(date = date1!!, time = time1!!),
                                    InterviewDateOption(date = date2!!, time = time2!!),
                                    InterviewDateOption(date = date3!!, time = time3!!)
                                )
                                onConfirm(dateOptions)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                    ) {
                        Text("Confirmer", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DateTimeSelector(
    label: String,
    date: String?,
    time: String?,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    displayDateFormat: SimpleDateFormat
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
            // Sélecteur de date
            OutlinedButton(
                onClick = onDateClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkBlue
                )
            ) {
                Text(
                    text = date?.let { 
                        try {
                            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
                            parsed?.let { displayDateFormat.format(it) } ?: "Sélectionner date"
                        } catch (e: Exception) {
                            "Sélectionner date"
                        }
                    } ?: "Sélectionner date",
                    fontSize = 14.sp
                )
            }
            
            // Sélecteur d'heure
            OutlinedButton(
                onClick = onTimeClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkBlue
                )
            ) {
                Text(
                    text = time ?: "Sélectionner heure",
                    fontSize = 14.sp
                )
            }
        }
    }
}

