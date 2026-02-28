package com.example.backend_side.controllers

import com.example.backend_side.entity.VaccineType
import com.example.backend_side.repositories.VaccineTypeRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vaccine-types")
@CrossOrigin(origins = ["*"])
class VaccineTypeController(private val vaccineTypeRepository: VaccineTypeRepository) {

    @GetMapping
    fun getAllVaccineTypes(): ResponseEntity<List<VaccineType>> {
        return ResponseEntity.ok(vaccineTypeRepository.findAll())
    }

    @GetMapping("/ordered")
    fun getAllVaccineTypesOrdered(): ResponseEntity<List<VaccineType>> {
        return ResponseEntity.ok(vaccineTypeRepository.findAllOrderedByAgeAndDose())
    }

    @GetMapping("/{vaccineId}")
    fun getVaccineTypeById(@PathVariable vaccineId: Int): ResponseEntity<VaccineType> {
        return vaccineTypeRepository.findById(vaccineId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/mandatory")
    fun getMandatoryVaccines(@RequestParam(defaultValue = "true") isMandatory: Boolean): ResponseEntity<List<VaccineType>> {
        return ResponseEntity.ok(vaccineTypeRepository.findByIsMandatory(isMandatory))
    }

    @GetMapping("/age/{ageInMonths}")
    fun getVaccinesByAge(@PathVariable ageInMonths: Int): ResponseEntity<List<VaccineType>> {
        return ResponseEntity.ok(vaccineTypeRepository.findByRecommendedAgeMonths(ageInMonths))
    }

    @GetMapping("/age-up-to/{ageInMonths}")
    fun getVaccinesUpToAge(@PathVariable ageInMonths: Int): ResponseEntity<List<VaccineType>> {
        return ResponseEntity.ok(vaccineTypeRepository.findByRecommendedAgeMonthsLessThanEqual(ageInMonths))
    }

    @GetMapping("/age-range")
    fun getVaccinesByAgeRange(
        @RequestParam minMonths: Int,
        @RequestParam maxMonths: Int
    ): ResponseEntity<List<VaccineType>> {
        return ResponseEntity.ok(vaccineTypeRepository.findByRecommendedAgeMonthsBetween(minMonths, maxMonths))
    }

    @GetMapping("/search")
    fun searchVaccineTypes(@RequestParam vaccineName: String): ResponseEntity<List<VaccineType>> {
        return ResponseEntity.ok(vaccineTypeRepository.findByVaccineNameContaining(vaccineName))
    }

    @PostMapping
    fun createVaccineType(@RequestBody vaccineType: VaccineType): ResponseEntity<VaccineType> {
        val savedVaccineType = vaccineTypeRepository.save(vaccineType)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVaccineType)
    }

    @PutMapping("/{vaccineId}")
    fun updateVaccineType(
        @PathVariable vaccineId: Int,
        @RequestBody vaccineType: VaccineType
    ): ResponseEntity<VaccineType> {
        return if (vaccineTypeRepository.existsById(vaccineId)) {
            vaccineType.vaccineId = vaccineId
            ResponseEntity.ok(vaccineTypeRepository.save(vaccineType))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{vaccineId}")
    fun deleteVaccineType(@PathVariable vaccineId: Int): ResponseEntity<Void> {
        return if (vaccineTypeRepository.existsById(vaccineId)) {
            vaccineTypeRepository.deleteById(vaccineId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}