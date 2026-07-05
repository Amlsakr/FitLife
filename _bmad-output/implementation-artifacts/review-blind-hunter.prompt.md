# Blind Hunter Code Review Prompt

You are an elite, adversarial code reviewer. Your goal is to find bugs, anti-patterns, and security risks in the provided code changes WITHOUT any project context or requirements. Treat the diff as the only source of truth.

## Instructions
1. Review the diff below.
2. Identify technical flaws, logic errors, concurrency issues, or poor coding practices.
3. Be cynical. Assume the code is broken until proven otherwise.
4. Format findings as a Markdown list. Each finding should have a one-line title and a brief explanation with code evidence.

## Diff to Review
(Note: Diff manually constructed from file contents)

### feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepository.kt
```kotlin
    override suspend fun deleteAccount(): Result<Unit, AuthError> {
        val authenticatedUser = when (val result = currentUser()) {
            is Result.Failure -> return Result.Failure(result.error)
            is Result.Success -> result.value ?: return Result.Failure(AuthError.NoAuthenticatedUser)
        }

        // Capture user data snapshot for potential restore
        analyticsLogger.logEvent("account_deletion_started", mapOf("uid" to authenticatedUser.id))
        val userDataSnapshot = try {
            userDataArchiveDataSource.snapshotUserData(authenticatedUser.id)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            return Result.Failure(FirebaseAuthExceptionMapper.map(throwable))
        }

        return try {
            userDataPurgeCoordinator.purgeUserData(authenticatedUser.id)
            userDocumentDataSource.deleteAuthenticatedUser(authenticatedUser.id)
            dataSource.deleteCurrentUser()
            try {
                googleCredentialStateDataSource.clearCredentialState()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                // Best effort only: the account has already been deleted, so
                // do not fail the request because credential cleanup failed.
            }
            analyticsLogger.logEvent("account_deletion", mapOf("uid" to authenticatedUser.id))
            Result.Success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            try {
                userDataArchiveDataSource.restoreUserData(userDataSnapshot)
            } catch (restoreCancellation: CancellationException) {
                throw restoreCancellation
            } catch (_: Throwable) {
                // Best effort: keep the original auth failure while restoring
                // only missing documents if we can.
            }
            analyticsLogger.logEvent("account_deletion_failed", mapOf("uid" to authenticatedUser.id, "error" to throwable.message))
            Result.Failure(FirebaseAuthExceptionMapper.map(throwable))
        }
    }
```

### feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseFirestoreUserDataPurgeContributor.kt
```kotlin
class FirebaseFirestoreUserDataPurgeContributor @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserDataPurgeContributor {
    override suspend fun purgeUserData(userId: String) {
        deleteCollection(
            firestore.collection(AuthDataConstants.FirestoreCollections.USERS)
                .document(userId)
                .collection(AuthDataConstants.FirestoreCollections.WORKOUT_PLANS)
        )
        deleteCollection(
            firestore.collection(AuthDataConstants.FirestoreCollections.USERS)
                .document(userId)
                .collection(AuthDataConstants.FirestoreCollections.SESSIONS)
        )
        deleteProgressDocs(userId)
    }

    private suspend fun deleteCollection(collection: com.google.firebase.firestore.CollectionReference) {
        while (true) {
            val snapshot = collection.limit(PAGE_SIZE.toLong()).get().await()
            if (snapshot.isEmpty) return

            firestore.batch().apply {
                snapshot.documents.forEach { document ->
                    delete(document.reference)
                }
            }.commit().await()
        }
    }

    private suspend fun deleteProgressDocs(userId: String) {
        while (true) {
            val snapshot = firestore.collection(AuthDataConstants.FirestoreCollections.PROGRESS)
                .whereEqualTo(AuthDataConstants.ProgressDocumentFields.USER_ID, userId)
                .limit(PAGE_SIZE.toLong())
                .get()
                .await()
            if (snapshot.isEmpty) return

            firestore.batch().apply {
                snapshot.documents.forEach { document ->
                    delete(document.reference)
                }
            }.commit().await()
        }
    }

    private companion object {
        const val PAGE_SIZE = 500
    }
}
```

### feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseFirestoreOwnedUserDataArchiveDataSource.kt
```kotlin
    override suspend fun snapshotUserData(userId: String): FirebaseOwnedUserDataSnapshot {
        val userDocument = firestore.collection(AuthDataConstants.FirestoreCollections.USERS)
            .document(userId)
            .get()
            .await()
            .takeIf { it.exists() }
            ?.toOwnedDocument()

        return FirebaseOwnedUserDataSnapshot(
            userDocument = userDocument,
            workoutPlans = snapshotCollection(
                firestore.collection(AuthDataConstants.FirestoreCollections.USERS)
                    .document(userId)
                    .collection(AuthDataConstants.FirestoreCollections.WORKOUT_PLANS)
            ),
            sessions = snapshotCollection(
                firestore.collection(AuthDataConstants.FirestoreCollections.USERS)
                    .document(userId)
                    .collection(AuthDataConstants.FirestoreCollections.SESSIONS)
            ),
            progressDocs = snapshotCollection(
                firestore.collection(AuthDataConstants.FirestoreCollections.PROGRESS)
                    .whereEqualTo(AuthDataConstants.ProgressDocumentFields.USER_ID, userId)
                    .get()
                    .await()
                    .documents
            )
        )
    }

    override suspend fun restoreUserData(snapshot: FirebaseOwnedUserDataSnapshot) {
        snapshot.userDocument?.let { restoreDocument(it) }
        snapshot.workoutPlans.forEach { restoreDocument(it) }
        snapshot.sessions.forEach { restoreDocument(it) }
        snapshot.progressDocs.forEach { restoreDocument(it) }
    }

    private suspend fun restoreDocument(document: FirebaseOwnedUserDocumentData) {
        val reference = firestore.document(document.path)
        if (!reference.get().await().exists()) {
            reference.set(document.data, SetOptions.merge()).await()
        }
    }
```
