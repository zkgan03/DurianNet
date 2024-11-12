package com.example.duriannet.data.repository.durian_dictionary

import com.example.duriannet.models.Durian
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DurianRepository {

    suspend fun getDurianDetails(durianId: String): Result<Durian> {
        return withContext(Dispatchers.IO) {
            // Implement your logic to fetch durian details here
            // For example, make a network request to fetch the durian details
            // Return Result.success(durian) if successful, or Result.failure(exception) if failed
        }
    }

    suspend fun getAllDurians(): Result<List<Durian>> {
        return withContext(Dispatchers.IO) {
            // Implement your logic to fetch all durians here
            // For example, make a network request to fetch the durians
            // Return Result.success(durianList) if successful, or Result.failure(exception) if failed
        }
    }
}