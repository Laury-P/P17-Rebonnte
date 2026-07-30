package com.openclassrooms.rebonnte.data.medecine

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.History
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.data.model.MedicineDto
import com.openclassrooms.rebonnte.data.model.toDto
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseMedicineRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MedicineRepository {

    override fun getListAisles(): Flow<List<Aisle>> {
        return firestore.collection("aisles")
            .snapshots()
            .map { snapshots ->
                snapshots.documents.mapNotNull { documentSnapshot ->
                    documentSnapshot.toObject(Aisle::class.java)
                }
            }
    }

    override fun getListAllMedicine(): Flow<List<Medicine>> {
        return firestore.collection("medicines")
            .snapshots()
            .map { snapshots ->
                snapshots.documents.mapNotNull { documentSnapshot ->
                    documentSnapshot.toObject(Medicine::class.java)
                }
            }
    }

    override fun getAisleNameById(aisleId: String): Flow<Aisle> = flow {
        val snapshot = firestore.collection("aisles").document(aisleId).get().await()
        emit(snapshot.toObject(Aisle::class.java) ?: return@flow)
    }

    override fun getListMedicineByAisleId(aisleId: String): Flow<List<Medicine>> {
        return firestore.collection("medicines")
            .whereEqualTo("aisleId", aisleId)
            .snapshots()
            .map { snapshots ->
                snapshots.documents.mapNotNull { documentSnapshot ->
                    documentSnapshot.toObject(Medicine::class.java)
                }
            }
    }

    override fun getMedicineDetailById(medicineId: String): Flow<Medicine> =
        flow {
        val snapshot = firestore.collection("medicines").document(medicineId).get().await()
        val entity = snapshot.toObject(MedicineDto::class.java) ?: return@flow

        val historySnapshot = firestore.collection("medicines")
            .document(medicineId)
            .collection("histories")
            .get().await()
        val histories = historySnapshot.toObjects(History::class.java)

        emit(entity.toDomain(histories))
    }

    override suspend fun addAisle(aisle: Aisle): Result<Unit> = runCatching {
        firestore.collection("aisles").document(aisle.aisleId).set(aisle).await()
    }

    override suspend fun saveMedicine(medicine: Medicine, history: History): Result<Unit> = runCatching {
        val batch = firestore.batch()

        val medicineRef = firestore.collection("medicines").document(medicine.medicineId)
        batch.set(medicineRef, medicine.toDto())

        val historyRef = medicineRef.collection("histories").document()
        batch.set(historyRef, history)

        batch.commit().await()
    }

    override suspend fun deleteMedicine(medicineId: String): Result<Unit> = runCatching{
        val batch = firestore.batch()

        val medicineRef = firestore.collection("medicines").document(medicineId)

        val histories = medicineRef.collection("histories").get().await()
        histories.documents.forEach { history ->
            batch.delete(history.reference)
        }

        batch.delete(medicineRef)

        batch.commit().await()
    }

    override fun generateAisleId(): String = firestore.collection("aisles").document().id

    override fun generateMedicineId(): String = firestore.collection("medicines").document().id

}