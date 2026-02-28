package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import org.jetbrains.compose.resources.StringResource

data class Feature(
    val titleRes: StringResource,
    val icon: String,
    val descriptionRes: StringResource
)

enum class AppointmentStatus { CONFIRMED, PENDING }

data class AppointmentData(
    val doctor: String,
    val specialty: String,
    val hospital: String,
    val dateTime: String,
    val status: AppointmentStatus
)